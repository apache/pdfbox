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

import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

/**
 * This is an example that creates a simple document
 * with a ttf-font.
 *
 * @author <a href="mailto:m.g.n@gmx.de">Michael Niedermair</a>
 * @version $Revision: 1.2 $
 */
public class HelloWorldTTF
{

    /**
     * create the second sample document from the PDF file format specification.
     *
     * @param file      The file to write the PDF to.
     * @param message   The message to write in the file.
     * @param fontfile  The ttf-font file.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public void doIt(final String file, final String message,
            final String fontfile) throws IOException, COSVisitorException
    {

        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            PDPage page = new PDPage();
            doc.addPage(page);
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontfile);

            PDPageContentStream contentStream = new PDPageContentStream(doc,
                    page);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(100, 700);
            contentStream.drawString(message);
            contentStream.endText();
            contentStream.close();
            doc.save(file);
            System.out.println(file + " created!");
        }
        finally
        {
            if (doc != null)
            {
                doc.close();
            }
        }
    }

    /**
     * This will create a hello world PDF document
     * with a ttf-font.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {

        HelloWorldTTF app = new HelloWorldTTF();
        try
        {
            if (args.length != 3)
            {
                app.usage();
            }
            else
            {
                app.doIt(args[0], args[1], args[2]);
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
        System.err.println("usage: " + this.getClass().getName()
                + " <output-file> <Message> <ttf-file>");
    }
}
