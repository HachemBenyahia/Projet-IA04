package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import sdljava.SDLException;

// conteneur principal, c'est par lui que d�marre l'application (quand on appuie sur �x�cuter dans eclipse)
public class MainContainer 
{	
	// le fichier properties
	public static String m_properties = "properties";

	public static void main(String[] args) throws SDLException, InterruptedException
	{
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		
		try
		{
			profile = new ProfileImpl(m_properties);
			AgentContainer container = runtime.createMainContainer(profile);
			
			// on cr�e l'agent display, qui prendra la rel�ve � partir d'ici (� partir de .start())
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
