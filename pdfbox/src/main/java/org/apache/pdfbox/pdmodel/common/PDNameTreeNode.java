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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * This class represents a node in a name tree.
 *
 * @author Ben Litchfield
 *
 * @param <T> The type of the values in this name tree.
 */
public abstract class PDNameTreeNode<T extends COSObjectable> implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(PDNameTreeNode.class);
    
    private final COSDictionary node;
    private PDNameTreeNode<T> parent;

    /**
     * Constructor.
     */
    protected PDNameTreeNode()
    {
        node = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param dict The dictionary that holds the name information.
     */
    protected PDNameTreeNode( COSDictionary dict )
    {
        node = dict;
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
     * Returns the parent node.
     * 
     * @return parent node
     */
    public PDNameTreeNode<T> getParent()
    {
        return parent;
    }

    /**
     * Sets the parent to the given node.
     * 
     * @param parentNode the node to be set as parent
     */
    public void setParent(PDNameTreeNode<T> parentNode)
    {
        parent = parentNode;
        calculateLimits();
    }
    
    /**
     * Determines if this is a root node or not.
     * 
     * @return true if this is a root node
     */
    public boolean isRootNode()
    {
        return parent == null;
    }
    
    /**
     * Return the children of this node.  This list will contain PDNameTreeNode objects.
     *
     * @return The list of children or null if there are no children.
     */
    public List<PDNameTreeNode<T>> getKids()
    {
        List<PDNameTreeNode<T>> retval = null;
        COSArray kids = node.getCOSArray(COSName.KIDS);
        if( kids != null )
        {
            List<PDNameTreeNode<T>> pdObjects = new ArrayList<>();
            for( int i=0; i<kids.size(); i++ )
            {
                pdObjects.add( createChildNode( (COSDictionary)kids.getObject(i) ) );
            }
            retval = new COSArrayList<>(pdObjects, kids);
        }

        return retval;
    }

    /**
     * Set the children of this named tree.
     *
     * @param kids The children of this named tree. These have to be in sorted order. Because of
     * that, it is usually easier to call {@link setNames} with a map and pass a single element list
     * here.
     */
    public void setKids( List<? extends PDNameTreeNode<T>> kids )
    {
        if (kids != null && !kids.isEmpty())
        {
            kids.forEach(kidsNode -> kidsNode.setParent(this));
            node.setItem(COSName.KIDS, new COSArray(kids));
            // root nodes with kids don't have Names
            if (isRootNode())
            {
                node.setItem(COSName.NAMES, null);
            }
        }
        else 
        {
            // remove kids
            node.setItem(COSName.KIDS, null);
            // remove Limits 
            node.setItem(COSName.LIMITS, null);
        }
        calculateLimits();
    }

    private void calculateLimits()
    {
        if (isRootNode())
        {
            node.setItem(COSName.LIMITS, null);
        }
        else
        {
            List<PDNameTreeNode<T>> kids = getKids();
            if (kids != null && !kids.isEmpty())
            {
                PDNameTreeNode<T> firstKid = kids.get(0);
                PDNameTreeNode<T> lastKid = kids.get(kids.size() - 1);
                String lowerLimit = firstKid.getLowerLimit();
                setLowerLimit(lowerLimit);
                String upperLimit = lastKid.getUpperLimit();
                setUpperLimit(upperLimit);
            }
            else
            {
                try 
                {
                    Map<String, T> names = getNames();
                    if (names != null && names.size() > 0)
                    {
                        Set<String> strings = names.keySet();
                        String[] keys = strings.toArray(new String[strings.size()]);
                        String lowerLimit = keys[0];
                        setLowerLimit(lowerLimit);
                        String upperLimit = keys[keys.length-1];
                        setUpperLimit(upperLimit);
                    }
                    else
                    {
                        node.setItem(COSName.LIMITS, null);
                    }
                }
                catch (IOException exception)
                {
                    node.setItem(COSName.LIMITS, null);
                    LOG.error("Error while calculating the Limits of a PageNameTreeNode:", exception);
                }
            }
        }
    }
    
    /**
     * The name to retrieve.
     *
     * @param name The name in the tree.
     * @return The value of the name in the tree.
     * @throws IOException If an there is a problem creating the destinations.
     */
    public T getValue( String name ) throws IOException
    {
        T retval = null;
        Map<String, T> names = getNames();
        if( names != null )
        {
            retval = names.get( name );
        }
        else
        {
            List<PDNameTreeNode<T>> kids = getKids();
            if (kids != null)
            {
                for( int i=0; i<kids.size() && retval == null; i++ )
                {
                    PDNameTreeNode<T> childNode = kids.get( i );
                    String upperLimit = childNode.getUpperLimit();
                    String lowerLimit = childNode.getLowerLimit();
                    if (upperLimit == null || lowerLimit == null || 
                        upperLimit.compareTo(lowerLimit) < 0 ||
                        (lowerLimit.compareTo(name) <= 0 && upperLimit.compareTo(name) >= 0))
                    {
                        retval = childNode.getValue( name );
                    }
                }
            }
            else
            {
                LOG.warn("NameTreeNode does not have \"names\" nor \"kids\" objects.");
            }
        }
        return retval;
    }

    /**
     * This will return a map of names on this level. The key will be a string,
     * and the value will depend on where this class is being used.
     *
     * @return ordered map of COS objects or <code>null</code> if the dictionary
     * contains no 'Names' entry on this level.
     *
     * @throws IOException If there is an error while creating the sub types.
     * @see #getKids()
     */
    public Map<String, T> getNames() throws IOException
    {
        COSArray namesArray = node.getCOSArray(COSName.NAMES);
        if( namesArray != null )
        {
            Map<String, T> names = new LinkedHashMap<>();
            if (namesArray.size() % 2 != 0)
            {
                LOG.warn("Names array has odd size: " + namesArray.size());
            }
            for (int i = 0; i + 1 < namesArray.size(); i += 2)
            {
                COSBase base = namesArray.getObject(i);
                if (!(base instanceof COSString))
                {
                    throw new IOException("Expected string, found " + base + " in name tree at index " + i);
                }
                COSString key = (COSString) base;
                COSBase cosValue = namesArray.getObject( i+1 );
                names.put( key.getString(), convertCOSToPD(cosValue) );
            }
            return Collections.unmodifiableMap(names);
        }
        else
        {
            return null;
        }
    }

    /**
     * Method to convert the COS value in the name tree to the PD Model object. The
     * default implementation will simply return the given COSBase object.
     * Subclasses should do something specific.
     *
     * @param base The COS object to convert.
     * @return The converted PD Model object.
     * @throws IOException If there is an error during creation.
     */
    protected abstract T convertCOSToPD( COSBase base ) throws IOException;

    /**
     * Create a child node object.
     *
     * @param dic The dictionary for the child node object to refer to.
     * @return The new child node object.
     */
    protected abstract PDNameTreeNode<T> createChildNode( COSDictionary dic );

    /**
     * Set the names for this node. This method will set the appropriate upper and lower limits
     * based on the keys in the map and take care of the ordering.
     *
     * @param names map of names to objects, or <code>null</code> for nothing.
     */
    public void setNames( Map<String, T> names )
    {
        if( names == null )
        {
            node.setItem( COSName.NAMES, (COSObjectable)null );
            node.setItem( COSName.LIMITS, (COSObjectable)null);
        }
        else
        {
            COSArray array = new COSArray();
            List<String> keys = new ArrayList<>(names.keySet());
            Collections.sort(keys);
            for (String key : keys) 
            {
                array.add(new COSString(key));
                array.add(names.get(key));
            }
            node.setItem(COSName.NAMES, array);
            calculateLimits();
        }
    }

    /**
     * Get the highest value for a key in the name map.
     *
     * @return The highest value for a key in the map.
     */
    public String getUpperLimit()
    {
        String retval = null;
        COSArray arr = node.getCOSArray(COSName.LIMITS);
        if( arr != null )
        {
            retval = arr.getString( 1 );
        }
        return retval;
    }

    /**
     * Set the highest value for the key in the map.
     *
     * @param upper The new highest value for a key in the map.
     */
    private void setUpperLimit( String upper )
    {
        COSArray arr = node.getCOSArray(COSName.LIMITS);
        if( arr == null )
        {
            arr = new COSArray();
            arr.add( null );
            arr.add( null );
            node.setItem(COSName.LIMITS, arr);
        }
        arr.setString( 1, upper );
    }

    /**
     * Get the lowest value for a key in the name map.
     *
     * @return The lowest value for a key in the map.
     */
    public String getLowerLimit()
    {
        String retval = null;
        COSArray arr = node.getCOSArray(COSName.LIMITS);
        if( arr != null )
        {
            retval = arr.getString( 0 );
        }
        return retval;
    }

    /**
     * Set the lowest value for the key in the map.
     *
     * @param lower The new lowest value for a key in the map.
     */
    private void setLowerLimit( String lower )
    {
        COSArray arr = node.getCOSArray(COSName.LIMITS);
        if( arr == null )
        {
            arr = new COSArray();
            arr.add( null );
            arr.add( null );
            node.setItem(COSName.LIMITS, arr);
        }
        arr.setString( 0, lower );
    }
}
