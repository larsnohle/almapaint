package se.nohle.kurser.java2.paint;
import java.awt.*;

/**
 * A shape in form of a line,
 */
class CircleShape extends AbstractDrawableShape
{
  private Color color; 
  private int topLeftX; 
  private int topLeftY; 
  private int radius; 
  private boolean fill;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startPoint The one point of box framing the circle.
   * @param endPoint The other point of box framing the circle.
   */
  CircleShape(Color color, CoordinatePair centerPoint, CoordinatePair endPoint,
              boolean fill, int strokeWidth)
  {
    super(strokeWidth, color);
        
    this.color = color;
    this.fill = fill;
    radius = (int)Math.sqrt(Math.pow(centerPoint.x - endPoint.x, 2) + Math.pow(centerPoint.y - endPoint.y, 2) );

    topLeftX = centerPoint.x - radius / 2;
    topLeftY = centerPoint.y - radius / 2;
  }


  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    Graphics2D g2 = (Graphics2D)g;
    
    if (fill)
    {
      g2.fillOval(topLeftX, topLeftY, radius, radius);
    }
    else
    {
      g2.drawOval(topLeftX, topLeftY, radius, radius);
    }
  }
}
