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

	public void setPosition(int x, int y)
	{
		m_x = x;
		m_y = y;
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
