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
    super(strokeWidth, color);
    startX = startPoint.x;
    startY = startPoint.y;

    endX = endPoint.x;
    endY = endPoint.y;
  }


  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    Graphics2D g2 = (Graphics2D)g;
    g2.drawLine(startX, startY, endX, endY);
  }

//   @Override
//   public String toString()
//   {
//     return "x: " + topLeftX + " y: " + topLeftY + " width: " + width + " height: " + height;
//  }
}
