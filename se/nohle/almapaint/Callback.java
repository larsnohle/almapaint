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

/**
 * Callback interface for communication ShapePanel => MainFrame.
 */
interface Callback
{
  /**
   * Called when the number of shapes changes.
   */
  public void numberOfShapesHasChanged();

  /**
   * Called when the mouse pointer moves to a new position.
   *
   * @param point The coordinates of the mouse pointer.
   */
  public void mousePointerCoordinatesChanged(CoordinatePair point);

  /**
   * Should return true if fill is seleted, false if not,.
   *
   * @return true if fill is seleted, false if not,.
   */
  public boolean fillSelected();
}
