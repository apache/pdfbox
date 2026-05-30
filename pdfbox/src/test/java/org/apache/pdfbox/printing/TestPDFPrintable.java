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
package org.apache.pdfbox.printing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.junit.Test;


/**
 * Tests for {@link PDFPrintable}.
 */
public class TestPDFPrintable
{
    private final int IMAGE_WIDTH = 100;
    private final int IMAGE_HEIGHT = 100;

    /**
     * Tests that the page border is drawn with Color.GRAY when showPageBorder is true.
     *
     * Without rasterization, {@code graphics} and {@code graphics2D} are the same object,
     * so setColor(GRAY) and drawRect() both act on the same Graphics2D. The border is drawn
     * correctly.
     */
    @Test
    public void testShowPageBorderIsGrayWithoutRasterization() throws Exception
    {
        testShowPageBorderIsGray(PDFPrintable.RASTERIZE_OFF);
    }

    /**
     * Tests that the page border is drawn with Color.GRAY when rasterizing.
     *
     * <p>When rasterizing (dpi &gt; 0), the border is drawn on a raster image that is
     * larger than the page (scaled by dpiScale), then blitted down to the output.
     * The {@code setClip} in the showPageBorder block must use raster-pixel dimensions
     * (imageableWidth * scale), not raw point dimensions (imageableWidth). Otherwise
     * the clip is too small, the border is drawn in only a fraction of the raster image,
     * and the thin border line gets lost during the scale-down blit to the output.</p>
     */
    @Test
    public void testShowPageBorderIsGrayWithRasterization() throws Exception
    {
        testShowPageBorderIsGray(150f);
    }

    /**
     * {@code print()} would otherwise mutate the caller's Graphics2D in several places:
     * translate() for imageable area and centering, scale() during rasterization,
     * setBackground() before the raster blit, and setColor / setStroke / setClip / setTransform
     * in the showPageBorder block. To isolate the caller, {@code print()} works on a private
     * copy obtained via {@code graphics.create()} and disposes it in the finally block, so none
     * of those mutations reach the caller. This test verifies that isolation by setting
     * distinctive state on the caller's Graphics2D before {@code print()} and asserting it is
     * unchanged afterwards.
     */
    @Test
    public void testPrinterGraphicsStateIsUnchangedAfterPrint() throws Exception
    {
        assertPrinterGraphicsStateUnchanged(PDFPrintable.RASTERIZE_OFF);
    }

    @Test
    public void testPrinterGraphicsStateIsUnchangedAfterPrintWhenRasterizing() throws Exception
    {
        assertPrinterGraphicsStateUnchanged(150f);
    }

    private void assertPrinterGraphicsStateUnchanged(float dpi) throws Exception
    {
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage(new PDRectangle(IMAGE_WIDTH, IMAGE_HEIGHT)));

        PDFPrintable printable = new PDFPrintable(doc, Scaling.ACTUAL_SIZE, true, dpi);

        BufferedImage output = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();

        // set a distinctive transform so we can detect leaks from internal translate()/scale() calls
        g2d.translate(7.0, 11.0);
        g2d.scale(1.3, 1.3);

        Color originalColor = Color.RED;
        Color originalBackground = Color.BLUE;
        Stroke originalStroke = new BasicStroke(3.7f);
        g2d.setColor(originalColor);
        g2d.setBackground(originalBackground);
        g2d.setStroke(originalStroke);
        AffineTransform originalTransform = g2d.getTransform();
        Rectangle2D originalClipDeviceBounds = deviceClipBounds(g2d);

        PageFormat pf = createPageFormat(IMAGE_WIDTH, IMAGE_HEIGHT);
        int result = printable.print(g2d, pf, 0);

        assertEquals(Printable.PAGE_EXISTS, result);
        assertEquals("color should be unchanged after print()", originalColor, g2d.getColor());
        assertEquals("background should be unchanged after print()", originalBackground, g2d.getBackground());
        assertEquals("stroke should be unchanged after print()", originalStroke, g2d.getStroke());
        assertEquals("transform should be unchanged after print() (translate/scale inside print() must not leak)",
                originalTransform, g2d.getTransform());
        // device-space comparison — invariant under transform changes on the same Graphics2D
        assertEquals("clip should be unchanged after print()", originalClipDeviceBounds, deviceClipBounds(g2d));

        g2d.dispose();
        doc.close();
    }

    /**
     * Returns the bounds of the current clip projected into device space via the current transform.
     * This is stable across transform changes on the same Graphics2D (unlike getClip().getBounds2D(),
     * which is in current user space).
     */
    private static Rectangle2D deviceClipBounds(Graphics2D g2d)
    {
        Shape clip = g2d.getClip();
        if (clip == null)
        {
            return null;
        }
        return g2d.getTransform().createTransformedShape(clip).getBounds2D();
    }

    @Test
    public void testPrintReturnsNoSuchPageForInvalidIndex() throws Exception
    {
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());

        PDFPrintable printable = new PDFPrintable(doc);

        BufferedImage output = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        PageFormat pf = createPageFormat(IMAGE_WIDTH, IMAGE_HEIGHT);

        assertEquals(Printable.NO_SUCH_PAGE, printable.print(g2d, pf, -1));
        assertEquals(Printable.NO_SUCH_PAGE, printable.print(g2d, pf, 1));
        assertEquals(Printable.PAGE_EXISTS, printable.print(g2d, pf, 0));

        g2d.dispose();
        doc.close();
    }

    private void testShowPageBorderIsGray(float dpi) throws Exception
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(IMAGE_WIDTH, IMAGE_HEIGHT));
        doc.addPage(page);

        PDFPrintable printable = new PDFPrintable(doc, Scaling.ACTUAL_SIZE, true, dpi);

        BufferedImage output = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        // fill with white so we can detect gray border pixels
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        PageFormat pf = createPageFormat(IMAGE_WIDTH, IMAGE_HEIGHT);
        int result = printable.print(g2d, pf, 0);
        g2d.dispose();

        assertEquals(Printable.PAGE_EXISTS, result);
        assertBorderPixelIsGray(output);
        doc.close();
    }

    /**
     * Asserts that at least one pixel along the top edge of the image is gray
     * (R == G == B, not white and not black), proving the border was drawn with Color.GRAY.
     */
    private static void assertBorderPixelIsGray(BufferedImage image)
    {
        boolean foundGray = false;
        int width = image.getWidth();
        // scan top row and left column where the border rect starts
        for (int x = 0; x < width; x++)
        {
            if (isGray(image.getRGB(x, 0)))
            {
                foundGray = true;
                break;
            }
        }
        if (!foundGray)
        {
            int height = image.getHeight();
            for (int y = 0; y < height; y++)
            {
                if (isGray(image.getRGB(0, y)))
                {
                    foundGray = true;
                    break;
                }
            }
        }
        assertTrue("Expected a gray border pixel in the top-left corner. " + 
                   "If this fails, drawRect may be called on the wrong Graphics object.",
                foundGray);
    }

    private static boolean isGray(int argb)
    {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        // Color.GRAY is (128, 128, 128) — allow some tolerance for antialiasing
        return a > 0 && r == g && g == b && r > 50 && r < 200;
    }

    private static PageFormat createPageFormat(double width, double height)
    {
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        PageFormat pf = new PageFormat();
        pf.setPaper(paper);
        return pf;
    }
}