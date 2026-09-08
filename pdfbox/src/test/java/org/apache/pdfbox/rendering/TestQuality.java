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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 *
 * @author Tilman Hausherr
 */
class TestQuality
{
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    /**
     * PDFBOX-4831: PDF with a 300 dpi bitonal scan must be bitonal when rendered at 300 dpi
     * and identical to the scan in the PDF.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox4831() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-4831.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage renderedImage = renderer.renderImageWithDPI(0, 300);
            Assertions.assertEquals(2, ValidateXImage.colorCount(renderedImage));
            PDImageXObject xObjectImage =
                    (PDImageXObject) doc.getPage(0).getResources().getXObject(COSName.getPDFName("I0"));
            BufferedImage extractedImage = xObjectImage.getImage();
            ValidateXImage.checkIdent(extractedImage, renderedImage);
        }
    }

    /**
     * PDFBOX-6077: a stencil mask filled with a pattern must not paint the gaps between the
     * pattern's own tiles as opaque black. Before the fix, the stencil mask's alpha overwrote
     * the pattern paint's own alpha instead of being combined with it, so any pixel the pattern
     * didn't itself draw into turned solid black instead of staying transparent.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox6077() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-6077-example.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage renderedImage = renderer.renderImageWithDPI(0, 100);
            // a gap between the tiling pattern's own painted tiles, which must stay transparent
            // (i.e. show the white page background) instead of turning opaque black
            Assertions.assertEquals(0xFFFFFFFF, renderedImage.getRGB(280, 23));
        }
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
    void testPDFBox5842() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5842-reduced.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage renderedImage = renderer.renderImageWithDPI(0, 100);
            // a pixel within the soft-masked pattern's map marker icon; if the soft mask's alpha
            // lookup is broken, this whole region renders as blank white instead
            Assertions.assertNotEquals(0xFFFFFFFF, renderedImage.getRGB(267, 1329));
        }
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
    void testPDFBox5403() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5403-bad-rendering.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage renderedImage = renderer.renderImageWithDPI(2, 100);
            // a pixel within a line of text rendered via a pattern-filled stencil mask; a
            // hairline tile-boundary seam previously showed through as a washed-out gray streak
            int rgb = renderedImage.getRGB(159, 115);
            int red = (rgb >> 16) & 0xFF;
            Assertions.assertTrue(red < 100,
                    "expected a dark text pixel but was too light: " + Integer.toHexString(rgb));
        }
    }

    /**
     * PDFBOX-5250: a mesh shading pattern used inside a transparency group that has its own
     * non-trivial /Matrix must be positioned using that group's own initial matrix, not the
     * parent's. Before the fix, the transparency group's /Matrix was concatenated into the CTM
     * only after the initial matrix had already been captured, so any pattern painted inside the
     * group (here, a colored tiling pattern whose cell is itself a transparency group filled
     * with a type 7 shading) was placed using the wrong reference matrix. That shifted the mesh
     * shading far out of position, so instead of the intended multicolor gradient, only a
     * single, mostly-green sliver of it ever landed on the visible glyphs.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox5250() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5250-pattern-reduced3.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage renderedImage = renderer.renderImageWithDPI(0, 100);
            // a pixel within the shading-pattern-filled text; before the fix, the mesh shading
            // was shifted out of view here, leaving this pixel blank white instead of the
            // gradient's red-ish color. Checking red without also ruling out green isn't enough
            // because white also has a maxed-out red channel.
            int rgb = renderedImage.getRGB(190, 331);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            Assertions.assertTrue(red > 150 && green < 150,
                    "expected a red-ish gradient pixel but was: " + Integer.toHexString(rgb));
        }
    }
  
    /**
     * PDFBOX-5876: rendering a page containing a very large JPEG 2000 (JPX) image at reduced
     * scale must not decode the image at full resolution first just to read its width, height
     * and color space. Before the fix, {@code PDImageXObject.initJPXValues()} did exactly that,
     * on top of the properly subsampled decode done afterwards for the actual rendering, so
     * memory usage was driven by the full image size regardless of how small the rendered output
     * was. This must run in a separate, heap-constrained JVM, since the heap size of the JVM
     * already running the test suite can't be changed after the fact, and the failure (an
     * OutOfMemoryError) only reproduces below a certain heap size.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @EnabledIfSystemProperty(named = "TestOOM", matches = "true")
    void testPDFBox5876() throws IOException, InterruptedException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-5876-jpeg2000.pdf");
        File outputFile = new File("target/test-output", file.getName() + "-p1.png");
        outputFile.delete(); // in case it exists from older test
        String javaBin = System.getProperty("java.home") + File.separator + "bin" +
                File.separator + "java";
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-Xmx600m",
                "-cp", System.getProperty("java.class.path"),
                JPXLowMemoryRenderMain.class.getName(), file.getAbsolutePath());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished)
        {
            process.destroy();
        }
        Assertions.assertTrue(finished, "subprocess timed out");
        Assertions.assertEquals(0, process.exitValue(), "subprocess failed:\n" + output);
        BufferedImage bim = ImageIO.read(outputFile);
        Assertions.assertEquals(297, bim.getWidth());
        Assertions.assertEquals(421, bim.getHeight());
        Files.delete(outputFile.toPath());
    }
}
