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
package org.pdfbox.encoding;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.pdfbox.cos.COSName;

/**
 * This class will handle getting the appropriate encodings.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class EncodingManager
{
    private static final Map ENCODINGS = new HashMap();

    static
    {
        ENCODINGS.put( COSName.MAC_ROMAN_ENCODING, new MacRomanEncoding() );
        ENCODINGS.put( COSName.PDF_DOC_ENCODING, new PdfDocEncoding() );
        ENCODINGS.put( COSName.STANDARD_ENCODING, new StandardEncoding() );
        ENCODINGS.put( COSName.WIN_ANSI_ENCODING, new WinAnsiEncoding() );
    }

    /**
     * This will get the standard encoding.
     *
     * @return The standard encoding.
     */
    public Encoding getStandardEncoding()
    {
        return (Encoding)ENCODINGS.get( COSName.STANDARD_ENCODING );
    }

    /**
     * This will get an encoding by name.
     *
     * @param name The name of the encoding to get.
     *
     * @return The encoding that matches the name.
     *
     * @throws IOException If there is not encoding with that name.
     */
    public Encoding getEncoding( COSName name ) throws IOException
    {
        Encoding encoding = (Encoding)ENCODINGS.get( name );
        if( encoding == null )
        {
            throw new IOException( "Unknown encoding for '" + name.getName() + "'" );
        }
        return encoding;
    }
}