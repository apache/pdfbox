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
package org.pdfbox.pdmodel.font;

import java.awt.Graphics;

import java.io.IOException;

import java.util.HashMap;

import org.fontbox.afm.FontMetric;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.encoding.Encoding;

import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This class contains implementation details of the simple pdf fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.18 $
 */
public abstract class PDSimpleFont extends PDFont
{
    private HashMap mFontSizes = new HashMap(128);
    private float avgFontWidth = 0.0f;
    
    /**
     * Constructor.
     */
    public PDSimpleFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDSimpleFont( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * {@inheritDoc}
     */
    public void drawString( String string, Graphics g, float fontSize, 
        float xScale, float yScale, float x, float y ) throws IOException
    {
        System.err.println( "Not yet implemented:" + getClass().getName() );
    }
    
    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontHeight( byte[] c, int offset, int length ) throws IOException
    {
        float retval = 0;
        int code = getCodeFromArray( c, offset, length );
        FontMetric metric = getAFM();
        if( metric != null )
        {
            Encoding encoding = getEncoding();
            COSName characterName = encoding.getName( code );
            retval = metric.getCharacterHeight( characterName.getName() );
        }
        else
        {
            PDFontDescriptor desc = getFontDescriptor();
            if( desc != null )
            {
                float xHeight = desc.getXHeight();
                float capHeight = desc.getCapHeight();
                if( xHeight != 0f && capHeight != 0 )
                {
                    //do an average of these two.  Can we do better???
                    retval = (xHeight + capHeight)/2f;
                }
                else if( xHeight != 0 )
                {
                    retval = xHeight;
                }
                else if( capHeight != 0 )
                {
                    retval = capHeight;
                }
                else
                {
                    retval = 0;
                }
                //hmm, not sure if this is 100% correct
                //but gives a height, Should we add Descent as well??
                if( retval == 0 )
                {
                    retval = desc.getAscent();
                }
            }
        }
        return retval;
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth( byte[] c, int offset, int length ) throws IOException
    {
        float fontWidth = 0;
        int code = getCodeFromArray( c, offset, length );

        Integer codeI = new Integer(code);
        if (mFontSizes.containsKey(codeI))
        {
            Float fontWidthF = (Float) mFontSizes.get(codeI);
            fontWidth = fontWidthF.floatValue();
        }
        else 
        {
            //hmm should this be in a subclass??
            COSInteger firstChar = (COSInteger)font.getDictionaryObject( COSName.FIRST_CHAR );
            COSInteger lastChar = (COSInteger)font.getDictionaryObject( COSName.LAST_CHAR );
            if( firstChar != null && lastChar != null )
            {
                long first = firstChar.intValue();
                long last = lastChar.intValue();
                if( code >= first && code <= last && font.getDictionaryObject( COSName.WIDTHS ) != null )
                {
                    COSArray widthArray = (COSArray)font.getDictionaryObject( COSName.WIDTHS );
                    COSNumber fontWidthObject = (COSNumber)widthArray.getObject( (int)(code - first) );
                    fontWidth = fontWidthObject.floatValue();
                }
                else
                {
                    fontWidth = getFontWidthFromAFMFile( code );
                }
            }
            else
            {
                fontWidth = getFontWidthFromAFMFile( code );
            }
            mFontSizes.put(codeI, new Float(fontWidth));
        }
        return fontWidth;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getAverageFontWidth() throws IOException
    {
        float average = 0.0f;

        //AJW
        if (avgFontWidth != 0.0f)
        {
            average = avgFontWidth;
        }
        else 
        {
            float totalWidth = 0.0f;
            float characterCount = 0.0f;
            COSArray widths = (COSArray)font.getDictionaryObject( COSName.WIDTHS );
            if( widths != null )
            {
                for( int i=0; i<widths.size(); i++ )
                {
                    COSNumber fontWidth = (COSNumber)widths.getObject( i );
                    if( fontWidth.floatValue() > 0 )
                    {
                        totalWidth += fontWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }

            if( totalWidth > 0 )
            {
                average = totalWidth / characterCount;
            }
            else
            {
                average = getAverageFontWidthFromAFMFile();
            }
            avgFontWidth = average;
        }
        return average;
    }

    /**
     * This will get the font descriptor for this font.
     *
     * @return The font descriptor for this font.
     *
     * @throws IOException If there is an error parsing an AFM file, or unable to
     *      create a PDFontDescriptor object.
     */
    public PDFontDescriptor getFontDescriptor() throws IOException
    {
        PDFontDescriptor retval = null;
        COSDictionary fd = (COSDictionary)font.getDictionaryObject( COSName.getPDFName( "FontDescriptor" ) );
        if( fd == null )
        {
            FontMetric afm = getAFM();
            if( afm != null )
            {
                retval = new PDFontDescriptorAFM( afm );
            }
        }
        else
        {
            retval = new PDFontDescriptorDictionary( fd );
        }

        return retval;
    }

    /**
     * This will set the font descriptor.
     *
     * @param fontDescriptor The font descriptor.
     */
    public void setFontDescriptor( PDFontDescriptorDictionary fontDescriptor )
    {
        COSDictionary dic = null;
        if( fontDescriptor != null )
        {
            dic = fontDescriptor.getCOSDictionary();
        }
        font.setItem( COSName.getPDFName( "FontDescriptor" ), dic );
    }
    
    /**
     * This will get the ToUnicode stream.
     * 
     * @return The ToUnicode stream.
     * @throws IOException If there is an error getting the stream.
     */
    public PDStream getToUnicode() throws IOException
    {
        return PDStream.createFromCOS( font.getDictionaryObject( "ToUnicode" ) );
    }
    
    /**
     * This will set the ToUnicode stream.
     * 
     * @param unicode The unicode stream.
     */
    public void setToUnicode( PDStream unicode )
    {
        font.setItem( "ToUnicode", unicode );
    }
    
    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bouding box.
     * 
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        return getFontDescriptor().getFontBoundingBox();
    }
}