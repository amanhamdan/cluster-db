package com.amanDB.ClusterDB.Indexing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;


public class BTree<Key extends Comparable<Key>, Value> implements Serializable {
    // max children per B-tree node = M-1
    // (must be even and greater than 2)
    private static final int MaxChildren = 4;

    private Node rootNode;
    private int treeHeight;
    private int MapCount;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    private static final class Node implements Serializable {
        private int NumberOfChildren;                             // number of children
        private Entry[] children = new Entry[MaxChildren];   // the array of children

        // create a node with k children
        private Node(int k) {
            NumberOfChildren = k;
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    private static class Entry implements Serializable {
        private Comparable key;
        private Object val;
        private Node next;     // helper field to iterate over array entries
        public Entry(Comparable key, Object val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTree() {
        rootNode = new Node(0);
    }

    /**
     * Returns true if this symbol table is empty.
     * @return {@code true} if this symbol table is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return MapCount;
    }

    /**
     * Returns the height of this B-tree (for debugging).
     *
     * @return the height of this B-tree
     */
    public int height() {
        return treeHeight;
    }


    /**
     * Returns the value associated with the given key.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in the symbol table
     *         and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        return search(rootNode, key, treeHeight);
    }

    private Value search(Node x, Key key, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.NumberOfChildren; j++) {
                if (eq(key, children[j].key)) return (Value) children[j].val;
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.NumberOfChildren; j++) {
                if (j+1 == x.NumberOfChildren || less(key, children[j+1].key))
                    return search(children[j].next, key, ht-1);
            }
        }
        return null;
    }


    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param  key the key
     * @param  val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("argument key to put() is null");
        Node u = insert(rootNode, key, val, treeHeight);
        MapCount++;
        if (u == null) return;

        // need to split root
        Node t = new Node(2);
        t.children[0] = new Entry(rootNode.children[0].key, null, rootNode);
        t.children[1] = new Entry(u.children[0].key, null, u);
        rootNode = t;
        treeHeight++;
    }

    private Node insert(Node h, Key key, Value val, int height) {
        int j;
        Entry t = new Entry(key, val, null);

        // external node
        if (height == 0) {
            for (j = 0; j < h.NumberOfChildren; j++) {
                if (less(key, h.children[j].key)) break;
            }
        }

        // internal node
        else {
            for (j = 0; j < h.NumberOfChildren; j++) {
                if ((j+1 == h.NumberOfChildren) || less(key, h.children[j+1].key)) {
                    Node u = insert(h.children[j++].next, key, val, height-1);
                    if (u == null) return null;
                    t.key = u.children[0].key;
                    t.val = null;
                    t.next = u;
                    break;
                }
            }
        }

        for (int i = h.NumberOfChildren; i > j; i--)
            h.children[i] = h.children[i-1];
        h.children[j] = t;
        h.NumberOfChildren++;

        if (h.NumberOfChildren < MaxChildren){
            return null;
        }
        else  {
            return split(h);
        }
    }

    private Node split(Node h) {
        Node t = new Node(MaxChildren/2);
        h.NumberOfChildren = MaxChildren/2;
        for (int j = 0; j < MaxChildren/2; j++)
            t.children[j] = h.children[MaxChildren/2+j];
        return t;
    }


    public String toString() {
        return toString(rootNode, treeHeight, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.children;

        if (ht == 0) {
            for (int j = 0; j < h.NumberOfChildren; j++) {
                s.append(indent + children[j].key + " " + children[j].val + "\n");
            }
        }
        else {
            for (int j = 0; j < h.NumberOfChildren; j++) {
                if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
                s.append(toString(children[j].next, ht-1, indent + "     "));
            }
        }
        return s.toString();
    }


    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }


    public static void main(String[] args) throws Exception {
        BTree<Integer, String> st = new BTree<Integer, String>();
        IndexingService service = new IndexingService();

        st.put(1,   "1 val");
        st.put(6,   "6 val");
        st.put(12,  "12 val");
        st.put(6,   "6 val");
        st.put(5,   "5 val");
        st.put(8,   "8 val");
        st.put(10,  "10 val");
        st.put(7,   "7 val");
        st.put(2,   "2 val");
        st.put(9,   "9 val");
        st.put(4,   "4 val");
        st.put(13,  "13 val");
        st.put(10,  "10 val");
        st.put(3,   "3 val");
        st.put(11,  "11 val");
        st.put(14,  "14 val");
        st.put(5,   "5 val");
        st.put(15,  "15 val");
        st.put(16,  "16 val");
        st.put(17,  "17 val");
        ObjectMapper objectMapper = new ObjectMapper();

        Gson gson = new Gson();
        String json = gson.toJson(st);
        System.out.println(json);
        Type empMapType = new TypeToken<BTree<Integer, String>>() {}.getType();
        //Object fromJson = gson.fromJson(json,  empMapType);
        Object fromMapper = objectMapper.readValue(json,Object.class);


        System.out.println("12 key: " + st.get(12));
        System.out.println("8 key: "  + st.get(8));
        System.out.println("11 key: " + st.get(11));
        System.out.println("9 key:  " + st.get(9 ));
        System.out.println("5 key:  " + st.get(5));
        System.out.println("10 key: " + st.get(10));
        System.out.println();

        System.out.println("size:    " + st.size());
        System.out.println("height:  " + st.height());
        System.out.println(st);
        System.out.println();
    }
    public void treeToJson(){

    }

// I should know how to save it and retrieve it.
}