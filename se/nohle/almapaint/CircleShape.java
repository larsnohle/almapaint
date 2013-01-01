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

/**
 * A shape in form of a line,
 */
class CircleShape extends AbstractDrawableShape
{
  private final Color color; 
  private int topLeftX; 
  private int topLeftY; 
  private CoordinatePair centerPoint;
  private final int radius; 
  private final boolean fill;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param centerPoint The center point of box framing the circle.
   * @param radius The radius of the circle.
   * @param fill true if the circle should be filled.
   * @param strokeWidth The width of the pen.
   */
  CircleShape(Color color, CoordinatePair centerPoint, int radius,
              boolean fill, int strokeWidth)
  {
    super(strokeWidth, color);
        
    this.color = color;
    this.fill = fill;
    this.centerPoint = centerPoint;
    this.radius = radius;

    topLeftX = centerPoint.x - radius;
    topLeftY = centerPoint.y - radius;
  }

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param centerPoint The center point of box framing the circle.
     @param endPoint The other point of the box framing the circle.
   * @param fill true if the circle should be filled.
   * @param strokeWidth The width of the pen.
   */
  CircleShape(Color color, CoordinatePair centerPoint, CoordinatePair endPoint,
              boolean fill, int strokeWidth)
  {
    this(color, centerPoint, 
         (int)Math.sqrt(Math.pow(centerPoint.x - endPoint.x, 2) + 
                        Math.pow(centerPoint.y - endPoint.y, 2)),
         fill, strokeWidth);
  }

  /**
   * Copy constructor
   *
   * @param that The circle to copy.
   */
  CircleShape(CircleShape that)
  {
    this(that.color, that.centerPoint, that.radius, that.fill, that.strokeWidth);
  }


  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    Graphics2D g2 = (Graphics2D)g;

    //----------------------------------------------------------
    // Should we translate the coordinate system (a.k.a. is this circle
    // dragged right now?)
    //---------------------------------------------------------- 
    int topLeftXToUse = topLeftX;
    int topLeftYToUse = topLeftY;

    if (translationVector != null)
    {
      topLeftXToUse += translationVector.x;
      topLeftYToUse += translationVector.y;
    }

    // The width and height are double the length of the radius.
    int side = 2 * radius;

    if (fill)
    {
      g2.fillOval(topLeftXToUse, topLeftYToUse, side, side);
    }
    else
    {
      g2.drawOval(topLeftXToUse, topLeftYToUse, side, side);
    }

    //----------------------------------------------------------
    // Is this shape selected? In that case we draw four small rectangles
    // to indicate that it is.
    //----------------------------------------------------------
    if (isSelected())
    {
      int rectWidthAndHight = getWidthOfMarkerSquare();
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2 + side / 2,
        topLeftYToUse - rectWidthAndHight / 2,
        rectWidthAndHight, rectWidthAndHight);
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2 + side / 2,
        topLeftYToUse - rectWidthAndHight / 2 + side,
        rectWidthAndHight, rectWidthAndHight);

      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2,
        topLeftYToUse - rectWidthAndHight / 2 + side / 2,
        rectWidthAndHight, rectWidthAndHight);
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2 + side,
        topLeftYToUse - rectWidthAndHight / 2 + side / 2,
        rectWidthAndHight, rectWidthAndHight);
    }
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
    if (fill)
    {
      return isPointInCircle(point, centerPoint, radius);
    }

    return isPointInCircle(point, centerPoint, radius + strokeWidth) && 
     !isPointInCircle(point, centerPoint, radius - strokeWidth);      
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

      centerPoint = centerPoint.add(translationVector);
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
    return new CircleShape(this);
  }

  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // PRIVATE METHODS.
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP 
  
  private static boolean isPointInCircle(CoordinatePair point, 
                                         CoordinatePair centerPoint, int radius)
  {
    // Translate the points so that the center point is in origo.
    int x = point.x - centerPoint.x;
    int y = point.y - centerPoint.y;
    return Math.sqrt(x * x + y * y) <= radius;
  }
}
