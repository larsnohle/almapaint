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

import java.awt.Graphics;
import java.io.Serializable;

/**
 * Interface implemented by all shapes in this application.
 */
public interface DrawableShape extends Serializable, Cloneable
{
  /**
   * Should draw the shape on to the specified Graphics.
   *
   * @param g The Graphics object to draw on.
   */
  public void draw(Graphics g);


  /**
   * Should return True if the specified point is included in this shape.
   *
   * @param point The point to check if it is included or not.
   * @return true if point is included in this shape, false if not.
   */
  public boolean isPointIncluded(CoordinatePair point);

  /**
   * Should return true if the specified point is located in a resize area of the shape.
   *
   * @param point The point to check.
   * @return true if point is located in a resize area.
   */
  public boolean isPointInResizeArea(CoordinatePair point);

  /**
   * Sets the resize area that the user has selected.
   */
  public void setSelectedResizeArea(CoordinatePair point);

  /**
   * Should set the resize vector to use when drawing this shape.
   *
   * @param resizeVector The translation vector.
   */
  public void setResizeVector(CoordinatePair resizeVector);

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the resize vector, which then is nulled out.
   */
  public void incorporateResizeVector();

  /**
   * Should set the translation vector to use when drawing this shape.
   *
   * @param translationVector The translation vector.
   */
  public void setTranslationVector(CoordinatePair translationVector);

  /**
   * Translates the coordinates used to draw this shape by the amount specified
   * by the translation vector, which then is nulled out.
   */
  public void incorporateTranslationVector();

  /**
   * Creates a clone of this shape.
   *
   * @return A clone of this shape.
   */
  public DrawableShape createClone();

  /**
   * Should mark this shape in some way indicated that it is "selected".
   */
  public void select();

  /**
   * Should remove any mark to indicate that this shape isn't "selected".
   */
  public void unselect();
}
