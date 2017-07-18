package tetris;

import java.awt.geom.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.lang.Math;
import java.text.NumberFormat;
import java.util.Set;
import java.util.HashSet;

/**
 *  PURPOSE: This class defines the board on which the tetris game is played.
 *
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 */

public class TetrisBoard {

  // Fixed values
  private static final int margin = 20;  // Borders of the window
  private static final int innerMargin = 5;  // Borders of tables
  private static final int textMargin = 15;  // Borders around text
  private static final int sectionMargin = 25;  // Section breaks

  // Changeable values
  private TetrisCell[][] board;  // Array of cells in the board (rows, cols)
  private boolean grid;  // Whether to display a grid

/*** CONSTRUCTOR(s) ***/

  /** PURPOSE: Initialize a new board of the specified size and appearance. */
  public TetrisBoard(int rows, int cols, boolean grid) {
    this.board = new TetrisCell[rows][cols];
    this.grid = grid;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        this.board[r][c] = new TetrisCell(r, c);  // Initialize the cells
      }
    }
  }

  /** Alternate constructor #1. */
  public TetrisBoard(int rows, int cols) {
    this(rows, cols, TetrisGame.DEFAULT_GRID);
  }

  /** Alternate constructor #2. */
  public TetrisBoard() {
    this(TetrisGame.DEFAULT_ROWS, TetrisGame.DEFAULT_COLS);
  }

/*** CUSTOM Methods ***/

  /** PURPOSE: Display the board on the specified window. */
  public void displayOn(Graphics g, int width, int height) {
    displayOn(g, width, height, 0, 0, 0);
  }
  
  /** PURPOSE: Display the board on the specified window. */
  public void displayOn(Graphics g, int width, int height, 
                        int lines, int score, int level) {
    Graphics2D window = (Graphics2D)g;
    Color origColor = window.getColor();

    int rows = this.board.length;
    int cols = this.board[0].length;

    int minStretch = 135;  // Minimum width of the right side
    height -= margin;

    // Size of a square cell in relation to the window
    int cell = (width - (margin * 3) - minStretch - 2) / cols;
    if (rows >= cols) {  // Use horizontal size computation by default
      if (height - (margin * 2) - 2 < (cell * rows)) {  // Too tight
        cell = (height - (margin * 2)) / rows;  // Use vertical size
      }
    }

    /* LEFT SIDE */

    // Playing field
    window.setColor(Color.BLACK);
    Rectangle well = new Rectangle(margin, margin * 2, (cell * cols) + 2, 
                                   (cell * rows) + 2);
    window.fill(well);
    window.draw(well);

    // Cells
    window.setColor(TetrisCell.COLOR_EMPTY);
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
      this.board[r][c].setRectangle(new Rectangle(margin + (cell * c) + 1, 
                                    (margin * 2) + (cell * r) + 1, 
                                    cell, cell));
        this.board[r][c].displayOn(g);
      }
    }

    // Grid
    Color gridColor = (this.grid) ? Color.GRAY : TetrisCell.COLOR_EMPTY;
    window.setColor(gridColor);

    for (int r = 0; r <= rows; r++) {
      window.draw(new Line2D.Double(margin + 1, (margin * 2) + (cell * r) + 1, 
                                    margin + (int)well.getWidth() - 1, 
                                    (margin * 2) + (cell * r) + 1));
    }
    for (int c = 0; c <= cols; c++) {
      window.draw(new Line2D.Double(margin + (cell * c) + 1, (margin * 2) + 1, 
                                    margin + (cell * c) + 1, 
                                    (margin * 2) + (int)well.getHeight() - 1));
    }

    /* RIGHT SIDE */

    int x = (margin * 2) + (int)well.getWidth();  // Horizontal offset
    int y = (margin * 3);  // Vertical offset
    int stretch = Math.abs(width - (margin * 3) - (int)well.getWidth());

    // Headers
    window.setColor(Color.BLACK);
    window.setFont(new Font("Times", Font.BOLD, 14));
    window.drawString("UW TETRIS", x, y);
    y += sectionMargin;
    window.setFont(new Font("Times", Font.PLAIN, 12));
    window.drawString("Brenn Berliner", x, y);
    y += textMargin;
    window.drawString("ARCH 486, Spring 2009", x, y);

    // Game status
    y += sectionMargin;
    Rectangle status = new Rectangle(x, y, stretch, 
                                     (innerMargin * 2) + (textMargin * 3));
    window.draw(status);
    int tableX = (int)status.getX();
    int tableY = (int)status.getY() + 13;
    window.drawString("LINES: ", tableX + innerMargin, tableY + innerMargin);
    window.drawString("SCORE: ", tableX + innerMargin, 
                      tableY + innerMargin + textMargin);
    window.drawString("LEVEL: ", tableX + innerMargin, 
                      tableY + innerMargin + (textMargin * 2));
    window.setColor(Color.RED);
    window.drawString("" + lines, tableX + innerMargin + 55, 
                      tableY + innerMargin);
    window.setColor(Color.BLUE);
    window.drawString("" + NumberFormat.getIntegerInstance().format(score), 
                      tableX + innerMargin + 55, 
                      tableY + innerMargin + textMargin);
    window.setColor(Color.GREEN);
    window.drawString("" + level, tableX + innerMargin + 55, 
                      tableY + innerMargin + (textMargin * 2));

    // Game commands
    y += sectionMargin + status.getHeight();
    window.setColor(Color.BLACK);
    Rectangle commands = new Rectangle(x, y, stretch, 
                                       (innerMargin * 2) + (textMargin * 4));
    window.draw(commands);
    tableX = (int)commands.getX();
    tableY = (int)commands.getY() + 13;
    window.drawString("1:  Play Game", tableX + innerMargin, 
                      tableY + innerMargin);
    window.drawString("2:  Watch Game", tableX + innerMargin, 
                      tableY + innerMargin + textMargin);
    window.draw(new Line2D.Double(tableX, 
                tableY - 13 + (innerMargin * 3) + (textMargin * 2), 
                tableX + stretch, 
                tableY - 13 + (innerMargin * 3) + (textMargin * 2)));
    window.drawString("3:  Pause", tableX + innerMargin, 
                      tableY + innerMargin + (textMargin * 3));

    // Game controls
    y += sectionMargin + commands.getHeight();
    Rectangle controls = new Rectangle(x, y, stretch, 
                                       (innerMargin * 2) + (textMargin * 6));
    window.draw(controls);
    tableX = (int)controls.getX();
    tableY = (int)controls.getY() + 13;
    window.drawString('\u25c4' + "  or  G:  Left", tableX + innerMargin, 
                      tableY + innerMargin);
    window.drawString('\u25ba' + "  or  J:  Right", tableX + innerMargin, 
                      tableY + innerMargin + textMargin);
    window.drawString('\u25b2' + "  or  H:  Rotate", tableX + innerMargin, 
                      tableY + innerMargin + (textMargin * 2));
    window.drawString("B:  Down", tableX + innerMargin, 
                      tableY + innerMargin + (textMargin * 3));
    window.draw(new Line2D.Double(tableX, 
                tableY - 13 + (innerMargin * 3) + (textMargin * 4), 
                tableX + stretch, 
                tableY - 13 + (innerMargin * 3) + (textMargin * 4)));
    window.drawString('\u25bc' + "  or  Space:  Drop", tableX + innerMargin, 
                      tableY + innerMargin + (textMargin * 5));

    // Dimensions
    y += sectionMargin + controls.getHeight();
    Rectangle dimensions = new Rectangle(x, y, stretch, 
                                         (innerMargin * 2) + textMargin);
    window.draw(dimensions);
    tableX = (int)dimensions.getX();
    tableY = (int)dimensions.getY() + 13;
    window.drawString(rows + " Rows x " + cols + " Cols", 
                      tableX + innerMargin, tableY + innerMargin);
    
    // Game over
    if (this.isFull()) {
      y += sectionMargin + dimensions.getHeight();
      Rectangle over = new Rectangle(x, y, stretch, 
                                     (innerMargin * 2) + textMargin);
      window.draw(over);
      tableX = (int)over.getX();
      tableY = (int)over.getY() + 13;
      window.setColor(Color.RED);
      window.setFont(new Font("Times", Font.BOLD, 14));
      window.drawString("GAME OVER!", tableX + innerMargin, 
                        tableY + innerMargin);
    }
    
    window.setColor(origColor);
  }
  
  /** PURPOSE: Return the cell at the specified location. */
  public TetrisCell getCell(int row, int col) {
    return this.board[row][col];
  }

  /** PURPOSE: Set the status indicator of the given cell.
   *
   *  @require (1) the change has been confirmed to be valid  */
  public void setCell(int row, int col, int mark) {
    this.board[row][col].setMark(mark);
  }

  /** PURPOSE: Return a set of the cells meeting the specified criteria. */
  private Set getCellsByMark(int mark) {
    int rows = this.board.length;
    int cols = this.board[0].length;
    HashSet h = new HashSet();

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (this.board[r][c].getMark() == mark) {
          h.add(this.board[r][c]);
        }
      }
    }

    return h;
  }

  /** PURPOSE: Return the height of the board, in rows. */
  public int getHeight() {
    return this.board.length;
  }

  /** PURPOSE: Return the width of the board, in columns. */
  public int getWidth() {
    return this.board[0].length;
  }

  /** PURPOSE: Return an array of the completed lines existing on the board. */
  public int[] getLines() {
    int rows = this.board.length;
    int cols = this.board[0].length;

    int found = 0;
    int[] lines = new int[TetrisPiece.SIZE];

    for (int r = rows - 1; r >= 0; r--) {
      boolean empty = false;

      for (int c = 0; c < cols; c++) {
        if (this.board[r][c].getMark() != TetrisCell.INACTIVE) {
          empty = true;
          break;
        }
      }

      if (!empty) {
        lines[found] = r;
        found++;
      }
    }

    return lines;
  }

  /** PURPOSE: Remove a completed line from the board and drop the remaining
   *  pieces by one row.
   * 
   *  @require (1) this.displayOn method has been called  */
  public void clearLine(int clear) {
    int rows = this.board.length;
    int cols = this.board[0].length;

    for (int r = clear; r > 0; r--) {
      for (int c = 0; c < cols; c++) {
        if (this.board[r][c].getMark() == TetrisCell.INACTIVE || 
            this.board[r-1][c].getMark() == TetrisCell.INACTIVE) {
          this.board[r][c].setMark(this.board[r-1][c].getMark());
          this.board[r][c].setColor(this.board[r-1][c].getColor());
        }
      }
    }
  }

  /** PURPOSE: Return a boolean indicating whether the well is too full to
   *  continue.  */
  public boolean isFull() {
    int left = (this.getWidth() - TetrisPiece.SIZE) / 2;
    int right = this.getWidth() - left;
    boolean full = false;

    for (int r = 0; r < 2; r++) {
      for (int c = left; c < right; c++) {
        if (this.board[r][c].getMark() == TetrisCell.INACTIVE) {
          full = true;
          break;
        }
      }
    }

    return full;
  }

  /** PURPOSE: Reset the board to the state it was in following initialization.
   * 
   *  @require (1) this.displayOn method has been called  */
  public void clear() {
    int rows = this.board.length;
    int cols = this.board[0].length;    

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        this.board[r][c].setMark(TetrisCell.EMPTY);
        this.board[r][c].setColor(TetrisCell.COLOR_EMPTY);
      }
    }
  }

}
