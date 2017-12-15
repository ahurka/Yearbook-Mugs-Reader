import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A panel containing the main user-interaction portion of the system, corresponding
 * to the controller and view of the system. Also constitutes the visible part of the
 * user interface that deals with name lookup operation input and output.
 * 
 * <p>This pane has three main components, each of which are mostly independent.
 * Two components are text input and output panels, and the other contains buttons.
 * One of the text panels is a single row designed for looking up information about
 * a single person. The multiple row text panel is for lookup on any number of names,
 * which can be flexibly formatted. The buttons trigger lookup operations on whichever
 * of the text panels was last highlighted.
 * 
 * <p>After being retrieved, information is displayed in output panels, next to the
 * corresponding input text panels. So any single name lookup requests made from the
 * single row text panel would have their output displayed on the adjacent single
 * row output text panel. After the output is displayed in the appropriate lookup pane's
 * output field, the input field remains selected. Further requests for different
 * information can be made on the same set of input without having to select the
 * input text field again. Doing so will replace the previous request's output with
 * that of the new request.
 * 
 * <p>This class is not an actual implementation of <code>JPanel</code>, and so any
 * class wanting to use a panel of this type must retrieve the working panel using the
 * <code>getContentPanel</code> method. After that the panel will continue to respond
 * to user input and update its output text fields without needing to be prompted from
 * outside.
 */
public class ControlPane extends Observable implements ActionListener, FocusListener, 
                                                       MugsEventStamps {

  /**
   * The panel containing all elements of the controller pane.
   * Contains text fields for single and multiple name lookup, and a panel of buttons
   * that trigger lookup requests.
   */
  private JPanel wholePane;

  /**
   * The panel responsible for collecting and displaying the results of single name
   * lookup requests. Once results arrive the input text field will not be changed and
   * will remain selected; clicking another lookup button will cause the output field
   * to be updated again with the requested information.
   * 
   * <p>To look up information using the single lookup pane, it must have been selected
   * (clicked on) more recently than the multiple lookup name.
   */
  private IndexLookupPane singleLookup;

  /**
   * The panel responsible for collecting and displaying the results of multiple name
   * lookup requests. Inputs can be formatted in a variety of formats, including direct
   * copies of mugs page names, sports/clubs lists, or manually input newline or comma
   * separated lists. Once results arrive the input text field will not be changed and
   * will remain selected; clicking another lookup button will cause the output field
   * to be updated again with the requested information.
   * 
   * <p>To look up information using the multiple lookup pane, it must have been selected
   * (clicked on) more recently than the single lookup name.
   */
  private IndexLookupPane massLookup;

  /**
   * The panel containing all buttons related to information queries. Pressing a button
   * in this panel will cause information to be fetched based on which button was pressed
   * and which lookup pane was most recently selected. The same lookup pane will then
   * display the output.
   * 
   * <p>Contains buttons to search for the position in the index file, the grade, or the
   * homeform of all names in the selected lookup pane.
   */
  private JPanel buttonPane;

  /**
   * The button set to trigger operations to look up the index position of a set of people.
   * This option is useful for determining which names are found at an index, and which are
   * not; the ones that are not are assumed to be misspelled.
   */
  private JButton spellcheck = new JButton("Spellcheck");

  /**
   * The button set to trigger operations to look up the grade of a set of people.
   */
  private JButton grade = new JButton("Search Grade");

  /**
   * The button set to trigger operations to look up the homeform room of a set of people.
   */
  private JButton homeform = new JButton("Search Homeform");

  /**
   * The lookup pane that was most recently selected; the lookup pane to draw names from
   * and output results when a lookup-related button is pressed.
   */
  private IndexLookupPane selected = null;

  /**
   * Initialize a new <code>ControlPane</code> by setting up all components inside.
   * The panel will not draw itself or provide a space in which to draw itself. Its main
   * content pane, which can be retrieved using the method <code>getContentPane</code>,
   * must be added to a frame for it to be rendered.
   */
  public ControlPane() {
    wholePane = new JPanel();
    wholePane.setLayout(new BoxLayout(wholePane, BoxLayout.PAGE_AXIS));
    singleLookup = new SingleLookupPane();
    massLookup = new MassLookupPane();
    
    wholePane.add(massLookup);
    wholePane.add(Box.createVerticalStrut(5));
    wholePane.add(singleLookup);
    wholePane.add(Box.createVerticalStrut(5));

    buttonPane = new JPanel();
    buttonPane.setLayout(new GridLayout(1, 0));
    buttonPane.add(spellcheck);
    buttonPane.add(grade);
    buttonPane.add(homeform);
    
    wholePane.add(buttonPane);
    
    spellcheck.addActionListener(this);
    grade.addActionListener(this);
    homeform.addActionListener(this);
    
    // Focus listeners are used to determine which lookup pane was last selected.
    singleLookup.addFocusListener(this);
    massLookup.addFocusListener(this);
  }

  /**
   * Returns the main panel, containing single and multiple lookup panes as well as the
   * button pane. Once retrieved and added to a frame, the pane will continue to behave
   * as expected, responding to user input and displaying output.
   * 
   * @return The main panel of this <code>ControlPane</code>
   */
  public JPanel getContentPanel() {
    return wholePane;
  }

  /**
   * Display the results of a lookup request in the output text field of the lookup pane
   * from which the input was drawn.
   * 
   * @param results
   *        The results of the last lookup request made
   */
  public void displayLines(String[][] results) {
    selected.display(results);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    JButton buttonPressed = (JButton)event.getSource();
    String selectedText = selected.getInputText();

    if (selectedText.length() > 0) {
      int queryStamp = -1;
      // Interpret the desired information by the button pressed
      if (buttonPressed == spellcheck) {
        queryStamp = spellcheckStamp;
      } else if (buttonPressed == grade) {
        queryStamp = gradeLookupStamp;
      } else if (buttonPressed == homeform) {
        queryStamp = homeformLookupStamp;
      }

      /* Send a request to the system to access the requested information.
         Once the request is processed, the results will be sent back from outside. */
      if (queryStamp != -1) {
        RequestEvent query = new RequestEvent(queryStamp, selectedText);
        setChanged();
        // Send the query using Observable.
        notifyObservers(query);
      }
    }
  }

  @Override
  public void focusGained(FocusEvent arg) {
    /* Record a selected lookup pane. The text field itself causes the focus event, while the
       desired lookup pane is four component levels above the text field. */
    selected = (IndexLookupPane)arg.getComponent().getParent().getParent().getParent().getParent();
  }

  @Override
  public void focusLost(FocusEvent arg) {
  }
}
