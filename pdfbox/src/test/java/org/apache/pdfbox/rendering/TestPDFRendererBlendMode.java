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

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import org.junit.Test;

/**
 * Tests that blend modes are detected wherever they are used on a page, so that the page is
 * rendered on a transparent backdrop and composited onto white afterwards (PDFBOX-4095). A blend
 * mode such as ColorBurn or Multiply applied directly onto an opaque white backdrop would make
 * the blended content disappear.
 */
public class TestPDFRendererBlendMode
{
    private static final int BLUE = 0xFF0000FF;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * Blend mode set in the resources of the page itself.
     */
    @Test
    public void testBlendModeInPageResources() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(200, 200));
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        drawBlueSquareWithColorBurn(cs);
        cs.close();
        assertBlueSquareVisible(doc);
        doc.close();
    }

    /**
     * Blend mode set in the resources of a form XObject that is drawn by the page. The page
     * resources themselves don't have any blend mode.
     */
    @Test
    public void testBlendModeInFormXObject() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(200, 200));
        doc.addPage(page);

        PDFormXObject form = createForm(doc);
        PDFormContentStream cs1 = new PDFormContentStream(form);
        drawBlueSquareWithColorBurn(cs1);
        cs1.close();
        PDPageContentStream cs2 = new PDPageContentStream(doc, page);
        cs2.drawForm(form);
        cs2.close();
        assertBlueSquareVisible(doc);
        doc.close();
    }

    /**
     * Blend mode set in the resources of a form XObject that is nested in another form XObject,
     * as done by layout applications placing artwork from illustration applications.
     */
    @Test
    public void testBlendModeInNestedFormXObject() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(200, 200));
        doc.addPage(page);

        PDFormXObject inner = createForm(doc);
        PDFormContentStream cs = new PDFormContentStream(inner);
        drawBlueSquareWithColorBurn(cs);
        cs.close();
        PDFormXObject outer = createForm(doc);
        PDFormContentStream cs2 = new PDFormContentStream(outer);
        cs2.drawForm(inner);
        cs2.close();
        PDPageContentStream cs3 = new PDPageContentStream(doc, page);
        cs3.drawForm(outer);
        cs3.close();
        assertBlueSquareVisible(doc);
        doc.close();
    }

    /**
     * A form XObject referencing itself must not cause endless recursion in the blend mode
     * detection.
     */
    @Test
    public void testSelfReferencingFormXObject() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(200, 200));
        doc.addPage(page);

        PDFormXObject form = createForm(doc);
        form.getResources().add(form);
        PDFormContentStream cs1 = new PDFormContentStream(form);
        cs1.setNonStrokingColor(0f, 0f, 1f);
        cs1.addRect(50, 50, 100, 100);
        cs1.fill();
        cs1.close();
        PDPageContentStream cs2 = new PDPageContentStream(doc, page);
        cs2.drawForm(form);
        cs2.close();
        assertBlueSquareVisible(doc);
        doc.close();
    }

    private static PDFormXObject createForm(PDDocument doc)
    {
        PDFormXObject form = new PDFormXObject(doc);
        form.setBBox(new PDRectangle(200, 200));
        form.setResources(new PDResources());
        return form;
    }

    private static void drawBlueSquareWithColorBurn(PDPageContentStream cs) throws IOException
    {
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setBlendMode(BlendMode.COLOR_BURN);
        cs.setGraphicsStateParameters(gs);
        cs.setNonStrokingColor(0f, 0f, 1f);
        cs.addRect(50, 50, 100, 100);
        cs.fill();
    }

    private static void drawBlueSquareWithColorBurn(PDFormContentStream cs) throws IOException
    {
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setBlendMode(BlendMode.COLOR_BURN);
        cs.setGraphicsStateParameters(gs);
        cs.setNonStrokingColor(0f, 0f, 1f);
        cs.addRect(50, 50, 100, 100);
        cs.fill();
    }

    private static void assertBlueSquareVisible(PDDocument doc) throws IOException
    {
        BufferedImage image = new PDFRenderer(doc).renderImage(0, 1, ImageType.RGB);
        assertEquals(BufferedImage.TYPE_INT_RGB, image.getType());
        assertEquals("blended content must be visible on white", BLUE, image.getRGB(100, 100));
        assertEquals("background must stay white", WHITE, image.getRGB(25, 25));
    }
}
