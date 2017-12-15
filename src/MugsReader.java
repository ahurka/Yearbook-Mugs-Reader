import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * A graphical interface-based program built around reading and interpreting mugs index
 * files for the BCI yearbook. Provides spellcheck functions both for single names and
 * lists of names, where the lists of names can copied directly from mugs pages,
 * clubs/sports membership lists, or manually written with separating commas or newlines.
 * Also supports functions to search the grades and homeforms of single or multiple people,
 * with the same input specifications as above.
 * 
 * <p>Each reader operates on a single index at a time, although it may have multiple
 * index files accessible at a time. The index file being used can be changed during
 * program runtime. All index files must be in the src folder to be used.
 * 
 * <p>The main function of the program, the spellcheck function, compares input against
 * names from the input file in use. Misspelled names are detected when a name in a user
 * input line does not match any names from the index. When this happens the user will
 * be notified. In cases where the index file does not agree with naming conventions
 * (a yearbook editor has chosen to spell a person's name differently from its appearance
 * in the mugs index consistently throughout the book) the difference will still be
 * registered as a spelling error. Be aware that detected spelling errors may be due
 * to intentional differences like these.
 * 
 * <p>-- NOT IMPLEMENTED --
 * <br>Multiple readers can run concurrently, using different saved index files. Multiple
 * concurrently running readers can have use the same mugs index at the same time, and each
 * stores an index preference set. When it is necessary to read from multiple index files
 * at a time, readers can be set up to run concurrently, where each is set to use a
 * different one of the desired index files. For instance, the first reader opened can
 * be set to start with the 2016 index open, while the second reader starts with the
 * 2017 index open, and so on.
 * 
 * <p>The order in which the readers are opened determines which saved index file they use,
 * and which indices they have open when the program starts. The nth reader opened will
 * use the nth saved index file; so the first reader opened on any given program run will
 * always use the same saved index file. When a reader is closed, the next reader to be
 * opened will take its place and use the same saved index file.
 * 
 * <p>For more details on how to use the interface, an instructions button is provided
 * in the graphical pane. For more details on how the system stores and uses data, see
 * documentation for custom attribute classes.
 */
public class MugsReader extends WindowAdapter implements Observer, MugsEventStamps {

  /**
   * The currently loaded mugs index file.
   */
  private IndexInterpreter index;

  /**
   * A center for file operations, including reading index files, and saving/retrieving
   * previously used index file interpreter objects.
   */
  private FileOperator fileIo;

  /**
   * The list of available (previously accessed) index files, ordered by their likeliness
   * to be used.
   */
  private IndexPriorityList savedIndices;

  /**
   * The graphical display (view) for the system. All input and output are
   * done through this frame.
   */
  private MugsReaderFrame hopeYouEnjoy;

  /**
   * Run the mugs reader program, opening a reader for each requested index file in
   * <code>args</code>. Each element of <code>args</code> should be the name of
   * a valid index file in the proper file location: each one will be opened with
   * a separate file reader. Note that only that file reader will save the specified
   * index, so if an index is set as the third element in <code>args</code>, it may not
   * be saved if the program is run later with only one of two readers. Likewise, the
   * third reader will not remember the same indices that have been used by the first.
   * 
   * <p>Enjoy!
   * 
   * @param args
   *        The names of the index files to open.
   *        Concurrent opening of multiple indices is not yet implemented. Currently only
   *        the first specified index file in the array will be opened
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    System.out.println("Running folder: " + System.getProperty("user.dir"));
    if (args.length == 0) {
      MugsReader first = new MugsReader();
    } else {
      // TODO add implementation for run with args.
      // Ask Mr. Mario about fork(?)?

      // For now only the first index in <args> is used.
      String fileSourceName = args[0];
      MugsReader first = new MugsReader(fileSourceName);
    }
  }

  /**
   * Initialize a new <code>MugsReader</code>. The index used by the reader is determined
   * by the saved index file the reader opens; whichever of the saved indices has been most
   * frequently used (or has been requested to be opened in previous runs of the program)
   * will be opened initially. If the reader has not been opened before, no index will be
   * initially loaded and the user must specify an index to load before information can be
   * requested.
   */
  public MugsReader() {
    if (recoverSavedIndices()) {
      index = savedIndices.get(0);
      String indexName = index.getSource();
      hopeYouEnjoy = new MugsReaderFrame(indexName, savedIndices.isManual(indexName));
    } else {
      index = new IndexInterpreter();
      hopeYouEnjoy = new MugsReaderFrame("", false);
    }
  
    hopeYouEnjoy.addObservers(this); 
    hopeYouEnjoy.addWindowListener(this);
    hopeYouEnjoy.setLocation(100, 100);
    hopeYouEnjoy.pack();
    hopeYouEnjoy.setVisible(true);
  }

  /**
   * Initialize a new <code>MugsReader</code>, which begins with the specified index open.
   * If the specified index has not been read before it will be set to automatic priority;
   * this is because this constructor is meant to be used to directly open index files that
   * are only expected to be used once. If the specified index is desired in future runs
   * of the program, it should be set to manual priority.
   * 
   * @param initialFileName
   *        The name of the file initially in use by this mugs reader
   */
  public MugsReader(String initialFileName) {
    if (recoverSavedIndices()) {
      index = savedIndices.get(initialFileName);
    } else {
      getNewIndex(initialFileName);
      savedIndices.add(index, false);
    }
    hopeYouEnjoy = new MugsReaderFrame(initialFileName, savedIndices.isManual(initialFileName));
    
    hopeYouEnjoy.addObservers(this); 
    hopeYouEnjoy.addWindowListener(this);
    hopeYouEnjoy.setLocation(200, 200);
    hopeYouEnjoy.pack();
    hopeYouEnjoy.setVisible(true);
  }

  /**
   * Attempt to load previously translated and saved index files from a files specific to
   * this <code>MugsReader</code>, and return whether or not the operation was successful.
   * Recovered indices are stored in a list and can be accessed one at a time. The list of
   * indices represents those that can be opened immediately without reading and parsing
   * a text file - not a set of indices currently in use.
   * 
   * @return <code>true</code> if the saved index list is nonempty after this operation
   */
  private boolean recoverSavedIndices() {
    fileIo = new FileOperator();
    savedIndices = new IndexPriorityList();
    if (fileIo.hasNextObject()) {
      try {
        savedIndices.reload(fileIo.retrieveNextObject());
        return true;
      } catch (IOException err) {
        savedIndices = new IndexPriorityList();
        return false;
      }
    } else {
      savedIndices = new IndexPriorityList();
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the specified <code>RequestEvent</code> represents
   * a request to access the stored mugs index, as opposed to operations on loaded index
   * files. Determined by the stamp loaded in the <code>RequestEvent</code>.
   * 
   * @param query
   *        The request being executed
   * @return <code>true</code> if the query is for lookup operations
   * @see MugsReaderStamps
   */
  private boolean isSearchOperation(RequestEvent query) {
    int stamp = query.getType();
    return stamp == spellcheckStamp
           || stamp == gradeLookupStamp
           || stamp == homeformLookupStamp;
  }
  
  private void getNewIndex(String fileName) {
    try {
      index = new IndexInterpreter(fileIo.readIndex(fileName), fileName);
    } catch (IOException err) {
      hopeYouEnjoy.displayErrorMessage(err.getMessage());
    }
  }

  @Override
  public void update(Observable source, Object request) {
    // Occurs when the user has 
    RequestEvent query = (RequestEvent)request;
    if (isSearchOperation(query)) {
      String[][] answer = index.execute(query);
      ((ControlPane)source).displayLines(answer);
    } else if (query.getData().equals(index.getSource())) {
      // The user wants to reconfigure the ordering of the current index in the saved list.
      if (savedIndices.isManual(index.getSource()) == (query.getType() == setManualPriority)) {
        // The index is already in the desired position. Record this index as frequently used.
        savedIndices.add(index, query.getType() == setManualPriority);
      } else {
        // The index is not in the desired position and should be moved there.
        savedIndices.changeOrdering(query.getData(), query.getType() == setManualPriority);
      }
    } else {
      // The user wants to change to a new mugs index.
      if (savedIndices.contains(query.getData())) {
        index = savedIndices.get(query.getData());
      } else {
        String name = query.getData();
        getNewIndex(name);
      }
      savedIndices.add(index, query.getType() == setManualPriority);
    }
  }
  
  @Override
  public void windowClosing(WindowEvent evt) {
    // Occurs when the main frame is closed and the program is shutting down.
    if (savedIndices.hasChanged()) {
      // The indices must be reserialized because of changes in their priority ordering.
      try {
        fileIo.saveObject(savedIndices.retrieveData());
      } catch (IOException err) {
        err.printStackTrace();
        System.err.println("Unable to preserve saved index data, "
                           + "next session will begin with no saved indices");
      }
    }
    fileIo.shutdown();
  }
}
