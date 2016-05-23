package main;

import java.util.Map;

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
	
	boolean isMaster = false;
	// identifiant du maitre
	AID masterId;
	// position du maitre
	Position masterPosition;
	
	// l'objectif initial du drone (en terme de position � atteindre)
	Position m_goal;
	
	protected void setup()
	{
		// on r�cup�re les param�tres pass�s lors de sa cr�ation
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		m_goal = (Position) arguments[2];
		
		addBehaviour(new RespondToDisplay(this));
		addBehaviour(new EmitEnvironment(this, Constants.m_emitEnvironmentPeriod));
		addBehaviour(new ReceiveEnvironment(this));
		addBehaviour(new Movement(this, Constants.m_movementPeriod));
	}
	
	// m�thode qui permet d'encoder les param�tres du drones au format JSON
	@SuppressWarnings("unchecked")
	String toJSONArray()
	{
		JSONArray args = new JSONArray();

		// on s�rialise l'id
		JSONObject id = new JSONObject();
		id.put("id", m_id);
		
		// on s�rialise la position
		JSONObject position = new JSONObject();
		position.put("x", m_position.getX());
		position.put("y", m_position.getY());
		
		JSONObject ismaster = new JSONObject();
		ismaster.put("isMaster", String.valueOf(this.isMaster));
		// on ajoute les deux dans le tableau d'objets JSON
		args.add(id);
		args.add(position);
		args.add(ismaster);
		
		// on renvoie le JSON en string
		return args.toJSONString();
	}
	
	// m�thode qui g�n�re une case du terrain et l'affecte � l'objectif
	public void generateGoal()
	{
		m_goal.setPosition(Position.random());
	}

	// renvoit vrai si l'objectif du drone a �t� atteint
	public boolean reachedGoal()
	{
		if(m_position.equals(m_goal))
			return true;
		
		return false;
	}
}

// behaviour qui r�pond � Display quand il lui demande quelque chose
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
		
		// si on a bien re�u un message de Display, on lui r�pond avec un INFORM
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

// behaviour qui �met des caract�ristiques du drone en permanence
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
		
		// on envoit � tous les drones sauf soi-m�me
		for(int i = 0 ; i < Constants.m_numberDrones ; i ++)
			if(i != m_drone.m_id)
				message.addReceiver(new AID("Drone" + i, AID.ISLOCALNAME));
		
		m_drone.send(message);
	}
}

// behaviour qui analyse les messages re�us des autres drones
class ReceiveEnvironment extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;

	Drone m_drone;
	
	public ReceiveEnvironment(Drone drone) 
	{
		m_drone = drone;
	}

	public void action() 
	{
		ACLMessage message = m_drone.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		if(message != null)
		{
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());

			int id = (int) parameters.get("id");
			Position position = (Position) parameters.get("position");
			
			// le drone �metteur est proche
			if(m_drone.m_position.reachable(position))
			{
				// � coder, en fonction de certains param�tres il faudra faire une chose ou une autre
				// voir cahier des charges pour les diff�rents cas possibles comment traiter
				// chacun d'entre eux
				
				System.out.println("Drone " + m_drone.m_id + " a d�tect� le drone " + id);
			}
		}
	}	
}

// classe qui g�re le mouvement d'un drone
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
		// par d�faut (pour l'instant) on va consid�rer qu'il bouge en permanence vers un objectif sur le terrain ;
		// en pratique, ca d�pendra de son statut (mort, faisant partie d'une flotte, seul, etc)
		m_drone.m_position.moveTowards(m_drone.m_goal);
		
		if(m_drone.reachedGoal())
			m_drone.generateGoal();
	}
}