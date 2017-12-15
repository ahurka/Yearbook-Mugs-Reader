import java.io.Serializable;
import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import java.util.Set;
import java.util.SortedSet;

/*
 * Methods to add:
 * reduceCount(T, int)
 * reduceCount(Collection<T>, int)
 */

/**
 * A data structure consisting of a sorted list of objects, retrievable in order as in a List.
 * Despite the fact that this class implements <code>Collection</code>, it supports several
 * List methods such as <code>get(int)</code> and <code>indexOf(E)</code>. However, not all
 * List methods are supported.
 * 
 * <p>The major difference between a <code>StackingPriorityList</code> and an ordinary List
 * is that <code>StackingPriorityList</code> does not permit multiple copies of the same
 * element. Instead, if an element is added which is already present in the list, its count
 * will increase; this represents the number of times the element has been added to the list.
 * 
 * <p><code>StackingPriorityList</code> works like a priority queue in that it manages the
 * order of its contents. The location of an object in a <code>StackingPriorityList</code>
 * is determined by its count. For example, if the elements "1", "2", "2" are added to a
 * <code>StackingPriorityList</code> of Strings, the element "2" would take the first index
 * because its count is greater: it has been added to the list more times than "1" has.
 * In the case of a tie, the element most recently inserted is closer to the front of the list.
 * 
 * <p><code>StackingPriorityList</code> also supports the option to set an element to manual
 * priority, which moves it to the front of the list regardless of its count. An object on
 * manual priority can be restored to automatic ordering, at which point it will once again
 * take a position in the list based on its count. This means that elements with low count
 * can be sent temporarily to the front of the list when they are expected to be used often,
 * and restored to their position at the back when they are no longer needed. Elements that are
 * manually ordered continue to keep track of further additions, which may be reflected when
 * they are returned to automatic ordering.
 * 
 * @param <E>
 *        The type of object stored in this <code>StackingPriorityList</code>
 */
public class StackingPriorityList<E extends Serializable> implements Collection<E>, Serializable {

  /**
   * The identifier used to serialize instances of class <code>StackingPriorityList</code>.
   */
  private static final long serialVersionUID = 8117386567813103635L;

  /**
   * A package containing supplemental information about a single element in a
   * <code>StackingPriorityList</code>, acting as a layer for <code>StackingPriorityList</code>
   * to store its information about an element it contains.
   * 
   * <p>Holds the count of the element (the number of times the element has been added
   * to its list) as well as the element itself.
   * 
   * <p>This class is not meant to be used by any class outside of
   * <code>StackingPriorityList</code>, but it can be cast to <code>Object</code> and
   * moved between <code>StackingPriorityLists</code> or serialized. See methods
   * <code>load</code> and <code>retrieve</code>.
   * 
   * @param <I>
   *        The type of object stored in this <code>StorageNode</code>, corresponding with
   *        the type of object stored in the <code>StackingPriorityList</code> to which this
   *        node belongs/originated from.
   */
  public class StorageNode<I> implements Serializable {

    /**
     * The identifier used to serialize instances of class <code>StorageNode</code>.
     */
    private static final long serialVersionUID = -7526844753486155067L;

    /**
     * The element stored in this node.
     */
    private I data;

    /**
     * The number of times this node's element has been added to the list.
     */
    private int count;

    /**
     * Initialize a new <code>StorageNode</code>, containing the element specified in
     * <code>elem</code>. All new <code>StorageNodes</code> start with a count of 1.
     * 
     * @param elem
     *        The element stored in this node.
     */
    private StorageNode(I elem) {
      data = elem;
      count = 1;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof StackingPriorityList<?>.StorageNode<?>) {
        return ((StackingPriorityList<?>.StorageNode<?>)other).data.equals(this.data);
      } else {
        return other != null && other.equals(this.data);
      }
    }
  }

  /**
   * An iterator over a <code>StackingPriorityList</code>. Implemented as a
   * <code>ListIterator</code>, allowing for iteration in both directions; not all methods
   * in <code>ListIterator</code> have been implemented. Particularly, <code>add</code>
   * and <code>set</code> are not implemented because index-wise arrangement of elements
   * in the list is not allowed.
   * 
   * <p>These iterators consider the list's manual priority element to be the item at index
   * 0, so these will always be iterated over first if present. If the list has no manual
   * element, the item at index 0 will be the first priority element in the automatic
   * priority section of the list.
   * 
   * <p>Calling method <code>remove</code> will remove the last returned element only if
   * said element had a count of 1 in the list; otherwise, its count will be reduced by one.
   * That is, removing an element will always undo whatever change was made by the last
   * addition of that element.
   */
  private class StackPriorityIterator implements ListIterator<E> {

    /**
     * The index of the next item in the list to be returned by method <code>next</code>.
     * Used as a tracker of the <code>StackPriorityIterator</code>'s position in the list.
     */
    private int pointerIndex;
 
    /**
     * A representation of direction the iterator moved in last; indicative of whether
     * <code>next</code> or <code>previous</code> was the last iterating method called.
     * 
     * <p>Takes the value 1 if <code>next</code> was most recently called, -1 if
     * <code>previous</code> was most recently called, and 0 if neither method has been
     * called at all or since the last call to any modification method.
     */
    private int previousMove;

    /**
     * An iterator through the automatically ordered section of the list.
     */
    private ListIterator<StorageNode<E>> autoOrderedIndexIterator;

    /**
     * Initialize a new <code>StackingPriorityIterator</code> at index 0.
     */
    private StackPriorityIterator() {
      pointerIndex = 0;
      previousMove = 0;
      autoOrderedIndexIterator = prioritizedList.listIterator();
    }
    
    @Override
    public boolean hasNext() {
      return pointerIndex < size;
    }
    
    @Override
    public boolean hasPrevious() {
      return pointerIndex > 0;
    }
    
    @Override
    public int nextIndex() {
      return pointerIndex;
    }
    
    @Override
    public int previousIndex() {
      return pointerIndex - 1;
    }
    
    @Override
    public E next() {
      previousMove = 1;
      if (!hasNext()) {
        throw new NoSuchElementException("The end of the list has been reached");
      } else if (pointerIndex++ == 0 && manualPrioritySeat != null) {
        // The iterator's pointer is at the manual priority element.
        return manualPrioritySeat.data;
      } else {
        return autoOrderedIndexIterator.next().data;
      }
    }
    
    @Override
    public E previous() {
      previousMove = -1;
      if (!hasPrevious()) {
        throw new NoSuchElementException("The end of the list has been reached");
      } else if (--pointerIndex == 0 && manualPrioritySeat != null) {
        // The iterator's pointer is at the manual priority element.
        return manualPrioritySeat.data;
      } else {
        return autoOrderedIndexIterator.previous().data;
      }
    }
    
    @Override
    public void remove() {
      if (previousMove == 0) {
        throw new IllegalStateException("List modification through iterator cannot be performed "
                                        + "due to consecutive modification operations");
      } else {
        size--;
        int manualIndex = 0;
        if (previousMove == 1) {
          // Adjust for the pointer being ahead of the last returned element's position.
          pointerIndex--;
        }  
        
        if (pointerIndex == manualIndex) {
          manualPrioritySeat = null;
        } else {
          autoOrderedIndexIterator.remove();
        }
      }
    }

    @Override
    public void add(E arg0) {
      throw new UnsupportedOperationException("StackingPriorityList does not allow "
                                               + "index-specific operations");
    }

    @Override
    public void set(E elem) {
      throw new UnsupportedOperationException("StackingPriorityList does not allow "
                                              + "index-specific operations");
    }
  }

  /**
   * The element of the list which has been set to manual priority, or <code>null</code>
   * if no element has been set to manual priority.
   * 
   * <p>This element is always considered to be at index 0 in the list, regardless of its count.
   * See class documentation for more details.
   */
  private StorageNode<E> manualPrioritySeat;

  /**
   * The container for all automatically ordered elements in the list. Elements are ordered
   * based on how many times they have been added to the list. See class documentation for
   * more details.
   */
  private ArrayList<StorageNode<E>> prioritizedList;

  /**
   * The total number of elements in the list. An element with a count greater than one is not
   * considered to constitute multiple elements. Therefore a <code>StackingPriorityList</code>'s
   * size will not necessarily be the same as the number of times the method <code>add</code>
   * has been called.
   */
  private int size;

  /**
   * Initialize a new <code>StackingPriorityList</code>, which starts out containing no items.
   */
  public StackingPriorityList() {
    manualPrioritySeat = null;
    prioritizedList = new ArrayList<>();
    size = 0;
  }

  /**
   * Initialize a new <code>StackingPriorityList</code>, which starts out with the same items
   * as the specified <code>Collection</code> in the same order as they are retrieved. All
   * items will be set to automatic ordering, and since each will have the same count of 1,
   * the ordering of the elements in the <code>StackingPriorityList</code> will initially
   * be the same as it was in the specified <code>Collection</code>.
   * 
   * <p>If the <code>Collection</code> does not have a specified order, namely by implementing 
   * <code>Set</code> and not implementing <code>SortedSet</code>, an
   * <code>IllegalArgumentException</code> will be thrown.
   * 
   * @param firstItems
   *        A collection of the elements to add to this <code>StackingPriorityList</code>.
   *        The items will be added in the as order as they appeared in <code>firstItems</code>
   */
  public StackingPriorityList(Collection<E> firstItems) {
    this();
    addAll(firstItems);
  }
  
  @Override
  public int size() {
    return size;
  }
  
  @Override
  public boolean isEmpty() {
    return size == 0;
  }
  
  @Override
  public void clear() {
    prioritizedList.clear();
    manualPrioritySeat = null;
    size = 0;
  }
  
  @Override
  public boolean contains(Object obj) {
    return containsInAutomatic(obj) || containsInManual(obj);
  }

  /**
   * Returns <code>true</code> if this <code>StackingPriorityList</code> contains the specified
   * element, and the element is set to automatic ordering.
   * 
   * @param obj
   *        The element whose presence in the list is to be tested
   * @return <code>true</code> if the list contains the specified element in automatic ordering
   * @throws NullPointerException
   *         If the specified element is null
   */
  public boolean containsInAutomatic(Object obj) {
    if (obj != null) {
      boolean found = false;
      for (int i = 0; i < prioritizedList.size() && !found; i++) {
        StorageNode<E> elem = prioritizedList.get(i);
        /* Returns true if obj is a StorageNode containing the same element as elem,
           or if obj is equal to the element contained in elem. */
        found = elem.equals(obj);
      }
      return found;
    } else {
      throw new NullPointerException("StackingPriorityList does not permit null elements");
    }
  }

  /**
   * Returns <code>true</code> if this <code>StackingPriorityList</code> contains the specified
   * element, and the element is set to manual priority.
   * 
   * @param obj
   *        The element whose presence in the list is to be tested
   * @return <code>true</code> if the list contains the specified element set to manual priority
   * @throws NullPointerException
   *         If the specified element is null
   */
  public boolean containsInManual(Object obj) {
    if (obj != null) {
      /* Returns true if obj is a StorageNode containing the same element as the manual node,
         or if obj is equal to the element contained in the manual node. */
      return manualPrioritySeat != null && manualPrioritySeat.equals(obj);
    } else {
      throw new NullPointerException("StackingPriorityList does not permit null elements");
    }
  }
  
  @Override
  public boolean containsAll(Collection<?> lst) {
    boolean allFound = true;
    Iterator<?> lstIterator = lst.iterator();
    while (lstIterator.hasNext() && allFound) {
      allFound = this.contains(lstIterator.next());
    }
    return allFound;
  }

  /**
   * Returns the index of the specified element in this automatically ordered section of this list,
   * or -1 if this list does not contain the element.
   * 
   * <p>If no element is set to manual priority, the return will be the same as that of
   * <code>indexOf(item)</code>. If an element is set to manual priority, the return will be 
   * equal to <code>(indexOf(item) - 1)</code> if the element is set to automatic ordering.
   * 
   * @param elem
   *        The element to search for
   * @return The index of the specified element in the automatically ordered section of the list,
   *         or -1 if the element is not set to automatic ordering
   * @throws NullPointerException
   *         If the specified element is null
   * @see indexOf(Object)
   */
  private int indexInAutomaticList(Object elem) {
    if (elem == null) {
      throw new NullPointerException("StackingPriorityList does not permit null elements");
    } else if (this.containsInAutomatic(elem)) {
      // Wrap in a StorageNode to take advantage of StorageNode.equals flexibility.
      return prioritizedList.indexOf(new StorageNode<Object>(elem));
    } else {
      return -1;
    }
  }

  /**
   * Returns the index of the specified element in this list, or -1 if this list does not contain
   * the element.
   *
   * @param elem
   *        The element to search for
   * @return The index of the specified element in the list, or -1 if the list does not contain
   *         that element.
   * @throws NullPointerException
   *         If the specified element is null
   */
  public int indexOf(Object elem) {
    if (this.containsInManual(elem)) {
      return 0;
    } else if (this.containsInAutomatic(elem)) {
      int start = manualPrioritySeat == null ? 0 : 1;
      return start + indexInAutomaticList(elem);
    } else {
      return -1;
    }
  }

  /**
   * Returns the index in the automatically ordered section of the list at which an element
   * with a count equal to <code>value</code> should go, if that element was set to automatic
   * ordering. More formally, returns an index <code>fL</code> such that for any element
   * <code>elem</code> in the list, where <code>elem</code> is set to automatic ordering:
   * 
   * <p>If <code>(indexInAutomaticList(elem) < fL)</code>, then <code>elem.count > value</code>.
   * <br>If <code>(indexInAutomaticList(elem) >= fL)</code>, then <code>elem.count <= value</code>.
   * 
   * <p>In general, the return will be equal to one more than the index of the last element in the
   * list with count greater than <code>value</code>, or 0 if no element exists in the list with
   * count greater than <code>value</code>.
   * 
   * @param value
   *        The element count being searched for
   * @return The index at which a node with count <code>value</code> should go
   */
  private int findLocation(int value) {
    int beg = 0;
    int end = prioritizedList.size() - 1;
    
    // Binary search.
    while (beg < end) {
      int middle = (beg + end) / 2;
      if (value < prioritizedList.get(middle).count) {
        beg = middle + 1;
      } else {
        end = middle;
      }
    }
    
    return beg;
  }

  /**
   * Inserts the specified element into this list, using automatic ordering. Shifts the
   * element currently at that position (if any) and any subsequent elements to the right
   * (adds one to their indices).
   * 
   * <p>Elements are positioned in the list based on their counts, which represent the
   * number of times the elements have been added to the list. Elements with higher counts
   * take positions closer to the front of the list. Since this is an add operation, count
   * is increased by 1 if the element is already present in the list.
   * 
   * <p>Relocation will be done if necessary, moving an element forward in the list if its
   * count increases sufficiently, or if it was set to manual priority beforehand, relocating
   * it to automatic ordering as well as increasing its count. 
   * 
   * @param elem
   *        The element being added or having its count increased
   * @return <code>true</code> if order of elements in the list changed as a result of the 
   *         operation
   */
  private boolean addPriorityOrdered(E elem) {
    StorageNode<E> addNext;
    boolean changedOrder = true;
    int position;

    if (this.containsInAutomatic(elem)) {
      // The list already contains the element with automatic ordering.
      int index = indexInAutomaticList(elem);
      addNext = prioritizedList.get(index);
      position = findLocation(++addNext.count);

      if (position != index) {
        prioritizedList.remove(addNext);
      } else {
        changedOrder = false;
      }
    } else if (this.containsInManual(elem)) {
      // Relocate the manual priority element, while increasing count.
      addNext = manualPrioritySeat;
      manualPrioritySeat = null;
      position = findLocation(addNext.count);
    } else {
      // Introduce a new element with a new storage node.
      addNext = new StorageNode<>(elem);
      position = findLocation(1);
      size++;
    }
    
    if (changedOrder) {
      // Represents cases where order in the list has changed.
      prioritizedList.add(position, addNext);
    }
    return changedOrder;
  }

  /**
   * Sets the specified element to manual priority, and removes it from automatic ordering.
   * If an element is already set to manual priority, it will be moved to automatic ordering
   * and positioned in the appropriate place in the list. The count of neither node will be
   * modified in the process.
   * 
   * @param elem
   *        The element to set to manual priority
   * @throws IllegalArgumentException
   *         If the specified element is not set to automatic priority in the list
   * @throws NullPointerException
   *         If the specified element is null
   */
  public void relocateAutomaticToManual(E elem) {
    if (this.containsInAutomatic(elem)) {
      StorageNode<E> relocating = prioritizedList.remove(indexInAutomaticList(elem));
      if (manualPrioritySeat != null) {
        relocateManualToAutomatic();
      }
      manualPrioritySeat = relocating;
    } else {
      throw new IllegalArgumentException("Element must be set to automatic priority in "
                                         + "order to relocate it to manual priority");
    } 
  }

  /**
   * Moves the element that is currently set to manual priority into the automatically
   * ordered section of the list. The element will be positioned in the list according to
   * its count, which will not be changed during the operation. After the operation no
   * element will be set to manual priority; none will be chosen to replace the relocated
   * manual element. This means that all elements in the list will be ordered by count
   * after this operation.
   * 
   * @throws IllegalStateException
   *         If no element is set to manual priority
   */
  public void relocateManualToAutomatic() {
    if (manualPrioritySeat == null) {
      throw new IllegalStateException("No element is set to manual priority");
    } else {
      // Modifications to the list are handled in this method call.
      addPriorityOrdered(manualPrioritySeat.data);
    }
  }

  /**
   * Ensures that the specified element is set to manual priority, and increases its count
   * by 1 if it is present in the list. Returns whether or not the order of elements in
   * the list was changed by this operation: this does not include increases in count
   * that do not cause elements to rearrange.
   * 
   * <p>If the specified element is already set to manual priority, its count will be increased
   * by 1 and no other changes will occur. If the element is set to automatic ordering and
   * no element is set to manual priority, the element will be moved to manual priority and
   * its count will be increased by 1. If the element is in automatic ordering and a different
   * element is set to manual priority, the current manual priority element will be moved
   * to automatic ordering, the specified element will take its place, and the specified
   * element's count will increase by 1. If the specified element is not already contained in
   * the list, it will be introduced with count equal to 1, the current manual priority element
   * will be moved to automatic ordering if applicable, and the specified element will be set
   * to manual priority.
   * 
   * @param newObj
   *        The element whose presence as manual priority element is being ensured
   * @return <code>true</code> if the order of elements was changed by this operation
   * @throws NullPointerException
   *         If the specified element is null
   */
  public boolean addToManual(E newObj) {
    if (newObj != null) {
      // Wrap in a StorageNode to make use of StorageNode.equals flexibility.
      StorageNode<E> newNode = new StorageNode<>(newObj);
      
      if (newNode.equals(manualPrioritySeat)) {
        manualPrioritySeat.count++;
        return false;
      } else {
        if (this.containsInAutomatic(newObj)) {
          // Recover the node from the list to not lose its count value.
          newNode = prioritizedList.remove(indexInAutomaticList(newObj));
          newNode.count++;
        } else {
          /* The element is not already contained in the list; the 'temporary' value of 
             newNode is kept and inserted later. */
          size++;
        }
        
        if (manualPrioritySeat != null) {
          relocateManualToAutomatic();
        }
        manualPrioritySeat = newNode;
        return true;
      }
    } else {
      throw new NullPointerException("StackingPriorityList does not permit null elements");
    }
  }

  /**
   * Ensures the specified element is set to automatic priority, and increases its count
   * by 1 if it is present in the list. Returns whether or not the order of elements in
   * the list was changed by this operation: this does not include increases in count
   * that do not cause elements to rearrange.
   * 
   * <p>If the specified element is already set to automatic priority, its count will be
   * increased by 1 and it will be moved forward in the list if its newly increased count
   * exceeds that of any elements before it in the list. If the element is set to manual
   * priority, its count will be increased by 1 and it will be moved to automatic priority
   * and positions as dictated by its count. If the element is not already present in the
   * list it will be introduced with count equal to 1, and positioned at the back of the
   * list where its count dictates.
   * 
   * @param newObj
   *        The element whose presence in automatic ordering is being ensured
   * @return <code>true</code> if the order of elements was changed by this operation
   * @throws NullPointerException
   *         If the specified element is null
   */
  public boolean addToAutomatic(E newObj) {
    if (newObj != null) {
      return addPriorityOrdered(newObj);
    } else {
      throw new NullPointerException("StackingPriorityList does not permit null elements");
    }
  }

  /**
   * Returns the element in the specified location in the list. Analogous to method
   * <code>get</code> under interface <code>List</code>.
   * 
   * <p>If an element is set to manual priority, it will always be considered to be
   * at index 0. All other elements will be at an index equal to their position in
   * the automatic ordered section of the list plus one. If no element is set to
   * manual priority, every element's index will be equal to its position in the
   * automatically ordered section of the list.
   * 
   * @param index
   *        Index of the element to return
   * @return The element at the specified position in the list
   * @throws IndexOutOfBoundsException
   *         If the index is out of range (index < 0 || index >= size())
   */
  public E get(int index) {
    if (manualPrioritySeat == null) {
      return prioritizedList.get(index).data;
    } else if (index == 0) {
      return manualPrioritySeat.data;
    } else {
      return prioritizedList.get(index - 1).data;
    }
  }

  @Override
  public boolean add(E elem) {
    if (this.containsInManual(elem)) {
      addToManual(elem);
      return false;
    } else {
      return addToAutomatic(elem);
    }
  }

  @Override
  public boolean addAll(Collection<? extends E> lst) {
    if (lst instanceof Set && !(lst instanceof SortedSet)) {
      throw new IllegalArgumentException("StackingPriorityList does not support addition "
                                         + "of unordered collections");
    } else if (lst == this) {
      throw new IllegalArgumentException("StackingPriorityList cannot perform operation "
                                         + "addAll with itself as the argument");
    } else {
      boolean changed = false;
      for (E item : lst) {
        if (add(item)) {
          changed = true;
        }
      }
      return changed;
    }
  }

  @Override
  public boolean remove(Object elem) {
    if (this.containsInAutomatic(elem)) {
      prioritizedList.remove(new StorageNode<Object>(elem));
      size--;
      return true;
    } else if (this.containsInManual(elem)) {
      manualPrioritySeat = null;
      size--;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean removeAll(Collection<?> lst) {
    boolean changed = false;
    for (Object item : lst) {
      if (this.remove(item)) {
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> lst) {
    boolean changed = false;
    Iterator<E> iter = iterator();
    while (iter.hasNext()) {
      if (!lst.contains(iter.next())) {
        iter.remove();
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Returns an instance of StorageNode containing data on the requested element. The
   * element will be removed from the list afterward, and its count and other information
   * will be preserved in the node returned.
   * 
   * <p>All information in StorageNode is private, including the element itself; the
   * return cannot be used outside of <code>StackingPriorityList</code>. The main
   * purposes for retrieving an element's node are to transfer it to another instance
   * of <code>StackingPriorityList</code> while preserving its count, or to serialize
   * the node and be able to read it and load it into a <code>StackingPriorityList</code>
   * later.
   * 
   * <p>Retrieved nodes can be returned to a <code>StackingPriorityList</code> using the
   * <code>load</code> method, so long as the node contains the same class of element
   * as the list holds. Otherwise, runtime exceptions may be thrown during loading.
   * 
   * @param elem
   *        The element whose node is to be returned
   * @return The node containing the requested element
   * @throws IllegalArgumentException
   *         If the requested node is not contained in the list
   */
  public StorageNode<E> retrieve(E elem) {
    if (!this.contains(elem)) {
      throw new IllegalArgumentException("The requested item is not contained in this list");
    } else if (manualPrioritySeat != null && manualPrioritySeat.equals(elem)) {
      StorageNode<E> ret = manualPrioritySeat;
      manualPrioritySeat = null;
      size--;
      return ret;
    } else {
      size--;
      return prioritizedList.remove(indexInAutomaticList(elem));
    }
  }
  
  /**
   * Returns an instance of StorageNode back into the list in its appropriate position.
   * Instances of StorageNode can only be accessed using the <code>retrieve</code> method.
   * 
   * <p>Where the node is inserted depends on the value of the argument <code>inManual</code>.
   * If true, the node will be set to manual priority, displacing any existing manual priority
   * element and sending it to automatic ordering. If false, the node will be set to automatic
   * ordering and positioned based on its count from before loading; the element's count will
   * not be changed during the process.
   * 
   * <p>Unlike adding, loading requires the element inside the node not to be stored in the
   * list, and cannot be used to add the count of a StorageNode to that of a copy of the same
   * element contained in the list. In order to substitute the retrieved node's count for
   * the count of the element contained in the list, the one in the list must be removed
   * and the retrieved one subsequently loaded.
   * 
   * @param node
   *        The node being loaded
   * @param inManual
   *        Whether or not the node is to be loaded into manual priority
   * @throws IllegalArgumentException
   *         If the loading argument is of the wrong class and runtime class, or is already
   *         contained in the list
   */
  @SuppressWarnings("unchecked")
  public void load(Object node, boolean inManual) {
    if (!(node instanceof StackingPriorityList<?>.StorageNode<?>)) {
      throw new IllegalArgumentException("Only StackingPriorityList.StorageNode can be loaded "
                                         + "into StackingPriorityList");
    } else {
      StackingPriorityList<?>.StorageNode<?> cast = (StackingPriorityList<?>.StorageNode<?>)node;
      if (size() > 0 && !(this.get(0).getClass().equals(cast.data.getClass()))) {
        throw new IllegalArgumentException("The loading argument does not contain the right class "
                                           + "of element");
      } else {
        if (this.contains(cast)) {
          throw new IllegalArgumentException("The loading argument is already contained "
                                             + "in the list");
        } else if (inManual) {
          if (manualPrioritySeat != null) {
            relocateManualToAutomatic();
          }
          manualPrioritySeat = (StackingPriorityList<E>.StorageNode<E>)cast;
          size++;
        } else {
          int index = findLocation(cast.count);
          prioritizedList.add(index, (StackingPriorityList<E>.StorageNode<E>)cast);
          size++;
        }
      }
    }
  }
  
  @Override
  public ListIterator<E> iterator() {
    return new StackPriorityIterator();
  }

  @Override
  public Object[] toArray() {
    Object[] deposit = new Object[size];
    int ind = 0;
    for (E item : this) {
      deposit[ind++] = item;
    }
    return deposit;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] ofType) {
    T[] deposit = ofType;
    if (ofType.length > this.size) {
      deposit[size] = null;
    } else {
      deposit = (T[])Array.newInstance(ofType.getClass().getComponentType(), size);
    }
    
    Object[] source = this.toArray();
    for (int i = 0; i < size; i++) {
      deposit[i] = (T)source[i];
    }
    
    return deposit;
  }
}