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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is implementation of the Type3 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDType3Font extends PDSimpleFont
{

	private PDResources type3Resources = null;
    
    private COSDictionary charProcs = null;
    
    /**
     * Constructor.
     */
    public PDType3Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TYPE3 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType3Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * Set the font matrix for this type3 font.
     *
     * @param matrix The font matrix for this type3 font.
     */
    public void setFontMatrix( PDMatrix matrix )
    {
        font.setItem( COSName.FONT_MATRIX, matrix );
    }


    /**
     * Returns the optional resources of the type3 stream.
     * 
     * @return the resources bound to be used when parsing the type3 stream 
     */
    public PDResources getType3Resources( )
    {
        if (type3Resources == null)
        {
            COSDictionary resources = (COSDictionary)font.getDictionaryObject( COSName.RESOURCES );
            if (resources != null)
            {
            	type3Resources = new PDResources( resources );
            }
        }
        return type3Resources;
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     *
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        COSArray rect = (COSArray)font.getDictionaryObject( COSName.FONT_BBOX );
        PDRectangle retval = null;
        if( rect != null )
        {
            retval = new PDRectangle( rect );
        }
        return retval;
    }
    
    /**
     * Returns the dictionary containing all streams to be used to render the glyphs.
     * 
     * @return the dictionary containing all glyph streams.
     */
    public COSDictionary getCharProcs()
    {
        if (charProcs == null)
        {
        	charProcs = (COSDictionary)font.getDictionaryObject( COSName.CHAR_PROCS );
        }
        return charProcs;
    }
    
    /**
     * Returns the stream of the glyph representing by the given character
     * 
     * @param character the represented character
     * @return the stream to be used to render the glyph
     * @throws IOException If something went wrong when getting the stream.
     */
    public COSStream getCharStream(Character character) throws IOException
    {
    	COSStream stream = null;
        String cMapsTo = getFontEncoding().getName(character);
        if (cMapsTo != null)
        {
        	stream = (COSStream)getCharProcs().getDictionaryObject( COSName.getPDFName( cMapsTo ) );
        }
        return stream;
    }
}
