package main;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jade.core.Agent;
import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;

// classe utilisant la librairie sdljava pour représente les drones graphiquement
// je ne vais pas trop détailler le fonctionnement, il faut regarder la doc pour comprendre
// comment elle fonctionne, mais elle n'est pas très compliquée à utiliser
/**
 * <b>GUI est la classe qui s'occupe de la répresentation graphique des drones.</b>
 * <p>Cette classe utilise la bibliothèque sldjava pour afficher la grille,
 * les drones et le reste de composants de la modélisation, cet agent recoit périodiquement
 * des message de la part de l'agent Display avec les nouvelles positions des drones et d'autres événements à afficher.</p>
 * <p>Pour mener à bien son travail, l'agent GUI dispose de suivantes composantes :</p>
 * <ul>
 * 	<li>L'écran principal.</li>
 * 	<li>La mappe contenant les positions des drones.</li>
 * 	<li>La mappe contenant la couleur associée à chaque drone.</li>
 * 	<li>La mappe contenant la surface associée à chaque drone.</li>
 * 	<li>La mappe contenant les positions précédentes des drones.</li>
 * 	<li>La queue contenant la liste de drones supprimés.</li>
 * 	<li>Un booléen indiquant la fin de la simulation.</li>
 * 	<li>Une référence vers l'agent Display.</li>
 * </ul>
 * 
 * @see Constants#randomColor
 * @see Drone
 * @see Display
 */
public class GUI extends Agent
{
	private static final long serialVersionUID = 1L;

	// C'est la surface principale (la surface noire en fond)
	/**
	 * L'écran principal, colorié en noir par défaut.
	*/
	SDLSurface m_screen = null;
	
	// la map qui associe à chaque drone une surface (un carré en l'occurence de taille Constants.dotSize)
	
	/**
	 * La mappe contenant la surface associée à chaque drone de taille m_dotSize fois m_dotSize et d'une couleur aléatoire.
	 * 
	 * @see Constants#m_dotSize
	*/
	Map<String, SDLSurface> m_surfaces = new HashMap<String, SDLSurface>();
	
	// map de couleurs
	/**
	 * La mappe contenant la couleur associée à chaque drone, elle est sélectionnée de manière aléatoire.
	 * 
	 * @see Constants#randomColor
	*/
	Map<String, Long> m_colors = new HashMap<String, Long>();
	
	// la map de drones de Display qui est récupérée et stockée dans cet attribut
	/**
	 * La mappe contenant les positions des drones, l'agent display s'occupe de fournir la liste actualisée de positions.
	 * 
	 * @see Display#getDrones
	*/
	Map<String, Position> m_drones = null;
	
	/**
	 * La mappe contenant les positions des drones pendant l'itération précédente.
	*/
	Map<String, Position> m_last_drones_state = null;
	
	/**
	 * La queue contenant les drones supprimés pour les effacer de l'écran.
	*/
	Queue<Map.Entry<String, Position>> m_deletedDrones = null;
	
	// le booléen de boucle principale
	/**
	 * Un booléen indiquant la fin de la simulation, par défaut sa valeur est vraie, 
	 * si l'on clique sur la croix rouge de la fenêtre, on termine la simulation.
	*/
	boolean m_running = true;
	
	// la référence vers l'agent Display
	/**
	 * Une référence vers l'agent Display.
	 * 
	 * @see Display
	*/
	Display m_display = null;
	
	// méthode appelée lors de la création de l'agent GUI
	/**
	 * 
	*/
	@SuppressWarnings("unchecked")
	/**
	 * Dans cette méthode on récupère périodiquement les positions des drones et on les affiche à l'écran,
	 * on efface aussi les drones qui on été supprimés. L'agent Display s'occupe de fournir la liste de positions
	 * actualisées.
	 * 
	 * @see Display#getDrones
	 * @see Constants#m_dotSize
	 * @see Constants#randomColor
	*/
	protected void setup() 
	{
		// On récupère les arguments passés lors de la création de l'agent
		Object[] arguments = this.getArguments();
		
		m_drones =  (Map<String, Position>) arguments[0];
		m_display = (Display) arguments[1];
		m_deletedDrones = new ArrayBlockingQueue <Map.Entry<String, Position>>(Constants.m_numberDrones);
		
	        try 
	        {
	        	// Initialisation de la gui
			SDLMain.init(SDLMain.SDL_INIT_VIDEO);
	        	// Initialisation de l'écran
			m_screen = SDLVideo.setVideoMode(Constants.m_pWidth, Constants.m_pHeight, 32, SDLVideo.SDL_DOUBLEBUF | SDLVideo.SDL_HWSURFACE);
		        // Caption de la fenètre (titre)
		        SDLVideo.wmSetCaption("Flotte de drones en 2D", null);
		} 
	        catch (SDLException exception) 
	        {
			exception.printStackTrace();
			System.exit(-1);
		}
       
	        try 
	        {
	        	//Pour chaque drone recu en paramètre
	        	for(Map.Entry<String, Position> entry : m_drones.entrySet())
	    		{
	    	        	// Initialisation aléatoire des couleurs
	    	        	m_colors.put(entry.getKey(), Constants.randomColor());
	        		
	    			// On crée une surface qui représentant le drone en question, de taille Constants.dotSize fois Constant.dotSize
	    	    		SDLSurface surface = SDLVideo.createRGBSurface(SDLVideo.SDL_HWSURFACE, Constants.m_dotSize, Constants.m_dotSize, 32, 0, 0, 0, 0);
	    	    	
	    	    		// On assigne une position à la surface, donnée dans la map des drones passée en paramètre
	    	    		SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
	    	    	
	    	    		// On colorie la surface
	    	    		surface.fillRect(m_colors.get(entry.getKey()).longValue());
	    	 
	    	    		// On affiche la surface à l'écran à la position rect
	    	        	surface.blitSurface(m_screen, rect);
	    	        
	    	        	// On stocke la surface pour pouvoir la mettre à jour pendant la simulation
	    	        	m_surfaces.put(entry.getKey(), surface);
	    		}
		} 
	        catch (SDLException exception) 
	        {
			exception.printStackTrace();
			System.exit(-1);
		}
		
        	// boucle principale
        	while(m_running) 
        	{  
        		// On recupère la liste de drones, moyennement l'agent Display
        		m_drones = m_display.getDrones();
			try 
			{
				// On attend un événement non-bloquant
				SDLEvent event = SDLEvent.pollEvent();
		           
				if(event instanceof SDLEvent )
				{
					switch (event.getType()) 
					{
						// Si l'on clique sur la croix rouge de la gui, on quitte la boucle
		             			case SDLEvent.SDL_QUIT:    
		             				m_running = false;    
		             				break;
		             			default:
		             				break;
		            		}
				}
			}
			catch (SDLException exception) 
			{
				exception.printStackTrace();
				System.exit(-1);
			}
			
			try 
			{
				// Effacement de l'écran en la coloriant en noir
				m_screen.fillRect(m_screen.mapRGB(Constants.m_screenRed, Constants.m_screenGreen, Constants.m_screenBlue));
				
				// Si l'on remarque une réduction dans la taille du tableau des drones, alors au moins un drone est mort 
				// m_deletedDrones contient les drones morts à un delta T donné, ils doivent clignoter juste avant de mourir
				// Pendant le premier cycle m_last_drones_state est égal à null
				if (m_last_drones_state != null && m_drones.size() < m_last_drones_state.size())
				{
					//On compare les positions actuelles et précédentes
					for(Map.Entry<String, Position> entry : m_last_drones_state.entrySet())
					{
						if (!m_drones.containsKey(entry.getKey()))
						{
							//On les ajoute à la queueu
							Map.Entry<String, Position> tmpEntry = new HashMap.SimpleEntry<String, Position>(entry.getKey(), entry.getValue());
							m_deletedDrones.add(tmpEntry);
						}
							
					}
				}
				
				// On réinitialise la liste de positions précédentes avec les nouvelles positions
				m_last_drones_state = new HashMap<String, Position>(m_drones);
				// blittage des diff�rentes surfaces
				
				// On met à jour les positions des drones
				for(Map.Entry<String, Position> entry : m_drones.entrySet())
				{
					SDLSurface surface = m_surfaces.get(entry.getKey());
					surface.fillRect(m_colors.get(entry.getKey()).longValue());
					surface.blitSurface(m_screen, new SDLRect(entry.getValue().getX(), entry.getValue().getY()));
				}
				
				// Pendant qu'il y ait des drones à effacer
				while(!m_deletedDrones.isEmpty())
				{
					// On les colorie en blanc
					Map.Entry<String, Position> deletedDrone = m_deletedDrones.poll();
					SDLSurface surface = m_surfaces.get(deletedDrone.getKey());
					surface.fillRect(Constants.whiteColor());
					surface.blitSurface(m_screen, new SDLRect(deletedDrone.getValue().getX(), deletedDrone.getValue().getY()));
				}
				
				// Rafraichissement de l'écran
				m_screen.flip();
			}
			catch (SDLException exception) 
			{
				exception.printStackTrace();
				System.exit(-1);
			}
        }
        
	        try
	        {
	        	// On libère les surfaces de la mémoire avant de quitter
	    		for(Map.Entry<String, SDLSurface> entry : m_surfaces.entrySet())
	    		{
	    	    		m_surfaces.get(entry.getKey()).freeSurface();
	    		}
	    		
	    		// On libère la surface de l'écran principal
	    		m_screen.freeSurface();
		} 
	        catch(SDLException exception) 
	        {
			exception.printStackTrace();
			System.exit(-1);
		}
        
	        // On quitte la GUI (fermeture de la fenètre)
	        SDLMain.quit();
	        
	        // On quitte le programme
	        System.exit(0);
	        
	} // Fin de la méthode
} // Fin de la classe

