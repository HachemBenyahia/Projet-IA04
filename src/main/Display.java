package main;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;

// L'agent display, qui est en charge de l'affichage et de l'actualisation de la GUI
/**
 * <b>Dipslay est l'agent qui s'occupe de gérer la logique fonctionnelle du système.</b>
 * <p>L'agent Display ne contient que l'attribut m_drones, la liste des positions de drones. </p>
 * <p>L'agent Display possède deux comportements :
 * RetrievePositions (le behaviour chargé de récupérer les positions des drones) et
 * DeathDetector (le behaviour chargé de récupérer les drones morts).</p>
 * 
 * @see Drone
 * @see GUI
 * @see RetrievePositions
 * @see	DeathDetector
 */
public class Display extends Agent
{
	private static final long serialVersionUID = 1L;
	// La mappe qui contient le nom du drone et sa position
	/**
	 * La mappe contenant les positions des drones, l'agent display s'occupe de fournir la liste actualisée de positions.
	 * 
	 * @see Display#getDrones
	*/
	Map<String, Position> m_drones = new HashMap<String, Position>();
	
	// La méthode d'initialisation de la classe Display
	/**
	 * C'est le constructeur de la classe, mais cette méthode s'occupe également de 
	 * demander la création de la GUI, des drones et d'affecter une position aléatoire à chaque drone.  
	 * 
	 * Dans cette méthode, on ajoute également les comportements de l'agent.
	 * 
	 * @see Constants.numberDrones 
 	 * @see Drone
	 * @see GUI
	 * @see RetrievePositions
	 * @see	DeathDetector
	*/
	protected void setup()
	{	
		// on crée les drones en leur affectant une position et un nom
		for( int i = 0 ; i < Constants.m_numberDrones ; i++ )
		{
			// Le nom est simplement la concaténation de "Drone" et de l'entier i
			String name = "Drone" + Integer.toString(i);
			
			// On calcule deux positions libres sur le terrain
			Position position = getFreePosition();
			Position goal = getFreePosition();	
			
			// Dans jade, au lieu de passer directement les arguments au constructeur,
			// il faut les passer au travers d'un tableau d'objets,
			// on passe l'identifiant et les positions
			Object[] arguments = {i, position, goal};

			try
			{
				// Création des drones
				this.getContainerController().createNewAgent(name, "main.Drone", arguments).start();
			}
			catch (StaleProxyException exception) 
			{
				exception.printStackTrace();
				System.exit(-1);
			}
			
			// on ajoute le drone créé à la map des drones
			m_drones.put(name, position);
		}
	
		// Création de l'interface graphique, on lui passe la mappe des drones pour qu'il initialise le terrain
		// et une référence sur Display
		// (je sais, c'est de la triche, mais c'est le seul moyen de faire fonctionner le truc)
		try
		{
			// paramètres à passer à l'agent GUI
			Object[] arguments = {m_drones, this};
			
			// création de l'agent GUI
			this.getContainerController().createNewAgent("GUI", "main.GUI", arguments).start();
		}
		catch (StaleProxyException exception) 
		{
			exception.printStackTrace();
			System.exit(-1);
		}
		
		// Le behaviour chargé de récupérer les positions des drones
		addBehaviour(new RetrievePositions(this, Constants.m_retrievePositionsPeriod));
		
		// Le behaviour chargé de récupérer les drones morts
		addBehaviour(new DeathDetector(this));
	}
	
	// une méthode pour mettre à jour la position d'un drone
	/**
	 * Permet d'actualiser la position d'un drone donné en entrée. 
	 * @param drone
	 * 	L'ID du drone dont la position on veut mettre à jour.
	 * @param position
	 * 	La nouvelle position du drone.
	 
	 * @see Position
	*/
	void updatePosition(String drone, Position position)
	{
		m_drones.get(drone).setPosition(position);
	}
	
	// Une méthode pour récupérer la map des drones (nom, position)
	/**
	 * Permet récupérer la liste actualisée des positions des drones.
	 * 
	 * @return la liste des positions actuelles des drones.
	*/
	Map<String, Position> getDrones()
	{
		return m_drones;
	}
	
	// Une méthode qui renvoie une position libre du terrain
	/**
	 * Permet d'obtenir une position libre dans le terrain.
	 * 
	 * @return Une position libre dans le terrain.
	 * 
	 * @see Position#random
	*/
	Position getFreePosition()
	{
		Position position = null;
		
		//Jusqu'à obtenir une position qui ne soit déjà occupée par un autre drone
		do
			{ position = Position.random(); }
		while(m_drones.containsValue(position));
		
		return position;
	}
} //Fin de la classe Display

// behaviour qui p�riodiquement update sa map de positions en questionnant les drones
/**
 * <b>RetrievePositions est le comportement de l'agent Display qui s'occupe de mettre à jour la liste avec les 
 * positions actuelles de drones, il s'agit d'un TickerBehaviour.</b>
 * <p>Pour effectuer sa tâche, la classe ne possède que deux champs : </p>
 * <ul>
 * 	<li>Une référence vers l'agent Display.</li>
 * 	<li>Le nombre de messages attendus.</li>
 * </ul>
 * 
 * @see Display
 * @see Constants#m_retrievePositionsPeriod
 */
class RetrievePositions extends TickerBehaviour 
{	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Une référence vers l'agent Display
	 * @see Display
	*/
	Display m_display = (Display) this.myAgent;
	
	// nombre de messages envoyés
	/**
	 * C'est le nombre de messages envoyés à chaque itération, c'est aussi le nombre de réponses attendues.
	 * @see RespondToDisplay
	*/
	int m_sent;
	
	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient et la période
	 * entre chaque exécution.
	 * 
	 * @param agent
	 * 		L'agent auquel le comportement appartient.
	 * @param period
	 * 		la période entre chaque exécution du comportement.
	 * 
	 * @see Constants#m_retrievePositionsPeriod
	 * @see Display
	*/
	public RetrievePositions(Agent agent, long period) 
	{
		// À l'initialisation du behaviour, on envoie un message à tous les drones
		super(agent, period);
		sendToAll();
	}

	// cette méthode se lance périodiquement
	/**
	 * C'est la méthode qui s'effectue périodiquement pour récupérer les messages des drones,
	 * si tous les drones ont emis une réponse, on rélance une requête a tous.
	 * 
	 * @see Constants#m_retrievePositionsPeriod
	*/
	public void onTick()
	{	
		ACLMessage message = m_display.receive( MessageTemplate.MatchPerformative(ACLMessage.INFORM) );
		
		// si l'on a recu un message de type INFORM (donc qui provient d'un drone puisque les messages INFORM
		// qui arrivent à Display viennent forcément d'un drone)
		if( message != null )
		{
			// on décrémente le nombre de messages attendus
			m_sent--;
			// on r�cup�re les param�tres du message JSON
			//Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());
			// le param�tre id
			//int id = (int) parameters.get("id");
			// le param�tre position
			//Position position = (Position) parameters.get("position");
			// affichage pour voir ce qui se passe
			// j'ai utilis� m�thode getDistance() pour montrer ce qu'elle donne
			// (m�thode qui sera utile pour le filtrage des messages des drones entre eux)
			//System.out.println(message.getSender().getLocalName() + " a envoy� " + position.toString());
			
			// si l'on recu toutes les réponses, on renvoie une load de message
			// (le paramètre m_sent est actualisé dans sendToAll())
			if(m_sent == 0)
				{ sendToAll(); }
		}
		else
			{ block(); }
	}
	
	// m�thode qui envoie une requ�te de demande de positions � tous les drones
	/**
	 * Permet d'envoyer une requête de position à tous les drones.
	 * 
	 * @see RespondToDisplay
	*/
	void sendToAll()
	{
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		
		for( int i = 0 ; i < m_display.m_drones.size() ; i ++ )
			{ message.addReceiver( new AID("Drone" + i, AID.ISLOCALNAME) ); }
		
		m_display.send(message);
		
		// on attend autant de messages qu'il y a de drones
		m_sent = m_display.m_drones.size();
	}
}

/**
 * <b>DeathDetector est le comportement de l'agent Display qui s'occupe d'identifier s'il y a des drones morts,
 * il s'agit d'un CyclicBehaviour.</b>
 * <p>Pour effectuer sa tâche, l'agent ne possède qu'un champ : une référence vers l'agent Display.</p>
 * 
 * @see Display
 * @see ReceiveEnvironment#action
 */
class DeathDetector extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Une référence vers l'agent Display
	 * @see Display
	*/
	Display m_display = (Display) this.myAgent;
	
	/**
	 * C'est le constructeur de la classe, il recoit en argument l'agent auquel il appartient.
	 * 
	 * @param agent
	 * 		L'agent auquel le comportement appartient.
	 * 
	 * @see Display
	*/
	public DeathDetector(Agent agent) 
	{
		super(agent);
	}
	
	/**
	 * C'est la méthode permet de récupérer les messages indiquant la mort d'un drone pour 
	 * le supprimer de la liste des drones.
	 * 
	 * @see ReceiveEnvironment#action
	*/
	public void action()
	{
		ACLMessage message = m_display.receive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE));
		if ( message != null )
		{
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());
			int id = (int) parameters.get("id");
			String name = "Drone"+Integer.toString(id);
			m_display.m_drones.remove(name);
		}
		else
			{ block(); }
	}
}

