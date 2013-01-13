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

import java.io.*;
import java.util.*;

class FileHandler
{
  /**
   * Saves the list of shapes in the specifed file.
   *
   * @param fileToSaveIn The file to save in.
   * @param shapes The shapes to save.
   */
  static void save(File fileToSaveIn, List<DrawableShape> shapes)
    throws IOException
  {
    // Write to file.
    try  (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileToSaveIn)))
    {
      oos.writeObject(shapes);
    }
  }

  /**
   * Loads shapes from the specifed file.
   *
   * @param fileToLoadFrom The file to save in.
   * @return The loaded shapes.
   */
  @SuppressWarnings("unchecked")
  static List<DrawableShape>  load(File fileToLoadFrom) 
    throws IOException,ClassNotFoundException
  {
    // Read from a file.
    List<DrawableShape> shapes;
    try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoadFrom)))
    {
      shapes = (List<DrawableShape>)ois.readObject();
    }

    return shapes;
  }
}
