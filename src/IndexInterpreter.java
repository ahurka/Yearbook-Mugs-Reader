import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
 * as the type of information being requested and current search parameters.
 * See <code>execute(RequestEvent)</code> documentation for more details.
 * 
 * <p>Any query, consisting of an array of names for lookup, should be answered by a
 * two-dimensional array containing at one index the names that produced results that
 * met the search's parameters, and at another index the search results for those names.
 * The index of a person's name in the first subarray corresponds to the index of the
 * query's response in the second subarray.
 */
public class IndexInterpreter implements Serializable, MugsEventStamps {

  /**
   * The identifier used to serialize instances of class <code>IndexInterpreter</code>.
   */
  private static final long serialVersionUID = -6273809401971336010L;

  /**
   * An access point to student/staff data, read from this <code>IndexInterpreter</code>'s
   * input text file. See <code>PeopleDataList</code> documentation.
   */
  private PeopleDataList index;

  /**
   * The name of the input file from which this <code>IndexInterpreter</code>'s data
   * was drawn.
   */
  private String sourceFileName = null;
  
  /**
   * The collection of homeforms read in from the input text file. These homeforms are
   * categorized into different indices in the array according to their grade. The
   * divisions are grade 9, 10, 11, 12, and all other improperly formatted homeforms.
   */
  private List<String>[] homeformList;
  
  /**
   * Initializes a new <code>IndexInterpreter</code>, which starts out without any
   * index file data. An index can be processed and stored by using the
   * <code>repopulate</code> method after initialization.
   */
  @SuppressWarnings("unchecked")
  public IndexInterpreter() {
    index = new PeopleDataList();
    ArrayList<String> dummy = new ArrayList<String>();
    homeformList = new List[] {dummy, dummy, dummy, dummy};
  }

  /**
   * Initializes a new <code>IndexInterpreter</code>, which starts out holding data as
   * specified by the input string from the file specified by <code>source</code>.
   * The input string should not be edited from its form in the input file.
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
    index = new PeopleDataList();
    homeformList = index.loadFileData(rawIndexData);
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
   * Returns the set of homeforms listed in the input file for this
   * <code>IndexInterpreter</code>. Each list of homeforms in the array consists of
   * all those homeforms of a certain category: index 0 of the array contains all
   * grade 9 homeforms, index 1 contains grade 10 homeforms, index 2 is grade 11,
   * index 3 is grade 12, and index 4 is all improperly formatted homeforms that
   * fall into none of the above categories, such as unknown homeform (###).
   * 
   * @return A structured set of homeforms to which students in this index belong.
   */
  public List<String>[] getHomeforms() {
    return homeformList;
  }
  
  /**
   * Process a request to access particular pieces of data about a list of people.
   * <br>The list of names to query, as well as search parameters, are stored in a
   * <code>RequestEvent</code>. The value of the request type is expected to be
   * set to <code>lookupStamp</code> under interface <code>MugsLookupStamps</code>,
   * but the value of the stamp is not used and so can be set to 0 for the purposes
   * of this method.
   * 
   * <p>The parameters specification inside the <code>RequestEvent</code> must
   * contain boolean flags for the following options:
   * <br>Three flags at the beginning of the array, representing [0] whether to report
   * index number in the output of the query, [1] whether to report grade, and [2] whether
   * to report homeform.
   * <br>The remaining flags specify which homeforms are excluded from the output.
   * A value at index i > 2 in the array specifies whether the (i - 3)'th homeform
   * is included in output. The ordering of the homeforms in this model is equivalent
   * to the ordering in the set returned from this class's <code>getHomeforms()</code>
   * method, and so the query parameters should be contructed based on that standard.
   * 
   * <p>The method returns an two-dimensional array containing at its first index all
   * names from the input that were not excluded by the parameter settings, and at its
   * second index the query results for those names. If a name is not found in the index,
   * its output will be an error string describing that the name was not found. If this
   * message appears as output for a name, then the name was spelled wrong or otherwise
   * not in the index file. Names that are not found will be reported no matter how the
   * search parameters are set.
   * 
   * <p>If the list of names to search on is the empty string, a search will be placed
   * on the whole contents of the index. Making a query with the empty string as its
   * name list can be used to report all staff and students in the index that meet
   * the parameters, which can, for instance, output all students in a certain grade or
   * all students in a certain set of homeforms.
   * 
   * @param query
   *        A request for a piece of information about a list of people
   * @return A set of the names that were queried, and the results from those queries
   */
  public String[][] execute(RequestEvent query) {
    String[] queryNames = filter(query.getData());
    boolean[] reportValues = Arrays.copyOf(query.getParams(), 3);
    
    // Sets to contain all grades and homeforms that will be allowed into output:
    // this corresponds to those whose flags in the parameters array are set to true.
    Set<String> selectedGrades = new TreeSet<>();
    Set<String> selectedHomeforms = new TreeSet<>();

    // Fill in all allowed grades from indices 3-7 in the parameters array.
    int posCounter = 3;
    for (String grade : new String[] {"09", "10", "11", "12", "Staff"}) {
      if (query.getParams()[posCounter++]) {
        selectedGrades.add(grade);
      }
    }
    
    // Add grade 9 (as opposed to grade 09) to the allowed grades if the grade 09
    // parameter flag is true, to account for badly formatted index files.
    if (query.getParams()[3]) {
      selectedGrades.add("9");
    }
    
    // Fill in all allowed, properly-formatted homeforms from indices 8 onward.
    // The last flag is not checked because that corresponds to badly formatted
    // homeforms, of which there are more than one.
    
    // A properly formatted homeform starts with two numbers, and ends in a letter,
    // for example 09A, 12S, or 09/10G. Poorly formatted homeforms may be room numbers
    // such as 211 or 302, or the unknown homeform string ###.
    for (int i = 0; i < homeformList.length - 1; i++) {
      for (String homeform : homeformList[i]) {
        if (query.getParams()[posCounter++]) {
          selectedHomeforms.add(homeform);
        }
      }
    }
    
    // Fill in all poorly-formatted homeforms into allowed homeforms if the
    // last flag in the parameters arrays is set to true.
    if (query.getParams()[posCounter]) {
      for (String mistakeHomeform : homeformList[homeformList.length - 1]) {
        selectedHomeforms.add(mistakeHomeform);
      }
    }
    
    // If the input string is empty, then use the index as a source of names
    // instead of the input. That is, inputting an empty string will cause a search
    // on all names in the index, respecting exclusions from the parameter settings.
    String[] nameInput = query.getData().equals("") ? index.getOrderedContents()
    		                                        : queryNames;
    
    // Get a list of output from the input and return.
    return lookup(nameInput, reportValues, selectedGrades, selectedHomeforms);
  }

  /**
   * Process a list of names of any format, and store the separated names in an array.
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
       in the middle, ask a teacher for more details if that fails.) */
    String nameFilterRegex = "(?<=([ \\n]|\\A))([A-Za-z -]|\\.)+(?=([,\\n]|\\z))";
    String separator = "" + (char)1;
    String processed = "";
    
    // Please do not change anything in this block.
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
   * Prepare a string to be used as output for a search that has produced the index, grade,
   * and homeform specified by parameters. Output is prepared based on the three flags
   * passed in through the method's parameters.
   * 
   * <p>The message produced is made by assuming the name was found with the specified
   * index, grade, and homeform. It will not produce an error message if the name was
   * not found. This error message must be produced externally.
   * 
   * @param foundIndex
   *        The index number at which the person was found
   * @param grade
   *        The grade to which the person belongs
   * @param homeform
   *        The person's homeform room designation
   * @param reportWhat
   *        Several boolean flags indicating which pieces of information are being
   *        requested in the output
   * @return A string with all requested data, formatted for output
   */
  private String prepareOutputString(int foundIndex, String grade, String homeform,
		                             boolean[] reportWhat) {
    String output = "Found";
    // Track whether any data have been added to the string, indicating whether
    // a comma is needed before the next data point.
    boolean elementWritten = false;

    if (reportWhat[0]) {
      // Index number information is requested as output: this is always listed first.
      output += " at index " + foundIndex;
      elementWritten = true;
    }
    
    if (reportWhat[1]) {
      // Grade information is requested in output: this takes second priority.
      output += elementWritten ? ", " : " ";
      output += "in grade " + grade;
      elementWritten = true;
    }
    
    if (reportWhat[2]) {
      // Homeform information is requested in output: this is always listed last.
      output += elementWritten ? ", " : " ";
      output += "in homeform " + homeform;
    }
    
    return output;
  }
  
  /**
   * Look up each of an array of names in the index stored in this
   * <code>IndexInterpreter</code>, and return information about the people
   * in the list as specified by <code>reportWhat</code>.
   * 
   * <p>If the name is found, its requested information will be returned.
   * Otherwise a message will be returned indicating that the name was spelled
   * incorrectly or otherwise not present in the index file.
   * 
   * The last two parameters, <code>allowedGrades</code> and
   * <code>allowedHomeforms</code>, allow control over what output is kept. If a
   * person being queried is in a grade not in <code>allowedGrades</code> or a
   * homeform not in <code>allowedHomeforms</code>, then that person's query
   * results wil not be reported in output.
   * 
   * @param nameData
   *        The list of processed names being queried
   * @param reportWhat
   *        Boolean flags indicating which data from each query to report in output.
   *        The first element is for index number, the second is for grade, and the
   *        third is for homeform. Each of the above datapoints will be included
   *        in output iff its corresponding flag is set to <code>true</code>.
   * @param allowedGrades
   *        The set of grades, in string form, to which output is restricted.
   * @param allowedHomeforms
   *        The set of homeforms, as strings, to which output is restricted.
   * 
   * @return In the first subarray, the names from the input that were not removed
   *         according to grade and homeform restrictions; in the second subarray,
   *         the output for each query in the first. The output and the query input
   *         are at the same indices in their respective arrays.
   */
  private String[][] lookup(String[] nameData, boolean[] reportWhat,
		                    Set<String> allowedGrades, Set<String> allowedHomeforms) {
    // Output lists for inputs and outputs are parallel:
    // names contains all input names of students that were in allowed grades and
    // homeforms. For any element at the ith index of names, the ith index of results
    // contains output from the query on the element.
    List<String> names   = new ArrayList<String>();
    List<String> results = new ArrayList<String>();

    for (int i = 0; i < nameData.length; i++) {
      // Search the index for the next name. Finding its index number will help identify
      // whether the name was actually in the index, or if it was not found.
      String name = nameData[i];
      int foundIndex = index.searchName(name);
      
      if (foundIndex == 0) {
        // The name was not found: report an error message as output for this query.
        // Unfound names are reported regardless of grade/homeform specifications.
        names.add(name);
        results.add("SPELLED WRONG/NOT FOUND"  + "   " + name.length());
      } else {
        // The name was found in the index. Both grade and homeform are required to
        // tell if it meets the output specifications.
    	String grade = index.searchGrade(name);
        String homeform = index.searchHomeform(name);
        
        if (allowedGrades.contains(grade) && allowedHomeforms.contains(homeform)) {
          // The name meets output specifications. Add it to output.
          names.add(name);
          // Prepare the query results for presentation and readability.
          results.add(prepareOutputString(foundIndex, grade, homeform, reportWhat));
        }
      }
    }
    
    // Convert list types into String arrays now that no more mutations are necessary.
    return new String[][] {names.toArray(new String[0]),
    	                   results.toArray(new String[0])};
  }
  
  @Override
  public String toString() {
    return getSource();
  }
}