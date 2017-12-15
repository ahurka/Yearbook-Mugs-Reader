import java.awt.Container;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The complete graphical interface of the mugs reader program, representing the full
 * view and controller implementations for the application. Contains spaces for user input
 * and program output, as well as other functions for user convenience. All active
 * components of the program are at lower levels; see attribute class documentation for
 * more details.
 *
 * @see ControlPane, IndexResetPane
 */
public class MugsReaderFrame extends JFrame {

  /**
   * The identifier used to serialize instances of class <code>MugsReaderFrame</code>.
   */
  private static final long serialVersionUID = -1236008027482635646L;

  /**
   * The pane containing the application's banner.
   */
  private HeaderPane title;

  /**
   * The pane giving the user access to index file modifications, such as changing the
   * index file in use or altering the priority of the currently selected index file
   * for future runs of the program.
   */
  private IndexResetPane indexReset;

  /**
   * The pane through which the user makes index operations, including spaces for input
   * text, spaces for output text, and buttons to trigger input processing.
   */
  private ControlPane control;

  /**
   * Initialize a new <code>MugsReaderFrame</code>, set to display the given information
   * about the initially loaded index. The system relies on <code>Observable</code> to
   * communicate with the model(s), so any system outside the frame must subscribe to
   * receive user information requests using the <code>addObservers</code> method.
   * 
   * @param sourceIndex
   *        The name of the index in use
   * @param sourceManual
   *        <code>true</code> if the index in use is set to manual priority
   */
  public MugsReaderFrame(String sourceIndex, boolean sourceManual) {
    setTitle("Bloor CI Mugs Reader v1.0");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setResizable(false);
    
    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

    // Add and position components.
    title = new HeaderPane();
    contentPane.add(title.getContentPanel());

    indexReset = new IndexResetPane(600, sourceIndex, sourceManual);
    contentPane.add(indexReset.getContentPanel());
    contentPane.add(Box.createVerticalStrut(5));
    
    control = new ControlPane();
    contentPane.add(control.getContentPanel());
  }
  
  public void displayErrorMessage(String errorText) {
    JOptionPane.showMessageDialog(null, errorText, "File Not Found", JOptionPane.ERROR_MESSAGE);
  }
  
  public void addObservers(Observer observer) {
    control.addObserver(observer);
    indexReset.addObserver(observer);
  }
}
