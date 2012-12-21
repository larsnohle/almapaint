package se.nohle.kurser.java2.paint;

/**
 * Contains an assorted set of utility methods.
 */
class Utilities
{
  /**
   * Returns the distance between a line and a points. 
   *
   * @param lineStartPoint The first point of the line.
   * @param lineEndPoint The second point of the line.
   * @param pointToCheck The second point of the line.
   * @return The distance between the point and the line. 
   */
  static double distanceBetweenLineAndPoint(CoordinatePair lineStartPoint,
                                      CoordinatePair lineEndPoint, CoordinatePair pointToCheck)
  {
    return distanceBetweenLineAndPoint(lineStartPoint.x, lineStartPoint.y, lineEndPoint.x, lineEndPoint.y, pointToCheck);
  }

  /**
   * Returns the distance between a line and a points. 
   *
   * @param startX The X coordinate of first point of the line.
   * @param startY The Y coordinate of first point of the line.
   * @param endX The X coordinate of second point of the line.
   * @param endY The Y coordinate of second point of the line.
   * @param pointToCheck The second point of the line.
   * @return The distance between the point and the line. 
   */
  static double distanceBetweenLineAndPoint(int startX, int startY, int endX, int endY, CoordinatePair pointToCheck)
  {
    //----------------------------------------------------------
    // Determine the equation of the line.
    //---------------------------------------------------------- 
    // y = kx + m
    double k = (endY - startY) / (double)(endX - startX); 
    double m = endY - k * endX;

    double lineYForPointX = k * pointToCheck.x + m;
    double lineXForPointY = (pointToCheck.y - m) / k;

    //----------------------------------------------------------
    // Classify the endpoints of the line. 
    //----------------------------------------------------------
    int minX = Math.min(startX, endX);
    int minY = Math.min(startY, endY);
    int maxX = Math.max(startX, endX);
    int maxY = Math.max(startY, endY);

    boolean xPointValid = lineXForPointY <= maxX && lineXForPointY >= minX;
    boolean yPointValid = lineYForPointX <= maxY && lineYForPointX >= minY;


    double minDistanceToEndpoints = Math.min(distanceBetweenPoints(startX, startY, pointToCheck),
                                             distanceBetweenPoints(endX, endY, pointToCheck) );
    double xDistance = minDistanceToEndpoints;
    double yDistance = minDistanceToEndpoints;

    if (xPointValid)
    {
      xDistance = Math.abs(lineXForPointY - pointToCheck.x);
    }

    if (yPointValid)
    {
      yDistance = Math.abs(lineYForPointX - pointToCheck.y);
    }

    return Math.min(Math.min(minDistanceToEndpoints, xDistance), yDistance);
  }

  /**
   * Returns the distance between two points. 
   *
   * @param p1 The first point.
   * @param p2 The second point.
   * @return The distance between the points. 
   */
  static double distanceBetweenPoints(CoordinatePair p1, CoordinatePair p2)
  {
    return distanceBetweenPoints(p1.x, p1.y, p2);
  }

  /**
   * Returns the distance between two points. 
   *
   * @param pX The X ccordinate of the first point.
   * @param pY The Y ccordinate of the first point.
   * @param p2 The second point.
   * @return The distance between the points. 
   */
  static double distanceBetweenPoints(int pX, int pY, CoordinatePair p2)
  {
    double deltaX  = pX -p2.x;
    double deltaY  = pY -p2.y;
    return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
  }
}
