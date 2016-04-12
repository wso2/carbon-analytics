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

    @SuppressWarnings({"unchecked", "NullableProblems", "SuspiciousMethodCalls"})
    @Override
    public boolean contains(Object o) {
        return hzMap.containsValue(o);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<E> iterator() {
        return new HzDistributedListIterator<>();
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public Object[] toArray() {
        synchronized (hzMap) {
            Object[] objects = new Object[hzMap.size()];
            for (int i = 0; i < size(); i++) {
                objects[i] = get(i);
            }
            return objects;
        }
    }

    @Override
    public boolean add(E o) {
        synchronized (hzMap) {
            int lastIndex = getLastIndex() + 1;
            this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(lastIndex), o);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (hzMap) {
            for (int i = 0; i < hzMap.size(); i++) {
                if (o == this.get(i) || o.equals(this.get(i))) {
                    this.remove(i);
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @Override
    public void clear() {
        this.hzMap.clear();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings({"unchecked", "NullableProblems", "SuspiciousSystemArraycopy"})
    @Override
    public <T> T[] toArray(T[] a) {
        // Make a new array of a's runtime type, but my contents:
        if (a.length < hzMap.size()) {
            return (T[]) Arrays.copyOf(toArray(), hzMap.size(), a.getClass());
        }
        System.arraycopy(toArray(), 0, a, 0, hzMap.size());
        if (a.length > hzMap.size()) {
            a[hzMap.size()] = null;
        }
        return a;
    }

    public E get(int id) {
        return this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(id));
    }

    @Override
    public E set(int index, E element) {
        E previousObject = this.get(index);
        this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(index), element);
        return previousObject;
    }

    @Override
    public void add(int index, E element) {
        for (int i = getLastIndex(); i <= index; i--) {
            this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(i), this.hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(i - 1)));
        }
        this.hzMap.put(HAZELCAST_LIST_KEY + String.valueOf(index), element);
    }

    @Override
    public E remove(int index) {
        synchronized (hzMap) {
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
    }

    @Override
    public int indexOf(Object o) {
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
    public int lastIndexOf(Object o) {
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

    @SuppressWarnings("NullableProblems")
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("This method is not implemented in Hazelcast distributed Lists.");
    }

    @SuppressWarnings("NullableProblems")
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

        public boolean hasNext() {
            return position < size();
        }

        @SuppressWarnings("unchecked")
        public T next() {
            synchronized (hzMap) {
                if (position >= size()) {
                    throw new NoSuchElementException();
                }
                T temp = (T) hzMap.get(HAZELCAST_LIST_KEY + String.valueOf(position));
                position++;
                return temp;
            }
        }

        public void remove() {
            if (position < 0) {
                throw new IllegalStateException();
            }
            try {
                HzDistributedList.this.remove(position);
                position--;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

    }
}
