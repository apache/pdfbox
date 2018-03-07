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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * Renders a PDF document to an AWT BufferedImage.
 * This class may be overridden in order to perform custom rendering.
 *
 * @author John Hewson
 */
public class PDFRenderer
{
    protected final PDDocument document;
    // TODO keep rendering state such as caches here
    
    /**
    * Default annotations filter, returns all annotations
    */
    private AnnotationFilter annotationFilter = new AnnotationFilter()
    {
        @Override
        public boolean accept(PDAnnotation annotation)
        {
            return true;
        }
    };

    /**
     * Creates a new PDFRenderer.
     * @param document the document to render
     */
    public PDFRenderer(PDDocument document)
    {
        this.document = document;
    }
    
    /**
     * Return the AnnotationFilter.
     * 
     * @return the AnnotationFilter
     */
    public AnnotationFilter getAnnotationsFilter()
    {
        return annotationFilter;
    }

    /**
     * Set the AnnotationFilter.
     * 
     * <p>Allows to only render annotation accepted by the filter.
     * 
     * @param annotationsFilter the AnnotationFilter
     */
    public void setAnnotationsFilter(AnnotationFilter annotationsFilter)
    {
        this.annotationFilter = annotationsFilter;
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

        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();
        int widthPx = Math.round(widthPt * scale);
        int heightPx = Math.round(heightPt * scale);
        int rotationAngle = page.getRotation();

        int bimType = imageType.toBufferedImageType();
        if (imageType != ImageType.ARGB && hasBlendMode(page))
        {
            // PDFBOX-4095: if the PDF has blending on the top level, draw on transparent background
            // Inpired from PDF.js: if a PDF page uses any blend modes other than Normal, 
            // PDF.js renders everything on a fully transparent RGBA canvas. 
            // Finally when the page has been rendered, PDF.js draws the RGBA canvas on a white canvas.
            bimType = BufferedImage.TYPE_INT_ARGB;
        }

        // swap width and height
        BufferedImage image;
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            image = new BufferedImage(heightPx, widthPx, bimType);
        }
        else
        {
            image = new BufferedImage(widthPx, heightPx, bimType);
        }

        // use a transparent background if the image type supports alpha
        Graphics2D g = image.createGraphics();
        if (image.getType() == BufferedImage.TYPE_INT_ARGB)
        {
            g.setBackground(new Color(0, 0, 0, 0));
        }
        else
        {
            g.setBackground(Color.WHITE);
        }
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
        
        transform(g, page, scale, scale);

        // the end-user may provide a custom PageDrawer
        PageDrawerParameters parameters = new PageDrawerParameters(this, page);
        PageDrawer drawer = createPageDrawer(parameters);
        drawer.drawPage(g, page.getCropBox());       
        
        g.dispose();

        if (image.getType() != imageType.toBufferedImageType())
        {
            // PDFBOX-4095: draw temporary transparent image on white background
            BufferedImage newImage = 
                    new BufferedImage(image.getWidth(), image.getHeight(), imageType.toBufferedImageType());
            Graphics2D dstGraphics = newImage.createGraphics();
            dstGraphics.setBackground(Color.WHITE);
            dstGraphics.clearRect(0, 0, image.getWidth(), image.getHeight());
            dstGraphics.drawImage(image, 0, 0, null);
            dstGraphics.dispose();
            image = newImage;
        }

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
    		renderPageToGraphics(pageIndex, graphics, scale, scale);
    }
    
    /**
     * Renders a given page to an AWT Graphics2D instance.
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @param scaleX the scale to draw the page at for the x-axis
     * @param scaleY the scale to draw the page at for the y-axis
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics, float scaleX, float scaleY)
            throws IOException
    {
        PDPage page = document.getPage(pageIndex);
        // TODO need width/wight calculations? should these be in PageDrawer?

        transform(graphics, page, scaleX, scaleY);

        PDRectangle cropBox = page.getCropBox();
        graphics.clearRect(0, 0, (int) cropBox.getWidth(), (int) cropBox.getHeight());

        // the end-user may provide a custom PageDrawer
        PageDrawerParameters parameters = new PageDrawerParameters(this, page);
        PageDrawer drawer = createPageDrawer(parameters);
        drawer.drawPage(graphics, cropBox);
    }

    // scale rotate translate
    private void transform(Graphics2D graphics, PDPage page, float scaleX, float scaleY)
    {
        graphics.scale(scaleX, scaleY);

        // TODO should we be passing the scale to PageDrawer rather than messing with Graphics?
        int rotationAngle = page.getRotation();
        PDRectangle cropBox = page.getCropBox();

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
                default:
                    break;
            }
            graphics.translate(translateX, translateY);
            graphics.rotate((float) Math.toRadians(rotationAngle));
        }
    }

    /**
     * Returns a new PageDrawer instance, using the given parameters. May be overridden.
     */
    protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
    {
        PageDrawer pageDrawer = new PageDrawer(parameters);
        pageDrawer.setAnnotationFilter(annotationFilter);
        return pageDrawer;
    }

    private boolean hasBlendMode(PDPage page)
    {
        // check the current resources for blend modes
        PDResources resources = page.getResources();
        if (resources == null)
        {
            return false;
        }
        for (COSName name : resources.getExtGStateNames())
        {
            PDExtendedGraphicsState extGState = resources.getExtGState(name);
            if (extGState == null)
            {
                // can happen if key exists but no value 
                // see PDFBOX-3950-23EGDHXSBBYQLKYOKGZUOVYVNE675PRD.pdf
                continue;
            }
            BlendMode blendMode = extGState.getBlendMode();
            if (blendMode != BlendMode.NORMAL)
            {
                return true;
            }
        }
        return false;
    }
}
