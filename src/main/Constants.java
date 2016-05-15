package main;

// classe qui recense les constantes utilis�es par le programme
public class Constants 
{
	// la p�riode de r�cup�ration des positions des drones (en ms) 
	static int retrievePositionsPeriod = 3000;
	
	// la p�riode de mise � jour de la GUI (en ms)
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