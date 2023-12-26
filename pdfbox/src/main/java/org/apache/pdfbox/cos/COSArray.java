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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * An array of PDFBase objects as part of the PDF document.
 *
 * @author Ben Litchfield
 */
public class COSArray extends COSBase implements Iterable<COSBase>, COSUpdateInfo
{
    private final List<COSBase> objects = new ArrayList<>();
    private final COSUpdateState updateState;

    /**
     * Constructor.
     */
    public COSArray()
    {
        updateState = new COSUpdateState(this);
        setDirect(true);
    }

    /**
     * Use the given list to initialize the COSArray.
     * 
     * @param cosObjectables the initial list of COSObjectables
     */
    public COSArray(List<? extends COSObjectable> cosObjectables)
    {
        if (cosObjectables == null)
        {
            throw new IllegalArgumentException("List of COSObjectables cannot be null");
        }
        updateState = new COSUpdateState(this);
        setDirect(true);
        cosObjectables.forEach(cosObjectable ->
            objects.add(cosObjectable != null ? cosObjectable.getCOSObject() : null));
    }

    /**
     * This will add an object to the array.
     *
     * @param object The object to add to the array.
     */
    public void add( COSBase object )
    {
        objects.add( object );
        getUpdateState().update(object);
    }

    /**
     * This will add an object to the array.
     *
     * @param object The object to add to the array.
     */
    public void add( COSObjectable object )
    {
        COSBase base = null;
        if (object != null)
        {
            base = object.getCOSObject();
        }
        objects.add(base);
        getUpdateState().update(base);
    }

    /**
     * Add the specified object at the ith location and push the rest to the
     * right.
     *
     * @param i The index to add at.
     * @param object The object to add at that index.
     */
    public void add( int i, COSBase object)
    {
        objects.add( i, object );
        getUpdateState().update(object);
    }

    /**
     * This will remove all of the objects in the collection.
     */
    public void clear()
    {
        objects.clear();
        getUpdateState().update();
    }

    /**
     * This will remove all of the objects in the collection.
     *
     * @param objectsList The list of objects to remove from the collection.
     */
    public void removeAll( Collection<COSBase> objectsList )
    {
        objects.removeAll( objectsList );
        getUpdateState().update();
    }

    /**
     * This will retain all of the objects in the collection.
     *
     * @param objectsList The list of objects to retain from the collection.
     */
    public void retainAll( Collection<COSBase> objectsList )
    {
        if (objects.retainAll(objectsList))
        {
            getUpdateState().update();
        }
    }

    /**
     * This will add an object to the array.
     *
     * @param objectsList The object to add to the array.
     */
    public void addAll( Collection<COSBase> objectsList )
    {
        if (objects.addAll(objectsList))
        {
            getUpdateState().update(objectsList);
        }
    }

    /**
     * This will add all objects to this array.
     *
     * @param objectList The list of objects to add.
     */
    public void addAll( COSArray objectList )
    {
        if( objectList != null )
        {
            if (objects.addAll(objectList.objects))
            {
                getUpdateState().update(objectList);
            }
        }
    }

    /**
     * Add the specified object at the ith location and push the rest to the
     * right.
     *
     * @param i The index to add at.
     * @param objectList The object to add at that index.
     */
    public void addAll( int i, Collection<COSBase> objectList )
    {
        if (objects.addAll(i, objectList))
        {
            getUpdateState().update(objectList);
        }
    }

    /**
     * This will set an object at a specific index.
     *
     * @param index zero based index into array.
     * @param object The object to set.
     */
    public void set( int index, COSBase object )
    {
        objects.set( index, object );
        getUpdateState().update(object);
    }

    /**
     * This will set an object at a specific index.
     *
     * @param index zero based index into array.
     * @param intVal The object to set.
     */
    public void set( int index, int intVal )
    {
        objects.set( index, COSInteger.get(intVal));
        getUpdateState().update();
    }

    /**
     * This will set an object at a specific index.
     *
     * @param index zero based index into array.
     * @param object The object to set.
     */
    public void set( int index, COSObjectable object )
    {
        COSBase base = null;
        if( object != null )
        {
            base = object.getCOSObject();
        }
        objects.set( index, base );
        getUpdateState().update(base);
    }

    /**
     * This will get an object from the array.  This will dereference the object.
     * If the object is COSNull then null will be returned.
     *
     * @param index The index into the array to get the object.
     *
     * @return The object at the requested index.
     */
    public COSBase getObject( int index )
    {
        COSBase obj = objects.get( index );
        if( obj instanceof COSObject )
        {
            obj = ((COSObject)obj).getObject();
        }
        if (obj instanceof COSNull)
        {
            obj = null;
        }
        return obj;
    }

    /**
     * This will get an object from the array.  This will NOT dereference
     * the COS object.
     *
     * @param index The index into the array to get the object.
     *
     * @return The object at the requested index.
     */
    public COSBase get( int index )
    {
        return objects.get( index );
    }

    /**
     * Get the value of the array as an integer.
     *
     * @param index The index into the list.
     *
     * @return The value at that index or -1 if does not exist.
     */
    public int getInt( int index )
    {
        return getInt( index, -1 );
    }

    /**
     * Get the value of the array as an integer, return the default if it does not exist.
     *
     * @param index The value of the array.
     * @param defaultValue The value to return if the value is null.
     * @return The value at the index or the defaultValue.
     */
    public int getInt( int index, int defaultValue )
    {
        int retval = defaultValue;
        if ( index < size() )
        {
            Object obj = objects.get( index );
            if( obj instanceof COSNumber )
            {
                retval = ((COSNumber)obj).intValue();
            }
        }
        return retval;
    }

    /**
     * Set the value in the array as an integer.
     *
     * @param index The index into the array.
     * @param value The value to set.
     */
    public void setInt( int index, int value )
    {
        set( index, COSInteger.get( value ) );
    }

    /**
     * Set the value in the array as a name.
     * @param index The index into the array.
     * @param name The name to set in the array.
     */
    public void setName( int index, String name )
    {
        set( index, COSName.getPDFName( name ) );
    }

    /**
     * Get the value of the array as a string.
     *
     * @param index The index into the array.
     * @return The name converted to a string or null if it does not exist.
     */
    public String getName( int index )
    {
        return getName( index, null );
    }

    /**
     * Get an entry in the array that is expected to be a COSName.
     * @param index The index into the array.
     * @param defaultValue The value to return if it is null.
     * @return The value at the index or defaultValue if none is found.
     */
    public String getName( int index, String defaultValue )
    {
        String retval = defaultValue;
        if( index < size() )
        {
            Object obj = objects.get( index );
            if( obj instanceof COSName )
            {
                retval = ((COSName)obj).getName();
            }
        }
        return retval;
    }

    /**
     * Set the value in the array as a string.
     * @param index The index into the array.
     * @param string The string to set in the array.
     */
    public void setString( int index, String string )
    {
        if ( string != null )
        {
            set( index, new COSString( string ) );
        }
        else
        {
            set( index, null );
        }
    }   

    /**
     * Get the value of the array as a string.
     *
     * @param index The index into the array.
     * @return The string or null if it does not exist.
     */
    public String getString( int index )
    {
        return getString( index, null );
    }

    /**
     * Get an entry in the array that is expected to be a COSName.
     * @param index The index into the array.
     * @param defaultValue The value to return if it is null.
     * @return The value at the index or defaultValue if none is found.
     */
    public String getString( int index, String defaultValue )
    {
        String retval = defaultValue;
        if( index < size() )
        {
            Object obj = objects.get( index );
            if( obj instanceof COSString )
            {
                retval = ((COSString)obj).getString();
            }
        }
        return retval;
    }

    /**
     * This will get the size of this array.
     *
     * @return The number of elements in the array.
     */
    public int size()
    {
        return objects.size();
    }

    /**
     * This will remove an element from the array.
     *
     * @param i The index of the object to remove.
     *
     * @return The object that was removed.
     */
    public COSBase remove( int i )
    {
        COSBase removedEntry = objects.remove( i );
        getUpdateState().update();
        return removedEntry;
    }

    /**
     * This will remove an element from the array.
     *
     * @param o The object to remove.
     *
     * @return <code>true</code> if the object was removed, <code>false</code>
     *  otherwise
     */
    public boolean remove( COSBase o )
    {
        boolean removed = objects.remove(o);
        if (removed)
        {
            getUpdateState().update();
        }
        return removed;
    }

    /**
     * This will remove an element from the array.
     * This method will also remove a reference to the object.
     *
     * @param o The object to remove.
     * @return <code>true</code> if the object was removed, <code>false</code>
     *  otherwise
     */
    public boolean removeObject(COSBase o)
    {
        boolean removed = this.remove(o);
        if (!removed)
        {
            for (int i = 0; i < this.size(); i++)
            {
                COSBase entry = this.get(i);
                if (entry instanceof COSObject)
                {
                    COSObject objEntry = (COSObject) entry;
                    if (objEntry.getObject().equals(o))
                    {
                        return this.remove(entry);
                    }
                }
            }
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSArray{" + objects + "}";
    }

    /**
     * Get access to the list.
     *
     * @return an iterator over the array elements
     */
    @Override
    public Iterator<COSBase> iterator()
    {
        return objects.iterator();
    }

    /**
     * This will return the index of the entry or -1 if it is not found.
     *
     * @param object The object to search for.
     * @return The index of the object or -1.
     */
    public int indexOf(COSBase object)
    {
        for (int i = 0; i < size(); i++)
        {
            COSBase item = get(i);
            if (item == null)
            {
                if (object == null)
                {
                    return i;
                }
            }
            else if (item.equals(object))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * This will return the index of the entry or -1 if it is not found.
     * This method will also find references to indirect objects.
     *
     * @param object The object to search for.
     * @return The index of the object or -1.
     */
    public int indexOfObject(COSBase object)
    {
        for (int i = 0; i < this.size(); i++)
        {
            COSBase item = this.get(i);
            if (item == null)
            {
                if (item == object)
                {
                    return i;
                }
            }
            else if (item.equals(object)
                    || item instanceof COSObject && ((COSObject) item).getObject().equals(object))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * This will add null values until the size of the array is at least
     * as large as the parameter.  If the array is already larger than the
     * parameter then nothing is done.
     *
     * @param size The desired size of the array.
     */
    public void growToSize( int size )
    {
        growToSize( size, null );
    }

    /**
     * This will add the object until the size of the array is at least
     * as large as the parameter.  If the array is already larger than the
     * parameter then nothing is done.
     *
     * @param size The desired size of the array.
     * @param object The object to fill the array with.
     */
    public void growToSize( int size, COSBase object )
    {
        while( size() < size )
        {
            add( object );
            getUpdateState().update(object);
        }
        getUpdateState().update();
    }

    /**
     * Visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromArray(this);
    }

    /**
     * This will take an COSArray of numbers and convert it to a float[].
     *
     * @return This COSArray as an array of float numbers.
     */
    public float[] toFloatArray()
    {
        float[] retval = new float[size()];
        for (int i = 0; i < retval.length; i++)
        {
            COSBase base = getObject(i);
            retval[i] = base instanceof COSNumber ? ((COSNumber) base).floatValue() : 0;
        }
        return retval;
    }

    /**
     * Clear the current contents of the COSArray and set it with the float[].
     *
     * @param value The new value of the float array.
     */
    public void setFloatArray( float[] value )
    {
        this.clear();
        for (float aValue : value)
        {
            add(new COSFloat(aValue));
        }
    }

    /**
     *  Return contents of COSArray as a Java List.
     *
     *  @return the COSArray as List
     */
    public List<? extends COSBase> toList()
    {
        return new ArrayList<>(objects);
    }

    /**
     * This will return a list of names if the COSArray consists of COSNames only.
     * 
     * @return the list of names of the COSArray of COSNames
     */
    public List<String> toCOSNameStringList()
    {
        return objects.stream() //
                .map(o -> ((COSName) o).getName()) //
                .collect(Collectors.toList());
    }

    /**
     * This will return a list of names if the COSArray consists of COSStrings only.
     * 
     * @return the list of names of the COSArray of COSStrings
     */
    public List<String> toCOSStringStringList()
    {
        return objects.stream() //
                .map(o -> ((COSString) o).getString()) //
                .collect(Collectors.toList());
    }

    /**
     * This will return a list of float values if the COSArray consists of COSNumbers only.
     * 
     * @return the list of float values of the COSArray of COSNumbers
     */
    public List<Float> toCOSNumberFloatList()
    {
        List<Float> numbers = new ArrayList<>(size());
        for (int i = 0; i < size(); i++)
        {
            COSBase num = getObject(i);
            if (num instanceof COSNumber)
            {
                numbers.add(((COSNumber) num).floatValue());
            }
            else
            {
                numbers.add(null);
            }
        }
        return numbers;
    }

    /**
     * This will return a list of int values if the COSArray consists of COSNumbers only.
     * 
     * @return the list of int values of the COSArray of COSNumbers
     */
    public List<Integer> toCOSNumberIntegerList()
    {
        List<Integer> numbers = new ArrayList<>(size());
        for (int i = 0; i < size(); i++)
        {
            COSBase num = getObject(i);
            if (num instanceof COSNumber)
            {
                numbers.add(((COSNumber) num).intValue());
            }
            else
            {
                numbers.add(null);
            }
        }
        return numbers;
    }

    /**
     * This will take a list of integer objects and return a COSArray of COSInteger objects.
     *
     * @param integer A list of integers
     *
     * @return An array of COSInteger objects
     */
    public static COSArray ofCOSIntegers(List<Integer> integer)
    {
        COSArray retval = new COSArray();
        integer.forEach(s -> retval.add(COSInteger.get(s.longValue())));
        return retval;
    }

    /**
     * This will take a list of string objects and return a COSArray of COSName objects.
     *
     * @param strings A list of strings
     *
     * @return An array of COSName objects
     */
    public static COSArray ofCOSNames(List<String> strings)
    {
        COSArray retval = new COSArray();
        strings.forEach(s -> retval.add(COSName.getPDFName(s)));
        return retval;
    }

    /**
     * This will take a list of string objects and return a COSArray of COSName objects.
     *
     * @param strings A list of strings
     *
     * @return An array of COSName objects
     */
    public static COSArray ofCOSStrings(List<String> strings)
    {
        COSArray retval = new COSArray();
        strings.forEach(s -> retval.add(new COSString(s)));
        return retval;
    }
    
    /**
     * Returns the current {@link COSUpdateState} of this {@link COSArray}.
     *
     * @return The current {@link COSUpdateState} of this {@link COSArray}.
     * @see COSUpdateState
     */
    @Override
    public COSUpdateState getUpdateState()
    {
        return updateState;
    }

    /**
     * Collects all indirect objects numbers within this COSArray and all included dictionaries. It is used to avoid
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

        for (COSBase cosBase : objects)
        {
            if (cosBase == null)
            {
                continue;
            }
            COSObjectKey cosBaseKey = cosBase.getKey();
            if (cosBaseKey != null && indirectObjects.contains(cosBaseKey))
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
