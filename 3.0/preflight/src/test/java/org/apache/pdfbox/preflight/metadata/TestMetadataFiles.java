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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.Test;


class TestMetadataFiles
{
    @Test()
    void validate() throws Exception
    {
        String testfileDirectory = "src/test/resources/org/apache/pdfbox/preflight/metadata/";

        File validFile = new File(testfileDirectory + "PDFAMetaDataValidationTestTrailingNul.pdf");
        assertTrue(checkPDF(validFile), "Metadata test file " + validFile + " has to be valid ");

        File invalidFile1 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestTrailingSpaces.pdf");
        assertFalse(checkPDF(invalidFile1), "Metadata test file " + invalidFile1 + " has to be invalid ");

        File invalidFile2 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestTrailingControlChar.pdf");
        assertFalse(checkPDF(invalidFile2), "Metadata test file " + invalidFile2 + " has to be invalid ");

        File invalidFile3 = new File(testfileDirectory + "PDFAMetaDataValidationTestMiddleNul.pdf");
        assertFalse(checkPDF(invalidFile3), "Metadata test file " + invalidFile3 + " has to be invalid ");

        File invalidFile4 = new File(testfileDirectory
                + "PDFAMetaDataValidationTestMiddleControlChar.pdf");
        assertFalse(checkPDF(invalidFile4), "Metadata test file " + invalidFile4 + " has to be invalid ");
    }

    private boolean checkPDF(File pdf)
    {
        boolean testResult = false;
        if (pdf.exists())
        {
            ValidationResult result = null;
            try
            {
                result = PreflightParser.validate(pdf);
            }
            catch (IOException e)
            {
                fail("An exception occurred while parsing the PDF " + pdf + ": " + e);
            }
            if (result != null)
            {
                testResult = result.isValid();
            }
        }
        else
        {
            fail("Can't find the given file " + pdf);
        }
        return testResult;
    }
}
