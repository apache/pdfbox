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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the PDSignatureField class.
 *
 */
class PDSignatureFieldTest
{
    private PDDocument document;
    private PDAcroForm acroForm;

    @BeforeEach
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
    }

    @Test
    void createDefaultSignatureField()
    {
        PDSignatureField sigField = new PDSignatureField(acroForm);
        sigField.setPartialName("SignatureField");

        assertEquals(sigField.getFieldType(), sigField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Sig", sigField.getFieldType());
        
        assertEquals(COSName.ANNOT, sigField.getCOSObject().getItem(COSName.TYPE));
        assertEquals(PDAnnotationWidget.SUB_TYPE, sigField.getCOSObject().getNameAsString(COSName.SUBTYPE));

        // Add the field to the acroform
        List<PDField> fields = new ArrayList<>();
        fields.add(sigField);
        this.acroForm.setFields(fields);

        assertNotNull(acroForm.getField("SignatureField"));
    }
    
    @Test
    void setValueForAbstractedSignatureField()
    {
        PDSignatureField sigField = new PDSignatureField(acroForm);
        sigField.setPartialName("SignatureField");
        assertThrows(UnsupportedOperationException.class, () -> {
            sigField.setValue("Can't set value using String");
        });
    }
    
    /**
     * PDFBOX-4822: test get the signature contents.
     * 
     * @throws IOException 
     */
    @Test
    void testGetContents() throws IOException
    {
        // Normally, range0 + range1 = position of "<", and range2 = position after ">"
        PDSignature signature = new PDSignature();
        signature.setByteRange(new int[]{ 0, 10, 30, 10});
        byte[] by = "AAAAAAAAAA<313233343536373839>BBBBBBBBBB".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("123456789", new String(signature.getContents(by)));
        assertEquals("123456789", new String(signature.getContents(new ByteArrayInputStream(by))));
    }
}
