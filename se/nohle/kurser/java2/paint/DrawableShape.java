package se.nohle.kurser.java2.paint;
import java.awt.Graphics;
import java.io.Serializable;

/**
 * Interface implemented by all shapes in this application.
 */
public interface DrawableShape extends Serializable, Cloneable
{
  /**
   * Should draw the shape on to the specified Graphics.
   *
   * @param g The Graphics object to draw on.
   */
  public void draw(Graphics g);


  /**
   * Should return True if the specified point is included in this shape.
   *
   * @param point The point to check if it is included or not.
   * @return true if point is included in this shape, false if not.
   */
  public boolean isPointIncluded(CoordinatePair point);

  /**
   * Should set the translation vector to use when drawing this shape.
   *
   * @param translationVector The translation vector.
   */
  public void setTranslationVector(CoordinatePair translationVector);

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the translation vector, which then is nulled out.
   */
  public void incorporateTranslationVector();

  /**
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  public DrawableShape createClone();
}
