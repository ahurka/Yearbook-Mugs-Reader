import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Observable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

/**
 * A panel containing the main user-interaction portion of the system, corresponding
 * to the controller and view of the system. Also constitutes the visible part of the
 * user interface that deals with name lookup operation input and output.
 * 
 * <p>This pane has three main components, each of which are mostly independent.
 * Two components are text input and output panels, and the other contains buttons.
 * One of the text panels is a single row designed for looking up information about
 * a single person. The multiple row text panel is for lookup on any number of names,
 * which can be flexibly formatted. The buttons mainly perform operations on the
 * contents of the text panes, and also provide options to customize searches.
 * 
 * <p>After a search is performed and results are available, the results are displayed
 * in output spaces to the right of the corresponding input text spaces. So any single
 * name lookup requests made from the single-row text panel would have their output
 * displayed on the adjacent single-row output text panel. After the output is displayed
 * in the appropriate lookup pane's output field, the input field remains selected.
 * Further requests for different information can be made on the same set of input
 * without having to select the input text field again. Doing so will replace the
 * previous request's output with that of the new request.
 * 
 * Several buttons are provided as parameters to each search. Firstly, two buttons
 * at the bottom of the control pane allow the user to specify which information
 * should be displayed from the search. More than one of the two options (grade and
 * homeform) can be selected, and both will be displayed. If neither of the buttons
 * is selected, index number will be displayed instead. An additional parameters
 * button opens a window with further options to specify search output.
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
   * for searches and maintenance.
   */
  private JPanel wholePane;

  /**
   * The panel responsible for collecting and displaying the results of single name
   * lookup requests. Once results arrive the input text field will not be changed and
   * will remain selected; performing another search will use the same input.
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
   * The panel containing all buttons related to information queries. Options are
   * provided for searches, specifying information in the output, and maintenance.
   * 
   * <p>Search button: Perform a search using the current parameters.
   * <br>Report buttons: Choose what information to display for subsequent searches.
   * <br>Parameters button: Opens a menu to set search parameters, controlling which
   * lines of output are kept.
   * <br>Clear button: Removes all text from all input and output text spaces.
   */
  private JPanel buttonPane;

  /**
   * The button that identifies whether a student's grade should be included in the output
   * of the next search operation.
   */
  private JRadioButton reportGrade = new JRadioButton("Report Grade");

  /**
   * The button that identifies whether homeform room should be included in the output
   * of the next search operation.
   */
  private JRadioButton reportHomeform = new JRadioButton("Report Homeform");
  
  /**
   * The button that resets both input and both output text spaces to contain no text.
   */
  private JButton clear = new JButton("Clear Text Frames");
  
  /**
   * The button that launches a search using the text from the previously selected input
   * space, with the currently selected parameters.
   */
  private JButton search = new JButton("Search");
  
  /**
   * The button to access the parameters window, from which the user can place
   * restrictions on which lines of output are kept and displayed from subsequent
   * searches.
   */
  private JButton params = new JButton("Set Parameters");
  
  /**
   * The functional controller for the parameters window, maintaining the window and
   * providing an interface through which the control pane can easily access the
   * current parameter specification.
   */
  private SearchParametersFrame getParams;

  /**
   * The lookup pane that was most recently selected; the lookup pane to draw names from
   * and output results when a lookup-related button is pressed.
   */
  private IndexLookupPane selected;

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
    selected = massLookup;
    
    wholePane.add(massLookup);
    wholePane.add(Box.createVerticalStrut(5));
    wholePane.add(singleLookup);
    wholePane.add(Box.createVerticalStrut(5));

    buttonPane = new JPanel();
    SpringLayout buttonsLayout = new SpringLayout();
    buttonPane.setLayout(buttonsLayout);
    buttonPane.add(search);
    buttonsLayout.putConstraint(SpringLayout.WEST, search, 0, SpringLayout.WEST, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.NORTH, search, 0, SpringLayout.NORTH, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.SOUTH, search, 0, SpringLayout.SOUTH, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.EAST, search, 165, SpringLayout.WEST, buttonPane);
    
    buttonPane.add(reportGrade);
    buttonsLayout.putConstraint(SpringLayout.WEST, reportGrade, 2, SpringLayout.EAST, search);
    buttonPane.add(reportHomeform);
    buttonsLayout.putConstraint(SpringLayout.WEST, reportHomeform, 2, SpringLayout.EAST, search);
    buttonsLayout.putConstraint(SpringLayout.NORTH, reportGrade, -2, SpringLayout.NORTH, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.SOUTH, reportHomeform, 1, SpringLayout.SOUTH, buttonPane);
    
    buttonPane.add(clear);
    buttonsLayout.putConstraint(SpringLayout.EAST, clear, 0, SpringLayout.EAST, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.NORTH, clear, 0, SpringLayout.NORTH, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.SOUTH, clear, 0, SpringLayout.SOUTH, buttonPane);
    
    buttonPane.add(params);
    buttonsLayout.putConstraint(SpringLayout.EAST, params, -5, SpringLayout.WEST, clear);
    buttonsLayout.putConstraint(SpringLayout.NORTH, params, 0, SpringLayout.NORTH, buttonPane);
    buttonsLayout.putConstraint(SpringLayout.SOUTH, params, 0, SpringLayout.SOUTH, buttonPane);
    
    int buttonsWidth = wholePane.getPreferredSize().width;
    int buttonsHeight = (int)(clear.getPreferredSize().height * 1.5);
    Dimension buttonsSize = new Dimension();
    buttonsSize.setSize(buttonsWidth, buttonsHeight);

    buttonPane.setPreferredSize(buttonsSize);
    
    wholePane.add(buttonPane);
    
    clear.addActionListener(this);
    params.addActionListener(this);
    search.addActionListener(this);
    
    // Focus listeners are used to determine which lookup pane was last selected.
    singleLookup.addFocusListener(this);
    massLookup.addFocusListener(this);
    
    getParams = new SearchParametersFrame();
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
  
  private void clearTextPanes() {
    singleLookup.display(null);
    massLookup.display(null);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    JButton buttonPressed = (JButton)event.getSource();
      
    if (buttonPressed == clear) {
      clearTextPanes();
    } else if (buttonPressed == params) {
      JFrame paramsFrame = getParams.getParametersFrame();
      paramsFrame.setLocation(300, 300);
      paramsFrame.setVisible(true);
    } else if (buttonPressed == search) {
      String selectedText = selected.getInputText();
//      System.out.println("Searching " + selectedText);

      if (selectedText != null) {
        boolean[] params = getParams.getSearchParameters(3);
          
        params[1] = reportGrade.isSelected();
        params[2] = reportHomeform.isSelected();
        params[0] = !(reportGrade.isSelected() || reportHomeform.isSelected());
          
        RequestEvent query = new RequestEvent(lookupStamp, params, selectedText);
      
        /* Send a request to the system to access the requested information.
           Once the request is processed, the results will be sent back from outside. */
        setChanged();
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

  public void loadHomeformList(List<String>[] homeforms) {
    getParams.loadHomeformList(homeforms);
  }
}
