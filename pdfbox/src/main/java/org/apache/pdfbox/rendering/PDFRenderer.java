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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Renders a PDF document to an AWT BufferedImage.
 * This class may be overridden in order to perform custom rendering.
 *
 * @author John Hewson
 * @author Andreas Lehmkühler
 */
public class PDFRenderer
{
    protected final PDDocument document;
    // TODO keep rendering state such as caches here

    private boolean clearResourcesAutomatically;
    
    /**
     * Creates a new PDFRenderer.
     * @param document the document to render
     */
    public PDFRenderer(PDDocument document)
    {
        this(document, true);
    }

    /**
     * Creates a new PDFRenderer.
     * @param document the document to render
     * @param clearResourcesAutomatically indicates that all resources will be cleared automatically after rendering
     */
    public PDFRenderer(PDDocument document, boolean clearResourcesAutomatically)
    {
        this.document = document;
        this.clearResourcesAutomatically = clearResourcesAutomatically;
    }

    /**
     * Returns the given page as an RGB image at 72 DPI
     * @param pageIndex the zero-based index of the page to be converted.
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImage(int pageIndex) throws IOException
    {
        return renderImage(pageIndex, 1);
    }

    /**
     * Returns the given page as an RGB image at the given scale.
     * A scale of 1 will render at 72 DPI.
     * @param pageIndex the zero-based index of the page to be converted
     * @param scale the scaling factor, where 1 = 72 DPI
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImage(int pageIndex, float scale) throws IOException
    {
        return renderImage(pageIndex, scale, ImageType.RGB);
    }

    /**
     * Returns the given page as an RGB image at the given DPI.
     * @param pageIndex the zero-based index of the page to be converted
     * @param dpi the DPI (dots per inch) to render at
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImageWithDPI(int pageIndex, float dpi) throws IOException
    {
        return renderImage(pageIndex, dpi / 72f, ImageType.RGB);
    }

    /**
     * Returns the given page as an RGB image at the given DPI.
     * @param pageIndex the zero-based index of the page to be converted
     * @param dpi the DPI (dots per inch) to render at
     * @param imageType the type of image to return
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImageWithDPI(int pageIndex, float dpi, ImageType imageType)
            throws IOException
    {
        return renderImage(pageIndex, dpi / 72f, imageType);
    }

    /**
     * Returns the given page as an RGB or ARGB image at the given scale.
     * @param pageIndex the zero-based index of the page to be converted
     * @param scale the scaling factor, where 1 = 72 DPI
     * @param imageType the type of image to return
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImage(int pageIndex, float scale, ImageType imageType)
            throws IOException
    {
        PDPage page = document.getPage(pageIndex);

        PDRectangle cropBox = page.findCropBox();
        float widthPt = cropBox.getWidth();
        float heightPt = cropBox.getHeight();
        int widthPx = Math.round(widthPt * scale);
        int heightPx = Math.round(heightPt * scale);
        int rotationAngle = page.findRotation();

        // normalize the rotation angle
        if (rotationAngle < 0)
        {
            rotationAngle += 360;
        }
        else if (rotationAngle >= 360)
        {
            rotationAngle -= 360;
        }

        // swap width and height
        BufferedImage image;
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            image = new BufferedImage(heightPx, widthPx, imageType.toBufferedImageType());
        }
        else
        {
            image = new BufferedImage(widthPx, heightPx, imageType.toBufferedImageType());
        }

        // use a transparent background if the imageType supports alpha
        Graphics2D g = image.createGraphics();
        if (imageType != ImageType.ARGB)
        {
            g.setBackground(Color.WHITE);
        }

        renderPage(page, g, image.getWidth(), image.getHeight(), scale, scale);
        g.dispose();

        return image;
    }

    /**
     * Renders a given page to an AWT Graphics2D instance.
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics) throws IOException
    {
        renderPageToGraphics(pageIndex, graphics, 1);
    }

    /**
     * Renders a given page to an AWT Graphics2D instance.
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @param scale the scale to draw the page at
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics, float scale)
            throws IOException
    {
        PDPage page = document.getPage(pageIndex);
        // TODO need width/wight calculations? should these be in PageDrawer?
        PDRectangle cropBox = page.findCropBox();
        renderPage(page, graphics, (int)cropBox.getWidth(), (int)cropBox.getHeight(), scale, scale);
    }

    // renders a page to the given graphics
    // TODO need to be able to override this
    private void renderPage(PDPage page, Graphics2D graphics, int width, int height, float scaleX,
                            float scaleY) throws IOException
    {
        graphics.clearRect(0, 0, width, height);

        graphics.scale(scaleX, scaleY);
        // TODO should we be passing the scale to PageDrawer rather than messing with Graphics?

        PDRectangle cropBox = page.findCropBox();
        int rotationAngle = page.findRotation();
        if (rotationAngle != 0)
        {
            float translateX = 0;
            float translateY = 0;
            switch (rotationAngle)
            {
                case 90:
                    translateX = cropBox.getHeight();
                    break;
                case 270:
                    translateY = cropBox.getWidth();
                    break;
                case 180:
                    translateX = cropBox.getWidth();
                    translateY = cropBox.getHeight();
                    break;
            }
            graphics.translate(translateX, translateY);
            graphics.rotate((float) Math.toRadians(rotationAngle));
        }

        PageDrawer drawer = new PageDrawer(this);   // TODO: need to make it easy to use a custom PageDrawer
        drawer.drawPage(graphics, page, cropBox);
        drawer.dispose();
        if (clearResourcesAutomatically)
        {
            page.clear();
        }
    }
}
