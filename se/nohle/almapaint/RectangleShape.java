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
 * A rectangular shape,
 */
class RectangleShape extends AbstractDrawableShape
{
  enum RectangleResizeArea
  {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
  }

  private int topLeftX; 
  private int topLeftY; 
  private int width;
  private int height;
  private boolean fill;

  private RectangleResizeArea selectedResizeArea;

  /**
   * Constructor
   *
   * @param color The color to use.
   * @param startPoint The start point of the user operation that resulted in the creation of this rectangle..
   * @param endPoint The end point,
   */
  RectangleShape(Color color, CoordinatePair startPoint, CoordinatePair endPoint,
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
  private RectangleShape(RectangleShape that)
  {
    super(that.strokeWidth, that.color, that.selected);
    this.topLeftX = that.topLeftX;
    this.topLeftY = that.topLeftY;
    this.width = that.width;
    this.height = that.height;
    this.fill = that.fill;

    copyTranslationAndResizeVectors(that);
  }  

  @Override
  public void draw(Graphics g)
  { 
    super.draw(g);

    int topLeftXToUse = topLeftX;
    int topLeftYToUse = topLeftY;
    int widthToUse = width;
    int heightToUse = height;

    //----------------------------------------------------------
    // Should we translate the coordinate system (a.k.a. is this rectangle
    // dragged right now?)
    //---------------------------------------------------------- 
    if (translationVector != null)
    {
      topLeftXToUse += translationVector.x;
      topLeftYToUse += translationVector.y;
    }
   else if (resizeVector != null)
    {
      CoordinatesWithAndHeight cwh = calculateCoordinatedWidthAndHeightFromResizeParams();
      topLeftXToUse = cwh.getX();
      topLeftYToUse = cwh.getY();
      widthToUse = cwh.getWidth();
      heightToUse = cwh.getHeight();
    }

    //----------------------------------------------------------
    // Should the triangle be filled?
    //---------------------------------------------------------- 
    Graphics2D g2 = (Graphics2D)g;
    if (fill)
    {
      g2.fillRect(topLeftXToUse, topLeftYToUse, widthToUse, heightToUse);
    }
    else
    {
      g2.drawRect(topLeftXToUse, topLeftYToUse, widthToUse, heightToUse);
    }

    //----------------------------------------------------------
    // Is this shape selected? In that case we draw four small rectangles
    // to indicate that it is.
    //----------------------------------------------------------
    if (isSelected())
    {
      int rectWidthAndHight = getWidthOfMarkerSquare();
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2,
        topLeftYToUse - rectWidthAndHight / 2,
        rectWidthAndHight, rectWidthAndHight);
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2 + widthToUse,
        topLeftYToUse - rectWidthAndHight / 2,
        rectWidthAndHight, rectWidthAndHight);

      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2,
        topLeftYToUse - rectWidthAndHight / 2 + heightToUse,
        rectWidthAndHight, rectWidthAndHight);
      g2.fillRect(topLeftXToUse - rectWidthAndHight / 2 + widthToUse,
        topLeftYToUse - rectWidthAndHight / 2 + heightToUse,
        rectWidthAndHight, rectWidthAndHight);
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
    int leftXOfLeftSquares = topLeftX - rectWidthAndHight / 2;
    int leftXOfRightSquares = topLeftX + width - rectWidthAndHight / 2;
    int topYOfTopSquares = topLeftY - rectWidthAndHight / 2;
    int topYOfBottomSquares = topLeftY + height - rectWidthAndHight / 2;


    return pointInRectangle(point, leftXOfLeftSquares, topYOfTopSquares, rectWidthAndHight, rectWidthAndHight)||
      pointInRectangle(point, leftXOfRightSquares, topYOfTopSquares, rectWidthAndHight, rectWidthAndHight)||
      pointInRectangle(point, leftXOfLeftSquares, topYOfBottomSquares, rectWidthAndHight, rectWidthAndHight)||
      pointInRectangle(point, leftXOfRightSquares, topYOfBottomSquares, rectWidthAndHight, rectWidthAndHight);
  }

  /**
   * Sets the resize area that the user has selected.
   */
  @Override
  public void setSelectedResizeArea(CoordinatePair point)
  {
    int rectWidthAndHight = getWidthOfMarkerSquare();
    int leftXOfLeftSquares = topLeftX - rectWidthAndHight / 2;
    int leftXOfRightSquares = topLeftX + width - rectWidthAndHight / 2;
    int topYOfTopSquares = topLeftY - rectWidthAndHight / 2;
    int topYOfBottomSquares = topLeftY + height - rectWidthAndHight / 2;


    if (pointInRectangle(point, leftXOfLeftSquares, topYOfTopSquares, rectWidthAndHight, rectWidthAndHight))
    {
      selectedResizeArea = RectangleResizeArea.TOP_LEFT;
    }
    else if (pointInRectangle(point, leftXOfRightSquares, topYOfTopSquares, rectWidthAndHight, rectWidthAndHight))
    {
      selectedResizeArea = RectangleResizeArea.TOP_RIGHT;
    }
    else if (pointInRectangle(point, leftXOfLeftSquares, topYOfBottomSquares, rectWidthAndHight, rectWidthAndHight))
    {
      selectedResizeArea = RectangleResizeArea.BOTTOM_LEFT;
    }
    else if (pointInRectangle(point, leftXOfRightSquares, topYOfBottomSquares, rectWidthAndHight, rectWidthAndHight))
    {
      selectedResizeArea = RectangleResizeArea.BOTTOM_RIGHT;
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
      topLeftX += translationVector.x;
      topLeftY += translationVector.y;
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
    CoordinatesWithAndHeight coordinatesWithAndHeight = calculateCoordinatedWidthAndHeightFromResizeParams();
    topLeftX = coordinatesWithAndHeight.getX();
    topLeftY = coordinatesWithAndHeight.getY();
    width = coordinatesWithAndHeight.getWidth();
    height = coordinatesWithAndHeight.getHeight();

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
    return new RectangleShape(this);
  }

  @Override
  public String toString()
  {
    String tx = translationVector != null ? "" + translationVector.x : "";
    String ty = translationVector != null ? "" + translationVector.y : "";
    String rx = resizeVector != null ? "" + resizeVector.x : "";
    String ry = resizeVector != null ? "" + resizeVector.y : "";
    return "hashcode: "+ hashCode() + " x: " + topLeftX + " y: " + topLeftY + " width: " + width + " height: " + height +
      "translationVector.x: " + tx + " translationVector.y: " + ty + " resizex: " + rx + " resizey: " + ry;
  }

  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // PRIVATE METHODS.
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP


  /**
   * Calculates the new coordinates of the top left corner, and the new width and height of this rectangle
   * based on the resize vector.
   *
   * @return The newly calculated coordinate, width and height.
   */
  private CoordinatesWithAndHeight calculateCoordinatedWidthAndHeightFromResizeParams()
  {
    if (selectedResizeArea == RectangleResizeArea.BOTTOM_RIGHT)
    {
      return calculateCoordinatedWidthAndHeightResizingLowerRight();
    }
    else if (selectedResizeArea == RectangleResizeArea.BOTTOM_LEFT)
    {
      return calculateCoordinatedWidthAndHeightResizingLowerLeft();
    }
    else if (selectedResizeArea == RectangleResizeArea.TOP_LEFT)
    {
      return calculateCoordinatedWidthAndHeightResizingUpperLeft();
    }
    else if (selectedResizeArea == RectangleResizeArea.TOP_RIGHT)
    {
      return calculateCoordinatedWidthAndHeightResizingUpperRight();
    }

    throw new IllegalStateException("calculateCoordinatedWidthAndHeightFromResizeParams() called " +
      "when no resize area has been selected!");
  }

  private CoordinatesWithAndHeight calculateCoordinatedWidthAndHeightResizingLowerRight()
  {
    CoordinatesWithAndHeight cwh = new CoordinatesWithAndHeight();
    int originalXOfSelectedResizeArea = topLeftX + width;
    int originalYOfSelectedResizeArea = topLeftY + height;
    int newXOfSelectedResizeArea = originalXOfSelectedResizeArea + resizeVector.x;
    int newYOfSelectedResizeArea = originalYOfSelectedResizeArea + resizeVector.y;

    if (newXOfSelectedResizeArea < topLeftX)
    {
      cwh.setX(newXOfSelectedResizeArea);
    }
    else
    {
      cwh.setX(topLeftX);
    }

    if (newYOfSelectedResizeArea < topLeftY)
    {
      cwh.setY(newYOfSelectedResizeArea);
    }
    else
    {
      cwh.setY(topLeftY);
    }

    cwh.setWidth(Math.abs(newXOfSelectedResizeArea - topLeftX));
    cwh.setHeight(Math.abs(newYOfSelectedResizeArea - topLeftY));
    return cwh;
  }


  private CoordinatesWithAndHeight calculateCoordinatedWidthAndHeightResizingLowerLeft()
  {
    CoordinatesWithAndHeight cwh = new CoordinatesWithAndHeight();
    int originalXOfSelectedResizeArea = topLeftX;
    int originalYOfSelectedResizeArea = topLeftY + height;
    int newXOfSelectedResizeArea = originalXOfSelectedResizeArea + resizeVector.x;
    int newYOfSelectedResizeArea = originalYOfSelectedResizeArea + resizeVector.y;

    if (newXOfSelectedResizeArea > topLeftX + width)
    {
      cwh.setX(topLeftX + width);
    }
    else
    {
      cwh.setX(newXOfSelectedResizeArea);
    }

    if (newYOfSelectedResizeArea < topLeftY)
    {
      cwh.setY(newYOfSelectedResizeArea);
    }
    else
    {
      cwh.setY(topLeftY);
    }

    cwh.setWidth(Math.abs(newXOfSelectedResizeArea - (topLeftX + width)));
    cwh.setHeight(Math.abs(newYOfSelectedResizeArea - topLeftY));

    return cwh;
  }

  private CoordinatesWithAndHeight calculateCoordinatedWidthAndHeightResizingUpperLeft()
  {
    CoordinatesWithAndHeight cwh = new CoordinatesWithAndHeight();
    int originalXOfSelectedResizeArea = topLeftX;
    int originalYOfSelectedResizeArea = topLeftY;
    int newXOfSelectedResizeArea = originalXOfSelectedResizeArea + resizeVector.x;
    int newYOfSelectedResizeArea = originalYOfSelectedResizeArea + resizeVector.y;

    if (newXOfSelectedResizeArea > topLeftX + width)
    {
      cwh.setX(topLeftX + width);
    }
    else
    {
      cwh.setX(newXOfSelectedResizeArea);
    }

    if (newYOfSelectedResizeArea > topLeftY + height)
    {
      cwh.setY(topLeftY + height);
    }
    else
    {
      cwh.setY(newYOfSelectedResizeArea);
    }

    cwh.setWidth(Math.abs(newXOfSelectedResizeArea - (topLeftX + width)));
    cwh.setHeight(Math.abs(newYOfSelectedResizeArea - (topLeftY + height)));

    return cwh;
  }

  private CoordinatesWithAndHeight calculateCoordinatedWidthAndHeightResizingUpperRight()
  {
    CoordinatesWithAndHeight cwh = new CoordinatesWithAndHeight();
    int originalXOfSelectedResizeArea = topLeftX + width;
    int originalYOfSelectedResizeArea = topLeftY;
    int newXOfSelectedResizeArea = originalXOfSelectedResizeArea + resizeVector.x;
    int newYOfSelectedResizeArea = originalYOfSelectedResizeArea + resizeVector.y;

    if (newXOfSelectedResizeArea < topLeftX)
    {
      cwh.setX(newXOfSelectedResizeArea);
    }
    else
    {
      cwh.setX(topLeftX);
    }

    if (newYOfSelectedResizeArea > topLeftY + height)
    {
      cwh.setY(topLeftY + height);
    }
    else
    {
      cwh.setY(newYOfSelectedResizeArea);
    }

    cwh.setWidth(Math.abs(newXOfSelectedResizeArea - topLeftX));
    cwh.setHeight(Math.abs(newYOfSelectedResizeArea - (topLeftY + height)));

    return cwh;
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  // INNER CLASS
  //
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP

  /**
   * Simple struct-like class containing a coordinate pair, a width and a height.
   */
  private class CoordinatesWithAndHeight
  {
    private int x;
    private int y;
    private int width;
    private int height;

    public int getX()
    {
      return x;
    }

    public void setX(int x)
    {
      this.x = x;
    }

    public int getY()
    {
      return y;
    }

    public void setY(int y)
    {
      this.y = y;
    }

    public int getWidth()
    {
      return width;
    }

    public void setWidth(int width)
    {
      this.width = width;
    }

    public int getHeight()
    {
      return height;
    }

    public void setHeight(int height)
    {
      this.height = height;
    }
  }

}
