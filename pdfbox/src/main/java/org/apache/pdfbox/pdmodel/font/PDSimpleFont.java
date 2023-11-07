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
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.fontbox.FontBoxFont;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.MacRomanEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.StandardEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.ZapfDingbatsEncoding;

/**
 * A simple font. Simple fonts use a PostScript encoding vector.
 *
 * @author John Hewson
 */
public abstract class PDSimpleFont extends PDFont
{
    private static final Logger LOG = LogManager.getLogger(PDSimpleFont.class);

    protected Encoding encoding;
    protected GlyphList glyphList;
    private Boolean isSymbolic;
    private final Set<Integer> noUnicode = new HashSet<>(); // for logging

    /**
     * Constructor for embedding.
     */
    PDSimpleFont()
    {
    }

    /**
     * Constructor for Standard 14.
     */
    PDSimpleFont(FontName baseFont)
    {
        super(baseFont);
        assignGlyphList(baseFont);
    }

    /**
     * Constructor.
     *
     * @param fontDictionary Font dictionary.
     */
    PDSimpleFont(COSDictionary fontDictionary)
    {
        super(fontDictionary);
    }

    /**
     * Reads the Encoding from the Font dictionary or the embedded or substituted font file.
     * Must be called at the end of any subclass constructors.
     *
     * @throws IOException if the font file could not be read
     */
    protected void readEncoding() throws IOException
    {
        COSBase encodingBase = dict.getDictionaryObject(COSName.ENCODING);
        if (encodingBase instanceof COSName)
        {
            COSName encodingName = (COSName) encodingBase;
            if (FontName.ZAPF_DINGBATS.getName().equals(getName()) && !isEmbedded())
            {
                // PDFBOX- and PDF.js issue 16464: ignore other encodings
                // this segment will work only if readEncoding() is called after the data
                // for getName() and isEmbedded() is available
                this.encoding = ZapfDingbatsEncoding.INSTANCE;
            }
            else
            {
                this.encoding = Encoding.getInstance(encodingName);
                if (this.encoding == null)
                {
                    LOG.warn("Unknown encoding: {}", encodingName.getName());
                    this.encoding = readEncodingFromFont(); // fallback
                }
            }
        }
        else if (encodingBase instanceof COSDictionary)
        {
            COSDictionary encodingDict = (COSDictionary) encodingBase;
            Encoding builtIn = null;
            Boolean symbolic = getSymbolicFlag();

            COSName baseEncoding = encodingDict.getCOSName(COSName.BASE_ENCODING);

            boolean hasValidBaseEncoding = baseEncoding != null &&
                                             Encoding.getInstance(baseEncoding) != null;

            if (!hasValidBaseEncoding && Boolean.TRUE.equals(symbolic))
            {
                builtIn = readEncodingFromFont();
            }

            if (symbolic == null)
            {
                symbolic = false;
            }
            this.encoding = new DictionaryEncoding(encodingDict, !symbolic, builtIn);
        }
        else
        {
            this.encoding = readEncodingFromFont();
        }
        // normalise the standard 14 name, e.g "Symbol,Italic" -> "Symbol"
        FontName standard14Name = Standard14Fonts.getMappedFontName(getName());
        assignGlyphList(standard14Name);
    }
    
    /**
     * Called by readEncoding() if the encoding needs to be extracted from the font file.
     *
     * @return encoding of the font
     * 
     * @throws IOException if the font file could not be read.
     */
    protected abstract Encoding readEncodingFromFont() throws IOException;

    /**
     * Returns the Encoding.
     * 
     * @return encoding
     */
    public Encoding getEncoding()
    {
        return encoding;
    }

    /**
     * Returns the glyphlist.
     * 
     * @return the glyphlist
     */
    public GlyphList getGlyphList()
    {
        return glyphList;
    }
    
    /**
     * Returns true if the font is a symbolic (that is, it does not use the Adobe Standard Roman character set).
     * 
     * @return true if the font is a symbolic
     */
    public final boolean isSymbolic()
    {
        if (isSymbolic == null)
        {
            Boolean result = isFontSymbolic();
            if (result != null)
            {
                isSymbolic = result;
            }
            else
            {
                // unless we can prove that the font is non-symbolic, we assume that it is not
                isSymbolic = true;
            }
        }
        return isSymbolic;
    }

    /**
     * Internal implementation of isSymbolic, allowing for the fact that the result may be indeterminate.
     * 
     * @return true if isSymbolic
     */
    protected Boolean isFontSymbolic()
    {
        Boolean result = getSymbolicFlag();
        if (result != null)
        {
            return result;
        }
        else if (isStandard14())
        {
            FontName mappedName = Standard14Fonts.getMappedFontName(getName());
            return mappedName == FontName.SYMBOL || mappedName == FontName.ZAPF_DINGBATS;
        }
        else
        {
            if (encoding == null)
            {
                // check, should never happen
                if (!(this instanceof PDTrueTypeFont))
                {
                    throw new IllegalStateException("PDFBox bug: encoding should not be null!");
                }

                // TTF without its non-symbolic flag set must be symbolic
                return true;
            }
            else if (encoding instanceof WinAnsiEncoding ||
                     encoding instanceof MacRomanEncoding ||
                     encoding instanceof StandardEncoding)
            {
                return false;
            }
            else if (encoding instanceof DictionaryEncoding)
            {
                // each name in Differences array must also be in the latin character set
                for (String name : ((DictionaryEncoding)encoding).getDifferences().values())
                {
                    if (".notdef".equals(name))
                    {
                        // skip
                    }
                    else if (!(WinAnsiEncoding.INSTANCE.contains(name) &&
                               MacRomanEncoding.INSTANCE.contains(name) &&
                               StandardEncoding.INSTANCE.contains(name)))
                    {
                        return true;
                    }

                }
                return false;
            }
            else
            {
                // we don't know
                return null;
            }
        }
    }

    /**
     * Returns the value of the symbolic flag, allowing for the fact that the result may be indeterminate.
     * 
     * @return the value of the isSymbolic flag form the font descriptor
     */
    protected final Boolean getSymbolicFlag()
    {
        if (getFontDescriptor() != null)
        {
            // fixme: isSymbolic() defaults to false if the flag is missing so we can't trust this
            return getFontDescriptor().isSymbolic();
        }
        return null;
    }

    @Override
    public String toUnicode(int code)
    {
        return toUnicode(code, GlyphList.getAdobeGlyphList());
    }

    @Override
    public String toUnicode(int code, GlyphList customGlyphList)
    {
        // allow the glyph list to be overridden for the purpose of extracting Unicode
        // we only do this when the font's glyph list is the AGL, to avoid breaking Zapf Dingbats
        GlyphList unicodeGlyphList;
        if (this.glyphList == GlyphList.getAdobeGlyphList())
        {
            unicodeGlyphList = customGlyphList;
        }
        else
        {
            unicodeGlyphList = this.glyphList;
        }

        // first try to use a ToUnicode CMap
        String unicode = super.toUnicode(code);
        if (unicode != null)
        {
            return unicode;
        }

        // if the font is a "simple font" and uses MacRoman/MacExpert/WinAnsi[Encoding]
        // or has Differences with names from only Adobe Standard and/or Symbol, then:
        //
        //    a) Map the character codes to names
        //    b) Look up the name in the Adobe Glyph List to obtain the Unicode value

        String name = null;
        if (encoding != null)
        {
            name = encoding.getName(code);
            unicode = unicodeGlyphList.toUnicode(name);
            if (unicode != null)
            {
                return unicode;
            }
        }

        // if no value has been produced, there is no way to obtain Unicode for the character.
        if (LOG.isWarnEnabled() && !noUnicode.contains(code))
        {
            // we keep track of which warnings have been issued, so we don't log multiple times
            noUnicode.add(code);
            if (name != null)
            {
                LOG.warn("No Unicode mapping for {} ({}) in font {}", name, code, getName());
            }
            else
            {
                LOG.warn("No Unicode mapping for character code {} in font {}", code, getName());
            }
        }

        return null;
    }

    @Override
    public boolean isVertical()
    {
        return false;
    }

    @Override
    protected final float getStandard14Width(int code)
    {
        if (getStandard14AFM() != null)
        {
            String nameInAFM = getEncoding().getName(code);

            // the Adobe AFMs don't include .notdef, but Acrobat uses 250, test with PDFBOX-2334
            if (".notdef".equals(nameInAFM))
            {
                return 250f;
            }

            if ("nbspace".equals(nameInAFM))
            {
                // PDFBOX-4944: nbspace is missing in AFM files,
                // but PDF specification tells "it shall be typographically the same as SPACE"
                nameInAFM = "space";
            }
            else if ("sfthyphen".equals(nameInAFM))
            {
                // PDFBOX-5115: sfthyphen is missing in AFM files,
                // but PDF specification tells "it shall be typographically the same as hyphen"
                nameInAFM = "hyphen";
            }

            return getStandard14AFM().getCharacterWidth(nameInAFM);
        }
        throw new IllegalStateException("No AFM");
    }

    @Override
    public boolean isStandard14()
    {
        // this logic is based on Acrobat's behaviour, see PDFBOX-2372
        // the Encoding entry cannot have Differences if we want "standard 14" font handling
        if (getEncoding() instanceof DictionaryEncoding)
        {
            DictionaryEncoding dictionary = (DictionaryEncoding)getEncoding();
            if (!dictionary.getDifferences().isEmpty())
            {
                // we also require that the differences are actually different, see PDFBOX-1900 with
                // the file from PDFBOX-2192 on Windows
                Encoding baseEncoding = dictionary.getBaseEncoding();
                for (Map.Entry<Integer, String> entry : dictionary.getDifferences().entrySet())
                {
                    if (!entry.getValue().equals(baseEncoding.getName(entry.getKey())))
                    {
                        return false;
                    }
                }
            }
        }
        return super.isStandard14();
    }

    protected boolean isNonZeroBoundingBox (PDRectangle bbox)
    {
        return bbox != null && (
            Float.compare(bbox.getLowerLeftX(), 0) != 0 ||
            Float.compare(bbox.getLowerLeftY(), 0) != 0 ||
            Float.compare(bbox.getUpperRightX(), 0) != 0 ||
            Float.compare(bbox.getUpperRightY(), 0) != 0
        );
    }

    /**
     * Returns the path for the character with the given name. For some fonts, GIDs may be used instead of names when
     * calling this method. *
     * 
     * @param name glyph name
     * @return glyph path of the character with the given name
     * 
     * @throws IOException if the path could not be read
     */
    public abstract GeneralPath getPath(String name) throws IOException;

    /**
     * Returns true if the font contains the character with the given name.
     *
     * @param name glyph name
     * @return true if the font contains the character with the given name
     * 
     * @throws IOException if the path could not be read
     */
    public abstract boolean hasGlyph(String name) throws IOException;

    /**
     * Returns the embedded or system font used for rendering. This is never null.
     * 
     * @return the embedded or system font used for rendering
     */
    public abstract FontBoxFont getFontBoxFont();

    @Override
    public void addToSubset(int codePoint)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subset() throws IOException
    {
        // only TTF subsetting via PDType0Font is currently supported
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean willBeSubset()
    {
        return false;
    }

    @Override
    public boolean hasExplicitWidth(int code) throws IOException
    {
        if (dict.containsKey(COSName.WIDTHS))
        {
            int firstChar = dict.getInt(COSName.FIRST_CHAR, -1);
            if (code >= firstChar && code - firstChar < getWidths().size())
            {
                return true;
            }
        }
        return false;
    }

    private void assignGlyphList(FontName fontName)
    {
        // assign the glyph list based on the font
        if (FontName.ZAPF_DINGBATS == fontName)
        {
            glyphList = GlyphList.getZapfDingbats();
        }
        else
        {
            glyphList = GlyphList.getAdobeGlyphList();
        }
    }
}
