/**
 * A collection of stamps used to communicate some type of operation that should be executed
 * in the mugs reader program.
 */
public interface MugsEventStamps {
  /**
   * A stamp signifying any kind of lookup operation. Additional parameters
   * and control must be set separately.
   */
  final byte lookupStamp = 10;

  /**
   * A stamp signifying that an index file should be set to manual priority.
   */
  final byte setManualPriority = 50;

  /**
   * A stamp signifying that an index file should be set to automatic ordering.
   */
  final byte setAutomaticPriority = 100;
}
