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

package org.apache.pdfbox.text;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PDFTextStripperByAreaTest
{
    @Test
    void testSomeMethod() throws IOException
    {
        File pdfFile = new File("src/test/resources/input","eu-001.pdf");
        try (PDDocument doc = Loader.loadPDF(pdfFile))
        {
            String regionName = "region";
            PDFTextStripperByArea textAreaStripper = new PDFTextStripperByArea();
            textAreaStripper.setShouldSeparateByBeads(false); // does nothing
            textAreaStripper.setSortByPosition(true);
            Rectangle2D rect = new Rectangle2D.Double(65, 227, 472, 34);
            textAreaStripper.addRegion(regionName, rect);
            textAreaStripper.setLineSeparator("");
            textAreaStripper.extractRegions(doc.getPage(0));
            String textForRegion = textAreaStripper.getTextForRegion(regionName);
            textForRegion = textForRegion.trim();
            Assertions.assertEquals("In the following tables you will find the 91 E-PRTR "
                    + "pollutants and their thresholds broken down by the 7 groups used in all "
                    + "the searches of the E-PRTR website.", textForRegion);
            textAreaStripper.removeRegion(regionName);
            rect = new Rectangle2D.Double(230, 370, 369, 10);
            textAreaStripper.addRegion(regionName, rect);
            textAreaStripper.extractRegions(doc.getPage(2));
            textForRegion = textAreaStripper.getTextForRegion(regionName);
            textForRegion = textForRegion.trim();
            Assertions.assertEquals("Inorganic substances", textForRegion);
            Assertions.assertEquals(1, textAreaStripper.getRegions().size());
        }
    }
    
}
