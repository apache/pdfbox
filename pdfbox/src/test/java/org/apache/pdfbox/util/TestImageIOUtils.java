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
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Test suite for ImageIOUtil.
 * 
 */
public class TestImageIOUtils extends TestCase
{

    /**
     * Logger instance.
     */
    private static final Log LOG = LogFactory.getLog(TestImageIOUtils.class);

    private boolean testFailed = false;

    /**
     * Validate page rendering for all supported image formats (JDK5).
     * 
     * @param file The file to validate
     * @param inDir Name of the input directory
     * @param outDir Name of the output directory
     * @throws Exception when there is an exception
     */
    private void doTestFile(File file, String inDir, String outDir) throws Exception
    {
        PDDocument document = null;
        String imageType = "png";
        LOG.info("Preparing to convert " + file.getName());
        try
        {
            int resolution = 120;
            document = PDDocument.load(file);
            // testing PNG
            writeImage(document, imageType, outDir + file.getName() + "-", BufferedImage.TYPE_INT_RGB, resolution);
            // testing JPG/JPEG
            imageType = "jpg";
            writeImage(document, imageType, outDir + file.getName() + "-", BufferedImage.TYPE_INT_RGB, resolution);
            // testing BMP
            imageType = "bmp";
            writeImage(document, imageType, outDir + file.getName() + "-", BufferedImage.TYPE_INT_RGB, resolution);
            // testing WBMP
            imageType = "wbmp";
            writeImage(document, imageType, outDir + file.getName() + "-", BufferedImage.TYPE_BYTE_BINARY, resolution);
        }
        catch (Exception e)
        {
            testFailed = true;
            LOG.error("Error converting file " + file.getName() + " using image type " + imageType, e);
        }
        finally
        {
            document.close();
        }

    }

    private void writeImage(PDDocument document, String imageFormat, String outputPrefix, int imageType, int resolution)
            throws IOException
    {
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        BufferedImage image = RenderUtil.convertToImage(pages.get(0), imageType, resolution);
        String fileName = outputPrefix + 1;
        System.out.println("Writing: " + fileName + "." + imageFormat);
        ImageIOUtil.writeImage(image, imageFormat, fileName, imageType, resolution);
    }

    /**
     * Test to validate image rendering of file set.
     * 
     * @throws Exception when there is an exception
     */
    public void testRenderImage() throws Exception
    {
        String inDir = "src/test/resources/input/rendering";
        String outDir = "target/test-output/";

        File[] testFiles = new File(inDir).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return (name.endsWith(".pdf") || name.endsWith(".ai"));
            }
        });

        for (int n = 0; n < testFiles.length; n++)
        {
            doTestFile(testFiles[n], inDir, outDir);
        }

        if (testFailed)
        {
            fail("One or more failures, see test log for details");
        }
    }

}
