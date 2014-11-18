/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestMetadataFiles
{
    @Test()
    public void validate() throws Exception
    {
        String testfileDirectory = "src/test/resources/org/apache/pdfbox/preflight/metadata/";

        File validFile = new File(testfileDirectory + "PDFAMetaDataValidationTestTrailingNul.pdf");
        assertTrue("Metadata test file " + validFile + " has to be valid ", checkPDF(validFile));

        File invalidFile1 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestTrailingSpaces.pdf");
        assertFalse("Metadata test file " + invalidFile1 + " has to be invalid ",
                checkPDF(invalidFile1));

        File invalidFile2 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestTrailingControlChar.pdf");
        assertFalse("Metadata test file " + invalidFile2 + " has to be invalid ",
                checkPDF(invalidFile2));

        File invalidFile3 = new File(testfileDirectory + "PDFAMetaDataValidationTestMiddleNul.pdf");
        assertFalse("Metadata test file " + invalidFile3 + " has to be invalid ",
                checkPDF(invalidFile3));

        File invalidFile4 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestMiddleControlChar.pdf");
        assertFalse("Metadata test file " + invalidFile4 + " has to be invalid ",
                checkPDF(invalidFile4));

    }

    private boolean checkPDF(File pdf)
    {
        PreflightDocument document = null;
        boolean testResult = false;
        try
        {
            InputStream input = new FileInputStream(pdf);
            ValidationResult result = null;
            try
            {
                PreflightParser parser = new PreflightParser(new ByteArrayDataSource(input));
                parser.parse();
                document = (PreflightDocument) parser.getPDDocument();
                document.validate();
                result = document.getResult();
            }
            catch (SyntaxValidationException e)
            {
                result = e.getResult();
            }
            catch (IOException e)
            {
                fail("An exception occured while parsing the PDF " + pdf + ": " + e);
            }
            if (result != null)
            {
                testResult = result.isValid();
            }
        }
        catch (FileNotFoundException e1)
        {
            fail("Can't find the given file " + pdf);
        }
        finally
        {
            if (document != null)
            {
                try
                {
                    document.close();
                }
                catch (IOException e)
                {
                    // shouldn't happen;
                }
            }
        }
        return testResult;
    }
}
