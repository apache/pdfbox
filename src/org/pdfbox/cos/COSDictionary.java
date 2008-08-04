/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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
package org.pdfbox.cos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Iterator;

import org.pdfbox.exceptions.COSVisitorException;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.util.DateConverter;

/**
 * This class represents a dictionary where name/value pairs reside.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.32 $
 */
public class COSDictionary extends COSBase
{
    private static final String PATH_SEPARATOR = "/";
    
    /**
     * These are all of the items in the dictionary.
     */
    private Map items = new HashMap();

    /**
     * Used to store original sequence of keys, for testing.
     */
    private List keys = new ArrayList();

    /**
     * Constructor.
     */
    public COSDictionary()
    {
        //default constructor
    }

    /**
     * Copy Constructor.  This will make a shallow copy of this dictionary.
     *
     * @param dict The dictionary to copy.
     */
    public COSDictionary( COSDictionary dict )
    {
        items = new HashMap( dict.items );
        keys = new ArrayList( dict.keys );
    }
    
    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     * 
     * @param value The value to find in the map.
     * 
     * @return true if the map contains this value.
     */
    public boolean containsValue( Object value )
    {
        boolean contains = items.containsValue( value );
        if( !contains && value instanceof COSObject )
        {
            contains = items.containsValue( ((COSObject)value).getObject());
        }
        return contains;
    }
    
    /**
     * Search in the map for the value that matches the parameter
     * and return the first key that maps to that value.
     * 
     * @param value The value to search for in the map.
     * @return The key for the value in the map or null if it does not exist.
     */
    public COSName getKeyForValue( Object value )
    {
        COSName key = null;
        Iterator iter = items.entrySet().iterator();
        while( key == null && iter.hasNext() )
        {
            Map.Entry next = (Map.Entry)iter.next();
            Object nextValue = next.getValue();
            if( nextValue.equals( value ) ||
                (nextValue instanceof COSObject && 
                 ((COSObject)nextValue).getObject().equals( value))
                )
            {
                key = (COSName)next.getKey();
            }
        }
        
        return key;
    }

    /**
     * This will return the number of elements in this dictionary.
     *
     * @return The number of elements in the dictionary.
     */
    public int size()
    {
        return keys.size();
    }

    /**
     * This will clear all items in the map.
     */
    public void clear()
    {
        items.clear();
        keys.clear();
    }

    /**
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.
     *
     * @param key The key to the object that we are getting.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject( String key )
    {
        return getDictionaryObject( COSName.getPDFName( key ) );
    }
    
    /**
     * This is a special case of getDictionaryObject that takes multiple keys, it will handle
     * the situation where multiple keys could get the same value, ie if either CS or ColorSpace
     * is used to get the colorspace.
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.  
     *
     * @param firstKey The first key to try.
     * @param secondKey The second key to try.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject( String firstKey, String secondKey )
    {
        COSBase retval = getDictionaryObject( COSName.getPDFName( firstKey ) );
        if( retval == null )
        {
            retval = getDictionaryObject( COSName.getPDFName( secondKey ) );
        }
        return retval;
    }
    
    /**
     * This is a special case of getDictionaryObject that takes multiple keys, it will handle
     * the situation where multiple keys could get the same value, ie if either CS or ColorSpace
     * is used to get the colorspace.
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.  
     *
     * @param keyList The list of keys to find a value.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject( String[] keyList )
    {
        COSBase retval = null;
        for( int i=0; i<keyList.length && retval == null; i++ )
        {
            retval = getDictionaryObject( COSName.getPDFName( keyList[i] ) ); 
        }
        return retval;
    }

    /**
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.
     *
     * @param key The key to the object that we are getting.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject( COSName key )
    {
        COSBase retval = (COSBase)items.get( key );
        if( retval instanceof COSObject )
        {
            retval = ((COSObject)retval).getObject();
        }
        if( retval instanceof COSNull )
        {
            retval = null;
        }
        return retval;
    }

    /**
     * This will set an item in the dictionary.  If value is null then the result
     * will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem( COSName key, COSBase value )
    {
        if( value == null )
        {
            removeItem( key );
        }
        else
        {
            if (!items.containsKey(key))
            {
                // insert only if not already there
                keys.add(key);
            }
            items.put( key, value );
        }
    }

    /**
     * This will set an item in the dictionary.  If value is null then the result
     * will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem( COSName key, COSObjectable value )
    {
        COSBase base = null;
        if( value != null )
        {
            base = value.getCOSObject();
        }
        setItem( key, base );
    }

    /**
     * This will set an item in the dictionary.  If value is null then the result
     * will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem( String key, COSObjectable value )
    {
        setItem( COSName.getPDFName( key ), value );
    }

    /**
     * This will set an item in the dictionary.
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setBoolean( String key, boolean value )
    {
        setItem( COSName.getPDFName( key ), COSBoolean.getBoolean( value ) );
    }

    /**
     * This will set an item in the dictionary.
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setBoolean( COSName key, boolean value )
    {
        setItem( key , COSBoolean.getBoolean( value ) );
    }

    /**
     * This will set an item in the dictionary.  If value is null then the result
     * will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem( String key, COSBase value )
    {
        setItem( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSName
     * object.  If it is null then the object will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setName( String key, String value )
    {
        setName( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSName
     * object.  If it is null then the object will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setName( COSName key, String value )
    {
        COSName name = null;
        if( value != null )
        {
            name = COSName.getPDFName( value );
        }
        setItem( key, name );
    }
    
    /**
     * Set the value of a date entry in the dictionary.
     * 
     * @param key The key to the date value.
     * @param date The date value.
     */
    public void setDate( String key, Calendar date )
    {
        setDate( COSName.getPDFName( key ), date );
    }
    
    /**
     * Set the date object.
     * 
     * @param key The key to the date.
     * @param date The date to set.
     */
    public void setDate( COSName key, Calendar date )
    {
        setString( key, DateConverter.toString( date ) );
    }
    
    /**
     * Set the value of a date entry in the dictionary.
     * 
     * @param embedded The embedded dictionary.
     * @param key The key to the date value.
     * @param date The date value.
     */
    public void setEmbeddedDate( String embedded, String key, Calendar date )
    {
        setEmbeddedDate( embedded, COSName.getPDFName( key ), date );
    }
    
    /**
     * Set the date object.
     * 
     * @param embedded The embedded dictionary.
     * @param key The key to the date.
     * @param date The date to set.
     */
    public void setEmbeddedDate( String embedded, COSName key, Calendar date )
    {
        COSDictionary dic = (COSDictionary)getDictionaryObject( embedded );
        if( dic == null && date != null )
        {
            dic = new COSDictionary();
            setItem( embedded, dic );
        }
        if( dic != null )
        {
            dic.setDate( key, date );
        }
    }

    /**
     * This is a convenience method that will convert the value to a COSString
     * object.  If it is null then the object will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setString( String key, String value )
    {
        setString( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSString
     * object.  If it is null then the object will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setString( COSName key, String value )
    {
        COSString name = null;
        if( value != null )
        {
            name = new COSString( value );
        }
        setItem( key, name );
    }
    
    /**
     * This is a convenience method that will convert the value to a COSString
     * object.  If it is null then the object will be removed.
     *
     * @param embedded The embedded dictionary to set the item in.
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setEmbeddedString( String embedded, String key, String value )
    {
        setEmbeddedString( embedded, COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSString
     * object.  If it is null then the object will be removed.
     *
     * @param embedded The embedded dictionary to set the item in.
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setEmbeddedString( String embedded, COSName key, String value )
    {
        COSDictionary dic = (COSDictionary)getDictionaryObject( embedded );
        if( dic == null && value != null )
        {
            dic = new COSDictionary();
            setItem( embedded, dic );
        }
        if( dic != null )
        {
            dic.setString( key, value );
        }
    }
    
    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.  
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setInt( String key, int value )
    {
        setInt( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setInt( COSName key, int value )
    {
        COSInteger intVal = null;
        intVal = new COSInteger(value);
        setItem( key, intVal );
    }
    
    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.  
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setLong( String key, long value )
    {
        setLong( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setLong( COSName key, long value )
    {
        COSInteger intVal = null;
        intVal = new COSInteger(value);
        setItem( key, intVal );
    }
    
    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.  
     *
     * @param embeddedDictionary The embedded dictionary.
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setEmbeddedInt( String embeddedDictionary, String key, int value )
    {
        setEmbeddedInt( embeddedDictionary, COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger
     * object.
     *
     * @param embeddedDictionary The embedded dictionary.
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setEmbeddedInt( String embeddedDictionary, COSName key, int value )
    {
        COSDictionary embedded = (COSDictionary)getDictionaryObject( embeddedDictionary );
        if( embedded == null )
        {
            embedded = new COSDictionary();
            setItem( embeddedDictionary, embedded );
        }
        embedded.setInt( key, value );
    }
    
    /**
     * This is a convenience method that will convert the value to a COSFloat
     * object.  
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setFloat( String key, float value )
    {
        setFloat( COSName.getPDFName( key ), value );
    }

    /**
     * This is a convenience method that will convert the value to a COSFloat
     * object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setFloat( COSName key, float value )
    {
        COSFloat fltVal = new COSFloat( value );
        setItem( key, fltVal );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getNameAsString( String key )
    {
        return getNameAsString( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getNameAsString( COSName key )
    {
        String retval = null;
        COSName name = (COSName)getDictionaryObject( key );
        if( name != null )
        {
            retval = name.getName();
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The name converted to a string.
     */
    public String getNameAsString( String key, String defaultValue )
    {
        return getNameAsString( COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The name converted to a string.
     */
    public String getNameAsString( COSName key, String defaultValue )
    {
        String retval = getNameAsString( key );
        if( retval == null )
        {
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getString( String key )
    {
        return getString( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getString( COSName key )
    {
        String retval = null;
        COSString name = (COSString)getDictionaryObject( key );
        if( name != null )
        {
            retval = name.getString();
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getString( String key, String defaultValue )
    {
        return getString( COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getString( COSName key, String defaultValue )
    {
        String retval = getString( key );
        if( retval == null )
        {
            retval = defaultValue;
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getEmbeddedString( String embedded, String key )
    {
        return getEmbeddedString( embedded, COSName.getPDFName( key ), null );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getEmbeddedString( String embedded, COSName key )
    {
        return getEmbeddedString( embedded, key, null );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getEmbeddedString( String embedded, String key, String defaultValue )
    {
        return getEmbeddedString( embedded, COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getEmbeddedString( String embedded, COSName key, String defaultValue )
    {
        String retval = defaultValue;
        COSDictionary dic = (COSDictionary)getDictionaryObject( embedded );
        if( dic != null )
        {
            retval = dic.getString( key, defaultValue );
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getDate( String key ) throws IOException
    {
        return getDate( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     * 
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getDate( COSName key ) throws IOException
    {
        COSString date = (COSString)getDictionaryObject( key );
        return DateConverter.toCalendar( date );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a date.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getDate( String key, Calendar defaultValue ) throws IOException
    {
        return getDate( COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a date.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getDate( COSName key, Calendar defaultValue ) throws IOException
    {
        Calendar retval = getDate( key );
        if( retval == null )
        {
            retval = defaultValue;
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getEmbeddedDate( String embedded, String key ) throws IOException
    {
        return getEmbeddedDate( embedded, COSName.getPDFName( key ), null );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     * 
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getEmbeddedDate( String embedded, COSName key ) throws IOException
    {
        return getEmbeddedDate( embedded, key, null );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a date.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getEmbeddedDate( String embedded, String key, Calendar defaultValue ) throws IOException
    {
        return getEmbeddedDate( embedded, COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a date.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
    public Calendar getEmbeddedDate( String embedded, COSName key, Calendar defaultValue ) throws IOException
    {
        Calendar retval = defaultValue;
        COSDictionary eDic = (COSDictionary)getDictionaryObject( embedded );
        if( eDic != null )
        {
            retval = eDic.getDate( key, defaultValue );
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a cos boolean and convert it to a primitive boolean.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value returned if the entry is null.
     *
     * @return The value converted to a boolean.
     */
    public boolean getBoolean( String key, boolean defaultValue )
    {
        return getBoolean( COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a COSBoolean and convert it to a primitive boolean.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value returned if the entry is null.
     *
     * @return The entry converted to a boolean.
     */
    public boolean getBoolean( COSName key, boolean defaultValue )
    {
        boolean retval = defaultValue;
        COSBoolean bool = (COSBoolean)getDictionaryObject( key );
        if( bool != null )
        {
            retval = bool.getValue();
        }
        return retval;
    }
    
    /**
     * Get an integer from an embedded dictionary.  Useful for 1-1 mappings.  default:-1
     * 
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     * 
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt( String embeddedDictionary, String key )
    {
        return getEmbeddedInt( embeddedDictionary, COSName.getPDFName( key ) );
    }
    
    /**
     * Get an integer from an embedded dictionary.  Useful for 1-1 mappings.  default:-1
     * 
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     * 
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt( String embeddedDictionary, COSName key )
    {
        return getEmbeddedInt( embeddedDictionary, key, -1 );
    }
    
    /**
     * Get an integer from an embedded dictionary.  Useful for 1-1 mappings.
     * 
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     * @param defaultValue The value if there is no embedded dictionary or it does not contain the key.
     * 
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt( String embeddedDictionary, String key, int defaultValue )
    {
        return getEmbeddedInt( embeddedDictionary, COSName.getPDFName( key ), defaultValue );
    }
    
    
    /**
     * Get an integer from an embedded dictionary.  Useful for 1-1 mappings.
     * 
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     * @param defaultValue The value if there is no embedded dictionary or it does not contain the key.
     * 
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt( String embeddedDictionary, COSName key, int defaultValue )
    {
        int retval = defaultValue;
        COSDictionary embedded = (COSDictionary)getDictionaryObject( embeddedDictionary );
        if( embedded != null )
        {
            retval = embedded.getInt( key, defaultValue );
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an int.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The integer value.
     */
    public int getInt( String key )
    {
        return getInt( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an int.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The integer value..
     */
    public int getInt( COSName key )
    {
        return getInt( key, -1 );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param keyList The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt( String[] keyList, int defaultValue )
    {
        int retval = defaultValue;
        COSNumber obj = (COSNumber)getDictionaryObject( keyList );
        if( obj != null )
        {
            retval = obj.intValue();
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt( String key, int defaultValue )
    {
        return getInt( new String []{ key }, defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt( COSName key, int defaultValue )
    {
        return getInt(key.getName(), defaultValue );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an long.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * 
     * @return The long value.
     */
    public long getLong( String key )
    {
        return getLong( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an long.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The long value.
     */
    public long getLong( COSName key )
    {
        return getLong( key, -1L );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an long.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param keyList The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The long value.
     */
    public long getLong( String[] keyList, long defaultValue )
    {
        long retval = defaultValue;
        COSNumber obj = (COSNumber)getDictionaryObject( keyList );
        if( obj != null )
        {
            retval = obj.longValue();
        }
        return retval;
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public long getLong( String key, long defaultValue )
    {
        return getLong( new String []{ key }, defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public long getLong( COSName key, long defaultValue )
    {
        return getLong(key.getName(), defaultValue );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an int.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The float value.
     */
    public float getFloat( String key )
    {
        return getFloat( COSName.getPDFName( key ) );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an float.  -1 is returned if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The float value.
     */
    public float getFloat( COSName key )
    {
        return getFloat( key, -1 );
    }
    
    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a float.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The float value.
     */
    public float getFloat( String key, float defaultValue )
    {
        return getFloat( COSName.getPDFName( key ), defaultValue );
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an float.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The float value.
     */
    public float getFloat( COSName key, float defaultValue )
    {
        float retval = defaultValue;
        COSNumber obj = (COSNumber)getDictionaryObject( key );
        if( obj != null )
        {
            retval = obj.floatValue();
        }
        return retval;
    }

    /**
     * This will remove an item for the dictionary.  This
     * will do nothing of the object does not exist.
     *
     * @param key The key to the item to remove from the dictionary.
     */
    public void removeItem( COSName key )
    {
        keys.remove( key );
        items.remove( key );
    }

    /**
     * This will do a lookup into the dictionary.
     *
     * @param key The key to the object.
     *
     * @return The item that matches the key.
     */
    public COSBase getItem( COSName key )
    {
        return (COSBase)items.get( key );
    }
    
    



    /**
     * This will get the keys for all objects in the dictionary in the sequence that
     * they were added.
     *
     * @return a list of the keys in the sequence of insertion
     *
     */
    public List keyList()
    {
        return keys;
    }

    /**
     * This will get all of the values for the dictionary.
     *
     * @return All the values for the dictionary.
     */
    public Collection getValues()
    {
        return items.values();
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return The object that the visitor returns.
     *
     * @throws COSVisitorException If there is an error visiting this object.
     */
    public Object accept(ICOSVisitor  visitor) throws COSVisitorException
    {
        return visitor.visitFromDictionary(this);
    }

    /**
     * This will add all of the dictionarys keys/values to this dictionary.
     *
     * @param dic The dic to get the keys from.
     */
    public void addAll( COSDictionary dic )
    {
        Iterator dicKeys = dic.keyList().iterator();
        while( dicKeys.hasNext() )
        {
            COSName key = (COSName)dicKeys.next();
            COSBase value = dic.getItem( key );
            setItem( key, value );
        }
    }
    
    /**
     * This will add all of the dictionarys keys/values to this dictionary, but only
     * if they don't already exist.  If a key already exists in this dictionary then 
     * nothing is changed.
     *
     * @param dic The dic to get the keys from.
     */
    public void mergeInto( COSDictionary dic )
    {
        Iterator dicKeys = dic.keyList().iterator();
        while( dicKeys.hasNext() )
        {
            COSName key = (COSName)dicKeys.next();
            COSBase value = dic.getItem( key );
            if( getItem( key ) == null )
            {
                setItem( key, value );
            }
        }
    }
    
    /**
     * Nice method, gives you every object you want
     * Arrays works properly too. Try "P/Annots/[k]/Rect"
     * where k means the index of the Annotsarray.
     *
     * @param objPath the relative path to the object.
     * @return the object
     */
    public COSBase getObjectFromPath(String objPath) 
    {
        COSBase retval = null;
        String[] path = objPath.split(PATH_SEPARATOR);
        retval = this;

        for (int i = 0; i < path.length; i++)
        {
            if(retval instanceof COSArray)
            {
                int idx = new Integer(path[i].replaceAll("\\[","").replaceAll("\\]","")).intValue();
                retval = ((COSArray)retval).getObject(idx);
            }
            else if (retval instanceof COSDictionary)
            {
                retval = ((COSDictionary)retval).getDictionaryObject( path[i] );
            }
        }
        return retval;
    }

}