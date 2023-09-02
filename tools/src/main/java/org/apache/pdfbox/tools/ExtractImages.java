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
import java.util.Collections;
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
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Extracts the images from a PDF file.
 *
 * @author Ben Litchfield
 */
@Command(name = "extractimages", header = "Extracts the images from a PDF document", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class ExtractImages implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSOUT = System.out;
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    private static final List<String> JPEG = Arrays.asList(
            COSName.DCT_DECODE.getName(),
            COSName.DCT_DECODE_ABBREVIATION.getName());

    @Option(names = "-password", description = "the password for the PDF or certificate in keystore.", arity = "0..1", interactive = true)    
    private String password;

    @Option(names = "-prefix", description = "the image prefix (default to pdf name).")    
    private String prefix;

    @Option(names = "-useDirectJPEG", description = "Forces the direct extraction of JPEG/JPX images " + 
        "regardless of colorspace or masking.")    
    private boolean useDirectJPEG;

    @Option(names = "-noColorConvert", description = "Images are extracted with their " +
        "original colorspace if possible.")    
    private boolean noColorConvert;

    @Option(names = {"-i", "--input"}, description = "the PDF file", required = true)
    private File infile;

    private final Set<COSStream> seen = new HashSet<>();
    private int imageCounter = 1;

    /**
     * Entry point for the application.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ExtractImages()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent())
            {
                SYSERR.println("You do not have permission to extract images");
                return 1;
            }

            if (prefix == null)
            {
                prefix = FilenameUtils.removeExtension(infile.getAbsolutePath());
            }

            for (PDPage page : document.getPages())
            {
                ImageGraphicsEngine extractor = new ImageGraphicsEngine(page);
                extractor.run();
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println("Error extracting images [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }

    private class ImageGraphicsEngine extends PDFGraphicsStreamEngine
    {
        protected ImageGraphicsEngine(PDPage page)
        {
            super(page);
        }

        public void run() throws IOException
        {
            PDPage page = getPage();
            processPage(page);
            PDResources res = page.getResources();
            if (res == null)
            {
                return;
            }
            for (COSName name : res.getExtGStateNames())
            {
                PDExtendedGraphicsState extGState = res.getExtGState(name);
                if (extGState == null)
                {
                    // can happen if key exists but no value 
                    continue;
                }
                PDSoftMask softMask = extGState.getSoftMask();
                if (softMask != null)
                {
                    PDTransparencyGroup group = softMask.getGroup();
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
        public void drawImage(PDImage pdImage) throws IOException
        {
            if (pdImage instanceof PDImageXObject)
            {
                if (pdImage.isStencil())
                {
                    processColor(getGraphicsState().getNonStrokingColor());
                }
                PDImageXObject xobject = (PDImageXObject)pdImage;
                if (seen.contains(xobject.getCOSObject()))
                {
                    // skip duplicate image
                    return;
                }
                seen.add(xobject.getCOSObject());
            }

            // save image
            String name = prefix + "-" + imageCounter;
            imageCounter++;

            write2file(pdImage, name, useDirectJPEG, noColorConvert);
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)
                throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void clip(int windingRule) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void moveTo(float x, float y) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void lineTo(float x, float y) throws IOException
        {
            // Empty: add special handling if needed
        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
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
        protected void showGlyph(Matrix textRenderingMatrix, 
                                 PDFont font,
                                 int code,
                                 Vector displacement) throws IOException
        {
            PDGraphicsState graphicsState = getGraphicsState();
            RenderingMode renderingMode = graphicsState.getTextState().getRenderingMode();
            if (renderingMode.isFill())
            {
                processColor(graphicsState.getNonStrokingColor());
            }
            if (renderingMode.isStroke())
            {
                processColor(graphicsState.getStrokingColor());
            }
        }

        @Override
        public void strokePath() throws IOException
        {
            processColor(getGraphicsState().getStrokingColor());
        }

        @Override
        public void fillPath(int windingRule) throws IOException
        {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException
        {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void shadingFill(COSName shadingName) throws IOException
        {
            // Empty: add special handling if needed
        }

        // find out if it is a tiling pattern, then process that one
        private void processColor(PDColor color) throws IOException
        {
            if (color.getColorSpace() instanceof PDPattern)
            {
                PDPattern pattern = (PDPattern) color.getColorSpace();
                PDAbstractPattern abstractPattern = pattern.getPattern(color);
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
        private void write2file(PDImage pdImage, String prefix, boolean directJPEG,
                boolean noColorConvert) throws IOException
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
                BufferedImage image = pdImage.getRawImage();
                if (image != null)
                {
                    int elements = image.getRaster().getNumDataElements();
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
                    String colorSpaceName = pdImage.getColorSpace().getName();
                    if (directJPEG || 
                        (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
                         PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)))
                    {
                        // RGB or Gray colorspace: get and write the unmodified JPEG stream
                        InputStream data = pdImage.createInputStream(JPEG);
                        data.transferTo(imageOutput);
                        IOUtils.closeQuietly(data);
                    }
                    else
                    {
                        // for CMYK and other "unusual" colorspaces, the JPEG will be converted
                        BufferedImage image = pdImage.getImage();
                        if (image != null)
                        {
                            ImageIOUtil.writeImage(image, suffix, imageOutput);
                        }
                    }
                }
                else if ("jp2".equals(suffix))
                {
                    String colorSpaceName = pdImage.getColorSpace().getName();
                    if (directJPEG
                            || (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName)
                            || PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)))
                    {
                        // RGB or Gray colorspace: get and write the unmodified JPEG2000 stream
                        InputStream data = pdImage.createInputStream(
                                Collections.singletonList(COSName.JPX_DECODE.getName()));
                        data.transferTo(imageOutput);
                        IOUtils.closeQuietly(data);
                    }
                    else
                    {
                        // for CMYK and other "unusual" colorspaces, the image will be converted
                        BufferedImage image = pdImage.getImage();
                        if (image != null)
                        {
                            ImageIOUtil.writeImage(image, "jpeg2000", imageOutput);
                        }
                    }
                }
                else if ("tiff".equals(suffix) && pdImage.getColorSpace().equals(PDDeviceGray.INSTANCE))
                {
                    BufferedImage image = pdImage.getImage();
                    if (image == null)
                    {
                        return;
                    }
                    // CCITT compressed images can have a different colorspace, but this one is B/W
                    // This is a bitonal image, so copy to TYPE_BYTE_BINARY
                    // so that a G4 compressed TIFF image is created by ImageIOUtil.writeImage()
                    int w = image.getWidth();
                    int h = image.getHeight();
                    BufferedImage bitonalImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
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
                    BufferedImage image = pdImage.getImage();
                    if (image != null)
                    {
                        ImageIOUtil.writeImage(image, suffix, imageOutput);
                    }
                }
                imageOutput.flush();
            }
        }

        private boolean hasMasks(PDImage pdImage) throws IOException
        {
            if (pdImage instanceof PDImageXObject)
            {
                PDImageXObject ximg = (PDImageXObject) pdImage;
                return ximg.getMask() != null || ximg.getSoftMask() != null;
            }
            return false;
        }
    }
}
