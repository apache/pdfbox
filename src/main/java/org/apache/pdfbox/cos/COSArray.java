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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;



import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * An array of PDFBase objects as part of the PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.24 $
 */
public class COSArray extends COSBase
{
    private List objects = new ArrayList();

    /**
     * Constructor.
     */
    public COSArray()
    {
        //default constructor
    }

    /**
     * This will add an object to the array.
     *
     * @param object The object to add to the array.
     */
    public void add( COSBase object )
    {
        objects.add( object );
    }

    /**
     * This will add an object to the array.
     *
     * @param object The object to add to the array.
     */
    public void add( COSObjectable object )
    {
        objects.add( object.getCOSObject() );
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
    }

    /**
     * This will remove all of the objects in the collection.
     */
    public void clear()
    {
        objects.clear();
    }

    /**
     * This will remove all of the objects in the collection.
     *
     * @param objectsList The list of objects to remove from the collection.
     */
    public void removeAll( Collection objectsList )
    {
        objects.removeAll( objectsList );
    }

    /**
     * This will retain all of the objects in the collection.
     *
     * @param objectsList The list of objects to retain from the collection.
     */
    public void retainAll( Collection objectsList )
    {
        objects.retainAll( objectsList );
    }

    /**
     * This will add an object to the array.
     *
     * @param objectsList The object to add to the array.
     */
    public void addAll( Collection objectsList )
    {
        objects.addAll( objectsList );
    }

    /**
     * This will add all objects to this array.
     *
     * @param objectList The objects to add.
     */
    public void addAll( COSArray objectList )
    {
        if( objectList != null )
        {
            objects.addAll( objectList.objects );
        }
    }

    /**
     * Add the specified object at the ith location and push the rest to the
     * right.
     *
     * @param i The index to add at.
     * @param objectList The object to add at that index.
     */
    public void addAll( int i, Collection objectList )
    {
        objects.addAll( i, objectList );
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
    }

    /**
     * This will set an object at a specific index.
     *
     * @param index zero based index into array.
     * @param intVal The object to set.
     */
    public void set( int index, int intVal )
    {
        objects.set( index, new COSInteger( intVal ) );
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
        Object obj = objects.get( index );
        if( obj instanceof COSObject )
        {
            obj = ((COSObject)obj).getObject();
        }
        if( obj instanceof COSNull )
        {
            obj = null;
        }
        return (COSBase)obj;
    }

    /**
     * This will get an object from the array.  This will NOT derefernce
     * the COS object.
     *
     * @param index The index into the array to get the object.
     *
     * @return The object at the requested index.
     */
    public COSBase get( int index )
    {
        return (COSBase)objects.get( index );
    }

    /**
     * Get the value of the array as an integer.
     *
     * @param index The index into the list.
     *
     * @return The value at that index or -1 if it is null.
     */
    public int getInt( int index )
    {
        return getInt( index, -1 );
    }

    /**
     * Get the value of the array as an integer, return the default if it does
     * not exist.
     *
     * @param index The value of the array.
     * @param defaultValue The value to return if the value is null.
     * @return The value at the index or the defaultValue.
     */
    public int getInt( int index, int defaultValue )
    {
        int retval = defaultValue;
        if( defaultValue < size() )
        {
            COSNumber number = (COSNumber)get( index );
            if( number != null )
            {
                retval = number.intValue();
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
        set( index, new COSInteger( value ) );
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
            COSName name = (COSName)get( index );
            if( name != null )
            {
                retval = name.getName();
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
        set( index, new COSString( string ) );
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
            COSString string = (COSString)get( index );
            if( string != null )
            {
                retval = string.getString();
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
        return (COSBase)objects.remove( i );
    }

    /**
     * This will remove an element from the array.
     *
     * @param o The object to remove.
     *
     * @return The object that was removed.
     */
    public boolean remove( COSBase o )
    {
        return objects.remove( o );
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "COSArray{" + objects + "}";
    }

    /**
     * Get access to the list.
     *
     * @return an iterator over the array elements
     */
    public Iterator iterator()
    {
        return objects.iterator();
    }

    /**
     * This will return the index of the entry or -1 if it is not found.
     *
     * @param object The object to search for.
     * @return The index of the object or -1.
     */
    public int indexOf( COSBase object )
    {
        int retval = -1;
        for( int i=0; retval < 0 && i<size(); i++ )
        {
            if( get( i ).equals( object ) )
            {
                retval = i;
            }
        }
        return retval;
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
        }
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public Object accept(ICOSVisitor visitor) throws COSVisitorException
    {
        return visitor.visitFromArray(this);
    }

    /**
     * This will take an COSArray of numbers and convert it to a float[].
     *
     * @return This COSArray as an array of float numbers.
     */
    public float[] toFloatArray()
    {
        float[] retval = new float[size()];
        for( int i=0; i<size(); i++ )
        {
            retval[i] = ((COSNumber)getObject( i )).floatValue();
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
        for( int i=0; i<value.length; i++ )
        {
            add( new COSFloat( value[i] ) );
        }
    }
    
    /*
	Return contents of COSArray as a Java List
    */
    public List toList()
    {
	    ArrayList retList = new ArrayList(size());
	    for (int i = 0; i < size(); i++)
		retList.add(get(i));
	    
	    return retList;
    }
}
