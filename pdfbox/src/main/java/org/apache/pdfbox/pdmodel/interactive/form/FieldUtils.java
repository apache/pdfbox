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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A set of utility methods to help with common AcroForm form and field related functions.
 */
public final class FieldUtils
{
    
    /**
     * An implementation of a basic key value pair.
     * 
     * This implementation is used to help sorting the content of
     * field option entries with an array of two-element arrays as
     * used by choice fields.
     * 
     */
    static class KeyValue
    {
        private final String key;
        private final String value;
        
        public KeyValue(final String theKey, final String theValue)
        {
            this.key = theKey;
            this.value = theValue;
        }
        
        public String getKey()
        {
            return this.key;
        }


        public String getValue()
        {
            return this.value;
        }
        
        public String toString()
        {
            return "(" + this.key + ", " + this.value + ")";
        }
    }
    
    /**
     * Comparator to sort KeyValue by key.
     */
    static class KeyValueKeyComparator implements Serializable, Comparator<KeyValue>
    {

        private static final long serialVersionUID = 6715364290007167694L;

        @Override
        public int compare(KeyValue o1, KeyValue o2)
        {
            return o1.key.compareTo(o2.key);
        }
    }

    /**
     * Comparator to sort KeyValue by value.
     */
    static class KeyValueValueComparator implements Serializable, Comparator<KeyValue>
    {

        private static final long serialVersionUID = -3984095679894798265L;

        @Override
        public int compare(KeyValue o1, KeyValue o2)
        {
            return o1.value.compareTo(o2.value);
        }
    }

    /**
     * Constructor.
     */
    private FieldUtils()
    {
    }
    
    /**
     * Return two related lists as a single list with key value pairs.
     * 
     * @param key the key elements
     * @param value the value elements
     * @return a sorted list of KeyValue elements.
     */
    static List<KeyValue> toKeyValueList(List<String> key, List<String> value)
    {
        List<KeyValue> list = new ArrayList<KeyValue>();
        for(int i =0; i<key.size(); i++)
        {
            list.add(new FieldUtils.KeyValue(key.get(i),value.get(i)));
        }
        return list;
    }    
    
    /**
     * Sort two related lists simultaneously by the elements in the key parameter.
     * 
     * @param pairs a list of KeyValue elements
     */
    static void sortByValue(List<KeyValue> pairs)
    {
        Collections.sort(pairs, new FieldUtils.KeyValueValueComparator());
    }

    /**
     * Sort two related lists simultaneously by the elements in the value parameter.
     * 
     * @param pairs a list of KeyValue elements
     */
    static void sortByKey(List<KeyValue> pairs)
    {
        Collections.sort(pairs, new FieldUtils.KeyValueKeyComparator());
    }
}
