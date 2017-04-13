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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map implementation with a smallest possible memory usage.
 * It should only be used for maps with small number of items
 * (e.g. &lt;30) since most operations have an O(n) complexity.
 * Thus it should be used in cases with large number of map
 * objects, each having only few items.
 * 
 * <p><code>null</code> is not supported for keys or values.</p>
 */
public class SmallMap<K, V> implements Map<K, V>
{
    /**
     * stores key-value pair as 2 objects; key first; in case of empty map this might be <code>null</code>
     */
    private Object[] mapArr;

    /** Creates empty map. */
    public SmallMap()
    {
    }
    
    /** Creates map filled with entries from provided map. */
    public SmallMap(Map<? extends K, ? extends V> initMap)
    {
        putAll(initMap);
    }
    
    /**
     * Returns index of key within map-array or <code>-1</code>
     * if key is not found (or key is <code>null</code>).
     */
    private int findKey(Object key)
    {
        if (isEmpty() || (key==null))
        {
            return -1;
        }
        
        for ( int aIdx = 0; aIdx < mapArr.length; aIdx+=2 )
        {
            if (key.equals(mapArr[aIdx]))
            {
                return aIdx;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns index of value within map-array or <code>-1</code>
     * if value is not found (or value is <code>null</code>).
     */
    private int findValue(Object value)
    {
        if (isEmpty() || (value==null))
        {
            return -1;
        }
        
        for ( int aIdx = 1; aIdx < mapArr.length; aIdx+=2 )
        {
            if (value.equals(mapArr[aIdx]))
            {
                return aIdx;
            }
        }
        
        return -1;
    }
    
    @Override
    public int size()
    {
        return mapArr == null ? 0 : mapArr.length >> 1;
    }

    @Override
    public boolean isEmpty()
    {
        return (mapArr == null) || (mapArr.length == 0);
    }

    @Override
    public boolean containsKey(Object key)
    {
        return findKey(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value)
    {
        return findValue(value) >= 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key)
    {
        int kIdx = findKey(key);
        
        return kIdx < 0 ? null : (V) mapArr[kIdx+1];
    }

    @Override
    public V put(K key, V value)
    {
        if ((key == null) || (value == null))
        {
            throw new NullPointerException( "Key or value must not be null.");
        }
        
        if (mapArr == null)
        {
            mapArr = new Object[] { key, value };
            return null;
        }
        else
        {
            int kIdx = findKey(key);
            
            if (kIdx < 0)
            {
                // key unknown
                int oldLen = mapArr.length;
                Object[] newMapArr = new Object[oldLen+2];
                System.arraycopy(mapArr, 0, newMapArr, 0, oldLen);
                newMapArr[oldLen] = key;
                newMapArr[oldLen+1] = value;
                mapArr = newMapArr;
                return null;
            }
            else
            {
                // key exists; replace value
                @SuppressWarnings("unchecked")
                V oldValue = (V) mapArr[kIdx+1];
                mapArr[kIdx+1] = value;
                return oldValue;
            }
        }
    }

    @Override
    public V remove(Object key)
    {
        int kIdx = findKey(key);
        
        if (kIdx < 0)
        {
            // not found
            return null;
        }

        @SuppressWarnings("unchecked")
        V oldValue = (V) mapArr[kIdx+1];
        int oldLen = mapArr.length;
        
        if (oldLen == 2)
        {
            // was last entry
            mapArr = null;
        }
        else
        {
            Object[] newMapArr = new Object[oldLen-2];
            System.arraycopy(mapArr, 0, newMapArr, 0, kIdx);
            System.arraycopy(mapArr, kIdx+2, newMapArr, kIdx, oldLen - kIdx - 2);
            mapArr = newMapArr;
        }
        
        return oldValue;
    }

    @Override
    public final void putAll(Map<? extends K, ? extends V> otherMap)
    {
        if ((mapArr == null) || (mapArr.length == 0))
        {
            // existing map is empty
            mapArr = new Object[otherMap.size() << 1];
            int aIdx = 0;
            for (Entry<? extends K, ? extends V> entry : otherMap.entrySet())
            {
                if ((entry.getKey() == null) || (entry.getValue() == null))
                {
                    throw new NullPointerException( "Key or value must not be null.");
                }
                
                mapArr[aIdx++] = entry.getKey();
                mapArr[aIdx++] = entry.getValue();
            }
        }
        else
        {
            int oldLen = mapArr.length;
            // first increase array size to hold all to put entries as if they have unknown keys
            // reduce after adding all to the required size
            Object[] newMapArr = new Object[oldLen+(otherMap.size() << 1)];
            System.arraycopy(mapArr, 0, newMapArr, 0, oldLen);
            
            int newIdx = oldLen;
            for (Entry<? extends K, ? extends V> entry : otherMap.entrySet())
            {
                if ((entry.getKey() == null) || (entry.getValue() == null))
                {
                    throw new NullPointerException( "Key or value must not be null.");
                }
                
                int existKeyIdx = findKey(entry.getKey());
                
                if (existKeyIdx >= 0)
                {
                    // existing key
                    newMapArr[existKeyIdx+1] = entry.getValue();
                }
                else
                {
                    // new key
                    newMapArr[newIdx++] = entry.getKey();
                    newMapArr[newIdx++] = entry.getValue();
                }
            }

            if (newIdx < newMapArr.length)
            {
                Object[] reducedMapArr = new Object[newIdx];
                System.arraycopy(newMapArr, 0, reducedMapArr, 0, newIdx);
                newMapArr = reducedMapArr;
            }
            
            mapArr = newMapArr;
        }
    }

    @Override
    public void clear()
    {
        mapArr = null;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * <p>The current implementation does not allow changes to the
     * returned key set (which would have to be reflected in the
     * underlying map.</p>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet()
    {
        if (isEmpty())
        {
            return Collections.emptySet();
        }
        
        Set<K> keys = new LinkedHashSet<>();
        for (int kIdx = 0; kIdx < mapArr.length; kIdx+=2)
        {
            keys.add((K)mapArr[kIdx]);
        }
        return Collections.unmodifiableSet( keys );
    }

    /**
     * Returns a collection of the values contained in this map.
     * 
     * <p>The current implementation does not allow changes to the
     * returned collection (which would have to be reflected in the
     * underlying map.</p>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values()
    {
        if (isEmpty())
        {
            return Collections.emptySet();
        }
        
        List<V> values = new ArrayList<>(mapArr.length >> 1);
        for (int vIdx = 1; vIdx < mapArr.length; vIdx+=2)
        {
            values.add((V)mapArr[vIdx]);
        }
        return Collections.unmodifiableList( values );
    }

    private class SmallMapEntry implements Entry<K, V>
    {
        private final int keyIdx;
        
        SmallMapEntry(int keyInMapIdx)
        {
            keyIdx = keyInMapIdx;
        }

        @SuppressWarnings("unchecked")
        @Override
        public K getKey()
        {
            return (K)mapArr[keyIdx];
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValue()
        {
            return (V)mapArr[keyIdx+1];
        }

        @Override
        public V setValue(V value)
        {
            if (value == null)
            {
                throw new NullPointerException( "Key or value must not be null.");
            }

            V oldValue = getValue();
            mapArr[keyIdx+1] = value;
            return oldValue;
        }
        
        @Override
        public int hashCode()
        {
            return getKey().hashCode();
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SmallMap.SmallMapEntry))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            SmallMapEntry other = (SmallMapEntry) obj;
            
            return getKey().equals(other.getKey()) && getValue().equals(other.getValue());
        }
    }
    
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        if (isEmpty())
        {
            return Collections.emptySet();
        }
        
        Set<java.util.Map.Entry<K, V>> entries = new LinkedHashSet<>();
        for (int kIdx = 0; kIdx < mapArr.length; kIdx+=2)
        {
            entries.add(new SmallMapEntry(kIdx));
        }
        return Collections.unmodifiableSet( entries );
    }

}
