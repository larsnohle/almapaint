package se.nohle.kurser.java2.paint;

/**
 * Enumerates the possible shapes of this application.
 */
enum ShapeType
{
  
  RECTANGLE("Rektangel"), FREEHAND("Frihand"), LINE("Linje"), CIRCLE("Cirkel");

  private String shapeTypeName;

  ShapeType(String shapeTypeName)
  {
    this.shapeTypeName = shapeTypeName;
  }
  
  String getShapeTypeName()
  {
    return shapeTypeName;
  }

  @Override
  public String toString()
  {
    return shapeTypeName;
  }
}
