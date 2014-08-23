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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Type 0 CIDFont (CFF).
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCIDFontType0 extends PDCIDFont
{
    private static final Log LOG = LogFactory.getLog(PDCIDFontType0.class);

    private CFFCIDFont cffFont = null;
    private final Map<Integer, Float> glyphHeights = new HashMap<Integer, Float>();
    private Float avgWidth = null;
    private final boolean isEmbedded;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType0(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        super(fontDictionary, parent);

        PDFontDescriptor fd = getFontDescriptor();
        byte[] bytes = null;
        if (fd != null && fd instanceof PDFontDescriptorDictionary) // <-- todo: must be true
        {
            PDStream ff3Stream = ((PDFontDescriptorDictionary) fd).getFontFile3();
            if (ff3Stream != null)
            {
                bytes = IOUtils.toByteArray(ff3Stream.createInputStream());
            }
        }

        if (bytes != null)
        {
            // embedded
            CFFParser cffParser = new CFFParser();
            cffFont = (CFFCIDFont)cffParser.parse(bytes).get(0);
            isEmbedded = true;
        }
        else
        {
            // substitute
            cffFont = ExternalFonts.getCFFCIDFont(getBaseFont());

            if (cffFont == null)
            {
                // todo: log message + substitute? But what would we substitute with?
                throw new UnsupportedOperationException("not implemented: missing CFF");
            }
            isEmbedded = false;
        }
    }

    /**
     * Returns the embedded CFF CIDFont.
     */
    public CFFCIDFont getCFFCIDFont()
    {
        return cffFont;
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    public int codeToCID(int code)
    {
        return parent.getCMap().toCID(code);
    }

    @Override
    public int codeToGID(int code)
    {
        int cid = codeToCID(code);
        return cffFont.getCharset().getGIDForCID(cid);
    }

    @Override
    protected float getWidthFromFont(int code) throws IOException
    {
        int cid = codeToCID(code);
        return cffFont.getType2CharString(cid).getWidth();
    }

    @Override
    protected boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        int cid = codeToCID(code);

        float height = 0;
        if (!glyphHeights.containsKey(cid))
        {
            height =  (float)cffFont.getType2CharString(cid).getBounds().getHeight();
            glyphHeights.put(cid, height);
        }
        return height;
    }

    @Override
    public float getAverageFontWidth()
    {
        if (avgWidth == null)
        {
            avgWidth = getAverageCharacterWidth();
        }
        return avgWidth;
    }

    // todo: this is a replacement for FontMetrics method
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }

    @Override
    public void clear()
    {
        super.clear();
        if (cffFont != null)
        {
            //cffFont.clear();
            cffFont = null;
        }
    }
}
