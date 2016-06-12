package main;

// classe simple qui représente une position (x, y)
// elle deviendra notament utile pour le filtrage des distances
// et autres calculs éventuels du genre

/**
 * <b>Position est la classe représentant une position quelconque de la grille.</b>
 * <p>Un membre de cette classe est caractérisé par les informations suivantes : </p>
 * <ul>
 * 	<li>Un entier representant la valeur des abscisses.</li>
 * 	<li>Un entier representant la valeur des ordonnées.</li>
 * </ul>
 */
public class Position 
{
	/**
	 * La coordonnée X de l'instance de la classe Position, il faut passer au constructeur une coordonnée X par défaut.
	 * @see Position#Position
	*/
	int m_x;
	
	/**
	 * La coordonnée Y de l'instance de la classe Position, il faut passer au constructeur une coordonnée Y par défaut.
	 * @see Position#Position
	*/
	int m_y;
	
	/**
	 * C'est le constructeur de la classe, il faut fournir les coordonnées X et Y par défaut.
	 * 
	 * @param x
	 * 		Valeur initiale de la coordonnée X.
	 * @param y
	 * 		Valeur initiale de la coordonnée Y.
	 * 
	 * @see Position#Random
	*/
	Position(int x, int y)
	{
		m_x = x;
		m_y = y;
	}
	
	/**
	 * Cette méthode permet de récupérer la valeur de la coordonnée X d'une instance de la classe Position.
	 * 
	 * @return Valeur de la coordonnée X.
	*/
	public int getX()
	{
		return m_x;
	}
	
	/**
	 * Cette méthode permet de récupérer la valeur de la coordonnée Y d'une instance de la classe Position.
	 * 
	 * @return Valeur de la coordonnée X.
	*/
	public int getY()
	{
		return m_y;
	}
	
	/**
	 * Cette méthode permet d'augmenter la valeur de la coordonnée X une quantité définie d'unités.
	 * Si la variable dépasse les limites de la grille, elle aura la valeur la plus grand possible.
	 * D'ailleurs, s'il y a un débordement de la variable on lui assigne la valeur 0.
	 * 
	 * @param x
	 * 		Le nombre d'unités que la coordonnée X sera incrémentée.
	 * @see Constants#m_pWidth
	*/
	public void addX(int x)
	{		
		m_x += x;
		
		if(m_x < 0)
			m_x = 0;
		else if(m_x > (Constants.m_pWidth - Constants.m_dotSize))
			m_x = Constants.m_pWidth - Constants.m_dotSize;
	}
	
	/**
	 * Cette méthode permet d'augmenter la valeur de la coordonnée Y une quatité définie d'unités.
	 * Si la variable dépasse les limites de la grille, elle aura la valeur la plus grand possible.
	 * D'ailleurs, s'il y a un débordement de la variable on lui assigne la valeur 0.
	 * 
	 * @param y
	 * 		Le nombre d'unités que la coordonnée Y sera incrémentée.
	 * @see Constants#m_pHeight
	*/
	public void addY(int y)
	{
		m_y += y;
		
		if(m_y < 0)
			m_y = 0;
		else if(m_y > (Constants.m_pHeight - Constants.m_dotSize))
			m_y = Constants.m_pHeight - Constants.m_dotSize;	
	}
	
	/**
	 * Cette méthode permet d'augmenter la valeur des coordonnées X et Y une quatité définie d'unités, respectivement.
	 * 
	 * @param x
	 * 		Le nombre d'unités que la coordonnée X sera incrémentée.
	 * @param y
	 * 		Le nombre d'unités que la coordonnée Y sera incrémentée.
	 * 
	 * @see Position#addX
	 * @see Position#addY
	*/
	public void add(int x, int y)
	{
		addX(x);
		addY(y);
	}
	
	/**
	 * Cette méthode permet d'augmenter une unité la valeur de la coordonnée X.
	 * 
	 * @see Position#addX
	*/
	public void incX()
	{
		addX(1);
	}
	
	/**
	 * Cette méthode permet d'augmenter une unité la valeur de la coordonnée Y.
	 * 
	 * @see Position#addY
	*/
	public void incY()
	{
		addY(1);
	}
	
	/**
	 * Cette méthode permet de diminuer une unité la valeur de la coordonnée X.
	 * 
	 * @see Position#addX
	*/
	public void decX()
	{
		addX(-1);
	}
	
	/**
	 * Cette méthode permet d'augmenter une unité la valeur de la coordonnée Y.
	 * 
	 * @see Position#addY
	*/
	public void decY()
	{
		addY(-1);
	}
	
	/**
	 * Cette méthode permet de diminuer la valeur de la coordonnée X le même nombre d'unités que la taille d'un point,
	 * cette dernière est définie dans la classe Constants.
	 * 
	 * @see Position#addY
	 * @see Constants#m_dotSize
	*/
	public void up()
	{
		addY(-Constants.m_dotSize);
	}
	
	/**
	 * Cette méthode permet de diminuer la valeur de la coordonnée Y le même nombre d'unités que la taille d'un point,
	 * cette dernière est définie dans la classe Constants.
	 * 
	 * @see Position#addX
	 * @see Constants#m_dotSize
	*/
	public void left()
	{
		addX(-Constants.m_dotSize);
	}
	
	/**
	 * Cette méthode permet d'augmenter la valeur de la coordonnée X le même nombre d'unités que la taille d'un point,
	 * cette dernière est définie dans la classe Constants.
	 * 
	 * @see Position#addX
	 * @see Constants#m_dotSize
	*/
	public void right()
	{
		addX(Constants.m_dotSize);
	}
	
	/**
	 * Cette méthode permet d'augmenter la valeur de la coordonnée X le même nombre d'unités que la taille d'un point,
	 * cette dernière est définie dans la classe Constants.
	 * 
	 * @see Position#addY
	 * @see Position#m_dotSize
	*/
	public void down()
	{
		addY(Constants.m_dotSize);
	}
	
	/**
	 * Cette méthode équivaut à executer de manière séquentielle les méthodes up() et left() de la classe Position.
	 * 
	 * @see Position#up
	 * @see Position#left
	*/
	public void upLeft()
	{
		up();
		left();
	}
	
	/**
	 * Cette méthode équivaut à executer de manière séquentielle les méthodes up() et right() de la classe Position.
	 * 
	 * @see Position#up
	 * @see Position#right
	*/
	public void upRight()
	{
		up();
		right();
	}
	
	/**
	 * Cette méthode équivaut à executer de manière séquentielle les méthodes down() et left() de la classe Position.
	 * 
	 * @see Position#down
	 * @see Position#left
	*/
	public void downLeft()
	{
		down();
		left();
	}
	
	/**
	 * Cette méthode équivaut à executer de manière séquentielle les méthodes down() et right() de la classe Position.
	 * 
	 * @see Position#down
	 * @see Position#right
	*/
	public void downRight()
	{
		down();
		right();
	}
	
	// modifie la position actuelle de manière à se rapprocher de l'objectif en paramètre
	
	/**
	 * Cette méthode modifie automatiquement les valeurs des coordonées d'une instance de la classe Position
	 * de façon à ce qu'elle s'approche d'une position cible donnée.
	 * 
	 * @param goal
	 * 		La position cible dont on veut s'apporcher.
	 * 
	 * @see Position#down
	 * @see Position#left
	 * @see Position#up
	 * @see Position#right
	*/
	public void moveTowards(Position goal)
	{
		int x = goal.getX() - m_x;
		int y = goal.getY() - m_y;
		
		if((x > 0) && (y > 0))
			downRight();
		
		else if((x > 0) && (y < 0))
			upRight();
		
		else if((x < 0) && (y > 0))
			downLeft();
		
		else if((x < 0) && (y < 0))
			upLeft();
		
		else if(x > 0)
			right();
		
		else if(x < 0)
			left();
		
		else if(y > 0)
			down();
		
		else if(y < 0)
			up();
	}
	
	// génère une position aléatoire sur le terrain
	/**
	 * Cette méthode statique génère une position aléatoire dans le terrain et la retourne, la position sera encadrée
	 * par les limites de la grille.
	 * 
	 * @return La position aléatoire générée.
	 * 
	 * @see Constants#m_width
	 * @see Constants#m_height
	 * @see Constants#m_dotSize
	*/
	static public Position random()
	{
		int x = (int) (Math.round(Math.random() * (Constants.m_width - 1))  * Constants.m_dotSize);
		int y = (int) (Math.round(Math.random() * (Constants.m_height - 1)) * Constants.m_dotSize);

		return new Position(x, y);
	}
	
	// renvoie la distance au point passé en paramètre
	
	/**
	 * Cette méthode calcule la distancie existante entre cette instance de la classe Position et une Position donnée.
	 * 
	 * @param position
	 * 		La position par rapport à laquelle on veut connaître la distance.
	 * 
	 * @return La distance calculée.
	 * 
	*/
	public int getDistance(Position position)
	{	
		return (int) Math.round(Math.sqrt(Math.pow(position.getX() - m_x, 2) + Math.pow(position.getY() - m_y, 2)));
	}
	
	// renvoie vrai si le signal on est à la portée de l'émetteur du signal
	
	/**
	 * Cette méthode indique si la distancie existante entre cette instance de la classe Position et une Position donnée
	 * est inférieure à la distance maximale de portée définie dans la classe Constants.
	 * 
	 * @param position
	 * 		La position que l'on veut savoir si elle est accesible.
	 * 
	 * @return Le resultat de la compairaison entre la distance calculée et la distance maximale de portée.
	 * 
	 * @see Position#getDistance
	 * @see Constants#m_maxRange
	 * 
	*/
	public boolean reachable(Position position)
	{
		if(getDistance(position) > Constants.m_maxRange)
			return false;
		
		return true;
	}
	
	/**
	 * Cette méthode permet d'assigner de nouvelles valeurs aux coordonnées X et Y d'une instance de la classe Position.
	 * 
	 * @param x
	 * 		La nouvelle valeur de la coordonnée X.
	 * @param y
	 * 		La nouvelle valeur de la coordonnée Y.
	 * 
	*/
	public void setPosition(int x, int y)
	{
		m_x = x;
		m_y = y;
	}
	
	/**
	 * Cette méthode permet de copier les valeurs des coordonnées X et Y d'une Position donnée.
	 * 
	 * @param position
	 * 		La position dont on veut prendre les coordonnées.
	 * 
	*/
	public void setPosition(Position position)
	{
		m_x = position.getX();
		m_y = position.getY();
	}
	
	/**
	 * Cette méthode permet de récupérer la chaîne representant l'état d'une instance de la classe Position.
	 * 
	 * @return La chaîne representant l'état d'une instance de la classe Position.
	 * 
	*/
	public String toString()
	{
		return "(" + m_x + ", " + m_y + ")";
	}
	
	/**
	 * Cette méthode indique si deux instances de la classe Position posèdent les mêmes coordonnées.
	 * 
	 * @return Le résultat de cette comparaison.
	 * 
	*/
	public boolean equals(Position position)
	{
		if((m_x == position.m_x) && (m_y == position.m_y))
			return true;
		
		return false;
	}
}
