package tetris;

import java.awt.event.*;
import java.awt.Button;
import java.awt.Color;
import java.awt.Checkbox;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.TextField;
import java.util.Random;
import javax.swing.Timer;

/**
 *  PURPOSE: This class defines the master controls and program flow of the
 *  tetris game, as well as registering listeners for events.
 * 
 *  NOTES: See the static methods for command line syntax.  The game rules are
 *  all consistent with every other version of tetris known to man.  Pieces
 *  gradually drop towards the bottom of the well.  When a piece reaches the
 *  bottom, it becomes fixed and another piece appears at the top.  You must
 *  complete a line to remove it from the board and continue playing.  The game
 *  ends when you no longer have room to maneuver your piece.  Pieces must have
 *  space to rotate and to move down, so make sure you're not up against a wall
 *  or another piece if nothing is happening.  Every time you make it to the
 *  next level, your ability to score points doubles, compared to the beginning
 *  of the game, but your reaction time is halved.  Eventually, the game
 *  becomes much too fast for any normal person to handle, and you lose.  It's
 *  inevitable.  In order to ruin your life, however, the dimensions of the
 *  game well are completely customizable.  So you can keep playing forever.
 *  Visit http://students.washington.edu/brenn/classes/arch486/ for updates.
 * 
 *  If you can manage 40 lines and 20,000 points in the standard configuration,
 *  you know how to play the game of tetris.  Break both 100 and 100,000 and
 *  you might be qualified to challenge me in PC-to-PC combat. :-)
 * 
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 *  @version 0.95 [Revised 6/8/09]
 */

public class TetrisGame implements KeyListener, FocusListener, ActionListener {

  // Shared, fixed values
  public static final int DEFAULT_WIDTH = 500;  // Window width
  public static final int DEFAULT_HEIGHT = 575;  // Window height
  public static final int DEFAULT_ROWS = 20;  // Rows in the board
  public static final int DEFAULT_COLS = 10;  // Columns in the board
  public static final int DEFAULT_LEVEL = 0;  // Starting level
  public static final boolean DEFAULT_GRID = false;  // No visible grid
  
  public static final int MIN_ROWS = TetrisPiece.SIZE + 2;  // Limits
  public static final int MIN_COLS = TetrisPiece.SIZE;
  public static final int MAX_ROWS = 100;
  public static final int MAX_COLS = 30;
  public static final int MAX_STARTLEVEL = 50;

  // Fixed values
  private static final int LINES_BASE = 200;  // Point multiplier for lines
  private static final int SCORE_BASE = 100;  // Point dividend for moves
  private static final int LEVEL_BASE = 10;  // Lines per level
  private static final int DELAY_BASE = 1000;  // Initial timer length (ms)
  
  // Changeable values
  private TetrisBoard board;  // The board on which the game is played
  private TetrisPiece piece;  // The active piece on the board
  private TetrisAI ai;  // The AI opponent

  private int lines;  // Total lines cleared
  private int score;  // Total score obtained
  private int level;  // Playing level within the game

  private int startLevel;  // Customizable starting level
  private int moves;  // Player keystroke count on the current piece
  private int delay;  // Delay before dropping the piece one row 

  private boolean paused;  // Whether the game is paused
  private boolean busy;  // Whether the thread is busy completing an operation
  private boolean isHuman;  // Whether the AI is playing
  
  private Timer timer;  // The timer responsible for firing events
  private Container window;  // The game area container
  private Image buffer;  // Screen image for buffering
  private Graphics graphics;  // Graphics context of buffer
  
  // Game option controls
  private Button update;
  private Button reset;
  private TextField rowsField;
  private TextField colsField;
  private TextField levelField;
  private Checkbox gridField;

/*** CONSTRUCTOR(s) ***/

  /** PURPOSE: Initialize a new tetris game. */
  public TetrisGame(Container window, int rows, int cols, 
                    int level, boolean grid) {
    this.board = new TetrisBoard(rows, cols, grid);
    this.startLevel = level;
    this.ai = new TetrisAI();

    this.rowsField = new TextField(3);
    this.rowsField.setText(new Integer(rows).toString());
    this.colsField = new TextField(3);
    this.colsField.setText(new Integer(cols).toString());
    this.levelField = new TextField(3);
    this.levelField.setText(new Integer(level).toString());
    this.gridField = new Checkbox("Grid");
    this.gridField.setState(grid);
    this.gridField.setBackground(Color.WHITE);
    this.update = new Button("Update");
    this.reset = new Button("Reset");
    
    this.window = window;
    this.window.add(this.rowsField);
    Label rowsLabel = new Label("Rows");
    rowsLabel.setBackground(Color.WHITE);
    this.window.add(rowsLabel);
    this.window.add(this.colsField);
    Label colsLabel = new Label("Cols");
    colsLabel.setBackground(Color.WHITE);
    this.window.add(colsLabel);
    this.window.add(this.levelField);
    Label levelLabel = new Label("Level");
    levelLabel.setBackground(Color.WHITE);
    this.window.add(levelLabel);
    this.window.add(this.gridField);
    this.window.add(this.update);
    this.window.add(this.reset);
    
    this.window.addFocusListener(this);  // Prepare to track component focus
    this.window.addKeyListener(this);  // Prepare to capture keyboard input
    this.update.addActionListener(this);
    this.reset.addActionListener(this);
    
    this.window.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        TetrisGame.this.window.requestFocus();
        TetrisGame.this.window.requestFocusInWindow();
      }
    });
  }

  /** Alternate constructor #1. */
  public TetrisGame(Container window, int rows, int cols, int level) {
    this(window, rows, cols, level, DEFAULT_GRID);
  }

  /** Alternate constructor #2. */
  public TetrisGame(Container window, int rows, int cols) {
    this(window, rows, cols, DEFAULT_LEVEL);
  }

  /** Alternate constructor #3. */
  public TetrisGame(Container window) {
    this(window, DEFAULT_ROWS, DEFAULT_COLS);
  }

/*** Methods for Applet ***/

  /** PURPOSE: Initialize. */
  public void init() {
    int width = this.window.getWidth();
    int height = this.window.getHeight();
    if (width < DEFAULT_WIDTH) width = DEFAULT_WIDTH;
    if (height < DEFAULT_HEIGHT) height = DEFAULT_HEIGHT;
  
    this.window.setBackground(Color.WHITE);
    this.buffer = this.window.createImage(width, height);
    this.graphics = this.buffer.getGraphics();
  }
  
  /** PURPOSE: Start. */
  public void start() {
    this.window.setVisible(true);
    this.window.requestFocus();
    this.window.requestFocusInWindow();
    this.window.repaint();  // Display it in all its glory!
  }
  
  /** PURPOSE: Update. */
  public void update(Graphics g) {
    this.window.setForeground(Color.WHITE);
    this.init();
    this.board.displayOn(this.graphics, this.window.getWidth(), 
                         this.window.getHeight(), this.lines, 
                         this.score, this.level);
    g.drawImage(this.buffer, 0, 0, this.window);
  }

  /** PURPOSE: Paint. */
  public void paint(Graphics g) {
    this.update(g);
  }

  /** PURPOSE: Stop. */
  public void stop() {
    if (this.timer != null) this.timer.stop();
  }

/*** Methods for KeyListener ***/

  /** PURPOSE: Determine which key was pressed and act accordingly. */
  public void keyPressed(KeyEvent e) {
    String key = ("" + e.getKeyChar()).toUpperCase();

    if (key.equals("1"))      { this.resetGame(true);  }  // Play
    else if (key.equals("2")) { this.resetGame(false); }  // Watch
    else if (key.equals("3")) { this.togglePause();    }  // Pause

    if (this.isHuman && !this.paused) {
      if (key.equals(" ") || e.getKeyCode() == KeyEvent.VK_DOWN)
        { this.userMove(0, 0);  }  // Drop
      else if (key.equals("H") || e.getKeyCode() == KeyEvent.VK_UP)
        { this.userMove(0, -1); }  // Rotate
      else if (key.equals("G") || e.getKeyCode() == KeyEvent.VK_LEFT)
        { this.userMove(-1, 0); }  // Left
      else if (key.equals("J") || e.getKeyCode() == KeyEvent.VK_RIGHT)
        { this.userMove(1, 0);  }  // Right
      else if (key.equals("B")) { this.userMove(0, 1);   }  // Down
    }
  }

  /** Stub for compatibility. */
  public void keyReleased(KeyEvent e) {}

  /** Stub for compatibility. */
  public void keyTyped(KeyEvent e) {}
  
/*** Methods for FocusListener ***/

  /** PURPOSE: Get focus event handler. */
  public void focusGained(FocusEvent e) {
    this.togglePause();
  }
  
  /** PURPOSE: Release focus event handler. */
  public void focusLost(FocusEvent e) {
    if (!this.paused) this.togglePause();
  }

/*** Methods for ActionListener ***/

  /** PURPOSE: Move the piece one row down, if possible; otherwise initialize
   *  a new piece. */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(this.update) || 
        e.getSource().equals(this.reset)) {
    
      String[] vals = new String[4];
      if (e.getSource().equals(this.update)) {
        vals[0] = this.rowsField.getText();
        vals[1] = this.colsField.getText();
        vals[2] = this.levelField.getText();
        vals[3] = new Boolean(this.gridField.getState()).toString();
      }
      int[] opts = TetrisGame.getOptions(vals);

      this.rowsField.setText(new Integer(opts[0]).toString());
      this.colsField.setText(new Integer(opts[1]).toString());
      this.levelField.setText(new Integer(opts[2]).toString());
      this.gridField.setState((opts[3] == 1));
    
      this.startLevel = opts[2];
      this.board = new TetrisBoard(opts[0], opts[1], (opts[3] == 1));
      this.resetGame(this.isHuman, false);
      this.board.displayOn(this.graphics, this.window.getWidth(), 
                           this.window.getHeight());
      this.window.repaint();
      return;
    
    }
  
    if (this.piece == null || this.piece.isInactive()) {

      int[] clearLines = this.board.getLines();  // Recalculate lines
      int updateLines = 0;
      for (int r = 0; r < clearLines.length; r++) {
        if (clearLines[r] > 0) {
          this.board.clearLine(clearLines[r] + updateLines);
          updateLines++;
        }
      }
      this.setLines(updateLines);

      int updateScore = (this.moves > 0) ? ((this.level + 1) * 
                                            (updateLines * LINES_BASE) + 
                                            (SCORE_BASE / this.moves)) : 0;
      this.setScore(updateScore);  // Recalculate score

      int updateLevel = 0;
      if (this.lines >= (this.level - this.startLevel + 1) * LEVEL_BASE) {
        updateLevel = 1;
      }
      this.setLevel(updateLevel);  // Recalculate level

      if (!this.isHuman) {
        this.delay = 100;  // Commandeer the timer to run the AI for now...
      } else {
        if (updateLevel > 0) {
          this.delay = DELAY_BASE / (this.level + 1);  // Recalculate delay
          if (this.delay > 10) {  // Keep high level speeds from flattening out
            this.delay -= 10;
          }
        }
      }

      this.moves = 0;

      this.timer.stop();
      if (!this.board.isFull()) this.addPiece();
      else this.window.repaint();  // Game over

    } else {

      if (!this.paused && !this.busy) {
        this.move(0, 1);  // Move down by one row automatically

        if (!this.isHuman) {
          this.aiMove();
        }
      }

    }
  }
  
/*** CUSTOM Methods ***/

  /** PURPOSE: Handle command-line input.  All parameters are optional, and 
   *  they can appear in any order.  The internal array format used here is of   
   *  length 6 and includes option values in the order they appear below.  */
  public static String[] parseArgs(String[] args) {
    String[] vals = new String[6];

    for (int pos = 0; pos < args.length; pos += 2) {
      if (args.length > pos + 1) {          
        if (args[pos].equals("-r")) vals[0] = args[pos + 1]; // Rows
        else if (args[pos].equals("-c")) vals[1] = args[pos + 1]; // Cols
        else if (args[pos].equals("-l")) vals[2] = args[pos + 1]; // Level
        else if (args[pos].equals("-g")) vals[3] = args[pos + 1]; // Grid
        else if (args[pos].equals("-w")) vals[4] = args[pos + 1]; // Width
        else if (args[pos].equals("-h")) vals[5] = args[pos + 1]; // Height
      }
    }

    return vals;
  }
  
  /** PURPOSE: Display help information. */
  public static void printHelp() {
    System.out.println("\nTETRIS CONFIGURATION HELP (-?)\n\n" +
      "The command line syntax for this game is as follows.  Invalid\n" +
      "values will be replaced by the defaults, while excessively low\n" +
      "or high parameters will be adjusted.\n\n" +
      "\t[-g true | false] = Show grid (default = " + DEFAULT_GRID + ")\n" +
      "\t[-r x] = Number of rows (default = " + DEFAULT_ROWS + ")\n" +
      "\t[-c x] = Number of columns (default = " + DEFAULT_COLS + ")\n" +
      "\t[-w x] = Window width (default = " + DEFAULT_WIDTH + ")\n" +
      "\t[-h x] = Window height (default = " + DEFAULT_HEIGHT + ")\n" +
      "\t[-l x] = Starting level (default = " + DEFAULT_LEVEL + ")\n\n" +
      "All parameters are optional, and they can appear in any order.\n" +
      "For example, the below suffixes are all valid:\n\n" +
      "\t-g true -r 25 -c 15 -w 500 -h 600 -l 4\n" +
      "\t-r 60 -c 4 -h 700\n" +
      "\t-r 8 -w 628 -l 2 -g true");
  }
  
  /** PURPOSE: Validate parameters. */
  public static int[] getOptions(String[] vals) {
    int[] opts = new int[vals.length];

    opts[0] = DEFAULT_ROWS;
    opts[1] = DEFAULT_COLS;
    opts[2] = DEFAULT_LEVEL;
    opts[3] = (DEFAULT_GRID == true) ? 1 : 0;
    if (vals.length == 6) {
      opts[4] = DEFAULT_WIDTH;
      opts[5] = DEFAULT_HEIGHT;
    }
  
    for (int x = 0; x < vals.length; x++) {
      if (vals[x] != null && !vals[x].equals("")) {
        vals[x] = vals[x].trim();
        if (x != 3) { // rows, cols, level, width, height
          if (vals[x].matches("[0-9]*")) {
            opts[x] = new Integer(vals[x]).intValue();
          }
        } else { // grid
          if (vals[x].equals("false")) vals[x] = "0";
          else if (vals[x].equals("true")) vals[x] = "1";
          if (vals[x].equals("0") || vals[x].equals("1")) {
            opts[x] = new Integer(vals[x]).intValue();
          }
        }
      }
    }
  
    if (opts[0] < MIN_ROWS) opts[0] = MIN_ROWS;
    if (opts[0] > MAX_ROWS) opts[0] = MAX_ROWS;
    if (opts[1] < MIN_COLS) opts[1] = MIN_COLS;
    if (opts[1] > MAX_COLS) opts[1] = MAX_COLS;
    if (opts[2] > MAX_STARTLEVEL) opts[2] = MAX_STARTLEVEL;
    if (vals.length == 6) {
      if (opts[4] < DEFAULT_WIDTH) opts[4] = DEFAULT_WIDTH;
      if (opts[5] < DEFAULT_HEIGHT) opts[5] = DEFAULT_HEIGHT;
    }
  
    return opts;
  }
  
  /** PURPOSE: Reset the game to its initial state. */
  public void resetGame(boolean isHuman) {
    this.resetGame(isHuman, true);
  }

  /** PURPOSE: Reset the game to its initial state. */
  public void resetGame(boolean isHuman, boolean start) {
    this.board.clear();

    this.lines = 0;
    this.score = 0;
    this.level = this.startLevel;

    this.moves = 0;
    this.delay = DELAY_BASE / (this.level + 1);

    this.paused = false;
    this.busy = false;
    this.isHuman = isHuman;

    if (this.timer != null) this.timer.stop();
    if (start) this.addPiece();
    else this.piece = null;

    this.window.requestFocus();
    this.window.requestFocusInWindow();
  }
  
  /** PURPOSE: Create a new piece on the board. */
  public void addPiece() {
    this.piece = new TetrisPiece(this.board);  // Choose the piece
    this.piece.setActive();  // Display the piece
    this.window.repaint();
    
    this.timer = new Timer(this.delay, this);
    this.timer.start();
  }
  
  /** PURPOSE: Redirect a user's move. */
  public void userMove(int col, int row) {
    this.move(col, row);
  }

  /** PURPOSE: Redirect a computer opponent's move.  See further explanation 
   *  in TetrisAI class.  */
  public void aiMove() {
    Random rand = new Random();
    int move = rand.nextInt(3);  // Just get a random move for now

    if (move == 0)      { this.move(0, -1); }  // Rotate
    else if (move == 1) { this.move(-1, 0); }  // Left
    else if (move == 2) { this.move(1, 0);  }  // Right
    // No point in allowing downward moves, since they will happen anyway
  }

  /** PURPOSE: Handle the logistics of moving a piece. */
  public void move(int col, int row) {
    this.busy = true;
    this.moves++;  // Moves count whether consummated or not

    if (col == 0 && row == 0) {  // Drop
      while (this.piece.canMove(this.board, 0, 1)) {
        this.piece.move(board, 0, 1);
      }
      this.piece.setInactive();

    } else if (row < 0) {  // Rotate
      if (this.piece.canRotate(this.board)) {
        this.piece.rotate(this.board);
      }

    } else {  // Any other move
      if (this.piece.canMove(this.board, col, row)) {
        this.piece.move(this.board, col, row);
      } else {
        if (row > 0) {
          this.piece.setInactive();
        }
      }
    }

    this.busy = false;
    this.window.repaint();
  }

  /** PURPOSE: Pause or release the game. */
  public void togglePause() {
    this.paused = !this.paused;
  }
  
  /** PURPOSE: Return the board on which the game will be played. */
  public TetrisBoard getBoard() {
    return this.board;
  }
  
  /** PURPOSE: Get the line count. */
  public int getLines() {
    return this.lines;
  }
  
  /** PURPOSE: Get the score count. */
  public int getScore() {
    return this.score;
  }
  
  /** PURPOSE: Get the level count. */
  public int getLevel() {
    return this.level;
  }

  /** PURPOSE: Update the line counter. */
  private void setLines(int add) {
    this.lines += add;
  }

  /** PURPOSE: Update the score counter. */
  private void setScore(int add) {
    this.score += add;
  }

  /** PURPOSE: Update the level counter. */
  private void setLevel(int add) {
    this.level += add;
  }

}
