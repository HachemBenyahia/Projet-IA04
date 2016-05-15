package main;

// classe qui recense les constantes utilisées par le programme
public class Constants 
{
	// la période de récupération des positions des drones (en ms) 
	static int retrievePositionsPeriod = 3000;
	
	// la période de mise à jour de la GUI (en ms)
	static int updateGUIPeriod = 1000;
	
	// la largeur du terrain (en nombre de cellules)
	static int environmentWidth = 40;
	
	// la hauteur du terrain (en nombre de cellules)
	static int environmentHeight = 40;
	
	// le nombre de drones sur le terrain
	static int numberDrones = 5;
	
	// la taille d'une cellule sur le terrain (en pixels)
	static int dotSize = 10;

	// les coefficients rgb de la couleur du drone
	static int droneRed = 255;
	static int droneGreen = 0;
	static int droneBlue = 0;
}