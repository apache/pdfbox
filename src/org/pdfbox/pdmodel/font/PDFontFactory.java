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
package org.pdfbox.pdmodel.font;

import java.io.IOException;
import java.util.Map;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

/**
 * This will create the correct type of font based on information in the dictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDFontFactory
{
    /**
     * private constructor, should only use static methods in this class.
     */
    private PDFontFactory()
    {
    }
    
    /**
     * Create a font from the dictionary.  Use the fontCache to get the existing
     * object instead of creating it.
     * 
     * @param dic The font dictionary.
     * @param fontCache The font cache.
     * @return The PDModel object for the cos dictionary.
     * @throws IOException If there is an error creating the font.
     */
    public static PDFont createFont( COSDictionary dic, Map fontCache ) throws IOException
    {
        PDFont font = null;
        if( fontCache != null )
        {
            font = (PDFont)fontCache.get( dic );
        }
        if( font == null )
        {
            font = createFont( dic );
            if( fontCache != null )
            {
                fontCache.put( dic, font );
            }
        }
        return font;
    }

    /**
     * This will create the correct font based on information in the dictionary.
     *
     * @param dic The populated dictionary.
     *
     * @return The corrent implementation for the font.
     *
     * @throws IOException If the dictionary is not valid.
     */
    public static PDFont createFont( COSDictionary dic ) throws IOException
    {
        PDFont retval = null;

        COSName type = (COSName)dic.getDictionaryObject( COSName.TYPE );
        if( !type.equals( COSName.FONT ) )
        {
            throw new IOException( "Cannot create font if /Type is not /Font.  Actual=" +type );
        }

        COSName subType = (COSName)dic.getDictionaryObject( COSName.SUBTYPE );
        if( subType.equals( COSName.getPDFName( "Type1" ) ) )
        {
            retval = new PDType1Font( dic );
        }
        else if( subType.equals( COSName.getPDFName( "MMType1" ) ) )
        {
            retval = new PDMMType1Font( dic );
        }
        else if( subType.equals( COSName.getPDFName( "TrueType" ) ) )
        {
            retval = new PDTrueTypeFont( dic );
        }
        else if( subType.equals( COSName.getPDFName( "Type3" ) ) )
        {
            retval = new PDType3Font( dic );
        }
        else if( subType.equals( COSName.getPDFName( "Type0" ) ) )
        {
            retval = new PDType0Font( dic );
        }
        else if( subType.equals( COSName.getPDFName( "CIDFontType0" ) ) )
        {
            retval = new PDCIDFontType0Font( dic );
        }
        else if( subType.equals( COSName.getPDFName( "CIDFontType2" ) ) )
        {
            retval = new PDCIDFontType2Font( dic );
        }
        else
        {
            throw new IOException( "Unknown font subtype=" + subType );
        }

        return retval;
    }
}