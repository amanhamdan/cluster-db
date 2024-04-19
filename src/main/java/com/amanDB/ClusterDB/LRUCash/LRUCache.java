package com.amanDB.ClusterDB.LRUCash;

import com.amanDB.ClusterDB.Indexing.BTree;
import lombok.Synchronized;

import java.util.HashMap;


public class LRUCache {
    private final HashMap<String, LinkedListNode> map;
    private final int capacity;
    private int count;
    private final LinkedListNode head;
    private final LinkedListNode tail;

    private final static LRUCache lruCache = new LRUCache(20);

    public static LRUCache getCache() {
        return lruCache;
    }

    private LRUCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<>();
        head = new LinkedListNode("", null);
        tail = new LinkedListNode("", null);
        head.next = tail;
        tail.pre = head;
        head.pre = null;
        tail.next = null;
        count = 0;
    }

    @Synchronized
    public void deleteNode(LinkedListNode node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    @Synchronized
    public void addToHead(LinkedListNode node) {
        node.next = head.next;
        node.next.pre = node;
        node.pre = head;
        head.next = node;
    }

    // This method works in O(1)
    @Synchronized
    public BTree<String , String> get(String key) {
        if (map.get(key) != null) {
            LinkedListNode node = map.get(key);
            BTree<String, String> result = node.value;
            deleteNode(node);
            addToHead(node);
            System.out.println("Got the value : " + result
                    + " for the key: " + key);
            return result;
        }
        System.out.println("Did not get any value"
                + " for the key: " + key);
        return null;
    }

    // This method works in O(1)
    @Synchronized
    public void set(String key, BTree<String, String> value) {
        System.out.println("Going to set the (key, "
                + "value) : (" + key + ", "
                + value + ")");
        if (map.get(key) != null) {
            LinkedListNode node = map.get(key);
            node.value = value;
            deleteNode(node);
            addToHead(node);
        } else {
            LinkedListNode node = new LinkedListNode(key, value);
            map.put(key, node);
            if (count < capacity) {
                count++;
                addToHead(node);
            } else {
                map.remove(tail.pre.key);
                deleteNode(tail.pre);
                addToHead(node);
            }
        }
    }

    static class LinkedListNode {
        String key;
        BTree<String, String> value;
        LinkedListNode pre;
        LinkedListNode next;

        public LinkedListNode(String key, BTree<String, String> value) {
            this.key = key;
            this.value = value;
        }
    }
}
