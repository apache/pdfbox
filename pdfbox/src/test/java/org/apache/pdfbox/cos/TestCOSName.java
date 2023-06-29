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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TestCOSName
{
    /**
     * PDFBOX-4076: Check that characters outside of US_ASCII are not replaced with "?".
     *
     * @throws IOException
     */
    @Test
    void PDFBox4076() throws IOException
    {
        String special = "中国你好!";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage();
            document.addPage(page);
            document.getDocumentCatalog().getCOSObject().setString(COSName.getPDFName(special), special);

            document.save(baos);
        }
        try (PDDocument document = Loader.loadPDF(baos.toByteArray()))
        {
            COSDictionary catalogDict = document.getDocumentCatalog().getCOSObject();
            assertTrue(catalogDict.containsKey(special));
            assertEquals(special, catalogDict.getString(special));
        }
    }

    /**
     * Verify the parameters with which OutputStream.write(int) is invoked
     * when calling writePDF on COSName.TYPE
     *
     * @throws IOException
     */
    @Test
    public void testBytesWrittenToOutputStreamForCOSNameType() throws IOException
    {
        // Arrange
        COSName cosNameType = COSName.TYPE;
        OutputStream mockOutputStream = Mockito.mock(OutputStream.class);

        // Act
        cosNameType.writePDF(mockOutputStream);

        // Assert
        verify(mockOutputStream, atLeastOnce()).write(47);
        verify(mockOutputStream, atLeastOnce()).write(84);
        verify(mockOutputStream, atLeastOnce()).write(121);
        verify(mockOutputStream, atLeastOnce()).write(112);
        verify(mockOutputStream, atLeastOnce()).write(101);
    }

    /**
     * Verify the number of times OutputStream.write(int) is invoked
     * when calling writePDF on COSName.TYPE
     *
     * @throws IOException
     */
    @Test
    public void testNumberOfBytesWrittenToOutputStreamForCOSNameType() throws IOException
    {
        // Arrange
        COSName cosNameType = COSName.TYPE;
        OutputStream mockOutputStream = Mockito.mock(OutputStream.class);

        // Act
        cosNameType.writePDF(mockOutputStream);

        // Assert
        verify(mockOutputStream, times(5)).write(anyInt());
    }

}
