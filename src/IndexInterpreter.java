import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A structure that holds and performs operations on data from an input text file
 * containing information about staff and students at Bloor. Input files are to be
 * read into a single <code>String</code> and submitted to an
 * <code>IndexInterpreter</code> either through constructor or through this class's
 * <code>repopulate</code> method. Note that each instance of this class can only hold
 * a single index; merging of indices must be done at a higher level in order to search
 * multiple indices at a time. Doing so is not recommended.
 * 
 * <p>Requests for information are made by calling this class's
 * <code>execute(RequestEvent)</code> method, where the <code>String</code> parameter
 * contains the names of the students on whom information is being requested, as well
 * as the type of information being requested. See <code>execute(RequestEvent)</code>
 * documentation for more details.
 * 
 * <p>Any query, consisting of an array of names for lookup, should be answered by an
 * array of the same length containing results to lookup operations on single names,
 * where the index of a person's name in the first array corresponds to the index of the
 * query's response. If the answering array is of a different length than the input array,
 * an error was made in name recognition and either a name was not recognized, or a non-name
 * was interpreted as a name.
 */
public class IndexInterpreter implements Serializable, MugsEventStamps {

  /**
   * The identifier used to serialize instances of class <code>IndexInterpreter</code>.
   */
  private static final long serialVersionUID = -6273809401971336010L;

  /**
   * An access point to student/staff data, read from this <code>IndexInterpreter</code>'s
   * input text file. See PeopleDataList documentation.
   */
  private PeopleDataList index;

  /**
   * The name of the input file from which this <code>IndexInterpreter</code>'s data was drawn.
   */
  private String sourceFileName = null;
  
  /**
   * Initializes a new <code>IndexInterpreter</code>, which starts out without any
   * index file data. An index can be processed and stored by using the
   * <code>repopulate</code> method after initialization.
   */
  public IndexInterpreter() {
    index = new PeopleDataList();
  }

  /**
   * Initializes a new <code>IndexInterpreter</code>, which starts out holding data as specified
   * by the input string from the file specified by <code>source</code>. The input string
   * should not be edited from its form in the input file.
   * 
   * <p><code>source</code> is expected to contain a number of newline-separated lines with
   * at least 7 tab-separated datapoints each. See PeopleDataList for details on the structure
   * of an input line.
   * 
   * @param rawIndexData
   *        The contents of the input file this <code>IndexInterpreter</code> stores
   * @param source
   *        The name of the source input file
   */
  public IndexInterpreter(String rawIndexData, String source) {
    this();
    repopulate(rawIndexData, source);
  }

  /**
   * Load a new index file into this <code>IndexInterpreter</code>.
   * <br>Operates almost identically to a call to <code>IndexInterpreter</code>(String, String)
   * except any attributes not associated with an index are preserved.
   * 
   * @param rawIndexData
   *        The contents of the new input file this <code>IndexInterpreter</code> stores
   * @param source
   *        The name of the new source input file
   */
  public void repopulate(String rawIndexData, String source) {
    setSource(source);
    index = new PeopleDataList(rawIndexData);
  }

  /**
   * Set a value for the name of the index file whose data is stored in this
   * <code>IndexInterpreter</code>.
   * 
   * @param source
   *        The name of the new source input file
   */
  private void setSource(String source) {
    sourceFileName = source;
  }

  /**
   * Returns the name of the input file whose data is stored in this
   * <code>IndexInterpreter</code>.
   */
  public String getSource() {
    return sourceFileName;
  }

  /**
   * Process a request to access a particular piece of data about a particular person.
   * <br>This data is stored in a <code>RequestEvent</code>, which stores the type of
   * information being accessed as an integer stamp. The values of stamps are from
   * interface <code>MugsEventStamps</code>, and any object calling this method is
   * expected to implement that interface.
   * 
   * <p>The following data can be requested:
   * <br>A person's line number in the input file, specified by 
   * <code>MugsEventStamps.spellcheckStamp</code>
   * <br>A person's grade/age group, specified by 
   * <code>MugsEventStamps.gradeLookupStamp</code>
   * <br>A person's homeform room, specified by
   * <code>MugsEventStamps.homeformLookupStamp</code>
   * 
   * <p>These queries are conducted on the name specified in the <code>data</code>
   * attribute of the <code>RequestEvent</code>. If a match for this name is not
   * found in the stored index, that information will be given in this method's return. 
   * 
   * @param query
   *        A request for a piece of information about a person
   * @return The requested information if the person's name was found in the index,
   *         else a message indicating that no match was found.
   */
  public String[][] execute(RequestEvent query) {
    String[] queryNames = filter(query.getData());
    if (query.getType() == spellcheckStamp) {
      return new String[][]{queryNames, lookupSpellcheck(queryNames)};
    } else if (query.getType() == gradeLookupStamp) {
      return new String[][]{queryNames, lookupGrade(queryNames)};
    } else if (query.getType() == homeformLookupStamp) {
      return new String[][]{queryNames, lookupHomeform(queryNames)};
    } else {
      return new String[][]{{"ERROR: Button designator not recognized."},
                            {"SPELLED WRONG/NOT FOUND"}};
    }
  }

  /**
   * Process a list of names of any format, and store the <code>Strings</code> representing
   * names into an array, which is returned.
   * Commas or newlines can be used as separators between names.
   * 
   * <p>Names are matched with a regular expression, and as such are expected to match
   * a certain format. Any series of characters consisting of a set of letters, periods,
   * dashes, and spaces preceded by a space or newline and followed by a comma or newline
   * are interpreted as names.
   * <br>Be aware that any name containing characters other than those specified is not
   * recognized as a name and will not be stored.
   * 
   * <p>Individual names, mugs lists, sports lists, clubs lists, and manually input lists
   * can all be recognized and parsed properly.
   * 
   * @param input
   *        A list of names with details as specified above
   * @return An array of all the names contained in the list
   */
  private static String[] filter(String input) {
    /* Modify this expression if necessary to allow for names to be recognized that
       contain characters not specified. (Add them between the square brackets
       in the middle, ask Mr. Mario for more details if that fails.) */
    String nameFilterRegex = "(\\A|(?<=[ \\n]))([A-Za-z -]|\\.)+((?=[,\\n])|\\z)";
    String separator = "" + (char)1;
    String processed = "";
    
    // Please do not change anything in this block trying to change name recognition.
    try {
      Matcher nameMatcher = Pattern.compile(nameFilterRegex).matcher(input);
      while (!nameMatcher.hitEnd()) {
        nameMatcher.find();
        processed += nameMatcher.group() + separator;
      }
      return processed.split(separator);
    } catch (IllegalStateException err) {
      return processed.split(separator);
    }
  }

  /**
   * Look up each of an array of names in the index stored in this
   * <code>IndexInterpreter</code>, and return information about its location
   * in the source input file.
   * 
   * <p>If the name is found, its location in the index file will be returned.
   * Otherwise a message will be returned indicating that the name was spelled
   * incorrectly. See class documentation for more details on incorrect spelling
   * messages.
   * 
   * @param nameData
   *        The list of processed names being queried
   * @return The list of results for the lookup queries, where each result is
   *         at the same index in its array as the name query it was conducted on
   *         in <code>nameData</code>
   */
  private String[] lookupSpellcheck(String[] nameData) {
    String[] results = new String[nameData.length];

    for (int i = 0; i < nameData.length; i++) {
      String name = nameData[i];
      int foundIndex = index.searchName(name);

      if (foundIndex == -1) {
        results[i] = "SPELLED WRONG/NOT FOUND";
      } else {
        results[i] = "Found at index " + (foundIndex + 1);
      }
    }
    return results;
  }

  /**
   * Look up each of an array of names in the index stored in this
   * <code>IndexInterpreter</code>, and return the grades of each person.
   * 
   * <p>If the name is found, the person's grade entry will be returned.
   * Otherwise a message will be returned indicating that the name was spelled
   * incorrectly. See class documentation for more details on incorrect spelling
   * messages.
   * 
   * @param nameData
   *        The list of processed names being queried
   * @return The list of results for the lookup queries, where each result is
   *         at the same index in its array as the name query it was conducted on
   *         in <code>nameData</code>
   */
  private String[] lookupGrade(String[] nameData) {
    String[] results = new String[nameData.length];

    for (int i = 0; i < nameData.length; i++) {
      String name = nameData[i];
      String foundGrade = index.searchGrade(name);

      if (foundGrade == null) {
        results[i] = "SPELLED WRONG/NOT FOUND";
      } else if (foundGrade.equals("Staff")) {
        results[i] = "Found as staff";
      } else {
        results[i] = "Found in grade " + foundGrade;
      }
    }
    return results;
  }

  /**
   * Look up each of an array of names in the index stored in this
   * <code>IndexInterpreter</code>, and return the homeform rooms of each person.
   * 
   * <p>If the name is found, the person's homeform room entry will be returned.
   * Otherwise a message will be returned indicating that the name was spelled
   * incorrectly. See class documentation for more details on incorrect spelling
   * messages.
   * 
   * @param nameData
   *        The list of processed names being queried
   * @return The list of results for the lookup queries, where each result is
   *         at the same index in its array as the name query it was conducted on
   *         in <code>nameData</code>
   */
  private String[] lookupHomeform(String[] nameData) {
    String[] results = new String[nameData.length];

    for (int i = 0; i < nameData.length; i++) {
      String name = nameData[i];
      String foundHomeform = index.searchHomeform(name);

      if (foundHomeform == null) {
        results[i] = "SPELLED WRONG/NOT FOUND";
      } else if (foundHomeform.equals("Staff")) {
        results[i] = "Found as staff";
      } else {
        results[i] = "Found in homeform " + foundHomeform;
      }
    }
    return results;
  }
  
  @Override
  public String toString() {
    return getSource();
  }
}