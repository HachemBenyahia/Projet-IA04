package main;

// classe simple qui représente une position (x, y)
// elle deviendra notament utile pour le filtrage des distances
// et autres calculs éventuels du genre
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
		else if(m_x > Constants.pWidth)
			m_x = Constants.pWidth;
	}
	
	public void addY(int y)
	{
		m_y += y;
		
		if(m_y < 0)
			m_y = 0;
		else if(m_y > Constants.pHeight)
			m_y = Constants.pHeight;	
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
		addY(-Constants.dotSize);
	}
	
	public void left()
	{
		addX(-Constants.dotSize);
	}
	
	public void right()
	{
		addX(Constants.dotSize);
	}
	
	public void down()
	{
		addY(Constants.dotSize);
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
	
	// renvoie la distance au point passé en paramètre
	public double getDistance(Position position)
	{	
		return Math.sqrt(Math.pow(position.getX() - m_x, 2) + Math.pow(position.getY() - m_y, 2));
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
}
