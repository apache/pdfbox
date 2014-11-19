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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.TIFFInputStream;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;

/**
 * Extracts the images from a PDF file.
 *
 * <p>usage: java org.apache.pdfbox.tools.ExtractImages &lt;pdffile&gt; &lt;password&gt; [imageprefix]
 *
 * @author Ben Litchfield
 */
public class ExtractImages
{
    private static final String PASSWORD = "-password";
    private static final String PREFIX = "-prefix";
    private static final String DIRECTJPEG = "-directJPEG";

    private static final List<String> JPEG = Arrays.asList(
            COSName.DCT_DECODE.getName(),
            COSName.DCT_DECODE_ABBREVIATION.getName());

    private boolean directJPEG;
    private String prefix;

    private Set<COSStream> seen = new HashSet<COSStream>();
    private int imageCounter = 1;

    private ExtractImages()
    {
    }

    /**
     * Entry point for the application.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main(String[] args) throws Exception
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        ExtractImages extractor = new ExtractImages();
        extractor.run(args);
    }

    private void run(String[] args) throws Exception
    {
        if (args.length < 1 || args.length > 4)
        {
            usage();
        }
        else
        {
            String pdfFile = null;
            String password = "";
            for(int i = 0; i < args.length; i++)
            {
                if (args[i].equals(PASSWORD))
                {
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    password = args[i];
                }
                else if (args[i].equals(PREFIX))
                {
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    prefix = args[i];
                }
                else if (args[i].equals(DIRECTJPEG))
                {
                    directJPEG = true;
                }
                else
                {
                    if (pdfFile == null)
                    {
                        pdfFile = args[i];
                    }
                }
            }
            if (pdfFile == null)
            {
                usage();
            }
            else
            {
                if (prefix == null && pdfFile.length() >4)
                {
                    prefix = pdfFile.substring(0, pdfFile.length() -4);
                }

                extract(pdfFile, password);
            }
        }
    }

    /**
     * Print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println("Usage: java org.apache.pdfbox.tools.ExtractImages [OPTIONS] <PDF file>\n" +
                "  -password  <password>        Password to decrypt document\n" +
                "  -prefix  <image-prefix>      Image prefix(default to pdf name)\n" +
                "  -directJPEG                  Forces the direct extraction of JPEG images regardless of colorspace\n" +
                "  <PDF file>                   The PDF document to use\n");
        System.exit(1);
    }

    private void extract(String pdfFile, String password) throws IOException
    {
        PDDocument document = null;
        try
        {
            document = PDDocument.load(new File(pdfFile), password);
            AccessPermission ap = document.getCurrentAccessPermission();
            if (! ap.canExtractContent())
            {
                throw new IOException("You do not have permission to extract images");
            }

            for (int i = 0; i < document.getNumberOfPages(); i++) // todo: ITERATOR would be much better
            {
                PDPage page = document.getPage(i);
                ImageGraphicsEngine extractor = new ImageGraphicsEngine(page);
                extractor.run();
            }
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    private class ImageGraphicsEngine extends PDFGraphicsStreamEngine
    {
        protected ImageGraphicsEngine(PDPage page) throws IOException
        {
            super(page);
        }

        public void run() throws IOException
        {
            processPage(getPage());
        }

        @Override
        public void drawImage(PDImage pdImage) throws IOException
        {
            if (pdImage instanceof PDImageXObject)
            {
                PDImageXObject xobject = (PDImageXObject)pdImage;
                if (seen.contains(xobject.getCOSStream()))
                {
                    // skip duplicate image
                    return;
                }
                seen.add(xobject.getCOSStream());
            }

            // save image
            String name = prefix + "-" + imageCounter;
            imageCounter++;

            System.out.println("Writing image: " + name);
            write2file(pdImage, name, directJPEG);
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)
                throws IOException
        {

        }

        @Override
        public void clip(int windingRule) throws IOException
        {

        }

        @Override
        public void moveTo(float x, float y) throws IOException
        {

        }

        @Override
        public void lineTo(float x, float y) throws IOException
        {

        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
                throws IOException
        {

        }

        @Override
        public Point2D getCurrentPoint() throws IOException
        {
            return new Point2D.Float(0, 0);
        }

        @Override
        public void closePath() throws IOException
        {

        }

        @Override
        public void endPath() throws IOException
        {

        }

        @Override
        public void strokePath() throws IOException
        {

        }

        @Override
        public void fillPath(int windingRule) throws IOException
        {

        }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException
        {

        }

        @Override
        public void shadingFill(COSName shadingName) throws IOException
        {

        }
    }

    /**
     * Writes the image to a file with the filename + an appropriate suffix, like "Image.jpg".
     * The suffix is automatically set by the
     * @param filename the filename
     * @throws IOException When somethings wrong with the corresponding file.
     */
    private void write2file(PDImage pdImage, String filename, boolean directJPEG) throws IOException
    {
        String suffix = pdImage.getSuffix();
        if (suffix == null)
        {
            suffix = "png";
        }

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(filename + "." + suffix);
            BufferedImage image = pdImage.getImage();
            if (image != null)
            {
                if ("tiff".equals(suffix))
                {
                    TIFFInputStream.writeToOutputStream(pdImage, out);
                }
                else if ("jpg".equals(suffix))
                {
                    String colorSpaceName = pdImage.getColorSpace().getName();
                    if (directJPEG || PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
                                      PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName))
                    {
                        // RGB or Gray colorspace: get and write the unmodifiedJPEG stream
                        InputStream data = pdImage.getStream().getPartiallyFilteredStream(JPEG);
                        IOUtils.copy(data, out);
                        IOUtils.closeQuietly(data);
                    }
                    else
                    {
                        // for CMYK and other "unusual" colorspaces, the JPEG will be converted
                        ImageIOUtil.writeImage(image, suffix, out);
                    }
                }
                else 
                {
                    ImageIOUtil.writeImage(image, suffix, out);
                }
            }
            out.flush();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
}
