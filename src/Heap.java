/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final double GoldenRation = (1 + Math.sqrt(5)) / 2; // keep for bucket calculations
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
    private int size;
    private HeapNode first;
    private int totalMarks = 0;
    private int totalLinks = 0;
    private int totalCuts = 0;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.size = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {    
        HeapNode node = new HeapNode();
        HeapItem item = new HeapItem();
        item.key = key;
        item.info = info;
        item.node = node;
        node.item = item;

        if (this.size == 0) {
            this.min = item;
            this.first = node;
            node.next = node;
            node.prev = node;
        } else {
            // add to the root list (circular) next to first for now
            HeapNode last = this.first.prev;
            last.next = node;
            node.prev = last;
            node.next = this.first;
            this.first.prev = node;
            if (item.key < this.min.key) {
                this.min = item;
            }
        }

        this.size++;
        return item;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return this.min;
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        return; // should be replaced by student code
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) 
    {    
        if (x == null || x.node == null) {
            return;
        }

        x.key -= diff;
        if (x.key < this.min.key) {
            this.min = x;
        }

        if (!this.lazyDecreaseKeys) {
            HeapifyUp(x.node);
        }
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {    
        // Positive keys requirement means we can make it negative and then deleteMin.
        if (x == null) {
            return;
        }
        decreaseKey(x, x.key + 1); // shift to a negative value
        deleteMin();
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        if (heap2 == null) {
            return;
        }
        if (!(heap2.lazyMelds == this.lazyMelds && heap2.lazyDecreaseKeys == this.lazyDecreaseKeys)) {
            return; // precondition not met
        }

        if (heap2.size == 0) {
            return;
        }
        if (this.size == 0) {
            // adopt heap2
            this.first = heap2.first;
            this.min = heap2.min;
            this.size = heap2.size;
            return;
        }

        // concatenate root lists (both circular)
        HeapNode aLast = this.first.prev;
        HeapNode bLast = heap2.first.prev;

        aLast.next = heap2.first;
        heap2.first.prev = aLast;

        bLast.next = this.first;
        this.first.prev = bLast;

        this.size += heap2.size;
        if (heap2.min != null && heap2.min.key < this.min.key) {
            this.min = heap2.min;
        }
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return this.size;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees() // O(number of trees)
    {
        HeapNode current = this.first;
        if (current == null) {return 0;}
        int count = 1;
        while(current.next != this.first){
            count++;
            current = current.next;
        }

            // Probably should be done in deleteMin or meld to be O(1)
        return count;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes() //O(1), work is done over cascadingCuts
    {
        if (lazyDecreaseKeys) return totalMarks; // should be replaced by student code
        return 0; // Cannot have marks when no LazyDecraeseKeys
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return 46; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return 46; // should be replaced by student code
    }

    public void HeapifyUp(HeapNode x){
        while (x != null && x.parent != null && x.item.key < x.parent.item.key){
            // swap only HeapItems between nodes
            HeapItem parentItem = x.parent.item;
            x.parent.item = x.item;
            x.parent.item.node = x.parent;
            x.item = parentItem;
            x.item.node = x;
            if (this.min == null || x.parent.item.key < this.min.key) {
                this.min = x.parent.item;
            }
        }
    }

    public void HeapifyDown(){}

    public void setFirst(HeapNode first){this.first = first;}

    public void cut( HeapNode x, HeapNode y){ // O(1)
        x.parent = null;
        x.mark = 0;
        y.rank--;
        if(x.next == x){
            y.child =null;
        }
        else{
            y.child = x.next;
            x.prev.next = x.next;
            x.next.prev = x.prev;
        }

    }

    public void CascadingCut(HeapNode x, HeapNode y){
        cut(x,y);
        if(y.parent != null){
            if (y.mark ==0){ 
                y.mark++;
                totalMarks++; //Maintaining totalMarks
            }
            else{
                totalMarks--; //Maintaining totalMarks
                CascadingCut(y,y.parent);}
        }
    }


    //this is Successive Linking
    public HeapNode consolidate(HeapNode x){
        //toBuckets(x);
       return toBuckets(x); //tobuckets calls frombuckets with the buckets array

    }
    public HeapNode toBuckets(HeapNode x){ //NEED TO WRITE
        HeapNode[ ] buckets = new HeapNode [getLogOfSizeBaseGoldenRatio().intValue()+1]; //Should initlize to null
        x.prev.next = null; //break circularity
        HeapNode current = x;
        while(current != null){
            HeapNode y = current;
            current = current.next;
            while(buckets[y.rank]!=null){
                y= link(y,buckets[y.rank]);
                buckets[y.rank -1] = null;
            }
            buckets[y.rank]= y;

        }
        return fromBuckets(buckets);

    }
    public HeapNode fromBuckets(HeapNode[ ] buckets){
        HeapNode x = null;
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i]!= null){
                if (x!=null){
                    x = buckets[i];
                    x.next =x;
                    x.prev =x;
                }
                else{
                    insertAfter(x,buckets[i]);
                    if (buckets[i].item.key < x.item.key){ // I AM NOT SUTE ABOUT THIS PART AND HeapItem
                        x = buckets[i];
                    }
                }
            }
        }
        return x;

    } 

    public HeapNode link(HeapNode x, HeapNode y){
        if (x.item.key < y.item.key){
            //Make y child of x, removes y from root list
            y.prev.next = y.next;
            y.next.prev = y.prev;
            //Adds y to x's child list
            if (x.child == null){
                x.child = y;
                y.next = y;
                y.prev = y;
            }
            else{
                insertAfter(x.child.prev,y);
            }
            y.parent = x;
            x.rank++;
            return x;
        }
        else{
            //Makes x child of y
            //Removes x from root list
            x.prev.next = x.next;
            x.next.prev = x.prev;
            //Adds x to y's child list
            if (y.child == null){
                y.child = x;
                x.next = x;
                x.prev = x;
            }
            else{
                insertAfter(y.child.prev,x);
            }
            x.parent = y;
            y.rank++;
            return y;
        }
    } 

    public Double getLogOfSizeBaseGoldenRatio(){return Math.log(this.size())/Math.log(GoldenRation);}

    public void insertAfter(HeapNode x, HeapNode y){
        if (x == null){
            y.next = y;
            y.prev = y;
        }
        else{
            y.next = x.next;
            x.next.prev = y;
            x.next = y;
            y.prev = x;
        }
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public int mark;
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
    }
}
