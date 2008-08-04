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
package org.pdfbox.pdmodel.graphics.xobject;

import java.io.IOException;
import java.util.List;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.PDMetadata;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * The base class for all XObjects in the PDF document.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @author Marcel Kammer
 * @version $Revision: 1.14 $
 */
public abstract class PDXObject implements COSObjectable 
{
    private PDStream xobject;
    
    /**
     * Standard constuctor. 
     * 
     * @param xobj The XObject dictionary.
     */
    public PDXObject(COSStream xobj) 
    {
        xobject = new PDStream( xobj );
        xobject.getStream().setName( COSName.TYPE, "XObject" );
    }
    
    /**
     * Standard constuctor. 
     * 
     * @param xobj The XObject dictionary.
     */
    public PDXObject(PDStream xobj) 
    {
        xobject = xobj;
        xobject.getStream().setName( COSName.TYPE, "XObject" );
    }
    
    /**
     * Standard constuctor. 
     * 
     * @param doc The doc to store the object contents.
     */
    public PDXObject(PDDocument doc) 
    {
        xobject = new PDStream(doc);
        xobject.getStream().setName( COSName.TYPE, "XObject" );
    }
    
    /**
     * Returns the stream. 
     * 
     * {@inheritDoc}
     */
    public COSBase getCOSObject() 
    {
        return xobject.getCOSObject();
    }
    
    /**
     * Returns the stream. 
     * @return The stream for this object.
     */
    public COSStream getCOSStream() 
    {
        return xobject.getStream();
    }
    
    /**
     * Returns the stream. 
     * @return The stream for this object.
     */
    public PDStream getPDStream() 
    {
        return xobject;
    }
    
    /**
     * Create the correct xobject from the cos base.
     * 
     * @param xobject The cos level xobject to create.
     * 
     * @return a pdmodel xobject
     * @throws IOException If there is an error creating the xobject.
     */
    public static PDXObject createXObject( COSBase xobject ) throws IOException
    {
        PDXObject retval = null;
        if( xobject == null )
        {
            retval = null;
        }
        else if( xobject instanceof COSStream )
        {
            COSStream xstream = (COSStream)xobject;
            String subtype = xstream.getNameAsString( "Subtype" ); 
            if( subtype.equals( PDXObjectImage.SUB_TYPE ) )
            {
                PDStream image = new PDStream( xstream );
                // See if filters are DCT or JPX otherwise treat as Bitmap-like
                // There might be a problem with several filters, but that's ToDo until
                // I find an example 
                List filters = image.getFilters();
                if( filters != null && filters.contains( COSName.DCT_DECODE.getName() ) ) 
                {
                    return new PDJpeg(image);
                } 
                else if ( filters != null && filters.contains( COSName.CCITTFAX_DECODE.getName() ) )
                {
                    return new PDCcitt(image);
                }
                else if( filters != null && filters.contains(COSName.JPX_DECODE.getName()))
                {
                    //throw new IOException( "JPXDecode has not been implemented for images" );
                    //JPX Decode is not really supported right now, but if we are just doing
                    //text extraction then we don't want to throw an exception, so for now
                    //just return a PDPixelMap, which will break later on if it is 
                    //actually used, but for text extraction it is not used.
                    return new PDPixelMap( image );
                    
                } 
                else 
                {
                    retval = new PDPixelMap(image);
                }
            }
            else if( subtype.equals( PDXObjectForm.SUB_TYPE ) )
            {
                retval = new PDXObjectForm( xstream );
            }
            else
            {
                throw new IOException( "Unknown xobject subtype '" + subtype + "'" );
            }
        }
        else
        {
            throw new IOException( "Unknown xobject type:" + xobject.getClass().getName() );
        }
        
        return retval;
    }
    
    /**
     * Get the metadata that is part of the document catalog.  This will 
     * return null if there is no meta data for this object.
     * 
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSStream mdStream = (COSStream)xobject.getStream().getDictionaryObject( "Metadata" );
        if( mdStream != null )
        {
            retval = new PDMetadata( mdStream );
        }
        return retval;
    }
    
    /**
     * Set the metadata for this object.  This can be null.
     * 
     * @param meta The meta data for this object.
     */
    public void setMetadata( PDMetadata meta )
    {
        xobject.getStream().setItem( "Metadata", meta );
    }
}
