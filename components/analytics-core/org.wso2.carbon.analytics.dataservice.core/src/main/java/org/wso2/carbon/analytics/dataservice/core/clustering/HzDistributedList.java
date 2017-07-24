/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.dataservice.core.clustering;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

/**
 * This is a custom List implementation to support distributed Lists in Hazelcast cluster.
 */
public class HzDistributedList<E> implements List<E> {
    
    private static String HAZELCAST_LIST_KEY = "DAS_HAZELCAST_LIST_KEY";
    private final ConcurrentMap<Object, E> hzMap;

    public HzDistributedList(ConcurrentMap<Object, E> map) {
        this.hzMap = map;
    }

    private int getLastIndex() {
        int lastIndex = -1;
        for (Object key : this.hzMap.keySet()) {
            String tempKey = key.toString();
            int index = Integer.parseInt(tempKey.substring(HAZELCAST_LIST_KEY.length()));
            if (index > lastIndex) {
                lastIndex = index;
            }
        }
        return lastIndex;
    }

    @Override
    public int size() {
        return this.hzMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hzMap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return hzMap.containsValue(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new HzDistributedListIterator<>();
    }

    @Override
    public synchronized Object[] toArray() {
        Object[] objects = new Object[hzMap.size()];
        for (int i = 0; i < size(); i++) {
            objects[i] = get(i);
        }
        return objects;
    }

    @Override
    public synchronized boolean add(E o) {
        int lastIndex = this.getLastIndex() + 1;
        this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(lastIndex), o);
        return true;
    }

    @Override
    public synchronized boolean remove(Object o) {
        for (int i = 0; i < this.hzMap.size(); i++) {
            if (o == this.get(i) || o.equals(this.get(i))) {
                this.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public void clear() {
        this.hzMap.clear();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T[] toArray(T[] a) {
        // Make a new array of a's runtime type, but my contents:
        if (a.length < hzMap.size()) {
            return (T[]) Arrays.copyOf(toArray(), hzMap.size(), a.getClass());
        }
        System.arraycopy(this.toArray(), 0, a, 0, hzMap.size());
        if (a.length > hzMap.size()) {
            a[hzMap.size()] = null;
        }
        return a;
    }

    public E get(int id) {
        return this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(id));
    }

    @Override
    public synchronized E set(int index, E element) {
        E previousObject = this.get(index);
        this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(index), element);
        return previousObject;
    }

    @Override
    public synchronized void add(int index, E element) {
        for (int i = this.getLastIndex(); i <= index; i--) {
            this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(i), this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(i - 1)));
        }
        this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(index), element);
    }

    @Override
    public synchronized E remove(int index) {
        E removingObject;
        removingObject = this.get(index);
        int lastItem = 0;
        for (int i = index; i <= getLastIndex(); i++) {
            E value = this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(i + 1));
            if (value != null) {
                this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(i), value);
            }
            lastItem = i;
        }
        this.hzMap.remove(HAZELCAST_LIST_KEY + String.valueOf(lastItem));
        return removingObject;
    }

    @Override
    public synchronized int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size(); i++) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size(); i++) {
                if (o.equals(this.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size() - 1; i >= 0; i--) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = size() - 1; i >= 0; i--) {
                if (o.equals(this.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    public Object getFirst() {
        return this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(0));
    }


    /**
     * Iterator implementation.
     */
    private class HzDistributedListIterator<T> implements Iterator<T> {
        
        int position;       // index of next element to return

        public synchronized boolean hasNext() {
            return position < size();
        }

        @SuppressWarnings("unchecked")
        public synchronized T next() {
            if (this.position >= size()) {
                throw new NoSuchElementException();
            }
            T temp = (T) hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(this.position));
            this.position++;
            return temp;
        }

        public synchronized void remove() {
            if (this.position < 0) {
                throw new IllegalStateException();
            }
            try {
                HzDistributedList.this.remove(this.position);
                this.position--;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

    }
    
}
