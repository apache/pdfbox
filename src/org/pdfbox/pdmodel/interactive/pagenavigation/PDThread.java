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
package org.pdfbox.pdmodel.interactive.pagenavigation;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * This a single thread in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDThread implements COSObjectable
{
    
    
    private COSDictionary thread;

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param t The underlying dictionary.
     */
    public PDThread( COSDictionary t )
    {
        thread = t;
    }
    
    /**
     * Default constructor.
     *
     */
    public PDThread()
    {
        thread = new COSDictionary();
        thread.setName( "Type", "Thread" );
    }

    /**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    public COSDictionary getDictionary()
    {
        return thread;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return thread;
    }
    
    /**
     * Get info about the thread, or null if there is nothing.
     * 
     * @return The thread information.
     */
    public PDDocumentInformation getThreadInfo()
    {
        PDDocumentInformation retval = null;
        COSDictionary info = (COSDictionary)thread.getDictionaryObject( "I" );
        if( info != null )
        {
            retval = new PDDocumentInformation( info );
        }
        
        return retval;
    }
    
    /**
     * Set the thread info, can be null.
     * 
     * @param info The info dictionary about this thread.
     */
    public void setThreadInfo( PDDocumentInformation info )
    {
        thread.setItem( "I", info );
    }
    
    /**
     * Get the first bead in the thread, or null if it has not been set yet.  This
     * is a required field for this object.
     * 
     * @return The first bead in the thread.
     */
    public PDThreadBead getFirstBead()
    {
        PDThreadBead retval = null;
        COSDictionary bead = (COSDictionary)thread.getDictionaryObject( "F" );
        if( bead != null )
        {
            retval = new PDThreadBead( bead );
        }
        
        return retval;
    }
    
    /**
     * This will set the first bead in the thread.  When this is set it will
     * also set the thread property of the bead object.  
     * 
     * @param bead The first bead in the thread.
     */
    public void setFirstBead( PDThreadBead bead )
    {
        if( bead != null )
        {
            bead.setThread( this );
        }
        thread.setItem( "F", bead );
    }
    
    
}