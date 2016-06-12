package main;

import org.json.simple.JSONObject;

// classe simple qui repr�sente une position (x, y)
// elle deviendra notament utile pour le filtrage des distances
// et autres calculs �ventuels du genre
public class Position 
{
	int m_x, m_y;
	
	Position(int x, int y)
	{
		m_x = x;
		m_y = y;
	}

	public int getX()
	{
		return m_x;
	}
	
	public int getY()
	{
		return m_y;
	}
	
	public void addX(int x)
	{		
		m_x += x;
		
		if(m_x < 0)
			m_x = 0;
		else if(m_x > (Constants.m_pWidth - Constants.m_dotSize))
			m_x = Constants.m_pWidth - Constants.m_dotSize;
	}
	
	public void addY(int y)
	{
		m_y += y;
		
		if(m_y < 0)
			m_y = 0;
		else if(m_y > (Constants.m_pHeight - Constants.m_dotSize))
			m_y = Constants.m_pHeight - Constants.m_dotSize;	
	}
	
	public void add(int x, int y)
	{
		addX(x);
		addY(y);
	}
	
	public void incX()
	{
		addX(1);
	}
	
	public void incY()
	{
		addY(1);
	}
	
	public void decX()
	{
		addX(-1);
	}
	
	public void decY()
	{
		addY(-1);
	}
	
	public void up()
	{
		addY(-Constants.m_dotSize);
	}
	
	public void left()
	{
		addX(-Constants.m_dotSize);
	}
	
	public void right()
	{
		addX(Constants.m_dotSize);
	}
	
	public void down()
	{
		addY(Constants.m_dotSize);
	}
	
	public void upLeft()
	{
		up();
		left();
	}
	
	public void upRight()
	{
		up();
		right();
	}
	
	public void downLeft()
	{
		down();
		left();
	}
	
	public void downRight()
	{
		down();
		right();
	}
	
	// modifie la position actuelle de mani�re � se rapprocher de l'objectif en param�tre
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
	
	// g�n�re une position al�atoire sur le terrain
	static public Position random()
	{
		int x = (int) (Math.round(Math.random() * (Constants.m_width - 1)) * Constants.m_dotSize);
		int y = (int) (Math.round(Math.random() * (Constants.m_height - 1)) * Constants.m_dotSize);

		return new Position(x, y);
	}
	
	// renvoie la distance au point pass� en param�tre
	public int getDistance(Position position)
	{	
		return (int) Math.round(Math.sqrt(Math.pow(position.getX() - m_x, 2) + Math.pow(position.getY() - m_y, 2)));
	}
	
	// renvoie vrai si le signal on est � la port�e de l'�metteur du signal
	public boolean reachable(Position position)
	{
		if(getDistance(position) > Constants.m_maxRange)
			return false;
		
		return true;
	}
	
	public void setPosition(int x, int y)
	{
		m_x = x;
		m_y = y;
	}
	
	public void setPosition(Position position)
	{
		m_x = position.getX();
		m_y = position.getY();
	}
	
	public String toString()
	{
		return "(" + m_x + ", " + m_y + ")";
	}
	
	public boolean equals(Position position)
	{
		if((m_x == position.m_x) && (m_y == position.m_y))
			return true;
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson()
	{
		JSONObject position = new JSONObject();
		position.put("x", this.getX());
		position.put("y", this.getY());
		return position;
	}
}
