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
import static se.nohle.almapaint.Utilities.pointInRectangle;

/**
 * A shape in form of a line,
 */
class CircleShape extends AbstractDrawableShape
{
  private enum CircleResizeArea
  {
    TOP, RIGHT, BOTTOM, LEFT
  }

  private final Color color; 
  private int topLeftX; 
  private int topLeftY; 
  private CoordinatePair centerPoint;
  private int radius;
  private final boolean fill;

  private CircleResizeArea selectedResizeArea;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param centerPoint The center point of box framing the circle.
   * @param radius The radius of the circle.
   * @param fill true if the circle should be filled.
   * @param strokeWidth The width of the pen.
   * @param selected true if it should be indicated that the circle is selected.
   */
  CircleShape(Color color, CoordinatePair centerPoint, int radius,
              boolean fill, int strokeWidth, boolean selected)
  {
    super(strokeWidth, color, selected);
        
    this.color = color;
    this.fill = fill;
    this.centerPoint = centerPoint;
    this.radius = radius;

    initializeTopLeftCoordinatesFromRadius(radius);
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
         fill, strokeWidth, false);
  }

  /**
   * Copy constructor
   *
   * @param that The circle to copy.
   */
  CircleShape(CircleShape that)
  {
    this(that.color, that.centerPoint, that.radius, that.fill, that.strokeWidth, that.selected);

    copyTranslationAndResizeVectors(that);
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
    int radiusToUse = radius;

    if (translationVector != null)
    {
      topLeftXToUse += translationVector.x;
      topLeftYToUse += translationVector.y;
    }
    else if (resizeVector != null)
    {
      radiusToUse = calculateRadiusFromResizeVector();
      topLeftXToUse = centerPoint.x - radiusToUse;
      topLeftYToUse = centerPoint.y - radiusToUse;
    }

    // The width and height are double the length of the radius.
    int side = 2 * radiusToUse;

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
   * Should return true if the specified point is located in a resize area of the shape.
   *
   * @param point The point to check.
   * @return true if point is located in a resize area.
   */
  @Override
  public boolean isPointInResizeArea(CoordinatePair point)
  {
    return getResizeAreaForPoint(point) != null;
  }

  /**
   * Sets the resize area that the user has selected.
   */
  @Override
  public void setSelectedResizeArea(CoordinatePair point)
  {
    selectedResizeArea = getResizeAreaForPoint(point);
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
   * Translates the coordinates used to draw this shape by the amount specified
   * by the resize vector, which then is nulled out.
   */
  @Override
  public void incorporateResizeVector()
  {
    if (resizeVector != null)
    {
      radius = calculateRadiusFromResizeVector();
      topLeftX = centerPoint.x - radius;
      topLeftY = centerPoint.y - radius;
    }

    super.incorporateResizeVector();
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

  /**
   * Initializes topLeftX and topLeftY from a radius.
   *
   * @param radius The radius to use for the initialization.
   */
  private void initializeTopLeftCoordinatesFromRadius(int radius)
  {
    topLeftX = centerPoint.x - radius;
    topLeftY = centerPoint.y - radius;
  }

  /**
   * Checks if a point is included in a circle.
   *
   * @param point The point to check.
   * @param centerPoint The center point of the circle to check.
   * @param radius The radius of the circle to check.
   * @return true if point is located inside the circle, false if not.
   */
  private static boolean isPointInCircle(CoordinatePair point, 
                                         CoordinatePair centerPoint, int radius)
  {
    // Translate the points so that the center point is in origo.
    int x = point.x - centerPoint.x;
    int y = point.y - centerPoint.y;
    return Math.sqrt(x * x + y * y) <= radius;
  }

  /**
   * Returns the resize area which includes the specified point, or null if no resize area contains the point.
   *
   * @param point The point to check.
   * @return The resize area in which the point is located or null if no such resize area exist.
   */
  private CircleResizeArea getResizeAreaForPoint(CoordinatePair point)
  {
    if (!isSelected())
    {
      return null;
    }

    // The width and height are double the length of the radius.
    int side = 2 * radius;
    int rectWidthAndHight = getWidthOfMarkerSquare();
    int leftXOfMiddleSquares = topLeftX - rectWidthAndHight / 2 + side / 2;
    int topYOfUpperSquare =  topLeftY - rectWidthAndHight / 2;
    int leftXOfRightSquare = topLeftX - rectWidthAndHight / 2 + side;
    int topYOfMiddleSquares = topLeftY - rectWidthAndHight / 2 + side / 2;
    int topYOfBottomSquare = topLeftY - rectWidthAndHight / 2 + side;
    int leftXOfLeftSquare = topLeftX - rectWidthAndHight / 2;

    if (pointInRectangle(point,leftXOfMiddleSquares,topYOfUpperSquare, rectWidthAndHight, rectWidthAndHight))
    {
      return CircleResizeArea.TOP;
    }
    else if (pointInRectangle(point, leftXOfRightSquare, topYOfMiddleSquares, rectWidthAndHight, rectWidthAndHight))
    {
      return CircleResizeArea.RIGHT;
    }
    else if (pointInRectangle(point, leftXOfLeftSquare, topYOfMiddleSquares, rectWidthAndHight, rectWidthAndHight))
    {
      return CircleResizeArea.LEFT;
    }
    else if (pointInRectangle(point, leftXOfMiddleSquares, topYOfBottomSquare, rectWidthAndHight, rectWidthAndHight))
    {
      return CircleResizeArea.BOTTOM;
    }

    return null;
  }

  /**
   * Calculates a new radius to use when drawing this circle shape when its resizing.
   *
   * @return the new radius to use when this circle is resizing.
   */
  private int calculateRadiusFromResizeVector()
  {
    int radiusToUse = radius;

    if (selectedResizeArea == CircleResizeArea.LEFT)
    {
      radiusToUse -= resizeVector.x;
    }
    else if (selectedResizeArea == CircleResizeArea.RIGHT)
    {
      radiusToUse += resizeVector.x;
    }
    else if (selectedResizeArea == CircleResizeArea.TOP)
    {
      radiusToUse -= resizeVector.y;
    }
    else if (selectedResizeArea == CircleResizeArea.BOTTOM)
    {
      radiusToUse += resizeVector.y;
    }

    // As the mouse pointer might have moved passed the center point (and the calculated radius hence is negative)
    // we return the absolute value of the calculated radius.
    return Math.abs(radiusToUse);
  }
}
