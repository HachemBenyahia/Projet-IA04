package main;

import jade.core.Agent;

// classe Drone
public class Drone extends Agent
{
	private static final long serialVersionUID = 1L;
	
	// l'id du drone
	int m_id;
	
	// sa position actuelle (x, y)
	Position m_position;
	
	protected void setup()
	{
		// on récupère les paramètres passés lors de sa création
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		
		String label = "Drone " + m_id + " lancé à la position " + m_position.toString();
		
		System.out.println(label);
	}
}
