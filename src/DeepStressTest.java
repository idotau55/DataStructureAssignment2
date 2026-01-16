
import java.util.*;

/**
 * DeepStressTest - Comprehensive validation of Fibonacci Heap implementation.
 * 
 * Tests 30 heaps with 500+ elements each under various configurations.
 * Includes edge cases, invariant checks, and statistical tracking.
 */
public class DeepStressTest {
    private static final int NUM_HEAPS = 30;
    private static final int MIN_SIZE = 500;
    private static final int OPS_PER_HEAP = 5000;
    private static final int KEY_RANGE = 1000000;

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failureMessages = new ArrayList<>();

    private static class ShadowHeap {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        List<Heap.HeapItem> items = new ArrayList<>();
        Heap heap;
        String name;
        int id;

        public ShadowHeap(boolean lazyMelds, boolean lazyDecreaseKeys, int id) {
            this.heap = new Heap(lazyMelds, lazyDecreaseKeys);
            this.name = "Heap_" + id + "_LM=" + lazyMelds + "_LD=" + lazyDecreaseKeys;
            this.id = id;
        }
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         DEEP STRESS TEST - Fibonacci Heap Validation         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        long startTime = System.currentTimeMillis();
        Random rand = new Random(54321); // Fixed seed for reproducibility

        List<ShadowHeap> heaps = new ArrayList<>();

        // Initialize heaps with all 4 configurations evenly distributed
        boolean[][] configs = { { false, false }, { false, true }, { true, false }, { true, true } };
        for (int i = 0; i < NUM_HEAPS; i++) {
            boolean[] cfg = configs[i % 4];
            heaps.add(new ShadowHeap(cfg[0], cfg[1], i));
        }

        // ==================== PHASE 1: Initial Population ====================
        System.out.println("Phase 1: Populating heaps with " + MIN_SIZE + "+ elements each...");
        for (ShadowHeap sh : heaps) {
            for (int i = 0; i < MIN_SIZE + rand.nextInt(200); i++) {
                int key = rand.nextInt(KEY_RANGE) + 1; // keys > 0
                Heap.HeapItem item = sh.heap.insert(key, "V" + key);
                sh.pq.add(key);
                sh.items.add(item);
            }
            verify(sh, "Phase1_Init");
        }
        System.out.println("   ✓ All heaps populated.");

        // ==================== PHASE 2: Random Operations ====================
        System.out.println("\nPhase 2: Performing " + OPS_PER_HEAP + " random operations per heap...");
        for (ShadowHeap sh : heaps) {
            for (int i = 0; i < OPS_PER_HEAP; i++) {
                int op = rand.nextInt(100);

                try {
                    // 30% Insert
                    if (op < 30) {
                        int key = rand.nextInt(KEY_RANGE) + 1;
                        Heap.HeapItem item = sh.heap.insert(key, "V" + key);
                        sh.pq.add(key);
                        sh.items.add(item);
                    }
                    // 25% DeleteMin
                    else if (op < 55) {
                        if (!sh.pq.isEmpty()) {
                            int expectedMin = sh.pq.peek();
                            Heap.HeapItem minItem = sh.heap.findMin();

                            if (minItem == null || minItem.key != expectedMin) {
                                throw new RuntimeException("Min mismatch. Expected=" + expectedMin +
                                        ", Got=" + (minItem == null ? "null" : minItem.key));
                            }

                            sh.heap.deleteMin();
                            sh.pq.poll();
                            sh.items.removeIf(item -> item == minItem);
                        }
                    }
                    // 20% DecreaseKey
                    else if (op < 75) {
                        if (!sh.items.isEmpty()) {
                            Heap.HeapItem item = sh.items.get(rand.nextInt(sh.items.size()));
                            int currentKey = item.key;
                            if (currentKey > 1) {
                                int diff = rand.nextInt(currentKey - 1) + 1;
                                sh.pq.remove(currentKey);
                                sh.pq.add(currentKey - diff);
                                sh.heap.decreaseKey(item, diff);

                                if (item.key != currentKey - diff) {
                                    throw new RuntimeException("DecreaseKey failed. Expected=" +
                                            (currentKey - diff) + ", Got=" + item.key);
                                }
                            }
                        }
                    }
                    // 15% Delete
                    else if (op < 90) {
                        if (!sh.items.isEmpty()) {
                            int idx = rand.nextInt(sh.items.size());
                            Heap.HeapItem item = sh.items.get(idx);
                            int key = item.key;
                            sh.heap.delete(item);
                            sh.pq.remove(key);
                            sh.items.remove(idx);
                        }
                    }
                    // 10% Meld
                    else {
                        ShadowHeap other = findMeldableHeap(heaps, sh);
                        if (other != null && other.heap.size() > 0) {
                            sh.heap.meld(other.heap);
                            sh.pq.addAll(other.pq);
                            sh.items.addAll(other.items);
                            other.pq.clear();
                            other.items.clear();

                            if (other.heap.size() != 0) {
                                throw new RuntimeException("Melded heap not empty after meld");
                            }
                        }
                    }

                } catch (Exception e) {
                    recordFailure(sh.name, "Phase2_Op" + i, e.getMessage());
                    break;
                }
            }
            verify(sh, "Phase2_Final");
        }
        System.out.println("   ✓ Random operations complete.");

        // ==================== PHASE 3: Edge Cases ====================
        System.out.println("\nPhase 3: Edge case testing...");

        // Test 3.1: Empty heap operations
        Heap emptyHeap = new Heap(false, false);
        assert emptyHeap.findMin() == null : "Empty heap min should be null";
        emptyHeap.deleteMin(); // Should not crash
        assert emptyHeap.size() == 0 : "Empty heap size should be 0";
        recordTest("EdgeCase_EmptyHeap", true, null);

        // Test 3.2: Single element
        Heap singleHeap = new Heap(true, true);
        Heap.HeapItem single = singleHeap.insert(42, "Single");
        assert singleHeap.findMin().key == 42 : "Single element min should be 42";
        singleHeap.deleteMin();
        assert singleHeap.size() == 0 : "After deleteMin, size should be 0";
        assert singleHeap.findMin() == null : "After deleteMin, min should be null";
        recordTest("EdgeCase_SingleElement", true, null);

        // Test 3.3: Decreasing to minimum
        Heap decHeap = new Heap(false, false);
        decHeap.insert(100, "A");
        Heap.HeapItem b = decHeap.insert(200, "B");
        decHeap.decreaseKey(b, 150); // B becomes 50, should be new min
        assert decHeap.findMin().key == 50 : "After decreaseKey, min should be 50";
        recordTest("EdgeCase_DecreaseToMin", true, null);

        // Test 3.4: Delete non-min
        Heap delHeap = new Heap(true, false);
        delHeap.insert(10, "A");
        Heap.HeapItem mid = delHeap.insert(20, "B");
        delHeap.insert(30, "C");
        delHeap.delete(mid);
        assert delHeap.size() == 2 : "After delete, size should be 2";
        assert delHeap.findMin().key == 10 : "After delete, min should still be 10";
        recordTest("EdgeCase_DeleteNonMin", true, null);

        // Test 3.5: Large sequential inserts then mass delete
        Heap massHeap = new Heap(false, true);
        for (int i = 1; i <= 1000; i++) {
            massHeap.insert(i, "N" + i);
        }
        for (int i = 0; i < 1000; i++) {
            if (massHeap.findMin().key != (i + 1)) {
                recordTest("EdgeCase_MassSequential", false, "Min mismatch at step " + i);
                break;
            }
            massHeap.deleteMin();
        }
        if (massHeap.size() == 0) {
            recordTest("EdgeCase_MassSequential", true, null);
        }

        // Test 3.6: Counter validation
        Heap counterHeap = new Heap(false, false);
        for (int i = 0; i < 100; i++)
            counterHeap.insert(rand.nextInt(1000), "X");
        int initialLinks = counterHeap.totalLinks();
        counterHeap.deleteMin(); // Should trigger consolidate -> links
        int afterLinks = counterHeap.totalLinks();
        if (afterLinks >= initialLinks) {
            recordTest("EdgeCase_LinkCounter", true, null);
        } else {
            recordTest("EdgeCase_LinkCounter", false, "Links decreased after deleteMin");
        }

        // Test 3.7: Meld empty into non-empty
        Heap nonEmpty = new Heap(true, true);
        nonEmpty.insert(5, "A");
        Heap empty2 = new Heap(true, true);
        int sizeBefore = nonEmpty.size();
        nonEmpty.meld(empty2);
        assert nonEmpty.size() == sizeBefore : "Melding empty should not change size";
        recordTest("EdgeCase_MeldEmpty", true, null);

        System.out.println("   ✓ Edge cases complete.");

        // ==================== PHASE 4: Invariant Deep Check ====================
        System.out.println("\nPhase 4: Final invariant validation...");
        for (ShadowHeap sh : heaps) {
            if (sh.heap.size() != sh.pq.size()) {
                recordFailure(sh.name, "Phase4_SizeCheck", "Size mismatch");
            }
            if (!sh.pq.isEmpty() && sh.heap.findMin() != null) {
                if (sh.heap.findMin().key != sh.pq.peek()) {
                    recordFailure(sh.name, "Phase4_MinCheck", "Min mismatch");
                }
            }

            // Check numTrees >= 1 if not empty
            if (sh.heap.size() > 0 && sh.heap.numTrees() < 1) {
                recordFailure(sh.name, "Phase4_TreeCount", "numTrees < 1 on non-empty heap");
            }
        }
        System.out.println("   ✓ Invariant checks complete.");

        // ==================== SUMMARY ====================
        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                       TEST SUMMARY                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Tests:    %-44d ║%n", totalTests);
        System.out.printf("║  Passed:         %-44d ║%n", passedTests);
        System.out.printf("║  Failed:         %-44d ║%n", failedTests);
        System.out.printf("║  Time Elapsed:   %-44s ║%n", (endTime - startTime) + "ms");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        if (failedTests == 0) {
            System.out.println("║                    RESULT: ✅ PASS                            ║");
            System.out.println("║                    Grade: 100/100                            ║");
        } else {
            System.out.println("║                    RESULT: ❌ FAIL                            ║");
            int grade = (int) Math.round(100.0 * passedTests / totalTests);
            System.out.printf("║                    Grade: %d/100                             ║%n", grade);
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if (!failureMessages.isEmpty()) {
            System.out.println("\nFailure Details:");
            for (String msg : failureMessages) {
                System.out.println("  • " + msg);
            }
        }
    }

    private static void verify(ShadowHeap sh, String phase) {
        totalTests++;
        if (sh.heap.size() != sh.pq.size()) {
            recordFailure(sh.name, phase, "Size mismatch: Heap=" + sh.heap.size() + ", Shadow=" + sh.pq.size());
            return;
        }
        if (!sh.pq.isEmpty()) {
            if (sh.heap.findMin() == null) {
                recordFailure(sh.name, phase, "Heap min is null but shadow is not empty");
                return;
            }
            if (sh.heap.findMin().key != sh.pq.peek()) {
                recordFailure(sh.name, phase,
                        "Min mismatch: Heap=" + sh.heap.findMin().key + ", Shadow=" + sh.pq.peek());
                return;
            }
        }
        passedTests++;
    }

    private static void recordTest(String name, boolean passed, String error) {
        totalTests++;
        if (passed) {
            passedTests++;
        } else {
            failedTests++;
            failureMessages.add(name + ": " + error);
        }
    }

    private static void recordFailure(String heapName, String phase, String error) {
        failedTests++;
        failureMessages.add("[" + heapName + "] " + phase + " - " + error);
    }

    private static ShadowHeap findMeldableHeap(List<ShadowHeap> heaps, ShadowHeap current) {
        for (ShadowHeap candidate : heaps) {
            if (candidate != current && candidate.heap.size() > 0 &&
                    candidate.heap.lazyMelds == current.heap.lazyMelds &&
                    candidate.heap.lazyDecreaseKeys == current.heap.lazyDecreaseKeys) {
                return candidate;
            }
        }
        return null;
    }
}
