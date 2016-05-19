package main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
		// on r�cup�re les param�tres pass�s lors de sa cr�ation
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		
		addBehaviour(new RespondToDisplay(this));
	}
	
	// m�thode qui permet d'encoder les param�tres du drones au format JSON
	@SuppressWarnings("unchecked")
	String toJSONArray()
	{
		JSONArray args = new JSONArray();

		// on s�rialise l'id
		JSONObject id = new JSONObject();
		id.put("id", m_id);
		
		// on s�rialise la position
		JSONObject position = new JSONObject();
		position.put("x", m_position.getX());
		position.put("y", m_position.getY());
		
		// on ajoute les deux dans le tableau d'objets JSON
		args.add(id);
		args.add(position);
		
		// on renvoie le JSON en string
		return args.toJSONString();
	}
}

// behaviour qui r�pond � Display quand il lui demande quelque chose
class RespondToDisplay extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Drone m_drone;

	public RespondToDisplay(Drone drone) 
	{
		m_drone = drone;
	}

	public void action() 
	{	
		ACLMessage message = m_drone.receive(MessageTemplate.MatchSender(new AID("Display", AID.ISLOCALNAME)));
		
		// d�commenter cette instruction pour avoir un exemple d'un d�placement simultan� de tous les drones
		// (c'est un exemple, et le d�placement des drones ne doit pas se faire dans ce behaviour qui sert
		// � r�pondre � Display et non � se d�placer)
		// m_drone.m_position.upLeft();
		
		// si on a bien re�u un message de Display, on lui r�pond avec un INFORM
		// dans lequel on envoie les informations en question au format JSON
		if(message != null)
		{
			ACLMessage reply = message.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent(m_drone.toJSONArray());
			
			m_drone.send(reply);
		}
		else
			block();
	}
}