/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.common;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSBoolean;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSString;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This is a Map that will automatically sync the contents to a COSDictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class COSDictionaryMap implements Map
{
    private COSDictionary map;
    private Map actuals;

    /**
     * Constructor for this map.
     *
     * @param actualsMap The map with standard java objects as values.
     * @param dicMap The map with COSBase objects as values.
     */
    public COSDictionaryMap( Map actualsMap, COSDictionary dicMap )
    {
        actuals = actualsMap;
        map = dicMap;
    }


    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return map.size();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(Object key)
    {
        return map.keyList().contains( key );
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(Object value)
    {
        return actuals.containsValue( value );
    }

    /**
     * {@inheritDoc}
     */
    public Object get(Object key)
    {
        return actuals.get( key );
    }

    /**
     * {@inheritDoc}
     */
    public Object put(Object key, Object value)
    {
        COSObjectable object = (COSObjectable)value;

        map.setItem( COSName.getPDFName( (String)key ), object.getCOSObject() );
        return actuals.put( key, value );
    }

    /**
     * {@inheritDoc}
     */
    public Object remove(Object key)
    {
        map.removeItem( COSName.getPDFName( (String)key ) );
        return actuals.remove( key );
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(Map t)
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        map.clear();
        actuals.clear();
    }

    /**
     * {@inheritDoc}
     */
    public Set keySet()
    {
        return actuals.keySet();
    }

    /**
     * {@inheritDoc}
     */
    public Collection values()
    {
        return actuals.values();
    }

    /**
     * {@inheritDoc}
     */
    public Set entrySet()
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
        boolean retval = false;
        if( o instanceof COSDictionaryMap )
        {
            COSDictionaryMap other = (COSDictionaryMap)o;
            retval = other.map.equals( this.map );
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return actuals.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return map.hashCode();
    }

    /**
     * This will take a map&lt;java.lang.String,org.pdfbox.pdmodel.COSObjectable&gt;
     * and convert it into a COSDictionary&lt;COSName,COSBase&gt;.
     *
     * @param someMap A map containing COSObjectables
     *
     * @return A proper COSDictionary
     */
    public static COSDictionary convert( Map someMap )
    {
        Iterator iter = someMap.keySet().iterator();
        COSDictionary dic = new COSDictionary();
        while( iter.hasNext() )
        {
            String name = (String)iter.next();
            COSObjectable object = (COSObjectable)someMap.get( name );
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
    public static COSDictionaryMap convertBasicTypesToMap( COSDictionary map ) throws IOException
    {
        COSDictionaryMap retval = null;
        if( map != null )
        {
            Map actualMap = new HashMap();
            Iterator keyIter = map.keyList().iterator();
            while( keyIter.hasNext() )
            {
                COSName key = (COSName)keyIter.next();
                COSBase cosObj = map.getDictionaryObject( key );
                Object actualObject = null;
                if( cosObj instanceof COSString )
                {
                    actualObject = ((COSString)cosObj).getString();
                }
                else if( cosObj instanceof COSInteger )
                {
                    actualObject = new Integer( ((COSInteger)cosObj).intValue() );
                }
                else if( cosObj instanceof COSName )
                {
                    actualObject = ((COSName)cosObj).getName();
                }
                else if( cosObj instanceof COSFloat )
                {
                    actualObject = new Float( ((COSInteger)cosObj).floatValue() );
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
            retval = new COSDictionaryMap( actualMap, map );
        }
        
        return retval;
    }
}