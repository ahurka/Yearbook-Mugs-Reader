import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A limited-access list providing storage for multiple <code>IndexInterpreter</code>
 * instances through mapping, where each index is mapped by the <code>String</code>
 * filename, equivalent to the <code>String</code> returned by calling the method
 * <code>getSource</code> on the <code>IndexInterpreter</code> in question.
 * 
 * <p>Indices are ordered based on the number of times they have been requested, and
 * can be accessed by position in this way. They can also be iterated through in the
 * order determined by their frequencies of use. Index-wise adding or setting
 * operations are not allowed, as the list determines the ordering of indices by itself.
 * 
 * <p>Methods allow a single index at a time to be set to manual priority, meaning
 * that index will be sent to the front of the list regardless of how many times
 * it has been requested, and can be guaranteed to be found at index 0. Which element
 * is set to manual priority can be adjusted using the <code>toManual</code> parameter
 * in any method that offers that parameter, and the index currently set to manual
 * priority can be checked using the method <code>isManual</code>.
 */
public class IndexPriorityList implements Iterable<IndexInterpreter> {

  /**
   * A map of String index source files to the indices drawn from those source files.
   */
  private HashMap<String, IndexInterpreter> translator;

  /**
   * A priority list that controls the order in which indices are retrieved when iterating.
   * Holds information on manual and automatic priority ordering of indices.
   */
  private StackingPriorityList<IndexInterpreter> orderer;

  /**
   * Whether information regarding the ordering of indices has changed since this
   * <code>IndexPriorityList</code> was initialized; if <code>true</code>, the saved index
   * storage file must be updated.
   */
  private boolean changed;

  /**
   * Initialize a new <code>IndexPriorityList</code>, which starts out containing no
   * indices.
   */
  public IndexPriorityList() {
    translator = new HashMap<>();
    orderer = new StackingPriorityList<>();
    changed = false;
  }

  /**
   * Initialize a new <code>IndexPriorityList</code>, which starts out containing the
   * indices in <code>start</code>, and in the same order.
   * 
   * <p>If <code>start</code> is an unordered collection such as an implementer of
   * <code>Set</code> and not an  implementer of <code>SortedSet</code>, no initial
   * order can be determined and an <code>IllegalArgumentException</code> will be thrown.
   * Otherwise the starting order of the indices in the <code>IndexPriorityList</code>
   * will be the same as it was in <code>start</code>.
   * 
   * @param start
   *        A collection of the elements to add to this <code>IndexPriorityList</code>.
   *        The items will be added in the as order as they appeared in <code>start</code>
   */
  public IndexPriorityList(Collection<IndexInterpreter> start) {
    orderer = new StackingPriorityList<>(start);
    translator = new HashMap<>();
    // Ensure each index has its source file recorded in the map.
    for (IndexInterpreter item : start) {
      translator.put(item.getSource(), item);
    }
  }

  /**
   * Returns <code>true</code> if the list contains the index specified by the given
   * source file name.
   * 
   * @param indexHeader
   *        The name of the source file for the index whose presence is being tested
   * @return <code>true</code> if the index is present in the list
   */
  public boolean contains(String indexHeader) {
    return translator.containsKey(indexHeader);
  }

  /**
   * Returns <code>true</code> if the index specified by the given source file name
   * is currently set to manual priority ordering.
   * 
   * @param indexHeader
   *        The name of the source file for the index whose position is being tested
   * @return <code>true</code> if the index is set to manual priority
   */
  public boolean isManual(String indexHeader) {
    return orderer.containsInManual(translator.get(indexHeader));
  }

  /**
   * Returns the index to which the specified source file name is mapped, or
   * <code>null</code> if the list does not contain an index from that source file.
   * 
   * @param indexHeader
   *        The name of the source file from which the desired index was drawn
   * @return The index built from the given source file
   */
  public IndexInterpreter get(String indexHeader) {
    return translator.get(indexHeader);
  }

  /**
   * Returns the index at the specified position in the list. An index's position
   * is determined by how many times it has been requested relative to other indices.
   * That is, lower values of <code>pos</code> will give more popular indices.
   * 
   * <p>If an index is set to manual priority, it will be at index 0.
   * 
   * @param pos
   *        The position of the element to return
   * @return The element at the specified position
   */
  public IndexInterpreter get(int pos) {
    return orderer.get(pos);
  }

  /**
   * Ensures the specified index is contained in the list, and that it is set to
   * the ordering setting specified by <code>toManual</code>. If <code>toManual</code>
   * is <code>true</code>, the index will be set to manual priority, else it will be
   * ordered based on the frequency of its being requested.
   * 
   * <p>If the index is not already contained in the list, it will be inserted into
   * the list at the appropriate position. Otherwise its frequency of loading will be
   * increased and it will be repositioned if necessary.
   * 
   * <p>Other resulting changes in the list may occur, for example moving the current
   * manual priority index to automatic ordering when this index is being added to
   * manual priority, or the rearranging of any indices due to changes in request
   * frequencies.
   * 
   * @param newIndex
   *        Index whose presence in the appropriate location of the list is to be ensured
   * @param toManual
   *        Whether or not the index is to be set to manual priority
   */
  public void add(IndexInterpreter newIndex, boolean toManual) {
    if (toManual) {
      orderer.addToManual(newIndex);
    } else {
      orderer.addToAutomatic(newIndex);
    }
    changed = true;
    if (!translator.containsKey(newIndex.getSource())) {
      // Index is new; the map must be updated.
      translator.put(newIndex.getSource(), newIndex);
    }
  }

  /**
   * Ensure the index specified by the given source file name is set to the desired
   * ordering setting without changing its frequency of being requested. The
   * desired ordering setting is specified by the value of <code>toManual</code>,
   * where a <code>true</code> value of that variable indicates the index is to
   * be moved to manual priority, and a value of <code>false</code> indicates
   * it should be moved to automatic ordering.
   * 
   * @param indexHeader
   *        The index whose ordering setting is being ensured
   * @param toManual
   *        <code>true</code> if the index is to be set to manual priority
   */
  public void changeOrdering(String indexHeader, boolean toManual) {
    IndexInterpreter thisIndex = translator.get(indexHeader);
    if (toManual && !(orderer.containsInManual(thisIndex))) {
      orderer.relocateAutomaticToManual(thisIndex);
      changed = true;
    } else if (orderer.containsInManual(thisIndex) && orderer.containsInManual(thisIndex)) {
      orderer.relocateManualToAutomatic();
      changed = true;
    }
  }

  /**
   * Returns <code>true</code> if the specified object is an instance of
   * <code>StackingPriorityList</code> parameterized to store instances of
   * <code>IndexInterpreter</code>. This indicates that <code>obj</code> is of
   * the appropriate type to replace this class's <code>orderer</code> attribute.
   * 
   * @param obj
   *        The object whose class and parameterization is being tested
   * @return <code>true</code> if the object is appropriately typed to replace
   *         the orderer list
   */
  private boolean assertCorrectParameters(Collection<?> obj) {
    boolean validParameter = true;
    Iterator<?> iter = ((StackingPriorityList<?>)obj).iterator();
    // Ensure that every object in the list is the right type.
    for (; iter.hasNext() && validParameter;) {
      validParameter = iter.next() instanceof IndexInterpreter;
    }
    return validParameter;
  }

  /**
   * Returns the ordering object. Should be orimarily used to serialize the orderer
   * to be able to load it into another <code>IndexPriorityList</code> later using
   * the <code>reload</code> method.
   * 
   * @return The index orderer
   */
  public Object retrieveData() {
    return orderer;
  }

  /**
   * Load a new set of indices into this <code>IndexPriorityList</code>. The indices
   * will retain the same order in the list as they took in the original collection,
   * and all will be set to automatic ordering. For this to occur the argument must be
   * a sorted implementation of <code>Collection</code>; that is, it must not be an
   * instance of <code>Set</code> and not an instance of <code>SortedSet</code>.
   * 
   * <p>Unlike normal adding operations, a collection is allowed to have duplicate
   * elements. The number of occurrences of each index in the collection will be
   * equal to the request frequency of that index in the list. Null elements are
   * still not allowed in the collection, however.
   * 
   * <p>Should be primarily used to reload an ordering list that was removed from a
   * <code>IndexPriorityList</code> using the method <code>retrieveData</code>.
   * 
   * @param newOrderer
   *        The ordered list of indices to be loaded
   */
  @SuppressWarnings("unchecked")
  public void reload(Object newOrderer) {
    if (newOrderer instanceof Collection<?> && assertCorrectParameters((Collection<?>)newOrderer)) {
      if (newOrderer instanceof StackingPriorityList<?>) {
        // newOrderer is of the right type to simply replace the orderer
        orderer = (StackingPriorityList<IndexInterpreter>)newOrderer;
      } else {
        // Transfer the indices in newOrderer into the orderer
        orderer = new StackingPriorityList<IndexInterpreter>((Collection<IndexInterpreter>)
                                                              newOrderer);
      }
      // Reset the translator to reflect the new orderer.
      translator.clear();
      for (IndexInterpreter ind : orderer) {
        translator.put(ind.getSource(), ind);
      }
    } else {
      throw new IllegalArgumentException("Only instances of Collection<IndexInterpreter> "
                                         + "can be reloaded");
    }
  }

  /**
   * Returns <code>true</code> if any significant changes have occurred that could
   * affect ordering of indices, including order changes and changes in request
   * frequencies of indices that contribute to future order changes.
   * 
   * <p>The orderer having changed signifies that the new order data should be used to
   * update the saved index file. It can be retrieved and serialized using the
   * <code>retrieveData</code> method.
   * 
   * @return <code>true</code> if any order factors in the orderer have changed
   */
  public boolean hasChanged() {
    return changed;
  }

  @Override
  public ListIterator<IndexInterpreter> iterator() {
    return orderer.iterator();
  }
}
