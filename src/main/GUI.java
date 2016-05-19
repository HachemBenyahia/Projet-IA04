package main;

import java.util.HashMap;
import java.util.Map;
import jade.core.Agent;
import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;

// classe utilisant la librairie sdljava pour représenter les drones graphiquement
// je ne vais pas trop détailler le fonctionnement, il faut regarder la doc pour comprendre
// comment elle fonctionne, mais elle n'est pas très compliquée à utiliser
public class GUI extends Agent
{
	private static final long serialVersionUID = 1L;

	// surface écran c'est la surface principale (la surface noire en fond)
	SDLSurface m_screen = null;
	
	// la map qui associe à chaque drone une surface (un carré rouge en l'occurence de taille Constants.dotSize)
	Map<String, SDLSurface> m_surfaces = new HashMap<String, SDLSurface>();
	
	// la map de drones de Display qui est récupérée et stockée dans cet attribut
	Map<String, Position> m_drones = null;
	
	// le booléen de boucle principale
	boolean m_running = true;
	
	// la référence vers l'agent Display
	Display m_display = null;
	
	// méthode appelée lors de la création de l'agent GUI
	@SuppressWarnings("unchecked")
	protected void setup() 
	{
		// on récupère les arguments passés lors de la création de l'agent
		Object[] arguments = this.getArguments();
		
		m_drones =  (Map<String, Position>) arguments[0];
		m_display = (Display) arguments[1];
		
        try 
        {
        	// initialisation de la gui
			SDLMain.init(SDLMain.SDL_INIT_VIDEO);
			
        	// initialisation de l'écran
			m_screen = SDLVideo.setVideoMode(Constants.m_pWidth, Constants.m_pHeight, 32, SDLVideo.SDL_DOUBLEBUF | SDLVideo.SDL_HWSURFACE);
	        // caption de la fenêtre (titre)
	        SDLVideo.wmSetCaption("Flotte de drones en 2D", null);
		} 
        catch (SDLException exception) 
        {
			exception.printStackTrace();
			System.exit(-1);
		}
       
        try 
        {
        	for(Map.Entry<String, Position> entry : m_drones.entrySet())
    		{
    			// on crée une surface qui représentant le drone en question (de taille carrée = Constants.dotSize x Constant.dotSize)
    	    	SDLSurface surface = SDLVideo.createRGBSurface(SDLVideo.SDL_HWSURFACE, Constants.m_dotSize, Constants.m_dotSize, 32, 0, 0, 0, 0);
    	    	
    	    	// on assigne une position à la surface, donnée dans la map des drones passée en paramètre
    	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
    	    	
    	    	// on colorie la surface (rouge en l'occurence, mais on peut modifier les paramètres couleurs dans Constants)
    	    	surface.fillRect(surface.mapRGB(Constants.m_droneRed, Constants.m_droneGreen, Constants.m_droneBlue));
    	    	
    	    	// on affiche la surface à l'écran à la position rect
    	        surface.blitSurface(m_screen, rect);
    	        
    	        // on stocke la surface pour pouvoir la mettre à jour au fur et à mesure du programme (c'est à dire modifier sa position)
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
        	m_drones = m_display.getDrones();
  
			try 
			{
				// on attend un évènement de manière non bloquante
				SDLEvent event = SDLEvent.pollEvent();
		           
				if(event instanceof SDLEvent )
				{
					switch (event.getType()) 
					{
						// si on clique sur la croix rouge de la gui, on quitte la boucle
		             	case SDLEvent.SDL_QUIT:    
		             		m_running = false;    
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
				// effacement de l'écran par une couleur unie
				m_screen.fillRect(m_screen.mapRGB(Constants.m_screenRed, Constants.m_screenGreen, Constants.m_screenBlue));
				
				// blittage des différentes surfaces
				for(Map.Entry<String, Position> entry : m_drones.entrySet())
				{
					SDLSurface surface = m_surfaces.get(entry.getKey());
					surface.blitSurface(m_screen, new SDLRect(entry.getValue().getX(), entry.getValue().getY()));
				}
				
				// rafraichissement de l'écran
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
        	// on libère les surfaces de la mémoire avant de quitter
    		for(Map.Entry<String, SDLSurface> entry : m_surfaces.entrySet())
    		{
    	    	m_surfaces.get(entry.getKey()).freeSurface();
    		}
    		
    		// surface de l'écran
    		m_screen.freeSurface();
		} 
        catch(SDLException exception) 
        {
			exception.printStackTrace();
			System.exit(-1);
		}
        
        // on quitte la gui (fermeture de la fenêtre)
        SDLMain.quit();
        
        // on quitte le programme
        System.exit(0);
	}
}