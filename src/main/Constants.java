package main;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// classe qui recense les constantes utilis�es par le programme
/**
 * <b>Constants est la classe contenant les valeurs fixes utilisées dans la simulation.</b>
 */
public class Constants 
{	
	// la taille d'une cellule sur le terrain (en pixels)
	/**
	 * La taille des cellules en nombre de pixels.
	 */
	static int m_dotSize = 10;
	
	// la largeur du terrain (en nombre de cellules)
	/**
	 * La largeur du terrain en nombre de cellules.
	 */
	static int m_width = 60;
	
	// la hauteur du terrain (en nombre de cellules)
	/**
	 * La hauteur du terrain en nombre de cellules.
	 */
	static int m_height = 40;
	
	// le nombre de drones sur le terrain
	/**
	 * Le nombre de drones sur le terrain, cette variable est susceptible d'avoir des modifications.
	 * @see Constants#setNumberDrones
	 */
	static int m_numberDrones = 8;
	
	// la p�riode de r�cup�ration des positions des drones (en ms)
	/**
	 * La période de récupération des positions des drones en millisecondes.
	 * @see RetrievePositions
	 */
	static int m_retrievePositionsPeriod = 1000;
	
	
	// p�riode d'�mission de caract�ristiques du drone dans l'environnement
	/**
	 * La période d'émission des caractéristiques des drones à l'environnement.
	 * @see EmitEnvironment
	 */
	static int m_emitEnvironmentPeriod = 1000;
	
	// p�riode de mouvement d'un drone, c'est-dire la p�riode entre
	// deux d�placements (ou deux tentatives de d�placement en tout cas
	/**
	 * La période entre deux tentatives de déplacement des drones.
	 * @see Movement
	*/
	static int m_movementPeriod = 1000;
	
	// port�e maximale en pixels
	/**
	 * C'est la portée maximale des drones en pixels.
	 * @see Position#reachable
	*/
	static int m_maxRange = 50;
	
	// les coefficients rgb de la couleur du drone
	/**
	 * Coefficient de la couler rouge utilisée pour définir la couleur des drones.
	*/
	static int m_droneRed = 255;
	/**
	 * Coefficient de la couler verte utilisée pour définir la couleur des drones.
	*/
	static int m_droneGreen = 0;
	/**
	 * Coefficient de la couler bleue utilisée pour définir la couleur des drones.
	*/
	static int m_droneBlue = 0;
	
	// les coefficients rgb de la couleur de l'�cran
	/**
	 * Coefficient de la couler rouge utilisée pour définir la couleur de l'écran.
	*/
	static int m_screenRed = 0;
	/**
	 * Coefficient de la couler verte utilisée pour définir la couleur de l'écran.
	*/
	static int m_screenGreen = 0;
	/**
	 * Coefficient de la couler bleue utilisée pour définir la couleur de l'écran.
	*/
	static int m_screenBlue = 0;
	
	// la largeur du terrain (en pixels)
	/**
	 * La largeur du terrain en pixels, calculée d'après les valeurs de m_width et m_dotSize.
	 * @see Constants#m_width
	 * @see Constants#m_dotSize
	*/
	static int m_pWidth = m_width * m_dotSize;
	
	// la hauteur du terrain (en pixels)
	/**
	 * La hauteur du terrain en pixels, calculée d'après les valeurs de m_height et m_dotSize.
	 * @see Constants#m_height
	 * @see Constants#m_dotSize
	*/
	static int m_pHeight = m_height * m_dotSize;
	
	/**
	 * <b>State est un type énuméré des états des drones.</b>
	 * <p>On a défini trois états possibles pour les drones : </p>
	 * <ul>
	 * 	<li>ALONE</li>
	 * 	<li>FLEET</li>
	 *  	<li>FUSION</li>
	 * </ul>
	 * <p>Ces états répresentent, respectivement, un drone qui n'a pas de flotte, 
	 * un drone qui appartient déjà à une flotte et un dron qui appartient à une flotte qui est en train de fussioner.</p>
	 * @see Drone
	 */
	static enum State{ALONE, FLEET, FUSION};
	
	/**
	 * Cette méthode statique permet de changer le nombre de drones sur le terrain
	 * et d'actualiser la valeur statique stockée dans cette classe.
	 * 
	 * @param numberDrones
	 * 		Le nouveau nombre de drones sur le terrain.
	 * 
	 * @see Constants#m_numberDrones
	*/
	static public void setNumberDrones(int numberDrones)
	{
		m_numberDrones = numberDrones;
	}
	
	// renvoie une couleur al�atoire
	/**
	 * Cette méthode statique retourne une couleur aléatoire entre 0 (noir) et 2^32 (blanc).
	 * 
	 * @return La couleur aléatoire générée.
	*/
	public static Long randomColor()
	{	
		return (Long) Math.round(Math.random() * Math.pow(2, 32));
	}
	
	/**
	 * Cette méthode statique retourne la valeur numérique correpondante avec la couleur blanche.
	 * 
	 * @return La valeur numérique de la couleur blanche.
	*/
	public static Long whiteColor()
	{
		return (Long) Math.round(Math.pow(2, 32));
	}
	
	// m�thode qui permet de traduire les param�tres du drones pass�s au format JSON
	// en tableau de param�tres Object (position, id, etc.)
	// je l'ai mise dans ce fichier parce que les drones doivent l'utiliser aussi a priori
	
	/**
	 * Cette méthode permet transformer une chaîne JSON en un tableau de paramètres avec l'ID du drone,
	 * sa position et le TreeMap contenant la liste du reste de membres de la flotte avec leurs positions. S'il y a une erreur lors de 
	 * la traduction, le programme est terminé immédiatement.
	 * 
	 * @param message
	 * 		La chaîne JSON que l'on veut transformer en table.
	 * @return La table contenant les objets qui représentent l'état d'un drone.
	 * 
	 * @see Drone
	 * 
	*/
	static public Map<String, Object> fromJSONArray(String message)
	{
		JSONParser jsonParser = new JSONParser();
		JSONArray args;
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		try 
		{
			args = (JSONArray) jsonParser.parse(message);
			
			// l'id du drone se trouve � la position 0
			JSONObject values = (JSONObject) args.get(0);
			int id = Integer.parseInt((values.get("id")).toString());
			parameters.put("id", id);
			
			// la position du drone se trouve � l'index 1 (voir encodage JSON dans Drone)
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
		
		// on renvoie la map des param�tres
		return parameters;
	}
}

