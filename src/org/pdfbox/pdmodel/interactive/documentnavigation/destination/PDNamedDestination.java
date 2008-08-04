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

import java.io.IOException;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSString;

/**
 * This represents a destination to a page by referencing it with a name.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDNamedDestination extends PDDestination
{
    private COSBase namedDestination;
    
    /**
     * Constructor.
     * 
     * @param dest The named destination.
     */
    public PDNamedDestination( COSString dest )
    {
        namedDestination = dest;
    }
    
    /**
     * Constructor.
     * 
     * @param dest The named destination.
     */
    public PDNamedDestination( COSName dest )
    {
        namedDestination = dest;
    }
    
    /**
     * Default constructor.
     */
    public PDNamedDestination()
    {
        //default, so do nothing
    }
    
    /**
     * Default constructor.
     * 
     * @param dest The named destination.
     */
    public PDNamedDestination( String dest )
    {
        namedDestination = new COSString( dest );
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return namedDestination;
    }
    
    /**
     * This will get the name of the destination.
     * 
     * @return The name of the destination.
     */
    public String getNamedDestination()
    {
        String retval = null;
        if( namedDestination instanceof COSString )
        {
            retval = ((COSString)namedDestination).getString();
        }
        else if( namedDestination instanceof COSName )
        {
            retval = ((COSName)namedDestination).getName();
        }
        
        return retval;
    }
    
    /**
     * Set the named destination.
     * 
     * @param dest The new named destination.
     * 
     * @throws IOException If there is an error setting the named destination.
     */
    public void setNamedDestination( String dest ) throws IOException
    {
        if( namedDestination instanceof COSString )
        {
            COSString string = ((COSString)namedDestination);
            string.reset();
            string.append( dest.getBytes() );
        }
        else if( dest == null )
        {
            namedDestination = null;
        }
        else
        {
            namedDestination = new COSString( dest );
        }
    }
    
}
