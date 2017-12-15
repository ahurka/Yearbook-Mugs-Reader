import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel containing the non-interactable components of the display that make up
 * the title area. May include non-editable text, images, or both. Can be interpreted
 * as a banner to attach to the top of a GUI.
 * 
 * <p>This class is not an actual implementation of <code>JPanel</code>, and so any
 * class wanting to use a panel of this type must retrieve the working panel using the
 * <code>getContentPanel</code> method.
 */
public class HeaderPane {

  /**
   * The panel containing all elements of the header pane.
   */
  private JPanel wholePane;

  /**
   * The title of the application.
   */
  private JLabel title;

  /**
   * Further information about the application, such as a subtitle, producer, version,
   * specifications, etc.
   */
  private JLabel subtitle;

  /**
   * Initialize a new <code>HeaderPane</code>, and position all components in the banner.
   * The panel will not draw itself or provide a space in which to draw itself. Its main
   * content pane, which can be retrieved using the method <code>getContentPane</code>,
   * must be added to a frame for it to be rendered.
   */
  public HeaderPane() {
    wholePane = new JPanel();
    wholePane.setLayout(new BoxLayout(wholePane, BoxLayout.PAGE_AXIS));

    // Add components.
    title = new JLabel("Welcome to the Yearbook's Mugs Reader!");
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    title.setFont(new Font("Arial", Font.PLAIN, 18));
    subtitle = new JLabel("Copyright The Bloor Banner ltd.");
    subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    wholePane.add(Box.createVerticalStrut(15));
    wholePane.add(title);
    wholePane.add(subtitle);
    wholePane.add(Box.createVerticalStrut(15));
  }

  /**
   * Returns a header panel for the mugs reader, containing formatted title text elements.
   * 
   * @return The mugs reader's header panel
   */
  public JPanel getContentPanel() {
    return wholePane;
  }
}
