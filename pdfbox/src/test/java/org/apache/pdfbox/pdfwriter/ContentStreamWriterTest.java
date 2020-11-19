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

package org.apache.pdfbox.pdfwriter;

import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class ContentStreamWriterTest
{
    private final File testDirIn = new File("target/test-output/contentstream/in");
    private final File testDirOut = new File("target/test-output/contentstream/out");
    
    public ContentStreamWriterTest()
    {
        testDirIn.mkdirs();
        testDirOut.mkdirs();
    }
    
    @BeforeAll
    public static void setUpClass()
    {
    }
    
    @AfterAll
    public static void tearDownClass()
    {
    }
    
    @BeforeEach
    public void setUp()
    {
    }
    
    @AfterEach
    public void tearDown()
    {
    }

    /**
     * Test parse content stream, write back tokens and compare rendering.
     *
     * @throws java.io.IOException
     */
    @Test
    void testPDFBox4750() throws IOException
    {
        String filename = "PDFBOX-4750.pdf";
        File file = new File("target/pdfs", filename);
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer r = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); ++i)
            {
                BufferedImage bim1 = r.renderImageWithDPI(i, 96);
                ImageIO.write(bim1, "png", new File(testDirIn, filename + "-" + (i + 1) + ".png"));
                PDPage page = doc.getPage(i);
                PDStream newContent = new PDStream(doc);
                try (OutputStream os = newContent.createOutputStream(COSName.FLATE_DECODE))
                {
                    PDFStreamParser parser = new PDFStreamParser(page);
                    ContentStreamWriter tokenWriter = new ContentStreamWriter(os);
                    tokenWriter.writeTokens(parser.parse());
                }
                page.setContents(newContent);
            }
            doc.save(new File(testDirIn, filename));
        }
        TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
        if (!testPDFToImage.doTestFile(new File(testDirIn, filename), testDirIn.getAbsolutePath(), testDirOut.getAbsolutePath()))
        {
            fail("Rendering failed or is not identical, see in " + testDirOut);
        }
    }
}
