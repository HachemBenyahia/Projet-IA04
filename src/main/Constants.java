package main;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// classe qui recense les constantes utilisées par le programme
public class Constants 
{
	// la période de récupération des positions des drones (en ms)
	static int retrievePositionsPeriod = 1000;
	
	// la largeur du terrain (en nombre de cellules)
	static int width = 40;
	
	// la hauteur du terrain (en nombre de cellules)
	static int height = 40;
	
	// le nombre de drones sur le terrain
	static int numberDrones = 5;
	
	// la taille d'une cellule sur le terrain (en pixels)
	static int dotSize = 10;

	// les coefficients rgb de la couleur du drone
	static int droneRed = 255;
	static int droneGreen = 0;
	static int droneBlue = 0;
	
	// les coefficients rgb de la couleur de l'écran
	static int screenRed = 0;
	static int screenGreen = 0;
	static int screenBlue = 0;
	
	// la largeur du terrain (en pixels)
	static int pWidth = width * dotSize;
	
	// la hauteur du terrain (en pixels)
	static int pHeight = height * dotSize;
	
	// méthode qui permet de traduire les paramètres du drones passés au format JSON
	// en tableau de paramètres Object (position, id, etc.)
	// je l'ai mise dans ce fichier parce que les drones doivent l'utiliser aussi a priori
	static public Map<String, Object> fromJSONArray(String message)
	{
		JSONParser jsonParser = new JSONParser();
		JSONArray args;
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		try 
		{
			args = (JSONArray) jsonParser.parse(message);
			
			// l'id du drone se trouve à la position 0
			JSONObject values = (JSONObject) args.get(0);
			int id = Integer.parseInt((values.get("id")).toString());
			
			// la position du drone se trouve à l'index 1 (voir encodage JSON dans Drone)
			values = (JSONObject) args.get(1);
			int x = Integer.parseInt((values.get("x")).toString());
			int y = Integer.parseInt((values.get("y")).toString());
			Position position = new Position(x, y);
			
			// on ajoute les paramètres dans la map
			parameters.put("id", id);
			parameters.put("position", position);
		} 
		catch(ParseException exception) 
		{
			exception.printStackTrace();
			System.exit(-1);
		}
		
		// on renvoie la map des paramètres
		return parameters;
	}
}