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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test handling some special characters when setting a fields value.
 * 
 * Compare the results of setting the values using PDFBox with setting the values
 * via Acrobat using JavaScript and manual input.
 * 
 * The JavaScript used for acrobat is
 * 
 * <pre>
 * {@code
 * this.getField("acrobat-nul").value = "NUL\0NUL";
 * this.getField("acrobat-tab").value = "TAB\tTAB";
 * this.getField("acrobat-space").value = "SPACE SPACE";
 * this.getField("acrobat-cr").value = "CR\rCR";
 * this.getField("acrobat-lf").value = "LF\nLF";
 * this.getField("acrobat-crlf").value = "CRLF\r\nCRLF";
 * this.getField("acrobat-lfcr").value = "LFCR\n\rLFCR";
 * this.getField("acrobat-linebreak").value = "linebreak\u2028linebreak";
 * this.getField("acrobat-paragraphbreak").value = "paragraphbreak\u2029paragraphbreak";
 * }
 * </pre>
 * 
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-3461">https://issues.apache.org/jira/browse/PDFBOX-3461</a>
 * 
 */
public class ControlCharacterTest {
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "ControlCharacters.pdf";

    private PDDocument document;
    private PDAcroForm acroForm;

    @Before
    public void setUp() throws IOException
    {
        document = PDDocument.load(new File(IN_DIR, NAME_OF_PDF));
        acroForm = document.getDocumentCatalog().getAcroForm();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void characterNUL() throws IOException
    {
    	acroForm.getField("pdfbox-nul").setValue("NUL\0NUL");
    }

    @Test(expected=IllegalArgumentException.class)
    public void characterTAB() throws IOException
    {
    	acroForm.getField("pdfbox-tab").setValue("TAB\tTAB");
    }
    
    @Test
    public void characterSPACE() throws IOException
    {
    	acroForm.getField("pdfbox-space").setValue("SPACE SPACE");
    }

    @Test
    public void characterCR() throws IOException
    {
    	acroForm.getField("pdfbox-cr").setValue("CR\rCR");
    }

    @Test
    public void characterLF() throws IOException
    {
    	acroForm.getField("pdfbox-lf").setValue("LF\nLF");
    }
    
    @Test
    public void characterCRLF() throws IOException
    {
    	acroForm.getField("pdfbox-crlf").setValue("CRLF\r\nCRLF");
    }

    @Test
    public void characterLFCR() throws IOException
    {
    	acroForm.getField("pdfbox-lfcr").setValue("LFCR\r\nLFCR");
    }
    
    @Test
    public void characterUnicodeLinebreak() throws IOException
    {
    	acroForm.getField("pdfbox-linebreak").setValue("linebreak\u2028linebreak");
    	
    }
    
    @Test
    public void characterUnicodeParagraphbreak() throws IOException
    {
    	acroForm.getField("pdfbox-paragraphbreak").setValue("paragraphbreak\u2029paragraphbreak");
    	
    }
    
    @After
    public void tearDown() throws IOException
    {
        document.close();
    }
}
