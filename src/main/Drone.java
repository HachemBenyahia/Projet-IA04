package main;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// classe Drone
public class Drone extends Agent
{
	private static final long serialVersionUID = 1L;
	
	// l'id du drone
	int m_id;
	
	// sa position actuelle (x, y)
	Position m_position;
	
	// l'objectif initial du drone (en terme de position à atteindre)
	Position m_goal;
	
	// l'état du drone
	Constants.State m_state;
	
	Map<Integer, Position> m_fleet = new TreeMap<Integer, Position>();
	
	long m_lastReception;
	
	protected void setup()
	{
		// on récupère les paramètres passés lors de sa création
		Object[] arguments = this.getArguments();

		m_lastReception = System.currentTimeMillis();
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		m_goal = (Position) arguments[2];
		m_state = Constants.State.ALONE;
		m_fleet.put(new Integer(m_id), m_position);
		
		addBehaviour(new RespondToDisplay(this));
		addBehaviour(new EmitEnvironment(this, Constants.m_emitEnvironmentPeriod));
		addBehaviour(new ReceiveEnvironment(this));
		addBehaviour(new Movement(this, Constants.m_movementPeriod));
	}

	// savoir si on est le master
	boolean isMaster()
	{
		if(((Integer) m_fleet.keySet().toArray()[0]).intValue() == m_id)
			return true;
		
		return false;
	}
	
	boolean isSecond()
	{
		if(((Integer) m_fleet.keySet().toArray()[1]).intValue() == m_id)
			return true;
		
		return false;
	}
	
	// méthode qui permet d'encoder les paramètres du drones au format JSON
	@SuppressWarnings("unchecked")
	String toJSONArray()
	{
		JSONArray args = new JSONArray();

		// on sérialise l'id
		JSONObject id = new JSONObject();
		id.put("id", m_id);
		args.add(id);
		
		// on sérialise la position
		JSONObject position = new JSONObject();
		position.put("x", m_position.getX());
		position.put("y", m_position.getY());
		args.add(position);
		
		JSONArray fleet = new JSONArray();
		for(Map.Entry<Integer, Position> entry : m_fleet.entrySet())
		{			
			// on sérialise l'id
			id = new JSONObject();
			id.put("id", entry.getKey());
			
			// on sérialise la position
			position = new JSONObject();
			position.put("x", entry.getValue().getX());
			position.put("y", entry.getValue().getY());
			
			JSONArray value = new JSONArray();
			value.add(id);
			value.add(position);
			fleet.add(value);
		}
		args.add(fleet);
		
		// on renvoie le JSON en string
		return args.toJSONString();
	}
	
	// méthode qui génère une case du terrain et l'affecte à l'objectif
	public void generateGoal()
	{
		m_goal.setPosition(Position.random());
	}

	// renvoit vrai si l'objectif du drone a été atteint
	public boolean reachedGoal()
	{
		if(m_position.equals(m_goal))
			return true;
		
		return false;
	}

	public Integer nextInFleet()
	{
		if(isMaster())
			return m_id;

		Object[] keys = m_fleet.keySet().toArray();

		for(int i = 0 ; i < keys.length ; i ++)
		{
			Integer key = (Integer) keys[i];
			if(m_id == key.intValue())
				return (Integer) keys[i - 1];
		}
		
		// erreur
		return -1;
	}
	
	public Position goalInFleet()
	{
		//int index = getIndexInFleet();
		//int size = m_fleet.size();
		
		// positionnement dans une structure en anneau
		
		//Position position = (Position) m_fleet.get(nextInFleet());
		if (this.isMaster())
		{
			return this.m_position;
		}
		
		double angle = (2*Math.PI)/m_fleet.size();
		angle /= this.getIndexInFleet();
		System.out.println("Angle for " + this.getIndexInFleet() + " : " + angle);
		
		int fleetRadius = getFleetRadius();
		
		Position position = m_fleet.get(m_fleet.keySet().toArray()[0]);
		System.out.println("Position a suivre : " + this.getIndexInFleet() + " : {" + position.m_x + ", " + position.m_y + "}");

		position.m_x += fleetRadius * Math.cos(angle);
		position.m_y -= fleetRadius * Math.sin(angle);
		System.out.println("Position for " + this.getIndexInFleet() + " : {" + position.m_x + ", " + position.m_y + "}");

		return position;
	}
	
	public int getFleetRadius()
	{
		if (!(this.m_fleet.size() <= Constants.m_droneNumberForMinRadius))
		{
			return Constants.m_minFleetRadius + m_fleet.size()* Constants.m_dotSize;
		}
		
		return Constants.m_minFleetRadius;
	}
	
	public boolean idIsMaster(int id)
	{
		Object[] keys = m_fleet.keySet().toArray();

		if(((Integer)keys[0]).intValue() == id)
			return true;
		
		return false;
	}
	
	public int getIndexInFleet()
	{
		Object[] keys = m_fleet.keySet().toArray();

		for(int i = 0 ; i < keys.length ; i ++)
		{
			Integer key = (Integer) keys[i];
			if(m_id == key.intValue())
				return i;
		}
		
		// erreur
		return -1;
	}
	
	public void updateMaster()
	{
		if((System.currentTimeMillis() - m_lastReception) > 3000)
		{
			m_fleet.remove(m_fleet.keySet().toArray()[0]);
		}
		
		//
		
	//	if(m_fleet.size() == 1)
		//	m_state = Constants.State.ALONE;
	}
}

// behaviour qui répond à Display quand il lui demande quelque chose
class RespondToDisplay extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Drone m_drone;

	public RespondToDisplay(Drone drone) 
	{
		m_drone = drone;
	}

	public void action() 
	{	
		ACLMessage message = m_drone.receive(MessageTemplate.MatchSender(new AID("Display", AID.ISLOCALNAME)));
		
		// si on a bien reçu un message de Display, on lui répond avec un INFORM
		// dans lequel on envoie les informations en question au format JSON
		if(message != null)
		{
			ACLMessage reply = message.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent(m_drone.toJSONArray());
			
			m_drone.send(reply);
		}
		else
			block();
	}
}

// behaviour qui émet des caractéristiques du drone en permanence
class EmitEnvironment extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;

	Drone m_drone;
	
	public EmitEnvironment(Agent agent, long period) 
	{
		super(agent, period);

		m_drone = (Drone) agent;
	}

	protected void onTick() 
	{
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		message.setContent(m_drone.toJSONArray());
		
		// on envoit à tous les drones sauf soi-même
		for(int i = 0 ; i < Constants.m_numberDrones ; i ++)
			if(i != m_drone.m_id)
				message.addReceiver(new AID("Drone" + i, AID.ISLOCALNAME));
		
		m_drone.send(message);
	}
}

// behaviour qui analyse les messages reçus des autres drones
class ReceiveEnvironment extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;

	Drone m_drone;
	
	public ReceiveEnvironment(Drone drone) 
	{
		m_drone = drone;
	}

	@SuppressWarnings("unchecked")
	public void action() 
	{
		ACLMessage message = m_drone.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		if(message != null)
		{
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());

			Position position = (Position) parameters.get("position");
			
			// le drone émetteur est proche
			if(m_drone.m_position.reachable(position))
			{	
				int id = (int) parameters.get("id");
				
				if(!m_drone.m_fleet.containsKey(new Integer(id)))
				{
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					reply.setContent(m_drone.toJSONArray());
					reply.addReceiver(new AID("Drone" + id, AID.ISLOCALNAME));
					m_drone.send(reply);
				}
				else
				{
					if(m_drone.idIsMaster(id))
						m_drone.m_lastReception = System.currentTimeMillis();
				}
				
				if(m_drone.m_position.equals(position))
					System.out.println("collision");
				
				Map<Integer, Position> fleet = (Map<Integer, Position>) parameters.get("fleet");
				System.out.println("Drone " + m_drone.m_id + " : " + m_drone.m_fleet + " next : " + m_drone.nextInFleet() 
				+ " " + m_drone.m_state.toString());
				
				switch(m_drone.m_state)
				{
					case ALONE :
						m_drone.m_state = Constants.State.FUSION;
						
						m_drone.m_fleet.putAll(fleet);
					break;
					
					case FUSION :
						m_drone.m_state = Constants.State.FLEET;
					break;
					
					case FLEET :
						m_drone.m_fleet.putAll(fleet);
						m_drone.m_fleet.replace(new Integer(id), position);
					break;
					
					default :
					break;
				}
			}
		}
	}	
}

// classe qui gère le mouvement d'un drone
class Movement extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;

	Drone m_drone;
	
	public Movement(Agent agent, long period) 
	{
		super(agent, period);

		m_drone = (Drone) agent;
	}

	protected void onTick() 
	{			
		switch(m_drone.m_state)
		{
			case ALONE :
				m_drone.m_position.moveTowards(m_drone.m_goal);
				
				if(m_drone.reachedGoal())
					m_drone.generateGoal();
			break;
			
			case FLEET :
				if(m_drone.isMaster())
				{
					if(m_drone.reachedGoal())
						m_drone.generateGoal();
				}
				else
				{
					m_drone.updateMaster();
					m_drone.m_goal = m_drone.goalInFleet();
				}

				m_drone.m_position.moveTowards(m_drone.m_goal);
			break;
			
			case FUSION :
			break;
		}
	}
}