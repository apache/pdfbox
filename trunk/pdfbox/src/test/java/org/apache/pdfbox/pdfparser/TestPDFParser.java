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

package org.apache.pdfbox.pdfparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.DateConverter;
import org.junit.jupiter.api.Test;

class TestPDFParser
{
    private static final File TARGETPDFDIR = new File("target/pdfs");

    @Test
    void testPDFParserMissingCatalog() throws URISyntaxException
    {
        // PDFBOX-3060
        try
        {
            Loader.loadPDF(new File(TestPDFParser.class.getResource("MissingCatalog.pdf").toURI()))
                .close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * Test whether /Info dictionary is retrieved correctly when rebuilding the trailer of a corrupt
     * file. An incorrect algorithm would result in an outline dictionary being mistaken for an
     * /Info.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox3208() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3208-L33MUTT2SVCWGCS6UIYL5TH3PNPXHIS6.pdf")))
        {
            PDDocumentInformation di = doc.getDocumentInformation();
            assertEquals("Liquent Enterprise Services", di.getAuthor());
            assertEquals("Liquent services server", di.getCreator());
            assertEquals("Amyuni PDF Converter version 4.0.0.9", di.getProducer());
            assertEquals("", di.getKeywords());
            assertEquals("", di.getSubject());
            assertEquals("892B77DE781B4E71A1BEFB81A51A5ABC_20140326022424.docx", di.getTitle());
            assertEquals(DateConverter.toCalendar("D:20140326142505-02'00'"), di.getCreationDate());
            assertEquals(DateConverter.toCalendar("20140326172513Z"), di.getModificationDate());
        }
    }

    /**
     * Test whether the /Info is retrieved correctly when rebuilding the trailer of a corrupt file,
     * despite the /Info dictionary not having a modification date.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox3940() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-3940-079977.pdf")))
        {
            PDDocumentInformation di = doc.getDocumentInformation();
            assertEquals("Unknown", di.getAuthor());
            assertEquals("C:REGULA~1IREGSFR_EQ_EM.WP", di.getCreator());
            assertEquals("Acrobat PDFWriter 3.02 for Windows", di.getProducer());
            assertEquals("", di.getKeywords());
            assertEquals("", di.getSubject());
            assertEquals("C:REGULA~1IREGSFR_EQ_EM.PDF", di.getTitle());
            assertEquals(DateConverter.toCalendar("Tuesday, July 28, 1998 4:00:09 PM"), di.getCreationDate());
        }
    }

    /**
     * PDFBOX-3783: test parsing of file with trash after %%EOF.
     */
    @Test
    void testPDFBox3783()
    {
        try
        {
            Loader.loadPDF(
                    new File(TARGETPDFDIR, "PDFBOX-3783-72GLBIGUC6LB46ELZFBARRJTLN4RBSQM.pdf"))
                    .close();
        }
        catch (Exception exception)
        {
            fail("Unexpected IOException");
        }

    }

    /**
     * PDFBOX-3785, PDFBOX-3957:
     * Test whether truncated file with several revisions has correct page count.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox3785() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-3785-202097.pdf")))
        {
            assertEquals(11, doc.getNumberOfPages());
        }
    }

    /**
     * PDFBOX-3947: test parsing of file with broken object stream.
     */
    @Test
    void testPDFBox3947()
    {
        try
        {
            Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-3947-670064.pdf")).close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * PDFBOX-3948: test parsing of file with object stream containing some unexpected newlines.
     */
    @Test
    void testPDFBox3948()
    {
        try
        {
            Loader.loadPDF(
                    new File(TARGETPDFDIR, "PDFBOX-3948-EUWO6SQS5TM4VGOMRD3FLXZHU35V2CP2.pdf"))
                    .close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * PDFBOX-3949: test parsing of file with incomplete object stream.
     */
    @Test
    void testPDFBox3949()
    {
        try
        {
            Loader.loadPDF(
                    new File(TARGETPDFDIR, "PDFBOX-3949-MKFYUGZWS3OPXLLVU2Z4LWCTVA5WNOGF.pdf"))
                    .close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * PDFBOX-3950: test parsing and rendering of truncated file with missing pages.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox3950() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3950-23EGDHXSBBYQLKYOKGZUOVYVNE675PRD.pdf")))
        {
            assertEquals(4, doc.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); ++i)
            {
                try
                {
                    renderer.renderImage(i);
                }
                catch (IOException ex)
                {
                    if (i == 3 && ex.getMessage().equals("Missing descendant font array"))
                    {
                        continue;
                    }
                    throw ex;
                }
            }
        }
    }

    /**
     * PDFBOX-3951: test parsing of truncated file.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox3951() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3951-FIHUZWDDL2VGPOE34N6YHWSIGSH5LVGZ.pdf")))
        {
            assertEquals(143, doc.getNumberOfPages());
        }
    }

    /**
     * PDFBOX-3964: test parsing of broken file.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox3964() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3964-c687766d68ac766be3f02aaec5e0d713_2.pdf")))
        {
            assertEquals(10, doc.getNumberOfPages());
        }
    }

    /**
     * Test whether /Info dictionary is retrieved correctly in brute force search for the
     * Info/Catalog dictionaries.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox3977() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3977-63NGFQRI44HQNPIPEJH5W2TBM6DJZWMI.pdf")))
        {
            PDDocumentInformation di = doc.getDocumentInformation();
            assertEquals("QuarkXPress(tm) 6.52", di.getCreator());
            assertEquals("Acrobat Distiller 7.0 pour Macintosh", di.getProducer());
            assertEquals("Fich sal Fabr corr1 (Page 6)", di.getTitle());
            assertEquals(DateConverter.toCalendar("D:20070608151915+02'00'"), di.getCreationDate());
            assertEquals(DateConverter.toCalendar("D:20080604152122+02'00'"), di.getModificationDate());
        }
    }

    /**
     * Test parsing the "genko_oc_shiryo1.pdf" file, which is susceptible to regression.
     */
    @Test
    void testParseGenko()
    {
        try
        {
            Loader.loadPDF(new File(TARGETPDFDIR, "genko_oc_shiryo1.pdf")).close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * Test parsing the file from PDFBOX-4338, which brought an
     * ArrayIndexOutOfBoundsException before the bug was fixed.
     */
    @Test
    void testPDFBox4338()
    {
        try
        {
            Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4338.pdf")).close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * Test parsing the file from PDFBOX-4339, which brought a
     * NullPointerException before the bug was fixed.
     */
    @Test
    void testPDFBox4339()
    {
        try
        {
            Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4339.pdf")).close();
        }
        catch (Exception exception)
        {
            fail("Unexpected Exception");
        }
    }

    /**
     * Test parsing the "WXMDXCYRWFDCMOSFQJ5OAJIAFXYRZ5OA.pdf" file, which is susceptible to
     * regression.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox4153() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4153-WXMDXCYRWFDCMOSFQJ5OAJIAFXYRZ5OA.pdf")))
        {
            PDDocumentOutline documentOutline = doc.getDocumentCatalog().getDocumentOutline();
            PDOutlineItem firstChild = documentOutline.getFirstChild();
            assertEquals("Main Menu", firstChild.getTitle());
        }
    }

    /**
     * Test that PDFBOX-4490 has 3 pages.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox4490() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4490.pdf")))
        {
            assertEquals(3, doc.getNumberOfPages());
        }
    }

}
