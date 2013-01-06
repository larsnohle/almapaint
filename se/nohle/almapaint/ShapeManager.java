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
  // CONSTANTS
  //---------------------------------------------------------- 
  private static final int CURRENTLY_NOT_IN_USE = -1;

  //----------------------------------------------------------
  // FIELDS
  //---------------------------------------------------------- 
  /** List containing the shapes to be drawn. */
  private List<DrawableShape> shapes = new ArrayList<>();

  /** Unmodifiable version of shapes. Meant to be returned to clients. */
  private List<DrawableShape> shapesToReturn = Collections.emptyList();

  private Stack<UndoQueueCommand> undoStack = new Stack<>();
  private Stack<UndoQueueCommand> redoStack = new Stack<>();


  private DrawableShape movedShape;
  private DrawableShape shapeDisplayedUnderMove;
  private int indexOfMovedShape = CURRENTLY_NOT_IN_USE;

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

  /**
   * Starts a move operation.
   *
   * @param movedShape The shape to move.
   * @param shapeDisplayedUnderMove The shape that should be displayed during the move.
   * @param unselectOtherSelectedShapes true if other selected shapes, if any, should be unselected.
   */
  void moveOperationStarted(DrawableShape movedShape, DrawableShape shapeDisplayedUnderMove,
                            boolean unselectOtherSelectedShapes)
  {
    indexOfMovedShape = shapes.indexOf(movedShape);
    if (indexOfMovedShape < 0)
    {
      resetMoveCache();
      throw new IllegalArgumentException("The moved shape is not managed!");
    }

    this.movedShape = movedShape;
    this.shapeDisplayedUnderMove = shapeDisplayedUnderMove;
    removeShapeDoNotAddToAnyStack(movedShape);
    addShapeDoNotAddToAnyStack(shapeDisplayedUnderMove);

    // We want the shape that is moved to be selected automatically.
    selectShape(shapeDisplayedUnderMove, unselectOtherSelectedShapes);
  }

  void moveOperationShapeHasMoved(CoordinatePair translationVector)
  {
    shapeDisplayedUnderMove.setTranslationVector(translationVector);
  }

  void moveOperationCompleted()
  {
    if (indexOfMovedShape < 0)
    {
      resetMoveCache();
      throw new IllegalStateException("No move operation is ongoing!");
    }
    
    // Tell the shape to calculate its new coordinates based on the 
    // delta it has moved.
    shapeDisplayedUnderMove.incorporateTranslationVector();    

    undoStack.push(new UndoQueueCommand(OperationType.REPLACE, 
      shapeDisplayedUnderMove, movedShape, indexOfMovedShape));
    resetMoveCache();    
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
   * Unselectes the selcted shape.
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

  List<DrawableShape> getShapesInReverseOrder()
  {
    List<DrawableShape> copy = new ArrayList<>(shapes);
    Collections.reverse(copy);

    return copy;
  }

  void undoLastOperation()
  {
    if (undoStack.isEmpty())
    {
      throw new IllegalStateException("The undo queue is empty!");
    }

    executeUndoCommandFromStack(undoStack, redoStack);
  }

  void redoLastOperation()
  {
    if (redoStack.isEmpty())
    {
      throw new IllegalStateException("The redo stack is empty!");
    }

    executeUndoCommandFromStack(redoStack, undoStack);
  }
  
  /**
   * Determines if there is at least one managed shape.
   *
   * @return true if least one shape is managed, false if not.
   */
  boolean hasAtLeastOneShape()
  {
    return !shapes.isEmpty();
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
      DrawableShape primaryShape = undoStackCommand.getPrimaryShape();
      DrawableShape secondaryShape = undoStackCommand.getSecondaryShape();
      int indexOfPrimaryShape = shapes.indexOf(primaryShape);

      removeShapeDoNotAddToAnyStack(primaryShape);
      addShapeDoNotAddToAnyStack(secondaryShape, indexOfPrimaryShape);
     stackToAddInverseTo.push(new UndoQueueCommand(OperationType.REPLACE, 
        secondaryShape, primaryShape, indexOfPrimaryShape));
      break;
    default:
      System.out.println("DEFAULT"); 
    }
  }
  
  private void resetMoveCache()
  {
    movedShape = null;
    shapeDisplayedUnderMove = null;
    indexOfMovedShape = CURRENTLY_NOT_IN_USE;
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  private class UndoQueueCommand
  {
    private OperationType operationType;
    private DrawableShape primaryShape;
    private DrawableShape secondaryShape;
    private int indexOfSecondaryShape;
    private List<DrawableShape> shapes = new ArrayList<>();

    private UndoQueueCommand(OperationType operationType,
                             List<DrawableShape> shapes)
    {
      this(operationType, null, null, CURRENTLY_NOT_IN_USE);
      this.shapes.addAll(shapes);
    }

    private UndoQueueCommand(OperationType operationType,
                             DrawableShape shape)
    {
      this(operationType, null, null, CURRENTLY_NOT_IN_USE);
      this.shapes.add(shape);
    }

    private UndoQueueCommand(OperationType operationType, 
                             DrawableShape primaryShape,
                             DrawableShape secondaryShape,
                             int indexOfSecondaryShape)
    {
      this.operationType = operationType;
      this.primaryShape = primaryShape;
      this.secondaryShape = secondaryShape;
      this.indexOfSecondaryShape = indexOfSecondaryShape;
    }


    private OperationType getOperationType()
    {
      return operationType;
    }

    private void setOperationType(OperationType operationType)
    {
      this.operationType = operationType;
    }

    private DrawableShape getPrimaryShape()
    {
      return primaryShape;
    }

    private void setPrimaryShape(DrawableShape primaryShape)
    {
      this.primaryShape = primaryShape;
    }

    private DrawableShape getSecondaryShape()
    {
      return secondaryShape;
    }

    private void setSecondaryShape(DrawableShape secondaryShape)
    {
      this.secondaryShape = secondaryShape;
    }

    private int getIndexOfSecondaryShape()
    {
      return indexOfSecondaryShape;
    }

    private List<DrawableShape> getShapes()
    {
      return new ArrayList<>(shapes);
    }

  }
}
