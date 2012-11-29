package se.nohle.kurser.java2.paint;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Shape for freehand drawings.
 */
class FreehandShape extends AbstractDrawableShape
{
  private List<CoordinatePair> coordinatePoints = new ArrayList<>();

  /**
   * Constructor
   *
   * @param The color to use.
   */
  FreehandShape(Color color, int strokeWidth)
  {
    super(strokeWidth, color);
  }

  /**
   * Adds a point to this freehand drawing.
   *
   * @param point The point to add.
   */
  void addPoint(CoordinatePair point)
  {
    coordinatePoints.add(point);  }

  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    Graphics2D g2 = (Graphics2D)g;

    for (int i = 1; i < coordinatePoints.size() - 1; i++)
    {
      CoordinatePair startPoint = coordinatePoints.get(i);
      CoordinatePair endPoint = coordinatePoints.get(i + 1);

      g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }
  }
}
