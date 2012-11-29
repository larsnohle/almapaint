package se.nohle.kurser.java2.paint;

/**
 * Enumerates the possible shapes of this application.
 */
enum ToolType
{
  
  RECTANGLE("Rectangle"), FREEHAND("Freehand"), LINE("Line"), CIRCLE("Circle"), MOVE("Move");

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
