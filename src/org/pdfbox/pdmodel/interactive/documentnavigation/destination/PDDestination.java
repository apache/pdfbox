/**
 * Copyright (c) 2004-2005, www.pdfbox.org
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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSString;

import org.pdfbox.pdmodel.common.PDDestinationOrAction;

/**
 * This represents a destination in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public abstract class PDDestination implements PDDestinationOrAction
{
    
    /**
     * This will create a new destination depending on the type of COSBase
     * that is passed in.
     * 
     * @param base The base level object.
     * 
     * @return A new destination.
     * 
     * @throws IOException If the base cannot be converted to a Destination.
     */
    public static PDDestination create( COSBase base ) throws IOException
    {
        PDDestination retval = null;
        if( base == null )
        {
            //this is ok, just return null.
        }
        else if( base instanceof COSArray && ((COSArray)base).size() > 0 )
        {
            COSArray array = (COSArray)base;
            COSName type = (COSName)array.getObject( 1 );
            String typeString = type.getName();
            if( typeString.equals( PDPageFitDestination.TYPE ) ||
                typeString.equals( PDPageFitDestination.TYPE_BOUNDED ))
            {
                retval = new PDPageFitDestination( array );
            }
            else if( typeString.equals( PDPageFitHeightDestination.TYPE ) ||
                     typeString.equals( PDPageFitHeightDestination.TYPE_BOUNDED ))
            {
                retval = new PDPageFitHeightDestination( array );
            }
            else if( typeString.equals( PDPageFitRectangleDestination.TYPE ) )
            {
                retval = new PDPageFitRectangleDestination( array );
            }
            else if( typeString.equals( PDPageFitWidthDestination.TYPE ) ||
                     typeString.equals( PDPageFitWidthDestination.TYPE_BOUNDED ))
            {
                retval = new PDPageFitWidthDestination( array );
            }
            else if( typeString.equals( PDPageXYZDestination.TYPE ) )
            {
                retval = new PDPageXYZDestination( array );
            }
            else
            {
                throw new IOException( "Unknown destination type:" + type );
            }
        }
        else if( base instanceof COSString )
        {
            retval = new PDNamedDestination( (COSString)base );
        }
        else if( base instanceof COSName )
        {
            retval = new PDNamedDestination( (COSName)base );
        }
        else
        {
            throw new IOException( "Error: can't convert to Destination " + base );
        }
        return retval;
    }
    
    /**
     * Return a string representation of this class.
     * 
     * @return A human readable string.
     */
    public String toString()
    {
        return super.toString();
    }
}
