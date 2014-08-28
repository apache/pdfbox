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
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

/**
 * A PostScript Type 3 Font.
 *
 * @author Ben Litchfield
 */
public class PDType3Font extends PDSimpleFont
{
    private static final Log LOG = LogFactory.getLog(PDFont.class);

	private PDResources type3Resources = null;
    private COSDictionary charProcs = null;
    private Matrix fontMatrix;

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType3Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        readEncoding();
    }

    @Override
    public String getName()
    {
        return dict.getNameAsString(COSName.NAME);
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        throw new UnsupportedOperationException("not supported for Type 3 fonts");
    }

    @Override
    protected Boolean isFontSymbolic()
    {
        return false;
    }

    @Override
    public float getWidth(int code) throws IOException
    {
        int firstChar = dict.getInt(COSName.FIRST_CHAR, -1);
        int lastChar = dict.getInt(COSName.LAST_CHAR, -1);
        if (getWidths().size() > 0 && code >= firstChar && code <= lastChar)
        {
            return getWidths().get(code - firstChar).floatValue();
        }
        else
        {
            PDFontDescriptor fd = getFontDescriptor();
            if (fd instanceof PDFontDescriptorDictionary)
            {
                return fd.getMissingWidth();
            }
            else
            {
                // todo: call getWidthFromFont?
                LOG.error("No width for glyph " + code + " in font " + getName());
                return 0;
            }
        }
    }

    @Override
    protected float getWidthFromFont(int code)
    {
       // todo: could these be extracted from the font's stream?
       throw new UnsupportedOperationException("not suppported");
    }

    @Override
    public boolean isEmbedded()
    {
        return true;
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    @Override
    public Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            COSArray array = (COSArray) dict.getDictionaryObject(COSName.FONT_MATRIX);
            if (array != null)
            {
                fontMatrix = new Matrix(array);
            }
            else
            {
                return super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    /**
     * Returns the optional resources of the type3 stream.
     *
     * @return the resources bound to be used when parsing the type3 stream
     */
    public PDResources getType3Resources()
    {
        if (type3Resources == null)
        {
            COSDictionary resources = (COSDictionary) dict.getDictionaryObject(COSName.RESOURCES);
            if (resources != null)
            {
            	type3Resources = new PDResources(resources);
            }
        }
        return type3Resources;
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getBoundingBox() throws IOException
    {
        COSArray rect = (COSArray) dict.getDictionaryObject(COSName.FONT_BBOX);
        PDRectangle retval = null;
        if(rect != null)
        {
            retval = new PDRectangle(rect);
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
        	charProcs = (COSDictionary) dict.getDictionaryObject(COSName.CHAR_PROCS);
        }
        return charProcs;
    }
    
    /**
     * Returns the stream of the glyph representing by the given character
     * 
     * @param code char code
     * @return the stream to be used to render the glyph
     * @throws IOException If something went wrong when getting the stream.
     */
    public COSStream getCharStream(int code) throws IOException
    {
    	COSStream stream = null;
        String cMapsTo = getEncoding().getName(code);
        if (cMapsTo != null)
        {
        	stream = (COSStream)getCharProcs().getDictionaryObject(COSName.getPDFName(cMapsTo));
        }
        return stream;
    }

    @Override
    public void clear()
    {
        super.clear();
        charProcs = null;
        if (type3Resources != null)
        {
            type3Resources.clearCache();
            type3Resources = null;
        }
    }
}
