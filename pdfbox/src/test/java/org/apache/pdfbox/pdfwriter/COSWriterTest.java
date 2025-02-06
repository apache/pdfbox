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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.Test;

public class COSWriterTest
{
    /**
     * PDFBOX-4241: check whether the output stream is closed twice.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox4241() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        doc.save(new BufferedOutputStream(new ByteArrayOutputStream(1024)
        {
            private boolean open = true;

            @Override
            public void close() throws IOException
            {
                //Thread.dumpStack();

                open = false;
                super.close();
            }

            @Override
            public void flush() throws IOException
            {
                if (!open)
                {
                    throw new IOException("Stream already closed");
                }

                //Thread.dumpStack();
            }
        }));
        doc.close();
    }

    @Test
    public void testPDFBox5945() throws Exception
    {
        byte[] input = create();
        checkTrailerSize(input);

        byte[] output = edit(input);
        checkTrailerSize(output);
    }

    private static void checkTrailerSize(byte[] docData) throws IOException
    {
        try
        {
            PDDocument pdDocument = PDDocument.load(docData);
            COSDocument cosDocument = pdDocument.getDocument();
            long maxObjNumber = 0;
            for (COSObjectKey key : cosDocument.getXrefTable().keySet())
            {
                maxObjNumber = key.getNumber() > maxObjNumber ? key.getNumber() : maxObjNumber;
            }
            long sizeFromTrailer = cosDocument.getTrailer().getLong(COSName.SIZE);
            assertEquals(maxObjNumber + 1, sizeFromTrailer);
            pdDocument.close();
        }
        catch (IOException exception)
        {
            // rethrow
            throw exception;
        }
    }

    private static byte[] create() throws IOException
    {
        try
        {
            PDDocument pdDocument = new PDDocument();
            PDAcroForm acroForm = new PDAcroForm(pdDocument);
            pdDocument.getDocumentCatalog().setAcroForm(acroForm);
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), PDType1Font.HELVETICA);
            resources.put(COSName.getPDFName("ZaDb"), PDType1Font.SYMBOL);
            acroForm.setDefaultResources(resources);
            PDPage page = new PDPage(PDRectangle.A4);
            pdDocument.addPage(page);
            PDTextField textField = new PDTextField(acroForm);
            textField.setPartialName("textFieldName");
            acroForm.getFields().add(textField);
            PDAnnotationWidget widget = textField.getWidgets().get(0);
            widget.setPage(page);
            page.getAnnotations().add(widget);
            PDRectangle rectangle = new PDRectangle(10, 200, 200, 15);
            widget.setRectangle(rectangle);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdDocument.save(out);
            pdDocument.close();
            return out.toByteArray();
        }
        catch (IOException exception)
        {
            // rethrow
            throw exception;
        }
    }

    private static byte[] edit(byte[] input) throws IOException
    {
        try 
        {
            PDDocument pdDocument = PDDocument.load(input);
            PDTextField textField = (PDTextField) pdDocument.getDocumentCatalog().getAcroForm()
                    .getField("textFieldName");
            assertNotNull(textField);
            textField.setMultiline(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdDocument.saveIncremental(out);
            pdDocument.close();
            return out.toByteArray();
        }
        catch(IOException exception)
        {
            // rethrow
            throw exception;
        }
    }

}
