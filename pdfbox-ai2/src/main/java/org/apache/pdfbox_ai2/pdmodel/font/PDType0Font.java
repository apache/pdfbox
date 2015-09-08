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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox_ai2.cmap.CMap;
import org.apache.fontbox_ai2.util.BoundingBox;
import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.pdmodel.PDDocument;
import org.apache.pdfbox_ai2.util.Matrix;
import org.apache.pdfbox_ai2.util.Vector;

/**
 * A Composite (Type 0) font.
 *
 * @author Ben Litchfield
 */
public class PDType0Font extends PDFont implements PDVectorFont
{
    private static final Log LOG = LogFactory.getLog(PDType0Font.class);

    private final PDCIDFont descendantFont;
    private CMap cMap, cMapUCS2;
    private boolean isCMapPredefined;
    private boolean isDescendantCJK;
    private PDCIDFontType2Embedder embedder;
    
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
        return new PDType0Font(doc, new FileInputStream(file), true);
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
        return new PDType0Font(doc, input, true);
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
        return new PDType0Font(doc, input, embedSubset);
    }

    /**
     * Constructor for reading a Type0 font from a PDF file.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType0Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        COSArray descendantFonts = (COSArray)dict.getDictionaryObject(COSName.DESCENDANT_FONTS);
        COSDictionary descendantFontDictionary = (COSDictionary) descendantFonts.getObject(0);

        if (descendantFontDictionary == null)
        {
            throw new IOException("Missing descendant font dictionary");
        }

        descendantFont = PDFontFactory.createDescendantFont(descendantFontDictionary, this);
        readEncoding();
        fetchCMapUCS2();
    }

    /**
    * Private. Creates a new TrueType font for embedding.
    */
    private PDType0Font(PDDocument document, InputStream ttfStream, boolean embedSubset)
            throws IOException
    {
        embedder = new PDCIDFontType2Embedder(document, dict, ttfStream, embedSubset, this);
        descendantFont = embedder.getCIDFont();
        readEncoding();
        fetchCMapUCS2();
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
            isDescendantCJK = ros.getRegistry().equals("Adobe") &&
                    (ros.getOrdering().equals("GB1") || 
                     ros.getOrdering().equals("CNS1") ||
                     ros.getOrdering().equals("Japan1") ||
                     ros.getOrdering().equals("Korea1"));
        }
    }

    /**
     * Fetches the corresponding UCS2 CMap if the font's CMap is predefined.
     */
    private void fetchCMapUCS2() throws IOException
    {
        // if the font is composite and uses a predefined cmap (excluding Identity-H/V) then
        // or if its decendant font uses Adobe-GB1/CNS1/Japan1/Korea1
        if (isCMapPredefined)
        {
            // a) Map the character code to a CID using the font's CMap
            // b) Obtain the ROS from the font's CIDSystemInfo
            // c) Construct a second CMap name by concatenating the ROS in the format "R-O-UCS2"
            // d) Obtain the CMap with the constructed name
            // e) Map the CID according to the CMap from step d), producing a Unicode value

            String cMapName = null;

            // get the encoding CMap
            COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
            if (encoding instanceof COSName)
            {
                cMapName = ((COSName)encoding).getName();
            }
            
            if ("Identity-H".equals(cMapName) || "Identity-V".equals(cMapName))
            {
                if (isDescendantCJK)
                {
                    cMapName = getCJKCMap(descendantFont.getCIDSystemInfo());
                }
                else
                {
                    // we can't map Identity-H or Identity-V to Unicode
                    return;
                }
            }
            
            // try to find the corresponding Unicode (UC2) CMap
            if (cMapName != null)
            {
                CMap cMap = CMapManager.getPredefinedCMap(cMapName);
                if (cMap != null)
                {
                    String ucs2Name = cMap.getRegistry() + "-" + cMap.getOrdering() + "-UCS2";
                    CMap ucs2CMap = CMapManager.getPredefinedCMap(ucs2Name);
                    if (ucs2CMap != null)
                    {
                        cMapUCS2 = ucs2CMap;
                    }
                }
            }
        }
    }

    /**
     * Returns the name of CJK CMap represented by the given CIDSystemInfo, if any. 
     */
    private String getCJKCMap(PDCIDSystemInfo ros)
    {
        // CJK can fallback to using CIDSystemInfo
        if (ros.getOrdering().equals("GB1"))
        {
            return "Adobe-GB1-0";
        }
        else if (ros.getOrdering().equals("CNS1"))
        {
            return "Adobe-CNS1-0";
        }
        else if (ros.getOrdering().equals("Japan1"))
        {
            return "Adobe-Japan1-1";
        }
        else if (ros.getOrdering().equals("Korea1"))
        {
            return "Adobe-Korea1-0";
        }
        else
        {
            throw new IllegalStateException();
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

        if (isCMapPredefined && cMapUCS2 != null)
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
            // if no value has been produced, there is no way to obtain Unicode for the character.
            String cid = "CID+" + codeToCID(code);
            LOG.warn("No Unicode mapping for " + cid + " (" + code + ") in font " + getName());
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
    public boolean hasGlyph(int code) throws IOException
    {
        return descendantFont.hasGlyph(code);
    }
}
