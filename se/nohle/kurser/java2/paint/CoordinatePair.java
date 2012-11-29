package se.nohle.kurser.java2.paint;

/**
 * Models a coordinate point.
 */
public class CoordinatePair implements java.io.Serializable
{
  protected final int x;
  protected final int y;

  /**
   * Constructor
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  public CoordinatePair(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ")"; 
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == this)
    {
      return true;
    }

    if (!(o instanceof CoordinatePair))
    {
      return false;
    }

    CoordinatePair that = (CoordinatePair)o;

    if (this.x == that.x && this.y == that.y)
    {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    return x + 37 * y;
  }
  
  /**
   * Return the difference this - that.
   *
   * @param that The point to use to compute difference with.
   * @retun this - that
   */
  public CoordinatePair difference(CoordinatePair that)
  {
    return new CoordinatePair(this.x - that.x, this.y - that.y);
  }
}
