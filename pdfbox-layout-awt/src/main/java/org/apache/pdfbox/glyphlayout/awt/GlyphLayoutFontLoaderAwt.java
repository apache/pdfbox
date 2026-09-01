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

package org.apache.pdfbox.glyphlayout.awt;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Loads the PDType0Font and awt.Font for GlyphLayoutProcessorAwt
 * <p>
 * Use an object of this class only in one thread.
 *
 * @author Volker Kunert
 */
public class GlyphLayoutFontLoaderAwt
{
    protected final GlyphLayoutProcessorAwt glyphLayoutProcessor;

    /**
     * Mapping from PDFBox font to AWT font
     */
    private final Map<PDType0Font, Font> awtFontMap = new ConcurrentHashMap<>();

    public GlyphLayoutFontLoaderAwt(GlyphLayoutProcessorAwt glyphLayoutProcessor) {
        this.glyphLayoutProcessor = glyphLayoutProcessor;
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @return PDType0Font PDFBox font
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream)
            throws IOException, FontFormatException
    {
        return loadFont(pdDocument, inputStream, true, null);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset True if the font will be subset before embedding. Set this to false when
     * creating a font for AcroForm.
     * @return PDType0Font PDFBox font
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset)
            throws IOException, FontFormatException
    {
        return loadFont(pdDocument, inputStream, embedSubset, null);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param fontOptions options for font
     * @return PDType0Font PDFBox font
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, FontOptions fontOptions)
            throws IOException, FontFormatException
    {
        return loadFont(pdDocument, inputStream, true, fontOptions);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset True if the font will be subset before embedding. Set this to false when
     * creating a font for AcroForm.
     * @param fontOptions Options for font
     * @return PDType0Font PDFBox font
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset, FontOptions fontOptions)
            throws IOException, FontFormatException
    {
        Objects.requireNonNull(inputStream, "InputStream must not be null");

        // Copy font stream into memory to read it twice for creation of PDType0Font and AWT Font
        try (ByteArrayInputStream bais = new ByteArrayInputStream(inputStream.readAllBytes()))
        {
            PDType0Font pdType0Font = PDType0Font.load(pdDocument, bais, embedSubset);
            pdType0Font.setGlyphLayoutProcessor(glyphLayoutProcessor);
            bais.reset();
            loadAwtFont(pdType0Font, bais, fontOptions);
            return pdType0Font;
        }
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdType0Font PDFBox font
     * @param inputStream of the font file
     * @param fontOptions Options for font
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    protected void loadAwtFont(PDType0Font pdType0Font, InputStream inputStream, FontOptions fontOptions)
            throws FontFormatException, IOException
    {
        if (fontOptions == null)
        {
            fontOptions = new FontOptions();
        }
        if (!awtFontMap.containsKey(pdType0Font))
        {
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream)
                    .deriveFont(fontOptions.getTextAttributes());
            awtFontMap.put(pdType0Font, awtFont);
        }
    }

    /**
     * Determines if glyph layout is supported for this font
     *
     * @param font PDFBox font
     * @return true if glyph layout is supported for this font and this font is a PDType0Font
     */
    public boolean supportsFont(PDFont font)
    {
        return (font instanceof PDType0Font)
                && awtFontMap.containsKey((PDType0Font)font);
    }

    /**
     * Gets the corresponding AWT-font for the given PDFBox-font
     *
     * @param font PDFBox font
     * @return AWT font if available
     */
    protected Font getAwtFont(PDType0Font font)
    {
        return awtFontMap.get(font);
    }

    /**
     * Specify Options for an AWT font
     */
    public static class FontOptions
    {
        private final Map<TextAttribute, Object> textAttributes = new HashMap<>();

        protected Map<TextAttribute, Object> getTextAttributes()
        {
            // always return an unmodifiableMap, so that internal state can not be changed
            // by changing the returned map
            return Collections.unmodifiableMap(textAttributes);
        }

        public FontOptions setKerningOn()
        {
            textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            return this;
        }

        public FontOptions setLigaturesOn()
        {
            textAttributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
            return this;
        }
    }
}
