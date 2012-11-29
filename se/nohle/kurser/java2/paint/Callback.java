package se.nohle.kurser.java2.paint;

/**
 * Callback interface for communication ShapePanel => MainFrame.
 */
interface Callback
{
  /**
   * Called when the number of shapes changes.
   */
  public void numberOfShapesHasChanged();

  /**
   * Called when the mouse pointer moves to a new position.
   *
   * @param point The coordinates of the mouse pointer.
   */
  public void mousePointerCoordinatesChanged(CoordinatePair point);

  /**
   * Should return true if fill is seleted, false if not,.
   *
   * @return true if fill is seleted, false if not,.
   */
  public boolean fillSelected();
}
