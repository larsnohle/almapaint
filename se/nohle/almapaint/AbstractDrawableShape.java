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

import java.awt.*;
import java.io.*;
/**
 * Abstract implementation of DrawableShape. Currently empy, but might be handy if 
 * we find something that is common to all DrawableShapes. 
 */
abstract class AbstractDrawableShape implements DrawableShape
{
  protected transient Stroke stroke;
  protected int strokeWidth;
  protected Color color;
  protected transient CoordinatePair resizeVector;
  protected transient CoordinatePair translationVector;
  private final static long serialVersionUID = 123457890L;

  /** Is this shape selected? */
  protected boolean selected;

  protected AbstractDrawableShape(int strokeWidth, Color color)
  {
    this(strokeWidth, color, false);
  }

  protected AbstractDrawableShape(int strokeWidth, Color color, boolean selected)
  {
    this.strokeWidth = strokeWidth;
    this.stroke = new BasicStroke(strokeWidth);
    this.color = color;
    this.selected = selected;
  }

  /**
   * Copies the translation vector and resize vector from the specified shape.
   * Can be used by copy constructors of derived classes.'
   *
   * @param that The shape whose translation and resize vecors should be copied into this object.
   */
  protected void copyTranslationAndResizeVectors(AbstractDrawableShape that)
  {
    if (that.translationVector != null)
    {
      this.translationVector = new CoordinatePair(that.translationVector.x,
        that.translationVector.y);
    }

    if (that.resizeVector != null)
    {
      this.resizeVector = new CoordinatePair(that.resizeVector.x, that.resizeVector.y);
    }
  }

  @Override
  public void draw(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;   
    g2.setStroke(stroke);
    g2.setColor(this.color);
  }

  /**
   * Should return True if the specified point is included in this shape.
   *
   * @param point The point to check if it is included or not.
   * @return true if point is included in this shape, false if not.
   */
  @Override
  public boolean isPointIncluded(CoordinatePair point)
  {
    return false;
  }

  /**
   * Should return true if the specified point is located in a resize area of the shape.
   *
   * @param point The point to check.
   * @return false
   */
  @Override
  public boolean isPointInResizeArea(CoordinatePair point)
  {
    return false;
  }

  /**
   * Sets the resize area that the user has selected.
   */
  public void setSelectedResizeArea(CoordinatePair point)
  {

  }

  /**
   * Should set the resize vector to use when drawing this shape.
   *
   * @param resizeVector The translation vector.
   */
  @Override
  public void setResizeVector(CoordinatePair resizeVector)
  {
    this.resizeVector = resizeVector;
  }

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the resize vector, which then is nulled out.
   */
  @Override
  public void incorporateResizeVector()
  {
    this.resizeVector = null;
  }

  /**
   * Sets the translation vector to use when drawing this shape.
   *
   * @param translationVector The translation vector.
   */
  @Override
  public void setTranslationVector(CoordinatePair translationVector)
  {
    this.translationVector = translationVector;    
  }

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the translation vector, which then is nulled out.
   *
   * This implementation just nulls out the translation vector.
   */
  @Override
  public void incorporateTranslationVector()
  {
    translationVector = null;
  }
  
  /**
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  @Override
  public DrawableShape createClone()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Should mark this shape in some way indicated that it is "selected".
   */
  @Override
  public void select()
  {
    selected = true;
  }

  /**
   * Should remove any mark to indicate that this shape isn't "selected".
   */
  @Override
  public void unselect()
  {
    selected = false;
  }

  //----------------------------------------------------------
  // PROTECTED METHODS.
  //----------------------------------------------------------

  /**
   * Returns true if this shape is selected and false if it is not.
   *
   * @return true if this shape is selected, false otherwise.
   */
  protected boolean isSelected()
  {
    return selected;
  }

  protected int getWidthOfMarkerSquare()
  {
    int rectWidthAndHight = (int)(strokeWidth * 1.5);
    if (rectWidthAndHight < 5)
    {
      rectWidthAndHight = 5;
    }

    return rectWidthAndHight;
  }

  //----------------------------------------------------------
  // PRIVATE METHODS.
  //---------------------------------------------------------- 

  private void readObject(ObjectInputStream ois) 
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    
    strokeWidth = ois.readInt();
    color = (Color)ois.readObject();
    stroke = new BasicStroke(strokeWidth);    
  }

  private void writeObject(ObjectOutputStream oos) 
    throws IOException
  {
    oos.defaultWriteObject();
    
    oos.writeInt(strokeWidth);
    oos.writeObject(color);  
  }
}
