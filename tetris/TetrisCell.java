package tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 *  PURPOSE: This class defines an individual cell within the tetris board.
 * 
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 */

public class TetrisCell {

  // Shared, fixed values
  public static final int EMPTY = 0;  // Cell mark identifiers
  public static final int ACTIVE = 1;
  public static final int INACTIVE = -1;
  public static final Color COLOR_EMPTY = Color.BLACK;  // Color of empty cells

  // Changeable values
  private int row;  // Row number
  private int col;  // Column number
  private int mark;  // Status indicator
  private Rectangle rect;  // Rectangle representation
  private Color color;  // Cell color

/*** CONSTRUCTOR(s) ***/

  /** PURPOSE: Initialize a new cell. */
  public TetrisCell(int row, int col) {
    this.row = row;
    this.col = col;
    this.mark = EMPTY;  // Default is empty
  }

/*** CUSTOM Methods ***/

  /** PURPOSE: Return the row index of the cell. */
  public int getRow() {
    return this.row;
  }
  
  /** PURPOSE: Return the column index of the cell. */
  public int getCol() {
    return this.col;
  }

  /** PURPOSE: Return the status indicator of the cell. */
  public int getMark() {
    return this.mark;
  }

  /** PURPOSE: Set the status indicator of the cell. Return the previous status
   *  indicator.  */
  public int setMark(int mark) {
    int temp = this.mark;
    this.mark = mark;
    return temp;
  }

  /** PURPOSE: Display the cell on the specified window.
   * 
   *  @require (1) this.rect != null  */
  public void displayOn(Graphics g) {
    Graphics2D window = (Graphics2D)g;
    Color origColor = window.getColor();
    window.setColor(this.getColor());
    window.fill(this.rect);
    window.draw(this.rect);
    window.setColor(origColor);
  }
  
  /** PURPOSE: Return the rectangle represented by the cell.
   * 
   *  @require (1) this.rect != null  */
  public Rectangle getRectangle() {
    return this.rect;
  }

  /** PURPOSE: Set the rectangle represented by the cell. */
  public void setRectangle(Rectangle rect) {
    this.rect = rect;
  }

  /** PURPOSE: Return the color of the cell.
   * 
   *  @require (1) this.rect != null  */
  public Color getColor() {
    return this.color;
  }

  /** PURPOSE: Set the color of the cell.
   * 
   *  @require (1) this.rect != null  */
  public void setColor(Color color) {
    this.color = color;
  }

  /** PURPOSE: Return whether the cell is empty. */
  public boolean isEmpty() {
    return (this.mark == EMPTY);
  }

  /** PURPOSE: Return whether the cell is active. */
  public boolean isActive() {
    return (this.mark == ACTIVE);
  }

  /** PURPOSE: Return whether the cell is inactive. */
  public boolean isInactive() {
    return (this.mark == INACTIVE);
  }

}
