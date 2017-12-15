import javax.swing.JTextField;

/**
 * A lookup pane purposed for searches on a single person at a time. Provides one
 * line for input and output.
 * 
 * <p>Input is done through the text field on the left. If a line of input is too
 * horizontally long to see in the text field, a split pane divider can be moved
 * to give more room to the input pane. On the other side of the divider is the
 * output text field, which is non-editable and is used to display requested information
 * about the name in the input text field.
 */
public class SingleLookupPane extends IndexLookupPane {

  /**
   * The identifier used to serialize instances of class <code>SingleLookupPane</code>.
   */
  private static final long serialVersionUID = -5057418291455801026L;

  @Override
  protected JTextField getTextComponent() {
    JTextField inputField = new JTextField();
    inputField.setFont(textDisplayFont);
    return inputField;
  }
}
