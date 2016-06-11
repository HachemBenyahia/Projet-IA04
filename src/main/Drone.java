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
 * 	<li>Une mappe qui contient la liste des membres de la flotte.</li>
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
	 * Permet savoir quelle est le drone qui se trouve avant nous dans la liste de la flotte.
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
	 * Permet savoir la position destin de ce drone
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
	 * Permet savoir si un ID donné correspond avec celui de notre maître.
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
	 * Permet connaître l'index que l'on occupe dans la liste d'éléments de la flotte.
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
	 * Permet remplacer le maître si le temps passé est supérieur à 3000 millisecondes.
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

// behaviour qui répond à l'agent Display quand il fait une requête
/**
 * <b>RespondToDisplay est le comportement d'un agent Drone qui s'occupe de répondre aux requêtes de l'agent
 * Display en lui envoyant les informations du drone, il s'agit d'un CyclicBehaviour.</b>
 * <p>Pour effectuer sa tâche, la classe ne possède qu'un champ : une référence vers l'agent auquel il appartient.</p>
 * 
 * @see Drone
 * @see RetrievePositions 
 */
class RespondToDisplay extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Une référence vers l'agent Drone auquel il appartient.
	*/
	Drone m_drone;

	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient.
	 * 
	 * @param drone
	 * 		L'agent drone auquel le comportement appartient.
	 * 
	 * @see Drone
	*/
	public RespondToDisplay(Drone drone) 
	{
		m_drone = drone;
	}
	
	/**
	 * C'est la méthode permet de répondre aux requêtes de l'agent Display et de lui envoyer les informations du drone. 
	 * 
	 * @see RetrievePositions
	 * @see Drone#toJSONArray
	*/
	public void action() 
	{	
		ACLMessage message = m_drone.receive(MessageTemplate.MatchSender(new AID("Display", AID.ISLOCALNAME)));
		
		// Si l'on a bien recu un message de Display, on lui répond avec un INFORM
		// dans lequel on envoie les informations sous format JSON
		if( message != null )
		{
			ACLMessage reply = message.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent(m_drone.toJSONArray());
			
			m_drone.send(reply);
		}
		else
			{ block(); }
	}
}

// behaviour qui �met des caract�ristiques du drone en permanence
/**
 * <b>EmitEnvironment est le comportement d'un agent Drone qui s'occupe d'envoyer périodiquement
 * ses coordonnées aux autres drones, il s'agit d'un TickerBehaviour.</b>
 * <p>Pour effectuer sa tâche, la classe ne possède qu'un champ : une référence vers l'agent auquel il appartient.</p>
 * 
 * @see Drone
 * @see Constants#m_emitEnvironmentPeriod
 * @see RetrievePositions
 * @see ReceiveEnvironment
 */
class EmitEnvironment extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * C'est une référence vers l'agent drone auquel ce comportement appartient.
	*/
	Drone m_drone;
	
	
	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient et la période
	 * entre chaque exécution.
	 * 
	 * @param agent
	 * 		L'agent auquel le comportement appartient.
	 * @param period
	 * 		la période entre chaque exécution du comportement.
	 * 
	 * @see Constants#m_emitEnvironmentPeriod
	 * @see Drone
	 * @see ReceiveEnvironment
	*/
	public EmitEnvironment(Agent agent, long period) 
	{
		super(agent, period);
		m_drone = (Drone) agent;
	}
	
	/**
	 * C'est la méthode qui s'effectue périodiquement pour envoyer les informations du drones
	 * aus autres drones. Si le drone n'est plus vivant, aucune action n'est effectuée.
	 * 
	 * @see Constants#m_emitEnvironmentPeriod
	 * @see Drone#toJSONArray
	*/
	protected void onTick() 
	{
		if ( !m_drone.m_alive )
			{ return; }
		
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.setContent(m_drone.toJSONArray());
		
		// Il envoit ses informations à tous les drones, sauf à soi-même
		for( int i = 0 ; i < Constants.m_numberDrones ; i ++ )
			if( i != m_drone.m_id )
				{ message.addReceiver(new AID("Drone" + i, AID.ISLOCALNAME)); }
		
		m_drone.send(message);
	}
}

// behaviour qui analyse les messages recus des autres drones
/**
 * <b>ReceiveEnvironment est le comportement de l'agent drone qui s'occupe de mettre à jour la liste des 
 * positions actuelles de drones de la flotte, ainsi que l'état du drone. Il s'agit d'un Behaviour simple.</b>
 * <p>Pour effectuer sa tâche, la classe ne possède qu'un champ : Une référence vers l'agent drone auquel il appartien.</p>
 * 
 * @see Display
 * @see EmitEnvironment
 */
class ReceiveEnvironment extends Behaviour
{
	private static final long serialVersionUID = 1L;

	/**
	 * C'est une référence vers l'agent drone auquel ce comportement appartient.
	*/
	Drone m_drone;
	
	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient.
	 * 
	 * @param drone
	 * 		L'agent drone auquel le comportement appartient.
	 * 
	 * @see Drone
	*/
	public ReceiveEnvironment(Drone drone) 
	{
		m_drone = drone;
	}

	@SuppressWarnings("unchecked")
	/**
	 * C'est la méthode permet d'actualiser la liste des positions des autres drones de la flotte
	 * et l'état du drone, également on peut rajouter un drone à la flotte s'il est assez proche.
	 * Finalement, cette méthode permet aussi de déterminer s'il y a eu une colision entre deux drones.
	 * 
	 * @see EmitEnvironment
	 * @see DeathDetector 
	 * @see Drone#toJSONArray
	*/
	public void action() 
	{
		ACLMessage message = m_drone.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		if( message != null )
		{
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());

			Position position = (Position) parameters.get("position");
			
			// le drone émetteur est proche
			if( m_drone.m_position.reachable(position) )
			{	
				// On recupère l'ID du drone émetteur
				int id = (int) parameters.get("id");
				
				//S'il ne fait pas partie de la flotte, on lui envoie une réponse.
				if(!m_drone.m_fleet.containsKey(new Integer(id)))
				{
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					reply.setContent(m_drone.toJSONArray());
					reply.addReceiver(new AID("Drone" + id, AID.ISLOCALNAME));
					m_drone.send(reply);
				}
				
				//S'il fait déjà partie de la flotte
				else
				{
					//S'il est le maître, on récupère le temps de la réception
					if(m_drone.idIsMaster(id))
						{ m_drone.m_lastReception = System.currentTimeMillis(); } 
				}
				
				//S'il y a eu une colision, on envoie un message et on meurt x_x
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
				
				// On recupère la flotte du drone émetteur
				Map<Integer, Position> fleet = (Map<Integer, Position>) parameters.get("fleet");
				//System.out.println("Drone " + m_drone.m_id + " : " + m_drone.m_fleet + " next : " + m_drone.nextInFleet() 
				//+ " " + m_drone.m_state.toString());
				
				switch(m_drone.m_state)
				{
					//Si le drone était seul, on commence une fusion
					case ALONE :
						m_drone.m_state = Constants.State.FUSION;
						m_drone.m_fleet.putAll(fleet);
						break;
					
					//Si le drone était en train de se fusioner, il devient partie de la flotte
					case FUSION :
						m_drone.m_state = Constants.State.FLEET;
						break;
					
					//Si le drone faisait déjà partie de la flotte, on remplace la position ancienne par la nouvelle
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
	
	//Ce comportement s'effectue pendant que le drone soit vivant
	/**
	 * Cette fonction permet de savoir si le comportement s'est déjà terminé,
	 * le comportement se finit lorsque le drone meurt.
	 * 
	 * @return Un boléean indiquant si le comportement s'est terminé.
	*/
	public boolean done()
	{
		return !m_drone.m_alive;
	}
}

// classe qui gère le mouvement d'un drone
/**
 * <b>Movement est le comportement d'un agent Drone qui s'occupe de
 * modifier périodiquement sa position d'après l'état du drone, il s'agit donc d'un TickerBehaviour.</b>
 * <p>Pour effectuer sa tâche, la classe ne possède qu'un champ : une référence vers l'agent auquel il appartient.</p>
 * 
 * @see Drone
 * @see Constants#State
 * @see Constant#m_movementPeriod
 */
class Movement extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * C'est une référence vers l'agent drone auquel ce comportement appartient.
	*/
	Drone m_drone;
	
	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient et la période
	 * entre chaque exécution.
	 * 
	 * @param agent
	 * 		L'agent auquel le comportement appartient.
	 * @param period
	 * 		la période entre chaque exécution du comportement.
	 * 
	 * @see Constants#m_movementPeriod
	 * @see Drone
	*/
	public Movement(Agent agent, long period) 
	{
		super(agent, period);
		m_drone = (Drone) agent;
	}
	
	/**
	 * C'est la méthode qui s'effectue périodiquement pour modifier sa position d'après l'état du drone.
	 * Si le drone n'est plus vivant, aucune action n'est effectuée. Un drone qui est seul déambulera librement
	 * dans le terrain, alors qu'un drone qui appartient à une flotte suivra à son maître, finalement, un maître 
	 * agira comme un drone seul.
	 * 
	 * @see Drone
	 * @see Constants#State
	 * @see Constant#m_movementPeriod
	*/
	protected void onTick() 
	{	
		//Si le drone n'est plus vivant, on ne fait rien
		if (!m_drone.m_alive)
			{ return; }
			
		switch(m_drone.m_state)
		{
			// Si le drone est seul, il se déplace vers son objectif
			case ALONE :
				m_drone.m_position.moveTowards(m_drone.m_goal);
				// Si le drone a achevé son objectif, on génère un nouveau
				if(m_drone.reachedGoal())
					{ m_drone.generateGoal(); } 
				break;
			
			// Si le drone appartient à une flotte
			case FLEET :
				// Si le drone est le maître 
				if(m_drone.isMaster())
				{
					// S'il a achevé son objectif, on génère un nouveau
					if(m_drone.reachedGoal())
						m_drone.generateGoal();
				}
				// sinon 
				else
				{
					// On actualise le maître et on actualise l'objectif
					m_drone.updateMaster();
					m_drone.m_goal = m_drone.goalInFleet();
				}

				m_drone.m_position.moveTowards(m_drone.m_goal);
			break;
			
			// Sinon, on ne fait rien
			case FUSION :
				break;
			default:
				break;
		}
	}
}

