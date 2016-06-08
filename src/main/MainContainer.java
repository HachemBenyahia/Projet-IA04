package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import sdljava.SDLException;

// conteneur principal, c'est par lui que démarre l'application (quand on appuie sur éxécuter dans eclipse)
// classe Drone
/**
 * <b>MainContainer est la classe représentant le conteneur principal du système.</b>
 * <p>La seule méthode de cette classe est main, dans cette fonction on initialise le conteneur principal proprement dit
 * et on crée une instance de la classe display, qui s'occupera de la logique fonctionnelle du système. </p>
 * 
 * <p>Le seul champ que cette classe possède est m_properties,
 * qui est une chaîne contenant le nom du fichier des propietés d'initialisation du conteneur. </p>
 * 
 * @see Display
 */
public class MainContainer 
{	
	// le fichier properties
	/**
	 * Le nom du fichier des propietés d'initialisation du conteneur, c'est déjà fixé est sa valeur est "properties".
	*/
	public static String m_properties = "properties";

	public static void main(String[] args) throws SDLException, InterruptedException
	{
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		
		try
		{
			profile = new ProfileImpl(m_properties);
			AgentContainer container = runtime.createMainContainer(profile);
			
			// on crée l'agent display, qui prendra la relève à partir d'ici (à partir de .start())
			AgentController display = container.createNewAgent("Display", "main.Display", null);
			display.start();
		}	
		catch(Exception exception) 
		{
			System.out.println(exception.getMessage());
			System.exit(-1);
		}
	}
}
