package se.nohle.kurser.java2.paint;

import java.io.*;
import java.util.*;

class FileHandler
{
  /**
   * Saves the list of shapes in the specifed file.
   *
   * @param file The file to save in.
   * @param shapes The shapes to save.
   */
  static void save(File fileToSaveIn, List<DrawableShape> shapes) 
    throws IOException
  {
    // Write to file.
    ObjectOutputStream oos = null;
    BufferedWriter buf = null; 
    try
    {
      oos = new ObjectOutputStream(new FileOutputStream(fileToSaveIn));
      oos.writeObject(shapes);
    }
    finally
    {
      if (oos != null)
      {
        oos.close();
      }
    }
  }

  /**
   * Loads shapes from the specifed file.
   *
   * @param file The file to save in.
   * @return The loaded shapes.
   */
  @SuppressWarnings("unchecked")
  static List<DrawableShape>  load(File fileToLoadFrom) 
    throws IOException,ClassNotFoundException
  {
    // Read from a file.
    List<DrawableShape> shapes = null;
    ObjectInputStream ois = null;
    try
    {
      ois = new ObjectInputStream(new FileInputStream(fileToLoadFrom));
      shapes = (List<DrawableShape>)ois.readObject();
    }
    finally
    {      
      if (ois != null)
      {
        ois.close();
      }
    }

    return shapes;
  }
}
