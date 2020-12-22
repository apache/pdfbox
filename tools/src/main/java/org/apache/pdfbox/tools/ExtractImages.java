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
package org.apache.pdfbox.tools;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Extracts the images from a PDF file.
 *
 * @author Ben Litchfield
 */
@Command(name = "ExtractImages", description = "Extracts the images from a PDF file.")
public final class ExtractImages implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSOUT = System.out;
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    private static final List<String> JPEG = Arrays.asList(
            COSName.DCT_DECODE.getName(),
            COSName.DCT_DECODE_ABBREVIATION.getName());

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = "-password", description = "the password for the PDF or certificate in keystore.")    
    private String password;

    @Option(names = "-prefix", description = "the image prefix (default to pdf name).")    
    private String prefix;

    @Option(names = "-useDirectJPEG", description = "Forces the direct extraction of JPEG/JPX images " + 
        "regardless of colorspace or masking.")    
    private boolean useDirectJPEG;

    @Option(names = "-noColorConvert", description = "Images are extracted with their " +
        "original colorspace if possible.")    
    private boolean noColorConvert;

    @Parameters(paramLabel = "inputfile", index = "0", arity = "1", description = "the PDF file to decrypt.")
    private File infile;

    private final Set<COSStream> seen = new HashSet<>();
    private int imageCounter = 1;

    /**
     * Entry point for the application.
     *
     * @param args The command-line arguments.
     */
    public static void main(final String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        final int exitCode = new CommandLine(new ExtractImages()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            final AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent())
            {
                SYSERR.println("You do not have permission to extract images");
                return 1;
            }

            if (prefix == null)
            {
                prefix = FilenameUtils.removeExtension(infile.getAbsolutePath());
            }

            for (final PDPage page : document.getPages())
            {
                final ImageGraphicsEngine extractor = new ImageGraphicsEngine(page);
                extractor.run();
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println("Error extracting images: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }

    private class ImageGraphicsEngine extends PDFGraphicsStreamEngine
    {
        protected ImageGraphicsEngine(final PDPage page)
        {
            super(page);
        }

        public void run() throws IOException
        {
            final PDPage page = getPage();
            processPage(page);
            final PDResources res = page.getResources();
            if (res == null)
            {
                return;
            }
            for (final COSName name : res.getExtGStateNames())
            {
                final PDExtendedGraphicsState extGState = res.getExtGState(name);
                if (extGState == null)
                {
                    // can happen if key exists but no value 
                    continue;
                }
                final PDSoftMask softMask = extGState.getSoftMask();
                if (softMask != null)
                {
                    final PDTransparencyGroup group = softMask.getGroup();
                    if (group != null)
                    {
                        // PDFBOX-4327: without this line NPEs will occur
                        res.getExtGState(name).copyIntoGraphicsState(getGraphicsState());

                        processSoftMask(group);
                    }
                }
            }
        }

        @Override
        public void drawImage(final PDImage pdImage) throws IOException
        {
            if (pdImage instanceof PDImageXObject)
            {
                if (pdImage.isStencil())
                {
                    processColor(getGraphicsState().getNonStrokingColor());
                }
                final PDImageXObject xobject = (PDImageXObject)pdImage;
                if (seen.contains(xobject.getCOSObject()))
                {
                    // skip duplicate image
                    return;
                }
                seen.add(xobject.getCOSObject());
            }

            // save image
            final String name = prefix + "-" + imageCounter;
            imageCounter++;

            write2file(pdImage, name, useDirectJPEG, noColorConvert);
        }

        @Override
        public void appendRectangle(final Point2D p0, final Point2D p1, final Point2D p2, final Point2D p3)
                throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void clip(final int windingRule) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void moveTo(final float x, final float y) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void lineTo(final float x, final float y) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void curveTo(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3)
                throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public Point2D getCurrentPoint() throws IOException
        {
            return new Point2D.Float(0, 0);
        }

        @Override
        public void closePath() throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void endPath() throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        protected void showGlyph(final Matrix textRenderingMatrix,
                                 final PDFont font,
                                 final int code,
                                 final Vector displacement) throws IOException
        {
            final RenderingMode renderingMode = getGraphicsState().getTextState().getRenderingMode();
            if (renderingMode.isFill())
            {
                processColor(getGraphicsState().getNonStrokingColor());
            }
            if (renderingMode.isStroke())
            {
                processColor(getGraphicsState().getStrokingColor());
            }
        }

        @Override
        public void strokePath() throws IOException
        {
            processColor(getGraphicsState().getStrokingColor());
        }

        @Override
        public void fillPath(final int windingRule) throws IOException
        {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void fillAndStrokePath(final int windingRule) throws IOException
        {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void shadingFill(final COSName shadingName) throws IOException
        {
            // Empty: add special handling if needed
        }

        // find out if it is a tiling pattern, then process that one
        private void processColor(final PDColor color) throws IOException
        {
            if (color.getColorSpace() instanceof PDPattern)
            {
                final PDPattern pattern = (PDPattern) color.getColorSpace();
                final PDAbstractPattern abstractPattern = pattern.getPattern(color);
                if (abstractPattern instanceof PDTilingPattern)
                {
                    processTilingPattern((PDTilingPattern) abstractPattern, null, null);
                }
            }
        }

        /**
         * Writes the image to a file with the filename prefix + an appropriate suffix, like
         * "Image.jpg". The suffix is automatically set depending on the image compression in the
         * PDF.
         *
         * @param pdImage the image.
         * @param prefix the filename prefix.
         * @param directJPEG if true, force saving JPEG/JPX streams as they are in the PDF file.
         * @param noColorConvert if true, images are extracted with their original colorspace if
         * possible.
         * @throws IOException When something is wrong with the corresponding file.
         */
        private void write2file(final PDImage pdImage, final String prefix, final boolean directJPEG,
                                final boolean noColorConvert) throws IOException
        {
            String suffix = pdImage.getSuffix();
            if (suffix == null || "jb2".equals(suffix))
            {
                suffix = "png";
            }
            else if ("jpx".equals(suffix))
            {
                // use jp2 suffix for file because jpx not known by windows
                suffix = "jp2";
            }

            if (hasMasks(pdImage))
            {
                // TIKA-3040, PDFBOX-4771: can't save ARGB as JPEG
                suffix = "png";
            }

            if (noColorConvert)
            {
                // We write the raw image if in any way possible.
                // But we have no alpha information here.
                final BufferedImage image = pdImage.getRawImage();
                if (image != null)
                {
                    final int elements = image.getRaster().getNumDataElements();
                    suffix = "png";
                    if (elements > 3)
                    {
                        // More then 3 channels: Thats likely CMYK. We use tiff here,
                        // but a TIFF codec must be in the class path for this to work.
                        suffix = "tiff";
                    }
                    try (FileOutputStream imageOutput = new FileOutputStream(prefix + "." + suffix))
                    {
                        SYSOUT.println("Writing image: " + prefix + "." + suffix);
                        ImageIOUtil.writeImage(image, suffix, imageOutput);
                        imageOutput.flush();
                    }
                    return;
                }
            }

            try (FileOutputStream imageOutput = new FileOutputStream(prefix + "." + suffix))
            {
                SYSOUT.println("Writing image: " + prefix + "." + suffix);

                if ("jpg".equals(suffix))
                {
                    final String colorSpaceName = pdImage.getColorSpace().getName();
                    if (directJPEG || 
                        (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
                         PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)))
                    {
                        // RGB or Gray colorspace: get and write the unmodified JPEG stream
                        final InputStream data = pdImage.createInputStream(JPEG);
                        IOUtils.copy(data, imageOutput);
                        IOUtils.closeQuietly(data);
                    }
                    else
                    {
                        // for CMYK and other "unusual" colorspaces, the JPEG will be converted
                        final BufferedImage image = pdImage.getImage();
                        if (image != null)
                        {
                            ImageIOUtil.writeImage(image, suffix, imageOutput);
                        }
                    }
                }
                else if ("jp2".equals(suffix))
                {
                    final String colorSpaceName = pdImage.getColorSpace().getName();
                    if (directJPEG
                            || (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName)
                            || PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)))
                    {
                        // RGB or Gray colorspace: get and write the unmodified JPEG2000 stream
                        final InputStream data = pdImage.createInputStream(
                                Arrays.asList(COSName.JPX_DECODE.getName()));
                        IOUtils.copy(data, imageOutput);
                        IOUtils.closeQuietly(data);
                    }
                    else
                    {
                        // for CMYK and other "unusual" colorspaces, the image will be converted
                        final BufferedImage image = pdImage.getImage();
                        if (image != null)
                        {
                            ImageIOUtil.writeImage(image, "jpeg2000", imageOutput);
                        }
                    }
                }
                else if ("tiff".equals(suffix) && pdImage.getColorSpace().equals(PDDeviceGray.INSTANCE))
                {
                    final BufferedImage image = pdImage.getImage();
                    if (image == null)
                    {
                        return;
                    }
                    // CCITT compressed images can have a different colorspace, but this one is B/W
                    // This is a bitonal image, so copy to TYPE_BYTE_BINARY
                    // so that a G4 compressed TIFF image is created by ImageIOUtil.writeImage()
                    final int w = image.getWidth();
                    final int h = image.getHeight();
                    final BufferedImage bitonalImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
                    // copy image the old fashioned way - ColorConvertOp is slower!
                    for (int y = 0; y < h; y++)
                    {
                        for (int x = 0; x < w; x++)
                        {
                            bitonalImage.setRGB(x, y, image.getRGB(x, y));
                        }
                    }
                    ImageIOUtil.writeImage(bitonalImage, suffix, imageOutput);
                }
                else
                {
                    final BufferedImage image = pdImage.getImage();
                    if (image != null)
                    {
                        ImageIOUtil.writeImage(image, suffix, imageOutput);
                    }
                }
                imageOutput.flush();
            }
        }

        private boolean hasMasks(final PDImage pdImage) throws IOException
        {
            if (pdImage instanceof PDImageXObject)
            {
                final PDImageXObject ximg = (PDImageXObject) pdImage;
                return ximg.getMask() != null || ximg.getSoftMask() != null;
            }
            return false;
        }
    }
}
