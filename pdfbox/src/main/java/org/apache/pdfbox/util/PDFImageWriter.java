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

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * This class writes single pages of a pdf to a file.
 * 
 * @author <a href="mailto:DanielWilson@Users.SourceForge.net">Daniel Wilson</a>
 * 
 */
public class PDFImageWriter extends PDFStreamEngine
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFImageWriter.class);

    /**
     * Instantiate a new PDFImageWriter object.
     */
    public PDFImageWriter()
    {
    }

    /**
     * Instantiate a new PDFImageWriter object. Loading all of the operator mappings from the properties object that is
     * passed in.
     * 
     * @param props The properties containing the mapping of operators to PDFOperator classes.
     * 
     * @throws IOException If there is an error reading the properties.
     */
    public PDFImageWriter(Properties props) throws IOException
    {
        super(props);
    }

    /**
     * Converts a given page range of a PDF document to bitmap images.
     * 
     * @param document the PDF document
     * @param imageType the target format (ex. "png")
     * @param password the password (needed if the PDF is encrypted)
     * @param startPage the start page (1 is the first page)
     * @param endPage the end page (set to Integer.MAX_VALUE for all pages)
     * @param outputPrefix used to construct the filename for the individual images
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public boolean writeImage(PDDocument document, String imageType, String password, int startPage, int endPage,
            String outputPrefix) throws IOException
    {
        int resolution;
        try
        {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        catch (HeadlessException e)
        {
            resolution = 96;
        }
        return writeImage(document, imageType, password, startPage, endPage, outputPrefix, 8, resolution);
    }

    /**
     * Converts a given page range of a PDF document to bitmap images.
     * 
     * @param document the PDF document
     * @param imageFormat the target format (ex. "png")
     * @param password the password (needed if the PDF is encrypted)
     * @param startPage the start page (1 is the first page)
     * @param endPage the end page (set to Integer.MAX_VALUE for all pages)
     * @param outputPrefix used to construct the filename for the individual images
     * @param imageType the image type (see {@link BufferedImage}.TYPE_*)
     * @param resolution the resolution in dpi (dots per inch)
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public boolean writeImage(PDDocument document, String imageFormat, String password, int startPage, int endPage,
            String outputPrefix, int imageType, int resolution) throws IOException
    {
        boolean bSuccess = true;
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        int pagesSize = pages.size();
        for (int i = startPage - 1; i < endPage && i < pagesSize; i++)
        {
            BufferedImage image = RenderUtil.convertToImage(pages.get(i), imageType, resolution);
            String fileName = outputPrefix + (i + 1);
            LOG.info("Writing: " + fileName + "." + imageFormat);
            bSuccess &= ImageIOUtil.writeImage(image, imageFormat, fileName, imageType, resolution);
        }
        return bSuccess;
    }

}
