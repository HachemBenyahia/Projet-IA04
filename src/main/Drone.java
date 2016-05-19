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
		// on récupère les paramètres passés lors de sa création
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		
		addBehaviour(new RespondToDisplay(this));
	}
	
	// méthode qui permet d'encoder les paramètres du drones au format JSON
	@SuppressWarnings("unchecked")
	String toJSONArray()
	{
		JSONArray args = new JSONArray();

		// on sérialise l'id
		JSONObject id = new JSONObject();
		id.put("id", m_id);
		
		// on sérialise la position
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

// behaviour qui répond à Display quand il lui demande quelque chose
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
		
		// décommenter cette instruction pour avoir un exemple d'un déplacement simultané de tous les drones
		// (c'est un exemple, et le déplacement des drones ne doit pas se faire dans ce behaviour qui sert
		// à répondre à Display et non à se déplacer)
		// m_drone.m_position.upLeft();
		
		// si on a bien reçu un message de Display, on lui répond avec un INFORM
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