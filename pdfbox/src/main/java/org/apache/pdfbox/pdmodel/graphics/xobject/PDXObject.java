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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 */
public abstract class PDXObject implements COSObjectable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDXObject.class);

    private PDStream xobject;

    /**
     * Standard constructor.
     * 
     * @param xobj The XObject dictionary.
     */
    public PDXObject(COSStream xobj)
    {
        xobject = new PDStream(xobj);
        getCOSStream().setItem(COSName.TYPE, COSName.XOBJECT);
    }

    /**
     * Standard constuctor.
     * 
     * @param xobj The XObject dictionary.
     */
    public PDXObject(PDStream xobj)
    {
        xobject = xobj;
        getCOSStream().setItem(COSName.TYPE, COSName.XOBJECT);
    }

    /**
     * Standard constuctor.
     * 
     * @param doc The doc to store the object contents.
     */
    public PDXObject(PDDocument doc)
    {
        xobject = new PDStream(doc);
        getCOSStream().setItem(COSName.TYPE, COSName.XOBJECT);
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
     * 
     * @return The stream for this object.
     */
    public COSStream getCOSStream()
    {
        return xobject.getStream();
    }

    /**
     * Returns the stream.
     * 
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
    public static PDXObject createXObject(COSBase xobject) throws IOException
    {
        return commonXObjectCreation(xobject, false);
    }

    /**
     * Create the correct xobject from the cos base.
     * 
     * @param xobject The cos level xobject to create.
     * @param isthumb specify if the xobject represent a Thumbnail Image (in this case, the subtype null must be
     * considered as an Image)
     * @return a pdmodel xobject
     * @throws IOException If there is an error creating the xobject.
     */
    protected static PDXObject commonXObjectCreation(COSBase xobject, boolean isThumb)
    {
        PDXObject retval = null;
        if (xobject == null)
        {
            retval = null;
        }
        else if (xobject instanceof COSStream)
        {
            COSStream xstream = (COSStream) xobject;
            String subtype = xstream.getNameAsString(COSName.SUBTYPE);
            // according to the PDF Reference : a thumbnail subtype must be Image if it is not null
            if (PDXObjectImage.SUB_TYPE.equals(subtype) || (subtype == null && isThumb))
            {
                PDStream image = new PDStream(xstream);
                // See if filters are DCT or JPX otherwise treat as Bitmap-like
                // There might be a problem with several filters, but that's ToDo until
                // I find an example
                List<COSName> filters = image.getFilters();
                if (filters != null && filters.contains(COSName.DCT_DECODE))
                {
                    return new PDJpeg(image);
                }
                else if (filters != null && filters.contains(COSName.CCITTFAX_DECODE))
                {
                    return new PDCcitt(image);
                }
                else if (filters != null && filters.contains(COSName.JPX_DECODE))
                {
                    // throw new IOException( "JPXDecode has not been implemented for images" );
                    // JPX Decode is not really supported right now, but if we are just doing
                    // text extraction then we don't want to throw an exception, so for now
                    // just return a PDPixelMap, which will break later on if it is
                    // actually used, but for text extraction it is not used.
                    return new PDPixelMap(image);
                }
                else
                {
                    retval = new PDPixelMap(image);
                }
            }
            else if (PDXObjectForm.SUB_TYPE.equals(subtype))
            {
                retval = new PDXObjectForm(xstream);
            }
            else
            {
                LOG.warn("Skipping unknown XObject subtype '" + subtype + "'");
            }
        }
        return retval;
    }

    /**
     * Get the metadata that is part of the document catalog. This will return null if there is no meta data for this
     * object.
     * 
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSStream mdStream = (COSStream) getCOSStream().getDictionaryObject(COSName.METADATA);
        if (mdStream != null)
        {
            retval = new PDMetadata(mdStream);
        }
        return retval;
    }

    /**
     * Set the metadata for this object. This can be null.
     * 
     * @param meta The meta data for this object.
     */
    public void setMetadata(PDMetadata meta)
    {
        getCOSStream().setItem(COSName.METADATA, meta);
    }

    /**
     * This will get the key of this XObject in the structural parent tree. Required if the form XObject is a structural
     * content item.
     * 
     * @return the integer key of the XObject's entry in the structural parent tree
     */
    public int getStructParent()
    {
        return getCOSStream().getInt(COSName.STRUCT_PARENT, 0);
    }

    /**
     * This will set the key for this XObject in the structural parent tree.
     * 
     * @param structParent The new key for this XObject.
     */
    public void setStructParent(int structParent)
    {
        getCOSStream().setInt(COSName.STRUCT_PARENT, structParent);
    }

}
