/**
 * Copyright (c) 2005, www.pdfbox.org
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
