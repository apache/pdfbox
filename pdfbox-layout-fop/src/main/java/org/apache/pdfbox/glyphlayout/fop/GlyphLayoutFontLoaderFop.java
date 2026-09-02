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
package org.apache.pdfbox.glyphlayout.fop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;


/**
 * Loads the PDType0Font and awt.Font for GlyphLayoutProcessorAwt
 * <p>
 * Use an object of this class only in one thread.
 *
 * @author Volker Kunert
 */
public class GlyphLayoutFontLoaderFop
{

    /**
     * Mapping from PDFBox font to AWT font
     */
    private final Map<PDType0Font, MultiByteFont> fopFontMap = new ConcurrentHashMap<>();

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset
     * @return pdType0Font PDFBox font
     * @throws IOException if font can not be loaded
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset)
            throws IOException
    {
        Objects.requireNonNull(inputStream, "InputStream must not be null");
        PDType0Font pdType0Font;

        // Copy font stream into memory to read it twice for creation of PDType0Font and aww.Font
        byte[] bytes = IOUtils.toByteArray(inputStream);
        pdType0Font = PDType0Font.load(pdDocument, new RandomAccessReadBuffer(bytes), embedSubset, false);
        loadFopFont(pdType0Font, new ByteArrayInputStream(bytes));
        return pdType0Font;
    }

    /**
     * Loads the Fop font needed for layout
     *
     * @param doc document
     * @param inputStream of the font
     * @return pdType0Font PDFBox font
     * @throws IOException if font can not be loaded
     */
    public PDType0Font loadFont(PDDocument doc, InputStream inputStream)
            throws IOException
    {
        return loadFont(doc, inputStream, true);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdType0Font PDFBox font
     * @param inputStream of the font file
     * @throws IOException if font can not be loaded
     */
    protected void loadFopFont(PDType0Font pdType0Font, InputStream inputStream)
            throws IOException
    {
        if (!fopFontMap.containsKey(pdType0Font))
        {
            try
            {
                ResourceResolver resourceResolver = new FopInputStreamResourceResolver(inputStream);
                InternalResourceResolver internalResourceResolver = ResourceResolverFactory
                        .createInternalResourceResolver(new URI(pdType0Font.getName()), resourceResolver);
                FontUris fontUris = new FontUris(new URI(pdType0Font.getName()), null);
                MultiByteFont mbf = (MultiByteFont) FontLoader.loadFont(fontUris, null, true, EmbeddingMode.AUTO,
                        EncodingMode.AUTO, true, true,
                        internalResourceResolver, false, false, false);
                Objects.requireNonNull(mbf);
                fopFontMap.put(pdType0Font, mbf);
            }
            catch (URISyntaxException ex)
            {
                throw new IOException(ex);
            }
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
        return fopFontMap.containsKey(font);
    }

    /**
     * Gets the corresponding Fop font for the given PDFBox font
     *
     * @param font PDFBox font
     * @return fop font if available
     */
    public MultiByteFont getFopFont(PDType0Font font)
    {
        return fopFontMap.get(font);
    }

    /**
     * Resolver needed to construct a FOP MultibyteFont
     */
    static class FopInputStreamResourceResolver implements ResourceResolver
    {
        private final InputStream inputStream;

        public FopInputStreamResourceResolver(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }

        @Override
        public OutputStream getOutputStream(URI arg0) throws IOException
        {
            return null;
        }

        @Override
        public Resource getResource(URI arg0) throws IOException
        {
            return new Resource(inputStream);
        }
    }
}
