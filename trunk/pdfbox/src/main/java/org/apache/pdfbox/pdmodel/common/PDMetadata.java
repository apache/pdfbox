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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.pdfbox.cos.COSName;


import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This class represents metadata for various objects in a PDF document.
 *
 * @author Ben Litchfield
 */
public class PDMetadata extends PDStream
{

    /**
     * This will create a new PDMetadata object.
     *
     * @param document The document that the stream will be part of.
     */
    public PDMetadata( PDDocument document )
    {
        super( document );
        getCOSObject().setName( COSName.TYPE, "Metadata" );
        getCOSObject().setName( COSName.SUBTYPE, "XML" );
    }

    /**
     * Constructor.  Reads all data from the input stream and embeds it into the
     * document, this will close the InputStream.
     *
     * @param doc The document that will hold the stream.
     * @param str The stream parameter.
     * @throws IOException If there is an error creating the stream in the document.
     */
    public PDMetadata( PDDocument doc, InputStream str) throws IOException
    {
        super( doc, str );
        getCOSObject().setName( COSName.TYPE, "Metadata" );
        getCOSObject().setName( COSName.SUBTYPE, "XML" );
    }

    /**
     * Constructor.
     *
     * @param str The stream parameter.
     */
    public PDMetadata( COSStream str )
    {
        super( str );
    }

    /**
     * Extract the XMP metadata.
     * To persist changes back to the PDF you must call importXMPMetadata.
     *
     * @return A stream to get the xmp data from.
     *
     * @throws IOException If there is an error parsing the XMP data.
     */
    public InputStream exportXMPMetadata() throws IOException
    {
        return createInputStream();
    }

    /**
     * Import an XMP stream into the PDF document.
     *
     * @param xmp The XMP data.
     *
     * @throws IOException If there is an error generating the XML document.
     */
    public void importXMPMetadata( byte[] xmp )
        throws IOException
    {
        try (OutputStream os = createOutputStream())
        {
            os.write(xmp);
        }
    }
}
