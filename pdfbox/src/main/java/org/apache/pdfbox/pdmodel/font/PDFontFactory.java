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
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * Logger instance.
     */
    private static final Log log = LogFactory.getLog(PDFontFactory.class);
    
    /**
     * This will create the correct font based on information in the dictionary.
     *
     * @param dic The populated dictionary.
     *
     * @param fontCache A Map to cache already created fonts
     *
     * @return The corrent implementation for the font.
     *
     * @throws IOException If the dictionary is not valid.
     */
    public static PDFont createFont(COSDictionary dic, Map fontCache) throws IOException
    {
        PDFont retval = null;
        if (fontCache != null) 
        {
            String fontKey = dic.getNameAsString(COSName.BASE_FONT) + dic.getNameAsString(COSName.NAME)
                                + dic.getNameAsString(COSName.SUBTYPE);
            if (dic.getItem(COSName.ENCODING) != null)
            {
                fontKey += dic.getItem(COSName.ENCODING).toString();
            }
            if (fontCache.containsKey(fontKey))
            {
                retval = (PDFont)fontCache.get(fontKey);
            }
            else 
            {
                retval = PDFontFactory.createFont( dic );
                fontCache.put(fontKey, retval);
            }
        }
        else
        {
            retval = PDFontFactory.createFont( dic );
        }
        return retval;
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
        if( subType.equals( COSName.TYPE1) )
        {
            retval = new PDType1Font( dic );
        }
        else if( subType.equals( COSName.MM_TYPE1 ) )
        {
            retval = new PDMMType1Font( dic );
        }
        else if( subType.equals( COSName.TRUE_TYPE ) )
        {
            retval = new PDTrueTypeFont( dic );
        }
        else if( subType.equals( COSName.TYPE3 ) )
        {
            retval = new PDType3Font( dic );
        }
        else if( subType.equals( COSName.TYPE0 ) )
        {
            retval = new PDType0Font( dic );
        }
        else if( subType.equals( COSName.CID_FONT_TYPE0 ) )
        {
            retval = new PDCIDFontType0Font( dic );
        }
        else if( subType.equals( COSName.CID_FONT_TYPE2 ) )
        {
            retval = new PDCIDFontType2Font( dic );
        }
        else
        {
            log.warn("Substituting TrueType for unknown font subtype=" + dic.getDictionaryObject( COSName.SUBTYPE ).toString());
            //throw new IOException( "Unknown font subtype=" + subType );
            retval = new PDTrueTypeFont( dic );
        }
        return retval;
    }
}
