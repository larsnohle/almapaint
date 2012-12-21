package se.nohle.kurser.java2.paint;
import java.awt.*;

/**
 * A shape in form of a line,
 */
class LineShape extends AbstractDrawableShape
{
  private int startX; 
  private int startY; 
  private int endX; 
  private int endY; 

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startPoint The start point of the line
   * @param endPoint The end point of the line,
   */
  LineShape(Color color, CoordinatePair startPoint, CoordinatePair endPoint, 
            int strokeWidth)
  {
    this(color, startPoint.x, startPoint.y, endPoint.x, endPoint.y, strokeWidth);
  }

  /**
   * Copy constructor
   *
   * @param that The Line top copy.
   */
  LineShape(LineShape that)
  {
    this(that.color, that.startX, that.startY, that.endX, that.endY, that.strokeWidth);
  }

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startPoint The start point of the line
   * @param endPoint The end point of the line,
   */
   private LineShape(Color color, int startX, int startY, int endX, int endY, 
            int strokeWidth)
  {
    super(strokeWidth, color);
    this.startX = startX;
    this.startY = startY;

    this.endX = endX;
    this.endY = endY;
  }

  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    //----------------------------------------------------------
    // Should we translate the coordinate system (a.k.a. is this circle
    // dragged right now?)
    //---------------------------------------------------------- 
    int startXToUse = startX;
    int startYToUse = startY;
    int endXToUse = endX;
    int endYToUse = endY;

    if (translationVector != null)
    {
      startXToUse += translationVector.x;
      startYToUse+= translationVector.y;
      endXToUse += translationVector.x;
      endYToUse+= translationVector.y;
    }

    Graphics2D g2 = (Graphics2D)g;
    g2.drawLine(startXToUse, startYToUse, endXToUse, endYToUse);
  }

  /**
   * Returns True if the specified point is included in this shape.
   *
   * @param point The point to check if it is included or not.
   * @return true if point is included in this shape, false if not.
   */
  @Override
  public boolean isPointIncluded(CoordinatePair point)
  {
    return Utilities.distanceBetweenLineAndPoint(startX, startY, endX, endY, point) <= strokeWidth;
  }

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the translation vector, which then is nulled out.
   */
  @Override
  public void incorporateTranslationVector()
  {
    if (translationVector != null)
    {
      startX += translationVector.x;
      startY += translationVector.y;

      endX += translationVector.x;
      endY += translationVector.y;
    }

    super.incorporateTranslationVector();
  }

  /**
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  @Override
  public DrawableShape createClone()
  {
    return new LineShape(this);
  }
}
