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

// classe utilisant la librairie sdljava pour repr�senter les drones graphiquement
// je ne vais pas trop d�tailler le fonctionnement, il faut regarder la doc pour comprendre
// comment elle fonctionne, mais elle n'est pas tr�s compliqu�e � utiliser
public class GUI extends Agent
{
	private static final long serialVersionUID = 1L;

	// surface �cran c'est la surface principale (la surface noire en fond)
	SDLSurface m_screen = null;
	
	// la map qui associe � chaque drone une surface (un carr� en l'occurence de taille Constants.dotSize)
	Map<String, SDLSurface> m_surfaces = new HashMap<String, SDLSurface>();
	
	// map de couleurs
	Map<String, Long> m_colors = new HashMap<String, Long>();
	
	// la map de drones de Display qui est r�cup�r�e et stock�e dans cet attribut
	Map<String, Position> m_drones = null;
	Map<String, Position> m_last_drones_state = null;
	Queue<Map.Entry<String, Position>> m_deletedDrones = null;
	
	// le bool�en de boucle principale
	boolean m_running = true;
	
	// la r�f�rence vers l'agent Display
	Display m_display = null;
	
	// m�thode appel�e lors de la cr�ation de l'agent GUI
	@SuppressWarnings("unchecked")
	protected void setup() 
	{
		// on r�cup�re les arguments pass�s lors de la cr�ation de l'agent
		Object[] arguments = this.getArguments();
		
		m_drones =  (Map<String, Position>) arguments[0];
		m_display = (Display) arguments[1];
		m_deletedDrones = new ArrayBlockingQueue<Map.Entry<String, Position>>(Constants.m_numberDrones);
		
        try 
        {
        	// initialisation de la gui
			SDLMain.init(SDLMain.SDL_INIT_VIDEO);
			
        	// initialisation de l'�cran
			m_screen = SDLVideo.setVideoMode(Constants.m_pWidth, Constants.m_pHeight, 32, SDLVideo.SDL_DOUBLEBUF | SDLVideo.SDL_HWSURFACE);
	        // caption de la fen�tre (titre)
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
    	        // initialisation des couleurs
    	        m_colors.put(entry.getKey(), Constants.randomColor());
        		
    			// on cr�e une surface qui repr�sentant le drone en question (de taille carr�e = Constants.dotSize x Constant.dotSize)
    	    	SDLSurface surface = SDLVideo.createRGBSurface(SDLVideo.SDL_HWSURFACE, Constants.m_dotSize, Constants.m_dotSize, 32, 0, 0, 0, 0);
    	    	
    	    	// on assigne une position � la surface, donn�e dans la map des drones pass�e en param�tre
    	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
    	    	
    	    	// on colorie la surface
    	    	surface.fillRect(m_colors.get(entry.getKey()).longValue());
    	 
    	    	// on affiche la surface � l'�cran � la position rect
    	        surface.blitSurface(m_screen, rect);
    	        
    	        // on stocke la surface pour pouvoir la mettre � jour au fur et � mesure du programme (c'est � dire modifier sa position)
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
				// on attend un �v�nement de mani�re non bloquante
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
				// effacement de l'�cran par une couleur unie
				m_screen.fillRect(m_screen.mapRGB(Constants.m_screenRed, Constants.m_screenGreen, Constants.m_screenBlue));
				
				// si on remarque une réduction dans la taille du tableau des drones => mort d'un drone
				// m_deletedDrones contient les drones morts à un delta T donné, ils doivent clignoter juste avant de mourir
				if (m_last_drones_state != null && m_drones.size() < m_last_drones_state.size())
				{
					for(Map.Entry<String, Position> entry : m_last_drones_state.entrySet())
					{
						if (!m_drones.containsKey(entry.getKey()))
						{
							Map.Entry<String, Position> tmpEntry = new HashMap.SimpleEntry<String, Position>(entry.getKey(), entry.getValue());
							m_deletedDrones.add(tmpEntry);
						}
							
					}
				}
				
				m_last_drones_state = new HashMap<String, Position>(m_drones);
				// blittage des diff�rentes surfaces
				
				for(Map.Entry<String, Position> entry : m_drones.entrySet())
				{
					SDLSurface surface = m_surfaces.get(entry.getKey());
					surface.fillRect(m_colors.get(entry.getKey()).longValue());
					surface.blitSurface(m_screen, new SDLRect(entry.getValue().getX(), entry.getValue().getY()));
				}
				while(!m_deletedDrones.isEmpty())
				{
					Map.Entry<String, Position> deletedDrone = m_deletedDrones.poll();
					SDLSurface surface = m_surfaces.get(deletedDrone.getKey());
					surface.fillRect(Constants.whiteColor());
					surface.blitSurface(m_screen, new SDLRect(deletedDrone.getValue().getX(), deletedDrone.getValue().getY()));
				}
				// rafraichissement de l'�cran
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
        	// on lib�re les surfaces de la m�moire avant de quitter
    		for(Map.Entry<String, SDLSurface> entry : m_surfaces.entrySet())
    		{
    	    	m_surfaces.get(entry.getKey()).freeSurface();
    		}
    		
    		// surface de l'�cran
    		m_screen.freeSurface();
		} 
        catch(SDLException exception) 
        {
			exception.printStackTrace();
			System.exit(-1);
		}
        
        // on quitte la gui (fermeture de la fen�tre)
        SDLMain.quit();
        
        // on quitte le programme
        System.exit(0);
	}
}