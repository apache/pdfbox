/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.util;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class provides an iterative (bottom-up) implementation of the
 * <a href="https://en.wikipedia.org/wiki/Merge_sort">MergeSort</a> algorithm for any generic Java
 * object which implements a {@link Comparator}.
 *
 * <p>
 * This implementation uses an iterative implementation approach over the more classical recursive
 * approach in order to save the auxiliary space required by the call stack in recursive
 * implementations.
 * </p>
 *
 * Complexity:
 * <ul>
 *     <li>Worst case time: O(n log n)</li>
 *     <li>Best case time: O(n log n)</li>
 *     <li>Average case time: O(n log n)</li>
 *     <li>Space: O(n log n)</li>
 * </ul>
 * 
 * @author Alistair Oldfield
 *
 */
public final class IterativeMergeSort
{
    private IterativeMergeSort()
    {
        // utility class
    }
    
    /**
     * Sorts this list according to the order induced by the specified
     * {@link Comparator}.
     * 
     * @param  <T> the class of the objects in the list
     * @param  list the list to be sorted.
     * @param  cmp the comparator to determine the order of the list.
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    public static <T> void sort(List<T> list, Comparator<? super T> cmp)
    {

        if (list.size() < 2)
        {
            return;
        }
        Object[] arr = list.toArray();
        iterativeMergeSort(arr, (Comparator) cmp);

        ListIterator<T> i = list.listIterator();
        for (Object e : arr)
        {
            i.next();
            i.set((T) e);
        }
    }

    /**
     * Sorts the array using iterative (bottom-up) merge sort.
     *
     * @param <T> the class of the objects in the list
     * @param arr the array of objects to be sorted.
     * @param cmp the comparator to determine the order of the list.
     */
    private static <T> void iterativeMergeSort(T[] arr, Comparator<? super T> cmp)
    {

        T[] aux = arr.clone();

        for (int blockSize = 1; blockSize < arr.length; blockSize = (blockSize << 1))
        {
            for (int start = 0; start < arr.length; start += (blockSize << 1))
            {
                merge(arr, aux, start, start + blockSize, start + (blockSize << 1), cmp);
            }
        }
    }

    /**
     * Merges two sorted subarrays arr and aux into the order specified by cmp and places the
     * ordered result back into into arr array.
     *
     * @param <T>
     * @param arr Array containing source data to be sorted and target for destination data
     * @param aux Array containing copy of source data to be sorted
     * @param from Start index of left data run so that Left run is arr[from : mid-1].
     * @param mid End index of left data run and start index of right run data so that Left run is
     * arr[from : mid-1] and Right run is arr[mid : to]
     * @param to End index of right run data so that Right run is arr[mid : to]
     * @param cmp the comparator to determine the order of the list.
     */
    private static <T> void merge(T[] arr, T[] aux, int from, int mid, int to, Comparator<? super T> cmp)
    {
        if (mid >= arr.length)
        {
            return;
        }
        if (to > arr.length)
        {
            to = arr.length;
        }
        int i = from;
        int j = mid;
        for (int k = from; k < to; k++)
        {
            if (i == mid)
            {
                aux[k] = arr[j++];
            }
            else if (j == to)
            {
                aux[k] = arr[i++];
            }
            else if (cmp.compare(arr[j], arr[i]) < 0)
            {
                aux[k] = arr[j++];
            }
            else
            {
                aux[k] = arr[i++];
            }
        }
        System.arraycopy(aux, from, arr, from, to - from);
    }
}
