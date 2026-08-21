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
package org.apache.pdfbox.rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 */
public class TestQuality
{
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    /**
     * PDFBOX-6077: a stencil mask filled with a pattern must not paint the gaps between the
     * pattern's own tiles as opaque black. Before the fix, the stencil mask's alpha overwrote
     * the pattern paint's own alpha instead of being combined with it, so any pixel the pattern
     * didn't itself draw into turned solid black instead of staying transparent.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox6077() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-6077-example.pdf");
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage renderedImage = renderer.renderImageWithDPI(0, 100);
        // a gap between the tiling pattern's own painted tiles, which must stay transparent
        // (i.e. show the white page background) instead of turning opaque black
        assertEquals(0xFFFFFFFF, renderedImage.getRGB(280, 23));
        doc.close();
    }

    /**
     * PDFBOX-6077: a soft mask applied to a pattern that is used as a stencil mask fill must
     * still be visible. Such a pattern is rendered into a separate scratch image rather than
     * directly onto the page, and the soft mask's own alpha lookup is keyed to absolute
     * page-device pixel coordinates, so a naive implementation renders it as fully transparent.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox5842() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5842-reduced.pdf");
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage renderedImage = renderer.renderImageWithDPI(0, 100);
        // a pixel within the soft-masked pattern's map marker icon; if the soft mask's alpha
        // lookup is broken, this whole region renders as blank white instead
        assertNotEquals(0xFFFFFFFF, renderedImage.getRGB(267, 1329));
        doc.close();
    }

    /**
     * PDFBOX-5403: a stencil mask filled with a pattern repeated many times over a large area
     * (e.g. one tiling-pattern-filled image per line of text) must not show a hairline seam
     * between the pattern's own tiles as a visible gap. Combining the mask's alpha with the
     * pattern's own alpha (see testPDFBox6077) can expose such a seam as a light gray line
     * cutting through otherwise-solid text, if it isn't first smoothed over.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox5403() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5403-bad-rendering.pdf");
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage renderedImage = renderer.renderImageWithDPI(2, 100);
        // a pixel within a line of text rendered via a pattern-filled stencil mask; a
        // hairline tile-boundary seam previously showed through as a washed-out gray streak
        int rgb = renderedImage.getRGB(159, 115);
        int red = (rgb >> 16) & 0xFF;
        assertTrue("expected a dark text pixel but was too light: " + Integer.toHexString(rgb), red < 100);
        doc.close();
    }
}
