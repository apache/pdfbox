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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Test suite for ImageIOUtil.
 */
public class TestImageIOUtils extends TestCase
{
    private static final Log LOG = LogFactory.getLog(TestImageIOUtils.class);

    /**
     * Validate page rendering for all supported image formats (JDK5).
     * 
     * @param file The file to validate
     * @param outDir Name of the output directory
     * @throws Exception when there is an exception
     */
    private void doTestFile(File file, String outDir) throws Exception
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
                            ImageType imageType, float dpi)
            throws IOException
    {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(0, dpi, imageType);
        String fileName = outputPrefix + 1;
        LOG.info("Writing: " + fileName + "." + imageFormat);
        ImageIOUtil.writeImage(image, imageFormat, fileName,  Math.round(dpi));
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
}
