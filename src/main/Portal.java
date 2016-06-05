package main;

import jade.core.Agent;

public class Portal extends Agent {
	private static final long serialVersionUID = 1L;
	
	int m_id;
	int m_nbDronesAccepted;
	Position m_position;
	boolean m_isOpen;
	
	public void setup()
	{
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		m_nbDronesAccepted = (int) arguments[2];
		m_isOpen = true;   // à l'initialisation, tous les portails sont ouverts
	}
}


// ajouter un behaviour qui périodiquement broadcast un message informant les drones aux alentours du nombre de 
// drones que ce portail accepte


// ajouter un behaviour qui écoute les messages venant de drones maitres. Ces messages sont envoyés par des maitres
// de flottes pour prévenir le portail de l'arrivée de x drones venant de sa flotte. Le portail réserve ainsi sa place
// à ces drones. le portail n'accepte une autre réservation que si le temps écoulé après la premiere reservation dépasse
// un DELTA T sans avoir recu tous les drones promis. Dans ce cas là il renvoi les drones qui ont pu arriver à temps. Ces
// drones renvoyés redeviendront des drones errants.


// ajouter un behaviour qui écoute les messages venant de drones non maitres (l'id du maitre de flotte est différent du
// sien. Le message contient l'id de son maitre. Si l'id du maitre correspond à celui qui avait reservé le portail, alors
// le portail accepte ce drone, et dérémente le nb de drones à attendre de ce maitre. Sinon, il n'accepte pas le drone.
// Lorsque le nombre de drones à attendre du maitre  = 0 et qu'il a le bon nombre de drones, il ferme ses portes et envoi
// un message à display pour l'en informer.