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
package org.apache.pdfbox.util;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
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
     * Validate page rendering for all supported image formats (JDK5).
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
            float dpi = 120;
            document = PDDocument.load(file);
            // testing PNG
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi);
            checkResolution(outDir + file.getName() + "-1." + imageType, (int) dpi);
            // testing JPG/JPEG
            imageType = "jpg";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi);
            // testing BMP
            imageType = "bmp";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi);
            // testing WBMP
            imageType = "wbmp";
            writeImage(document, imageType, outDir + file.getName() + "-", ImageType.RGB, dpi);
            // testing TIFF
            imageType = "tif";
            writeImage(document, imageType, outDir + file.getName() + "-bw-", ImageType.BINARY, dpi);
            writeImage(document, imageType, outDir + file.getName() + "-co-", ImageType.RGB, dpi);
        }
        finally
        {
            if (document!= null)
            {
                document.close();
            }
        }
    }

    private void writeImage(PDDocument document, String imageFormat, String outputPrefix,
                            ImageType imageType, float dpi) throws IOException
    {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(0, dpi, imageType);
        String fileName = outputPrefix + 1;
        LOG.info("Writing: " + fileName + "." + imageFormat);
        ImageIOUtil.writeImage(image, imageFormat, fileName,  Math.round(dpi));
    }

    /**
     * Test to validate image rendering of file set.
     * @throws Exception when there is an exception
     */
    public void testRenderImage() throws Exception
    {
        String inDir = "src/test/resources/input/ImageIOUtil";
        String outDir = "target/test-output/ImageIOUtil/";
        new File(outDir).mkdir();

        File[] testFiles = new File(inDir).listFiles(new FilenameFilter()
        {
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
     * Get the compression of a TIFF file
     *
     * @param filename Filename
     * @return the TIFF compression
     *
     * @throws IOException
     */
    String getTiffCompression(String filename) throws IOException
    {
        Iterator readers = ImageIO.getImageReadersBySuffix("tiff");
        ImageReader reader = (ImageReader) readers.next();
        ImageInputStream iis = ImageIO.createImageInputStream(new File(filename));
        reader.setInput(iis);
        IIOMetadata imageMetadata = reader.getImageMetadata(0);
        Element root = (Element) imageMetadata.getAsTree(STANDARD_METADATA_FORMAT);
        Element comprElement = (Element) root.getElementsByTagName("Compression").item(0);
        Node comprTypeNode = comprElement.getElementsByTagName("CompressionTypeName").item(0);
        String compression = comprTypeNode.getAttributes().getNamedItem("value").getNodeValue();
        iis.close();
        reader.dispose();
        return compression;
    }
    
}
