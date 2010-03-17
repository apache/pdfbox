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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;

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
                /*else if( filters != null && filters.contains(COSName.FLATE_DECODE.getName()))
                {
            retval = new PDPixelMap(image);
        }*/
                else
                {
                    retval = new PDPixelMap(image);
            //throw new IOException ("Default branch: filters = " + filters.toString());
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
