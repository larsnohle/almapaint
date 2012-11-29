package se.nohle.kurser.java2.paint;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The panel on which the shapes are drawn.
 */
class ShapePanel extends JPanel
{
  /** List containing the shapes to be drawn. */
  private List<DrawableShape> shapes = new ArrayList<>();

  /** List containing that have been removed by the undo operation. */
  private List<DrawableShape> redoList = new ArrayList<>();

  /** Shape on which the user is currently working. Not persistent. */
  private DrawableShape shapeUnderConstruction;

  private ToolType currentToolType;
  private Color currentColor;
  private int strokeWidth = 1;
  private CoordinatePair dragStartPoint;
  private boolean dragStarted;
  
  private FreehandShape freehandShapeUnderConstruction;
  private DrawableShape shapeToMove;
  private DrawableShape shapeThatIsMoved;
  private int previousIndexOfMovedShape = -1; 
  private Callback callback;

  /**
   * Constructor
   *
   * @param callback. The callback (to MainFrame) on which to invoke methods when the user has 
   *                  performed some action that affects the container in which this panel resides.  
   */
  ShapePanel(Callback callback)
  {
    this.callback = callback;

    MouseAdapter ma = new ShapeMouseListener();
    addMouseMotionListener(ma);
    addMouseListener(ma);
  }

  //----------------------------------------------------------
  // SETTERS 
  //---------------------------------------------------------- 

  void setCurrentToolType(ToolType currentToolType)
  {
    this.currentToolType = currentToolType;
  }

  void setCurrentColor(Color currentColor)
  {
    this.currentColor = currentColor;
  }

  void setStrokeWidth(int width)
  {
    if (width < 1)
    {
      throw new IllegalArgumentException("width must be >= 1");
    }

    this.strokeWidth = width;
  }

  //----------------------------------------------------------
  // PACKAGE METHODS,
  //---------------------------------------------------------- 

  /**
   * Removes all shapes.
   */
  void removeAllShapes()
  {
    shapes.clear();
    repaint();
    
    // Tell the main frame that the number of shapes has changed.
    callback.numberOfShapesHasChanged();
  }

  /**
   * Removes the most current shape.
   *
   * @throws IllegalStateException If there is no shape contained in this panel.
   */
  void removeLastShape()
  {
    if (shapes.size() == 0)
    {
      throw new IllegalStateException();
    }
    
    redoList.add(shapes.get(shapes.size() - 1));
    shapes.remove(shapes.size() - 1);    
    repaint();

    // Tell the main frame that the number of shapes has changed.
    callback.numberOfShapesHasChanged();
  }

  /**
   * Moves the most current shape from the redo list to the list of shaped that are displayed..
   *
   * @throws IllegalStateException If there is no shape in the redo list.
   */
  void addLastRemovedShape()
  {
    if (redoList.size() == 0)
    {
      throw new IllegalStateException();
    }
    
    shapes.add(redoList.get(redoList.size() - 1));
    redoList.remove(redoList.size() - 1);    
    repaint();

    // Tell the main frame that the number of shapes has changed.
    callback.numberOfShapesHasChanged();
  }

  /**
   * Determines if this panel contains at least one shape.
   *
   * @retun true if this panel contains at least one shape, false if not.
   */
  boolean hasAtLeastOneShape()
  {
    return !shapes.isEmpty();
  }

  /**
   * Determines if there is at least one shape in the redo list.
   *
   * @retun true if there is at least one shape in the redo list, false if not.
   */
  boolean hasAtLeastOneShapeInRedoList()
  {
    return !redoList.isEmpty();
  }

  /**
   * Returns the shapes contained in this panel as an immutable list.
   *
   *@return The shapes contained in this panel as an immutable list.
   */ 
  List<DrawableShape> getShapes()
  {
    return Collections.unmodifiableList(shapes);
  }

  /**
   * Removes all current shapes then adds the specified ones.
   *
   * @param shapes The shapes that this panel should contain after this call
   *               has executed.
   */
  void setShapes(List<DrawableShape> shapes)
  {
    this.shapes.clear();
    this.shapes.addAll(shapes);
    redoList.clear();
    repaint();

    callback.numberOfShapesHasChanged();
  }

  /**
   * Returns the sum of the hash codes of the shapes.
   *
   * @return The sum of the hash codes of the shapes. 
   */
  int hashCodeOfShapes()
  {
    int sum = 0;
    for (DrawableShape shape : shapes)
    {
      sum += shape.hashCode();
    }

    return sum;
  }

//----------------------------------------------------------
// PRIVATE METHODS.
//---------------------------------------------------------- 
  
  /**
   * Adds a shape to this panel.
   *
   * @param shape The shape to add.
   */
  private void addShape(DrawableShape shape)    
  {
    shapes.add(shape);
    redoList.clear();
    repaint();
    
    // Tell the main frame that the number of shapes has changed.
    callback.numberOfShapesHasChanged();
  }

  /**
   * Sets the shape that is under construction.
   *
   * @param shapeUnderConstruction The shape that is under construction.
   */
  private void setShapeUnderConstruction(DrawableShape shapeUnderConstruction)
  {    
    this.shapeUnderConstruction = shapeUnderConstruction;
    repaint();
  }
  
  /**
   * Adds a point to the freehand shape under construction.
   *
   * @param cp The point to add.
   */
  private void addFreehandPointToShapeUnderConstruction(CoordinatePair cp)
  {
    if (freehandShapeUnderConstruction == null)
    {
      freehandShapeUnderConstruction = new FreehandShape(currentColor, 
                                                         strokeWidth);
    }
    
    freehandShapeUnderConstruction.addPoint(cp);
    setShapeUnderConstruction(freehandShapeUnderConstruction);
  }

  /**
   * Persists the freehand shape under construction.
   */
  private void persistFreehandShapeUnderConstruction()
  {
    // If we do not have any shape under construction, we just return.
    if (freehandShapeUnderConstruction == null)
    {
      return;
    }
    
    setShapeUnderConstruction(null);
    addShape(freehandShapeUnderConstruction);
    freehandShapeUnderConstruction = null;
  }

  /**
   * Determines if the user has chosen the rectangle shape.
   *
   * @return true if the user has choosen the rectangle shape, false if not.
   */
  private boolean isRectangleShapeSelected()
  {
    return currentToolType == ToolType.RECTANGLE;
  }

  /**
   * Determines if the user has chosen the freehand shape.
   *
   * @return true if the user has choosen the freehand shape, false if not.
   */
  private boolean isFreehandShapeSelected()
  {
    return currentToolType == ToolType.FREEHAND;
  }

  /**
   * Determines if the user has chosen the line shape.
   *
   * @return true if the user has choosen the line shape, false if not.
   */
  private boolean isLineShapeSelected()
  {
    return currentToolType == ToolType.LINE;
  }

  /**
   * Determines if the user has chosen the circle shape.
   *
   * @return true if the user has choosen the circle shape, false if not.
   */
  private boolean isCircleShapeSelected()
  {
    return currentToolType == ToolType.CIRCLE;
  }


  /**
   * Determines if the user has chosen the move tool.
   *
   * @return true if the user has choosen the move tool, false if not.
   */
  private boolean isMoveToolSelected()
  {
    return currentToolType == ToolType.MOVE;
  }

  /**
   * Creates a rectangle with the lower right corner at the specified point.
   * 
   * @param x The x-xoordinate of the lower right corner of the rectangle.
   * @param y The y-xoordinate of the lower right corner of the rectangle.
   */
  private void createRectangle(int x, int y)
  {
    CoordinatePair dragEndPoint = new CoordinatePair(x, y);
    setShapeUnderConstruction(null);
    addShape(new Rectangle(currentColor, dragStartPoint, dragEndPoint,
                           callback.fillSelected(), strokeWidth));
  }
  /**
   * Displays a rectangle under construction with the lower left corner at the specified point.
   *
   * @param point The lower right corner of the rectangle,
   */  
  private void createTemporaryRectangle(CoordinatePair point)
  {
    setShapeUnderConstruction(new Rectangle(currentColor, dragStartPoint, 
                                            point, callback.fillSelected(),
                                            strokeWidth));
  }

  /**
   * Creates a line with the end point at the specified point.
   * 
   * @param x The x-xoordinate of the end point.
   * @param y The y-xoordinate of the end point.
   */
  private void createLine(int x, int y)
  {
    CoordinatePair dragEndPoint = new CoordinatePair(x, y);
    setShapeUnderConstruction(null);
    addShape(new LineShape(currentColor, dragStartPoint, dragEndPoint, 
                           strokeWidth));
  }

  /**
   * Displays a line under construction with the end point at the specified point.
   *
   * @param point The end point of the line.
   */  
  private void createTemporaryLine(CoordinatePair point)
  {
    setShapeUnderConstruction(new LineShape(currentColor, dragStartPoint, 
                                            point, strokeWidth));
  }

  /**
   * Creates a circle with the radius point at the specified point.
   * 
   * @param x The x-xoordinate of the radius point.
   * @param y The y-xoordinate of the radius point.
   */
  private void createCircle(int x, int y)
  {
    CoordinatePair dragEndPoint = new CoordinatePair(x, y);
    setShapeUnderConstruction(null);
    addShape(new CircleShape(currentColor, dragStartPoint, dragEndPoint, 
                             callback.fillSelected(), strokeWidth));
  }

  /**
   * Displays a circle under construction with the radius  point at the specified point.
   *
   * @param point The radius point of the circle.
   */  
  private void createTemporaryCircle(CoordinatePair point)
  {
    setShapeUnderConstruction(new CircleShape(currentColor, dragStartPoint, 
                                              point, callback.fillSelected(),
                                              strokeWidth));
  }
  
  /**
   * If a shape was selected prior to starting the drag operation,
   * this method moves that shape by an amount indicate by the vector beteween
   * the drag start point and the point specified when calling this method.
   *
   *@point The current mouse pointer position.
   */
  private void moveTopShapeIfAny(CoordinatePair point) 
  {
    if (shapeThatIsMoved == null)
    {
      shapeToMove = findTopmostShapeThatIncludesPoint(point);    

      if (shapeToMove != null)
      {
        // Keep track of previous index in list.
        previousIndexOfMovedShape = shapes.indexOf(shapeToMove);

        // Remove from list.
        shapes.remove(shapeToMove);

        // Add to undo queue.
        // TODO: implement

        // Clone
        shapeThatIsMoved = shapeToMove.createClone();

        // Add to shapes list.
         shapes.add(shapeThatIsMoved);
      }
    }
    

    if (shapeThatIsMoved != null)
    {
      // Calculate translation vector.
      CoordinatePair translationVector = point.difference(dragStartPoint);

      shapeThatIsMoved.setTranslationVector(translationVector);
      repaint();
    }

  }

  /**
   * Returns the topmost shape in which the specified point is included.
   *
   * @param point The point to check.
   * @return The topmost shape that includes point, or null if point is not 
   *        included in any shape.
   */
  private DrawableShape findTopmostShapeThatIncludesPoint(CoordinatePair point)
  {
    for (int i = shapes.size() -1 ; i >= 0; i--)
    {
      if (shapes.get(i).isPointIncluded(point))
      {
        return shapes.get(i);
      }
    }

    return null;
  }


  /**
   * P A I N T
   *
   * Paints the shapes in this panel.
   *
   * @param g The Graphics object to draw on.
   */
  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    for (DrawableShape shape : shapes)
    {
      shape.draw(g);
    }

    if (shapeUnderConstruction != null)
    {
      shapeUnderConstruction.draw(g);
    }
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS.
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  /**
   * Mouse listener.
   */
  private class ShapeMouseListener extends MouseAdapter
  {
    @Override
    public void mouseMoved(MouseEvent e)
    {      
      callback.mousePointerCoordinatesChanged(new CoordinatePair(e.getX(), e.getY()));
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      dragStartPoint = new CoordinatePair(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      if (dragStarted)
      {
        if (isRectangleShapeSelected())
        {
          createRectangle(e.getX() - 1, e.getY() - 1);
        }
        else if (isFreehandShapeSelected())
        {
          addFreehandPointToShapeUnderConstruction(new CoordinatePair(e.getX() - 1, e.getY() - 1));
          persistFreehandShapeUnderConstruction();
        }
        else if (isLineShapeSelected())
        {
          createLine(e.getX() - 1, e.getY() - 1);
        }
        else if (isCircleShapeSelected())
        {
          createCircle(e.getX(), e.getY());
        }
        else if (shapeThatIsMoved != null) // A move has ended.
        {
          shapeThatIsMoved.incorporateTranslationVector();
        }
      }
      else
      {
        CoordinatePair dragEndPoint = new CoordinatePair(e.getX(), e.getY());
      }
      
      // Clear the cache values.
      dragStarted = false;
      dragStartPoint = null;
      freehandShapeUnderConstruction = null;
      shapeToMove = null;
      shapeThatIsMoved = null;
      previousIndexOfMovedShape = -1;
    }


    @Override
    public void mouseDragged(MouseEvent e)
    {
      dragStarted = true;
      if (isRectangleShapeSelected())
      {
        createTemporaryRectangle(new CoordinatePair(e.getX() - 1, e.getY() - 1));
      }
      else if (isFreehandShapeSelected())
      { 
        addFreehandPointToShapeUnderConstruction(new CoordinatePair(e.getX() - 1, e.getY()- 1));
      }
      else if (isLineShapeSelected())
      {
        createTemporaryLine(new CoordinatePair(e.getX() - 1, e.getY() - 1));
      }
      else if (isCircleShapeSelected())
      {
        createTemporaryCircle(new CoordinatePair(e.getX() - 1, e.getY() - 1));
      }
      else if (isMoveToolSelected())
      {
        moveTopShapeIfAny(new CoordinatePair(e.getX() - 1, e.getY() - 1));
      }      
    }
  }
}
