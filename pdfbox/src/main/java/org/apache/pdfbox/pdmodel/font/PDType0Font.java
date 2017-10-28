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

import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * A Composite (Type 0) font.
 *
 * @author Ben Litchfield
 */
public class PDType0Font extends PDFont implements PDVectorFont
{
    private static final Log LOG = LogFactory.getLog(PDType0Font.class);

    private final PDCIDFont descendantFont;
    private final Set<Integer> noUnicode = new HashSet<>(); 
    private CMap cMap, cMapUCS2;
    private boolean isCMapPredefined;
    private boolean isDescendantCJK;
    private PDCIDFontType2Embedder embedder;
    private TrueTypeFont ttf;
    
    /**
     * Constructor for reading a Type0 font from a PDF file.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     * @throws IOException if the descendant font is missing.
     */
    public PDType0Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        COSBase base = dict.getDictionaryObject(COSName.DESCENDANT_FONTS);
        if (!(base instanceof COSArray))
        {
            throw new IOException("Missing descendant font array");
        }
        COSArray descendantFonts = (COSArray) base;
        if (descendantFonts.size() == 0)
        {
            throw new IOException("Descendant font array is empty");
        }
        COSBase descendantFontDictBase = descendantFonts.getObject(0);
        if (!(descendantFontDictBase instanceof COSDictionary))
        {
            throw new IOException("Missing descendant font dictionary");
        }
        descendantFont = PDFontFactory.createDescendantFont((COSDictionary) descendantFontDictBase, this);
        readEncoding();
        fetchCMapUCS2();
    }

    /**
    * Private. Creates a new TrueType font for embedding.
    */
    private PDType0Font(PDDocument document, TrueTypeFont ttf, boolean embedSubset,
            boolean closeOnSubset)
            throws IOException
    {
        embedder = new PDCIDFontType2Embedder(document, dict, ttf, embedSubset, this);
        descendantFont = embedder.getCIDFont();
        readEncoding();
        fetchCMapUCS2();
        if (closeOnSubset)
        {
            if (embedSubset)
            {
                this.ttf = ttf;
            }
            else
            {
                // the TTF is fully loaded and it is save to close the underlying data source
                ttf.close();
            }
        }

    }

    /**
    * Loads a TTF to be embedded into a document as a Type 0 font.
    *
    * @param doc The PDF document that will hold the embedded font.
    * @param file A TrueType font.
    * @return A Type0 font with a CIDFontType2 descendant.
    * @throws IOException If there is an error reading the font file.
    */
    public static PDType0Font load(PDDocument doc, File file) throws IOException
    {
        return new PDType0Font(doc, new TTFParser().parse(file), true, true);
    }

    /**
    * Loads a TTF to be embedded into a document as a Type 0 font.
    *
    * @param doc The PDF document that will hold the embedded font.
    * @param input A TrueType font.
    * @return A Type0 font with a CIDFontType2 descendant.
    * @throws IOException If there is an error reading the font stream.
    */
    public static PDType0Font load(PDDocument doc, InputStream input) throws IOException
    {
        return new PDType0Font(doc, new TTFParser().parse(input), true, true);
    }

    /**
     * Loads a TTF to be embedded into a document as a Type 0 font.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param input A TrueType font.
     * @param embedSubset True if the font will be subset before embedding
     * @return A Type0 font with a CIDFontType2 descendant.
     * @throws IOException If there is an error reading the font stream.
     */
    public static PDType0Font load(PDDocument doc, InputStream input, boolean embedSubset)
            throws IOException
    {
        return new PDType0Font(doc, new TTFParser().parse(input), embedSubset, true);
    }

    /**
     * Loads a TTF to be embedded into a document as a Type 0 font.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param ttf A TrueType font.
     * @param embedSubset True if the font will be subset before embedding
     * @return A Type0 font with a CIDFontType2 descendant.
     * @throws IOException If there is an error reading the font stream.
     */
    public static PDType0Font load(PDDocument doc, TrueTypeFont ttf, boolean embedSubset)
            throws IOException
    {
        return new PDType0Font(doc, ttf, embedSubset, false);
    }

    @Override
    public void addToSubset(int codePoint)
    {
        if (!willBeSubset())
        {
            throw new IllegalStateException("This font was created with subsetting disabled");
        }
        embedder.addToSubset(codePoint);
    }
    
    @Override
    public void subset() throws IOException
    {
        if (!willBeSubset())
        {
            throw new IllegalStateException("This font was created with subsetting disabled");
        }
        embedder.subset();
        if (ttf != null)
        {
            ttf.close();
            ttf = null;
        }
    }
    
    @Override
    public boolean willBeSubset()
    {
        return embedder != null && embedder.needsSubset();
    }

    /**
     * Reads the font's Encoding entry, which should be a CMap name/stream.
     */
    private void readEncoding() throws IOException
    {
        COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
        if (encoding instanceof COSName)
        {
            // predefined CMap
            COSName encodingName = (COSName) encoding;
            cMap = CMapManager.getPredefinedCMap(encodingName.getName());
            if (cMap != null)
            {
                isCMapPredefined = true;
            }
            else
            {
                throw new IOException("Missing required CMap");
            }
        }
        else if (encoding != null)
        {
            cMap = readCMap(encoding);
            if (cMap == null)
            {
                throw new IOException("Missing required CMap");
            }
            else if (!cMap.hasCIDMappings())
            {
                LOG.warn("Invalid Encoding CMap in font " + getName());
            }
        }
        
        // check if the descendant font is CJK
        PDCIDSystemInfo ros = descendantFont.getCIDSystemInfo();
        if (ros != null)
        {
            isDescendantCJK = "Adobe".equals(ros.getRegistry()) &&
                    ("GB1".equals(ros.getOrdering()) || 
                     "CNS1".equals(ros.getOrdering()) ||
                     "Japan1".equals(ros.getOrdering()) ||
                     "Korea1".equals(ros.getOrdering()));
        }
    }

    /**
     * Fetches the corresponding UCS2 CMap if the font's CMap is predefined.
     */
    private void fetchCMapUCS2() throws IOException
    {
        // if the font is composite and uses a predefined cmap (excluding Identity-H/V)
        // or whose descendant CIDFont uses the Adobe-GB1, Adobe-CNS1, Adobe-Japan1, or
        // Adobe-Korea1 character collection:
        COSName name = dict.getCOSName(COSName.ENCODING);
        if (isCMapPredefined && !(name == COSName.IDENTITY_H || name == COSName.IDENTITY_V) ||
            isDescendantCJK)
        {
            // a) Map the character code to a CID using the font's CMap
            // b) Obtain the ROS from the font's CIDSystemInfo
            // c) Construct a second CMap name by concatenating the ROS in the format "R-O-UCS2"
            // d) Obtain the CMap with the constructed name
            // e) Map the CID according to the CMap from step d), producing a Unicode value

            // todo: not sure how to interpret the PDF spec here, do we always override? or only when Identity-H/V?
            String strName = null;
            if (isDescendantCJK)
            {
                strName = descendantFont.getCIDSystemInfo().getRegistry() + "-" +
                          descendantFont.getCIDSystemInfo().getOrdering() + "-" +
                          descendantFont.getCIDSystemInfo().getSupplement();
            }
            else if (name != null)
            {
                strName = name.getName();
            }
            
            // try to find the corresponding Unicode (UC2) CMap
            if (strName != null)
            {
                CMap prdCMap = CMapManager.getPredefinedCMap(strName);
                String ucs2Name = prdCMap.getRegistry() + "-" + prdCMap.getOrdering() + "-UCS2";
                cMapUCS2 = CMapManager.getPredefinedCMap(ucs2Name);
            }
        }
    }

    /**
     * Returns the PostScript name of the font.
     */
    public String getBaseFont()
    {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    /**
     * Returns the descendant font.
     */
    public PDCIDFont getDescendantFont()
    {
        return descendantFont;
    }

    /**
     * Returns the font's CMap.
     */
    public CMap getCMap()
    {
        return cMap;
    }

    /**
     * Returns the font's UCS2 CMap, only present this font uses a predefined CMap.
     */
    public CMap getCMapUCS2()
    {
        return cMapUCS2;
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        return descendantFont.getFontDescriptor();
    }

    @Override
    public Matrix getFontMatrix()
    {
        return descendantFont.getFontMatrix();
    }

    @Override
    public boolean isVertical()
    {
        return cMap.getWMode() == 1;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        return descendantFont.getHeight(code);
    }

    @Override
    protected byte[] encode(int unicode) throws IOException
    {
        return descendantFont.encode(unicode);
    }

    @Override
    public float getAverageFontWidth()
    {
        return descendantFont.getAverageFontWidth();
    }

    @Override
    public Vector getPositionVector(int code)
    {
        // units are always 1/1000 text space, font matrix is not used, see FOP-2252
        return descendantFont.getPositionVector(code).scale(-1 / 1000f);
    }

    @Override
    public Vector getDisplacement(int code) throws IOException
    {
        if (isVertical())
        {
            return new Vector(0, descendantFont.getVerticalDisplacementVectorY(code) / 1000f);
        }
        else
        {
            return super.getDisplacement(code);
        }
    }

    @Override
    public float getWidth(int code) throws IOException
    {
        return descendantFont.getWidth(code);
    }

    @Override
    protected float getStandard14Width(int code)
    {
        throw new UnsupportedOperationException("not suppported");
    }

    @Override
    public float getWidthFromFont(int code) throws IOException
    {
        return descendantFont.getWidthFromFont(code);
    }

    @Override
    public boolean isEmbedded()
    {
        return descendantFont.isEmbedded();
    }

    @Override
    public String toUnicode(int code) throws IOException
    {
        // try to use a ToUnicode CMap
        String unicode = super.toUnicode(code);
        if (unicode != null)
        {
            return unicode;
        }

        if ((isCMapPredefined || isDescendantCJK) && cMapUCS2 != null)
        {
            // if the font is composite and uses a predefined cmap (excluding Identity-H/V) then
            // or if its decendant font uses Adobe-GB1/CNS1/Japan1/Korea1

            // a) Map the character code to a character identifier (CID) according to the font?s CMap
            int cid = codeToCID(code);

            // e) Map the CID according to the CMap from step d), producing a Unicode value
            return cMapUCS2.toUnicode(cid);
        }
        else
        {
            if (LOG.isWarnEnabled() && !noUnicode.contains(code))
            {
                // if no value has been produced, there is no way to obtain Unicode for the character.
                String cid = "CID+" + codeToCID(code);
                LOG.warn("No Unicode mapping for " + cid + " (" + code + ") in font " + getName());
                // we keep track of which warnings have been issued, so we don't log multiple times
                noUnicode.add(code);
            }
            return null;
        }
    }

    @Override
    public String getName()
    {
        return getBaseFont();
    }

    @Override
    public BoundingBox getBoundingBox() throws IOException
    {
        // Will be cached by underlying font
        return descendantFont.getBoundingBox();
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return cMap.readCode(in);
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    public int codeToCID(int code)
    {
        return descendantFont.codeToCID(code);
    }

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @return GID
     */
    public int codeToGID(int code) throws IOException
    {
        return descendantFont.codeToGID(code);
    }

    @Override
    public boolean isStandard14()
    {
        return false;
    }

    @Override
    public boolean isDamaged()
    {
        return descendantFont.isDamaged();
    }

    @Override
    public String toString()
    {
        String descendant = null;
        if (getDescendantFont() != null)
        {
            descendant = getDescendantFont().getClass().getSimpleName();
        }
        return getClass().getSimpleName() + "/" + descendant + " " + getBaseFont();
    }

    @Override
    public GeneralPath getPath(int code) throws IOException
    {
        return descendantFont.getPath(code);
    }

    
    @Override
    public GeneralPath getNormalizedPath(int code) throws IOException
    {
        return descendantFont.getNormalizedPath(code);
    }
    
    @Override
    public boolean hasGlyph(int code) throws IOException
    {
        return descendantFont.hasGlyph(code);
    }
}
