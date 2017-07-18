package tetris;

import java.applet.*;
import java.awt.*;

/**
 *  PURPOSE: Run the tetris game as an applet.
 * 
 *  @author Brenn Berliner, ARCH 486, Spring 2009
 */

public class TetrisApplet extends Applet {

  private TetrisGame game;

/*** Methods for Applet ***/

  /** PURPOSE: Initialize. */
  public void init() {
    String[] vals = new String[4];
    vals[0] = getParameter("rows");
    vals[1] = getParameter("cols");
    vals[2] = getParameter("level");
    vals[3] = getParameter("grid");
    int[] opts = TetrisGame.getOptions(vals);
  
    this.game = new TetrisGame(this, opts[0], opts[1], 
                               opts[2], (opts[3] == 1));
    this.game.init();
    this.setVisible(true);
  }
  
  /** PURPOSE: Start. */
  public void start() {
    this.game.start();
  }
  
  /** PURPOSE: Update. */
  public void update(Graphics g) {
    this.game.update(g);
  }

  /** PURPOSE: Paint. */
  public void paint(Graphics g) {
    int width = this.getWidth();
    int height = this.getHeight();
    if (width < TetrisGame.DEFAULT_WIDTH) width = TetrisGame.DEFAULT_WIDTH;
    if (height < TetrisGame.DEFAULT_HEIGHT) height = TetrisGame.DEFAULT_HEIGHT;
    this.setSize(width, height);
  
    if (this.game != null) this.game.paint(g);
  }

  /** PURPOSE: Stop. */
  public void stop() {
    this.game.stop();
  }
  
}
