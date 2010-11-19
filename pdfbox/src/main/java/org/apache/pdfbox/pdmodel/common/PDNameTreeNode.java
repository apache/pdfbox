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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * This class represends a PDF Name tree.  See the PDF Reference 1.5 section 3.8.5
 * for more details.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDNameTreeNode implements COSObjectable
{
    private static final Log log = LogFactory.getLog(PDNameTreeNode.class);
    private COSDictionary node;
    private Class valueType = null;

    /**
     * Constructor.
     *
     * @param valueClass The PD Model type of object that is the value.
     */
    public PDNameTreeNode( Class valueClass )
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
    public PDNameTreeNode( COSDictionary dict, Class valueClass )
    {
        node = dict;
        valueType = valueClass;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return node;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return node;
    }

    /**
     * Return the children of this node.  This list will contain PDNameTreeNode objects.
     *
     * @return The list of children or null if there are no children.
     */
    public List getKids()
    {

        List retval = null;
        COSArray kids = (COSArray)node.getDictionaryObject( COSName.KIDS );
        if( kids != null )
        {
            List pdObjects = new ArrayList();
            for( int i=0; i<kids.size(); i++ )
            {
                pdObjects.add( createChildNode( (COSDictionary)kids.getObject(i) ) );
            }
            retval = new COSArrayList(pdObjects,kids);
        }

        return retval;
    }

    /**
     * Set the children of this named tree.
     *
     * @param kids The children of this named tree.
     */
    public void setKids( List kids )
    {
        if (kids != null && kids.size() > 0)
        {
            PDNameTreeNode firstKid = (PDNameTreeNode) kids.get(0);
            PDNameTreeNode lastKid = (PDNameTreeNode) kids.get(kids.size() - 1);
            String lowerLimit = firstKid.getLowerLimit();
            this.setLowerLimit(lowerLimit);
            String upperLimit = lastKid.getUpperLimit();
            this.setUpperLimit(upperLimit);
        }
        node.setItem( "Kids", COSArrayList.converterToCOSArray( kids ) );
    }

    /**
     * The name to retrieve.
     *
     * @param name The name in the tree.
     *
     * @return The value of the name in the tree.
     *
     * @throws IOException If an there is a problem creating the destinations.
     */
    public Object getValue( String name ) throws IOException
    {
        Object retval = null;
        Map names = getNames();
        if( names != null )
        {
            retval = names.get( name );
        }
        else
        {
            List kids = getKids();
            if (kids != null) 
            {
                for( int i=0; i<kids.size() && retval == null; i++ )
                {
                    PDNameTreeNode childNode = (PDNameTreeNode)kids.get( i );
                    if( childNode.getLowerLimit().compareTo( name ) <= 0 &&
                        childNode.getUpperLimit().compareTo( name ) >= 0 )
                    {
                        retval = childNode.getValue( name );
                    }
                }
            }
            else
            {
                log.warn("NameTreeNode does not have \"names\" nor \"kids\" objects.");
            }
        }
        return retval;
    }


    /**
     * This will return a map of names. The key will be a string, and the
     * value will depend on where this class is being used.
     *
     * @return ordered map of cos objects
     * @throws IOException If there is an error while creating the sub types.
     */
    public Map<String, Object> getNames() throws IOException
    {
        COSArray namesArray = (COSArray)node.getDictionaryObject( COSName.NAMES );
        if( namesArray != null )
        {
            Map<String, Object> names = new LinkedHashMap<String, Object>();
            for( int i=0; i<namesArray.size(); i+=2 )
            {
                COSString key = (COSString)namesArray.getObject(i);
                COSBase cosValue = namesArray.getObject( i+1 );
                names.put( key.getString(), convertCOSToPD( cosValue ) );
            }
            return Collections.unmodifiableMap(names);
        }
        else
        {
            return null;
        }
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
    protected Object convertCOSToPD( COSBase base ) throws IOException
    {
        Object retval = null;
        try
        {
            Constructor ctor = valueType.getConstructor( new Class[] { base.getClass() } );
            retval = ctor.newInstance( new Object[] { base } );
        }
        catch( Throwable t )
        {
            throw new IOException( "Error while trying to create value in named tree:" + t.getMessage());

        }
        return retval;
    }

    /**
     * Create a child node object.
     *
     * @param dic The dictionary for the child node object to refer to.
     * @return The new child node object.
     */
    protected PDNameTreeNode createChildNode( COSDictionary dic )
    {
        return new PDNameTreeNode(dic,valueType);
    }

    /**
     * Set the names of for this node.  The keys should be java.lang.String and the
     * values must be a COSObjectable.  This method will set the appropriate upper and lower
     * limits based on the keys in the map.
     *
     * @param names map of names to objects, or <code>null</code>
     */
    public void setNames( Map<String, ? extends COSObjectable> names )
    {
        if( names == null )
        {
            node.setItem( "Names", (COSObjectable)null );
            node.setItem( COSName.LIMITS, (COSObjectable)null);
        }
        else
        {
            COSArray array = new COSArray();
            List<String> keys = new ArrayList<String>(names.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                array.add(new COSString(key));
                array.add(names.get(key));
            }
            setLowerLimit(keys.get(0));
            setUpperLimit(keys.get(keys.size() - 1));
            node.setItem("Names", array);
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
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
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
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
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
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
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
        COSArray arr = (COSArray)node.getDictionaryObject( COSName.LIMITS );
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
