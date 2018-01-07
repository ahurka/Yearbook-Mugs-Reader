import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * A panel controlling miscellaneous operations and input controls. More specifically,
 * allows the user to change the input source used in the program's model. Provides
 * options to control how indices are searched through and chosen, by allowing the
 * user to specify an input file they want to use frequently.
 * 
 * <p>The user can choose an input file to use by typing its file name into the
 * text bar provided in this panel, and send the request to switch to that input
 * file by clicking the button to the left of the text bar. Options for controlling
 * the order in which parsed input files are searched through are provided by
 * toggling buttons to the right of the text bar.
 * 
 * <p>An instructions button is also provided, which opens a set of popups
 * that explain the setup of the GUI and how to use it.
 * 
 * <p>This class is not an actual implementation of <code>JPanel</code>, and so any
 * class wanting to use a panel of this type must retrieve the working panel using the
 * <code>getContentPanel</code> method. After that the panel will continue to operate
 * as expected and respond to user interactions.
 */
public class IndexResetPane extends Observable implements ActionListener, MugsEventStamps {

  /**
   * The panel containing all elements of the pane. Contains a text bar for inputing
   * source file names, buttons for modifying source file options, and an instructions
   * button.
   */
  private JPanel contentPanel;

  /**
   * The text bar for inputing source file names.
   */
  private JTextField fileTextInput;

  /**
   * The button that triggers changes in the source file. When pressed, the source file
   * will be set to the one whose name is specified in the input text bar.
   */
  private JButton sourceFileReset;

  /**
   * The button to set the chosen source file to manual ordering. A file set to manual
   * priority will always begin in use when the program is restarted. Pressing this button
   * disables the complementary button <code>setAutomatic</code>.
   */
  private JRadioButton setManual;

  /**
   * The button to set the chosen source file to automatic ordering. A file set to automatic
   * ordering will be retrieved after some iteration, at a position depending on how frequently
   * it is used. Pressing this button disables the complementary button <code>setManual</code>.
   */
  private JRadioButton setAutomatic;

  /**
   * The button to open instruction slides.
   */
  private JButton instructions;

  /**
   * Initialize a new <code>IndexResetPane</code> with the specified preferred pixel width
   * and using the specified source file. The name of the file will be initially displayed
   * in the input text bar, with the appropriate ordering button enabled.
   * <code>sourceIndexData</code> may be null, in which case no source file is currently in
   * use and one must be loaded for the program to be useful.
   * 
   * <p>All setup and arranging of components will be done, and the content panel will be
   * prepared for retrieval.
   * 
   * @param width
   *        The preferred pixel with of this panel
   * @param sourceIndexData
   *        The source file already in use, if applicable
   * @param sourceManual
   *        <code>true</code> if the source file in use is set to manual priority
   */
  public IndexResetPane(int width, String sourceIndexData, boolean sourceManual) {
    contentPanel = new JPanel();
    SpringLayout contentLayout = new SpringLayout();
    contentPanel.setLayout(contentLayout);
    
    // Add the button to update source file.
    sourceFileReset = new JButton("Update saved index file:");
    contentPanel.add(sourceFileReset);
    sourceFileReset.addActionListener(this);
    contentLayout.putConstraint(SpringLayout.WEST, sourceFileReset, 
                                0, SpringLayout.WEST, contentPanel);

    // Add the input text bar.
    fileTextInput = new JTextField(sourceIndexData, 15);
    fileTextInput.setFont(new Font("Arial", Font.PLAIN, 14));
    contentPanel.add(fileTextInput);
    contentLayout.putConstraint(SpringLayout.WEST, fileTextInput, 
                                5, SpringLayout.EAST, sourceFileReset);
    
    // Add the ordering buttons.
    setManual = new JRadioButton("Manual", sourceManual);
    contentPanel.add(setManual);
    setAutomatic = new JRadioButton("Automatic", !sourceManual);
    contentPanel.add(setManual);
    contentLayout.putConstraint(SpringLayout.WEST, setManual,
                                0, SpringLayout.EAST, fileTextInput);
    contentPanel.add(setAutomatic);
    contentLayout.putConstraint(SpringLayout.WEST, setAutomatic,
                                0, SpringLayout.EAST, setManual);

    // Set the ordering buttons to be enabled one at a time.
    ButtonGroup priorityMethodToggle = new ButtonGroup();
    priorityMethodToggle.add(setManual);
    priorityMethodToggle.add(setAutomatic);

    // Add the instructions button.
    instructions = new JButton("Instructions");
    instructions.addActionListener(this);
    contentPanel.add(instructions);
    contentLayout.putConstraint(SpringLayout.EAST, instructions,
                                0, SpringLayout.EAST, contentPanel);

    // Set layout options.
    contentPanel.setPreferredSize(new Dimension(width, sourceFileReset.getPreferredSize().height));
    pinVertical(contentLayout, sourceFileReset);
    pinVertical(contentLayout, fileTextInput);
    pinVertical(contentLayout, setManual);
    pinVertical(contentLayout, setAutomatic);
    pinVertical(contentLayout, instructions);
  }

  /**
   * Ensure the specified component is vertically centered in the specified
   * <code>SpringLayout</code>. The component must already have been added to the
   * layout. Horizontal position is not determined or changed.
   * 
   * @param layout
   *        The <code>SpringLayout</code> the component is placed in
   * @param component
   *        The component being positioned
   */
  private void pinVertical(SpringLayout layout, JComponent component) {
    layout.putConstraint(SpringLayout.NORTH, component, 
                                0, SpringLayout.NORTH, contentPanel);
    layout.putConstraint(SpringLayout.SOUTH, component, 
                                0, SpringLayout.SOUTH, contentPanel);
  }

  /**
   * Returns the formatted panel, containing functions for the user to control the index file
   * in use. Once retrieved and added to a frame, the pane will continue to behave as expected,
   * responding to user input appropriately.
   * 
   * @return The main panel of this <code>IndexResetPane</code>
   */
  public JPanel getContentPanel() {
    return contentPanel;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    JButton buttonPressed = (JButton)evt.getSource();
    if (buttonPressed == sourceFileReset && fileTextInput.getText().length() > 0) {
      // Represents a request to change source file.
      setChanged();
      
      byte requestStamp;
      /* This stamp communicates to observers whether the user wants the file to use
         manual or automatic ordering. */
      if (setManual.isSelected()) {
        requestStamp = setManualPriority;
      } else {
        requestStamp = setAutomaticPriority;
      }

      // The request is packaged into a RequestEvent with the chosen stamp.
      notifyObservers(new RequestEvent(requestStamp, null, fileTextInput.getText()));
    } else if (buttonPressed == instructions) {
      // Create and set an InstructionsFrame, or refocus an existing one, which operates on its own.
      InstructionsFrame intr = InstructionsFrame.getFrame();
      intr.setLocation(600, 200);
      intr.setVisible(true);
    }
  }
}
