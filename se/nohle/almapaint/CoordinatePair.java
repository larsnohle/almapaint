/*
 Copyright 2012 Lars Nohle

 This file is part of AlmaPaint.

 AlmaPaint is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 AlmaPaint is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with AlmaPaint.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.nohle.almapaint;

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

  /**
   * Copy Constructor
   *
   */
  public CoordinatePair(CoordinatePair that)
  {
    this.x = that.x;
    this.y = that.y;
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
   * @return this - that
   */
  public CoordinatePair difference(CoordinatePair that)
  {
    return new CoordinatePair(this.x - that.x, this.y - that.y);
  }

  /**
   * Adds a CoordinatePair to this object.
   *
   * @param that The CoordinatePair to add.
   * @return The sum of this and that.
   */
  public CoordinatePair add(CoordinatePair that)
  {
    return new CoordinatePair(this.x + that.x, this.y + that.y);
  }
}
