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
   * @param startX The x coordinate of the start point of the line
   * @param startY The y coordinate of the start point of the line
   * @param endX The x coordinate of the end point of the line
   * @param endY The y coordinate of the end point of the line
   * @param strokeWidth The width of the pen.
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
