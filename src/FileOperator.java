import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A hub for file operations, including direct file reading, serializing, and
 * deserializing. File names are stored inside instances of this class and can
 * only be modified by changing the values of private attributes. However,
 * different files will be used  class are running concurrently.
 * 
 * <p>Functions:
 * <br>Read the contents of a file directly. The file to be read can be specified
 * by the file's name and file extension, although it must be located in the
 * running directory.
 * <br>Serialize an object into a predetermined text file location.
 * <br>Deserialize and retrieve an object that has previously been saved to a
 * predetermined text file.
 */
public class FileOperator {

  /**
   * The folder in which input/output files will be located.
   */
  private String folderPath = System.getProperty("user.dir");

  /**
   * The standard name of the output file for serialized objects. This file name
   * will have a number inserted in the middle to distinguish object files for
   * multiple concurrently running instances of <code>FileOperator</code>.
   */
  private String savedIndexFileName = "savedIndex.txt";

  /**
   * A set of numbers corresponding to unopened input files. Numbers are stored in
   * ascending order such that the first n index files are guaranteed to be opened
   * before the n+1th is opened.
   */
  private static ConcurrentSkipListSet<Integer> availableInputFiles = null;

  /**
   * The file for storing and retrieving serialized objects.
   */
  private File savedIndexFile;

  /**
   * <code>true</code> if the input stream reading serialized objects has not
   * reached the end of the stream.
   */
  private boolean containsBytes;

  /**
   * An input stream that reads bytes from the saved objects file.
   */
  private FileInputStream fileReader;

  /**
   * An input stream that reads and reconstructs objects from the saved objects file.
   */
  private ObjectInputStream indexReader;

  /**
   * An output stream that serializes and saves objects to the saved objects file.
   */
  private ObjectOutputStream indexWriter;

  /**
   * Initialize a new <code>FileOperator</code>. The file used for input and output
   * of serialized objects will be determined based on which serialized object files
   * are currently in use. If a serialized objects file does not exist yet, a new one
   * will be created, otherwise the existing one will be used.
   */
  public FileOperator() {
    int fileSourceNumber;
    if (availableInputFiles == null) {
      // This FileOperator is the first instantiated in this run of the program
      fileSourceNumber = 1;
      availableInputFiles = new ConcurrentSkipListSet<>();
      availableInputFiles.add(2);
    } else {
      // 
      fileSourceNumber = availableInputFiles.pollFirst();
      if (availableInputFiles.isEmpty()) {
        // The first <fileSourceNumber> files have been opened; the next to be opened is the n+1th.
        availableInputFiles.add(fileSourceNumber + 1);
      }
    }
    String[] fileNameComponents = savedIndexFileName.split("\\.");
    savedIndexFileName = fileNameComponents[0] + fileSourceNumber + "." + fileNameComponents[1];
    fileSourceNumber++;
    setupIo();
  }

  /**
   * Initialize input and output streams for access to the serialized objects file, and
   * perform other checks to ensure input and output can run smoothly.
   */
  private void setupIo() {
    savedIndexFile = new File(folderPath, savedIndexFileName);
    try {
      if (!savedIndexFile.exists()) {
        // The file has not been used before and must be set up.
        savedIndexFile.createNewFile();
        // Write an output stream header to prevent EOFExceptions while reading.
        indexWriter = new ObjectOutputStream(new FileOutputStream(savedIndexFile));
        indexWriter.close();
      }
      fileReader = new FileInputStream(savedIndexFile);
      indexReader = new ObjectInputStream(fileReader);
      containsBytes = fileReader.available() > 0;
    } catch (IOException err) {
      err.printStackTrace();
      containsBytes = false;
    }
  }

  /**
   * Close all input and output streams that are connected to the serialized objects
   * file, to allow the file to be freely moved, manipulated, or deleted, or to prepare
   * for program termination.
   */
  private void takeDownIo() {
    try {
      for (Closeable ioObject : new Closeable[]{fileReader, indexReader, indexWriter}) {
        if (ioObject != null) {
          ioObject.close();
        }
      }
    } catch (IOException err) {
      fileReader = null;
      indexReader = null;
      indexWriter = null;
    } finally {
      containsBytes = false;
    }
  }

  /**
   * Returns <code>true</code> if objects can still be retrieved from the serialized
   * objects file. If <code>false</code>, either the input stream for the file reached
   * the end of the file, or the file was not successfully opened.
   * 
   * @return <code>true</code> if more serialized objects can be retrieved
   */
  public boolean hasNextObject() {
    return containsBytes;
  }

  /**
   * Retrieve, deserialize, and return the next object accessed by the input stream
   * for the serialized objects file. Assumes the serialized objects file contains
   * more objects, which can be checked using the method <code>hasNextObject</code>.
   * If that method is not called beforehand, an <code>IOException</code> may be thrown.
   * 
   * @return The next available serialized object
   * @throws IOException
   *         If the serialized object file could not be accessed or no more objects
   *         were left in the file
   */
  public Object retrieveNextObject() throws IOException {
    try {
      Object index = indexReader.readObject();
      containsBytes = fileReader.available() > 0;
      return index;
    } catch (ClassNotFoundException err) {
      return null;
    }
  }

  /**
   * Serialize and save a single object to the serialized objects file. The object will
   * be appended to the end of the file; be aware that it may already be saved there.
   * Serialized objects cannot be singly removed after being added to the file; the only
   * way to remove an object from the file is to use the <code>wipeFile</code> method to
   * clear the file entirely.
   * 
   * @param obj
   *        The object to serialize
   * @throws IOException
   *         If the serialized object file could not be accessed
   */
  public void saveObject(Object obj) throws IOException {
    wipeFile();
    indexWriter = new ObjectOutputStream(new FileOutputStream(savedIndexFile));
    indexWriter.writeObject(obj);
  }

  /**
   * Clear the serialized objects file. Does not save the file's contents in any way.
   * Be aware that any objects deleted from the file cannot be retrieved except by
   * initializing new instances with the same arguments.
   * 
   * @throws IOException
   *         If the serialized objects file could not be accessed
   */
  private void wipeFile() throws IOException {
    takeDownIo();
    savedIndexFile.delete();
    savedIndexFile.createNewFile();
  }

  /**
   * Perform decoupling operations with input and output files, such as closing input and
   * output streams, to prepare for program termination.
   */
  public void shutdown() {
    takeDownIo();
  }

  /**
   * Read the contents of the specified text file into a single <code>String</code>. Line
   * breaks and formatting will be preserved. The text file will not be modified during
   * the process.
   * 
   * <p>The text file must be in the running directory to be accessed: this location is
   * likely to be in the src folder, one level up from the folder containing the .java files.
   * If no src folder exists, place input text files in the same folder as the .java files.
   * 
   * @param indexPath
   *        The name of the file to read
   * @return The contents of the specified file
   */
  public String readIndex(String indexPath) throws IOException {
    RandomAccessFile file;

    file = new RandomAccessFile(indexPath, "r");
    byte[] bytes = new byte[4096];
    String fileContents = "";

    for (int i = file.read(bytes); i > 0; i = file.read(bytes)) {
      String newData = new String(bytes);
      fileContents += newData.substring(0, i);
    }

    file.close();
    return fileContents;
  }
}
