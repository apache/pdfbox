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
package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;

/**
 * Create a 3-page PDF with the page labels "RO III", "RO IV", "1".
 *
 * @author Tilman Hausherr
 */
public class CreatePageLabels
{
    /**
     * Constructor.
     */
    private CreatePageLabels()
    {
    }

    public static void main(String[] args) throws IOException
    {
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        PDPageLabels pageLabels = new PDPageLabels(doc);
        PDPageLabelRange pageLabelRange1 = new PDPageLabelRange();
        pageLabelRange1.setPrefix("RO ");
        pageLabelRange1.setStart(3);
        pageLabelRange1.setStyle(PDPageLabelRange.STYLE_ROMAN_UPPER);
        pageLabels.setLabelItem(0, pageLabelRange1);
        PDPageLabelRange pageLabelRange2 = new PDPageLabelRange();
        pageLabelRange2.setStart(1);
        pageLabelRange2.setStyle(PDPageLabelRange.STYLE_DECIMAL);
        pageLabels.setLabelItem(2, pageLabelRange2);
        doc.getDocumentCatalog().setPageLabels(pageLabels);
        doc.save("labels.pdf");
        doc.close();
    }
}
