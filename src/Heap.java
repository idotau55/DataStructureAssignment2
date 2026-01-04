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
    public final double GoldenRation =  (1 + Math.sqrt(5))/2;
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapNode min;
    private int size;
    private HeapNode first;
    private HeapNode[] trees;


    
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

        // student code can be added here
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapNode insert(int key, String info)  // O(1)
    {
        HeapNode newNode = new HeapNode(key, info);
        if (this.size() == 0){
            this.min = newNode;
            this.size++;
            this.setFirst(newNode);
            return newNode;

        }
        else if (newNode.key < this.min.key) { this.min = newNode;}
        this.size++; return newNode;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapNode findMin()
    {return this.min;} // O(1)

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
    public void decreaseKey(HeapNode x, int diff) 
    {    
        return; // should be replaced by student code
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapNode x) 
    {    
        return; // should be replaced by student code
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        if(this.first.rank != heap2.first.rank){ return;} //cannot meld heaps of different ranks
        if(!((heap2.lazyMelds == this.lazyMelds) && (heap2).lazyDecreaseKeys ==this.lazyDecreaseKeys)){return;} //validate pre condition

        HeapNode x = this.first; //link per Binomial Heap
        HeapNode y = heap2.first;
        HeapNode temp;
        if (x.key > y.key){
            temp =x; x = y; y = temp;}

        y.next = x.child;
        x.child = y;

        if(!this.lazyMelds){ // Need to do
        }
    }


    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return 46; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return 46; // should be replaced by student code
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
        while (x.key >1 && x.parent.key > x.key){
            HeapNode temp = x.parent;
            x.parent = x.child;
            x.child = temp;
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
            if (y.mark ==0){ y.mark++;}
            else{CascadingCut(y,y.parent);}
        }
    }


    //this is Successive Linking
    public HeapNode consolidate(HeapNode x){
       toBuckets(x);
       return fromBuckets();

    }
    public void toBuckets(HeapNode x){ //NEED TO WRITE
        for (int i  =0 ;  i < getLogOfSizeBaseGoldenRatio(); i++){

        }

    }
    public HeapNode fromBuckets(){return null;} //NEED TO WRITE

    public Double getLogOfSizeBaseGoldenRatio(){return Math.log(this.size())/Math.log(GoldenRation);}
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public int mark;


        public HeapNode(int key, String info) {
            this.key = key;
            this.info = info;
        }
    }
}
