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
  private int requestType;

  /**
   * The object of this event.
   */
  private String requestData;

  /**
   * Initialize a new <code>RequestEvent</code>, representing an event of a type specified
   * by <code>type</code> that is to be applied to the object specified by <code>data</code>.
   * 
   * @param type
   *        A signifier of the type of event in progress
   * @param data
   *        The object of the event
   */
  public RequestEvent(int type, String data) {
    requestType = type;
    requestData = data;
  }

  /**
   * Returns the type stamp applied to this request.
   * 
   * @return The request's stamp
   */
  public int getType() {
    return requestType;
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