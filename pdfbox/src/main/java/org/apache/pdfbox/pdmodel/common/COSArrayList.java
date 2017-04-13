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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.pdfbox.cos.COSObject;

/**
 * This is an implementation of a List that will sync its contents to a COSArray.
 *
 * @author Ben Litchfield
 */
public class COSArrayList<E> implements List<E>
{
    private final COSArray array;
    private final List<E> actual;

    private COSDictionary parentDict;
    private COSName dictKey;

    /**
     * Default constructor.
     */
    public COSArrayList()
    {
        array = new COSArray();
        actual = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param actualList The list of standard java objects
     * @param cosArray The COS array object to sync to.
     */
    public COSArrayList( List<E> actualList, COSArray cosArray )
    {
        actual = actualList;
        array = cosArray;
    }

    /**
     * This constructor is to be used if the array doesn't exist, but is to be created and added to
     * the parent dictionary as soon as the first element is added to the array.
     *
     * @param dictionary The dictionary that holds the item, and will hold the array if an item is
     * added.
     * @param dictionaryKey The key into the dictionary to set the item.
     */
    public COSArrayList(COSDictionary dictionary, COSName dictionaryKey)
    {
        array = new COSArray();
        actual = new ArrayList<>();
        parentDict = dictionary;
        dictKey = dictionaryKey;
    }

    /**
     * This is a really special constructor.  Sometimes the PDF spec says
     * that a dictionary entry can either be a single item or an array of those
     * items.  But in the PDModel interface we really just want to always return
     * a java.util.List.  In the case were we get the list and never modify it
     * we don't want to convert to COSArray and put one element, unless we append
     * to the list.  So here we are going to create this object with a single
     * item instead of a list, but allow more items to be added and then converted
     * to an array.
     *
     * @param actualObject The PDModel object.
     * @param item The COS Model object.
     * @param dictionary The dictionary that holds the item, and will hold the array if an item is added.
     * @param dictionaryKey The key into the dictionary to set the item.
     */
    public COSArrayList( E actualObject, COSBase item, COSDictionary dictionary, COSName dictionaryKey )
    {
        array = new COSArray();
        array.add( item );
        actual = new ArrayList<>();
        actual.add( actualObject );

        parentDict = dictionary;
        dictKey = dictionaryKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return actual.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return actual.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o)
    {
        return actual.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator()
    {
        return actual.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray()
    {
        return actual.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X>X[] toArray(X[] a)
    {
        return actual.toArray(a);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(E o)
    {
        //when adding if there is a parentDict then change the item
        //in the dictionary from a single item to an array.
        if( parentDict != null )
        {
            parentDict.setItem( dictKey, array );
            //clear the parent dict so it doesn't happen again, there might be
            //a usecase for keeping the parentDict around but not now.
            parentDict = null;
        }
        //string is a special case because we can't subclass to be COSObjectable
        if( o instanceof String )
        {
            array.add( new COSString( (String)o ) );
        }
        else
        {
            if(array != null)
            {
                array.add(((COSObjectable)o).getCOSObject());
            }
        }
        return actual.add(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o)
    {
        boolean retval = true;
        int index = actual.indexOf( o );
        if( index >= 0 )
        {
            actual.remove( index );
            array.remove( index );
        }
        else
        {
            retval = false;
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return actual.containsAll( c );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        //when adding if there is a parentDict then change the item
        //in the dictionary from a single item to an array.
        if( parentDict != null && c.size() > 0)
        {
            parentDict.setItem( dictKey, array );
            //clear the parent dict so it doesn't happen again, there might be
            //a usecase for keeping the parentDict around but not now.
            parentDict = null;
        }
        array.addAll( toCOSObjectList( c ) );
        return actual.addAll( c );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        //when adding if there is a parentDict then change the item
        //in the dictionary from a single item to an array.
        if( parentDict != null && c.size() > 0)
        {
            parentDict.setItem( dictKey, array );
            //clear the parent dict so it doesn't happen again, there might be
            //a usecase for keeping the parentDict around but not now.
            parentDict = null;
        }

        array.addAll( index, toCOSObjectList( c ) );
        return actual.addAll( index, c );
    }

    /**
     * This will take an array of COSNumbers and return a COSArrayList of
     * java.lang.Integer values.
     *
     * @param intArray The existing integer Array.
     *
     * @return A list that is part of the core Java collections.
     */
    public static List<Integer> convertIntegerCOSArrayToList(COSArray intArray)
    {
        List<Integer> retval = null;
        if (intArray != null)
        {
            List<Integer> numbers = new ArrayList<>();
            for (int i = 0; i < intArray.size(); i++)
            {
                COSNumber num;
                if (intArray.get(i) instanceof COSObject)
                {
                    num = (COSNumber) ((COSObject) intArray.get(i)).getObject();
                }
                else
                {
                    num = (COSNumber) intArray.get(i);
                }
                numbers.add(num.intValue());
            }
            retval = new COSArrayList<>(numbers, intArray);
        }
        return retval;
    }

    /**
     * This will take an array of COSNumbers and return a COSArrayList of
     * java.lang.Float values.
     *
     * @param floatArray The existing float Array.
     *
     * @return The list of Float objects.
     */
    public static List<Float> convertFloatCOSArrayToList( COSArray floatArray )
    {
        List<Float> retval = null;
        if( floatArray != null )
        {
            List<Float> numbers = new ArrayList<>(floatArray.size());
            for( int i=0; i<floatArray.size(); i++ )
            {
                COSBase base = floatArray.getObject(i);
                if (base instanceof COSNumber)
                {
                    numbers.add(((COSNumber) base).floatValue());
                }
                else
                {
                    numbers.add(null);
                }
            }
            retval = new COSArrayList<>( numbers, floatArray );
        }
        return retval;
    }

    /**
     * This will take an array of COSName and return a COSArrayList of
     * java.lang.String values.
     *
     * @param nameArray The existing name Array.
     *
     * @return The list of String objects.
     */
    public static List<String> convertCOSNameCOSArrayToList( COSArray nameArray )
    {
        List<String> retval = null;
        if( nameArray != null )
        {
            List<String>names = new ArrayList<>();
            for( int i=0; i<nameArray.size(); i++ )
            {
                names.add( ((COSName)nameArray.getObject( i )).getName() );
            }
            retval = new COSArrayList<>( names, nameArray );
        }
        return retval;
    }

    /**
     * This will take an array of COSString and return a COSArrayList of
     * java.lang.String values.
     *
     * @param stringArray The existing name Array.
     *
     * @return The list of String objects.
     */
    public static List<String> convertCOSStringCOSArrayToList( COSArray stringArray )
    {
        List<String> retval = null;
        if( stringArray != null )
        {
            List<String> string = new ArrayList<>();
            for( int i=0; i<stringArray.size(); i++ )
            {
                string.add( ((COSString)stringArray.getObject( i )).getString() );
            }
            retval = new COSArrayList<>( string, stringArray );
        }
        return retval;
    }

    /**
     * This will take an list of string objects and return a COSArray of COSName
     * objects.
     *
     * @param strings A list of strings
     *
     * @return An array of COSName objects
     */
    public static COSArray convertStringListToCOSNameCOSArray( List<String> strings )
    {
        COSArray retval = new COSArray();
        for (String string : strings)
        {
            retval.add(COSName.getPDFName(string));
        }
        return retval;
    }

    /**
     * This will take an list of string objects and return a COSArray of COSName
     * objects.
     *
     * @param strings A list of strings
     *
     * @return An array of COSName objects
     */
    public static COSArray convertStringListToCOSStringCOSArray( List<String> strings )
    {
        COSArray retval = new COSArray();
        for (String string : strings)
        {
            retval.add(new COSString(string));
        }
        return retval;
    }

    /**
     * This will convert a list of COSObjectables to an array list of COSBase objects.
     *
     * @param cosObjectableList A list of COSObjectable.
     *
     * @return A list of COSBase.
     * @throws IllegalArgumentException if an object type is not supported for conversion to a
     * COSBase object.
     */
    public static COSArray converterToCOSArray( List<?> cosObjectableList )
    {
        COSArray array = null;
        if( cosObjectableList != null )
        {
            if( cosObjectableList instanceof COSArrayList )
            {
                //if it is already a COSArrayList then we don't want to recreate the array, we want to reuse it.
                array = ((COSArrayList<?>)cosObjectableList).array;
            }
            else
            {
                array = new COSArray();
                for (Object next : cosObjectableList)
                {
                    if( next instanceof String )
                    {
                        array.add( new COSString( (String)next ) );
                    }
                    else if( next instanceof Integer || next instanceof Long )
                    {
                        array.add( COSInteger.get( ((Number)next).longValue() ) );
                    }
                    else if( next instanceof Float || next instanceof Double )
                    {
                        array.add( new COSFloat( ((Number)next).floatValue() ) );
                    }
                    else if( next instanceof COSObjectable )
                    {
                        COSObjectable object = (COSObjectable)next;
                        array.add( object.getCOSObject() );
                    }
                    else if( next == null )
                    {
                        array.add( COSNull.NULL );
                    }
                    else
                    {
                        throw new IllegalArgumentException( "Error: Don't know how to convert type to COSBase '" +
                        next.getClass().getName() + "'" );
                    }
                }
            }
        }
        return array;
    }

    private List<COSBase> toCOSObjectList( Collection<?> list )
    {
        List<COSBase> cosObjects = new ArrayList<>();
        for (Object next : list)
        {
            if( next instanceof String )
            {
                cosObjects.add( new COSString( (String)next ) );
            }
            else
            {
                COSObjectable cos = (COSObjectable)next;
                cosObjects.add( cos.getCOSObject() );
            }
        }
        return cosObjects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c)
    {
        array.removeAll( toCOSObjectList( c ) );
        return actual.removeAll( c );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c)
    {
        array.retainAll( toCOSObjectList( c ) );
        return actual.retainAll( c );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        //when adding if there is a parentDict then change the item
        //in the dictionary from a single item to an array.
        if( parentDict != null )
        {
            parentDict.setItem( dictKey, null );
        }
        actual.clear();
        array.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        return actual.equals( o );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return actual.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index)
    {
        return actual.get( index );

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(int index, E element)
    {
        if( element instanceof String )
        {
            COSString item = new COSString( (String)element );
            if( parentDict != null && index == 0 )
            {
                parentDict.setItem( dictKey, item );
            }
            array.set( index, item );
        }
        else
        {
            if( parentDict != null && index == 0 )
            {
                parentDict.setItem( dictKey, ((COSObjectable)element).getCOSObject() );
            }
            array.set( index, ((COSObjectable)element).getCOSObject() );
        }
        return actual.set( index, element );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, E element)
    {
        //when adding if there is a parentDict then change the item
        //in the dictionary from a single item to an array.
        if( parentDict != null )
        {
            parentDict.setItem( dictKey, array );
            //clear the parent dict so it doesn't happen again, there might be
            //a usecase for keeping the parentDict around but not now.
            parentDict = null;
        }
        actual.add( index, element );
        if( element instanceof String )
        {
            array.add( index, new COSString( (String)element ) );
        }
        else
        {
            array.add( index, ((COSObjectable)element).getCOSObject() );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int index)
    {
        array.remove( index );
        return actual.remove( index );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o)
    {
        return actual.indexOf( o );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o)
    {
        return actual.indexOf( o );

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator()
    {
        return actual.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(int index)
    {
        return actual.listIterator( index );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return actual.subList( fromIndex, toIndex );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSArrayList{" + array.toString() + "}";
    }
    
    /**
     * This will return then underlying COSArray.
     * 
     * @return the COSArray
     */
    public COSArray toList() 
    {
        return array;
    }

}
