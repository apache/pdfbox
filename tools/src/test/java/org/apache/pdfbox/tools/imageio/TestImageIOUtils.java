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
package org.apache.pdfbox.tools.imageio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.filetypedetector.FileType;
import org.apache.pdfbox.util.filetypedetector.FileTypeDetector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test suite for ImageIOUtil.
 */
public class TestImageIOUtils extends TestCase
{
    private static final Log LOG = LogFactory.getLog(TestImageIOUtils.class);
    
    /**
     * Check whether the resource images can be saved.
     * 
     * @param resources
     * @throws IOException 
     */
    void checkSaveResources(PDResources resources) throws IOException
    {
        if (resources == null)
        {
            return;
        }
        for (COSName name : resources.getXObjectNames())
        {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDImageXObject)
            {
                PDImageXObject imageObject = (PDImageXObject) xobject;
                String suffix = imageObject.getSuffix();
                if (suffix != null)
                {
                    if ("jpx".equals(suffix))
                    {
                        suffix = "JPEG2000";
                    }
                    if ("jb2".equals(suffix))
                    {
                        // jbig2 usually not available
                        suffix = "PNG";
                    }
                    boolean writeOK = ImageIOUtil.writeImage(imageObject.getImage(), suffix,
                            new ByteArrayOutputStream());
                    assertTrue(writeOK);
                }
            }
            else if (xobject instanceof PDFormXObject)
            {
                checkSaveResources(((PDFormXObject) xobject).getResources());
            }
        }
    }

    /**
     * Validate page rendering for all supported image formats (JDK5).
     *
     * @param file The file to validate
     * @param outDir Name of the output directory
     * @throws IOException when there is an exception
     */
    private void doTestFile(File file, String outDir) throws IOException
    {
        PDDocument document = null;
        String imageType = "png";
        LOG.info("Preparing to convert " + file.getName());
        try
        {
            float dpi = 36; // low DPI so that rendering is FAST
            document = PDDocument.load(file);

            // Save image resources of first page
            checkSaveResources(document.getPage(0).getResources());

            // testing PNG
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi, 1, "");
            checkResolution(outDir + file.getName() + "-1." + imageType, (int) dpi);
            checkFileTypeByContent(outDir + file.getName() + "-1." + imageType, FileType.PNG);

            // testing JPG/JPEG
            imageType = "jpg";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi, 0.5f, "");
            checkResolution(outDir + file.getName() + "-1." + imageType, (int) dpi);
            checkFileTypeByContent(outDir + file.getName() + "-1." + imageType, FileType.JPEG);

            // testing BMP
            imageType = "bmp";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi, 1, "");
            checkResolution(outDir + file.getName() + "-1." + imageType, (int) dpi);
            checkFileTypeByContent(outDir + file.getName() + "-1." + imageType, FileType.BMP);

            // testing GIF
            imageType = "gif";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi, 1, "");
            // no META data posible for GIF, thus no dpi test
            checkFileTypeByContent(outDir + file.getName() + "-1." + imageType, FileType.GIF);

            // testing WBMP
            imageType = "wbmp";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.BINARY, dpi, 1, "");
            // no META data posible for WBMP, thus no dpi test

            // testing TIFF
            imageType = "tif";
            writeImage(document, imageType, outDir + file.getName() + "-bw-", ImageType.BINARY, dpi, 1, "");
            checkResolution(outDir + file.getName() + "-bw-1." + imageType, (int) dpi);
            checkTiffCompression(outDir + file.getName() + "-bw-1." + imageType, "CCITT T.6");
            checkFileTypeByContent(outDir + file.getName() + "-bw-1." + imageType, FileType.TIFF);

            writeImage(document, imageType, outDir + file.getName() + "-coLZW-", ImageType.RGB, dpi, 1, "");
            checkResolution(outDir + file.getName() + "-coLZW-1." + imageType, (int) dpi);
            checkTiffCompression(outDir + file.getName() + "-coLZW-1." + imageType, "LZW");
            checkFileTypeByContent(outDir + file.getName() + "-coLZW-1." + imageType, FileType.TIFF);

            writeImage(document, imageType, outDir + file.getName() + "-coJPEG-", ImageType.RGB, dpi, 0.5f, "JPEG");
            checkResolution(outDir + file.getName() + "-coJPEG-1." + imageType, (int) dpi);
            checkTiffCompression(outDir + file.getName() + "-coJPEG-1." + imageType, "JPEG");
            checkFileTypeByContent(outDir + file.getName() + "-coJPEG-1." + imageType, FileType.TIFF);

            writeImage(document, imageType, outDir + file.getName() + "-coNone-", ImageType.RGB, dpi, 1, null);
            checkResolution(outDir + file.getName() + "-coNone-1." + imageType, (int) dpi);
            checkTiffCompression(outDir + file.getName() + "-coNone-1." + imageType, "None");
            checkFileTypeByContent(outDir + file.getName() + "-coNone-1." + imageType, FileType.TIFF);
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    /**
     * Checks whether file image size and content are identical
     *
     * @param filename the filename where we just wrote to
     * @param image the image that is to be checked
     * @throws IOException if something goes wrong
     */
    private void checkImageFileSizeAndContent(String filename, BufferedImage image)
            throws IOException
    {
        BufferedImage newImage = ImageIO.read(new File(filename));
        assertNotNull("File '" + filename + "' could not be read", newImage);
        checkNotBlank(filename, newImage);
        checkBufferedImageSize(filename, image, newImage);
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                if (image.getRGB(x, y) != newImage.getRGB(x, y))
                {
                    assertEquals("\"File '" + filename + "' has different pixel at (" + x + "," + y + ")", new Color(image.getRGB(x, y)), new Color(newImage.getRGB(x, y)));
                }
            }
        }
    }

    /**
     * Checks whether file image size is identical
     *
     * @param filename the filename where we just wrote to
     * @param image the image that is to be checked
     * @throws IOException if something goes wrong
     */
    private void checkImageFileSize(String filename, BufferedImage image)
            throws IOException
    {
        BufferedImage newImage = ImageIO.read(new File(filename));
        assertNotNull("File '" + filename + "' could not be read", newImage);
        checkNotBlank(filename, newImage);
        checkBufferedImageSize(filename, image, newImage);
    }

    private void checkBufferedImageSize(String filename,
            BufferedImage image, BufferedImage newImage) throws IOException
    {
        assertEquals("File '" + filename + "' has different height after read", image.getHeight(), newImage.getHeight());
        assertEquals("File '" + filename + "' has different width after read", image.getWidth(), newImage.getWidth());
    }

    private void checkNotBlank(String filename, BufferedImage newImage)
    {
        // http://stackoverflow.com/a/5253698/535646
        Set<Integer> colors = new HashSet<Integer>();
        int w = newImage.getWidth();
        int h = newImage.getHeight();
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                colors.add(newImage.getRGB(x, y));
            }
        }
        assertFalse("File '" + filename + "' has less than two colors", colors.size() < 2);
    }

    private void writeImage(PDDocument document, String imageFormat, String outputPrefix,
            ImageType imageType, float dpi, float compressionQuality,
            String compressionType) throws IOException
    {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(0, dpi, imageType);
        String fileName = outputPrefix + 1;
        LOG.info("Writing: " + fileName + "." + imageFormat);
        System.out.println("  " + fileName + "." + imageFormat); // for Maven (keep me!)
        OutputStream os = new FileOutputStream(fileName + "." + imageFormat);
        boolean res = ImageIOUtil.writeImage(image, imageFormat, os, Math.round(dpi), compressionQuality, compressionType);
        os.close();
        assertTrue("ImageIOUtil.writeImage() failed for file " + fileName, res);
        if ("jpg".equals(imageFormat) || "gif".equals(imageFormat) || "JPEG".equals(compressionType))
        {
            // jpeg is lossy, gif has 256 colors, 
            // so we can't check for content identity
            checkImageFileSize(fileName + "." + imageFormat, image);
        }
        else
        {
            checkImageFileSizeAndContent(fileName + "." + imageFormat, image);
        }
    }

    /**
     * Test to validate image rendering of file set.
     *
     * @throws Exception when there is an exception
     */
    public void testRenderImage() throws Exception
    {
        String inDir = "src/test/resources/input/ImageIOUtil";
        String outDir = "target/test-output/ImageIOUtil/";

        new File(outDir).mkdirs();
        if (!new File(outDir).exists())
        {
            throw new IOException("could not create output directory");
        }

        File[] testFiles = new File(inDir).listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return (name.endsWith(".pdf") || name.endsWith(".ai"));
            }
        });

        for (File file : testFiles)
        {
            doTestFile(file, outDir);
        }
    }

    private static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";

    /**
     * checks whether the resolution of an image file is as expected.
     *
     * @param filename the name of the file
     * @param expectedResolution the expected resolution
     *
     * @throws IOException if something goes wrong
     */
    private void checkResolution(String filename, int expectedResolution)
            throws IOException
    {
        assertFalse("Empty file " + filename, new File(filename).length() == 0);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        if ("BMP".equals(suffix.toUpperCase()))
        {
            // BMP reader doesn't work
            checkBmpResolution(filename, expectedResolution);
            return;
        }
        Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
        assertTrue("No image reader found for suffix " + suffix, readers.hasNext());
        ImageReader reader = (ImageReader) readers.next();
        ImageInputStream iis = ImageIO.createImageInputStream(new File(filename));
        assertNotNull("No ImageInputStream created for file " + filename, iis);
        reader.setInput(iis);
        IIOMetadata imageMetadata = reader.getImageMetadata(0);
        Element root = (Element) imageMetadata.getAsTree(STANDARD_METADATA_FORMAT);
        NodeList dimensionNodes = root.getElementsByTagName("Dimension");
        assertTrue("No resolution found in image file " + filename, dimensionNodes.getLength() > 0);
        Element dimensionElement = (Element) dimensionNodes.item(0);

        NodeList pixelSizeNodes = dimensionElement.getElementsByTagName("HorizontalPixelSize");
        assertTrue("No X resolution found in image file " + filename, pixelSizeNodes.getLength() > 0);
        Node pixelSizeNode = pixelSizeNodes.item(0);
        String val = pixelSizeNode.getAttributes().getNamedItem("value").getNodeValue();
        int actualResolution = (int) Math.round(25.4 / Double.parseDouble(val));
        assertEquals("X resolution doesn't match in image file " + filename, expectedResolution, actualResolution);

        pixelSizeNodes = dimensionElement.getElementsByTagName("VerticalPixelSize");
        assertTrue("No Y resolution found in image file " + filename, pixelSizeNodes.getLength() > 0);
        pixelSizeNode = pixelSizeNodes.item(0);
        val = pixelSizeNode.getAttributes().getNamedItem("value").getNodeValue();
        actualResolution = (int) Math.round(25.4 / Double.parseDouble(val));
        assertEquals("Y resolution doesn't match", expectedResolution, actualResolution);

        iis.close();
        reader.dispose();
    }

    /**
     * checks whether the resolution of a BMP image file is as expected.
     *
     * @param filename the name of the BMP file
     * @param expectedResolution the expected resolution
     *
     * @throws IOException if something goes wrong
     */
    private void checkBmpResolution(String filename, int expectedResolution)
            throws FileNotFoundException, IOException
    {
        // BMP format explained here:
        // http://www.javaworld.com/article/2077561/learn-java/java-tip-60--saving-bitmap-files-in-java.html
        // we skip 38 bytes and then read two 4 byte-integers and reverse the bytes
        DataInputStream dis = new DataInputStream(new FileInputStream(new File(filename)));
        int skipped = dis.skipBytes(38);
        assertEquals("Can't skip 38 bytes in image file " + filename, 38, skipped);
        int pixelsPerMeter = Integer.reverseBytes(dis.readInt());
        int actualResolution = (int) Math.round(pixelsPerMeter / 100.0 * 2.54);
        assertEquals("X resolution doesn't match in image file " + filename,
                expectedResolution, actualResolution);
        pixelsPerMeter = Integer.reverseBytes(dis.readInt());
        actualResolution = (int) Math.round(pixelsPerMeter / 100.0 * 2.54);
        assertEquals("Y resolution doesn't match in image file " + filename,
                expectedResolution, actualResolution);
        dis.close();
    }

    /**
     * checks whether the compression of a TIFF file is as expected.
     *
     * @param filename Filename
     * @param expectedCompression expected TIFF compression
     *
     * @throws IOException if something goes wrong
     */
    void checkTiffCompression(String filename, String expectedCompression) throws IOException
    {
        Iterator readers = ImageIO.getImageReadersBySuffix("tiff");
        ImageReader reader = (ImageReader) readers.next();
        ImageInputStream iis = ImageIO.createImageInputStream(new File(filename));
        reader.setInput(iis);
        IIOMetadata imageMetadata = reader.getImageMetadata(0);
        Element root = (Element) imageMetadata.getAsTree(STANDARD_METADATA_FORMAT);
        Element comprElement = (Element) root.getElementsByTagName("Compression").item(0);
        Node comprTypeNode = comprElement.getElementsByTagName("CompressionTypeName").item(0);
        String actualCompression = comprTypeNode.getAttributes().getNamedItem("value").getNodeValue();
        assertEquals("Incorrect TIFF compression in file " + filename, expectedCompression, actualCompression);
        iis.close();
        reader.dispose();
    }

    private void checkFileTypeByContent(String filename, FileType fileType) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
        assertEquals(fileType, FileTypeDetector.detectFileType(bis));
        IOUtils.closeQuietly(bis);  
    }

}
