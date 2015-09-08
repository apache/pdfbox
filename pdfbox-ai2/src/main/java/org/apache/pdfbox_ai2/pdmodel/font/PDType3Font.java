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
package org.apache.pdfbox_ai2.pdmodel.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox_ai2.FontBoxFont;
import org.apache.fontbox_ai2.util.BoundingBox;
import org.apache.pdfbox_ai2.contentstream.operator.Operator;
import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.cos.COSNumber;
import org.apache.pdfbox_ai2.cos.COSObject;
import org.apache.pdfbox_ai2.cos.COSStream;
import org.apache.pdfbox_ai2.pdfparser.PDFStreamParser;
import org.apache.pdfbox_ai2.pdmodel.PDResources;
import org.apache.pdfbox_ai2.pdmodel.common.PDRectangle;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox_ai2.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox_ai2.util.Matrix;
import org.apache.pdfbox_ai2.util.Vector;

/**
 * A PostScript Type 3 Font.
 *
 * @author Ben Litchfield
 */
public class PDType3Font extends PDSimpleFont
{
    private static final Log LOG = LogFactory.getLog(PDType3Font.class);

    private static final String D0_OPERATOR = "d0";
    private static final String D1_OPERATOR = "d1";

    private PDResources resources;
    private COSDictionary charProcs;
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
    protected final void readEncoding() throws IOException
    {
        COSDictionary encodingDict = (COSDictionary)dict.getDictionaryObject(COSName.ENCODING);
        encoding = new DictionaryEncoding(encodingDict);
        glyphList = GlyphList.getZapfDingbats();
    }
    
    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        // Type 3 fonts do not have a built-in encoding
        throw new UnsupportedOperationException("not supported for Type 3 fonts");
    }

    @Override
    protected Boolean isFontSymbolic()
    {
        return false;
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        // Type 3 fonts do not use vector paths
        throw new UnsupportedOperationException("not supported for Type 3 fonts");
    }

    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        COSStream stream = (COSStream) getCharProcs().getDictionaryObject(COSName.getPDFName(name));
        return stream != null;
    }

    @Override
    public FontBoxFont getFontBoxFont()
    {
        // Type 3 fonts do not use FontBox fonts
        throw new UnsupportedOperationException("not supported for Type 3 fonts");
    }

    @Override
    public Vector getDisplacement(int code) throws IOException
    {
        return getFontMatrix().transform(new Vector(getWidth(code), 0));
    }

    @Override
    public float getWidth(int code) throws IOException
    {
        int firstChar = dict.getInt(COSName.FIRST_CHAR, -1);
        int lastChar = dict.getInt(COSName.LAST_CHAR, -1);
        if (getWidths().size() > 0 && code >= firstChar && code <= lastChar)
        {
            return getWidths().get(code - firstChar);
        }
        else
        {
            PDFontDescriptor fd = getFontDescriptor();
            if (fd != null)
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
    public float getWidthFromFont(int code) throws IOException
    {
        try
        {
            PDType3CharProc charProc = getCharProc(code);
            if (charProc == null)
            {
                throw new IOException("No CharProc for glyph " + code + " found");
            }
            List<COSBase> arguments = new ArrayList<COSBase>();
            PDFStreamParser parser = new PDFStreamParser(charProc);
            Object token = parser.parseNextToken();
            while (token != null)
            {
                if (token instanceof COSObject)
                {
                    arguments.add(((COSObject) token).getObject());
                }
                else if (token instanceof Operator)
                {
                    return parseType3WidthOperator((Operator) token, arguments);
                }
                else
                {
                    arguments.add((COSBase) token);
                }
                token = parser.parseNextToken();
            }
        }
        catch (IOException e)
        {
            LOG.error("Error processing CharProc for glyph " + code);
            LOG.error(e);
        }
        return -1;
    }

    private float parseType3WidthOperator(Operator operator, List arguments) throws IOException
    {
        if (operator.getName().equals(D0_OPERATOR) || operator.getName().equals(D1_OPERATOR))
        {
            Object obj = arguments.get(0);
            if (obj instanceof Number)
            {
                return ((Number) obj).floatValue();
            }
            else if (obj instanceof COSNumber)
            {
                return ((COSNumber) obj).floatValue();
            }
            else
            {
                throw new IOException("Unexpected argument type. Expected : COSInteger or Number / Received : "
                        + obj.getClass().getName());
            }
        }
        else
        {
            throw new IOException("Type3 CharProc : First operator must be d0 or d1");
        }
    }

    @Override
    public boolean isEmbedded()
    {
        return true;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        PDFontDescriptor desc = getFontDescriptor();
        if (desc != null)
        {
            // the following values are all more or less accurate at least all are average
            // values. Maybe we'll find another way to get those value for every single glyph
            // in the future if needed
            PDRectangle fontBBox = desc.getFontBoundingBox();
            float retval = 0;
            if (fontBBox != null)
            {
                retval = fontBBox.getHeight() / 2;
            }
            if (retval == 0)
            {
                retval = desc.getCapHeight();
            }
            if (retval == 0)
            {
                retval = desc.getAscent();
            }
            if (retval == 0)
            {
                retval = desc.getXHeight();
                if (retval > 0)
                {
                    retval -= desc.getDescent();
                }
            }
            return retval;
        }
        return 0;
    }

    @Override
    protected byte[] encode(int unicode) throws IOException
    {
        throw new UnsupportedOperationException("Not implemented: Type3");
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

    @Override
    public boolean isDamaged()
    {
        // there's no font file to load
        return false;
    }

    /**
     * Returns the optional resources of the type3 stream.
     *
     * @return the resources bound to be used when parsing the type3 stream
     */
    public PDResources getResources()
    {
        if (resources == null)
        {
            COSDictionary resources = (COSDictionary) dict.getDictionaryObject(COSName.RESOURCES);
            if (resources != null)
            {
                this.resources = new PDResources(resources);
            }
        }
        return resources;
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     */
    public PDRectangle getFontBBox()
    {
        COSArray rect = (COSArray) dict.getDictionaryObject(COSName.FONT_BBOX);
        PDRectangle retval = null;
        if(rect != null)
        {
            retval = new PDRectangle(rect);
        }
        return retval;
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        PDRectangle rect = getFontBBox();
        return new BoundingBox(rect.getLowerLeftX(), rect.getLowerLeftY(),
                               rect.getWidth(), rect.getHeight());
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
     * Returns the stream of the glyph for the given character code
     * 
     * @param code character code
     * @return the stream to be used to render the glyph
     */
    public PDType3CharProc getCharProc(int code)
    {
        String name = getEncoding().getName(code);
        if (!name.equals(".notdef"))
        {
            COSStream stream;
            stream = (COSStream)getCharProcs().getDictionaryObject(COSName.getPDFName(name));
            if (stream == null)
            {
                return null;
            }
            return new PDType3CharProc(this, stream);
        }
        return null;
    }
}
