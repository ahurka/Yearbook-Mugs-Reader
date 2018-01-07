
/**
 * An event in progress. Usually represents a user request to access/change the model of
 * an application.
 * 
 * <p>The purpose of the event is denoted by a 'stamp' stored inside it. To be a valid stamp,
 * a value must be recognized by both the sender and the receiver, so that it can be
 * interpreted correctly. The values used should be constants known to both classes, such
 * as public constants/protected constants in a class hierarchy, etc. Requests also carry
 * information about an object that the event is to be enacted on. Both the stamp and the
 * object can be retrieved with getter methods.
 */
public class RequestEvent {

  /**
   * A stamp applied to this request for its purpose to be identified by receivers.
   */
  private byte requestType;

  /**
   * A set of additional parameter flags, the purpose of which depends on the operation.
   * An operation may choose to not use the flags, in which case it should set them to
   * null or an array of length zero.
   * 
   * <p>Search operations use these flags to communicate which pieces of output are
   * retained and which are lost. File manipulation operations currently do not use
   * any additional parameters, although that may change.
   */
  private boolean[] requestParams;
  
  /**
   * The object of this event, be it the name of a file or a number of people's names.
   */
  private String requestData;

  /**
   * Initialize a new <code>RequestEvent</code>, representing an event of a type specified
   * by <code>type</code> that is to be applied to the object specified by <code>data</code>,
   * with additional event parameters according to <code>parameters</code>.
   * 
   * @param type
   *        A signifier of the type of event in progress
   * @param parameters
   *        A series of boolean flags that are used as additional settings in a request
   * @param data
   *        The object of the event
   */
  public RequestEvent(byte type, boolean[] parameters, String data) {
    requestType = type;
    requestParams = parameters;
    requestData = data;
    
  }
  
  public byte getType() {
    return requestType;
  }
  
  public boolean[] getParams() {
    return requestParams;
  }

  /**
   * Returns the object on which this event is to be performed.
   * 
   * @return The request's object
   */
  public String getData() {
    return requestData;
  }
}