import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Superclass for lookup panes. A lookup pane provides input and output spaces in the form
 * of text fields separated by a split pane. The input space, on the left, is editable. The
 * output space on the right is not editable and displays output based on processing input
 * from the input field.
 * 
 * <p>This superclass provides implementations for any functions, including management of
 * the input and output text fields, and responses to any changes that occur to any component.
 * The subclass must provide the exact text component type to use, as well as what its
 * properties are. Subclasses must provide an instance of a text field, with options customized
 * to the subclass, that is accessible through the <code>getTextComponent</code>. Subclasses
 * are not responsible for any behaviour.
 * 
 * <p>This class is an instance of <code>JPanel</code>, so it can be directly placed in a frame
 * without having to retrieve a central pane through a method.
 */
public abstract class IndexLookupPane extends JPanel implements DocumentListener,
                                                                ComponentListener {

  /**
   * An identifier used to serialize instances of class <code>IndexLookupPane</code>.
   */
  private static final long serialVersionUID = 2418167439991010275L;

  /**
   * The font to be used in the input and output text fields.
   */
  protected Font textDisplayFont = new Font("Arial", Font.PLAIN, 14);

  /**
   * The editable text field that contains user input text.
   */
  private JTextComponent inputField;

  /**
   * The non-editable text field that contains the output of user input queries.
   */
  private JTextComponent outputField;

  /**
   * The scrolling pane containing the input text field.
   */
  private JScrollPane inputScroller;

  /**
   * The scrolling pane containing the output text field.
   */
  private JScrollPane outputScroller;

  /**
   * A copy of the original scroll bar model for the output scroll pane.
   * Saved to be reapplied to the output scroll bar when the scroll bars must be
   * desynchronized to operate on their own.
   */
  private BoundedRangeModel backupScrollModel;

  /**
   * Initialize a new <code>IndexLookupPane</code> and set up all components inside.
   * Both input and output text fields begin with no content.
   */
  protected IndexLookupPane() {
    setLayout(new GridLayout(1, 1));

    inputField = getTextComponent();
    outputField = getTextComponent();
    outputField.setEditable(false);
    
    inputScroller = new JScrollPane(inputField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    outputScroller = new JScrollPane(outputField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // Listeners cause this panel to be notified when scroll bar synchronization should be changed.
    inputField.getDocument().addDocumentListener(this);
    outputScroller.getVerticalScrollBar().addComponentListener(this);
    
    // Save a copy of the output scroll pane's model to restore when scroll bars are desynchronized.
    backupScrollModel = outputScroller.getVerticalScrollBar().getModel();
    
    JSplitPane wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScroller,
                                          outputScroller);
    wholePane.setResizeWeight(0.35);
    add(wholePane);
  }

  /**
   * Return a copy of the subclass-specific text field. The text field must be completely
   * set up and its options specified. This type of component will be used for input and
   * output text fields.
   * 
   * @return The subclass-specific text field
   */
  protected abstract JTextComponent getTextComponent();

  /**
   * Synchronize the scroll bars on the input and output scroll panes such that both move
   * in the same way whenever one is dragged. For this to occur, the two text fields must
   * be the same height; if this condition is not met, one of the text fields may have lines
   * cut off.
   */
  public void matchScrollBars() {
    /* Have the input and output scroll bars use the same model; using either bar will update
       the model, which will cause both bars to move. */
    BoundedRangeModel sharedScrollBar = outputScroller.getVerticalScrollBar().getModel();
    inputScroller.getVerticalScrollBar().setModel(sharedScrollBar);
  }

  /**
   * Restore the scroll bars on the input and output scroll panes to their original status,
   * making them move independently of each other.
   */
  private void unmatchScrollBars() {
    outputScroller.getVerticalScrollBar().setModel(backupScrollModel);
  } 

  @Override
  public void componentMoved(ComponentEvent evt) {
    /* This event is only triggered when the scroll bars are repositioned by a system
       other than the user. This corresponds to the text fields updating when output
       is returned, which is when scroll bars can be matched. Otherwise text fields may
       be different heights. */
    matchScrollBars();
  }

  @Override
  public void componentHidden(ComponentEvent evt) {
    // No response is necessary for this event.
  }

  @Override
  public void componentResized(ComponentEvent evt) {
    // No response is necessary for this event.
  }

  @Override
  public void componentShown(ComponentEvent evt) {
    // No response is necessary for this event.
  }

  /**
   * Adds the specified focus listener to receive focus events from this component
   * when this component gains input focus. If listener l is null, no exception is
   * thrown and no action is performed.
   */
  public void addFocusListener(FocusListener fc) {
    inputField.addFocusListener(fc);
  }
  
  @Override
  public void changedUpdate(DocumentEvent evt) {
    // No response is necessary for this event.
  }
  
  @Override
  public void insertUpdate(DocumentEvent evt) {
    /* Document update are triggered when the input text field is modified by the user.
       When this happens, the input and output text fields may become different heights,
       which makes synchronized scroll bars cut off lines of text in the shorter text field.
       Scroll bars are desynchronized on these events to prevent cut off text. */
    unmatchScrollBars();
  }
  
  @Override
  public void removeUpdate(DocumentEvent evt) {
    // Same response as for insertions. See method body above.
    insertUpdate(evt);
  }

  /**
   * Returns the contents of the input text field. Does not erase the text field.
   * 
   * @return User-input text
   */
  public String getInputText() {
    return inputField.getText();
  }

  /**
   * Display a set of input and output <code>Strings</code> from the specified array.
   * 
   * 
   * <p>The first nested array contains the input, reformatted to be line-divided with no
   * commas or other separating values. All features other than names are cleared out.
   * The second nested array contains the output, where the item at each index in the
   * second nested array corresponds to the output of the single piece of input stored
   * at the same index in the first nested array. So a piece of input and the generated
   * output are stored at the same index in their respective arrays.
   * 
   * <p>Input and output are displayed each in the appropriate text field, such that every
   * piece of input is lined up with its output on the right. When text is displayed the
   * scroll bars on the input and output panes are synchronized, so the two panes will scroll
   * together. This behaviour will cease when the input text field is modified.
   * 
   * @param results
   *        The input and output of a lookup request.
   */
  public void display(String[][] results) {
    // Input text must be replaced in case the original input was not line-separated (eg. clubs)
    String formattedInput = String.join("\n", results[0]);
    inputField.setText(formattedInput);
    String output = String.join("\n", results[1]);
    outputField.setText(output);
  }
}
