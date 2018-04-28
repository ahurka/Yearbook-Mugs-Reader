import java.awt.Font;
import java.awt.GridLayout;
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
 * <p>This superclass provides implementations for many functions, including management of
 * the input and output text fields, and responses to any changes that occur to any component.
 * The subclass must provide the exact text component type to use, as well as specify the
 * component's properties. Subclasses must provide an instance of a text field, with options
 * customized to the subclass, that is accessible through <code>getTextComponent</code>
 * 
 * <p>This class is an instance of <code>JPanel</code>, so it can be directly placed in a frame
 * without having to retrieve a central pane through a method.
 */
public abstract class IndexLookupPane extends JPanel implements DocumentListener {

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
   * 
   */
  private String queryString;

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
   * If <code>true</code>, no overridden event handler method will trigger,
   * allowing action events to be fired without provoking unwanted response.
   */
  private boolean activeSuppress = false;

  /**
   * Initialize a new <code>IndexLookupPane</code> and set up all components inside.
   * Both input and output text fields begin with no content.
   */
  protected IndexLookupPane() {
    setLayout(new GridLayout(1, 1));
    queryString = "";

    inputField = getTextComponent();
    outputField = getTextComponent();
    outputField.setEditable(false);
    
    inputScroller = new JScrollPane(inputField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    outputScroller = new JScrollPane(outputField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    // Save a copy of the output scroll pane's model to restore when scroll bars are desynchronized.
    backupScrollModel = outputScroller.getVerticalScrollBar().getModel();

    // Listeners cause this panel to be notified when scroll bar synchronization should be changed.
    inputField.getDocument().addDocumentListener(this);
    
    JSplitPane wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScroller,
                                          outputScroller);
    wholePane.setResizeWeight(0.35);
    add(wholePane);
    activeSuppress = false;
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
   * 
   * 
   * @return
   */
  protected String getQueryString() {
    return queryString;
  }
  
  /**
   * Synchronize the scroll bars on the input and output scroll panes such that both move
   * in the same way whenever one is dragged. For this to occur, the two text fields must
   * be the same height; if this condition is not met, one of the text fields may have lines
   * cut off.
   */
  public void matchScrollBars() {
    /* Have the input and output scroll bars use the same model; using either bar will update
       the model, which will cause both bars to move. */
    BoundedRangeModel sharedScrollBar = inputScroller.getVerticalScrollBar().getModel();
    outputScroller.getVerticalScrollBar().setModel(sharedScrollBar);
  }

  /**
   * Restore the scroll bars on the input and output scroll panes to their original status,
   * making them move independently of each other.
   */
  private void unmatchScrollBars() {
    // Create a clone of the input scroll bar's model, which gets inherited by
    // the output scroll bar. This means the output scroll bar will start in the
    // same location as it previously was.
    BoundedRangeModel orig = outputScroller.getVerticalScrollBar().getModel();
    backupScrollModel.setMaximum(orig.getMaximum());
    backupScrollModel.setValue(orig.getValue());

    outputScroller.getVerticalScrollBar().setModel(backupScrollModel);
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
    if (!activeSuppress) {
      /* Document update are triggered when the input text field is modified by the user.
         When this happens, the input and output text fields may become different heights,
         which makes synchronized scroll bars cut off lines of text in the shorter text field.
         Scroll bars are desynchronized on these events to prevent cut off text. */
      queryString = inputField.getText();
      unmatchScrollBars();
    }
  }
  
  @Override
  public void removeUpdate(DocumentEvent evt) {
    if (!activeSuppress) {
      // Same response as for insertions. See method body above.
      queryString = inputField.getText();
      unmatchScrollBars();
    }
  }

  /**
   * Returns the contents of the input text field. Does not erase the text field.
   * 
   * @return User-input text
   */
  public String getInputText() {
    return queryString;
  }

  /**
   * Display a set of input and output strings from the input array.
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
    String formattedInput;
    String output;
    if (results == null) {
      // Default to setting text to blank if null results are received.
      formattedInput = "";
      output = "";
    } else {
      // Input text must be replaced in case names from the original
      // input were changed.
      String[] parsedResults = getDisplayText(results);
      formattedInput = parsedResults[0];
      output = parsedResults[1];
    }
    
    // Put the input and output of the recent query on the text panes.
    displayQueryResults(formattedInput, output);
  }
  
  /**
   * Display the string parameters on the input and output text spaces,
   * and reset the text panels to the desired behaviour for looking at
   * query results. In particular, the scroll bars are scrolled to the top
   * whenever new text is displayed, and then the input and output panels'
   * scroll bars are matched such that they move together.
   * 
   * For the scroll bars to be able to move together as expected, the input
   * and output string parameters must have the same number of newlines.
   * Otherwise the end of one string may be partially cut off.
   * 
   * @param in
   *        Text to be displayed in the input text space
   * @param out
   *        Text to be displayed in the output text space
   */
  private void displayQueryResults(String in, String out) {
    // Prevent scroll bars from being unmatched upon modification of text
    // in the text spaces due to document listeners.
    activeSuppress = true;
    
    // Display the text.
    inputField.setText(in);
    outputField.setText(out);
    // Move scroll bars to the top of their respective spaces.
    inputField.setCaretPosition(0);
    outputField.setCaretPosition(0);
    
    matchScrollBars();
    
    // Allow document listeners to resume listening to any further changes.
    activeSuppress = false;
  }
  
  /**
   * Prepare the output from a query for display on the screen. This means
   * compressing the results into single strings that can be written to a text
   * component. In addition to this, subclasses may override this method to
   * add a header to the output or to customize how the results are displayed.
   * 
   * @param results
   *        Raw output from a search query
   * @return The same output prepared for direct display on the text panels
   */
  protected String[] getDisplayText(String[][] results) {
    String[] out = new String[2];
    // Convert multiple array elements into a single, newline-separated string.
    out[0] = String.join("\n", results[0]);
    out[1] = String.join("\n", results[1]);
    
    return out;
  }
}