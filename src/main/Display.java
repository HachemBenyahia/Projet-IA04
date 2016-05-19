package main;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;

// l'agent display, qui est en charge de l'affichage et de son actualisation (GUI = Graphical User Interface = interface graphique)
public class Display extends Agent
{
	private static final long serialVersionUID = 1L;
	
	// la map qui contient le nom du drone et sa position (la position est un couple (x,y))
	// par exemple ["Drone1"] = (232, 105)
	Map<String, Position> m_drones = new HashMap<String, Position>();
	
	// la méthode d'initialisation de la classe Display
	protected void setup()
	{	
		// on crée les drones en leur affectant une position et un nom
		for(int i = 0 ; i < Constants.numberDrones ; i++)
		{
			// le nom est simplement la concaténation de "Drone" et de l'entier i
			String name = "Drone" + Integer.toString(i);
			
			// on calcule une position libre sur le terrain
			Position position = getFreePosition();	
			
			// dans jade, au lieu de passer par les arguments par le constructeur, il
			// faut les passer dans un tableau d'objets ; on passe l'identifiant i et
			// la position
			Object[] arguments = {i, position};

			try
			{
				// création des drones un à un
				this.getContainerController().createNewAgent(name, "main.Drone", arguments).start();
			}
			catch (StaleProxyException exception) 
			{
				exception.printStackTrace();
				System.exit(-1);
			}
			
			// on ajoute le drone créé dans la map des drones
			m_drones.put(name, position);
		}
	
		// création de l'interface graphique ; on lui passe la map des drones pour qu'elle initialise le terrain
		// et une référence sur Display (je sais, c'est de la triche, mais c'est le seul moyen de faire fonctionner
		// le truc)
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
		
		// le behaviour chargé de récupérer les positions des drones
		addBehaviour(new RetrievePositions(this, Constants.retrievePositionsPeriod));
	}
	
	// une méthode pour mettre à jour la position d'un drone
	void updatePosition(String drone, Position position)
	{
		m_drones.get(drone).setPosition(position);
	}
	
	// une méthode pour avoir la map des drones (nom, position)
	Map<String, Position> getDrones()
	{
		return m_drones;
	}
	
	// une méthode qui renvoie une position libre ; notons que la méthode renvoie une position multiple
	// de Constants.dotSize, car on veut que le terrain soit divisé en une grille de Constants.width x Constants.height
	// cellules dont chaque cellule fait Constants.dotSize pixels de hauteur et largeur
	Position getFreePosition()
	{
		int x, y;
		Position position = new Position(0, 0);
		
		do
		{
			x = (int) Math.floor(Math.random() * Constants.width) * Constants.dotSize;
			y = (int) Math.floor(Math.random() * Constants.height) * Constants.dotSize;

			position.setPosition(x, y);
		}
		while(m_drones.containsValue(position));
		
		return position;
	}
}

// behaviour qui périodiquement update sa map de positions en questionnant les drones
class RetrievePositions extends TickerBehaviour 
{	
	private static final long serialVersionUID = 1L;
	
	Display m_display = (Display) this.myAgent;
	
	// nombre de messages envoyés
	int m_sent;

	public RetrievePositions(Agent agent, long period) 
	{
		super(agent, period);
		
		// à l'initialisation du behaviour, on envoie un message à tous les drones
		sendToAll();
	}

	// cette méthode se lance périodiquement
	public void onTick()
	{	
		ACLMessage message = m_display.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		// si on a reçu un message de type INFORM (donc qui provient d'un drone puisque les messages INFORM
		// qui arrivent à Display viennent forcément d'un drone)
		if(message != null)
		{
			// on décrémente le nombre de messages attendus
			m_sent --;
			
			// on récupère les paramètres du message JSON
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());
			
			// le paramètre id
			int id = (int) parameters.get("id");
			
			// le paramètre position
			Position position = (Position) parameters.get("position");
			
			// affichage pour voir ce qui se passe
			// j'ai utilisé méthode getDistance() pour montrer ce qu'elle donne
			// (méthode qui sera utile pour le filtrage des messages des drones entre eux)
			System.out.println(message.getSender().getLocalName() + " a envoyé " + position.toString());
			System.out.println("Distance du drone " + id + " à l'origine : " 
			+ m_display.m_drones.get("Drone" + id).getDistance(new Position(0, 0)));
			
			// si on reçu toutes les réponses, on renvoie une load de message
			// (le paramètre m_sent est actualisé dans sendToAll())
			if(m_sent == 0)
				sendToAll();
		}
		else
			block();
	}
	
	// méthode qui envoie une requête de demande de positions à tous les drones
	void sendToAll()
	{
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		
		for(int i = 0 ; i < m_display.m_drones.size() ; i ++)
			message.addReceiver(new AID("Drone" + i, AID.ISLOCALNAME));
		
		m_display.send(message);
		
		// on attend autant de messages qu'il y a de drones
		m_sent = m_display.m_drones.size();
	}
}