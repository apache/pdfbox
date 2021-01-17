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
package org.apache.pdfbox.examples.util;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Split a booklet. Based on the discussion from
 * <a href="https://issues.apache.org/jira/browse/PDFBOX-5078">PDFBOX-5078</a>, see there for
 * example files, more sample code, and a link to a project to create booklets.
 *
 * @author Tilman Hausherr
 */
public class SplitBooklet
{
    /**
     * Default constructor.
     */
    private SplitBooklet()
    {
        // example class should not be instantiated
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            usage();
            System.exit(-1);
        }
        try (PDDocument document = Loader.loadPDF(new File(args[0]));
             PDDocument outdoc = new PDDocument())
        {
            for (PDPage page : document.getPages())
            {
                PDRectangle cropBoxORIG = page.getCropBox();
                
                // make sure to have new objects
                PDRectangle cropBoxLEFT = new PDRectangle(cropBoxORIG.getCOSArray());
                PDRectangle cropBoxRIGHT = new PDRectangle(cropBoxORIG.getCOSArray());
                
                if (page.getRotation() == 90 || page.getRotation() == 270)
                {
                    cropBoxLEFT.setUpperRightY(cropBoxORIG.getLowerLeftY() + cropBoxORIG.getHeight() / 2);
                    cropBoxRIGHT.setLowerLeftY(cropBoxORIG.getLowerLeftY() + cropBoxORIG.getHeight() / 2);
                }
                else
                {
                    cropBoxLEFT.setUpperRightX(cropBoxORIG.getLowerLeftX() + cropBoxORIG.getWidth() / 2);
                    cropBoxRIGHT.setLowerLeftX(cropBoxORIG.getLowerLeftX() + cropBoxORIG.getWidth() / 2);
                }
                
                if (page.getRotation() == 180 || page.getRotation() == 270)
                {
                    PDPage pageRIGHT = outdoc.importPage(page);
                    pageRIGHT.setCropBox(cropBoxRIGHT);
                    PDPage pageLEFT = outdoc.importPage(page);
                    pageLEFT.setCropBox(cropBoxLEFT);
                }
                else
                {
                    PDPage pageLEFT = outdoc.importPage(page);
                    pageLEFT.setCropBox(cropBoxLEFT);
                    PDPage pageRIGHT = outdoc.importPage(page);
                    pageRIGHT.setCropBox(cropBoxRIGHT);
                }
            }
            outdoc.save(args[1]);
            // closing must be after saving the destination document
        }
    }
    
    private static void usage()
    {
        System.err.println("Usage: java " + SplitBooklet.class.getName() + " <input-pdf> <output-pdf>");
    }
    
}
