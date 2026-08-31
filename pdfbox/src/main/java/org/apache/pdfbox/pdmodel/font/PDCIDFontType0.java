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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.Type2CharString;
import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;


import static org.apache.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;

/**
 * Type 0 CIDFont (CFF).
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCIDFontType0 extends PDCIDFont
{
    private static final Logger LOG = LogManager.getLogger(PDCIDFontType0.class);

    private final CFFCIDFont cidFont;  // Top DICT that uses CIDFont operators
    private final FontBoxFont t1Font; // Top DICT that does not use CIDFont operators

    // substitute is CID-keyed with a different ROS: this font's CIDs are meaningless in it,
    // resolve glyphs via Unicode instead (PDFBOX-6249)
    private final CmapLookup substituteUnicodeCmap;
    
    private final Map<Integer, Float> glyphHeights = new HashMap<>();
    private final AffineTransform fontMatrixTransform;
    private Float avgWidth = null;
    private Matrix fontMatrix;
    private BoundingBox fontBBox;
    private int[] cid2gid = null;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     * @param resourceCache ResourceCache, can be null.
     * 
     * @throws IOException if the font could not be read
     */
    public PDCIDFontType0(COSDictionary fontDictionary, ResourceCache resourceCache)
            throws IOException
    {
        super(fontDictionary, resourceCache);

        boolean fontIsDamaged = false;
        CmapLookup substituteCmap = null;
        CFFFont cffFont = null;
        PDFontDescriptor fd = getFontDescriptor();
        if (fd != null)
        {
            PDStream ff3Stream = fd.getFontFile3();
            if (ff3Stream != null)
            {
                try (RandomAccessRead randomAccessRead = ff3Stream.getCOSObject().createView())
                {
                    if (randomAccessRead.length() > 0 && randomAccessRead.peek() == '%')
                    {
                        // PDFBOX-2642 contains a corrupt PFB font instead of a CFF
                        LOG.warn("Found PFB but expected embedded CFF font {}", fd.getFontName());
                        fontIsDamaged = true;
                    }
                    else
                    {
                        CFFParser cffParser = new CFFParser();
                        cffFont = cffParser.parse(randomAccessRead).get(0);
                    }
                }
                catch (IOException e)
                {
                    LOG.error(() -> "Can't read the embedded CFF font " + fd.getFontName(), e);
                    fontIsDamaged = true;
                }
            }
        }

        if (cffFont != null)
        {
            // embedded
            if (cffFont instanceof CFFCIDFont)
            {
                cidFont = (CFFCIDFont)cffFont;
                t1Font = null;
            }
            else
            {
                cidFont = null;
                t1Font = cffFont;
            }
            cid2gid = readCIDToGIDMap();
            isEmbedded = true;
            isDamaged = false;
        }
        else
        {
            // find font or substitute
            CIDFontMapping mapping = FontMappers.instance().getCIDFont(getBaseFont(), fd,
                    getCIDSystemInfo());
            FontBoxFont font;
            if (mapping.isCIDFont())
            {
                cffFont = mapping.getFont().getCFF().getFont();
                if (cffFont instanceof CFFCIDFont)
                {
                    cidFont = (CFFCIDFont) cffFont;
                    t1Font = null;
                    font = cidFont;
                }
                else
                {
                    // PDFBOX-3515: OpenType fonts are loaded as CFFType1Font
                    CFFType1Font f = (CFFType1Font) cffFont;
                    cidFont = null;
                    t1Font = f;
                    font = f;
                }
            }
            else
            {
                cidFont = null;
                t1Font = mapping.getTrueTypeFont();
                font = t1Font;
            }

            if (mapping.isFallback())
            {
                LOG.warn("Using fallback {} for CID-keyed font {}", font.getName(), getBaseFont());
            }
            if (cidFont != null && mapping.isCIDFont() && !isCharacterCollectionMatch(cidFont) &&
                "Identity".equals(cidFont.getOrdering()))
            {
                try
                {
                    substituteCmap = mapping.getFont().getUnicodeCmapLookup();
                }
                catch (IOException e)
                {
                    LOG.warn("Could not read cmap of the substitute for font {}", getBaseFont(), e);
                }
            }
            isEmbedded = false;
            isDamaged = fontIsDamaged;
        }
        substituteUnicodeCmap = substituteCmap;
        fontMatrixTransform = getFontMatrix().createAffineTransform();
        fontMatrixTransform.scale(1000, 1000);
    }
    
    @Override
    public final Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers;
            if (cidFont != null)
            {
                numbers = cidFont.getFontMatrix();
            }
            else
            {
                try
                {
                    numbers = t1Font.getFontMatrix();
                }
                catch (IOException e)
                {
                    LOG.debug("Couldn't get font matrix - returning default value", e);
                    return new Matrix(0.001f, 0, 0, 0.001f, 0, 0);
                }
            }

            if (numbers != null && numbers.size() == 6)
            {
                fontMatrix = new Matrix(numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
            }
            else
            {
                fontMatrix = new Matrix(0.001f, 0, 0, 0.001f, 0, 0);
            }
        }
        return fontMatrix;
    }
    
    @Override
    public BoundingBox getBoundingBox()
    {
        if (fontBBox == null)
        {
            fontBBox = generateBoundingBox();
        }
        return fontBBox;
    }

    private BoundingBox generateBoundingBox()
    {
        if (getFontDescriptor() != null)
        {
            PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
            if (bbox != null && (Float.compare(bbox.getLowerLeftX(),0) != 0 ||
                Float.compare(bbox.getLowerLeftY(),0) != 0 ||
                Float.compare(bbox.getUpperRightX(),0) != 0 ||
                Float.compare(bbox.getUpperRightY(),0) != 0))
            {
                return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(),
                                          bbox.getUpperRightX(), bbox.getUpperRightY());
            }
        }
        try
        {
            return cidFont != null ? cidFont.getFontBBox() : t1Font.getFontBBox();
        }
        catch (IOException e)
        {
            LOG.debug("Couldn't get font bounding box - returning default value", e);
            return new BoundingBox();
        }
    }

    /**
     * Returns the embedded CFF CIDFont, or null if the substitute is not a CFF font.
     * 
     * @return the embedded CFF CIDFont or null
     */
    public CFFFont getCFFFont()
    {
        if (cidFont != null)
        {
            return cidFont;
        }
        else if (t1Font instanceof CFFType1Font)
        {
            return (CFFType1Font)t1Font;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the embedded or substituted font.
     * 
     * @return the embedded or substituted font
     */
    public FontBoxFont getFontBoxFont()
    {
        if (cidFont != null)
        {
            return cidFont;
        }
        else
        {
            return t1Font;
        }
    }

    /**
     * Returns the Type 2 charstring for the given CID, or null if the substituted font does not contain Type 2
     * charstrings.
     *
     * @param cid CID
     * @return the Type 2 charstring for the given CID or null
     * 
     * @throws IOException if the charstring could not be read
     */
    public Type2CharString getType2CharString(int cid) throws IOException
    {
        if (cidFont != null)
        {
            return cidFont.getType2CharString(cid);
        }
        else if (t1Font instanceof CFFType1Font)
        {
            return ((CFFType1Font)t1Font).getType2CharString(cid);
        }
        else
        {
            return null;
        }
    }

    private boolean isCharacterCollectionMatch(CFFCIDFont substitute) throws IOException
    {
        PDCIDSystemInfo ros = getCIDSystemInfo();
        return ros != null && ros.getRegistry().equals(substitute.getRegistry()) &&
                ros.getOrdering().equals(substitute.getOrdering());
    }

    /**
     * GID in the substitute for the given code, via Unicode; -1 if unmapped. The substitute's
     * Identity charset makes GIDs address its charstrings directly.
     */
    private int codeToSubstituteGID(int code, PDType0Font parent)
    {
        String unicodes = parent.toUnicode(code);
        if (unicodes == null)
        {
            return -1;
        }
        return substituteUnicodeCmap.getGlyphId(unicodes.codePointAt(0));
    }

    /**
     * Returns the name of the glyph with the given character code. This is done by looking up the
     * code in the parent font's ToUnicode map and generating a glyph name from that.
     */
    private String getGlyphName(int code, PDType0Font parent)
    {
        String unicodes = parent.toUnicode(code);
        if (unicodes == null)
        {
            return ".notdef";
        }
        return getUniNameOfCodePoint(unicodes.codePointAt(0));
    }

    @Override
    protected GeneralPath getPath(int code, PDType0Font parent) throws IOException
    {
        int cid = codeToCID(code, parent);
        if (substituteUnicodeCmap != null)
        {
            int gid = codeToSubstituteGID(code, parent);
            return getType2CharString(Math.max(gid, 0)).getPath();
        }
        if (cid2gid != null && isEmbedded)
        {
            // PDFBOX-4093: despite being a type 0 font, there is a CIDToGIDMap
            cid = cid2gid[cid];
        }
        Type2CharString charstring = getType2CharString(cid);
        if (charstring != null)
        {
            return charstring.getPath();
        }
        else if (isEmbedded && t1Font instanceof CFFType1Font)
        {
            return ((CFFType1Font)t1Font).getType2CharString(cid).getPath();
        }
        else
        {
            return t1Font.getPath(getGlyphName(code, parent));
        }
    }

    @Override
    protected GeneralPath getNormalizedPath(int code, PDType0Font parent) throws IOException
    {
        return getPath(code, parent);
    }

    @Override
    protected boolean hasGlyph(int code, PDType0Font parent) throws IOException
    {
        int cid = codeToCID(code, parent);
        if (substituteUnicodeCmap != null)
        {
            return codeToSubstituteGID(code, parent) > 0;
        }
        Type2CharString charstring = getType2CharString(cid);
        if (charstring != null)
        {
            return charstring.getGID() != 0;
        }
        else if (isEmbedded && t1Font instanceof CFFType1Font)
        {
            return ((CFFType1Font)t1Font).getType2CharString(cid).getGID() != 0;
        }
        else
        {
            return t1Font.hasGlyph(getGlyphName(code, parent));
        }
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    @Override
    protected int codeToCID(int code, PDType0Font parent)
    {
        return parent.getCMap().toCID(code);
    }

    @Override
    protected int codeToGID(int code, PDType0Font parent)
    {
        int cid = codeToCID(code, parent);
        if (substituteUnicodeCmap != null)
        {
            return Math.max(codeToSubstituteGID(code, parent), 0);
        }
        if (cidFont != null)
        {
            // The CIDs shall be used to determine the GID value for the glyph procedure using the
            // charset table in the CFF program
            return cidFont.getCharset().getGIDForCID(cid);
        }
        else
        {
            // The CIDs shall be used directly as GID values
            return cid;
        }
    }

    @Override
    protected byte[] encodeGlyphId(int glyphId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected float getWidthFromFont(int code, PDType0Font parent) throws IOException
    {
        int cid = codeToCID(code, parent);
        float width;
        if (substituteUnicodeCmap != null)
        {
            width = getType2CharString(Math.max(codeToSubstituteGID(code, parent), 0)).getWidth();
        }
        else if (cidFont != null)
        {
            width = getType2CharString(cid).getWidth();
        }
        else if (isEmbedded && t1Font instanceof CFFType1Font)
        {
            width = ((CFFType1Font)t1Font).getType2CharString(cid).getWidth();
        }
        else
        {
            width = t1Font.getWidth(getGlyphName(code, parent));
        }
        
        Point2D p = new Point2D.Float(width, 0);
        fontMatrixTransform.transform(p, p);
        return (float)p.getX();
    }

    @Override
    protected float getHeight(int code, PDType0Font parent) throws IOException
    {
        int cid = codeToCID(code, parent);

        float height;
        if (!glyphHeights.containsKey(cid))
        {
            height = (float) getType2CharString(cid).getBounds().getHeight();
            glyphHeights.put(cid, height);
        }
        else
        {
            height = glyphHeights.get(cid);
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

}
