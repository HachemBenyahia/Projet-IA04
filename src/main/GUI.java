package main;

import java.util.HashMap;
import java.util.Map;

import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;

// classe utilisant la librairie sdljava pour repr�senter les drones graphiquement
// je ne vais pas trop d�tailler le fonctionnement, il faut regarder la doc pour comprendre
// comment elle fonctionne, mais elle n'est pas tr�s compliqu�e � utiliser
public class GUI
{
	// surface �cran c'est la surface principale (la surface noire en fond)
	SDLSurface m_screen = null;
	
	// la map qui associe � chaque drone une surface (un carr� rouge en l'occurence de taille Constants.dotSize)
	Map<String, SDLSurface> m_surfaces = new HashMap<String, SDLSurface>();
	
	// le bool�en de boucle principale
	boolean m_running = true;
	
	// m�thode qui initialise les surfaces � partir de la map des drones
	void initSurfaces(Map<String, Position> drones) throws SDLException
	{
		for(Map.Entry<String, Position> entry : drones.entrySet())
		{
			// on cr�e une surface qui repr�sentant le drone en question (de taille carr�e = Constants.dotSize x Constant.dotSize)
	    	SDLSurface surface = SDLVideo.createRGBSurface(SDLVideo.SDL_HWSURFACE, Constants.dotSize, Constants.dotSize, 32, 0, 0, 0, 0);
	    	
	    	// on assigne une position � la surface, donn�e dans la map des drones pass�e en param�tre
	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
	    	
	    	// on colorie la surface (rouge en l'occurence, mais on peut modifier les param�tres couleurs dans Constants)
	    	surface.fillRect(surface.mapRGB(Constants.droneRed, Constants.droneGreen, Constants.droneBlue));
	    	
	    	// on affiche la surface � l'�cran � la position rect
	        surface.blitSurface(m_screen, rect);
	        
	        // on stocke la surface pour pouvoir la mettre � jour au fur et � mesure du programme (c'est � dire modifier sa position)
	        m_surfaces.put(entry.getKey(), surface);
		}
	}
	
	// constructeur de la classe
    public GUI(Map<String, Position> drones) throws SDLException, InterruptedException 
    {
    	// initialisation de la gui
        SDLMain.init(SDLMain.SDL_INIT_VIDEO);
        
        // initialisation de l'�cran
        m_screen = SDLVideo.setVideoMode(Constants.environmentWidth * Constants.dotSize, 
        Constants.environmentHeight * Constants.dotSize, 32, SDLVideo.SDL_DOUBLEBUF | SDLVideo.SDL_HWSURFACE);
        
        // caption de la fen�tre (titre)
        SDLVideo.wmSetCaption("Flotte de drones en 2D", null);
        
        // on appelle la m�thode d�finie plus haut
        initSurfaces(drones);
       
        // boucle principale
        while(m_running) 
        {
        	// on attend un �v�nement de mani�re non bloquante (c'est � dire qu'on fait ce qu'on � faire mais s'il y a un �v�nement,
        	// alors on le traite)
            SDLEvent event = SDLEvent.pollEvent();

            if(event instanceof SDLEvent) 
            {
                switch (event.getType()) 
                {
                	// si on clique sur la croix rouge de la fen�tre, alors m_running passe � faux
	                case SDLEvent.SDL_QUIT:     
	                	m_running = false;    
	                break;
                }
            }
            
            // on met � jour l'�cran � chaque tour de boucle
            m_screen.flip();
        }

        // on lib�re les surfaces de la m�moire avant de quitter
        freeSurfaces();
        
        // on quitte la gui (fermeture de la fen�tre)
        SDLMain.quit();
    }
    
    // m�thode pour mettre � jour la gui, elle met � jour la position des drones (les carr�s rouges)
    public void updateGUI(Map<String, Position> drones) throws SDLException
    {
		for(Map.Entry<String, Position> entry : drones.entrySet())
		{
			// on cr�e une nouvelle position bas�e sur la map recue en param�tre
	    	SDLRect rect = new SDLRect(entry.getValue().getX(), entry.getValue().getY());
	    	
	    	// on update la surface concern�e (celle dont la cl� est �gale � celle du drone actuel
	    	m_surfaces.get(entry.getKey()).updateRect(rect);
	    	
	    	// on r�affiche la surface sur l'�cran
	    	m_surfaces.get(entry.getKey()).blitSurface(m_screen, rect);
		}
    }
    
    // m�thode qui lib�re les surfaces utilis�es
    void freeSurfaces() throws SDLException
    {
    	// surface des drones
		for(Map.Entry<String, SDLSurface> entry : m_surfaces.entrySet())
		{
	    	m_surfaces.get(entry.getKey()).freeSurface();
		}
		
		// surface de l'�cran
		m_screen.freeSurface();
    }
}
