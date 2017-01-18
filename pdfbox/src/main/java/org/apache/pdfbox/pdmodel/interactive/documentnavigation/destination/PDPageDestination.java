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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

/**
 * This represents a destination to a page, see subclasses for specific parameters.
 *
 * @author Ben Litchfield
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
     * This will get the page for this destination. A page destination can either reference a page
     * (for a local destination) or a page number (when doing a remote destination to another PDF).
     * If this object is referencing by page number then this method will return null and
     * {@link #getPageNumber()} should be used.
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
     * Set the page for a local destination. For an external destination, call {@link #setPageNumber(int) setPageNumber(int pageNumber)}.
     *
     * @param page The page for a local destination.
     */
    public void setPage( PDPage page )
    {
        array.set( 0, page );
    }

    /**
     * This will get the page number for this destination. A page destination can either reference a
     * page (for a local destination) or a page number (when doing a remote destination to another
     * PDF). If this object is referencing by page number then this method will return that number,
     * otherwise -1 will be returned.
     *
     * @return The zero-based page number for this destination.
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
     * Returns the page number for this destination, regardless of whether this is a page number or
     * a reference to a page.
     *
     * @since Apache PDFBox 1.0.0
     * @see org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
     * @return page number, or -1 if the destination type is unknown. The page number is 0-based if
     * it was in the dictionary (for remote destinations), and 1-based if it was computed from a
     * page reference (for local destinations).
     * @deprecated This method has inconsistent behavior (see returns), use {@link #retrievePageNumber()} instead.
     */
    @Deprecated
    public int findPageNumber()
    {
        int retval = -1;
        if( array.size() > 0 )
        {
            COSBase page = array.getObject( 0 );
            if( page instanceof COSNumber )
            {
                retval = ((COSNumber)page).intValue();
            }
            else if (page instanceof COSDictionary)
            {
                COSBase parent = page;
                while (((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P) != null)
                {
                    parent = ((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P);
                }
                // now parent is the pages node
                PDPageTree pages = new PDPageTree((COSDictionary) parent);
                return pages.indexOf(new PDPage((COSDictionary) page)) + 1;
            }
        }
        return retval;
    }

    /**
     * Returns the page number for this destination, regardless of whether this is a page number or
     * a reference to a page.
     *
     * @see org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
     * @return the 0-based page number, or -1 if the destination type is unknown.
     */
    public int retrievePageNumber()
    {
        int retval = -1;
        if (array.size() > 0)
        {
            COSBase page = array.getObject(0);
            if (page instanceof COSNumber)
            {
                retval = ((COSNumber) page).intValue();
            }
            else if (page instanceof COSDictionary)
            {
                //TODO make this a static utility method of PDPageTree?
                COSBase parent = page;
                while (((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P) != null)
                {
                    parent = ((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P);
                }
                // now parent is the pages node
                PDPageTree pages = new PDPageTree((COSDictionary) parent);
                return pages.indexOf(new PDPage((COSDictionary) page));
            }
        }
        return retval;
    }

    /**
     * Set the page number for a remote destination. For an internal destination, call 
     * {@link #setPage(PDPage) setPage(PDPage page)}.
     *
     * @param pageNumber The page for a remote destination.
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
    @Override
    public COSArray getCOSObject()
    {
        return array;
    }

}
