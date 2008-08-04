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
package org.pdfbox.pdmodel.interactive.documentnavigation.destination;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSNumber;

import org.pdfbox.pdmodel.PDPage;

/**
 * This represents a destination to a page, see subclasses for specific parameters.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public abstract class PDPageDestination extends PDDestination
{
    /**
     * Storage for the page destination.
     */
    protected COSArray array;
    
    /**
     * Constructor to create empty page destination.
     *
     */
    protected PDPageDestination()
    {
        array = new COSArray();
    }
    
    /**
     * Constructor to create empty page destination.
     *
     * @param arr A page destination array.
     */
    protected PDPageDestination( COSArray arr )
    {
        array = arr;
    }
    
    /**
     * This will get the page for this destination.  A page destination
     * can either reference a page or a page number(when doing a remote destination to 
     * another PDF).  If this object is referencing by page number then this method will
     * return null and getPageNumber should be used.
     * 
     * @return The page for this destination.
     */
    public PDPage getPage()
    {
        PDPage retval = null;
        if( array.size() > 0 )
        {
            COSBase page = array.getObject( 0 );
            if( page instanceof COSDictionary )
            {
                retval = new PDPage( (COSDictionary)page );
            }
        }
        return retval;
    }
    
    /**
     * Set the page for this destination.
     * 
     * @param page The page for the destination.
     */
    public void setPage( PDPage page )
    {
        array.set( 0, page );
    }
    
    /**
     * This will get the page number for this destination.  A page destination
     * can either reference a page or a page number(when doing a remote destination to 
     * another PDF).  If this object is referencing by page number then this method will
     * return that number, otherwise -1 will be returned.
     * 
     * @return The page number for this destination.
     */
    public int getPageNumber()
    {
        int retval = -1;
        if( array.size() > 0 )
        {
            COSBase page = array.getObject( 0 );
            if( page instanceof COSNumber )
            {
                retval = ((COSNumber)page).intValue();
            }
        }
        return retval;
    }
    
    /**
     * Set the page number for this destination.
     * 
     * @param pageNumber The page for the destination.
     */
    public void setPageNumber( int pageNumber )
    {
        array.set( 0, pageNumber );
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return array;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSArray getCOSArray()
    {
        return array;
    }
}
