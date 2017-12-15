import java.io.Serializable;
import java.util.HashMap;

/**
 * A digital structure that stores the entire contents of an input file, interpreted
 * as the student/staff population of BCI. Information about each person is exactly
 * as it was in the input file.
 * 
 * <p>People are stored by full name, meaning that for any further data to be retrieved,
 * the person's name must be known and spelled correctly in order to be recognized by
 * the <code>PeopleDataList</code>. Queries with names that are not recognized in the
 * index are interpreted as spelling errors. Note that names used in mugs pages may
 * disagree with names from the mugs index; what is interpreted as a spelling error
 * may be a difference in naming conventions on a person.
 */
public class PeopleDataList implements Serializable {

  /**
   * The identifier used to serialize instances of class <code>PeopleDataList</code>.
   */
  private static final long serialVersionUID = 3098066693138104289L;

  /**
   * A representation of a student or staff member from BCI. Information about a
   * <code>Person</code> is extracted from an input file, and <code>Person</code>
   * stores that data exactly as it comes from the input.
   * 
   * <p>A <code>Person</code> can either represent a student or a staff member.
   * The two representations differ only because their information is presented
   * differently in the input file. Staff members will have their <code>grade</code>
   * and <code>homeform</code> attributes set to Staff, and their first name(s)
   * will be initialized.
   * <br>Aside from these points, staff and students are not differentiated.
   */
  private class Person implements Serializable {

    /**
     * The identifier used to serialize instances of class <code>Person</code>.
     */
    private static final long serialVersionUID = 6072202665031406203L;

    /**
     * The line number in the input file at which this <code>Person</code>'s data is located.
     */
    private int indexNum;

    /**
     * A recording of the age group this <code>Person</code> belongs to.
     * 
     * <p>The student's grade, if this <code>Person</code> represents a student.
     * <br>"Staff", if this <code>Person</code> represents a staff member.
     */
    private String grade;

    /**
     * The student's homeform name, if this <code>Person</code> represents a student.
     * <br>"Staff", if this <code>Person</code> represents a staff member.
     * <br>Be aware that the a teacher's homeform class will not be stored here. 
     */
    private String homeform;

    /**
     * The first and last name of the student or staff this <code>Person</code> represents.
     * <br>Students will have full first and last names, while teachers only have first initials.
     * <br>Names are separated by spaces, and first and last names are not distinguished.
     */
    private String name;

    /**
     * Initializes a new <code>Person</code> instance based on an index file input line.
     * 
     * <p>Input data must contain at least 7 tab-separated values, where the 4th value is the
     * person's grade, the 5th value is the person's last name, the 6th value is the
     * person's first name, and the 7th value is the person's homeform room (or Staff).
     * <br>Any other value in the line can be any length and content.
     * <br>There can be leading or trailing tabs.
     * 
     * @param rawData
     *        The input file line this <code>Person</code> is constructed from.
     * @param ind
     *        The line number at which the above information was located.
     */
    private Person(String rawData, int ind) {
      indexNum = ind;
      String[] data = rawData.split("\\t+");
      grade = data[3];
      homeform = data[6];
      // Condense first and last names
      name = data[5] + " " + data[4];
    }

    /**
     * Returns the index location for the original input data this <code>Person</code>
     * was generated from.
     * <br>Can be interpreted as the line in the relevant input file where the person's
     * full data is located.
     * 
     * @return The initial line number of this <code>Person</code>.
     */
    private int getIndexNum() {
      return indexNum;
    }

    /**
     * Returns the <code>Person</code>'s grade/age group: the student's grade number if
     * this <code>Person</code> represents a student, or "Staff" if this <code>Person</code>
     * represent a staff member.
     * 
     * @return The person's grade.
     */
    private String getGrade() {
      return grade;
    }

    /**
     * Returns the <code>Person</code>'s homeform room if this <code>Person</code>
     * represents a student, or "Staff" if this <code>Person</code> represent a staff member.
     * 
     * @return The person's homeform.
     */
    private String getHomeform() {
      return homeform;
    }

    /**
     * Returns the <code>Person</code>'s full name. if this <code>Person</code>
     * represents a staff member, the first name will be initialized.
     * 
     * <p>Middle names are included in their order. First, middle, and last names
     * are not distinguished.
     * 
     * @return The person's full name.
     */
    private String getName() {
      return name;
    }
  }

  /**
   * A processed form of a index text file containing staff and student information.
   * <code>String</code> keys in the map represent the full names of people in the
   * input file, which map to further data about the person in question.
   */
  private HashMap<String, Person> mugsIndex;

  /**
   * Initializes a new <code>PeopleDataList</code> which starts out without any
   * index information.
   */
  public PeopleDataList() {
    mugsIndex = new HashMap<String, Person>();
  }

  /**
   * Initializes a new <code>PeopleDataList</code>, which starts out holding data as specified
   * by the input string. The input string should not be edited from its form in the input file.
   * 
   * <p>The input file is expected to contain a number of newline-separated lines with at least
   * 7 tab-separated datapoints each. For initialization to succeed, the datapoints must be in
   * the order as specified by the PeopleDataList$Person constructor.
   * 
   * 
   * @param rawInput
   *        The contents of the input file this <code>PeopleDataList</code> will hold data from.
   */
  public PeopleDataList(String rawInput) {
    this();
    
    String[] lineDividedInput = rawInput.split("\\n");
    for (int indexPos = 0; indexPos < lineDividedInput.length; indexPos++) {
      String line = lineDividedInput[indexPos];
      add(line, indexPos);
    }
  }

  /**
   * Store away data on a single person, represented by a single unmodified line from
   * the input file. The line's relevant data (name, grade, homeform) is preserved
   * alongside the location of the line in the input file.
   * 
   * @param data
   *        A line of student/staff information from the input file
   * @param ind
   *        The line number of the line in the input file from which said information
   *        was gathered
   */
  private void add(String data, int ind) {
    // Person constructor processes the input
    Person newPerson = new Person(data, ind);
    mugsIndex.put(newPerson.getName(), newPerson);
  }

  /**
   * Look up a person's name in the stored index, confirming that the person's name
   * is present there.
   * <br>If the name is found, its location in the index file will be returned.
   * If the name is not found, -1 will be returned, indicating that the name was
   * incorrectly spelled or the person was not present in the stored index.
   * 
   * @param name
   *        The name of the person being spell-checked
   * @return The input file line number the person's data came from, or -1 if the person
   *         was not found in the index
   */
  public int searchName(String name) {
    Person nameMatch = mugsIndex.get(name);
    if (nameMatch == null) {
      return -1;
    } else {
      return nameMatch.getIndexNum();
    }
  }

  /**
   * Look up a person's name in the stored index, returning his/her grade if
   * a name match is found.
   * <br>If the person is a student, his/her grade number will be returned;
   * if he/she is a staff member, "Staff" will be returned.
   * <br>If the name is not found, null will be returned, indicating that the name was
   * incorrectly spelled or the person was not present in the stored index.
   * 
   * @param name
   *        The name of the person being queried
   * @return The students's grade if the person is a student, "Staff" if the person
   *         is a staff member, or null if the person was not found in the index
   */
  public String searchGrade(String name) {
    Person nameMatch = mugsIndex.get(name);
    if (nameMatch == null) {
      return null;
    } else {
      return nameMatch.getGrade();
    }
  }

  /**
   * Look up a person's name in the stored index, returning his/her homeform room if
   * a name match is found.
   * <br>If the person is a student, his/her homeform class will be returned;
   * if he/she is a staff member, "Staff" will be returned.
   * <br>If the name is not found, null will be returned, indicating that the name was
   * incorrectly spelled or the person was not present in the stored index.
   * 
   * @param name
   *        The name of the person being queried
   * @return The students's homeform room if the person is a student, "Staff" if he/she
   *         is a staff member, or null if the person was not found in the index
   */
  public String searchHomeform(String name) {
    Person nameMatch = mugsIndex.get(name);
    if (nameMatch == null) {
      return null;
    } else {
      return nameMatch.getHomeform();
    }
  }
}