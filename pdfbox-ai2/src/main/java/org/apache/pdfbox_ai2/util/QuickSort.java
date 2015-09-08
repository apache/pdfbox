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
package org.apache.pdfbox_ai2.util;

import java.util.Comparator;
import java.util.List;

/**
 * see http://de.wikipedia.org/wiki/Quicksort.
 * 
 * @author UWe Pachler
 */
public class QuickSort
{
    
    private QuickSort()
    {
    }
    
    private static final Comparator<? extends Comparable> objComp = new Comparator<Comparable>()
    {
        public int compare(Comparable object1, Comparable object2)
        {
            return object1.compareTo(object2);
        }
    };

    /**
     * Sorts the given list using the given comparator.
     * 
     * @param list list to be sorted
     * @param cmp comparator used to compare the object swithin the list
     */
    public static <T> void sort(List<T> list, Comparator<T> cmp)
    {
        int size = list.size();
        if (size < 2)
        {
            return;
        }
        quicksort(list, cmp, 0, size - 1);
    }

    /**
     * Sorts the given list using compareTo as comparator.
     * 
     * @param list list to be sorted
     */
    public static <T extends Comparable> void sort(List<T> list)
    {
        sort(list, (Comparator<T>) objComp);
    }

    private static <T> void quicksort(List<T> list, Comparator<T> cmp, int left, int right)
    {
        if (left < right)
        {
            int splitter = split(list, cmp, left, right);
            quicksort(list, cmp, left, splitter - 1);
            quicksort(list, cmp, splitter + 1, right);
        }
    }

    private static <T> void swap(List<T> list, int i, int j)
    {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    private static <T> int split(List<T> list, Comparator<T> cmp, int left, int right)
    {
        int i = left;
        int j = right - 1;
        T pivot = list.get(right);
        do
        {
            while (cmp.compare(list.get(i), pivot) <= 0 && i < right)
            {
                ++i;
            }
            while (cmp.compare(pivot, list.get(j)) <= 0 && j > left)
            {
                --j;
            }
            if (i < j)
            {
                swap(list, i, j);
            }

        } while (i < j);

        if (cmp.compare(pivot, list.get(i)) < 0)
        {
            swap(list, i, right);
        }
        return i;
    }
}
