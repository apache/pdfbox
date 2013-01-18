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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class represents an XML Forms Architecture Data packet.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDXFA implements COSObjectable
{
    private COSBase xfa;

    /**
     * Constructor.
     *
     * @param xfaBase The xfa resource.
     */
    public PDXFA( COSBase xfaBase )
    {
        xfa = xfaBase;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return xfa;
    }
    
    
    /**
     * Get the XFA content as byte array.
     * 
     * The XFA is either a stream containing the entire XFA resource
     * or an array specifying individual packets that together make
     * up the XFA resource.
     * 
     * A packet is a pair of a string and stream. The string contains
     * the name of the XML element and the stream contains the complete
     * text of this XML element. Each packet represents a complete XML
     * element, with the exception of the first and last packet,
     * which specify begin and end tags for the xdp:xdp element.
     * [IS0 32000-1:2008: 12.7.8]
     * 
     * @return the XFA content
     * @throws IOException 
     */    
    public byte[] getBytes() throws IOException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        byte[] xfaBytes = null;

        try 
        {
            // handle the case if the XFA is split into individual parts
            if (this.getCOSObject() instanceof COSArray) 
            {
                xfaBytes = new byte[1024];
                COSArray cosArray = (COSArray) this.getCOSObject();
                for (int i = 1; i < cosArray.size(); i += 2) 
                {
                    COSBase cosObj = cosArray.getObject(i);
                    if (cosObj instanceof COSStream) 
                    {
                        is = ((COSStream) cosObj).getUnfilteredStream();
                        int nRead = 0;
                        while ((nRead = is.read(xfaBytes, 0, xfaBytes.length)) != -1) 
                        {
                          baos.write(xfaBytes, 0, nRead);
                        }
                        baos.flush();
                    }
                }
            // handle the case if the XFA is represented as a single stream
            } 
            else if (xfa.getCOSObject() instanceof COSStream) 
            {
                xfaBytes = new byte[1024];
                is = ((COSStream) xfa.getCOSObject()).getUnfilteredStream();
                int nRead = 0;
                while ((nRead = is.read(xfaBytes, 0, xfaBytes.length)) != -1) 
                {
                  baos.write(xfaBytes, 0, nRead);
                }
                baos.flush();
            }
        } 
        finally 
        {
            if (is != null) 
            {
                is.close();
            }
            if (baos != null) 
            {
                baos.close();
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * Get the XFA content as W3C document.
     * 
     * @see #getBytes()
     * 
     * @return the XFA content
     * 
     * @throws ParserConfigurationException parser exception.
     * @throws SAXException parser exception.
     * @throws IOException if something went wrong when reading the XFA content.
     * 
     */        
    public Document getDocument() throws ParserConfigurationException, SAXException, IOException 
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document xfaDocument = builder.parse(new ByteArrayInputStream(this.getBytes())); 
        return xfaDocument;
    }
}
