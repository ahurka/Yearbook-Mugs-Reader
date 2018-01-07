import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A digital structure that stores the entire contents of an input file, interpreted
 * as a population of students and staff in a school. Information about each person
 * is exactly as it was in the input file.
 * 
 * <p>People are stored by first and last name, meaning that for any further data to be
 * retrieved, the person's name must be known and spelled correctly in order to be
 * recognized by the <code>PeopleDataList</code>. Queries with names that are not
 * recognized in the index are interpreted as spelling errors. Note that names used in
 * mugs pages may disagree with names from the index file; what is interpreted as a
 * spelling error may be a difference in naming conventions on a person.
 */
public class PeopleDataList implements Serializable {

  /**
   * The identifier used to serialize instances of class <code>PeopleDataList</code>.
   */
  private static final long serialVersionUID = 3098066693138104289L;

  /**
   * A representation of a student or staff member from a school. Information about a
   * <code>Person</code> is extracted from an input file, and <code>Person</code>
   * stores that data exactly as it comes from the input.
   * 
   * <p>A <code>Person</code> can either represent a student or a staff member.
   * The two representations differ only because their information is presented
   * differently in the input file. Staff members will have their <code>grade</code>
   * and <code>homeform</code> attributes set to Staff, and their first name(s)
   * will be left as initials.
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
     * The first name of this <code>Person</code>, or first initial if this <code>
     * Person</code> represents a staff member.
     */
    private String first;
    
    /**
     * The last name of this <code>Person</code>.
     */
    private String last;
    
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
      // Separate datapoints around tab separators.
      String[] data = rawData.split("\\t+");
      first = data[5];
      last  = data[4];
      grade = data[3];
      homeform = data[6];
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
     * <p>Middle names are included in the appropriate order. First, middle, and
     * last names are not distinguished.
     * 
     * @return The person's full name.
     */
    private String getName() {
      return first + " " + last;
    }
  }

  /**
   * A comparator unit for comparing two <code>Person</code> objects.
   * 
   * <p>Comparisons are made in the following way: relative size is determined
   * primarily by grade, next by last name should the two people have the same
   * grade, next by first name should the people have the same grade and last
   * name, and finally index number should the people share all of the above.
   * <br>Grade, first name, and last name are all strings, and their relative
   * sizes are determined by their lexicographic ordering. Index numbers are
   * integers, and their ordering is determined by their magnitude. In all cases,
   * if one <code>Person</code> is found to have a larger value to the attribute
   * being compared, it is considered to be the larger instance.
   * 
   * <p>Currently this comparator does not support any null values, and so
   * both the <code>Person</code> instances and all their String members must be
   * non-null.
   */
  private class PersonComparator implements Comparator<Person> {

	@Override
	public int compare(Person arg0, Person arg1) {
      int firstNameCompare = arg0.first.compareTo(arg1.first);
      int lastNameCompare  = arg0.last.compareTo(arg1.last);
      int gradeCompare     = arg0.grade.compareTo(arg1.grade);
      int indexCompare     = arg0.indexNum - arg1.indexNum;
      
      if (gradeCompare != 0) {
        // Grades are different, so grade is the determining factor.
        return gradeCompare;
      } else if (lastNameCompare != 0) {
        // Last names are the determining factor, since grades are the same.
        return lastNameCompare;
      } else if (firstNameCompare != 0) {
        // First names are the determining factor, since grades and last names
        // are the same.
        return firstNameCompare;
      } else {
        // Grades, first and last names are the same, so index is used as
        // determining factor. If the indices are equal, the instances are
        // considered equal.
        return indexCompare;
      }
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
   * <p>Input data must contain at least 7 tab-separated values, where the 4th value is the
   * person's grade, the 5th value is the person's last name, the 6th value is the
   * person's first name, and the 7th value is the person's homeform room (or Staff).
   * <br>Any other value in the line can be any length and content.
   * <br>There can be leading or trailing tabs.
   * 
   * @param rawInput
   *        The contents of the input file this <code>PeopleDataList</code> will hold data from.
   */
  @SuppressWarnings("unchecked")
  public List<String>[] loadFileData(String rawInput) {
    // Set up data structures to store all homeforms received in file data.
    // The first four each hold single-grade classes, which must be formatted
    // as the grade number followed by a letter. Split classes, badly-formatted
    // homeforms, and unknown homeforms are all added to the fifth set.
    TreeSet<String> gr9Homeforms = new TreeSet<>();
    TreeSet<String> gr10Homeforms = new TreeSet<>();
    TreeSet<String> gr11Homeforms = new TreeSet<>();
    TreeSet<String> gr12Homeforms = new TreeSet<>();
    TreeSet<String> otherHomeforms = new TreeSet<>();
    
    // Separate file data into its lines, based on newline separators.
    String[] lineDividedInput = rawInput.split("\\n");
    for (int indexPos = 0; indexPos < lineDividedInput.length; indexPos++) {
      String line = lineDividedInput[indexPos];
      // Add the new line to the index, parsing it along the way. Retrieve the
      // person's homeform to ensure it is tracked in the homeform sets.
      String newHomeform = add(line, indexPos + 1);
      
      TreeSet<String> dest;
      // Determine from the contents of the homeform which set it should belong to.

      if (newHomeform.indexOf('/') == -1) {
        // Homeform is not a split class.
        if (newHomeform.charAt(0) == '9') {
          // Homeform is a misformatted grade 9 class, but still a grade 9 class.
          dest = gr9Homeforms;
        } else {
          // Find out the person's grade number from the first two characters in
          // the homeform.
          switch (newHomeform.substring(0, 2)) {
            case "09":
              dest = gr9Homeforms;
              break;
            case "10":
              dest = gr10Homeforms;
              break;
            case "11":
              dest = gr11Homeforms;
              break;
            case "12":
              dest = gr12Homeforms;
              break;
            default:
              // Class is likely a badly-formatted homeform, an unknown homeform,
              // or "Staff". All are sorted into the non-numeric homeform designation.
              dest = otherHomeforms;
          }
        }
      } else {
        // Homeform is a split class, and is sorted into the appropriate grade later.
        dest = otherHomeforms;
      }
      
      if (!dest.contains(newHomeform)) {
        // The homeform has not been already added to its set: Add it in now.
        dest.add(newHomeform);
      }
    }
    // Sort the split class homeforms into the appropriate grade, and return.
    return sortHomeforms(new SortedSet[] {gr9Homeforms, gr10Homeforms, gr11Homeforms, gr12Homeforms}, otherHomeforms);
  }

  /**
   * Rearrange the homeforms from input to satisfy the following specifications:
   * 
   * <p>The output consists of one list of homeforms for each grade, and one additional
   * list for non-numeric or Staff designations. Within a grade, single-grade classes
   * are arranged in ascending order, and split classes are also arranged in ascending
   * order. However, split classes are located in the list corresponding to the last
   * grade reported in their titles. For instance, homeform 11/12G would be sorted into
   * the list for grade 10, and would occur after 11/12F and before 11/12H. All 11/12
   * split classes will be found after all pure 12 classes, meaning the lists are not
   * uniformly ordered the whole way through.
   * 
   * <p>Any homeforms that do not meet the standard form of homeforms, one or more
   * slash-separated digits followed by a character, occupy the final list in the
   * array. This list is entirely sorted in ascending order.
   * 
   * @param singleGradeHomeforms
   *        An array of sets, where each set contains all single-grade homeforms
   *        for a certain grade. The first index should be grade 9, the second grade
   *        10, the third grade 11, and the fourth grade 12.
   * @param multiGradeHomeforms
   *        A single set containing all split homeforms, non-numeric homeforms, and
   *        Staff.
   * @return The same homeforms sorted for graphical display, with grade 9 homeforms
   *         at index 0, grade 10 and 9/10 split at index 1, grade 11 and 10/11 split
   *         at index 2, etc. The last index contains any homeforms that do not meet
   *         the standard structure as specified above in the documentation.
   */
  private List<String>[] sortHomeforms(SortedSet<String>[] singleGradeHomeforms,
		                               SortedSet<String> multiGradeHomeforms) {
    @SuppressWarnings("unchecked")
	ArrayList<String>[] homeforms = new ArrayList[5];

    // Directly transfer all non-split classes into their respective lists, since
    // they are already sorted by the SortedSet input.
    for (int i = 0; i < homeforms.length - 1; i++) {
      homeforms[i] = new ArrayList<String>();
      for (String homeform : singleGradeHomeforms[i]) {
        homeforms[i].add(homeform);
      }
    }
    // The last index is reserved for Staff and non-numeric homeforms.
    homeforms[homeforms.length - 1] = new ArrayList<String>();
    
    // Sort all split classes and non-numeric homeforms into the proper locations.
    // Since they come from a SortedSet, all split classes and non-numeric homeforms
    // will be processed in ascending order and therefore appear in the output
    // in ascending order.
    for (String splitHomeform : multiGradeHomeforms) {
      // Placement is determined by everything after the last / character in the
      // homeform for split classes. If there is no / character, the homeform
      // is not a split class and is separated into the non-numeric list.
      switch (splitHomeform.substring(splitHomeform.lastIndexOf('/') + 1, splitHomeform.length() - 1)) {
        case "09":
          homeforms[0].add(splitHomeform);
          break;
        case "10":
          homeforms[1].add(splitHomeform);
          break;
        case "11":
          homeforms[2].add(splitHomeform);
          break;
        case "12":
          homeforms[3].add(splitHomeform);
          break;
        default:
          // Homeform is Staff or non-numeric, and is put into the list for those
          // designations.	
          homeforms[4].add(splitHomeform);
      }
    }

    return homeforms;
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
  private String add(String data, int ind) {
    // Person constructor parses the input line, so no parsing is done
    // in the outer scope.
    Person newPerson = new Person(data, ind);
    mugsIndex.put(newPerson.getName(), newPerson);
    return newPerson.getHomeform();
  }

  /**
   * Look up a person's name in the stored index, confirming that the person's name
   * is present there.
   * <br>If the name is found, its location in the index file will be returned.
   * If the name is not found, 0 will be returned, indicating that the name was
   * incorrectly spelled or the person was not present in the stored index.
   * Note that the first line in the index is considered to be line 1, and so an
   * output of 0 happens only for unfound names.
   * 
   * @param name
   *        The name of the person being spell-checked
   * @return The input file line number the person's data came from, or 0 if the person
   *         was not found in the index
   */
  public int searchName(String name) {
    Person nameMatch = mugsIndex.get(name);
    if (nameMatch == null) {
      // Name was not found in the index.
      return 0;
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
      // Name was not found in the index: report null.
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
      // Name was not found in the index: report null.
      return null;
    } else {
      return nameMatch.getHomeform();
    }
  }
  
  /**
   * Returns the names of all people in the index in an array, sorted according
   * to the ordering specified by the local <code>PersonComparator</code> class.
   * <br>The ordering is defined as follows:
   * 
   * <p>Two <code>Person</code> objects are compared based first on their grade,
   * then on their last name, then on their first name, then on their index number.
   * Each of the attributes listed above will be compared in that order until one is
   * found for which the two instances have different values. Strings are compared
   * lexicographically. Integers are compared by value. Whichever <code>Person</code>
   * instance has a greater value in the attribute under comparison will be considered
   * the 'larger' instance.
   * 
   * <p>Under the specified ordering, output will be structured such that all grade 9's
   * come before all grade 10's, who come before all grade 11's, etc. Staff come last.
   * Within each grade, names are listed in alphabetical order or last name. People with
   * the same last name are arranged in alphabetical order of first name.
   * 
   * @return An ordered array of all the names in this index.
   */
  public String[] getOrderedContents() {
    // Retrieve all Person objects from the index, and reorder them based on the
    // specifications of PersonComparator.
    Person[] people = mugsIndex.values().toArray(new Person[0]);
    PersonComparator orderer = new PersonComparator();
    Arrays.sort(people, orderer);

    // Transfer only the people's names, as opposed to whole Person objects, into
    // a new array, maintaining the same ordering.
    // The names alone are not enough to do the ordering, because grade and index
    // number are required alongside first and last name.
    String[] names = new String[people.length];
    for (int i = 0; i < people.length; i++) {
      names[i] = people[i].getName();
    }

    return names;
  }
}