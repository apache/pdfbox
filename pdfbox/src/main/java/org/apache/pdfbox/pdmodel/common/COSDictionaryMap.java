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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * This is a Map that will automatically sync the contents to a COSDictionary.
 *
 * @author Ben Litchfield
 */
public class COSDictionaryMap<K,V> implements Map<K,V>
{
    private final COSDictionary map;
    private final Map<K,V> actuals;

    /**
     * Constructor for this map.
     *
     * @param actualsMap The map with standard java objects as values.
     * @param dicMap The map with COSBase objects as values.
     */
    public COSDictionaryMap( Map<K,V> actualsMap, COSDictionary dicMap )
    {
        actuals = actualsMap;
        map = dicMap;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return map.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key)
    {
        return actuals.containsKey( key );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value)
    {
        return actuals.containsValue( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object key)
    {
        return actuals.get( key );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value)
    {
        COSObjectable object = (COSObjectable)value;

        map.setItem( COSName.getPDFName( (String)key ), object.getCOSObject() );
        return actuals.put( key, value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(Object key)
    {
        map.removeItem( COSName.getPDFName( (String)key ) );
        return actuals.remove( key );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> t)
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        map.clear();
        actuals.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet()
    {
        return actuals.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values()
    {
        return actuals.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        return Collections.unmodifiableSet(actuals.entrySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        boolean retval = false;
        if( o instanceof COSDictionaryMap )
        {
            COSDictionaryMap<K,V> other = (COSDictionaryMap)o;
            retval = other.map.equals( this.map );
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return actuals.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return map.hashCode();
    }

    /**
     * This will take a map&lt;java.lang.String,org.apache.pdfbox.pdmodel.COSObjectable&gt;
     * and convert it into a COSDictionary.
     *
     * @param someMap A map containing COSObjectables
     *
     * @return A proper COSDictionary
     */
    public static COSDictionary convert(Map<String, ?> someMap)
    {
        COSDictionary dic = new COSDictionary();
        for (Entry<String, ?> entry : someMap.entrySet())
        {
            String name = entry.getKey();
            COSObjectable object = (COSObjectable) entry.getValue();
            dic.setItem( COSName.getPDFName( name ), object.getCOSObject() );
        }
        return dic;
    }

    /**
     * This will take a COS dictionary and convert it into COSDictionaryMap.  All cos
     * objects will be converted to their primitive form.
     *
     * @param map The COS mappings.
     * @return A standard java map.
     * @throws IOException If there is an error during the conversion.
     */
    public static COSDictionaryMap<String, Object> convertBasicTypesToMap( COSDictionary map ) throws IOException
    {
        COSDictionaryMap<String, Object> retval = null;
        if( map != null )
        {
            Map<String, Object> actualMap = new HashMap<>();
            for( COSName key : map.keySet() )
            {
                COSBase cosObj = map.getDictionaryObject( key );
                Object actualObject = null;
                if( cosObj instanceof COSString )
                {
                    actualObject = ((COSString)cosObj).getString();
                }
                else if( cosObj instanceof COSInteger )
                {
                    actualObject = ((COSInteger)cosObj).intValue();
                }
                else if( cosObj instanceof COSName )
                {
                    actualObject = ((COSName)cosObj).getName();
                }
                else if( cosObj instanceof COSFloat )
                {
                    actualObject = ((COSFloat)cosObj).floatValue();
                }
                else if( cosObj instanceof COSBoolean )
                {
                    actualObject = ((COSBoolean)cosObj).getValue() ? Boolean.TRUE : Boolean.FALSE;
                }
                else
                {
                    throw new IOException( "Error:unknown type of object to convert:" + cosObj );
                }
                actualMap.put( key.getName(), actualObject );
            }
            retval = new COSDictionaryMap<>( actualMap, map );
        }

        return retval;
    }
}
