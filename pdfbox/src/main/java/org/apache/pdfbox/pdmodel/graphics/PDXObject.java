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
package org.apache.pdfbox.pdmodel.graphics;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

/**
 * An external object, or "XObject".
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDXObject implements COSObjectable
{
    private final PDStream stream;

    /**
     * Creates a new XObject instance of the appropriate type for the COS stream.
     *
     * @param base The stream which is wrapped by this XObject.
     * @param resources
     * @return A new XObject instance.
     * @throws java.io.IOException if there is an error creating the XObject.
     */
    public static PDXObject createXObject(COSBase base, PDResources resources) throws IOException
    {
        if (base == null)
        {
            // TODO throw an exception?
            return null;
        }

        if (!(base instanceof COSStream))
        {
            throw new IOException("Unexpected object type: " + base.getClass().getName());
        }

        COSStream stream = (COSStream)base;
        String subtype = stream.getNameAsString(COSName.SUBTYPE);

        if (COSName.IMAGE.getName().equals(subtype))
        {
            return new PDImageXObject(new PDStream(stream), resources);
        }
        else if (COSName.FORM.getName().equals(subtype))
        {
            ResourceCache cache = resources != null ? resources.getResourceCache() : null;
            COSDictionary group = (COSDictionary)stream.getDictionaryObject(COSName.GROUP);
            if (group != null && COSName.TRANSPARENCY.equals(group.getCOSName(COSName.S)))
            {
                return new PDTransparencyGroup(stream, cache);
            }
            return new PDFormXObject(stream, cache);
        }
        else if (COSName.PS.getName().equals(subtype))
        {
            return new PDPostScriptXObject(stream);
        }
        else
        {
            throw new IOException("Invalid XObject Subtype: " + subtype);
        }
    }

    /**
     * Creates a new XObject from the given stream and subtype.
     * @param stream The stream to read.
     * @param subtype
     */
    protected PDXObject(COSStream stream, COSName subtype)
    {
        this.stream = new PDStream(stream);
        // could be used for writing:
        stream.setName(COSName.TYPE, COSName.XOBJECT.getName());
        stream.setName(COSName.SUBTYPE, subtype.getName());
    }

    /**
     * Creates a new XObject from the given stream and subtype.
     * @param stream The stream to read.
     * @param subtype
     */
    protected PDXObject(PDStream stream, COSName subtype)
    {
        this.stream = stream;
        // could be used for writing:
        stream.getCOSObject().setName(COSName.TYPE, COSName.XOBJECT.getName());
        stream.getCOSObject().setName(COSName.SUBTYPE, subtype.getName());
    }

    /**
     * Creates a new XObject of the given subtype for writing.
     * @param document The document in which to create the XObject.
     * @param subtype The subtype of the new XObject.
     */
    protected PDXObject(PDDocument document, COSName subtype)
    {
        stream = new PDStream(document);
        stream.getCOSObject().setName(COSName.TYPE, COSName.XOBJECT.getName());
        stream.getCOSObject().setName(COSName.SUBTYPE, subtype.getName());
    }

    /**
     * Returns the stream.
     * {@inheritDoc}
     */
    @Override
    public final COSStream getCOSObject()
    {
        return stream.getCOSObject();
    }

    /**
     * Returns the stream.
     * @return The stream for this object.
     * @deprecated use {@link #getCOSObject() }
     */
    @Deprecated
    public final COSStream getCOSStream()
    {
        return stream.getCOSObject();
    }

    /**
     * Returns the stream.
     * @return The stream for this object.
     * @deprecated Use {@link #getStream()} instead.
     */
    @Deprecated
    public final PDStream getPDStream()
    {
        return stream;
    }

    /**
     * Returns the stream.
     * @return The stream for this object.
     */
    public final PDStream getStream()
    {
        return stream;
    }
}
