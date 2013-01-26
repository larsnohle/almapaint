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
class LineShape extends AbstractDrawableShape
{
  private enum LineResizeArea
  {
    FIRST_POINT, SECOND_POINT
  }

  private int startX; 
  private int startY; 
  private int endX; 
  private int endY; 
  private LineResizeArea selectedResizeArea;

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
    this(color, startPoint.x, startPoint.y, endPoint.x, endPoint.y, strokeWidth, false);
  }

  /**
   * Copy constructor
   *
   * @param that The Line top copy.
   */
  LineShape(LineShape that)
  {
    this(that.color, that.startX, that.startY, that.endX, that.endY,
      that.strokeWidth, that.selected);

    copyTranslationAndResizeVectors(that);
  }

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startX The x coordinate of the start point of the line
   * @param startY The y coordinate of the start point of the line
   * @param endX The x coordinate of the end point of the line
   * @param endY The y coordinate of the end point of the line
   * @param strokeWidth The width of the pen.
   * @param selected true if the line is selected.
   */
   private LineShape(Color color, int startX, int startY, int endX, int endY, 
            int strokeWidth, boolean selected)
  {
    super(strokeWidth, color, selected);
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
    else if (resizeVector != null)
    {
      TwoPointsTuple tpt = calculateLinePointsWithRegardingToResizeVector();
      startXToUse = tpt.getStartX();
      startYToUse = tpt.getStartY();
      endXToUse = tpt.getEndX();
      endYToUse = tpt.getEndY();
    }

    Graphics2D g2 = (Graphics2D)g;
    g2.drawLine(startXToUse, startYToUse, endXToUse, endYToUse);

    //----------------------------------------------------------
    // Is this shape selected? In that case we draw two small rectangles
    // at the ends of the line.
    //----------------------------------------------------------
    if (isSelected())
    {
      int rectWidthAndHight = getWidthOfMarkerSquare();
      g2.fillRect(startXToUse - rectWidthAndHight / 2, startYToUse - rectWidthAndHight / 2,
        rectWidthAndHight, rectWidthAndHight);
      g2.fillRect(endXToUse - rectWidthAndHight / 2, endYToUse - rectWidthAndHight / 2,
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
    return Utilities.distanceBetweenLineAndPoint(startX, startY, endX, endY, point) <= strokeWidth;
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
    if (!isSelected())
    {
      return false;
    }

    int rectWidthAndHight = getWidthOfMarkerSquare();
    int leftXOfFirstSquare = startX - rectWidthAndHight / 2;
    int leftXOfSecondSquare = endX- rectWidthAndHight / 2;
    int topYOfFirstSquare = startY - rectWidthAndHight / 2;
    int topYOfSecondSquare = endY - rectWidthAndHight / 2;

    return pointInRectangle(point, leftXOfFirstSquare, topYOfFirstSquare, rectWidthAndHight, rectWidthAndHight)||
      pointInRectangle(point, leftXOfSecondSquare, topYOfSecondSquare, rectWidthAndHight, rectWidthAndHight);
  }

  /**
   * Sets the resize area that the user has selected.
   */
  @Override
  public void setSelectedResizeArea(CoordinatePair point)
  {
    double distanceToFirstPoint = Utilities.distanceBetweenPoints(startX, startY, point);
    double distanceToSecondPoint = Utilities.distanceBetweenPoints(endX, endY, point);

    if (distanceToFirstPoint < distanceToSecondPoint)
    {
      selectedResizeArea = LineResizeArea.FIRST_POINT;
    }
    else
    {
      selectedResizeArea = LineResizeArea.SECOND_POINT;
    }
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
   * Translates the coordinates of the start point, the width and the height by the amount specified
   * by the resize vector, which then is nulled out.
   */
  @Override
  public void incorporateResizeVector()
  {
    TwoPointsTuple tpt = calculateLinePointsWithRegardingToResizeVector();
    startX = tpt.getStartX();
    startY = tpt.getStartY();
    endX = tpt.getEndX();
    endY = tpt.getEndY();

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
    return new LineShape(this);
  }

  //----------------------------------------------------------
  // PRIVATE METHODS.
  //----------------------------------------------------------

  /**
   * Calculated the start and end points this line shape should have based on the resize vector.
   *
   * @return The start- and end-points this line shape should have with regards to the resize vector.
   */
  private TwoPointsTuple calculateLinePointsWithRegardingToResizeVector()
  {
    int startXToUse = startX;
    int startYToUse = startY;
    int endXToUse = endX;
    int endYToUse = endY;

    if (selectedResizeArea == LineResizeArea.FIRST_POINT)
    {
      startXToUse += resizeVector.x;
      startYToUse += resizeVector.y;
    }
    else if (selectedResizeArea == LineResizeArea.SECOND_POINT)
    {
      endXToUse += resizeVector.x;
      endYToUse += resizeVector.y;
    }

    return new TwoPointsTuple(startXToUse, startYToUse, endXToUse, endYToUse);
  }


  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  // INNER CLASS
  //
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP

  /**
   * Struct like class containing the same data that is used by the surrounding class
   * to define where to draw the line.
   */
  private static class TwoPointsTuple
  {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    private TwoPointsTuple(int startX, int startY, int endX, int endY)
    {
      this.startX = startX;
      this.startY = startY;
      this.endX = endX;
      this.endY = endY;
    }

    public int getStartX()
    {
      return startX;
    }

    public int getStartY()
    {
      return startY;
    }

    public int getEndX()
    {
      return endX;
    }

    public int getEndY()
    {
      return endY;
    }
  }

}
