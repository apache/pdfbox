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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;

/**
 * This class represents a PDF Number tree. See the PDF Reference 1.7 section
 * 7.9.7 for more details.
 *
 * @author Ben Litchfield,
 * @author Igor Podolskiy
 */
public class PDNumberTreeNode implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog( PDNumberTreeNode.class );

    private final COSDictionary node;
    private Class<? extends COSObjectable> valueType = null;

    /**
     * Constructor.
     *
     * @param valueClass The PD Model type of object that is the value.
     */
    public PDNumberTreeNode( Class<? extends COSObjectable> valueClass )
    {
        node = new COSDictionary();
        valueType = valueClass;
    }

    /**
     * Constructor.
     *
     * @param dict The dictionary that holds the name information.
     * @param valueClass The PD Model type of object that is the value.
     */
    public PDNumberTreeNode( COSDictionary dict, Class<? extends COSObjectable> valueClass )
    {
        node = dict;
        valueType = valueClass;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return node;
    }

    /**
     * Return the children of this node.  This list will contain PDNumberTreeNode objects.
     *
     * @return The list of children or null if there are no children.
     */
    public List<PDNumberTreeNode> getKids()
    {
        List<PDNumberTreeNode> retval = null;
        COSArray kids = (COSArray)node.getDictionaryObject( COSName.KIDS );
        if( kids != null )
        {
            List<PDNumberTreeNode> pdObjects = new ArrayList<PDNumberTreeNode>();
            for( int i=0; i<kids.size(); i++ )
            {
                pdObjects.add( createChildNode( (COSDictionary)kids.getObject(i) ) );
            }
            retval = new COSArrayList<PDNumberTreeNode>(pdObjects,kids);
        }

        return retval;
    }

    /**
     * Set the children of this number tree.
     *
     * @param kids The children of this number tree.
     */
    public void setKids( List<? extends PDNumberTreeNode> kids )
    {
        if (kids != null && !kids.isEmpty())
        {
            PDNumberTreeNode firstKid = kids.get(0);
            PDNumberTreeNode lastKid = kids.get(kids.size() - 1);
            Integer lowerLimit = firstKid.getLowerLimit();
            this.setLowerLimit(lowerLimit);
            Integer upperLimit = lastKid.getUpperLimit();
            this.setUpperLimit(upperLimit);
        }
        else if ( node.getDictionaryObject( COSName.NUMS ) == null )
        {
            // Remove limits if there are no kids and no numbers set.
            node.setItem( COSName.LIMITS, null);
        }
        node.setItem( COSName.KIDS, COSArrayList.converterToCOSArray( kids ) );
    }

    /**
     * Returns the value corresponding to an index in the number tree.
     *
     * @param index The index in the number tree.
     *
     * @return The value corresponding to the index.
     *
     * @throws IOException If there is a problem creating the values.
     */
    public Object getValue(Integer index) throws IOException
    {
        Map<Integer, COSObjectable> names = getNumbers();
        if (names != null)
        {
            return names.get(index);
        }
        Object retval = null;
        List<PDNumberTreeNode> kids = getKids();
        if (kids != null)
        {
            for (int i = 0; i < kids.size() && retval == null; i++)
            {
                PDNumberTreeNode childNode = kids.get(i);
                if (childNode.getLowerLimit().compareTo(index) <= 0 &&
                    childNode.getUpperLimit().compareTo(index) >= 0)
                {
                    retval = childNode.getValue(index);
                }
            }
        }
        else
        {
            LOG.warn("NumberTreeNode does not have \"nums\" nor \"kids\" objects.");
        }
        return retval;
    }


    /**
     * This will return a map of numbers.  The key will be a java.lang.Integer, the value will
     * depend on where this class is being used.
     *
     * @return A map of COS objects.
     *
     * @throws IOException If there is a problem creating the values.
     */
    public Map<Integer,COSObjectable> getNumbers()  throws IOException
    {
        Map<Integer, COSObjectable> indices = null;
        COSArray namesArray = (COSArray)node.getDictionaryObject( COSName.NUMS );
        if( namesArray != null )
        {
            indices = new HashMap<Integer,COSObjectable>();
            for( int i=0; i<namesArray.size(); i+=2 )
            {
                COSInteger key = (COSInteger)namesArray.getObject(i);
                COSBase cosValue = namesArray.getObject( i+1 );
                COSObjectable pdValue = convertCOSToPD( cosValue );
                indices.put( key.intValue(), pdValue );
            }
            indices = Collections.unmodifiableMap(indices);
        }
        return indices;
    }

    /**
     * Method to convert the COS value in the name tree to the PD Model object.  The
     * default implementation will simply use reflection to create the correct object
     * type.  Subclasses can do whatever they want.
     *
     * @param base The COS object to convert.
     * @return The converted PD Model object.
     * @throws IOException If there is an error during creation.
     */
    protected COSObjectable convertCOSToPD( COSBase base ) throws IOException
    {
        COSObjectable retval = null;
        try
        {
            Constructor<? extends COSObjectable> ctor = valueType.getConstructor(base.getClass());
            retval = ctor.newInstance(base);
        }
        catch( Throwable t )
        {
            throw new IOException( "Error while trying to create value in number tree:" + t.getMessage(), t);
        }
        return retval;
    }

    /**
     * Create a child node object.
     *
     * @param dic The dictionary for the child node object to refer to.
     * @return The new child node object.
     */
    protected PDNumberTreeNode createChildNode( COSDictionary dic )
    {
        return new PDNumberTreeNode(dic,valueType);
    }

    /**
     * Set the names of for this node.  The keys should be java.lang.String and the
     * values must be a COSObjectable.  This method will set the appropriate upper and lower
     * limits based on the keys in the map.
     *
     * @param numbers The map of names to objects.
     */
    public void setNumbers( Map<Integer, ? extends COSObjectable> numbers )
    {
        if( numbers == null )
        {
            node.setItem( COSName.NUMS, (COSObjectable)null );
            node.setItem( COSName.LIMITS, (COSObjectable)null);
        }
        else
        {
            List<Integer> keys = new ArrayList<Integer>( numbers.keySet() );
            Collections.sort( keys );
            COSArray array = new COSArray();
            for (Integer key : keys)
            {
                array.add( COSInteger.get( key ) );
                COSObjectable obj = numbers.get( key );
                array.add( obj );
            }
            Integer lower = null;
            Integer upper = null;
            if (!keys.isEmpty())
            {
                lower = keys.get( 0 );
                upper = keys.get( keys.size()-1 );
            }
            setUpperLimit( upper );
            setLowerLimit( lower );
            node.setItem( COSName.NUMS, array );
        }
    }

    /**
     * Get the highest value for a key in the name map.
     *
     * @return The highest value for a key in the map.
     */
    public Integer getUpperLimit()
    {
        Integer retval = null;
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
        if( arr != null && arr.get(0) != null )
        {
            retval = arr.getInt( 1 );
        }
        return retval;
    }

    /**
     * Set the highest value for the key in the map.
     *
     * @param upper The new highest value for a key in the map.
     */
    private void setUpperLimit( Integer upper )
    {
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
        if( arr == null )
        {
            arr = new COSArray();
            arr.add( null );
            arr.add( null );
            node.setItem( COSName.LIMITS, arr );
        }
        if ( upper != null)
        {
            arr.setInt( 1, upper);
        }
        else
        {
            arr.set( 1, null );
        }
    }

    /**
     * Get the lowest value for a key in the name map.
     *
     * @return The lowest value for a key in the map.
     */
    public Integer getLowerLimit()
    {
        Integer retval = null;
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
        if( arr != null && arr.get(0) != null )
        {
            retval = arr.getInt( 0 );
        }
        return retval;
    }

    /**
     * Set the lowest value for the key in the map.
     *
     * @param lower The new lowest value for a key in the map.
     */
    private void setLowerLimit( Integer lower )
    {
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
        if( arr == null )
        {
            arr = new COSArray();
            arr.add( null );
            arr.add( null );
            node.setItem( COSName.LIMITS, arr );
        }
        if ( lower != null )
        {
            arr.setInt( 0, lower);
        }
        else
        {
            arr.set( 0, null );
        }
    }
}
