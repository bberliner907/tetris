package tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 *  PURPOSE: This class defines an individual piece on the tetris board.
 *
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 */

public class TetrisPiece {

  // Shared, fixed values
  public static final int SIZE = 4;

  // Fixed values
  private static final int SQUARE = 0;  // Piece type identifiers
  private static final int LINE = 1;
  private static final int PYRAMID = 2;
  private static final int L_LEFT = 3;
  private static final int L_RIGHT = 4;
  private static final int S_LEFT = 5;
  private static final int S_RIGHT = 6;

  // Changeable values
  private int type;  // Type of the piece
  private boolean active;  // Whether it has been dropped
  private boolean busy;  // Whether to wait before executing

  private int centerRow;  // Cell to rotate around
  private int centerCol;

  private TetrisCell[] piece;  // Array of cells in the board
  private Color color;  // Color of the piece

/*** CONSTRUCTOR(s) ***/

  /** PURPOSE: Initialize a new piece. */
  public TetrisPiece(TetrisBoard b) {
    TetrisBoard board = b;
    Random rand = new Random();

    this.type = rand.nextInt(7);  // Get a random type
    this.active = true;
    this.busy = false;

    this.centerRow = 0;  // Start in the middle of the top row
    this.centerCol = (board.getWidth() / 2) + (board.getWidth() % 2) - 1;

    this.piece = new TetrisCell[SIZE];

    int x = this.centerRow;
    int y = this.centerCol;    

    switch (this.type) {  // List the cells comprising a piece of this shape
      case SQUARE:
        this.piece[0] = board.getCell(x, y);
        this.piece[1] = board.getCell(x+1, y);
        this.piece[2] = board.getCell(x, y+1);
        this.piece[3] = board.getCell(x+1, y+1);

        this.color = new Color(255, 128, 0);  // Color.ORANGE does not work!
        break;
      case LINE:
        this.piece[0] = board.getCell(x, y-1);
        this.piece[1] = board.getCell(x, y);
        this.piece[2] = board.getCell(x, y+1);
        this.piece[3] = board.getCell(x, y+2);

        this.color = Color.RED;
        break;
      case PYRAMID:
        this.piece[0] = board.getCell(x, y-1);
        this.piece[1] = board.getCell(x, y);
        this.piece[2] = board.getCell(x+1, y);
        this.piece[3] = board.getCell(x, y+1);

        this.color = Color.CYAN;
        break;
      case L_LEFT:
        this.piece[0] = board.getCell(x, y-1);
        this.piece[1] = board.getCell(x+1, y-1);
        this.piece[2] = board.getCell(x, y);
        this.piece[3] = board.getCell(x, y+1);

        this.color = Color.MAGENTA;
        break;
      case L_RIGHT:
        this.piece[0] = board.getCell(x, y-1);
        this.piece[1] = board.getCell(x, y);
        this.piece[2] = board.getCell(x, y+1);
        this.piece[3] = board.getCell(x+1, y+1);

        this.color = Color.YELLOW;
        break;
      case S_LEFT:
        this.piece[0] = board.getCell(x, y-1);
        this.piece[1] = board.getCell(x, y);
        this.piece[2] = board.getCell(x+1, y);
        this.piece[3] = board.getCell(x+1, y+1);

        this.color = Color.BLUE;
        break;
      case S_RIGHT:
        this.piece[0] = board.getCell(x+1, y-1);
        this.piece[1] = board.getCell(x, y);
        this.piece[2] = board.getCell(x+1, y);
        this.piece[3] = board.getCell(x, y+1);

        this.color = Color.GREEN;
        break;
    }
  }

/*** CUSTOM Methods ***/

  /** PURPOSE: Rotate the piece counter-clockwise in the ancient tetris
   *  tradition.
   * 
   *  @require (1) rotation has been confirmed to be valid  */
  public void rotate(TetrisBoard b) {
    if (!this.busy) {
      this.busy = true;

      TetrisBoard board = b;
      this.setEmpty(this.piece);

      for (int cell = 0; cell < SIZE; cell++) {
        int[] rotated = this.getRotation(cell);
    
        this.piece[cell] = board.getCell(
                           this.piece[cell].getRow() + rotated[0], 
                           this.piece[cell].getCol() + rotated[1]);
      }

      this.setActive();

      this.busy = false;
    } else {
      try {
        /* This weirdness is necessary to prevent display artifacts and piece
         * fragmentation.  If there is no verification as to whether the thread
         * is busy, rectangles will be randomly left on the board at higher
         * speeds, and pieces will separate when the piece tries to move down
         * in the middle of another move.  On the other hand, if the condition
         * cycles too fast, you will get stack overflow errors and keystrokes
         * will be ignored.  Pausing for 1 ms allows for enough down time and
         * therefore cleanly fixes both of these problems. */
        Thread.sleep(1);
        if (this.canRotate(b)) {
          this.rotate(b);
        }
      } catch (InterruptedException e) {}
    }
  }

  /** PURPOSE: Move the piece left, right, or down on the board.
   * 
   *  @require (1) move has been confirmed to be valid  */
  public void move(TetrisBoard b, int col, int row) {
    if (!this.busy) {
      this.busy = true;

      TetrisBoard board = b;
      this.setEmpty(this.piece);

      for (int cell = 0; cell < SIZE; cell++) {
        this.piece[cell] = board.getCell(
                           this.piece[cell].getRow() + row, 
                           this.piece[cell].getCol() + col);
      }

      this.centerRow += row;
      this.centerCol += col;
      
      this.setActive();

      this.busy = false;
    } else {
      try {
        Thread.sleep(1);  // See note above
        if (this.canMove(b, col, row)) {
          this.move(b, col, row);
        }
      } catch (InterruptedException e) {}
    }
  }

  /** PURPOSE: Return an array of length 2 indicating the rotation.  The array
   *  indices represent what would be the change in position of the x and y
   *  values, respectively, if the cell at the given index was rotated.
   * 
   *  @require (1) cell < SIZE  */
  private int[] getRotation(int cell) {
    int[] rotated = new int[2];
              
    int row = this.piece[cell].getRow();
    int col = this.piece[cell].getCol();
    int x = 0, y = 0;

    if (this.type != SQUARE) {
      if (cell == 3 && this.type == LINE) {
        if (col != this.centerCol) {
          if (col > this.centerCol) { x = -2; y = -2; }
          else                      { x = 2;  y = 2;  }
        } else {
          if (row > this.centerRow) { x = -2; y = 2;  }
          else                      { x = 2;  y = -2; }
        }

      } else {  // This is easier to read and understand than some algorithm...
        if (col < this.centerCol) {
          if (row < this.centerRow)       { x = 2;  y = 0;  }
          else if (row == this.centerRow) { x = 1;  y = 1;  }
          else                            { x = 0;  y = 2;  }
        } else if (col == this.centerCol) {
          if (row < this.centerRow)       { x = 1;  y = -1; }
          else if (row == this.centerRow) { x = 0;  y = 0;  }
          else                            { x = -1; y = 1;  }
        } else {
          if (row < this.centerRow)       { x = 0;  y = -2; }
          else if (row == this.centerRow) { x = -1; y = -1; }
          else                            { x = -2; y = 0;  }
        }
      }
    }

    rotated[0] = x;
    rotated[1] = y;    

    return rotated;
  }

  /** PURPOSE: Return a boolean indicating whether the piece can be rotated in
   *  its current position.  */
  public boolean canRotate(TetrisBoard b) {
    TetrisBoard board = b;
    boolean valid = true;

    for (int cell = 0; cell < SIZE; cell++) {
      int[] rotated = this.getRotation(cell);

      int newRow = this.piece[cell].getRow() + rotated[0];
      int newCol = this.piece[cell].getCol() + rotated[1];
    
      if (newRow < board.getHeight() && newRow >= 0 && 
        newCol < board.getWidth() && newCol >= 0) {
        if (board.getCell(newRow, newCol).getMark() == TetrisCell.INACTIVE) {
          valid = false;
          break;
        }
      } else {
        valid = false;
        break;
      }
    }

    return valid;
  }

  /** PURPOSE: Return a boolean indicating whether the piece can be moved
   *  according to the given parameters.
   * 
   *  @require (1) Math.abs(row) == 1
   *           (2) Math.abs(col) == 1
               (3) Math.abs(row) + Math.abs(col) == 1  */
  public boolean canMove(TetrisBoard b, int col, int row) {
    TetrisBoard board = b;
    boolean valid = true;

    for (int cell = 0; cell < SIZE; cell++) {
      int nextRow = this.piece[cell].getRow() + row;
      int nextCol = this.piece[cell].getCol() + col;

      if ((row > 0 && nextRow < board.getHeight()) || 
          (col > 0 && nextCol < board.getWidth()) || 
          (col < 0 && nextCol >= 0)) {
        if (board.getCell(nextRow, nextCol).getMark() == TetrisCell.INACTIVE) {
          valid = false;
          break;
        }
      } else {
        valid = false;
        break;
      }
    }

    return valid;
  }
  
  /** PURPOSE: Set all of the cells in the provided array as empty.
   * 
   *  @require (1) c.length == SIZE  */
  public void setEmpty(TetrisCell[] c) {
    for (int num = 0; num < SIZE; num++) {
      TetrisCell cell = c[num];
      cell.setMark(TetrisCell.EMPTY);
      cell.setColor(TetrisCell.COLOR_EMPTY);
    }
  }

  /** PURPOSE: Set the piece as active on the board. */
  public void setActive() {
    this.active = true;
    for (int num = 0; num < SIZE; num++) {
      this.piece[num].setMark(TetrisCell.ACTIVE);
      this.piece[num].setColor(this.color);
    }
  }

  /** PURPOSE: Set the piece as inactive on the board. */
  public void setInactive() {
    this.active = false;
    for (int num = 0; num < SIZE; num++) {
      this.piece[num].setMark(TetrisCell.INACTIVE);
    }
  }

  /** PURPOSE: Return a boolean indicating whether the piece is inactive. */
  public boolean isInactive() {
    return !this.active;
  }
  
  /** PURPOSE: Return cells of piece. */
  public TetrisCell[] getCells() {
   return this.piece;
  }
  
  /** PURPOSE: Display the piece on the specified window.
   * 
   *  @require (1) this.piece != null  */
  public void displayOn(Graphics g) {
    for (int num = 0; num < SIZE; num++) {
      this.piece[num].displayOn(g);
    }
  }

}
