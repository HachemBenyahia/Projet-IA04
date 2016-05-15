package main;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sdljava.SDLException;

// l'agent display, qui est en charge de l'affichage et de son actualisation (GUI = Graphical User Interface = interface graphique)
public class Display extends Agent
{
	private static final long serialVersionUID = 1L;
	
	// la map qui contient le nom du drone et sa position (la position est un couple (x,y))
	// par exemple ["Drone1"] = (232, 105)
	Map<String, Position> m_drones = new HashMap<String, Position>();
	
	// l'attribut de l'interface graphique ; la librairie utilisée est sdljava, qui nécessite que
	// le code de la gui se trouve dans une classe à part (pas un agent)
	GUI m_gui = null;
	
	
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
				this.getContainerController().createNewAgent(name, "main.Drone", arguments).start();
			}
			catch(Exception exception)
			{
				System.out.println("Erreur : " + exception.getMessage());
				System.exit(-1);	
			}
			
			// on ajoute le drone créé dans la map des drones
			m_drones.put(name, position);
		}
	
		// création de l'interface graphique ; on lui passe la map des drones pour qu'elle initialise le terrain
		try
		{
			m_gui = new GUI(m_drones);
		}
		catch (SDLException | InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		// les deux behaviours que gère la classe Display : un behaviour qui envoie des messages périodiquement
		// aux autres drones et un behaviour qui met à jour périodiquement également (les périodes peuvent être
		// différentes) l'interface graphique
		addBehaviour(new RetrievePositions(this, Constants.retrievePositionsPeriod));
		addBehaviour(new UpdateGUI(this, Constants.updateGUIPeriod));
	}
	
	// une méthode pour avoir la map des drones (nom, position)
	Map<String, Position> getDrones()
	{
		return m_drones;
	}
	
	// une méthode qui renvoie une position libre ; notons que la méthode renvoie une position multiple
	// de Constants.dotSize, car on veut que le terrain soit divisé en une grille de Constants.environmentWidth x Constants.environmentHeight
	// cellules dont chaque cellule fait Constants.dotSize pixels de hauteur et largeur
	Position getFreePosition()
	{
		int x, y;
		Position position = new Position(0, 0);
		
		do
		{
			x = (int) Math.floor(Math.random() * Constants.environmentWidth) * Constants.dotSize;
			y = (int) Math.floor(Math.random() * Constants.environmentHeight) * Constants.dotSize;

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
	
	Display display = (Display) this.myAgent;
	Map<String, Position> drones = display.getDrones();
	
	public RetrievePositions(Agent agent, long period) 
	{
		super(agent, period);
	}

	public void onTick()
	{	
		System.out.println("retrievePositions");
	}
}

// behaviour qui périodiquement met à jour l'interface graphique via le paramètre m_gui
class UpdateGUI extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Display display = (Display) this.myAgent;
	
	public UpdateGUI(Agent agent, long period)
	{
		super(agent, period);	
	}

	protected void onTick() 
	{
		System.out.println("updateGUI");
	}
}