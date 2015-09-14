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
package org.apache.pdfbox.examples.pdmodel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

/**
 * Creates a simple PDF/A document.
 */
public final class CreatePDFA
{
    private CreatePDFA()
    {
    }
    
    public static void main(String[] args) throws IOException, TransformerException
    {
        if (args.length != 3)
        {
            System.err.println("usage: " + CreatePDFA.class.getName() +
                    " <output-file> <Message> <ttf-file>");
            System.exit(1);
        }

        String file = args[0];
        String message = args[1];
        String fontfile = args[2];

        PDDocument doc = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            // load the font as this needs to be embedded
            PDFont font = PDType0Font.load(doc, new File(fontfile));

            // create a page with the message
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(message);
            contents.endText();
            contents.saveGraphicsState();
            contents.close();

            // add XMP metadata
            XMPMetadata xmp = XMPMetadata.createXMPMetadata();
            
            try
            {
                DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
                dc.setTitle(file);
                
                PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
                id.setPart(1);
                id.setConformance("B");
                
                XmpSerializer serializer = new XmpSerializer();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                serializer.serialize(xmp, baos, true);

                PDMetadata metadata = new PDMetadata(doc);
                metadata.importXMPMetadata(baos.toByteArray());
                doc.getDocumentCatalog().setMetadata(metadata);
            }
            catch(BadFieldValueException e)
            {
                // won't happen here, as the provided value is valid
                throw new IllegalArgumentException(e);
            }

            // sRGB output intent
            InputStream colorProfile = CreatePDFA.class.getResourceAsStream(
                    "/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
            PDOutputIntent intent = new PDOutputIntent(doc, colorProfile);
            intent.setInfo("sRGB IEC61966-2.1");
            intent.setOutputCondition("sRGB IEC61966-2.1");
            intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
            intent.setRegistryName("http://www.color.org");
            doc.getDocumentCatalog().addOutputIntent(intent);

            doc.save(file);
        }
        finally
        {
            doc.close();
        }
    }
}
