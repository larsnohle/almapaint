package se.nohle.kurser.java2.paint;
import java.awt.*;

/**
 * A rectangular shape,
 */
class Rectangle extends AbstractDrawableShape
{
  private int topLeftX; 
  private int topLeftY; 
  private int width;
  private int height;
  private boolean fill;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startPoint The start point of the user operation that resulted in the creation of this rectangle..
   * @param endPoint The end point,
   */
  Rectangle(Color color, CoordinatePair startPoint, CoordinatePair endPoint,
            boolean fill, int strokeWidth)
  {
    super(strokeWidth, color);
    this.fill = fill;
    topLeftX = Math.min(startPoint.x, endPoint.x);
    topLeftY = Math.min(startPoint.y, endPoint.y);
    
    width = Math.abs(startPoint.x - endPoint.x);
    height = Math.abs(startPoint.y - endPoint.y);
  }

  /**
   * Copy constructor.
   */
  private Rectangle(Rectangle that)
  {
    super(that.strokeWidth, that.color);
    this.topLeftX = that.topLeftX;
    this.topLeftY = that.topLeftY;
    this.width = that.width;
    this.height = that.height;
    this.fill = that.fill;

    if (that.translationVector != null)
    {
      this.translationVector = new CoordinatePair(that.translationVector.x, 
                                                  that.translationVector.y);
     }
  }  

  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    int topLeftXToUse = topLeftX;
    int topLeftYToUse = topLeftY;

    //----------------------------------------------------------
    // Should we translate the coordinate system (a.k.a. is this rectangle
    // dragged right now?)
    //---------------------------------------------------------- 
    if (translationVector != null)
    {
      topLeftXToUse += translationVector.x;
      topLeftYToUse += translationVector.y;
    }

    //----------------------------------------------------------
    // Should the triangle be filled?
    //---------------------------------------------------------- 
    Graphics2D g2 = (Graphics2D)g;
    if (fill)
    {
      g2.fillRect(topLeftXToUse, topLeftYToUse, width, height);
    }
    else
    {
      g2.drawRect(topLeftXToUse, topLeftYToUse, width, height);
    }
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
    if (fill)
    {
      return pointInRectangle(point, topLeftX, topLeftY, width, height);
    }

    int startXLeft = topLeftX - (strokeWidth + 1) / 2;
    int startYTop = topLeftY - (strokeWidth + 1) / 2;
    int startXRight = topLeftX + width  - (strokeWidth + 1) / 2;
    int startYBottom = topLeftY + height - (strokeWidth + 1) / 2;
    
    return pointInRectangle(point, startXLeft, startYTop, width, strokeWidth)||
     pointInRectangle(point, startXLeft, startYTop, strokeWidth, height)||
     pointInRectangle(point, startXRight, startYTop, strokeWidth, height)||
     pointInRectangle(point, startXLeft, startYBottom, width, strokeWidth);
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
      topLeftX += translationVector.x;
      topLeftY += translationVector.y;
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
    return new Rectangle(this);
  }

  @Override
  public String toString()
  {
    return "x: " + topLeftX + " y: " + topLeftY + " width: " + width + " height: " + height;
  }

  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // PRIVATE METHODS.
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP 

  /**
   * Determines if the specified point is inside the specified rectangle.
   */
  private static boolean pointInRectangle(CoordinatePair point, 
                                          int topLeftX, int topLeftY, 
                                          int width, int height)
  {
    if (point.x < topLeftX || point.x > topLeftX + width || 
          point.y < topLeftY || point.y > topLeftY + height) {
        return false;
      }
    
    return true;
  }

}
