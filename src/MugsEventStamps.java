/**
 * A collection of stamps used to communicate some type of operation that should be executed
 * in the mugs reader program.
 */
public interface MugsEventStamps {
  /**
   * A stamp signifying a request to spellcheck a set of names.
   */
  final int spellcheckStamp = 100;

  /**
   * A stamp signifying a request to search for the grades of a set of names.
   */
  final int gradeLookupStamp = 200;

  /**
   * A stamp signifying a request to search for the homeforms of a set of names.
   */
  final int homeformLookupStamp = 300;

  /**
   * A stamp signifying that an index file should be set to manual priority.
   */
  final int setManualPriority = 500;

  /**
   * A stamp signifying that an index file should be set to automatic ordering.
   */
  final int setAutomaticPriority = 1000;
}
