import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A self-contained frame containing a series of instructions for the mugs reader system.
 * The frame is separate from the remainder of the system, occupying a different thread,
 * and the system can be run normally with the instructions frame open.
 * 
 * <p>Instructions are divided into several pages, only one of which can be open at a time.
 * The active page can be changed using the <code>back</code> and <code>next</code> buttons
 * at the bottom of the frame: these buttons will become inactive if no previous or subsequent
 * page exists, respectively. Pages can only be traversed in order.
 * 
 * <p>The main instructional content of each page is in the form of a .jpg image that must be
 * appropriately named (<code>instr_pagex</code>, where x is the page number) and in the correct
 * folder to be used. Modifying the contents of this folder should be avoided if possible.
 * 
 * <p>This class implements a modified form of the Singleton design pattern: a single instance
 * of the class will exist at a time and can be retrieved by the method
 * <code>getInstructionsFrame</code> if and only if the previously loaded frame was able to
 * successfully render the content image for each page. Otherwise, the method
 * <code>getInstructionsFrame</code> will destroy the old,  incomplete instance and create
 * a new instance in case the images have become available. Either way, only one instance exists
 * at a time.
 */
public class InstructionsFrame extends JFrame implements ActionListener {

  /**
   * The identifier used to serialize instances of class <code>InstructionsFrame</code>.
   */
  private static final long serialVersionUID = -2376957726221393427L;

  /**
   * The instance of <code>InstructionsFrame</code> that is currently open. As per the
   * Singleton design pattern, this instance will be the only instance existing at any given time.
   * An instance that has successfully loaded all pages' content images will be reused indefinitely;
   * an instance that has not successfully loaded all content images may be eventually replaced.
   */
  private static InstructionsFrame inUse = new InstructionsFrame();

  /**
   * <code>true</code> if this instance has successfully rendered all pages worth of content images.
   */
  private boolean imgRendered = true;

  /**
   * The number of the page that is currently open.
   */
  private int pageNum = 0;
  
  /**
   * The total number of pages that can be accessed.
   */
  private final int pageNumTotal = 7;

  /**
   * A label displaying the number of the page currently open.
   */
  private JLabel pageNumLabel;

  /**
   * A button that causes the previous page to be shown. This button is inactive when
   * the first page is being displayed.
   */
  private JButton prev;

  /**
   * A button that causes the next page to be shown. This button is inactive when
   * the last page is being displayed.
   */
  private JButton next;

  /**
   * The main content of the instructions pane, displaying a .jpg image with details
   * on how to use the mugs reader. The content of this pane changes with the active page
   * number. The title and page-swapping buttons are not contained in this pane.
   */
  private JPanel display;

  /**
   * The beginning of the frame's title, which is constant with respect to active page number.
   */
  private final String titleBase = "Instructions - Page ";

  /**
   * The font used to render title elements in the frame.
   */
  private final Font titleFont = new Font("Arial", Font.BOLD, 22);

  /**
   * The colour used for the backgrounds of every panel in the frame.
   */
  private final Color background = new Color(100, 100, 100);
  
  /**
   * The colour of all title text, specified to be visible on the panel background colour.
   */
  private final Color textCol = new Color(230, 230, 230);

  /**
   * The name of the folder containing the images that fill the display pane.
   */
  private final String imgFolder = "img_instr";

  /**
   * The full filepath of the folder containing the images that fill the display pane.
   */
  private final String imgFolderPath = System.getProperty("user.dir") + File.separator 
                                       + imgFolder + File.separator;

  /**
   * Instantiate a new <code>InstructionsFrame</code>, starting with the first page active.
   */
  private InstructionsFrame() {
    pageNum = 0;
    setTitle(titleBase + "1");  // Frame header - ends with 'page 1'
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    
    display = new JPanel();
    display.setLayout(new CardLayout());

    // Page titles, in order of appearance.
    String[] titles = new String[] {"Welcome!", "Loading Index Files", "Priority Settings",
                                    "Single Lookup", "Mass Lookup", "Run from Command Prompt",
                                    "Run from Command Prompt (cont.)"};
    // Names of image files for each page, in order of appearance.
    String[] srcFiles = new String[] {"instr_page1.jpg", "instr_page2.jpg", "instr_page3.jpg",
                                      "instr_page4.jpg", "instr_page5.jpg", "instr_page6.jpg",
                                      "instr_page7.jpg"};
    for (int i = 0; i < pageNumTotal; i++) {
      display.add(setupDisplayPane(titles[i], srcFiles[i]), "" + i);
    }

    contentPane.add(display);
    contentPane.add(setupButtonsPane());

    setResizable(false);
    setDefaultCloseOperation(InstructionsFrame.DISPOSE_ON_CLOSE);
    pack();
  }

  /**
   * Returns the single active instance of this class, as per the Singleton design pattern.
   * The instance will try to reload itself upon the retrieval if it has failed to render the
   * content images of every page (see class documentation).
   * 
   * @return The active instance of <code>InstructionsFrame</code>
   */
  public static InstructionsFrame getFrame() {
    if (!inUse.imgRendered) {
      inUse.dispose();
      inUse = new InstructionsFrame();
    }
    return inUse;
  }

  /**
   * Reads and returns the content of an image file specified by <code>name</code>, packaged
   * as the icon of a <code>JLabel</code>. If the file could not be accessed, a
   * <code>JLabel</code> will be returned with text and null icon. This can be used to determine
   * if the image was successfully loaded.
   * 
   * <p>Images must be located in the correct folder for instructions links in order to be
   * properly loaded. The file must be one of a selection of readable formats, including JPG.
   * 
   * <p>Regardless of whether it was successfully loaded, the <code>JLabel</code> should have the
   * same preferred size of (400, 400) pixels. Images being loaded through this method can be
   * set to those dimensions beforehand to speed the loading and scaling process.
   * 
   * @param name
   *        The name of the image file
   * @return A <code>JLabel</code> object containing the specified image
   */
  private JLabel loadImage(String name) {
    try {
      Image basis = ImageIO.read(new File(imgFolderPath + name));
      basis = basis.getScaledInstance(400, 400, Image.SCALE_FAST);
      JLabel img = new JLabel(new ImageIcon(basis));
      return img;
    } catch (IOException err) {
      err.printStackTrace();
      // Construct a replacement with the appropriate size and layout.
      JLabel fallback = new JLabel("Unable to load " + name);
      fallback.setBackground(background);
      fallback.setPreferredSize(new Dimension(400, 400));
      imgRendered = false;
      return fallback;
    }
  }

  /**
   * Creates and returns a formatted <code>JPanel</code> with <code>text</code> written in the
   * center, for use as a title to the frame.
   * 
   * @param text
   *        The title to use
   * @return A title panel with the specified text
   */
  private JPanel setupTitlePane(String text) {
    JPanel head = new JPanel();
    head.setLayout(new BoxLayout(head, BoxLayout.PAGE_AXIS));
    head.setBackground(background);
    JLabel title = new JLabel(text);
    title.setFont(titleFont);
    title.setForeground(textCol);
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    head.add(Box.createVerticalStrut(15));
    head.add(title);
    head.add(Box.createVerticalStrut(15));
    return head;
  }

  /**
   * Assembles and returns a <code>JPanel</code> representing a single page of instructions.
   * 
   * <p>The parameter <code>backgroundImg</code> specifies the filename of an image containing
   * the entire instructional contents of the page in question, including text, images, and
   * formatting. An InDesign template is provided in the instructions resources folder. See
   * <code>loadImage</code> documentation for more details.
   * 
   * @param title
   *        The title for the page
   * @param backgroundImg
   *        The file name of the page's background image
   * @return The formatted page
   */
  private JPanel setupDisplayPane(String title, String backgroundImg) {
    JPanel centerPanel = new JPanel();
    centerPanel.setBackground(background);
    // GridBagLayout is used because BoxLayout seems to have trouble rendering the images.
    centerPanel.setLayout(new GridBagLayout());
    GridBagConstraints settings = new GridBagConstraints();
      
    settings.gridx = 0;
    settings.gridy = 0;
    centerPanel.add(setupTitlePane(title), settings);
      
    settings.gridx = 0;
    settings.gridy = 1;
    centerPanel.add(loadImage(backgroundImg), settings);
    
    return centerPanel;
  }

  /**
   * Prepares and returns the panel at the bottom of the frame containing buttons for page flipping.
   * One button, labeled 'back', flips to the previous page; the button labeled 'next' flips to the
   * next page, as expected. Once the panel is set up it will maintain itself without any need for
   * outside interference; that is, this method only needs to be called once, after which point the
   * buttons will track track when they need to update.
   * 
   * @return The panel containing page-flipping buttons
   */
  private JPanel setupButtonsPane() {
    JPanel btnPane = new JPanel();
    btnPane.setBackground(background);
    btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));

    prev = new JButton("Back");
    next = new JButton("Next");
    Dimension largeSize = new Dimension(prev.getPreferredSize().width * 2,
                                        prev.getPreferredSize().height * 2);
    for (JButton btn : new JButton[] {prev, next}) {
      btn.setPreferredSize(largeSize);
      btn.addActionListener(this);
      btn.setOpaque(false);
    }
    prev.setEnabled(false);
    
    pageNumLabel = new JLabel("1");
    pageNumLabel.setForeground(textCol);
    pageNumLabel.setFont(titleFont);
    btnPane.add(Box.createHorizontalStrut(5));
    btnPane.add(prev);
    btnPane.add(Box.createHorizontalGlue());
    btnPane.add(pageNumLabel);
    btnPane.add(Box.createHorizontalGlue());
    btnPane.add(next);
    btnPane.add(Box.createHorizontalStrut(5));
    return btnPane;
  }

  @Override
  public void actionPerformed(ActionEvent buttonPressed) {
    if (buttonPressed.getSource() == prev) {
      // Change the displayed page background.
      CardLayout c1 = (CardLayout)display.getLayout();
      c1.show(display, "" + --pageNum);
      // Modify which buttons are enabled according to the position of the enabled page.
      prev.setEnabled(pageNum > 0);
      next.setEnabled(true);

    } else if (buttonPressed.getSource() == next) {
      // Change the displayed page background.
      CardLayout c1 = (CardLayout)display.getLayout();
      c1.show(display, "" + ++pageNum);
      // Modify which buttons are enabled according to the position of the enabled page.
      next.setEnabled(pageNum < pageNumTotal - 1);
      prev.setEnabled(true);
    }
    // Update peripheral details to match the enabled page.
    setTitle(titleBase + (pageNum + 1));
    pageNumLabel.setText("" + (pageNum + 1));
  }
}
