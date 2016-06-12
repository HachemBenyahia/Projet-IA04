package main;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// classe qui recense les constantes utilisï¿½es par le programme
public class Constants 
{	
	// la pï¿½riode de rï¿½cupï¿½ration des positions des drones (en ms)
	static int m_retrievePositionsPeriod = 1000;
	
	// la largeur du terrain (en nombre de cellules)
	static int m_width = 60;
	
	// la hauteur du terrain (en nombre de cellules)
	static int m_height = 40;
	
	// le nombre de drones sur le terrain
	static int m_numberDrones = 8;
	
	// la taille d'une cellule sur le terrain (en pixels)
	static int m_dotSize = 10;

	// pï¿½riode d'ï¿½mission de caractï¿½ristiques du drone dans l'environnement
	static int m_emitEnvironmentPeriod = 1000;
	
	// pï¿½riode de mouvement d'un drone, c'est-dire la pï¿½riode entre
	// deux dï¿½placements (ou deux tentatives de dï¿½placement en tout cas
	static int m_movementPeriod = 1000;
	
	// portï¿½e maximale en pixels
	static int m_maxRange = 50;
	
	// les coefficients rgb de la couleur du drone
	static int m_droneRed = 255;
	static int m_droneGreen = 0;
	static int m_droneBlue = 0;
	
	// les coefficients rgb de la couleur de l'ï¿½cran
	static int m_screenRed = 0;
	static int m_screenGreen = 0;
	static int m_screenBlue = 0;
	
	// la largeur du terrain (en pixels)
	static int m_pWidth = m_width * m_dotSize;
	
	// la hauteur du terrain (en pixels)
	static int m_pHeight = m_height * m_dotSize;
	
	static enum State{ALONE, FLEET, FUSION};
	
	// Le temps avant la remise à 0 du mot de passe pour un portail
	static long m_passwordResetDelay = 30000;
	
	static boolean m_landingGranted = true;
	static boolean m_landingRefused = false;
	
	static public void setNumberDrones(int numberDrones)
	{
		m_numberDrones = numberDrones;
	}
	
	// mï¿½thode qui permet de traduire les paramï¿½tres du drones passï¿½s au format JSON
	// en tableau de paramï¿½tres Object (position, id, etc.)
	// je l'ai mise dans ce fichier parce que les drones doivent l'utiliser aussi a priori
	static public Map<String, Object> fromJSONArray(String message)
	{
		JSONParser jsonParser = new JSONParser();
		JSONArray args;
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		try 
		{
			args = (JSONArray) jsonParser.parse(message);
			
			// l'id du drone se trouve ï¿½ la position 0
			JSONObject values = (JSONObject) args.get(0);
			int id = Integer.parseInt((values.get("id")).toString());
			parameters.put("id", id);
			
			// la position du drone se trouve ï¿½ l'index 1 (voir encodage JSON dans Drone)
			values = (JSONObject) args.get(1);
			int x = Integer.parseInt((values.get("x")).toString());
			int y = Integer.parseInt((values.get("y")).toString());
			Position position = new Position(x, y);
			parameters.put("position", position);
			
			Map<Integer, Position> fleet = new TreeMap<Integer, Position>();
			JSONArray fleetArgs = (JSONArray) args.get(2);
			
			for(int i = 0 ; i < fleetArgs.size() ; i ++) 
			{
				args = (JSONArray) fleetArgs.get(i);
				
				values = (JSONObject) args.get(0);
				id = new Integer(Integer.parseInt((values.get("id")).toString()));
				
				values = (JSONObject) args.get(1);
				x = Integer.parseInt((values.get("x")).toString());
				y = Integer.parseInt((values.get("y")).toString());
				position = new Position(x, y);
				
				fleet.put(id, position);
			}
			parameters.put("fleet", fleet);
		}
		catch(ParseException exception) 
		{
			exception.printStackTrace();
			System.exit(-1);
		}
		
		// on renvoie la map des paramï¿½tres
		return parameters;
	}
	
	// renvoie une couleur alï¿½atoire
	public static Long randomColor()
	{	
		return (Long) Math.round(Math.random() * Math.pow(2, 32));
	}
	public static Long whiteColor()
	{
		return new Long((int) Math.pow(2, 32));
	}
}