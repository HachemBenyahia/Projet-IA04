package main;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Portal extends Agent {
	private static final long serialVersionUID = 1L;
	
	int m_id;
	int m_nbDronesAccepted;
	Position m_position;
	boolean m_isOpen;
	boolean m_isFree;
	String m_password;
	ArrayList<AID> m_inProcedureDrones;
	
	
	protected void setup()
	{
		Object[] arguments = this.getArguments();
		
		m_id = (int) arguments[0];
		m_position = (Position) arguments[1];
		m_nbDronesAccepted = (int) arguments[2];
		m_inProcedureDrones = new ArrayList<AID>();
		m_isOpen = true;   // à l'initialisation, tous les portails sont ouverts
		m_isFree = true;   // à l'initialisation, tous les portails sont libres
		m_password = "";
		
		addBehaviour(new PlacesBroadcast(this, Constants.m_emitEnvironmentPeriod));
		addBehaviour(new receiveLandingRequest(this));
		addBehaviour(new receiveDrones(this));
	}
	
	void replyToDrones(boolean permission)
	{
		ACLMessage message = new ACLMessage((permission ? ACLMessage.AGREE : ACLMessage.REFUSE));
		
		Iterator<AID> ite = this.m_inProcedureDrones.iterator();
		
		while (ite.hasNext())
		{
			AID element = ite.next();
			message.addReceiver(element);
		}
		
		this.send(message);
	}
	
	@SuppressWarnings("unchecked")
	String toJson()
	{
		JSONObject args = new JSONObject();
		
		JSONObject position = new JSONObject();
		position.put("x", m_position.getX());
		position.put("y", m_position.getY());
		args.put("position", position);
		
		args.put("nbDronesAccepted", m_nbDronesAccepted);
		
		return args.toJSONString();
	}
}


// ajouter un behaviour qui périodiquement broadcast un message informant les drones aux alentours du nombre de 
// drones que ce portail accepte

class PlacesBroadcast extends TickerBehaviour
{
	private static final long serialVersionUID = 1L;
	Portal m_portal = (Portal) this.myAgent;
	
	public PlacesBroadcast(Agent agent, long period) 
	{
		super(agent, period);
	}
	
	public void onTick()
	{
		if (!m_portal.m_isOpen) return;
		
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.setContent(m_portal.toJson());
		
		// on envoit � tous les drones
		for(int i = 0 ; i < Constants.m_numberDrones ; i ++)
			message.addReceiver(new AID("Drone" + i, AID.ISLOCALNAME));
		
		m_portal.send(message);
	}
}

class receiveLandingRequest extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Portal m_portal;
	
	public receiveLandingRequest(Portal portal)
	{
		super();
		m_portal = portal;
	}

	public void action() {
		ACLMessage message = m_portal.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
		
		if (message != null)
		{
			ACLMessage reply = message.createReply();
			
			if (m_portal.m_isFree) // La place est dispo, request OK
			{
				m_portal.m_isFree = false;
				System.out.println("ok JE SUIS FREEEE");
				m_portal.m_password = message.getConversationId();
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				this.getAgent().send(reply);
				// d�clenche le timer pour le reset du password
				this.getAgent().addBehaviour(new ResetPassword(m_portal, Constants.m_passwordResetDelay));
				
			}
			else // Un autre maitre est d�j� en train d'envoyer des drones, on refuse
			{
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				this.getAgent().send(reply);
			}
		}
		else
		{
			block();
		}
	}
	
}


// V�rifie que le temps imparti pour rentrer dans le portail apr�s acceptation de la proc�dure n'est pas d�pass�.
class ResetPassword extends WakerBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Portal m_portal;

	public ResetPassword(Portal portal, long timeout) {
		super(portal, timeout);
		m_portal = portal;
	}
	
	public void handleElapsedTimeout()
	{
		System.out.println("Password reset pour PORTAL " + m_portal.m_id + "(timeout)");
		m_portal.m_password = "";
		for (int i=0; i<m_portal.m_inProcedureDrones.size(); i++)
		{
			m_portal.replyToDrones(Constants.m_landingRefused); // reject all former drones awaiting for an answer
		}
		m_portal.m_inProcedureDrones = new ArrayList<AID>(); // reset in_procedure_drones array
	}
}

class receiveDrones extends CyclicBehaviour
{
	private static final long serialVersionUID = 1L;
	
	Portal m_portal;
	
	public receiveDrones(Portal portal)
	{
		super();
		m_portal = portal;
	}

	public void action() {
		ACLMessage message = m_portal.receive(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));
		
		if (message != null)
		{
			String password = message.getContent();
			
			if (password == m_portal.m_password)
			{	// on ajoute � la liste des drones ayant donn� le bon pwd pour v�rifier s'ils sont tous arriv�s
				m_portal.m_inProcedureDrones.add(message.getSender());
				if (m_portal.m_inProcedureDrones.size() == m_portal.m_nbDronesAccepted)
				{
					m_portal.replyToDrones(Constants.m_landingGranted);
				}
			}
			else
			{	// pwd incorrect
				ACLMessage reply = message.createReply();
				reply.setPerformative(ACLMessage.REFUSE);
				this.getAgent().send(reply);
			}
		}
		else
		{
			block();
		}
	}
	
}


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