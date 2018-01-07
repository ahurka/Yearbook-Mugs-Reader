import javax.swing.JTextArea;

/**
 * A lookup pane purposed for searches on a multiple people at a time. Provides
 * multiple rows of text spaces for input and output. Input can be formatted in
 * a variety of ways, including directly copied from a mugs page or a sports/clubs
 * naming list, or manually entered with commas or newlines separating each name.
 * 
 * <p>Input is done through the text field on the left, which is variable height
 * to allow as much text as is necessary. If a set of input is too horizontally long
 * to see in the text field, a split pane divider can be moved to give more room
 * to the input pane. On the other side of the divider is the output text field, which
 * is non-editable and is used to display requested information about the set of
 * names in the input text field. A name in the input field and its output in the
 * output field will be at the same vertical position in their respective text areas.
 * 
 * <p>The input and output text fields are both provided with scroll bars, which will
 * synchronize whenever the input and output text fields contain matching content
 * (the output field displays the output for all content in the input field), such
 * that the two move together whenever one is moved by the user. This will ensure
 * that a person's name in the input field is lined up properly with its associated
 * output. When the input field is modified, it will no longer perfectly match with
 * the output field and the two scroll bars will be desynchronized and operate on
 * their own.
 * 
 * <p>For searches on many names, <code>MassLookupPane</code> will count output and
 * notify the user how many lines of output there are. This helps the user to identify
 * the size of the output when it is not the same size as the input.
 */
public class MassLookupPane extends IndexLookupPane {

  /**
   * An identifier used to serialize instances of class <code>MassLookupPane</code>.
   */
  private static final long serialVersionUID = 6923577320726514111L;

  @Override
  protected JTextArea getTextComponent() {
    JTextArea inputField = new JTextArea();
    inputField.setRows(18);
    inputField.setFont(textDisplayFont);
    return inputField;
  }
  
  @Override
  protected String[] getDisplayText(String[][] results) {
    // This subclass displays text in the same was as the superclass, except
    // with a two-line header to show how many results were received.
    String[] out = super.getDisplayText(results);
    // Input side two empty lines for its side of the header.
    out[0] = "\n\n" + out[0];
    String header = "Showing " + results[1].length + " result";
    
    if (results[1].length != 1) {
      // Pluralize 'results' for any number except for 1 result.
      header += "s";
    }
    
    // Output side receives the output line count for its side of the header.
    out[1] = header + ":\n\n" + out[1];
    
    return out;
  }
}
