package se.nohle.kurser.java2.paint;

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
