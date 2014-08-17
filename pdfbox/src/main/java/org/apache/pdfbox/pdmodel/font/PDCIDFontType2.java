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
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Type 2 CIDFont (TrueType).
 * 
 * @author Ben Litchfield
 */
public class PDCIDFontType2 extends PDCIDFont
{
    private static final Log LOG = LogFactory.getLog(PDCIDFontType2.class);

    private final TrueTypeFont ttf;
    private Boolean hasCIDToGIDMap = null;
    private Boolean hasIdentityCIDToGIDMap = null;
    private int[] cid2gid = null;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType2(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        super(fontDictionary, parent);

        PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary) getFontDescriptor();
        PDStream ff2Stream = fd.getFontFile2();

        if (ff2Stream != null)
        {
            // embedded
            TTFParser ttfParser = new TTFParser(true);
            ttf = ttfParser.parseTTF(ff2Stream.createInputStream());
        }
        else
        {
            // substitute
            TrueTypeFont ttfSubstitute = ExternalFonts.getTrueTypeFont(getBaseFont());
            if (ttfSubstitute != null)
            {
                ttf = ttfSubstitute;
            }
            else
            {
                // fallback
                LOG.warn("Using fallback font for " + getBaseFont());
                ttf = ExternalFonts.getFallbackFont();
            }
        }
    }

    /**
     * Indicates if this font has a CIDToGIDMap.
     * 
     * @return returns true if the font has a CIDToGIDMap.
     */
    public boolean hasCIDToGIDMap()
    {
        if (hasCIDToGIDMap == null)
        {
            COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
            if (map != null && map instanceof COSStream)
            {
                hasCIDToGIDMap = Boolean.TRUE;
            }
            else
            {
                hasCIDToGIDMap = Boolean.FALSE;
            }
        }
        return hasCIDToGIDMap;
    }

    /**
     * Indicates if this font has an identity CIDToGIDMap.
     * 
     * @return returns true if the font has an identity CIDToGIDMap.
     */
    public boolean hasIdentityCIDToGIDMap()
    {
        if (hasIdentityCIDToGIDMap == null)
        {
            COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
            if (map != null && map instanceof COSName)
            {
                hasIdentityCIDToGIDMap = Boolean.TRUE;
            }
            else
            {
                hasIdentityCIDToGIDMap = Boolean.FALSE;
            }
        }
        return hasIdentityCIDToGIDMap;
    }

    /**
     * Maps the given CID to the correspondent GID.
     * 
     * @param cid the given CID
     * @return the mapped GID, or -1 if something went wrong.
     */
    public int mapCIDToGID(int cid)
    {
        if (hasCIDToGIDMap())
        {
            if (cid2gid == null)
            {
                readCIDToGIDMapping();
            }
            if (cid2gid != null && cid < cid2gid.length)
            {
                return cid2gid[cid];
            }
            return -1;
        }
        else
        {
            // identity is the default value
            return cid;
        }
    }

    private void readCIDToGIDMapping()
    {
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        if (map instanceof COSStream)
        {
            COSStream stream = (COSStream) map;
            try
            {
                InputStream is = stream.getUnfilteredStream();
                byte[] mapAsBytes = IOUtils.toByteArray(is);
                IOUtils.closeQuietly(is);
                int numberOfInts = mapAsBytes.length / 2;
                cid2gid = new int[numberOfInts];
                int offset = 0;
                for (int index = 0; index < numberOfInts; index++)
                {
                    cid2gid[index] = getCodeFromArray(mapAsBytes, offset, 2);
                    offset += 2;
                }
            }
            catch (IOException exception)
            {
                LOG.error("Can't read the CIDToGIDMap", exception);
            }
        }
    }

    /**
     * Returns the embedded or substituted TrueType font.
     */
    public TrueTypeFont getTrueTypeFont()
    {
        return ttf;
    }

    @Override
    public float getFontWidth(byte[] c, int offset, int length)
    {
        // a suitable mapping is needed to address the correct width value
        int code = getCodeFromArray(c, offset, length);
        if (hasIdentityCIDToGIDMap() || hasCIDToGIDMap())
        {
            return getFontWidth(code);
        }
        else if (getParent().getCMap() != null)
        {
            String mappedString = getParent().getCMap().lookup(code, length);
            if (mappedString != null)
            {
                return getFontWidth(mappedString.codePointAt(0));
            }
        }
        return super.getFontWidth(c, offset, length);
    }
}
