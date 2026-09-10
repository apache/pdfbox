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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.Test;

/**
 * Tests that blend modes are detected wherever they are used on a page, so that the page is
 * rendered on a transparent backdrop and composited onto white afterwards (PDFBOX-4095). A blend
 * mode such as ColorBurn or Multiply applied directly onto an opaque white backdrop would make
 * the blended content disappear.
 */
class TestPDFRendererBlendMode
{
    private static final int BLUE = 0xFF0000FF;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * Blend mode set in the resources of the page itself.
     */
    @Test
    void testBlendModeInPageResources() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(new PDRectangle(200, 200));
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                drawBlueSquareWithColorBurn(cs);
            }
            assertBlueSquareVisible(doc);
        }
    }

    /**
     * Blend mode set in the resources of a form XObject that is drawn by the page. The page
     * resources themselves don't have any blend mode.
     */
    @Test
    void testBlendModeInFormXObject() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(new PDRectangle(200, 200));
            doc.addPage(page);

            PDFormXObject form = createForm(doc);
            try (PDFormContentStream cs = new PDFormContentStream(form))
            {
                drawBlueSquareWithColorBurn(cs);
            }
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.drawForm(form);
            }
            assertBlueSquareVisible(doc);
        }
    }

    /**
     * Blend mode set in the resources of a form XObject that is nested in another form XObject,
     * as done by layout applications placing artwork from illustration applications.
     */
    @Test
    void testBlendModeInNestedFormXObject() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(new PDRectangle(200, 200));
            doc.addPage(page);

            PDFormXObject inner = createForm(doc);
            try (PDFormContentStream cs = new PDFormContentStream(inner))
            {
                drawBlueSquareWithColorBurn(cs);
            }
            PDFormXObject outer = createForm(doc);
            try (PDFormContentStream cs = new PDFormContentStream(outer))
            {
                cs.drawForm(inner);
            }
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.drawForm(outer);
            }
            assertBlueSquareVisible(doc);
        }
    }

    /**
     * A form XObject referencing itself must not cause endless recursion in the blend mode
     * detection.
     */
    @Test
    void testSelfReferencingFormXObject() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(new PDRectangle(200, 200));
            doc.addPage(page);

            PDFormXObject form = createForm(doc);
            form.getResources().add(form);
            try (PDFormContentStream cs = new PDFormContentStream(form))
            {
                cs.setNonStrokingColor(0f, 0f, 1f);
                cs.addRect(50, 50, 100, 100);
                cs.fill();
            }
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.drawForm(form);
            }
            assertBlueSquareVisible(doc);
        }
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
        assertEquals(BLUE, image.getRGB(100, 100), "blended content must be visible on white");
        assertEquals(WHITE, image.getRGB(25, 25), "background must stay white");
    }
}
