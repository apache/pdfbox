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
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
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
    private static final Log LOG = LogFactory.getLog(PDFRenderer.class);

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

    private boolean subsamplingAllowed = false;

    private RenderDestination defaultDestination;

    private RenderingHints renderingHints = null;

    private BufferedImage pageImage;

    private static boolean kcmsLogged = false;

    /**
     * Creates a new PDFRenderer.
     * @param document the document to render
     */
    public PDFRenderer(PDDocument document)
    {
        this.document = document;

        if (!kcmsLogged)
        {
            suggestKCMS();
            kcmsLogged = true;
        }
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
     * Value indicating if the renderer is allowed to subsample images before drawing, according to
     * image dimensions and requested scale.
     *
     * Subsampling may be faster and less memory-intensive in some cases, but it may also lead to
     * loss of quality, especially in images with high spatial frequency.
     *
     * @return true if subsampling of images is allowed, false otherwise.
     */
    public boolean isSubsamplingAllowed()
    {
        return subsamplingAllowed;
    }

    /**
     * Sets a value instructing the renderer whether it is allowed to subsample images before
     * drawing. The subsampling frequency is determined according to image size and requested scale.
     *
     * Subsampling may be faster and less memory-intensive in some cases, but it may also lead to
     * loss of quality, especially in images with high spatial frequency.
     *
     * @param subsamplingAllowed The new value indicating if subsampling is allowed.
     */
    public void setSubsamplingAllowed(boolean subsamplingAllowed)
    {
        this.subsamplingAllowed = subsamplingAllowed;
    }

    /**
     * @return the defaultDestination
     */
    public RenderDestination getDefaultDestination()
    {
        return defaultDestination;
    }

    /**
     * @param defaultDestination the defaultDestination to set
     */
    public void setDefaultDestination(RenderDestination defaultDestination)
    {
        this.defaultDestination = defaultDestination;
    }

    /**
     * Get the rendering hints.
     *
     * @return the rendering hints or null if none are set.
     */
    public RenderingHints getRenderingHints()
    {
        return renderingHints;
    }

    /**
     * Set the rendering hints. Use this to influence rendering quality and speed. If you don't set
     * them yourself or pass null, PDFBox will decide <b><u>at runtime</u></b> depending on the
     * destination.
     *
     * @param renderingHints
     */
    public void setRenderingHints(RenderingHints renderingHints)
    {
        this.renderingHints = renderingHints;
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
        return renderImage(pageIndex, scale, imageType, 
                           defaultDestination == null ? RenderDestination.EXPORT : defaultDestination);
    }

    /**
     * Returns the given page as an RGB or ARGB image at the given scale.
     * @param pageIndex the zero-based index of the page to be converted
     * @param scale the scaling factor, where 1 = 72 DPI
     * @param imageType the type of image to return
     * @param destination controlling visibility of optional content groups
     * @return the rendered page image
     * @throws IOException if the PDF cannot be read
     */
    public BufferedImage renderImage(int pageIndex, float scale, ImageType imageType, RenderDestination destination)
            throws IOException
    {
        PDPage page = document.getPage(pageIndex);

        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();

        // PDFBOX-4306 avoid single blank pixel line on the right or on the bottom
        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);

        // PDFBOX-4518 the maximum size (w*h) of a buffered image is limited to Integer.MAX_VALUE
        if ((long) widthPx * (long) heightPx > Integer.MAX_VALUE)
        {
            throw new IOException("Maximum size of image exceeded (w * h * scale) = "//
                    + widthPt + " * " + heightPt + " * " + scale + " > " + Integer.MAX_VALUE);
        }

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

        pageImage = image;

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
        RenderingHints actualRenderingHints =
                renderingHints == null ? createDefaultRenderingHints(g) : renderingHints;
        PageDrawerParameters parameters = new PageDrawerParameters(this, page, subsamplingAllowed,
                                                                   destination, actualRenderingHints);
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
     * Renders a given page to an AWT Graphics2D instance at 72 DPI.
     * <p>
     * Read {@link #renderPageToGraphics(int, java.awt.Graphics2D, float, float, org.apache.pdfbox.rendering.RenderDestination) renderPageToGraphics(int, Graphics2D, float, float, RenderDestination)}
     * before using this.
     *
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
     * <p>
     * Read {@link #renderPageToGraphics(int, java.awt.Graphics2D, float, float, org.apache.pdfbox.rendering.RenderDestination) renderPageToGraphics(int, Graphics2D, float, float, RenderDestination)}
     * before using this.
     *
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @param scale the scaling factor, where 1 = 72 DPI
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics, float scale)
            throws IOException
    {
        renderPageToGraphics(pageIndex, graphics, scale, scale);
    }

    /**
     * Renders a given page to an AWT Graphics2D instance.
     * <p>
     * Read {@link #renderPageToGraphics(int, java.awt.Graphics2D, float, float, org.apache.pdfbox.rendering.RenderDestination) renderPageToGraphics(int, Graphics2D, float, float, RenderDestination)}
     * before using this.
     *
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @param scaleX the scale to draw the page at for the x-axis, where 1 = 72 DPI
     * @param scaleY the scale to draw the page at for the y-axis, where 1 = 72 DPI
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics, float scaleX, float scaleY)
            throws IOException
    {
        renderPageToGraphics(pageIndex, graphics, scaleX, scaleY, 
                             defaultDestination == null ? RenderDestination.VIEW : defaultDestination);
    }

    /**
     * Renders a given page to an AWT Graphics2D instance.
     * <p>
     * Known problems:
     * <ul>
     * <li>rendering of PDF files with transparencies is not supported on Ubuntu, see
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4581">PDFBOX-4581</a> and
     * <a href="https://bugs.openjdk.java.net/browse/JDK-6689349">JDK-6689349</a>. Rendering will
     * not abort, but the pages will be rendered incorrectly.</li>
     * <li>Clipping the Graphics2D will not work properly, see
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4583">PDFBOX-4583</a>.</li>
     * </ul>
     * If you encounter these problems, then you should render into an image by using the
     * {@link #renderImage(int) renderImage} methods.
     * 
     * @param pageIndex the zero-based index of the page to be converted
     * @param graphics the Graphics2D on which to draw the page
     * @param scaleX the scale to draw the page at for the x-axis, where 1 = 72 DPI
     * @param scaleY the scale to draw the page at for the y-axis, where 1 = 72 DPI
     * @param destination controlling visibility of optional content groups
     * @throws IOException if the PDF cannot be read
     */
    public void renderPageToGraphics(int pageIndex, Graphics2D graphics, float scaleX, float scaleY, RenderDestination destination)
            throws IOException
    {
        PDPage page = document.getPage(pageIndex);
        // TODO need width/wight calculations? should these be in PageDrawer?

        transform(graphics, page, scaleX, scaleY);

        PDRectangle cropBox = page.getCropBox();
        graphics.clearRect(0, 0, (int) cropBox.getWidth(), (int) cropBox.getHeight());

        // the end-user may provide a custom PageDrawer
        RenderingHints actualRenderingHints =
                renderingHints == null ? createDefaultRenderingHints(graphics) : renderingHints;
        PageDrawerParameters parameters = new PageDrawerParameters(this, page, subsamplingAllowed,
                                                                   destination, actualRenderingHints);
        PageDrawer drawer = createPageDrawer(parameters);
        drawer.drawPage(graphics, cropBox);
    }

    /**
     * Indicates whether an optional content group is enabled.
     * @param group the group
     * @return true if the group is enabled
     */
    public boolean isGroupEnabled(PDOptionalContentGroup group)
    {
        PDOptionalContentProperties ocProperties = document.getDocumentCatalog().getOCProperties();
        return ocProperties == null || ocProperties.isGroupEnabled(group);
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
            graphics.rotate(Math.toRadians(rotationAngle));
        }
    }

    private boolean isBitonal(Graphics2D graphics)
    {
        GraphicsConfiguration deviceConfiguration = graphics.getDeviceConfiguration();
        if (deviceConfiguration == null)
        {
            return false;
        }
        GraphicsDevice device = deviceConfiguration.getDevice();
        if (device == null)
        {
            return false;
        }
        DisplayMode displayMode = device.getDisplayMode();
        if (displayMode == null)
        {
            return false;
        }
        return displayMode.getBitDepth() == 1;
    }

    private RenderingHints createDefaultRenderingHints(Graphics2D graphics)
    {
        RenderingHints r = new RenderingHints(null);
        r.put(RenderingHints.KEY_INTERPOLATION, isBitonal(graphics) ?
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR :
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        r.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        r.put(RenderingHints.KEY_ANTIALIASING, isBitonal(graphics) ?
                                        RenderingHints.VALUE_ANTIALIAS_OFF :
                                        RenderingHints.VALUE_ANTIALIAS_ON);
        return r;
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

    /**
     * Returns the image to which the current page is being rendered.
     * May be null if the page is rendered to a Graphics2D object
     * instead of a BufferedImage.
     */
    BufferedImage getPageImage()
    {
        return pageImage;
    }

    private static void suggestKCMS()
    {
        String cmmProperty = System.getProperty("sun.java2d.cmm");
        if (isMinJdk8() && !"sun.java2d.cmm.kcms.KcmsServiceProvider".equals(cmmProperty))
        {
            try
            {
                // Make sure that class exists
                Class.forName("sun.java2d.cmm.kcms.KcmsServiceProvider");

                String version = System.getProperty("java.version");
                if (version == null ||
                    isGoodVersion(version, "1.8.0_(\\d+)", 191) ||
                    isGoodVersion(version, "9.0.(\\d+)", 4))
                {
                    return;
                }
                LOG.info("Your current java version is: " + version);
                LOG.info("To get higher rendering speed on old java 1.8 or 9 versions,");
                LOG.info("  update to the latest 1.8 or 9 version (>= 1.8.0_191 or >= 9.0.4),");
                LOG.info("  or");
                LOG.info("  use the option -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider");
                LOG.info("  or call System.setProperty(\"sun.java2d.cmm\", \"sun.java2d.cmm.kcms.KcmsServiceProvider\")");
            }
            catch (ClassNotFoundException e)
            {
                // KCMS not available
            }
        }
    }

    private static boolean isGoodVersion(String version, String regex, int min)
    {
        Matcher matcher = Pattern.compile(regex).matcher(version);
        if (matcher.matches() && matcher.groupCount() >= 1)
        {
            try
            {
                int v = Integer.parseInt(matcher.group(1));
                if (v >= min)
                {
                    // LCMS no longer bad
                    return true;
                }
            }
            catch (NumberFormatException ex)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isMinJdk8()
    {
        // strategy from lucene-solr/lucene/core/src/java/org/apache/lucene/util/Constants.java
        String version = System.getProperty("java.specification.version");
        final StringTokenizer st = new StringTokenizer(version, ".");
        try
        {
            int major = Integer.parseInt(st.nextToken());
            int minor = 0;
            if (st.hasMoreTokens())
            {
                minor = Integer.parseInt(st.nextToken());
            }
            return major > 1 || (major == 1 && minor >= 8);
        }
        catch (NumberFormatException nfe)
        {
            // maybe some new numbering scheme in the 22nd century
            return true;
        }
    }
}
