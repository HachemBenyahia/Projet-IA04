package main;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// classe Drone
/**
 * <b>Drone est la classe représentant un individu de la flotte.</b>
 * <p>Un membre de la classe Drone est caractérisé par les informations suivantes : </p>
 * <ul>
 * 	<li>Un identifiant unique.</li>
 * 	<li>Sa position actuelle.</li>
 *	<li>La position que le drone veut atteindre.</li>
 * 	<li>L'état du drone.</li>
 * 	<li>Un Map qui contient la liste des membres de la flotte.</li>
 * </ul>
 * 
 * @see Constants#State
 * @see RespondToDisplay
 * @see EmitEnvironment
 * @see	ReceiveEnvironment
 * @see Movement
 */
public class Drone extends Agent
{
	private static final long serialVersionUID = 1L;
	
	// l'id du drone
	/**
	 * L'ID du drone, c'est fixé pendant l'initialisation du drone, il sert aussi pour savoir si le drone est un maître ou pas.
	 * @see Drone#isMaster
	*/
	int m_id;
	
	// sa position actuelle (x, y)
	
	/**
	 * C'est la position où le drone se trouve à chaque instant.
	 * 
	 * @see Position
	*/
	Position m_position;
	
	// l'objectif initial du drone (en terme de position à atteindre)
	/**
	 * C'est la position que le drone veut attaindre.
	 * @see Position
	*/
	Position m_goal;
	
	// l'état du drone
	/**
	 * C'est l'état actuel du drone par rapport à la flotte : seul, flotte, fusion, etc.
	 * Pour connaître tous les possibles états d'un drone, regardez la documentation de Constants.State
	 * @see Constants#State 
	 * @see Drone#goalInFleet
	*/
	Constants.State m_state;
	
	/**
	 * C'est la liste de drones qu'appartiennent à la flotte de ce drone|, y compris la position actuele de chaqu'un.
	 * Le maître de la flotte est l'élément 0 de la liste.
	 * 
	 * @see Drone#idIsMaster
	*/
	Map<Integer, Position> m_fleet = new TreeMap<Integer, Position>();
	
	/**
	 * C'est un nombre indicant le moment de la dernière réception d'un message.
	*/
	long m_lastReception;
	
	/**
	 * C'est un boolean indicant si ce drone est toujours vivant.
	*/
	boolean m_alive;
	
	/**
	 * C'est le constructeur de la classe, tout drone créé est initialisé comme ALONE,
	 * sa dernière réception prend la valeur de l'instant de création, la position de destin et d'origine
	 * doivent être fournies pendant la création du drone,
	 * ce drone est le premier élément ajouté à la liste de membres de la flotte.
	 * 
	 * Dans cette méthode, on ajoute également les comportements du drone.
	 * 
	 * @param m_lastReception
	 * 		Instant de la dernière réception d'un message
	 * @param m_id
	 * 		ID de ce drone
	 * @param m_position
	 * 		Position initial de ce drone
	 * @param m_goal
	 * 		Position destin originale de ce drone
	 * @see Position
	 * @see Constants.State
	 * @see RespondToDisplay
	 * @see EmitEnvironment
	 * @see	ReceiveEnvironment
	 * @see Movement
	*/
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
		m_alive = true;
		
		addBehaviour(new RespondToDisplay(this));
		addBehaviour(new EmitEnvironment(this, Constants.m_emitEnvironmentPeriod));
		addBehaviour(new ReceiveEnvironment(this));
		addBehaviour(new Movement(this, Constants.m_movementPeriod));
	}

	// savoir si l'on est le master
	/**
	 * Vérifie si l'on est le maître.
	 * @return La valeur de la vérification.
	 * @see Drone#m_fleet
	*/
	boolean isMaster()
	{
		if(((Integer) m_fleet.keySet().toArray()[0]).intValue() == m_id)
			return true;
		
		return false;
	}
	
	/**
	 * Vérifie si l'on est le deuxième.
	 * @return La valeur de la vérification.
	 * @see Drone#m_fleet
	*/
	boolean isSecond()
	{
		if(((Integer) m_fleet.keySet().toArray()[1]).intValue() == m_id)
			return true;
		
		return false;
	}
	
	// méthode qui permet d'encoder les paramètres du drones au format JSON
	/**
	 * Retourne le drone sous forme d'une chaîne JSON.
	 * @return La chaîne JSON contenant l'information de ce drone.
	*/
	@SuppressWarnings("unchecked")
	String toJSONArray()
	{
		//Objet qui contiendra la sérialisation du drone
		JSONArray args = new JSONArray();

		// on sérialise l'id
		JSONObject id = new JSONObject();
		id.put("id", m_id);
		args.add(id);
		
		// on sérialise la position dans un objet JSON  position et après on rajoute cet objet à args
		JSONObject position = new JSONObject();
		position.put("x", m_position.getX());
		position.put("y", m_position.getY());
		args.add(position);
		
		//On rajoute chaque drone de la flotte à la sérialisation
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
	/**
	 * Permet d'affecter le drone à un destin (Position) sélectioné aléatoirement.
	 * @see Position#random
	*/
	public void generateGoal()
	{
		m_goal.setPosition(Position.random());
	}

	// renvoit vrai si l'objectif du drone a été atteint
	/**
	 * Permet savoir si l'on a atteint le destin, i.e., si la position actuelle est égale à la position destin.
	 * @return Le résultat de la comparaison
	 * @see Drone#m_position
	 * @see Drone#m_goal
	*/
	public boolean reachedGoal()
	{
		if(m_position.equals(m_goal))
			return true;
		
		return false;
	}
	
	/**
	 * Permet de savoir quelle est le drone qui se trouve avant nous dans la liste de la flotte.
	 * Si l'on est le maître elle retourne notre propre ID.
	 * @return L'ID du drone qui se trouve avant nous dans la liste.
	 * @see Drone#m_fleet
	*/
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
	
	/**
	 * Permet de savoir la position destin de ce drone
	 * Si l'on est le maître elle retourne la position destin originale, c'est-à-dire, comme si le dron était tout seul.
	 * Si l'on n'est pas le maìtre, on calcule la position destin en fonction de la position destin du maître de la flotte.
	 
	 * @return La prochaine destin de ce drone.
	 * @see Drone#m_goal
	 * @see isMaster
	*/
	public Position goalInFleet()
	{
		//int index = getIndexInFleet();
		//int size = m_fleet.size();
		
		// Éventuellement se positionner dans une structure en anneau
		
		Position position = (Position) m_fleet.get(nextInFleet());

		return position;
	}
	
	/**
	 * Permet de savoir si un ID donné correspond avec celui de notre maître.
	 * @param id
	 * 	L'ID que l'on veut comparer avec celui de notre maître
	 * @return Le résultat de la comparaison
	 
	 * @see isMaster
	*/
	public boolean idIsMaster(int id)
	{
		Object[] keys = m_fleet.keySet().toArray();

		if(((Integer)keys[0]).intValue() == id)
			return true;
		
		return false;
	}
	
	/**
	 * Permet de connaître l'index que l'on occupe dans la liste d'éléments de la flotte.
	 * @return L'index que l'on occupe parmi les éléments de la flotte.
	*/
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
	
	/**
	 * Permet de remplacer le maître si le temps passé est supérieur à 3000 millisecondes.
	*/
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
		if (!m_drone.m_alive) return;
		
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
class ReceiveEnvironment extends Behaviour
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
			
			// le drone �metteur est proche
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
				{
					System.out.println("collision");
					ACLMessage deathMessage = new ACLMessage(ACLMessage.FAILURE);
					deathMessage.addReceiver(new AID("Display", AID.ISLOCALNAME));
					deathMessage.setContent(m_drone.toJSONArray());
					m_drone.m_alive = false;
					m_drone.send(deathMessage);
					return;
				}
				
				Map<Integer, Position> fleet = (Map<Integer, Position>) parameters.get("fleet");
				//System.out.println("Drone " + m_drone.m_id + " : " + m_drone.m_fleet + " next : " + m_drone.nextInFleet() 
				//+ " " + m_drone.m_state.toString());
				
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
	
	public boolean done() {
		return !m_drone.m_alive;
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
		if (!m_drone.m_alive) return;
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
