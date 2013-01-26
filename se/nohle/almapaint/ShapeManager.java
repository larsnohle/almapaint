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

import java.util.*;

/**
 * Manages the displayed shapes.
 */
class ShapeManager
{
  //----------------------------------------------------------
  // FIELDS
  //---------------------------------------------------------- 
  /** List containing the shapes to be drawn. */
  private List<DrawableShape> shapes = new ArrayList<>();

  /** Unmodifiable version of shapes. Meant to be returned to clients. */
  private List<DrawableShape> shapesToReturn = Collections.emptyList();

  private Stack<UndoQueueCommand> undoStack = new Stack<>();
  private Stack<UndoQueueCommand> redoStack = new Stack<>();


  private DragType ongoingDragOperation;
  private ShapeTupleList movedShapes = new ShapeTupleList();
  private DrawableShape shapeToDisplayWhenResizing;
  private DrawableShape originalShapeBeforeResizing;
  private Set<DrawableShape> selectedShapes = new LinkedHashSet<>();

  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // CONSTRUCTOR
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP 

  List<DrawableShape> getShapes()
  {
    return shapesToReturn;
  }


  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // PACKAGE METHODS.
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP 

  void addShape(DrawableShape shape)
  {
    addShapeDoNotAddToAnyStack(shape);
    undoStack.push(new UndoQueueCommand(OperationType.REMOVE, shape));
  }

  /**
   * Removes all current shapes then adds the specified ones.
   *
   * @param shapes The shapes that this object manager should contain after this call has executed.
   */
  void setShapes(List<DrawableShape> shapes)
  {
    removeAllShapes();
    this.shapes.addAll(shapes);
    shapesToReturn = Collections.unmodifiableList(shapes);
  }

  /**
   * Removes the selected shape, if any.
   */
  void removeSelectedShapes()
  {
    if (!selectedShapes.isEmpty())
    {
      removeShapes(selectedShapes);
      selectedShapes.clear();
    }
  }

  boolean handleDragOfExistingShape(CoordinatePair point)
  {
    ShapeAndDragTypeTuple shapeAndDragTypeTuple = findTopmostShapeThatIncludesPoint(point, true);

    if (shapeAndDragTypeTuple != null)
    {
      DrawableShape shape =  shapeAndDragTypeTuple.getShape();
      if (shapeAndDragTypeTuple.getDragType() == DragType.MOVE)
      {
        ongoingDragOperation = DragType.MOVE;
        moveOperationStarted(shape);
      }
      else if (shapeAndDragTypeTuple.getDragType() == DragType.RESIZE)
      {
        ongoingDragOperation = DragType.RESIZE;
        resizeOperationStarted(shape, point);
      }
      else
      {
       ongoingDragOperation = null;
      }

      return true;
    }

    return  false;
  }

  void dragOperationShapeHasBeenDragged(CoordinatePair translationVector)
  {
    if (ongoingDragOperation == DragType.MOVE)
    {
      for (ShapeTuple shapeTuple : movedShapes)
      {
        shapeTuple.getSecondShape().setTranslationVector(translationVector);
      }
    }
    else if (ongoingDragOperation == DragType.RESIZE)
    {
      shapeToDisplayWhenResizing.setResizeVector(translationVector);
    }
  }

  void dragOfExistingShapeCompleted()
  {
     if (ongoingDragOperation == DragType.MOVE)
     {
       moveOperationCompleted();
     }
    else if (ongoingDragOperation == DragType.RESIZE)
     {
       resizeOperationCompleted();
     }
  }

  /**
   * Starts a move operation.
   *
   * @param movedShape The shape to move.
   */
  private void moveOperationStarted(DrawableShape movedShape)
  {
    //----------------------------------------------------------
    // GUARD
    //----------------------------------------------------------
    if (!shapes.contains(movedShape))
    {
      resetMoveCache();
      throw new IllegalArgumentException("The moved shape is not managed!");
    }

    // We want the shape that is moved to be selected automatically.
    selectShape(movedShape, false);

    //----------------------------------------------------------
    //  Loop over a copy of the set containing the selected shapes.
    // We us a copy as we want to modify the original sest.
    //----------------------------------------------------------
    Set<DrawableShape> currentLySelectedShapes = getCopyOfSelectedShapesSet();
    for (DrawableShape shapeToMove : currentLySelectedShapes)
    {
      // Remove original shape. If UNDO is executed later the original objects should be unselected.
      removeShapeDoNotAddToAnyStack(shapeToMove);
      unselectSelectedShape(shapeToMove);

      // Clone the shape so we have an object we can translate.
      DrawableShape shapeToDisplayUnderMove = shapeToMove.createClone();

      // Add the clone to the managed set, the selected set and the moved set.
      addShapeDoNotAddToAnyStack(shapeToDisplayUnderMove);
      selectShape(shapeToDisplayUnderMove, false);
      movedShapes.add(new ShapeTuple(shapeToMove, shapeToDisplayUnderMove));
    }
  }

  private void moveOperationCompleted()
  {
    if (movedShapes.size() == 0)
    {
      throw new IllegalStateException("No move operation is ongoing!");
    }

    // Tell the shape to calculate its new coordinates based on the
    // delta it has moved.
    for (ShapeTuple shapeTuple : movedShapes)
    {
      shapeTuple.getSecondShape().incorporateTranslationVector();
    }

    undoStack.push(new UndoQueueCommand(OperationType.REPLACE, movedShapes.swapItemsInTuples()));
    resetMoveCache();
  }

  private void resizeOperationStarted(DrawableShape shapeToResize, CoordinatePair point)
  {
    //----------------------------------------------------------
    // GUARD
    //----------------------------------------------------------
    if (!shapes.contains(shapeToResize))
    {
      resetResizeCache();
      throw new IllegalArgumentException("The shape is not managed!");
    }

    // Clone the shape so we have an object we can translate.
    DrawableShape shapeToDisplayUnderResize = shapeToResize.createClone();

    // Tell the shape to determine which resize area that is used.
    shapeToDisplayUnderResize.setSelectedResizeArea(point);

    // Remove original shape from the managed set and the set of selected shapes.
    removeShapeDoNotAddToAnyStack(shapeToResize);
    unselectSelectedShape(shapeToResize);

    // Add the clone to the managed set, select it and remember that just it is the shape that is resized.
    addShapeDoNotAddToAnyStack(shapeToDisplayUnderResize);
    selectShape(shapeToDisplayUnderResize, false);
    shapeToDisplayWhenResizing = shapeToDisplayUnderResize;
    originalShapeBeforeResizing = shapeToResize;
  }

  private void resizeOperationCompleted()
  {
    if (shapeToDisplayWhenResizing == null)
    {
      throw new IllegalStateException("No resize operation is ongoing!");
    }

    // Tell the shape to calculate its new coordinates based on the
    // delta it has moved.
    shapeToDisplayWhenResizing.incorporateResizeVector();

    // Create UndoCommand so that we can undo the resize.
    ShapeTupleList stl = new ShapeTupleList();
    stl.add(new ShapeTuple(shapeToDisplayWhenResizing, originalShapeBeforeResizing));
    undoStack.push(new UndoQueueCommand(OperationType.REPLACE, stl));
    resetResizeCache();
  }

  /**
   * Returns the topmost shape in which the specified point is included in either a resize area or in the
   * actual shape.
   *
   * @param point The point to check.
   * @param shouldIncludeResizeAreas true if resize areas should be included in the search.
   * @return Tuple containing the topmost shape that includes point in either resize area or actual shape,
   *         or null if point is not included in any shape. The dragType of the returned tuple indicates if the
   *         point was included in a resize area or in the actual shape.
   */
  ShapeAndDragTypeTuple findTopmostShapeThatIncludesPoint(CoordinatePair point,
                                                                  boolean shouldIncludeResizeAreas)
  {
    for (DrawableShape shape : getShapesInReverseOrder())
    {
      if (shouldIncludeResizeAreas && shape.isPointInResizeArea(point))
      {
        return new ShapeAndDragTypeTuple(shape, DragType.RESIZE);
      }

      if (shape.isPointIncluded(point))
      {
        return new ShapeAndDragTypeTuple(shape, DragType.MOVE);
      }
    }

    return null;
  }

  /**
   * Selectes the specified shape.
   *
   * @param shapeToSelect The shape to select.
   * @param unselectOtherSelectedShapes true if the currently selected shapes, if any, should be unselected.
   */
  void selectShape(DrawableShape shapeToSelect, boolean unselectOtherSelectedShapes)
  {
    //----------------------------------------------------------
    // The shape to select must be managed.
    //----------------------------------------------------------
    if (!shapes.contains(shapeToSelect))
    {
      throw new IllegalArgumentException("The selected shape is not managed!");
    }

    //----------------------------------------------------------
    // Unselect the currently selected shapes, if so indicated.
    //----------------------------------------------------------
    if (unselectOtherSelectedShapes)
    {
      unselectSelectedShapes();
    }

    //----------------------------------------------------------
    // Select the shape.
    //----------------------------------------------------------
    selectedShapes.add(shapeToSelect);
    shapeToSelect.select();
  }

  /**
   * Unselectes all selcted shapes.
   *
   * @return true if a shape was unselected, false if not.
   */
  boolean unselectSelectedShapes()
  {
    //----------------------------------------------------------
    // Unselect the currently selected shape, if any.
    //----------------------------------------------------------
    if (!selectedShapes.isEmpty())
    {
      for (DrawableShape shape : selectedShapes)
      {
        shape.unselect();
      }
      selectedShapes.clear();

      return true;
    }

    return false;
  }

  /**
   * Unselects the specified selected shape.
   *
   * @param shapeToUnselect The shape to unselect.
   */
  private void unselectSelectedShape(DrawableShape shapeToUnselect)
  {
    selectedShapes.remove(shapeToUnselect);
    shapeToUnselect.unselect();
  }

  /**
   * Returs of copy of the set containg selected shapes.
   *
   * @return A copy of the selected shapes set.
   */
  private  Set<DrawableShape> getCopyOfSelectedShapesSet()
  {
    Set<DrawableShape> currentLySelectedShapes = new LinkedHashSet<>();
    currentLySelectedShapes.addAll(selectedShapes);
    return currentLySelectedShapes;
  }

  /**
   * Returns the managed shapes in reverse order.
   *
   * @return The managed shapes in reverse order.
   */
  List<DrawableShape> getShapesInReverseOrder()
  {
    List<DrawableShape> copy = new ArrayList<>(shapes);
    Collections.reverse(copy);

    return copy;
  }

  /**
   * Undos the last operation.
   */
  void undoLastOperation()
  {
    if (undoStack.isEmpty())
    {
      throw new IllegalStateException("The undo queue is empty!");
    }

    executeUndoCommandFromStack(undoStack, redoStack);
  }

  /**
   * Redos the last operation.
   */
  void redoLastOperation()
  {
    if (redoStack.isEmpty())
    {
      throw new IllegalStateException("The redo stack is empty!");
    }

    executeUndoCommandFromStack(redoStack, undoStack);
  }

  /**
   * Determines if there is at least one shape in the redo list.
   *
   * @return true if there is at least one shape in the redo list, false if not.
   */
  boolean hasAtLeastOneShapeInUndoStack()
  {
    return !undoStack.isEmpty();
  }

  /**
   * Determines if there is at least one shape in the redo list.
   *
   * @return true if there is at least one shape in the redo list, false if not.
   */
  boolean hasAtLeastOneShapeInRedoStack()
  {
    return !redoStack.isEmpty();
  }

  /**
   * Returns true if a shape is selected.
   *
   * @return true if a shape is selected.
   */
  boolean isAShapeSelected()
  {
    return !selectedShapes.isEmpty();
  }

  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
  // 
  // PRIVATE METHODS.
  // 
  //PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP 

  private void removeShapes(Set<DrawableShape> shapes)
  {
    List<DrawableShape>  shapesToRemove = new ArrayList<>(shapes);
    removeShapesDoNotAddToAnyStack(shapesToRemove);

    undoStack.push(new UndoQueueCommand(OperationType.ADD, shapesToRemove));
  }

  private void removeShapesDoNotAddToAnyStack(List<DrawableShape> shapesToRemove)
  {
    for (DrawableShape shape : shapesToRemove)
    {
      shapes.remove(shape);
    }

    shapesToReturn = Collections.unmodifiableList(shapes);
  }

  private void removeShapeDoNotAddToAnyStack(DrawableShape shape)
  {
    shapes.remove(shape);
    shapesToReturn = Collections.unmodifiableList(shapes);
  }

  private void addShapesDoNotAddToAnyStack(List<DrawableShape> shapes)
  {
      for (DrawableShape shape : shapes)
      {
        addShapeDoNotAddToAnyStack(shape);
      }
  }

  private void addShapeDoNotAddToAnyStack(DrawableShape shape)
  {
    addShapeDoNotAddToAnyStack(shape, -1);
  }

  private void addShapeDoNotAddToAnyStack(DrawableShape shape, int index)
  {
    if (index < 0 || index >= shapes.size())
    {
      shapes.add(shape);
    }
    else
    {
      shapes.add(index, shape);
    }

    shapesToReturn = Collections.unmodifiableList(shapes);
  }


  /**
   * Removes all shapes.
   */
  void removeAllShapes()
  {
    shapes.clear();
    shapesToReturn = Collections.emptyList();
    undoStack.clear();
    redoStack.clear();
  }

  private void executeUndoCommandFromStack(Stack<UndoQueueCommand> stackToExecuteCommandFrom, 
    Stack<UndoQueueCommand> stackToAddInverseTo)
  {
    if (stackToExecuteCommandFrom.isEmpty())
    {
      throw new IllegalStateException("The stack is empty!");
    }

    UndoQueueCommand undoStackCommand = stackToExecuteCommandFrom.pop();
    List<DrawableShape> shapes = undoStackCommand.getShapes();
    switch (undoStackCommand.getOperationType())
    {
      case ADD:
        addShapesDoNotAddToAnyStack(shapes);
        stackToAddInverseTo.push(new UndoQueueCommand(OperationType.REMOVE, shapes));
        break;
      case REMOVE:
        removeShapesDoNotAddToAnyStack(shapes);
        stackToAddInverseTo.push(new UndoQueueCommand(OperationType.ADD, shapes));
        break;
    case REPLACE:
      ShapeTupleList shapeTupleList= undoStackCommand.getShapeTupleList();
      for (ShapeTuple shapeTuple : shapeTupleList)
      {
        DrawableShape shapeToReplace = shapeTuple.getFirstShape();
        DrawableShape shapeToReplaceWith = shapeTuple.getSecondShape();
        int indexOfPrimaryShape = shapes.indexOf(shapeToReplace);

        removeShapeDoNotAddToAnyStack(shapeToReplace);
        addShapeDoNotAddToAnyStack(shapeToReplaceWith, indexOfPrimaryShape);

      }
      // Just revert the shape lists in order to create the inverse operation.
      stackToAddInverseTo.push(new UndoQueueCommand(OperationType.REPLACE, shapeTupleList.swapItemsInTuples()));

      break;
    default:
      System.out.println("DEFAULT"); 
    }
  }

  /**
   * Reset the move cache.
   */
  private void resetMoveCache()
  {
    movedShapes.clear();
    ongoingDragOperation = null;
  }

  /**
   * Reset the cache containing the shape displayed during a resize operation..
   */
  private void resetResizeCache()
  {
    shapeToDisplayWhenResizing = null;
    originalShapeBeforeResizing = null;
  }


  /**
   * Prints the managed shapes to std out.
   */
  private void printManagedShapes()
  {
    System.out.println("*** ShapeManager.shapes: ");
    for (DrawableShape shape : shapes)
    {
      System.out.println(shape);
    }

    System.out.println("\n***shapesToReturn: ");
    for (DrawableShape shape : shapesToReturn)
    {
      System.out.println(shape);
    }
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  private class UndoQueueCommand
  {
    private final OperationType operationType;
    private final List<DrawableShape> shapes = new ArrayList<>();
    private final ShapeTupleList shapeTupleList = new ShapeTupleList();

    private UndoQueueCommand(OperationType operationType,
                             List<DrawableShape> shapes)
    {
      this.operationType = operationType;
      this.shapes.addAll(shapes);
    }

    private UndoQueueCommand(OperationType operationType,
                             DrawableShape shape)
    {
      this.operationType = operationType;
      this.shapes.add(shape);
    }

    private UndoQueueCommand(OperationType operationType, ShapeTupleList shapeTupleList)
    {
      this.operationType = operationType;
      this.shapeTupleList.initializeFrom(shapeTupleList);
    }


    private OperationType getOperationType()
    {
      return operationType;
    }

    private List<DrawableShape> getShapes()
    {
      return new ArrayList<>(shapes);
    }

    public ShapeTupleList getShapeTupleList()
    {
      return shapeTupleList;
    }
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  // INNER CLASS
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  private static class ShapeTuple
  {
    private final DrawableShape firstShape;
    private final DrawableShape secondShape;

    private ShapeTuple(DrawableShape firstShape, DrawableShape secondShape)
    {
      this.firstShape = firstShape;
      this.secondShape = secondShape;
    }

    public DrawableShape getFirstShape()
    {
      return firstShape;
    }

    public DrawableShape getSecondShape()
    {
      return secondShape;
    }
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  // INNER CLASS
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  /**
   * List of Shape tuples.
   */
  private static class ShapeTupleList implements Iterable<ShapeTuple>
  {
    private List<DrawableShape> firstShapeList = new ArrayList<>();
    private List<DrawableShape> secondShapeList = new ArrayList<>();

    private ShapeTupleList()
    {
    }

    /**
     * Copies all elements from the specified ShapeTupleList to this ShapeTupleList.
     *
     * @param listToCopy The ShapeTupleList to initialize this object from.
     */
    private void initializeFrom(ShapeTupleList listToCopy)
    {
      firstShapeList.clear();
      secondShapeList.clear();

      for (ShapeTuple shapeTuple : listToCopy)
      {
        firstShapeList.add(shapeTuple.getFirstShape());
        secondShapeList.add(shapeTuple.getSecondShape());
      }
    }

    private void add(ShapeTuple shapeTuple)
    {
      firstShapeList.add(shapeTuple.getFirstShape());
      secondShapeList.add(shapeTuple.getSecondShape());
    }

    private int size()
    {
      return firstShapeList.size();
    }

    private void clear()
    {
      firstShapeList.clear();
      secondShapeList.clear();
    }

    /**
     * Swaps the lists containing the first items of the tuples and the list containing the second item of the tuples.
     * From the client perspective, this in effect swaps the items in the tuples.
     *
     * Performs the operations in place and return this object.
     *
     * @return this
     */
    private ShapeTupleList swapItemsInTuples()
    {
      List<DrawableShape> tmpList = firstShapeList;
      firstShapeList = secondShapeList;
      secondShapeList = tmpList;
      return this;
    }

    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Implementation of the Iterable interface.
    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<ShapeTuple> iterator()
    {
      return new ShapeTupleIterator();
    }

    private class ShapeTupleIterator implements Iterator<ShapeTuple>
    {
      private int currentElement = 0;
      /**
       * Returns {@code true} if the iteration has more elements.
       * (In other words, returns {@code true} if {@link #next} would
       * return an element rather than throwing an exception.)
       *
       * @return {@code true} if the iteration has more elements
       */
      @Override
      public boolean hasNext()
      {
        return currentElement < firstShapeList.size();
      }

      /**
       * Returns the next element in the iteration.
       *
       * @return the next element in the iteration
       * @throws java.util.NoSuchElementException
       *          if the iteration has no more elements
       */
      @Override
      public ShapeTuple next()
      {
        if (!hasNext())
        {
          throw new NoSuchElementException();
        }

        ShapeTuple shapeTuple =
          new ShapeTuple(firstShapeList.get(currentElement), secondShapeList.get(currentElement));
          currentElement++;
        return shapeTuple;
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException("remove is not supported");
      }
    }

  }

}
