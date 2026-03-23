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
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.Assert;
import org.junit.Test;

public class TestCOSName
{
    private static final File TARGETPDFDIR = new File("target/pdfs");

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

    /**
     * PDFBOX-6178: Ensure that names with escape sequences #xx are written as is.
     * 
     * @throws IOException 
     */
    @Test
    public void PDFBox6178() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument document = PDDocument.load(new File(TARGETPDFDIR,"PDFBOX-6178.pdf"));
        PDField field = document.getDocumentCatalog()
            .getAcroForm(null)
            .getField("Geschlecht");

        field.setValue("männlich");

        COSDictionary dict = (COSDictionary) field.getWidgets()
                .get(0).getAppearance().getNormalAppearance().getCOSObject();
        for (COSName k : dict.keySet())
        {
            try
            {
                k.writePDF(baos);
            }
            catch (IOException e)
            {
                // ignored
            }
        }
        String writtenKeys = new String(baos.toByteArray(), "UTF-8");
        Assert.assertTrue("Output should be /m#e4nnlich (with 0xE4 as hex escape)", writtenKeys.contains("/m#E4nnlich"));
        document.close();
    }

    /**
     * PDFBOX-6178: Ensure that names with escape sequences #xx are written as is.
     * 
     * @throws IOException 
     */
    @Test
    public void NameWithASCII_NUL() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument document = PDDocument.load(new File(TARGETPDFDIR,"PDFBOX-6178-1.pdf"));
        PDField field = document.getDocumentCatalog()
            .getAcroForm(null)
            .getField("Geschlecht");

        COSDictionary dict = (COSDictionary) field.getWidgets()
            .get(0).getAppearance().getNormalAppearance().getCOSObject();
        for (COSName k : dict.keySet())
        {
            try
            {
                k.writePDF(baos);
            }
            catch (IOException e)
            {
                // ignored
            }
        }
        String writtenKeys = new String(baos.toByteArray(), "UTF-8");
        Assert.assertTrue("Output should be /m#00nnlich (with 0xE4 as hex escape)", writtenKeys.contains("/m#00nnlich"));
        document.close();
    }
}
