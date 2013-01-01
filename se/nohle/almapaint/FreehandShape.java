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

/**
 * Shape for freehand drawings.
 */
class FreehandShape extends AbstractDrawableShape
{
  private List<CoordinatePair> coordinatePoints = new ArrayList<>();

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


    if (translationVector != null)
    {
      for (int i = 0; i < coordinatePoints.size() - 1; i++)
      {
        CoordinatePair startPoint = coordinatePoints.get(i).add(translationVector);
        CoordinatePair endPoint = coordinatePoints.get(i + 1).add(translationVector);
        
        g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
      }
    }
    else
    {      
      for (int i = 0; i < coordinatePoints.size() - 1; i++)
      {
        CoordinatePair startPoint = coordinatePoints.get(i);
        CoordinatePair endPoint = coordinatePoints.get(i + 1);
        
        g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
      }
    }

    //----------------------------------------------------------
    // Is this shape selected? In that case we draw two small rectangles
    // at the first end of the first line and the last end of the last line.
    //----------------------------------------------------------
    if (isSelected())
    {
      int rectWidthAndHight = getWidthOfMarkerSquare();

      CoordinatePair startPointOfFirstLine = coordinatePoints.get(0);
      CoordinatePair endPointOfLastLine = coordinatePoints.get(coordinatePoints.size() - 1);
      if (translationVector != null)
      {
        startPointOfFirstLine = startPointOfFirstLine.add(translationVector);
        endPointOfLastLine = endPointOfLastLine.add(translationVector);
      }

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
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  @Override
  public DrawableShape createClone()
  {
    return new FreehandShape(this);
  }

}
