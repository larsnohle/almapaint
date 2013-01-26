package se.nohle.almapaint;

/**
 * A tuple containing a DrawableShape and an OperationType.
 */
public class ShapeAndDragTypeTuple
{
  private final DrawableShape shape;
  private final DragType dragType;

  public ShapeAndDragTypeTuple(DrawableShape shape, DragType dragType)
  {
    this.shape = shape;
    this.dragType = dragType;
  }

  public DrawableShape getShape()
  {
    return shape;
  }

  public DragType getDragType()
  {
    return dragType;
  }
}
