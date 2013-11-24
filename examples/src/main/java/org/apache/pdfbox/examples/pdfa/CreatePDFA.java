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
package org.apache.pdfbox.examples.pdfa;

import java.io.InputStream;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;

/**
 * This is an example that creates a simple PDF/A document.
 *
 */
public class CreatePDFA
{
    /**
     * Constructor.
     */
    public CreatePDFA()
    {
        super();
    }

    /**
     * Create a simple PDF/A document.
     * 
     * This example is based on HelloWorld example.
     * 
     * As it is a simple case, to conform the PDF/A norm, are added :
     * - the font used in the document
     * - a light xmp block with only PDF identification schema (the only mandatory)
     * - an output intent
     *
     * @param file The file to write the PDF to.
     * @param message The message to write in the file.
     *
     * @throws Exception If something bad occurs
     */
    public void doIt( String file, String message) throws Exception
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            PDPage page = new PDPage();
            doc.addPage( page );

            // load the font from pdfbox.jar
            InputStream fontStream = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);
            
            // create a page with the message where needed
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.setFont( font, 12 );
            contentStream.moveTextPositionByAmount( 100, 700 );
            contentStream.drawString( message );
            contentStream.endText();
            contentStream.saveGraphicsState();
            contentStream.close();
            
            PDDocumentCatalog cat = doc.getDocumentCatalog();
            PDMetadata metadata = new PDMetadata(doc);
            cat.setMetadata(metadata);

            // jempbox version
            XMPMetadata xmp = new XMPMetadata();
            XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
            xmp.addSchema(pdfaid);
            pdfaid.setConformance("B");
            pdfaid.setPart(1);
            pdfaid.setAbout("");
            metadata.importXMPMetadata(xmp);
    
            InputStream colorProfile = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
            // create output intent
            PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
            oi.setInfo("sRGB IEC61966-2.1"); 
            oi.setOutputCondition("sRGB IEC61966-2.1"); 
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
            oi.setRegistryName("http://www.color.org"); 
            cat.addOutputIntent(oi);
            
            doc.save( file );
           
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    /**
     * This will create a hello world PDF/A document.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        CreatePDFA app = new CreatePDFA();
        try
        {
            if( args.length != 2 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1] );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <output-file> <Message>" );
    }
}
