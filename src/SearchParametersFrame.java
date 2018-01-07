import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Manager class to the search parameters frame, which is used to select advanced
 * options for search queries. These options take the form of grades and homeforms.
 * A user can use the mouse to select or deselect each grade or homeform.
 * Parameters will remain set and will be used even after the frame is closed.
 * 
 * <p>The default and initial setting for the frame is to treat all grades and
 * homeforms as selected. This default ccan be restored at any time by toggling
 * the Enable Parameters button at the top of the frame. When this button is
 * deselected, default behaviour will be used. When the button is selected,
 * the settings specified by the remaining toggle buttons will be used. Although
 * the remaining buttons will be disabled from changing whenever the Enable
 * Parameters button is deselected, the current settings on those buttons will be
 * retained and can be used and edited further by selected Enable Parameters again.
 * 
 * <p>This class is not an actual implementation of <code>JFrame</code>, and so any
 * class wanting to use a frame of this type must retrieve the active frame using the
 * <code>getParametersFrame</code> method.
 */
public class SearchParametersFrame extends Observable implements ActionListener {

  /**
   * The active frame containing all parameter buttons and options.
   */
  private JFrame inUse;
  
  /**
   * The Enable Parameters button, controlling whether default options are used.
   * <p>Default settings will be used if this button is deselected; otherwise,
   * settings will be as specified by the remaining buttons.
   */
  private JRadioButton parametrize;
  
  /**
   * The button specifying whether 9th grade is selected in the parameters for
   * the subsequent query.
   */
  private JRadioButton select9;
  
  /**
   * The button specifying whether 10th grade is selected in the parameters for
   * the subsequent query.
   */
  private JRadioButton select10;
  
  /**
   * The button specifying whether 11th grade is selected in the parameters for
   * the subsequent query.
   */
  private JRadioButton select11;
  
  /**
   * The button specifying whether 12th grade is selected in the parameters for
   * the subsequent query.
   */
  private JRadioButton select12;
  
  /**
   * The button specifying whether Staff designation is selected in the parameters
   * for the subsequent query.
   */
  private JRadioButton selectStaff;
  
  /**
   * A collection of all buttons that specify which grade 9 homeforms are selected
   * in the parameters for the subsequent query. The exact homeforms in this
   * collection are variable by year, since additional homeforms may be added from
   * year to year. Each button corresponds to a single, properly-formatted homeform,
   * not including split classes.
   */
  private JRadioButton[] homeforms9;
  
  /**
   * A collection of all buttons that specify which grade 10 homeforms are selected
   * in the parameters for the subsequent query. The exact homeforms in this
   * collection are variable by year, since additional homeforms may be added from
   * year to year. Each button corresponds to a single, properly-formatted homeform,
   * including 9/10 split classes.
   */
  private JRadioButton[] homeforms10;
  
  /**
   * A collection of all buttons that specify which grade 11 homeforms are selected
   * in the parameters for the subsequent query. The exact homeforms in this
   * collection are variable by year, since additional homeforms may be added from
   * year to year. Each button corresponds to a single, properly-formatted homeform,
   * including 10/11 split classes.
   */
  private JRadioButton[] homeforms11;
  
  /**
   * A collection of all buttons that specify which grade 12 homeforms are selected
   * in the parameters for the subsequent query. The exact homeforms in this
   * collection are variable by year, since additional homeforms may be added from
   * year to year. Each button corresponds to a single, properly-formatted homeform,
   * including 11/12 split classes.
   */
  private JRadioButton[] homeforms12;
  
  /**
   * A collection of all buttons that specify which of the following list of
   * homeform designations are selected in the parameters for the subsequent query:
   * <br>Staff
   * <br>Improperly formatted homeforms (e.g. those listed as room numbers)
   * <br>Unknown homeform (usually represented by ###)
   * 
   * <p> The exact homeforms in this collection are very likely to change from year
   * to year, as many of them depend on missing information or mistakes in the index
   * files that are received each year.
   */
  private JRadioButton otherHomeforms;
  
  /**
   * A button that will deselect all search parameters when pressed. This does not
   * include the Enable Parameters button, which will retain its previous state.
   */
  private JButton clearAll;
  
  /**
   * A button that will select all grade settings when pressed, namely grades 9-12
   * and Staff. All other buttons, including homeform settings and the Enable
   * Parameters button, will retain their previous states. 
   */
  private JButton fillGrades;
  
  /**
   * A button that will select exactly those homeform settings for which the
   * corresponding grade setting is selected, and deselect those for which the
   * corresponding grade is deselected. For example, if grade 9 is the only grade
   * that is selected, all grade 9 homeforms (excluding 9/10 split) will be selected
   * and no other homeforms will be selected. Intuitively, each homeform button is
   * set to match the grade button in the same row (excluding Staff and ### buttons).
   */
  private JButton fillHomeforms;
  
  /**
   * Set up a new <code>SearchParameters</code> frame, without providing any homeforms
   * to use for homeform settings. By default, no homeforms will be used, although
   * grade settings will still be present.
   */
  public SearchParametersFrame() {
    inUse = null;
    parametrize = new JRadioButton("Enable Parametrized Search");
    
    // There is no specification for what homeforms to include in these collections,
    // so set to null and await a list of homeforms to be submitted.
    homeforms9 = null;
    homeforms10 = null;
    homeforms11 = null;
    homeforms12 = null;
    otherHomeforms = null;
    
    // Grade selection settings can be used normally.
    select9 = new JRadioButton("Gr. 9");
    select10 = new JRadioButton("Gr. 10");
    select11 = new JRadioButton("Gr. 11");
    select12 = new JRadioButton("Gr. 12");
    selectStaff = new JRadioButton("Staff");
    
    // Extra utilities for selecting groups of buttons can be used normally.
    clearAll = new JButton("Clear");
    fillGrades = new JButton("Select Grades");
    fillHomeforms = new JButton("Select Homeforms");
  }
  
  /**
   * Create a layout for the current state of the <code>SearchParametersFrame</code>,
   * including the specified arrangement of homeforms.
   * 
   * <p>Homeforms are submitted as an array of lists. The list at each index in the
   * array is rendered as a row of buttons, the text from each of which is taken
   * from the list. So the text for a button in the ith row and kth column is taken
   * from the kth index of the list at index i of the array.
   * 
   * <p>Because there are to be four rows of homeforms in the display, the array of
   * lists must contain exactly four lists. In addition, each list must contain at
   * least one element. This restriction is not currently enforced, but it should
   * be followed to avoid unwanted behaviour or errors.
   * 
   * @param homeforms
   *        A preformatted array of four lists of homeform names, interpreted
   *        and rendered as rows
   * @return A frame
   */
  private JFrame createParametersFrame(List<String>[] homeforms) {
    // TODO: Method body too long, break apart into sections.
    // TODO: Setup of the frame and all components beside the homeforms panel
    //       should be moved to the constructor.
    // TODO: Add checking for illegal homeforms list input.
    JFrame newFrame = new JFrame("Mugs Reader - Set Search Parameters");
    Container contentPane = newFrame.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    
    JPanel top = new JPanel();
    parametrize = new JRadioButton("Enable Parametrized Search");
    top.add(parametrize);
    
    JPanel divider1 = new JPanel();
    divider1.setPreferredSize(new Dimension(top.getPreferredSize().width, 3));
    divider1.setBackground(new Color(180, 180, 180));
    
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    JPanel gradeButtons = new JPanel();
    SpringLayout gradeButtonsLayout = new SpringLayout();
    gradeButtons.setLayout(gradeButtonsLayout);
    
    JRadioButton[] selectButtons = new JRadioButton[] {select9, select10, select11, select12, selectStaff};
    for (int i = 0; i < selectButtons.length; i++) {
      selectButtons[i].setEnabled(false);
      gradeButtons.add(selectButtons[i]);
      gradeButtonsLayout.putConstraint(SpringLayout.WEST, selectButtons[i], 3, SpringLayout.WEST, gradeButtons);
      gradeButtonsLayout.putConstraint(SpringLayout.EAST, selectButtons[i], 0, SpringLayout.EAST, gradeButtons);
      
      if (i > 0) {
        gradeButtonsLayout.putConstraint(SpringLayout.NORTH, selectButtons[i], 0, SpringLayout.SOUTH, selectButtons[i - 1]);
      } else {
        gradeButtonsLayout.putConstraint(SpringLayout.NORTH, selectButtons[i], 3, SpringLayout.NORTH, gradeButtons);
      }
    }
    Dimension singleButton = select12.getPreferredSize();
    gradeButtons.setPreferredSize(new Dimension(singleButton.width + 3, (singleButton.height - 1) * 5));
    bottom.add(gradeButtons);
    
    bottom.add(Box.createHorizontalStrut(3));
    bottom.setBackground(new Color(180, 180, 180));
    
    JPanel homeformButtons = new JPanel();
    SpringLayout homeformLayout = new SpringLayout();
    homeformButtons.setLayout(homeformLayout);
    
    // TODO: Consolidate these next four loops into one loop.
    
    homeforms9 = new JRadioButton[homeforms[0].size()];
    int grade9Width = 0;
    for (int i = 0; i < homeforms9.length; i++) {
      homeforms9[i] = new JRadioButton((String)homeforms[0].get(i));
      homeforms9[i].setEnabled(false);
      homeformButtons.add(homeforms9[i]);
      homeformLayout.putConstraint(SpringLayout.NORTH, homeforms9[i], 3, SpringLayout.NORTH, homeformButtons);
      if (i > 0) {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms9[i], 0, SpringLayout.EAST, homeforms9[i - 1]);
      } else {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms9[i], 3, SpringLayout.WEST, homeformButtons);
      }
      grade9Width += homeforms9[i].getPreferredSize().width;
    }
    
    homeforms10 = new JRadioButton[homeforms[1].size()];
    int grade10Width = 0;
    for (int i = 0; i < homeforms10.length; i++) {
      homeforms10[i] = new JRadioButton((String)homeforms[1].get(i));
      homeforms10[i].setEnabled(false);
      homeformButtons.add(homeforms10[i]);
      homeformLayout.putConstraint(SpringLayout.NORTH, homeforms10[i], 0, SpringLayout.SOUTH, homeforms9[0]);
      if (i > 0) {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms10[i], 0, SpringLayout.EAST, homeforms10[i - 1]);
      } else {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms10[i], 3, SpringLayout.WEST, homeformButtons);
      }
      grade10Width += homeforms10[i].getPreferredSize().width;
    }
    
    homeforms11 = new JRadioButton[homeforms[2].size()];
    int grade11Width = 0;
    for (int i = 0; i < homeforms11.length; i++) {
      homeforms11[i] = new JRadioButton((String)homeforms[2].get(i));
      homeforms11[i].setEnabled(false);
      homeformButtons.add(homeforms11[i]);
      homeformLayout.putConstraint(SpringLayout.NORTH, homeforms11[i], 0, SpringLayout.SOUTH, homeforms10[0]);
      if (i > 0) {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms11[i], 0, SpringLayout.EAST, homeforms11[i - 1]);
      } else {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms11[i], 3, SpringLayout.WEST, homeformButtons);
      }
      grade11Width += homeforms11[i].getPreferredSize().width;
    }
    
    homeforms12 = new JRadioButton[homeforms[3].size()];
    int grade12Width = 0;
    for (int i = 0; i < homeforms12.length; i++) {
      homeforms12[i] = new JRadioButton((String)homeforms[3].get(i));
      homeforms12[i].setEnabled(false);
      homeforms12[i].addActionListener(this);
      homeformButtons.add(homeforms12[i]);
      homeformLayout.putConstraint(SpringLayout.NORTH, homeforms12[i], 0, SpringLayout.SOUTH, homeforms11[0]);
      if (i > 0) {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms12[i], 0, SpringLayout.EAST, homeforms12[i - 1]);
      } else {
        homeformLayout.putConstraint(SpringLayout.WEST, homeforms12[i], 3, SpringLayout.WEST, homeformButtons);
      }
      grade12Width += homeforms12[i].getPreferredSize().width;
    }
    
    // TODO: Consolidate the three button initializations into a loop for readability.
    
    if (!homeforms[3].isEmpty()) {
      otherHomeforms = new JRadioButton("###");
      otherHomeforms.setEnabled(false);
      homeformButtons.add(otherHomeforms);
      homeformLayout.putConstraint(SpringLayout.WEST, otherHomeforms, 3, SpringLayout.WEST, homeformButtons);
      homeformLayout.putConstraint(SpringLayout.NORTH, otherHomeforms, 0, SpringLayout.SOUTH, homeforms12[0]);
    
      int buttonHeight = homeforms12[0].getPreferredSize().height;
    
      homeformButtons.add(fillHomeforms);
      fillHomeforms.setEnabled(false);
      fillHomeforms.setPreferredSize(new Dimension(fillHomeforms.getPreferredSize().width, buttonHeight));
      homeformLayout.putConstraint(SpringLayout.EAST, fillHomeforms, 0, SpringLayout.EAST, homeformButtons);
      homeformLayout.putConstraint(SpringLayout.NORTH, fillHomeforms, 2, SpringLayout.SOUTH, homeforms12[0]);
      homeformLayout.putConstraint(SpringLayout.SOUTH, fillHomeforms, 0, SpringLayout.SOUTH, homeformButtons);
    
      homeformButtons.add(fillGrades);
      fillGrades.setEnabled(false);
      fillGrades.setPreferredSize(new Dimension(fillGrades.getPreferredSize().width, buttonHeight));
      homeformLayout.putConstraint(SpringLayout.EAST, fillGrades, 3, SpringLayout.WEST, fillHomeforms);
      homeformLayout.putConstraint(SpringLayout.NORTH, fillGrades, 2, SpringLayout.SOUTH, homeforms12[0]);
      homeformLayout.putConstraint(SpringLayout.SOUTH, fillGrades, 0, SpringLayout.SOUTH, homeformButtons);

      homeformButtons.add(clearAll);
      clearAll.setEnabled(false);
      clearAll.setPreferredSize(new Dimension(clearAll.getPreferredSize().width, buttonHeight));
      homeformLayout.putConstraint(SpringLayout.EAST, clearAll, 0, SpringLayout.WEST, fillGrades);
      homeformLayout.putConstraint(SpringLayout.NORTH, clearAll, 2, SpringLayout.SOUTH, homeforms12[0]);
      homeformLayout.putConstraint(SpringLayout.SOUTH, clearAll, 0, SpringLayout.SOUTH, homeformButtons);

      clearAll.addActionListener(this);
      fillGrades.addActionListener(this);
      fillHomeforms.addActionListener(this);
    }

    parametrize.addActionListener(this);
   
    int maxWidth = Math.max(grade9Width, Math.max(grade10Width, Math.max(grade11Width, grade12Width)));
    homeformButtons.setPreferredSize(new Dimension(maxWidth + 3, gradeButtons.getPreferredSize().height));
    
    bottom.add(homeformButtons);
    
    contentPane.add(top);
    contentPane.add(divider1);
    contentPane.add(bottom);

    return newFrame;
  }
  
  /**
   * Returns the parameters frame, with its layout prepared and ready for rendering.
   * 
   * @return The frame that contains and manages search parameter settings. 
   */
  @SuppressWarnings("unchecked")
  public JFrame getParametersFrame() {
    if (inUse == null) {
      // Render a temporary state with no homeform buttons, to allow the parameters
      // frame to be usable before a homeform list is submitted.
      ArrayList<String> dummy = new ArrayList<String>(); 
      loadHomeformList(new ArrayList[] {dummy, dummy, dummy, dummy, dummy});
    }
    return inUse;
  }
  
  /**
   * Restructure the current parameter state of the <code>SearchParametersFrame</code>
   * so that its homeform settings buttons display matches the list of homeforms
   * specified through the parameters. Doing this will destroy any previous state
   * of the parameters frame, and will close the frame if it is open. The new frame
   * will not be displayed after the change in homeforms, and will begin with default
   * settings.
   * 
   * <p>Homeforms are submitted as an array of lists. The list at each index in the
   * array is rendered as a row of buttons, the text from each of which is taken
   * from the list. So the text for a button in the ith row and kth column is taken
   * from the kth index of the list at index i of the array.
   * 
   * <p>Because there are to be four rows of homeforms in the display, the array of
   * lists must contain exactly four lists. In addition, each list must contain at
   * least one element. This restriction is not currently enforced, but it should
   * be followed to avoid unwanted behaviour or errors.
   * 
   * @param homeforms
   *        An array of four lists of homeform names, interpreted and rendered
   *        as rows of buttons.
   */
  public void loadHomeformList(List<String>[] homeforms) {
    // Wipe the previous state of the frame before recreating a new one.
	if (inUse != null) {
      inUse.dispose();
	}
	
	// Create a new parameters frame using the new homeform list.
    inUse = createParametersFrame(homeforms);
    inUse.pack();
    inUse.setResizable(false);
    // Sets the frame to not discard the state of its buttons when closed.
    inUse.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    // The new frame should not be in use immediately.
    inUse.setVisible(false);
  }
  
  /**
   * Returns all buttons in the <code>SearchParametersFrame</code> that represent
   * search settings; that is, all buttons beside the Enable Parameters button.
   * The buttons are formatted in a two-dimensional array, which is an analogue to
   * the structure in which they are rendered.
   * 
   * <p>Indices 1-4 in the outermost layer of the array represents a row of buttons.
   * The buttons occur in the inner arrays in the same order as that in which they
   * are rendered. Indices in the inner arrays represent columns, so two buttons at
   * the same index in their respective inner arrays are in the same column in the
   * rendered frame.
   * 
   * <p>Index 0 in the outermost array consists of all the grade selection buttons,
   * arranged in order from youngest to oldest. That is, grade 9 comes first, and
   * Staff comes last. The last index of the array is reserved for homeforms that
   * could not be assigned to a grade, such as Staff, improperly formatted homeforms,
   * and unknown homeforms.
   * 
   * @return The collection of search parameter buttons, formatted in a structure
   *         that imitates their arrangement in the frame.
   */
  private JRadioButton[][] getParameterButtons() {
    JRadioButton[] gradeButtons = new JRadioButton[] {select9, select10, select11, select12, selectStaff};
    return new JRadioButton[][] {gradeButtons, homeforms9, homeforms10, homeforms11, homeforms12, new JRadioButton[] {otherHomeforms}};
  }
  
  /**
   * Fills an array with boolean flags representing which grade/homeform
   * search parameter buttons are currently selected. Some extra spaces at the
   * beginning of the array are left as default values, the number of which is
   * specified by <code>offset</code>. These spaces are reserved for additional
   * parameters to be added in from the outside scope.
   * 
   * <p>The output is formatted with grades appearing first after the reserved
   * spaces, in order of age (i.e. grade 9 first, Staff last). Next, the statuses
   * of homeform buttons are added in the order in which they were arranged in
   * the homeform list through method <code>loadHomeformList</code>. The final
   * flag is for all non-standard homeforms, including Staff and unknown homeform.
   * 
   * <p>The array returned does not account for the state of the Enable Parameters
   * button, and only returns the state of other buttons. Therefore it is not
   * completely representative of which options to use for a subsequent search.
   * 
   * @param offset
   *        The number of spaces reserved at the beginning of the array
   * @return An array of boolean flags indicating the
   */
  private boolean[] getSelectedButtonsMask(int offset) {
    JRadioButton[][] allButtons = getParameterButtons();

    // Count the total number of search parameter buttons to create an array
    // of the right length.
    int fullSize = 0;
    for (JRadioButton[] buttonsSet : allButtons) {
      fullSize += buttonsSet.length;
    }
    boolean bitFlags[] = new boolean[fullSize + offset];

    // Variable to track current position in the output array, as opposed to
    // any indices in the buttons arrays.
    int absoluteIndex = 0;
    // As its name suggests, allButtons contains all relevant search parameter
    // buttons, including grades and 'other' (###), and already in proper order.
    for (JRadioButton[] buttonsSet : allButtons) {
      for (JRadioButton button : buttonsSet) {
        // Set the value of each button's flag to refer to whether it is selected.
        bitFlags[offset + absoluteIndex++] = button.isSelected();
      }
    }
    
    return bitFlags;
  }
  
  /**
   * Returns an array of flags indicating which search parameters are set to use
   * for a search. This array has several unchanged indices at the beginning, the
   * number of which is given by <code>offset</code>. This allows the outside scope
   * to reserve some number of spaces to add its own parameters.
   * 
   * <p>The array returned will account for search parameters being disabled, i.e.
   * the Enable Parameters button being deselected, by considering all grades
   * and homeforms to be selected. That is, it will fill the output array with
   * <code>true</code> values, excluding the reserved spaces at the beginning.
   * 
   * @param offset
   *        The number of reserved spaces at the beginning of the array
   * @return An array representing which search parameters are selected
   */
  public boolean[] getSearchParameters(int offset) {
    if (!parametrize.isSelected()) {
      // Search parameters are disabled: return an array of True values
      int numButtons = offset + 5 + homeforms9.length + homeforms10.length
    		                  + homeforms11.length + homeforms12.length + 1;
      boolean[] allTrue = new boolean[numButtons];
      Arrays.fill(allTrue, offset, numButtons, true);
      return allTrue;
    } else {
      // Search parameters are enabled: construct the array based on selected
      // buttons and return.
      return getSelectedButtonsMask(3);
    }
  }
  
  @Override
  public void actionPerformed(ActionEvent arg0) {
    // Some event has been triggered that requires response. This could be any
    // press of a button besides any grade/homeform search parameter button.
    // The toggling of those buttons does not evoke or require any response.

    if (arg0.getSource() == clearAll) {
      // Clear button has been pressed: deselect all search settings.
      // getParameterButtons() returns exactly the buttons that are associated
      // with search settings, which are exactly those that must be deselected.
      for (JRadioButton[] buttonsSet : getParameterButtons()) {
        for (JRadioButton button : buttonsSet) {
          button.setSelected(false);
        }
      }
    } else if (arg0.getSource() == fillGrades) {
      // Fill grades button has been pressed: select all grade options.
      JRadioButton[] grades = new JRadioButton[] {select9, select10, select11, select12, selectStaff};
      for (JRadioButton grade : grades) {
        grade.setSelected(true);
      }
    } else if (arg0.getSource() == fillHomeforms) {
      // Fill homeforms button has been pressed: this operation is described as
      // matching each homeform button's setting with the grade setting in the
      // same row.
      JRadioButton[][] buttons = getParameterButtons();
      // First index in getParameterButtons() stores all grade buttons in order.
      JRadioButton[] gradeButtons = buttons[0];
      // Loop excludes the last index in gradeButtons because that row does not
      // represent a grade, and contains only Staff and 'other' (###) buttons.
      for (int i = 0; i < gradeButtons.length - 1; i++) {
        // Maintain a flag representing the grade button's setting in this row.
        boolean rowActive = gradeButtons[i].isSelected();
        
        // Recalling that indices 1-4 in getParameterButtons() represent a row
        // of homeform buttons, set each row's value to match the grade button
        // in the same row.
        for (int m = 0; m < buttons[i + 1].length; m++) {
          buttons[i + 1][m].setSelected(rowActive);
        }
      }
    } else if (arg0.getSource() == parametrize) {
      // Enable Parameters button has been toggled: now all other buttons have
      // either been enabled if Enable Parameters has been selected, or disabled
      // if it has been deselected.
      for (JRadioButton[] buttonsSet : getParameterButtons()) {
        for (JRadioButton button : buttonsSet) {
          // Each button's enabled status is set to Enable Parameters' selected
          // status.
          button.setEnabled(parametrize.isSelected());
        }
      }
      
      // Also enable/disable the utility buttons in the same way.
      JButton[] buttonsList = new JButton[] {clearAll, fillGrades, fillHomeforms};
      for (JButton button : buttonsList) {
        button.setEnabled(parametrize.isSelected());
      }
    }
  }
}
