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
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Charsets;
import static org.junit.Assume.assumeTrue;


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
    public void testPDFBox5876() throws IOException, InterruptedException
    {
        String featureFlag = System.getProperty("TestOOM");
        assumeTrue("true".equals(featureFlag));
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
        String output = new String(IOUtils.toByteArray(process.getInputStream()), Charsets.UTF_8);

        // segment by ChatGPT because wait
        long timeout = 120000L;
        long startTime = System.currentTimeMillis();
        boolean finished = false;
        while (System.currentTimeMillis() - startTime < timeout)
        {
            try
            {
                process.exitValue(); // throws IllegalThreadStateException if still running
                finished = true;
                break;
            }
            catch (IllegalThreadStateException e)
            {
                Thread.sleep(1000); // check every second
            }
        }
        if (!finished)
        {
            process.destroy();
        }

        assertTrue("subprocess timed out", finished);
        assertEquals("subprocess failed:\n" + output, 0, process.exitValue());
        BufferedImage bim = ImageIO.read(outputFile);
        assertEquals(298, bim.getWidth());
        assertEquals(421, bim.getHeight());
        outputFile.delete();
    }
}
