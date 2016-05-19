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
	
	// la m�thode d'initialisation de la classe Display
	protected void setup()
	{	
		// on cr�e les drones en leur affectant une position et un nom
		for(int i = 0 ; i < Constants.numberDrones ; i++)
		{
			// le nom est simplement la concat�nation de "Drone" et de l'entier i
			String name = "Drone" + Integer.toString(i);
			
			// on calcule une position libre sur le terrain
			Position position = getFreePosition();	
			
			// dans jade, au lieu de passer par les arguments par le constructeur, il
			// faut les passer dans un tableau d'objets ; on passe l'identifiant i et
			// la position
			Object[] arguments = {i, position};

			try
			{
				// cr�ation des drones un � un
				this.getContainerController().createNewAgent(name, "main.Drone", arguments).start();
			}
			catch (StaleProxyException exception) 
			{
				exception.printStackTrace();
				System.exit(-1);
			}
			
			// on ajoute le drone cr�� dans la map des drones
			m_drones.put(name, position);
		}
	
		// cr�ation de l'interface graphique ; on lui passe la map des drones pour qu'elle initialise le terrain
		// et une r�f�rence sur Display (je sais, c'est de la triche, mais c'est le seul moyen de faire fonctionner
		// le truc)
		try
		{
			// param�tres � passer � l'agent GUI
			Object[] arguments = {m_drones, this};
			
			// cr�ation de l'agent GUI
			this.getContainerController().createNewAgent("GUI", "main.GUI", arguments).start();
		}
		catch (StaleProxyException exception) 
		{
			exception.printStackTrace();
			System.exit(-1);
		}
		
		// le behaviour charg� de r�cup�rer les positions des drones
		addBehaviour(new RetrievePositions(this, Constants.retrievePositionsPeriod));
	}
	
	// une m�thode pour mettre � jour la position d'un drone
	void updatePosition(String drone, Position position)
	{
		m_drones.get(drone).setPosition(position);
	}
	
	// une m�thode pour avoir la map des drones (nom, position)
	Map<String, Position> getDrones()
	{
		return m_drones;
	}
	
	// une m�thode qui renvoie une position libre ; notons que la m�thode renvoie une position multiple
	// de Constants.dotSize, car on veut que le terrain soit divis� en une grille de Constants.width x Constants.height
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

// behaviour qui p�riodiquement update sa map de positions en questionnant les drones
class RetrievePositions extends TickerBehaviour 
{	
	private static final long serialVersionUID = 1L;
	
	Display m_display = (Display) this.myAgent;
	
	// nombre de messages envoy�s
	int m_sent;

	public RetrievePositions(Agent agent, long period) 
	{
		super(agent, period);
		
		// � l'initialisation du behaviour, on envoie un message � tous les drones
		sendToAll();
	}

	// cette m�thode se lance p�riodiquement
	public void onTick()
	{	
		ACLMessage message = m_display.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		// si on a re�u un message de type INFORM (donc qui provient d'un drone puisque les messages INFORM
		// qui arrivent � Display viennent forc�ment d'un drone)
		if(message != null)
		{
			// on d�cr�mente le nombre de messages attendus
			m_sent --;
			
			// on r�cup�re les param�tres du message JSON
			Map<String, Object> parameters = Constants.fromJSONArray(message.getContent());
			
			// le param�tre id
			int id = (int) parameters.get("id");
			
			// le param�tre position
			Position position = (Position) parameters.get("position");
			
			// affichage pour voir ce qui se passe
			// j'ai utilis� m�thode getDistance() pour montrer ce qu'elle donne
			// (m�thode qui sera utile pour le filtrage des messages des drones entre eux)
			System.out.println(message.getSender().getLocalName() + " a envoy� " + position.toString());
			System.out.println("Distance du drone " + id + " � l'origine : " 
			+ m_display.m_drones.get("Drone" + id).getDistance(new Position(0, 0)));
			
			// si on re�u toutes les r�ponses, on renvoie une load de message
			// (le param�tre m_sent est actualis� dans sendToAll())
			if(m_sent == 0)
				sendToAll();
		}
		else
			block();
	}
	
	// m�thode qui envoie une requ�te de demande de positions � tous les drones
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