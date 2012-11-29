package se.nohle.kurser.java2.paint;

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
  protected CoordinatePair translationVector;
  private final static long serialVersionUID = 123457890L;

  protected AbstractDrawableShape(int strokeWidth, Color color)
  {
    this.strokeWidth = strokeWidth;
    this.stroke = new BasicStroke(strokeWidth);
    this.color = color;
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
