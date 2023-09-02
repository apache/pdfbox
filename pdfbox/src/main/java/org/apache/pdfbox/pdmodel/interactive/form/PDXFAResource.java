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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.w3c.dom.Document;

/**
 * An XML Forms Architecture (XFA) resource.
 *
 * @author Ben Litchfield
 */
public final class PDXFAResource implements COSObjectable
{
    
    private final COSBase xfa;

    /**
     * Constructor.
     *
     * @param xfaBase The xfa resource.
     */
    public PDXFAResource(COSBase xfaBase)
    {
        xfa = xfaBase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public COSBase getCOSObject()
    {
        return xfa;
    }
    
    
    /**
     * Get the XFA content as byte array.
     * 
     * The XFA is either a stream containing the entire XFA resource or an array specifying individual packets that
     * together make up the XFA resource.
     * 
     * A packet is a pair of a string and stream. The string contains the name of the XML element and the stream
     * contains the complete text of this XML element. Each packet represents a complete XML element, with the exception
     * of the first and last packet, which specify begin and end tags for the xdp:xdp element. [IS0 32000-1:2008:
     * 12.7.8]
     * 
     * @return the XFA content
     * @throws IOException if the XFA content could not be created
     */    
    public byte[] getBytes() throws IOException 
    {
        // handle the case if the XFA is split into individual parts
        if (this.getCOSObject() instanceof COSArray) 
        {
            return getBytesFromPacket((COSArray) this.getCOSObject());
        }
        else if (xfa.getCOSObject() instanceof COSStream) 
        {
            return getBytesFromStream((COSStream) this.getCOSObject());
        }
        return new byte[0];
    }
    
    /*
     * Read all bytes from a packet
     */
    private static byte[] getBytesFromPacket(final COSArray cosArray) throws IOException
    {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            for (int i = 1; i < cosArray.size(); i += 2) 
            {
                COSBase cosObj = cosArray.getObject(i);
                if (cosObj instanceof COSStream) 
                {
                    baos.write(getBytesFromStream((COSStream) cosObj.getCOSObject()));
                }
            }
            return baos.toByteArray();
        }
    }
    
    /*
     * Read all bytes from a COSStream
     */
    private static byte[] getBytesFromStream(final COSStream stream) throws IOException
    {
        try (final InputStream is = stream.createInputStream())
        {
            return is.readAllBytes();
        }
    }
    
    /**
     * Get the XFA content as W3C document.
     * 
     * @see #getBytes()
     * 
     * @return the XFA content
     * 
     * @throws IOException if something went wrong when reading the XFA content.
     * 
     */        
    public Document getDocument() throws IOException
    {
        return org.apache.pdfbox.util.XMLUtil //
                .parse(new ByteArrayInputStream(this.getBytes()), true);
    }
}
