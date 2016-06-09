package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import sdljava.SDLException;

// conteneur principal, c'est par lui que démarre l'application (quand on appuie sur éxécuter dans eclipse)
/**
 * <b>MainContainer est la classe représentant le conteneur principal du système.</b>
 * <p>La seule méthode de cette classe est main, dans cette fonction on initialise le conteneur principal proprement dit
 * et on crée une instance de la classe display, qui s'occupera de la logique fonctionnelle du système. </p>
 * 
 * <p>Le seul champ que cette classe possède est m_properties,
 * qui est une chaîne contenant le nom du fichier des propriétés d'initialisation du conteneur. </p>
 * 
 * @see Display
 */
public class MainContainer 
{	
	// le fichier properties
	/**
	 * Le nom du fichier des propriétés d'initialisation du conteneur, c'est déjà fixé est sa valeur est "properties".
	*/
	public static String m_properties = "properties";

	/**
	 * C'est la fonction qui démarre tout le fonctionnement du système,
	 * dans cette méthode on initialise le conteneur principal proprement dit
	 * et on crée une instance de la classe display, qui s'occupera de la logique fonctionnelle du système.
	 * 
	 * @see Display
	 * @throws SDLException Si jamais il y a un problème avec la bibliothèque SDL. 
	 * @throws InterruptedException Si un thread est interrompu pendant ou après son activité et ne finit pas normalement.
	*/
	public static void main(String[] args) throws SDLException, InterruptedException
	{
		//C'est l'environnement d'exécution, ici on va créer le conteneur principal 
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		
		try
		{
			//Lecture du fichier de propriétés
			profile = new ProfileImpl(m_properties);
			//Création du conteneur
			AgentContainer container = runtime.createMainContainer(profile);
			
			//On crée l'agent display, qui prendra la relève à partir d'ici (à partir de .start())
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

