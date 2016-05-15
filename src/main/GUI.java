package main;

import java.util.HashMap;
import java.util.Map;

import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;

// classe utilisant la librairie sdljava pour représenter les drones graphiquement
// je ne vais pas trop détailler le fonctionnement, il faut regarder la doc pour comprendre
// comment elle fonctionne, mais elle n'est pas très compliquée à utiliser
public class GUI
{
	// surface écran c'est la surface principale (la surface noire en fond)
	SDLSurface m_screen = null;
	
	// la map qui associe à chaque drone une surface (un carré rouge en l'occurence de taille Constants.dotSize)
	Map<String, SDLSurface> m_surfaces = new HashMap<String, SDLSurface>();
	
	// le booléen de boucle principale
	boolean m_running = true;
	
	// méthode qui initialise les surfaces à partir de la map des drones
	void initSurfaces(Map<String, Position> drones) throws SDLException
	{
		for(Map.Entry<String, Position> entry : drones.entrySet())
		{
			// on crée une surface qui représentant le drone en question (de taille carrée = Constants.dotSize x Constant.dotSize)
	    	SDLSurface surface = SDLVideo.createRGBSurface(SDLVideo.SDL_HWSURFACE, Constants.dotSize, Constants.dotSize, 32, 0, 0, 0, 0);
	    	
	    	// on assigne une position à la surface, donnée dans la map des drones passée en paramètre
	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
	    	
	    	// on colorie la surface (rouge en l'occurence, mais on peut modifier les paramètres couleurs dans Constants)
	    	surface.fillRect(surface.mapRGB(Constants.droneRed, Constants.droneGreen, Constants.droneBlue));
	    	
	    	// on affiche la surface à l'écran à la position rect
	        surface.blitSurface(m_screen, rect);
	        
	        // on stocke la surface pour pouvoir la mettre à jour au fur et à mesure du programme (c'est à dire modifier sa position)
	        m_surfaces.put(entry.getKey(), surface);
		}
	}
	
	// constructeur de la classe
    public GUI(Map<String, Position> drones) throws SDLException, InterruptedException 
    {
    	// initialisation de la gui
        SDLMain.init(SDLMain.SDL_INIT_VIDEO);
        
        // initialisation de l'écran
        m_screen = SDLVideo.setVideoMode(Constants.environmentWidth * Constants.dotSize, 
        Constants.environmentHeight * Constants.dotSize, 32, SDLVideo.SDL_DOUBLEBUF | SDLVideo.SDL_HWSURFACE);
        
        // caption de la fenêtre (titre)
        SDLVideo.wmSetCaption("Flotte de drones en 2D", null);
        
        // on appelle la méthode définie plus haut
        initSurfaces(drones);
       
        // boucle principale
        while(m_running) 
        {
        	// on attend un évènement de manière non bloquante (c'est à dire qu'on fait ce qu'on à faire mais s'il y a un évènement,
        	// alors on le traite)
            SDLEvent event = SDLEvent.pollEvent();

            if(event instanceof SDLEvent) 
            {
                switch (event.getType()) 
                {
                	// si on clique sur la croix rouge de la fenêtre, alors m_running passe à faux
	                case SDLEvent.SDL_QUIT:     
	                	m_running = false;    
	                break;
                }
            }
            
            // on met à jour l'écran à chaque tour de boucle
            m_screen.flip();
        }

        // on libère les surfaces de la mémoire avant de quitter
        freeSurfaces();
        
        // on quitte la gui (fermeture de la fenêtre)
        SDLMain.quit();
    }
    
    // méthode pour mettre à jour la gui, elle met à jour la position des drones (les carrés rouges)
    public void updateGUI(Map<String, Position> drones) throws SDLException
    {
		for(Map.Entry<String, Position> entry : drones.entrySet())
		{
			// on crée une nouvelle position basée sur la map recue en paramètre
	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
	    	
	    	// on update la surface concernée (celle dont la clé est égale à celle du drone actuel
	    	m_surfaces.get(entry.getKey()).updateRect(rect);
	    	
	    	// on réaffiche la surface sur l'écran
	    	m_surfaces.get(entry.getKey()).blitSurface(m_screen, rect);
		}
    }
    
    // méthode qui libère les surfaces utilisées
    void freeSurfaces() throws SDLException
    {
    	// surface des drones
		for(Map.Entry<String, SDLSurface> entry : m_surfaces.entrySet())
		{
	    	m_surfaces.get(entry.getKey()).freeSurface();
		}
		
		// surface de l'écran
		m_screen.freeSurface();
    }
}
