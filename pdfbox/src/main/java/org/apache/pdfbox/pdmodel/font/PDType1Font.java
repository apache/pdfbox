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
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.EncodedFont;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.type1.DamagedFontException;
import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.StandardEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.Type1Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.ZapfDingbatsEncoding;
import org.apache.pdfbox.util.Matrix;


import static org.apache.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;
import org.apache.pdfbox.pdmodel.font.encoding.SymbolEncoding;

/**
 * A PostScript Type 1 Font.
 *
 * @author Ben Litchfield
 */
public class PDType1Font extends PDSimpleFont implements PDVectorFont
{
    private static final Log LOG = LogFactory.getLog(PDType1Font.class);

    // todo: replace with enum? or getters?
    public static final PDType1Font TIMES_ROMAN = new PDType1Font("Times-Roman");
    public static final PDType1Font TIMES_BOLD = new PDType1Font("Times-Bold");
    public static final PDType1Font TIMES_ITALIC = new PDType1Font("Times-Italic");
    public static final PDType1Font TIMES_BOLD_ITALIC = new PDType1Font("Times-BoldItalic");
    public static final PDType1Font HELVETICA = new PDType1Font("Helvetica");
    public static final PDType1Font HELVETICA_BOLD = new PDType1Font("Helvetica-Bold");
    public static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font("Helvetica-Oblique");
    public static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font("Helvetica-BoldOblique");
    public static final PDType1Font COURIER = new PDType1Font("Courier");
    public static final PDType1Font COURIER_BOLD = new PDType1Font("Courier-Bold");
    public static final PDType1Font COURIER_OBLIQUE = new PDType1Font("Courier-Oblique");
    public static final PDType1Font COURIER_BOLD_OBLIQUE = new PDType1Font("Courier-BoldOblique");
    public static final PDType1Font SYMBOL = new PDType1Font("Symbol");
    public static final PDType1Font ZAPF_DINGBATS = new PDType1Font("ZapfDingbats");

    // alternative names for glyphs which are commonly encountered
    private static final Map<String, String> ALT_NAMES = new HashMap<String, String>();
    private static final int PFB_START_MARKER = 0x80;

    static
    {
        ALT_NAMES.put("ff", "f_f");
        ALT_NAMES.put("ffi", "f_f_i");
        ALT_NAMES.put("ffl", "f_f_l");
        ALT_NAMES.put("fi", "f_i");
        ALT_NAMES.put("fl", "f_l");
        ALT_NAMES.put("st", "s_t");
        ALT_NAMES.put("IJ", "I_J");
        ALT_NAMES.put("ij", "i_j");
        ALT_NAMES.put("ellipsis", "elipsis"); // misspelled in ArialMT
    }

    /**
     * embedded font.
     */
    private final Type1Font type1font;
    
    /**
     * embedded or system font for rendering.
     */
    private final FontBoxFont genericFont;
    
    private final boolean isEmbedded;
    private final boolean isDamaged;
    private final AffineTransform fontMatrixTransform;
    /**
     * to improve encoding speed.
     */
    private final Map <Integer,byte[]> codeToBytesMap;
    private Matrix fontMatrix;
    private BoundingBox fontBBox;

    /**
     * Creates a Type 1 standard 14 font for embedding.
     *
     * @param baseFont One of the standard 14 PostScript names
     */
    private PDType1Font(String baseFont)
    {
        super(baseFont);
        
        dict.setItem(COSName.SUBTYPE, COSName.TYPE1);
        dict.setName(COSName.BASE_FONT, baseFont);
        if ("ZapfDingbats".equals(baseFont))
        {
            encoding = ZapfDingbatsEncoding.INSTANCE;
        }
        else if ("Symbol".equals(baseFont))
        {
            encoding = SymbolEncoding.INSTANCE;
        }
        else
        {
            encoding = WinAnsiEncoding.INSTANCE;
            dict.setItem(COSName.ENCODING, COSName.WIN_ANSI_ENCODING);
        }

        // standard 14 fonts may be accessed concurrently, as they are singletons
        codeToBytesMap = new ConcurrentHashMap<Integer,byte[]>();

        // todo: could load the PFB font here if we wanted to support Standard 14 embedding
        type1font = null;
        FontMapping<FontBoxFont> mapping = FontMappers.instance()
                                                      .getFontBoxFont(getBaseFont(),
                                                                      getFontDescriptor());
        genericFont = mapping.getFont();
        
        if (mapping.isFallback())
        {
            String fontName;
            try
            {
                fontName = genericFont.getName();
            }
            catch (IOException e)
            {
                fontName = "?";
            }
            LOG.warn("Using fallback font " + fontName + " for base font " + getBaseFont());
        }
        isEmbedded = false;
        isDamaged = false;
        fontMatrixTransform = new AffineTransform();
    }

    /**
     * Creates a new Type 1 font for embedding.
     *
     * @param doc PDF document to write to
     * @param pfbIn PFB file stream
     * @throws IOException
     */
    public PDType1Font(PDDocument doc, InputStream pfbIn) throws IOException
    {
        PDType1FontEmbedder embedder = new PDType1FontEmbedder(doc, dict, pfbIn, null);
        encoding = embedder.getFontEncoding();
        glyphList = embedder.getGlyphList();
        type1font = embedder.getType1Font();
        genericFont = embedder.getType1Font();
        isEmbedded = true;
        isDamaged = false;
        fontMatrixTransform = new AffineTransform();
        codeToBytesMap = new HashMap<Integer,byte[]>();
    }

    /**
     * Creates a new Type 1 font for embedding.
     *
     * @param doc PDF document to write to
     * @param pfbIn PFB file stream
     * @param encoding
     * @throws IOException
     */
    public PDType1Font(PDDocument doc, InputStream pfbIn, Encoding encoding) throws IOException
    {
        PDType1FontEmbedder embedder = new PDType1FontEmbedder(doc, dict, pfbIn, encoding);
        this.encoding = encoding;
        glyphList = embedder.getGlyphList();
        type1font = embedder.getType1Font();
        genericFont = embedder.getType1Font();
        isEmbedded = true;
        isDamaged = false;
        fontMatrixTransform = new AffineTransform();
        codeToBytesMap = new HashMap<Integer,byte[]>();
    }

    /**
     * Creates a Type 1 font from a Font dictionary in a PDF.
     * 
     * @param fontDictionary font dictionary.
     * @throws IOException if there was an error initializing the font.
     * @throws IllegalArgumentException if /FontFile3 was used.
     */
    public PDType1Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        codeToBytesMap = new HashMap<Integer,byte[]>();

        PDFontDescriptor fd = getFontDescriptor();
        Type1Font t1 = null;
        boolean fontIsDamaged = false;
        if (fd != null)
        {
            // a Type1 font may contain a Type1C font
            PDStream fontFile3 = fd.getFontFile3();
            if (fontFile3 != null)
            {
                throw new IllegalArgumentException("Use PDType1CFont for FontFile3");
            }

            // or it may contain a PFB
            PDStream fontFile = fd.getFontFile();
            if (fontFile != null)
            {
                try
                {
                    COSStream stream = fontFile.getCOSObject();
                    int length1 = stream.getInt(COSName.LENGTH1);
                    int length2 = stream.getInt(COSName.LENGTH2);

                    // repair Length1 and Length2 if necessary
                    byte[] bytes = fontFile.toByteArray();
                    length1 = repairLength1(bytes, length1);
                    length2 = repairLength2(bytes, length1, length2);
                    
                    if (bytes.length > 0 && (bytes[0] & 0xff) == PFB_START_MARKER)
                    {
                        // some bad files embed the entire PFB, see PDFBOX-2607
                        t1 = Type1Font.createWithPFB(bytes);
                    }
                    else
                    {
                        // the PFB embedded as two segments back-to-back
                        byte[] segment1 = Arrays.copyOfRange(bytes, 0, length1);
                        byte[] segment2 = Arrays.copyOfRange(bytes, length1, length1 + length2);

                        // empty streams are simply ignored
                        if (length1 > 0 && length2 > 0)
                        {
                            t1 = Type1Font.createWithSegments(segment1, segment2);
                        }
                    }
                }
                catch (DamagedFontException e)
                {
                    LOG.warn("Can't read damaged embedded Type1 font " + fd.getFontName());
                    fontIsDamaged = true;
                }
                catch (IOException e)
                {
                    LOG.error("Can't read the embedded Type1 font " + fd.getFontName(), e);
                    fontIsDamaged = true;
                }
            }
        }
        isEmbedded = t1 != null;
        isDamaged = fontIsDamaged;
        type1font = t1;

        // find a generic font to use for rendering, could be a .pfb, but might be a .ttf
        if (type1font != null)
        {
            genericFont = type1font;
        }
        else
        {
            FontMapping<FontBoxFont> mapping = FontMappers.instance()
                                                          .getFontBoxFont(getBaseFont(), fd);
            genericFont = mapping.getFont();
            
            if (mapping.isFallback())
            {
                LOG.warn("Using fallback font " + genericFont.getName() + " for " + getBaseFont());
            }
        }
        readEncoding();
        fontMatrixTransform = getFontMatrix().createAffineTransform();
        fontMatrixTransform.scale(1000, 1000);
    }

    /**
     * Some Type 1 fonts have an invalid Length1, which causes the binary segment of the font
     * to be truncated, see PDFBOX-2350, PDFBOX-3677.
     *
     * @param bytes Type 1 stream bytes
     * @param length1 Length1 from the Type 1 stream
     * @return repaired Length1 value
     */
    private int repairLength1(byte[] bytes, int length1)
    {
        // scan backwards from the end of the first segment to find 'exec'
        int offset = Math.max(0, length1 - 4);
        if (offset <= 0 || offset > bytes.length - 4)
        {
            offset = bytes.length - 4;
        }

        offset = findBinaryOffsetAfterExec(bytes, offset);
        if (offset == 0 && length1 > 0)
        {
            // 2nd try with brute force
            offset = findBinaryOffsetAfterExec(bytes, bytes.length - 4);
        }

        if (length1 - offset != 0 && offset > 0)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn("Ignored invalid Length1 " + length1 + " for Type 1 font " + getName());
            }
            return offset;
        }

        return length1;
    }

    private static int findBinaryOffsetAfterExec(byte[] bytes, int startOffset)
    {
        int offset = startOffset;
        while (offset > 0)
        {
            if (bytes[offset + 0] == 'e'
                    && bytes[offset + 1] == 'x'
                    && bytes[offset + 2] == 'e'
                    && bytes[offset + 3] == 'c')
            {
                offset += 4;
                // skip additional CR LF space characters
                while (offset < bytes.length && 
                        (bytes[offset] == '\r' || bytes[offset] == '\n' || 
                         bytes[offset] == ' ' || bytes[offset] == '\t'))
                {
                    offset++;
                }
                break;
            }
            offset--;
        }
        return offset;
    }

    /**
     * Some Type 1 fonts have an invalid Length2, see PDFBOX-3475. A negative /Length2 brings an
     * IllegalArgumentException in Arrays.copyOfRange(), a huge value eats up memory because of
     * padding.
     *
     * @param bytes Type 1 stream bytes
     * @param length1 Length1 from the Type 1 stream
     * @param length2 Length2 from the Type 1 stream
     * @return repaired Length2 value
     */
    private int repairLength2(byte[] bytes, int length1, int length2)
    {
        // repair Length2 if necessary
        if (length2 < 0 || length2 > bytes.length - length1)
        {
            LOG.warn("Ignored invalid Length2 " + length2 + " for Type 1 font " + getName());
            return bytes.length - length1;
        }
        return length2;
    }

    /**
     * Returns the PostScript name of the font.
     */
    public final String getBaseFont()
    {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        String name = codeToName(code);
        if (getStandard14AFM() != null)
        {
            String afmName = getEncoding().getName(code);
            return getStandard14AFM().getCharacterHeight(afmName); // todo: isn't this the y-advance, not the height?
        }
        else
        {
            // todo: should be scaled by font matrix
            return (float) genericFont.getPath(name).getBounds().getHeight();
        }
    }

    @Override
    protected byte[] encode(int unicode) throws IOException
    {
        byte[] bytes = codeToBytesMap.get(unicode);
        if (bytes != null)
        {
            return bytes;
        }

        String name = getGlyphList().codePointToName(unicode);
        if (isStandard14())
        {
            // genericFont not needed, thus simplified code
            // this is important on systems with no installed fonts
            if (!encoding.contains(name))
            {
                throw new IllegalArgumentException(
                        String.format("U+%04X ('%s') is not available in this font %s encoding: %s",
                                unicode, name, getName(), encoding.getEncodingName()));
            }
            if (".notdef".equals(name))
            {
                throw new IllegalArgumentException(
                        String.format("No glyph for U+%04X in font %s", unicode, getName()));
            }
        }
        else
        {
            if (!encoding.contains(name))
            {
                throw new IllegalArgumentException(
                        String.format("U+%04X ('%s') is not available in this font %s (generic: %s) encoding: %s",
                                unicode, name, getName(), genericFont.getName(), encoding.getEncodingName()));
            }

            String nameInFont = getNameInFont(name);

            if (".notdef".equals(nameInFont) || !genericFont.hasGlyph(nameInFont))
            {
                throw new IllegalArgumentException(
                        String.format("No glyph for U+%04X in font %s (generic: %s)", unicode, getName(), genericFont.getName()));
            }
        }

        Map<String, Integer> inverted = encoding.getNameToCodeMap();
        int code = inverted.get(name);
        bytes = new byte[] { (byte)code };
        codeToBytesMap.put(code, bytes);
        return bytes;
    }

    @Override
    public float getWidthFromFont(int code) throws IOException
    {
        String name = codeToName(code);

        // width of .notdef is ignored for substitutes, see PDFBOX-1900
        if (!isEmbedded && name.equals(".notdef"))
        {
            return 250;
        }
        float width = genericFont.getWidth(name);

        Point2D p = new Point2D.Float(width, 0);
        fontMatrixTransform.transform(p, p);
        return (float)p.getX();
    }

    @Override
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public float getAverageFontWidth()
    {
        if (getStandard14AFM() != null)
        {
            return getStandard14AFM().getAverageCharacterWidth();
        }
        else
        {
            return super.getAverageFontWidth();
        }
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        if (getStandard14AFM() != null)
        {
            // read from AFM
            return new Type1Encoding(getStandard14AFM());
        }
        else
        {
            // extract from Type1 font/substitute
            if (genericFont instanceof EncodedFont)
            {
                return Type1Encoding.fromFontBox(((EncodedFont) genericFont).getEncoding());
            }
            else
            {
                // default (only happens with TTFs)
                return StandardEncoding.INSTANCE;
            }
        }
    }

    /**
     * Returns the embedded or substituted Type 1 font, or null if there is none.
     */
    public Type1Font getType1Font()
    {
        return type1font;
    }

    @Override
    public FontBoxFont getFontBoxFont()
    {
        return genericFont;
    }

    @Override
    public String getName()
    {
        return getBaseFont();
    }

    @Override
    public BoundingBox getBoundingBox() throws IOException
    {
        if (fontBBox == null)
        {
            fontBBox = generateBoundingBox();
        }
        return fontBBox;
    }

    private BoundingBox generateBoundingBox() throws IOException
    {
        if (getFontDescriptor() != null) {
            PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
            if (bbox != null &&
                    (bbox.getLowerLeftX() != 0 || bbox.getLowerLeftY() != 0 ||
                     bbox.getUpperRightX() != 0 || bbox.getUpperRightY() != 0))
            {
                return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(),
                                       bbox.getUpperRightX(), bbox.getUpperRightY());
            }
        }
        return genericFont.getFontBBox();
    }

    //@Override
    public String codeToName(int code) throws IOException
    {
        String name = getEncoding().getName(code);
        return getNameInFont(name);
    }

    /**
     * Maps a PostScript glyph name to the name in the underlying font, for example when
     * using a TTF font we might map "W" to "uni0057".
     */
    private String getNameInFont(String name) throws IOException
    {
        if (isEmbedded() || genericFont.hasGlyph(name))
        {
            return name;
        }
        else
        {
            // try alternative name
            String altName = ALT_NAMES.get(name);
            if (altName != null && !name.equals(".notdef") && genericFont.hasGlyph(altName))
            {
                return altName;
            }
            else
            {
                // try unicode name
                String unicodes = getGlyphList().toUnicode(name);
                if (unicodes != null && unicodes.length() == 1)
                {
                    String uniName = getUniNameOfCodePoint(unicodes.codePointAt(0));
                    if (genericFont.hasGlyph(uniName))
                    {
                        return uniName;
                    }
                }
            }
        }
        return ".notdef";
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        // Acrobat does not draw .notdef for Type 1 fonts, see PDFBOX-2421
        // I suspect that it does do this for embedded fonts though, but this is untested
        if (name.equals(".notdef") && !isEmbedded)
        {
            return new GeneralPath();
        }
        else
        {
            return genericFont.getPath(getNameInFont(name));
        }
    }

    @Override
    public GeneralPath getPath(int code) throws IOException
    {
        String name = getEncoding().getName(code);
        return getPath(name);
    }

    @Override
    public GeneralPath getNormalizedPath(int code) throws IOException
    {
        String name = getEncoding().getName(code);
        GeneralPath path = getPath(name);
        if (path == null)
        {
            return getPath(".notdef");
        }
        return path;
    }
    
    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        return genericFont.hasGlyph(getNameInFont(name));
    }

    @Override
    public boolean hasGlyph(int code) throws IOException
    {
        return !getEncoding().getName(code).equals(".notdef");
    }

    @Override
    public final Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            // PDF specified that Type 1 fonts use a 1000upem matrix, but some fonts specify
            // their own custom matrix anyway, for example PDFBOX-2298
            List<Number> numbers = null;
            try
            {
                numbers = genericFont.getFontMatrix();
            }
            catch (IOException e)
            {
                fontMatrix = DEFAULT_FONT_MATRIX;
            }
            
            if (numbers != null && numbers.size() == 6)
            {
                fontMatrix = new Matrix(
                        numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
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
        return isDamaged;
    }
}
