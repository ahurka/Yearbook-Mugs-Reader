import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.junit.Test;

public class StackingPriorityListTest {
  private StackingPriorityList<Integer> fpQueue;
  
  private void setupAuto() {
    fpQueue = new StackingPriorityList<>();
    fpQueue.addToAutomatic(1);
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(3);
  }
  
  private void setupManual() {
    setupAuto();
    fpQueue.addToManual(5);
  }
  
  @Test
  public void testDefaultConstructorStartsEmpty() {
    fpQueue = new StackingPriorityList<>();
    assertEquals(0, fpQueue.size());
  }

  @Test
  public void testOneArgConstructor() {
    ArrayList<Integer> p = new ArrayList<>();
    p.add(5);
    p.add(4);
    p.add(3);
    fpQueue = new StackingPriorityList<Integer>(p);
    assertTrue(fpQueue.contains(5));
    assertTrue(fpQueue.containsInAutomatic(5));
    assertFalse(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(4));
    assertEquals(3, fpQueue.size());
  }
  
  @Test
  public void testIsEmpty() {
    fpQueue = new StackingPriorityList<>();
    assertTrue(fpQueue.isEmpty());
    fpQueue.addToAutomatic(5);
    assertFalse(fpQueue.isEmpty());
    fpQueue = new StackingPriorityList<>();
    fpQueue.addToManual(5);
    assertFalse(fpQueue.isEmpty());
    fpQueue.addToAutomatic(6);
    assertFalse(fpQueue.isEmpty());
    assertEquals(2, fpQueue.size());
  }
  
  @Test
  public void testClear() {
    setupManual();
    fpQueue.clear();
    assertTrue(fpQueue.isEmpty());
    Iterator<Integer> iter = fpQueue.iterator();
    assertFalse(iter.hasNext());
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullAutomaticAdd() {
    setupAuto();
    fpQueue.addToAutomatic(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullManualAdd() {
    setupAuto();
    fpQueue.addToManual(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullDefaultAdd() {
    setupAuto();
    fpQueue.add(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullRemoval() {
    setupManual();
    fpQueue.remove(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullAutomaticContains() {
    setupManual();
    fpQueue.contains(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRejectsNullManualContains() {
    setupManual();
    fpQueue.containsInManual(null);
  }
  
  @Test(expected = NoSuchElementException.class)
  public void testIterationAutomaticOrdering() {
    setupAuto();
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(3, fpQueue.size());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertEquals(1, (int)iter.next());
    assertFalse(iter.hasNext());
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(1));
    iter.next();
  }
  
  @Test(expected = NoSuchElementException.class)
  public void testForwardIterationManualOrder() {
    setupManual();
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(4, fpQueue.size());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertEquals(1, (int)iter.next());
    assertFalse(iter.hasNext());
    assertTrue(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(1));
    iter.next();
  }
  
  @Test
  public void testSetAutomaticToManualNoCurrentManual() {
    setupAuto();
    fpQueue.relocateAutomaticToManual(1);
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertFalse(fpQueue.containsInAutomatic(1));
    assertTrue(fpQueue.containsInManual(1));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(3, fpQueue.size());
    assertEquals(1, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertFalse(iter.hasNext());
  }

  @Test
  public void testSetAutomaticToManualCurrentManual() {
    setupManual();
    fpQueue.relocateAutomaticToManual(1);
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertFalse(fpQueue.containsInAutomatic(1));
    assertFalse(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInManual(1));
    assertTrue(fpQueue.containsInAutomatic(5));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(4, fpQueue.size());
    assertEquals(1, (int)iter.next());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertFalse(iter.hasNext());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgumentForRelocateAutomatic() {
    setupAuto();
    fpQueue.relocateAutomaticToManual(5);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgumentForRelocateAutomaticWithManual() {
    setupManual();
    fpQueue.relocateAutomaticToManual(9);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgumentForRelocateAutomaticWithRealManual() {
    setupManual();
    fpQueue.relocateAutomaticToManual(5);
  }
  
  @Test
  public void testRelocateManualWithManual() {
    setupManual();
    fpQueue.relocateManualToAutomatic();
    assertTrue(fpQueue.containsInAutomatic(3));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(1));
    assertFalse(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(5));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(4, fpQueue.size());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertEquals(1, (int)iter.next());
    assertFalse(iter.hasNext());
  }
  
  @Test(expected = IllegalStateException.class)
  public void testRelocateManualWithoutManual() {
    setupAuto();
    fpQueue.relocateManualToAutomatic();
  }
  
  @Test
  public void testRearrangesPriority() {
    setupAuto();
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(1);
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(3, fpQueue.size());
    assertEquals(1, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertFalse(iter.hasNext());
  }
  
  @Test
  public void testManualPriorityTrumpsAutoPriority() {
    setupManual();
    fpQueue.addToAutomatic(1);
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(3);
    fpQueue.addToAutomatic(1);
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(3);
    assertTrue(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(1));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(3));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(4, fpQueue.size());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(2, (int)iter.next());
    assertEquals(1, (int)iter.next());
    assertFalse(iter.hasNext());
  }
  
  @Test
  public void testManualToAutomaticOrdering() {
    setupManual();
    fpQueue.addToManual(5);
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(2);
    fpQueue.addToAutomatic(3);
    fpQueue.relocateManualToAutomatic();
    assertTrue(fpQueue.containsInAutomatic(5));
    assertTrue(fpQueue.containsInAutomatic(1));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(3));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(4, fpQueue.size());
    assertEquals(2, (int)iter.next());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());
    assertEquals(1, (int)iter.next());
    assertFalse(iter.hasNext());
  }
  
  @Test
  public void testMovesAutomaticToManual() {
    setupAuto();
    fpQueue.addToManual(2);
    assertTrue(fpQueue.containsInManual(2));
    assertFalse(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(1));
    assertTrue(fpQueue.containsInAutomatic(3));
  }
  
  @Test
  public void testMovesManualToAutomatic() {
    setupManual();
    fpQueue.addToManual(9);
    assertEquals(5, fpQueue.size());
    assertTrue(fpQueue.containsInManual(9));
    assertFalse(fpQueue.containsInAutomatic(9));
    assertTrue(fpQueue.containsInAutomatic(5));
    assertFalse(fpQueue.containsInManual(5));
    Iterator<Integer> iter = fpQueue.iterator();
    assertEquals(9, (int)iter.next());
    assertEquals(5, (int)iter.next());
    assertEquals(3, (int)iter.next());    
  }
  
  @Test
  public void testContains() {
    setupManual();
    assertTrue(fpQueue.contains(5));
    assertTrue(fpQueue.contains(1));
    assertTrue(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(1));
    assertFalse(fpQueue.containsInManual(1));
    assertFalse(fpQueue.containsInAutomatic(5));
    assertFalse(fpQueue.contains(9));
  }
  
  @Test
  public void testContainsAll() {
    setupManual();
    ArrayList<Integer> lst = new ArrayList<>();
    lst.add(1);
    lst.add(2);
    lst.add(3);
    lst.add(5);
    assertTrue(fpQueue.containsAll(lst));
    lst.add(9);
    assertFalse(fpQueue.containsAll(lst));
    lst.remove(1);
    lst.remove(1);
    assertFalse(fpQueue.containsAll(lst));
    lst.remove(2);
    assertTrue(fpQueue.containsAll(lst));
    lst.clear();
    assertTrue(fpQueue.containsAll(lst));
  }
  
  @Test
  public void testIndexing() {
    setupAuto();
    assertEquals(0, fpQueue.indexOf(3));
    assertEquals(1, fpQueue.indexOf(2));
    assertEquals(2, fpQueue.indexOf(1));
    fpQueue.addToAutomatic(1);
    fpQueue.addToManual(5);
    assertEquals(0, fpQueue.indexOf(5));
    assertEquals(1, fpQueue.indexOf(1));
    assertEquals(2, fpQueue.indexOf(3));
    assertEquals(3, fpQueue.indexOf(2));
    assertEquals(-1,fpQueue.indexOf(9));
  }
  
  @Test
  public void testGetIndexNoManual() {
    setupAuto();
    Iterator<Integer> iter = fpQueue.iterator();
    for (int i = 0; i < fpQueue.size(); i++) {
      assertEquals(iter.next(), fpQueue.get(i));
    }
  }
  
  @Test
  public void testGetIndexWithManual() {
    setupManual();
    Iterator<Integer> iter = fpQueue.iterator();
    for (int i = 0; i < fpQueue.size(); i++) {
      assertEquals(iter.next(), fpQueue.get(i));
    }
  }
  
  @Test
  public void testAddReturn() {
    setupManual();
    assertFalse(fpQueue.add(5));
    assertTrue(fpQueue.add(2));
    assertFalse(fpQueue.add(2));
    assertTrue(fpQueue.add(1));
    assertTrue(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(2));
    assertTrue(fpQueue.containsInAutomatic(1));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRejectsSelf() {
    setupManual();
    fpQueue.addAll(fpQueue);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRejectsUnsortedSets() {
    setupAuto();
    HashSet<Integer> reject = new HashSet<>();
    reject.add(5);
    reject.add(2);
    fpQueue.addAll(reject);
  }
  
  @Test
  public void testAcceptsSortedSets() {
    setupManual();
    TreeSet<Integer> accept = new TreeSet<>();
    accept.add(5);
    assertFalse(fpQueue.addAll(accept));
    accept.add(9);
    assertTrue(fpQueue.addAll(accept));
    assertEquals(5, fpQueue.size());
    assertTrue(fpQueue.containsInManual(5));
    assertTrue(fpQueue.containsInAutomatic(9));
  }
  
  @Test
  public void testRemoveInvalid() {
    setupAuto();
    assertFalse(fpQueue.remove(9));
    assertEquals(3, fpQueue.size());
  }
  
  @Test
  public void testRemoveValid() {
    setupManual();
    assertTrue(fpQueue.remove(3));
    assertFalse(fpQueue.containsInAutomatic(3));
    assertEquals(3, fpQueue.size());
    assertTrue(fpQueue.remove(5));
    assertFalse(fpQueue.containsInManual(5));
    assertEquals(2, fpQueue.size());
  }
  
  @Test
  public void testRemoveCollection() {
    setupManual();
    HashSet<Integer> accept = new HashSet<>();
    accept.add(2);
    assertTrue(fpQueue.removeAll(accept));
    assertFalse(fpQueue.removeAll(accept));
    accept.add(5);
    assertTrue(fpQueue.removeAll(accept));
    assertEquals(2, fpQueue.size());
    fpQueue.clear();
    assertFalse(fpQueue.removeAll(accept));
  }
  
  @Test
  public void testRetainCollection() {
    setupManual();
    HashSet<Integer> accept = new HashSet<>();
    accept.add(2);
    accept.add(5);
    accept.add(1);
    assertTrue(fpQueue.retainAll(accept));
    assertEquals(3, fpQueue.size());
    accept.add(9);
    assertFalse(fpQueue.retainAll(accept));
    accept.clear();
    assertTrue(fpQueue.retainAll(accept));
    assertTrue(fpQueue.isEmpty());
  }
  
  @Test
  public void testToArrayNoArgs() {
    setupManual();
    fpQueue.addToAutomatic(9);
    Object[] ints = fpQueue.toArray();
    assertEquals(5, ints.length);
    assertEquals(5, fpQueue.size());
    for (int i = 0; i < fpQueue.size(); i++) {
      assertEquals(fpQueue.get(i), ints[i]);
    }
  }
  
  @Test
  public void testToArrayWithEmptyArg() {
    setupManual();
    fpQueue.addToAutomatic(9);
    Integer[] ints = fpQueue.toArray(new Integer[0]);
    assertEquals(5, ints.length);
    assertEquals(5, fpQueue.size());
    for (int i = 0; i < fpQueue.size(); i++) {
      assertEquals(fpQueue.get(i), ints[i]);
    }
  }
  
  @Test
  public void testToArrayWithLargeArg() {
    setupManual();
    fpQueue.addToAutomatic(9);
    Integer[] intsExtra = new Integer[7];
    intsExtra[5] = 6;
    intsExtra[6] = 7;
    Integer[] ints = fpQueue.toArray(new Integer[5]);
    intsExtra = fpQueue.toArray(intsExtra);
    assertEquals(5, fpQueue.size());
    assertEquals(5, ints.length);
    assertEquals(7, intsExtra.length);
    for (int i = 0; i < fpQueue.size(); i++) {
      assertEquals(fpQueue.get(i), ints[i]);
      assertEquals(fpQueue.get(i), intsExtra[i]);
    }
    assertNull(intsExtra[5]);
    assertEquals(7, (int)intsExtra[6]);
  }
  
  @Test(expected = NoSuchElementException.class)
  public void testBackwardIterationWithManual() {
    setupManual();
    ListIterator<Integer> iter = fpQueue.iterator();
    assertFalse(iter.hasPrevious());
    iter.next();
    assertTrue(iter.hasPrevious());
    assertEquals(0, iter.previousIndex());
    iter.next();
    assertEquals(1, iter.previousIndex());
    iter.next();
    assertEquals(2, iter.previousIndex());
    iter.next();
    assertEquals(3, iter.previousIndex());
    assertTrue(iter.hasPrevious());
    assertEquals(1, (int)iter.previous());
    assertEquals(2, iter.previousIndex());
    assertEquals(2, (int)iter.previous());
    assertEquals(1, iter.previousIndex());
    assertEquals(3, (int)iter.previous());
    assertEquals(0, iter.previousIndex());
    assertEquals(5, (int)iter.previous());
    assertEquals(-1, iter.previousIndex());
    assertFalse(iter.hasPrevious());
    iter.previous();
  }
  
  @Test(expected = NoSuchElementException.class)
  public void testBackwardIterationNoManual() {
    setupAuto();
    ListIterator<Integer> iter = fpQueue.iterator();
    assertFalse(iter.hasPrevious());
    iter.next();
    assertTrue(iter.hasPrevious());
    assertEquals(0, iter.previousIndex());
    iter.next();
    assertEquals(1, iter.previousIndex());
    iter.next();
    assertEquals(2, iter.previousIndex());
    assertEquals(1, (int)iter.previous());
    assertEquals(1, iter.previousIndex());
    assertEquals(2, (int)iter.previous());
    assertEquals(0, iter.previousIndex());
    assertEquals(3, (int)iter.previous());
    assertEquals(-1, iter.previousIndex());
    assertFalse(iter.hasPrevious());
    iter.previous();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testRejectsRemovalFromBeginning() {
    setupManual();
    fpQueue.iterator().remove();
  }
  
  @Test
  public void testRemovalForward() {
    setupManual();
    ListIterator<Integer> iter = fpQueue.iterator();
    iter.next();
    iter.remove();
    assertFalse(fpQueue.contains(5));
    assertEquals(3, fpQueue.size()); 
    iter.next();
    iter.remove();
    iter.next();
    iter.remove();
    iter.next();
    iter.remove();
    assertTrue(fpQueue.isEmpty());
    assertFalse(iter.hasNext());
    assertFalse(iter.hasPrevious());
  }
  
  @Test
  public void testRemovalBackward() {
    setupManual();
    ListIterator<Integer> iter = fpQueue.iterator();
    iter.next();
    iter.next();
    iter.next();
    iter.next();
    iter.previous();
    iter.remove();
    assertFalse(fpQueue.contains(1));
    assertEquals(3, fpQueue.size()); 
    iter.previous();
    iter.remove();
    iter.previous();
    iter.remove();
    iter.previous();
    iter.remove();
    assertTrue(fpQueue.isEmpty());
    assertFalse(iter.hasPrevious());
    assertFalse(iter.hasNext());
  }
  
  @Test
  public void testStorageNodeLoading() {
    setupManual();
    StackingPriorityList<Integer>.StorageNode<Integer> node1 = fpQueue.retrieve(5);
    assertEquals(3, fpQueue.size());
    assertFalse(fpQueue.contains(5));
    StackingPriorityList<Integer>.StorageNode<Integer> node2 = fpQueue.retrieve(3);
    StackingPriorityList<Integer>.StorageNode<Integer> node3 = fpQueue.retrieve(2);
    StackingPriorityList<Integer>.StorageNode<Integer> node4 = fpQueue.retrieve(1);
    assertTrue(fpQueue.isEmpty());
    fpQueue.load(node3, false);
    assertEquals(1, (int)fpQueue.size());
    assertTrue(fpQueue.contains(2));
    fpQueue.load(node2, false);
    fpQueue.load(node4, false);
    fpQueue.load(node1, true);
    assertTrue(fpQueue.containsInManual(5));
    assertEquals(4, fpQueue.size());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveRejectsInvalidArg() {
    fpQueue = new StackingPriorityList<>();
    fpQueue.retrieve(6);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testLoadRejectsPresentArg() {
    setupManual();
    StackingPriorityList<Integer> otherOne = new StackingPriorityList<>();
    otherOne.addToAutomatic(5);
    fpQueue.load(otherOne.retrieve(5), false);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRejectsWrongTypeNodes() {
    setupAuto();
    StackingPriorityList<String> otherQueue = new StackingPriorityList<>();
    otherQueue.add("");
    StackingPriorityList<String>.StorageNode<String> p = otherQueue.retrieve("");
    fpQueue.load(p, false);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRejectsWrongLoadingWrongObjects() {
    setupAuto();
    fpQueue.load(9, true);
  }
}
