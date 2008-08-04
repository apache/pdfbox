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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;

/**
 * This will perform the encoding from a dictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.13 $
 */
public class DictionaryEncoding extends Encoding
{
    private COSDictionary encoding = null;

    /**
     * Constructor.
     *
     * @param fontEncoding The encoding dictionary.
     *
     * @throws IOException If there is a problem getting the base font.
     */
    public DictionaryEncoding( COSDictionary fontEncoding ) throws IOException
    {
        encoding = fontEncoding;

        //first set up the base encoding
        //The previious value WinAnsiEncoding() has been changed to StandardEnding
        //see p 389 of the PDF 1.5 reférence table 5.11 entries in a dictionary encoding
        //"If this entry is absent, the Differences entry describes differences from an implicit
        //base encoding. For a font program that is embedded in the PDF file, the
        //implicit base encoding is the font program’s built-in encoding, as described
        //above and further elaborated in the sections on specific font types below. Otherwise,
        //for a nonsymbolic font, it is StandardEncoding, and for a symbolic font, it
        //is the font’s built-in encoding."

        //so the default base encoding is standardEncoding
        Encoding baseEncoding = new StandardEncoding();
        COSName baseEncodingName = (COSName)encoding.getDictionaryObject( COSName.BASE_ENCODING );

        if( baseEncodingName != null )
        {
            EncodingManager manager = new EncodingManager();
            baseEncoding = manager.getEncoding( baseEncodingName );
        }
        nameToCode.putAll( baseEncoding.nameToCode );
        codeToName.putAll( baseEncoding.codeToName );


        //now replace with the differences.
        COSArray differences = (COSArray)encoding.getDictionaryObject( COSName.DIFFERENCES );
        int currentIndex = -1;
        for( int i=0; differences != null && i<differences.size(); i++ )
        {
            COSBase next = differences.getObject( i );
            if( next instanceof COSNumber )
            {
                currentIndex = ((COSNumber)next).intValue();
            }
            else if( next instanceof COSName )
            {
                COSName name = (COSName)next;
                addCharacterEncoding( currentIndex++, name );
            }
        }
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return encoding;
    }
}
