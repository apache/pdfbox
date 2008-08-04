/**
 * Copyright (c) 2003, www.pdfbox.org
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