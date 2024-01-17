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
package org.apache.pdfbox.cos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.DateConverter;
import org.apache.pdfbox.util.SmallMap;

/**
 * This class represents a dictionary where name/value pairs reside.
 *
 * @author Ben Litchfield
 * 
 */
public class COSDictionary extends COSBase implements COSUpdateInfo
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(COSDictionary.class);

    private static final String PATH_SEPARATOR = "/";
    private static final int MAP_THRESHOLD = 1000;

    /**
     * The name-value pairs of this dictionary. The pairs are kept in the order they were added to the dictionary.
     */
    protected Map<COSName, COSBase> items = new SmallMap<>();
    private final COSUpdateState updateState;

    /**
     * Constructor.
     */
    public COSDictionary()
    {
        updateState = new COSUpdateState(this);
    }

    /**
     * Copy Constructor. This will make a shallow copy of this dictionary.
     *
     * @param dict The dictionary to copy.
     */
    public COSDictionary(COSDictionary dict)
    {
        updateState = new COSUpdateState(this);
        addAll(dict);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     *
     * @param value The value to find in the map.
     *
     * @return true if the map contains this value.
     */
    public boolean containsValue(Object value)
    {
        boolean contains = items.containsValue(value);
        if (!contains && value instanceof COSObject)
        {
            contains = items.containsValue(((COSObject) value).getObject());
        }
        return contains;
    }

    /**
     * Search in the map for the value that matches the parameter and return the first key that maps to that value.
     *
     * @param value The value to search for in the map.
     * @return The key for the value in the map or null if it does not exist.
     */
    public COSName getKeyForValue(Object value)
    {
        for (Map.Entry<COSName, COSBase> entry : items.entrySet())
        {
            Object nextValue = entry.getValue();
            if (nextValue.equals(value)
                    || (nextValue instanceof COSObject && ((COSObject) nextValue).getObject()
                            .equals(value)))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * This will return the number of elements in this dictionary.
     *
     * @return The number of elements in the dictionary.
     */
    public int size()
    {
        return items.size();
    }

    /**
     * This will clear all items in the map.
     */
    public void clear()
    {
        items.clear();
        getUpdateState().update();
    }

    /**
     * This will get an object from this dictionary. If the object is a reference then it will dereference it and get it
     * from the document. If the object is COSNull then null will be returned.
     *
     * @param key The key to the object that we are getting.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject(String key)
    {
        return getDictionaryObject(COSName.getPDFName(key));
    }

    /**
     * This is a special case of getDictionaryObject that takes multiple keys, it will handle the situation where
     * multiple keys could get the same value, ie if either CS or ColorSpace is used to get the colorspace. This will
     * get an object from this dictionary. If the object is a reference then it will dereference it and get it from the
     * document. If the object is COSNull then null will be returned.
     *
     * @param firstKey The first key to try.
     * @param secondKey The second key to try.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject(COSName firstKey, COSName secondKey)
    {
        COSBase retval = getDictionaryObject(firstKey);
        if (retval == null && secondKey != null)
        {
            retval = getDictionaryObject(secondKey);
        }
        return retval;
    }

    /**
     * This will get an object from this dictionary. If the object is a reference then it will dereference it and get it
     * from the document. If the object is COSNull then null will be returned.
     *
     * @param key The key to the object that we are getting.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject(COSName key)
    {
        COSBase retval = items.get(key);
        if (retval instanceof COSObject)
        {
            retval = ((COSObject) retval).getObject();
        }
        if (retval instanceof COSNull)
        {
            retval = null;
        }
        return retval;
    }

    /**
     * This will set an item in the dictionary. If value is null then the result will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem(COSName key, COSBase value)
    {
        if (value == null)
        {
            removeItem(key);
        }
        else
        {
            if (items instanceof SmallMap && items.size() >= MAP_THRESHOLD)
            {
                items = new LinkedHashMap<>(items);
            }
            items.put(key, value);
            getUpdateState().update(value);
        }
    }

    /**
     * This will set an item in the dictionary. If value is null then the result will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem(COSName key, COSObjectable value)
    {
        COSBase base = null;
        if (value != null)
        {
            base = value.getCOSObject();
        }
        setItem(key, base);
    }

    /**
     * This will set an item in the dictionary. If value is null then the result will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem(String key, COSObjectable value)
    {
        setItem(COSName.getPDFName(key), value);
    }

    /**
     * This will set an item in the dictionary.
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setBoolean(String key, boolean value)
    {
        setItem(COSName.getPDFName(key), COSBoolean.getBoolean(value));
    }

    /**
     * This will set an item in the dictionary.
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setBoolean(COSName key, boolean value)
    {
        setItem(key, COSBoolean.getBoolean(value));
    }

    /**
     * This will set an item in the dictionary. If value is null then the result will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
    public void setItem(String key, COSBase value)
    {
        setItem(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSName object. If it is null then the object will
     * be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setName(String key, String value)
    {
        setName(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSName object. If it is null then the object will
     * be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setName(COSName key, String value)
    {
        COSName name = null;
        if (value != null)
        {
            name = COSName.getPDFName(value);
        }
        setItem(key, name);
    }

    /**
     * Set the value of a date entry in the dictionary.
     *
     * @param key The key to the date value.
     * @param date The date value.
     */
    public void setDate(String key, Calendar date)
    {
        setDate(COSName.getPDFName(key), date);
    }

    /**
     * Set the date object.
     *
     * @param key The key to the date.
     * @param date The date to set.
     */
    public void setDate(COSName key, Calendar date)
    {
        setString(key, DateConverter.toString(date));
    }

    /**
     * Set the date object.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the date.
     * @param date The date to set.
     */
    public void setEmbeddedDate(COSName embedded, COSName key, Calendar date)
    {
        COSDictionary dic = getCOSDictionary(embedded);
        if (dic == null && date != null)
        {
            dic = new COSDictionary();
            setItem(embedded, dic);
        }
        if (dic != null)
        {
            dic.setDate(key, date);
        }
    }

    /**
     * This is a convenience method that will convert the value to a COSString object. If it is null then the object
     * will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setString(String key, String value)
    {
        setString(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSString object. If it is null then the object
     * will be removed.
     *
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setString(COSName key, String value)
    {
        COSString name = null;
        if (value != null)
        {
            name = new COSString(value);
        }
        setItem(key, name);
    }

    /**
     * This is a convenience method that will convert the value to a COSString object. If it is null then the object
     * will be removed.
     *
     * @param embedded The embedded dictionary to set the item in.
     * @param key The key to the object,
     * @param value The string value for the name.
     */
    public void setEmbeddedString(COSName embedded, COSName key, String value)
    {
        COSDictionary dic = getCOSDictionary(embedded);
        if (dic == null && value != null)
        {
            dic = new COSDictionary();
            setItem(embedded, dic);
        }
        if (dic != null)
        {
            dic.setString(key, value);
        }
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setInt(String key, int value)
    {
        setInt(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setInt(COSName key, int value)
    {
        setItem(key, COSInteger.get(value));
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setLong(String key, long value)
    {
        setLong(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setLong(COSName key, long value)
    {
        COSInteger intVal = COSInteger.get(value);
        setItem(key, intVal);
    }

    /**
     * This is a convenience method that will convert the value to a COSInteger object.
     *
     * @param embeddedDictionary The embedded dictionary.
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setEmbeddedInt(COSName embeddedDictionary, COSName key, int value)
    {
        COSDictionary embedded = getCOSDictionary(embeddedDictionary);
        if (embedded == null)
        {
            embedded = new COSDictionary();
            setItem(embeddedDictionary, embedded);
        }
        embedded.setInt(key, value);
    }

    /**
     * This is a convenience method that will convert the value to a COSFloat object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setFloat(String key, float value)
    {
        setFloat(COSName.getPDFName(key), value);
    }

    /**
     * This is a convenience method that will convert the value to a COSFloat object.
     *
     * @param key The key to the object,
     * @param value The int value for the name.
     */
    public void setFloat(COSName key, float value)
    {
        COSFloat fltVal = new COSFloat(value);
        setItem(key, fltVal);
    }

    /**
     * Sets the given boolean value at bitPos in the flags.
     *
     * @param field The COSName of the field to set the value into.
     * @param bitFlag the bit position to set the value in.
     * @param value the value the bit position should have.
     */
    public void setFlag(COSName field, int bitFlag, boolean value)
    {
        int currentFlags = getInt(field, 0);
        if (value)
        {
            currentFlags = currentFlags | bitFlag;
        }
        else
        {
            currentFlags &= ~bitFlag;
        }
        setInt(field, currentFlags);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name. Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The COS name.
     */
    public COSName getCOSName(COSName key)
    {
        COSBase name = getDictionaryObject(key);
        if (name instanceof COSName)
        {
            return (COSName) name;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSObject. Null is
     * returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The COSObject.
     */
    public COSObject getCOSObject(COSName key)
    {
        COSBase object = getItem(key);
        if (object instanceof COSObject)
        {
            return (COSObject) object;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSDictionary. Null is
     * returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The COSDictionary.
     */
    public COSDictionary getCOSDictionary(COSName key)
    {
        COSBase dictionary = getDictionaryObject(key);
        if (dictionary instanceof COSDictionary)
        {
            return (COSDictionary) dictionary;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSDictionary. Null is
     * returned if the entry does not exist in the dictionary.
     *
     * @param firstKey The first key to the item in the dictionary.
     * @param secondKey The second key to the item in the dictionary.
     * @return The COSDictionary.
     */
    public COSDictionary getCOSDictionary(COSName firstKey, COSName secondKey)
    {
        COSBase dictionary = getDictionaryObject(firstKey, secondKey);
        if (dictionary instanceof COSDictionary)
        {
            return (COSDictionary) dictionary;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSStream. Null is
     * returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The COSStream.
     */
    public COSStream getCOSStream(COSName key)
    {
        COSBase base = getDictionaryObject(key);
        if (base instanceof COSStream)
        {
            return (COSStream) base;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSArray. Null is
     * returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The COSArray.
     */
    public COSArray getCOSArray(COSName key)
    {
        COSBase array = getDictionaryObject(key);
        if (array instanceof COSArray)
        {
            return (COSArray) array;
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name. Default is
     * returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The COS name.
     */
    public COSName getCOSName(COSName key, COSName defaultValue)
    {
        COSBase name = getDictionaryObject(key);
        if (name instanceof COSName)
        {
            return (COSName) name;
        }
        return defaultValue;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getNameAsString(String key)
    {
        return getNameAsString(COSName.getPDFName(key));
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getNameAsString(COSName key)
    {
        String retval = null;
        COSBase name = getDictionaryObject(key);
        if (name instanceof COSName)
        {
            retval = ((COSName) name).getName();
        }
        else if (name instanceof COSString)
        {
            retval = ((COSString) name).getString();
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The name converted to a string.
     */
    public String getNameAsString(String key, String defaultValue)
    {
        return getNameAsString(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The name converted to a string.
     */
    public String getNameAsString(COSName key, String defaultValue)
    {
        String retval = getNameAsString(key);
        if (retval == null)
        {
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a
     * string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getString(String key)
    {
        return getString(COSName.getPDFName(key));
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a
     * string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getString(COSName key)
    {
        String retval = null;
        COSBase value = getDictionaryObject(key);
        if (value instanceof COSString)
        {
            retval = ((COSString) value).getString();
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a
     * string.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getString(String key, String defaultValue)
    {
        return getString(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a
     * string.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getString(COSName key, String defaultValue)
    {
        String retval = getString(key);
        if (retval == null)
        {
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public String getEmbeddedString(COSName embedded, COSName key)
    {
        return getEmbeddedString(embedded, key, null);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string.
     *
     * @param embedded The embedded dictionary.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return.
     * @return The name converted to a string.
     */
    public String getEmbeddedString(COSName embedded, COSName key, String defaultValue)
    {
        COSDictionary eDic = getCOSDictionary(embedded);
        return eDic != null ? eDic.getString(key, defaultValue) : defaultValue;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary or if the date was invalid.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a date.
     */
    public Calendar getDate(String key)
    {
        return getDate(COSName.getPDFName(key));
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary or if the date was invalid.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a date.
     */
    public Calendar getDate(COSName key)
    {
        COSBase base = getDictionaryObject(key);
        if (base instanceof COSString)
        {
            return DateConverter.toCalendar((COSString) base);
        }
        return null;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a date.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return if the entry does not exist in the dictionary or if the date was invalid.
     * @return The name converted to a date.
     */
    public Calendar getDate(String key, Calendar defaultValue)
    {
        return getDate(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a date.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return if the entry does not exist in the dictionary or if the date was invalid.
     * @return The name converted to a date.
     */
    public Calendar getDate(COSName key, Calendar defaultValue)
    {
        Calendar retval = getDate(key);
        if (retval == null)
        {
            retval = defaultValue;
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a name and convert it to
     * a string. Null is returned if the entry does not exist in the dictionary.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     */
    public Calendar getEmbeddedDate(COSName embedded, COSName key)
    {
        return getEmbeddedDate(embedded, key, null);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a date.
     *
     * @param embedded The embedded dictionary to get.
     * @param key The key to the item in the dictionary.
     * @param defaultValue The default value to return if the entry does not exist in the dictionary or if the date was
     * invalid.
     * @return The name converted to a string.
     */
    public Calendar getEmbeddedDate(COSName embedded, COSName key, Calendar defaultValue)
    {
        COSDictionary eDic = getCOSDictionary(embedded);
        return eDic != null ? eDic.getDate(key, defaultValue) : defaultValue;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a cos boolean and convert
     * it to a primitive boolean.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value returned if the entry is null.
     *
     * @return The value converted to a boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSBoolean and convert
     * it to a primitive boolean.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value returned if the entry is null.
     *
     * @return The entry converted to a boolean.
     */
    public boolean getBoolean(COSName key, boolean defaultValue)
    {
        return getBoolean(key, null, defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a COSBoolean and convert
     * it to a primitive boolean.
     *
     * @param firstKey The first key to the item in the dictionary.
     * @param secondKey The second key to the item in the dictionary.
     * @param defaultValue The value returned if the entry is null.
     *
     * @return The entry converted to a boolean.
     */
    public boolean getBoolean(COSName firstKey, COSName secondKey, boolean defaultValue)
    {
        boolean retval = defaultValue;
        COSBase bool = getDictionaryObject(firstKey, secondKey);
        if (bool instanceof COSBoolean)
        {
            retval = bool == COSBoolean.TRUE;
        }
        return retval;
    }

    /**
     * Get an integer from an embedded dictionary. Useful for 1-1 mappings. default:-1
     *
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     *
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt(COSName embeddedDictionary, COSName key)
    {
        return getEmbeddedInt(embeddedDictionary, key, -1);
    }

    /**
     * Get an integer from an embedded dictionary. Useful for 1-1 mappings.
     *
     * @param embeddedDictionary The name of the embedded dictionary.
     * @param key The key in the embedded dictionary.
     * @param defaultValue The value if there is no embedded dictionary or it does not contain the key.
     *
     * @return The value of the embedded integer.
     */
    public int getEmbeddedInt(COSName embeddedDictionary, COSName key, int defaultValue)
    {
        COSDictionary embedded = getCOSDictionary(embeddedDictionary);
        return embedded != null ? embedded.getInt(key, defaultValue) : defaultValue;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an int. -1 is returned if
     * there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The integer value.
     */
    public int getInt(String key)
    {
        return getInt(COSName.getPDFName(key), -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an int. -1 is returned if
     * there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The integer value..
     */
    public int getInt(COSName key)
    {
        return getInt(key, -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(String key, int defaultValue)
    {
        return getInt(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the defaultvalue will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(COSName key, int defaultValue)
    {
        return getInt(key, null, defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the default value -1 will be returned.
     *
     * @param firstKey The first key to the item in the dictionary.
     * @param secondKey The second key to the item in the dictionary.
     * @return The integer value.
     */
    public int getInt(COSName firstKey, COSName secondKey)
    {
        return getInt(firstKey, secondKey, -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param firstKey The first key to the item in the dictionary.
     * @param secondKey The second key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(COSName firstKey, COSName secondKey, int defaultValue)
    {
        int retval = defaultValue;
        COSBase obj = getDictionaryObject(firstKey, secondKey);
        if (obj instanceof COSNumber)
        {
            retval = ((COSNumber) obj).intValue();
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an long. -1 is returned
     * if there is no value.
     *
     * @param key The key to the item in the dictionary.
     *
     * @return The long value.
     */
    public long getLong(String key)
    {
        return getLong(COSName.getPDFName(key), -1L);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an long. -1 is returned
     * if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The long value.
     */
    public long getLong(COSName key)
    {
        return getLong(key, -1L);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public long getLong(String key, long defaultValue)
    {
        return getLong(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an integer. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public long getLong(COSName key, long defaultValue)
    {
        long retval = defaultValue;
        COSBase obj = getDictionaryObject(key);
        if (obj instanceof COSNumber)
        {
            retval = ((COSNumber) obj).longValue();
        }
        return retval;
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an float. -1 is returned
     * if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The float value.
     */
    public float getFloat(String key)
    {
        return getFloat(COSName.getPDFName(key), -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an float. -1 is returned
     * if there is no value.
     *
     * @param key The key to the item in the dictionary.
     * @return The float value.
     */
    public float getFloat(COSName key)
    {
        return getFloat(key, -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be a float. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The float value.
     */
    public float getFloat(String key, float defaultValue)
    {
        return getFloat(COSName.getPDFName(key), defaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that is expected to be an float. If the
     * dictionary value is null then the default value will be returned.
     *
     * @param key The key to the item in the dictionary.
     * @param defaultValue The value to return if the dictionary item is null.
     * @return The float value.
     */
    public float getFloat(COSName key, float defaultValue)
    {
        float retval = defaultValue;
        COSBase obj = getDictionaryObject(key);
        if (obj instanceof COSNumber)
        {
            retval = ((COSNumber) obj).floatValue();
        }
        return retval;
    }

    /**
     * Gets the boolean value from the flags at the given bit position.
     *
     * @param field The COSName of the field to get the flag from.
     * @param bitFlag the bitPosition to get the value from.
     *
     * @return true if the number at bitPos is '1'
     */
    public boolean getFlag(COSName field, int bitFlag)
    {
        int ff = getInt(field, 0);
        return (ff & bitFlag) == bitFlag;
    }

    /**
     * This will remove an item for the dictionary. This will do nothing of the object does not exist.
     *
     * @param key The key to the item to remove from the dictionary.
     */
    public void removeItem(COSName key)
    {
        items.remove(key);
        getUpdateState().update();
    }

    /**
     * This will do a lookup into the dictionary.
     *
     * @param key The key to the object.
     *
     * @return The item that matches the key.
     */
    public COSBase getItem(COSName key)
    {
        return items.get(key);
    }

    /**
     * This will do a lookup into the dictionary.
     * 
     * @param key The key to the object.
     *
     * @return The item that matches the key.
     */
    public COSBase getItem(String key)
    {
        return getItem(COSName.getPDFName(key));
    }

    /**
     * This is a special case of getItem that takes multiple keys, it will handle the situation
     * where multiple keys could get the same value, ie if either CS or ColorSpace is used to get
     * the colorspace. This will get an object from this dictionary.
     *
     * @param firstKey The first key to try.
     * @param secondKey The second key to try.
     *
     * @return The object that matches the key.
     */
    public COSBase getItem(COSName firstKey, COSName secondKey)
    {
        COSBase retval = getItem(firstKey);
        if (retval == null && secondKey != null)
        {
            retval = getItem(secondKey);
        }
        return retval;
    }
    
    /**
     * Returns the names of the entries in this dictionary. The returned set is in the order the entries were added to
     * the dictionary.
     *
     * @since Apache PDFBox 1.1.0
     * @return names of the entries in this dictionary
     */
    public Set<COSName> keySet()
    {
        return items.keySet();
    }

    /**
     * Returns the name-value entries in this dictionary. The returned set is in the order the entries were added to the
     * dictionary.
     *
     * @since Apache PDFBox 1.1.0
     * @return name-value entries in this dictionary
     */
    public Set<Map.Entry<COSName, COSBase>> entrySet()
    {
        return items.entrySet();
    }

    /**
     * Convenience method that calls {@link Map#forEach(java.util.function.BiConsumer) Map.forEach(BiConsumer)}.
     *
     * @param action The action to be performed for each entry
     * 
     */
    public void forEach(BiConsumer<? super COSName, ? super COSBase> action)
    {
        items.forEach(action);
    }

    /**
     * This will get all of the values for the dictionary.
     *
     * @return All the values for the dictionary.
     */
    public Collection<COSBase> getValues()
    {
        return items.values();
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If there is an error visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromDictionary(this);
    }

    /**
     * This will add all of the dictionaries keys/values to this dictionary. Existing key/value pairs will be
     * overwritten.
     *
     * @param dict The dictionaries to get the key/value pairs from.
     */
    public void addAll(COSDictionary dict)
    {
        if (items instanceof SmallMap && items.size() + dict.items.size() >= MAP_THRESHOLD)
        {
            items = new LinkedHashMap<>(items);
        }
        items.putAll(dict.items);
    }

    /**
     * @see java.util.Map#containsKey(Object)
     *
     * @param name The key to find in the map.
     * @return true if the map contains this key.
     */
    public boolean containsKey(COSName name)
    {
        return this.items.containsKey(name);
    }

    /**
     * @see java.util.Map#containsKey(Object)
     *
     * @param name The key to find in the map.
     * @return true if the map contains this key.
     */
    public boolean containsKey(String name)
    {
        return containsKey(COSName.getPDFName(name));
    }

    /**
     * Nice method, gives you every object you want Arrays works properly too. Try "P/Annots/[k]/Rect" where k means the
     * index of the Annots array.
     *
     * @param objPath the relative path to the object.
     * @return the object
     */
    public COSBase getObjectFromPath(String objPath)
    {
        String[] path = objPath.split(PATH_SEPARATOR);
        COSBase retval = this;
        for (String pathString : path)
        {
            if (retval instanceof COSArray)
            {
                int idx = Integer.parseInt(pathString.replace("\\[", "").replace("\\]", ""));
                retval = ((COSArray) retval).getObject(idx);
            }
            else if (retval instanceof COSDictionary)
            {
                retval = ((COSDictionary) retval).getDictionaryObject(pathString);
            }
        }
        return retval;
    }

    /**
     * Returns an unmodifiable view of this dictionary.
     * 
     * @return an unmodifiable view of this dictionary
     */
    public COSDictionary asUnmodifiableDictionary()
    {
        return new UnmodifiableCOSDictionary(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        try
        {
            return getDictionaryString(this, new ArrayList<>());
        }
        catch (IOException e)
        {
            LOG.debug("An exception occurred trying - returning error message instead", e);
            return "COSDictionary{" + e.getMessage() + "}";
        }
    }

    private static String getDictionaryString(COSBase base, List<COSBase> objs) throws IOException
    {
        if (base == null)
        {
            return "null";
        }
        if (objs.contains(base))
        {
            // avoid endless recursion
            return "hash:" + base.hashCode();
        }
        objs.add(base);
        if (base instanceof COSDictionary)
        {
            StringBuilder sb = new StringBuilder("COSDictionary{");
            for (Map.Entry<COSName, COSBase> x : ((COSDictionary) base).entrySet())
            {
                sb.append(x.getKey());
                sb.append(":");
                sb.append(getDictionaryString(x.getValue(), objs));
                sb.append(";");
            }
            sb.append("}");
            if (base instanceof COSStream)
            {
                try (InputStream stream = ((COSStream) base).createRawInputStream())
                {
                    byte[] b = IOUtils.toByteArray(stream);
                    sb.append("COSStream{").append(Arrays.hashCode(b)).append("}");
                }
            }
            return sb.toString();
        }
        if (base instanceof COSArray)
        {
            StringBuilder sb = new StringBuilder("COSArray{");
            for (COSBase x : (COSArray) base)
            {
                sb.append(getDictionaryString(x, objs));
                sb.append(";");
            }
            sb.append("}");
            return sb.toString();
        }
        if (base instanceof COSObject)
        {
            COSObject obj = (COSObject) base;
            return "COSObject{"
                    + getDictionaryString(
                            obj.isObjectNull() ? COSNull.NULL : obj.getObject(), objs)
                    + "}";
        }
        return base.toString();
    }
    
    /**
     * Returns the current {@link COSUpdateState} of this {@link COSDictionary}.
     *
     * @return The current {@link COSUpdateState} of this {@link COSDictionary}.
     * @see COSUpdateState
     */
    @Override
    public COSUpdateState getUpdateState()
    {
        return updateState;
    }

    /**
     * Collects all indirect objects numbers within this dictionary and all included dictionaries. It is used to avoid
     * mixed up object numbers when importing an existing page to another pdf.
     * 
     * Expert use only. You might run into an endless recursion if choosing a wrong starting point.
     * 
     * @param indirectObjects a list of already found indirect objects.
     * 
     */
    public void getIndirectObjectKeys(List<COSObjectKey> indirectObjects)
    {
        if (indirectObjects == null)
        {
            return;
        }
        COSObjectKey key = getKey();
        if (key != null)
        {
            // avoid endless recursions
            if (indirectObjects.contains(key))
            {
                return;
            }
            else
            {
                indirectObjects.add(key);
            }
        }
        for (Entry<COSName, COSBase> entry : items.entrySet())
        {
            COSBase cosBase = entry.getValue();
            COSObjectKey cosBaseKey = cosBase != null ? cosBase.getKey() : null;
            // avoid endless recursions
            if (COSName.PARENT.equals(entry.getKey())
                    || (cosBaseKey != null && indirectObjects.contains(cosBaseKey)))
            {
                continue;
            }
            if (cosBase instanceof COSObject)
            {
                // dereference object
                cosBase = ((COSObject) cosBase).getObject();
            }
            if (cosBase instanceof COSDictionary)
            {
                // descend to included dictionary to collect all included indirect objects
                ((COSDictionary) cosBase).getIndirectObjectKeys(indirectObjects);
            }
            else if (cosBase instanceof COSArray)
            {
                // descend to included array to collect all included indirect objects
                ((COSArray) cosBase).getIndirectObjectKeys(indirectObjects);
            }
            else if (cosBaseKey != null)
            {
                // add key for all indirect objects other than COSDictionary/COSArray
                indirectObjects.add(cosBaseKey);
            }
        }
    }

}
