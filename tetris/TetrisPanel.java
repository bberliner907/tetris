package tetris;

import java.awt.*;
import javax.swing.*;

/**
 *  PURPOSE: Run the tetris game in a standalone window.
 * 
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 */

public class TetrisPanel extends JPanel {

  private TetrisGame game;

/*** CONSTRUCTOR(s) ***/

  /** PURPOSE: Create new game. */
  public TetrisPanel(int[] opts) {
    this.game = new TetrisGame(this, opts[0], opts[1], 
                               opts[2], (opts[3] == 1));
  }

/*** CUSTOM Methods ***/

  /** PURPOSE: Run program. */
  public static void main(String[] args) {
    if (args != null && args.length > 0) {
      if ((args[0] != null && args[0].equals("-?")) || args.length % 2 != 0) {
        TetrisGame.printHelp();  // Must be an even number of arguments
        return;
      }
    }
    
    String[] vals = TetrisGame.parseArgs(args);
    int[] opts = TetrisGame.getOptions(vals);
    
    JFrame frame = new JFrame("UW TETRIS");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(opts[4], opts[5]);
    frame.setPreferredSize(new Dimension(opts[4], opts[5]));
    frame.setMinimumSize(new Dimension(TetrisGame.DEFAULT_WIDTH, 
                         TetrisGame.DEFAULT_HEIGHT));
    frame.setBackground(Color.WHITE);
    frame.pack();
  
    TetrisPanel panel = new TetrisPanel(opts);
    frame.getContentPane().add(panel);
    panel.init();
    panel.start();
    frame.setVisible(true);
  }

  /** PURPOSE: Initialize. */
  public void init() {
    this.game.init();
  }
  
  /** PURPOSE: Start. */
  public void start() {
    this.game.start();
  }

  /** PURPOSE: Paint. */
  public void paint(Graphics g) {
    if (this.game != null) this.game.paint(g);
  }
  
}
