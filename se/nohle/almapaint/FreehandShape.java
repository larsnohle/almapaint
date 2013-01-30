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
import java.util.List;
import java.util.ArrayList;

import static se.nohle.almapaint.Utilities.pointInRectangle;

/**
 * Shape for freehand drawings.
 */
class FreehandShape extends AbstractDrawableShape
{
  private enum FreehandResizeArea
  {
    BEFORE, AFTER
  }

  /** The coordinates making up this shape.  */
  private List<CoordinatePair> coordinatePoints = new ArrayList<>();

  /** Fields containing data used when resizing this shape. */
  private List<CoordinatePair> coordinatePointsWhenResizing = new ArrayList<>();
  private FreehandResizeArea selectedResizePoint;
  private CoordinatePair resizeStartPoint;
  private CoordinatePair latestResizePosition;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param strokeWidth The width of the pen.
   */
  FreehandShape(Color color, int strokeWidth)
  {
    super(strokeWidth, color);
  }

  /**
   * Copy constructor
   *
   * @param that The object to copy.
   */
  FreehandShape(FreehandShape that)
  {
    super(that.strokeWidth, that.color, that.selected);
    
    for (CoordinatePair point : that.coordinatePoints)
    {
      coordinatePoints.add(new CoordinatePair(point));
    }

    copyTranslationAndResizeVectors(that);
  }

  /**
   * Adds a point to this freehand drawing.
   *
   * @param point The point to add.
   */
  void addPoint(CoordinatePair point)
  {
    coordinatePoints.add(point);  
  }

  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    Graphics2D g2 = (Graphics2D)g;

    // In this list we store the point between which we will draw lines.
    List<CoordinatePair> coordinatePointsToDraw = new ArrayList<>();

    // Start with adding all "permanent" points.
    coordinatePointsToDraw.addAll(coordinatePoints);

    // Is the shape moving?
    if (translationVector != null)
    {
      for (int i = 0; i < coordinatePointsToDraw.size(); i++)
      {
        CoordinatePair translatedPoint = coordinatePointsToDraw.get(i).add(translationVector);
        coordinatePointsToDraw.set(i, translatedPoint);
      }
    }
    else if (coordinatePointsWhenResizing.size() > 0)
    {
      // Is the shape moving?

      // Adding in the beginning or the end?
      if (selectedResizePoint == FreehandResizeArea.BEFORE)
      {
        coordinatePointsToDraw.addAll(0, coordinatePointsWhenResizing);
      }
      else if (selectedResizePoint == FreehandResizeArea.AFTER)
      {
        coordinatePointsToDraw.addAll(coordinatePointsWhenResizing);
      }
    }

    //----------------------------------------------------------
    // Draw the shape.
    //----------------------------------------------------------
    drawLines(g2, coordinatePointsToDraw);

    //----------------------------------------------------------
    // Is this shape selected? In that case we draw two small rectangles
    // at the first end of the first line and the last end of the last line.
    //----------------------------------------------------------
    if (isSelected())
    {
      int rectWidthAndHight = getWidthOfMarkerSquare();

      CoordinatePair startPointOfFirstLine = coordinatePointsToDraw.get(0);
      CoordinatePair endPointOfLastLine = coordinatePointsToDraw.get(coordinatePointsToDraw.size() - 1);

      g2.fillRect(startPointOfFirstLine.x - rectWidthAndHight / 2,
        startPointOfFirstLine.y - rectWidthAndHight / 2,
        rectWidthAndHight, rectWidthAndHight);

      g2.fillRect(endPointOfLastLine.x - rectWidthAndHight / 2,
        endPointOfLastLine.y - rectWidthAndHight / 2,
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
    for (int i = 0; i < coordinatePoints.size() - 1; i++)
    {
      CoordinatePair startPoint = coordinatePoints.get(i);
      CoordinatePair endPoint = coordinatePoints.get(i + 1);
      if (isPointOnLine(startPoint, endPoint, point)) 
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Should return true if the specified point is located in a resize area of the shape.
   *
   * @param point The point to check.
   * @return true if point is located in one of the resize areas.
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
    selectedResizePoint = getResizeAreaForPoint(point);
    resizeStartPoint = point;
  }

  /**
   * Should set the resize vector to use when drawing this shape.
   *
   * @param resizeVector The translation vector.
   */
  @Override
  public void setResizeVector(CoordinatePair resizeVector)
  {
    super.setResizeVector(resizeVector);

    // Check if we're passed the same point as the previous time this method was invoked.
    // If so, just return. This is to avoid adding uneccessary lines.
    if (latestResizePosition != null && latestResizePosition.equals(resizeVector))
    {
      return;
    }

    latestResizePosition = resizeVector;

    if (selectedResizePoint == FreehandResizeArea.BEFORE)
    {
      // Add first to make the make the latest point the first one in the shape.
      coordinatePointsWhenResizing.add(0, resizeStartPoint.add(resizeVector));
    }
    else if (selectedResizePoint == FreehandResizeArea.AFTER)
    {
      // Add last to make the latest point the last one in the shape.
      coordinatePointsWhenResizing.add(resizeStartPoint.add(resizeVector));
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
      List<CoordinatePair> translatedCoordinatePoints = new ArrayList<>();
      for (CoordinatePair point : coordinatePoints)
      {
        translatedCoordinatePoints.add(point.add(translationVector));
      }
      
      coordinatePoints = translatedCoordinatePoints;
    }

    super.incorporateTranslationVector();
  }

  /**
   * Incorporates the point added while "resizing".
   */
  @Override
  public void incorporateResizeVector()
  {
    // Add the points that have been created since while resizing.
    if (selectedResizePoint == FreehandResizeArea.BEFORE)
    {
      // Add first to make the make the latest point the first one in the shape.
      coordinatePoints.addAll(0, coordinatePointsWhenResizing);
    }
    else if (selectedResizePoint == FreehandResizeArea.AFTER)
    {
      // Add last to make the latest point the last one in the shape.
      coordinatePoints.addAll(coordinatePointsWhenResizing);
    }

    // Clear the fields we use while resizing.
    super.incorporateResizeVector();
    resizeStartPoint = null;
    selectedResizePoint = null;
    resizeVector = null;
    coordinatePointsWhenResizing.clear();
  }

  /**
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  @Override
  public DrawableShape createClone()
  {
    return new FreehandShape(this);
  }

  /**
   * Returns True if the specified point is sufficiently near the specified line.
   *
   * @param startPoint The start point of the line.
   * @param endPoint The end point of the line.
   * @param pointToCheck The point to check if it is on the line or not.
   * @return true if point is on the line false if
   */
  private boolean isPointOnLine(CoordinatePair startPoint, CoordinatePair endPoint,
                                CoordinatePair pointToCheck)
  {
    return Utilities.distanceBetweenLineAndPoint(startPoint, endPoint, pointToCheck) <= strokeWidth;
  }

  /**
   * Draws lines between the point in the specified list.
   *
   * @param g2 The Graphics to draw on.
   * @param coordinatePointsToDraw The list containing the points to draw lines between.
   */
  private void drawLines(Graphics g2, List<CoordinatePair> coordinatePointsToDraw)
  {
    for (int i = 0; i < coordinatePointsToDraw.size() - 1; i++)
    {
      CoordinatePair startPoint = coordinatePointsToDraw.get(i);
      CoordinatePair endPoint = coordinatePointsToDraw.get(i + 1);

      g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }
  }

  /**
   * Returns the resize area which includes the specified point, or null if no resize area contains the point.
   *
   * @param point The point to check.
   * @return The resize area in which the point is located or null if no such resize area exist.
   */
  private FreehandResizeArea getResizeAreaForPoint(CoordinatePair point)
  {
    if (!isSelected())
    {
      return null;
    }

    int rectWidthAndHight = getWidthOfMarkerSquare();

    CoordinatePair startPointOfFirstLine = coordinatePoints.get(0);
    CoordinatePair endPointOfLastLine = coordinatePoints.get(coordinatePoints.size() - 1);

    int xOfFirstPoint = startPointOfFirstLine.x - rectWidthAndHight / 2;
    int yOfFirstPoint = startPointOfFirstLine.y - rectWidthAndHight / 2;
    int xOfLastPoint = endPointOfLastLine.x - rectWidthAndHight / 2;
    int yOfLastPoint = endPointOfLastLine.y - rectWidthAndHight / 2;

    if (pointInRectangle(point,xOfFirstPoint,yOfFirstPoint, rectWidthAndHight, rectWidthAndHight))
    {
      return FreehandResizeArea.BEFORE;
    }
    else if (pointInRectangle(point, xOfLastPoint, yOfLastPoint, rectWidthAndHight, rectWidthAndHight))
    {
      return FreehandResizeArea.AFTER;
    }

    return null;
  }
}
