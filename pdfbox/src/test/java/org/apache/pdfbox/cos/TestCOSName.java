/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Assert;
import org.junit.Test;

public class TestCOSName
{
    /**
     * PDFBOX-4076: Check that characters outside of US_ASCII are not replaced with "?".
     * 
     * @throws IOException 
     */
    @Test
    public void PDFBox4076() throws IOException
    {
        String special = "中国你好!";
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        document.getDocumentCatalog().getCOSObject().setString(COSName.getPDFName(special), special);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        document = PDDocument.load(baos.toByteArray());
        COSDictionary catalogDict = document.getDocumentCatalog().getCOSObject();
        Assert.assertTrue(catalogDict.containsKey(special));
        Assert.assertEquals(special, catalogDict.getString(special));
        document.close();
    }
}
