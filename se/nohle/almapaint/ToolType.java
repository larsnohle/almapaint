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
 * Enumerates the possible shapes of this application.
 */
enum ToolType
{
  
  RECTANGLE("RECTANGLE"), FREEHAND("FREE_HAND"), LINE("LINE"), CIRCLE("CIRCLE"), MOVE("MOVE");

  private String shapeTypeName;

  ToolType(String shapeTypeName)
  {
    this.shapeTypeName = shapeTypeName;
  }
  
  String getToolTypeName()
  {
    return shapeTypeName;
  }

  @Override
  public String toString()
  {
    return shapeTypeName;
  }
}
