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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;

/**
 * This is an example on how to extract metadata from a PDF document.
 * 
 * Usage: java org.apache.pdfbox.examples.pdmodel.ExtractDocument &lt;input-pdf&gt;
 *
 */
public class ExtractMetadata
{
    private ExtractMetadata()
    {
        // utility class
    }

    /**
     * This is the main method.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            usage();
            System.exit(1);
        }
        else
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load(new File(args[0]));
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata meta = catalog.getMetadata();
                if (meta != null)
                {
                    DomXmpParser xmpParser = new DomXmpParser();
                    try
                    {
                        XMPMetadata metadata = xmpParser.parse(meta.createInputStream());

                        DublinCoreSchema dc = metadata.getDublinCoreSchema();
                        if (dc != null)
                        {
                            display("Title:", dc.getTitle());
                            display("Description:", dc.getDescription());
                            listString("Creators: ", dc.getCreators());
                            listCalendar("Dates:", dc.getDates());
                            listString("Subjects:", dc.getSubjects());
                        }

                        AdobePDFSchema pdf = metadata.getAdobePDFSchema();
                        if (pdf != null)
                        {
                            display("Keywords:", pdf.getKeywords());
                            display("PDF Version:", pdf.getPDFVersion());
                            display("PDF Producer:", pdf.getProducer());
                        }

                        XMPBasicSchema basic = metadata.getXMPBasicSchema();
                        if (basic != null)
                        {
                            display("Create Date:", basic.getCreateDate());
                            display("Modify Date:", basic.getModifyDate());
                            display("Creator Tool:", basic.getCreatorTool());
                        }
                    }
                    catch (XmpParsingException e)
                    {
                        System.err.println("An error ouccred when parsing the meta data: "
                                + e.getMessage());
                    }
                }
                else
                {
                    // The pdf doesn't contain any metadata, try to use the
                    // document information instead
                    PDDocumentInformation information = document.getDocumentInformation();
                    if (information != null)
                    {
                        showDocumentInformation(information);
                    }
                }

            }
            finally
            {
                if (document != null)
                {
                    document.close();
                }
            }
        }
    }

    private static void showDocumentInformation(PDDocumentInformation information)
    {
        display("Title:", information.getTitle());
        display("Subject:", information.getSubject());
        display("Author:", information.getAuthor());
        display("Creator:", information.getCreator());
        display("Producer:", information.getProducer());
    }

    private static void listString(String title, List<String> list)
    {
        if (list == null)
        {
            return;
        }
        System.out.println(title);
        Iterator<String> iter = list.iterator();
        while (iter.hasNext())
        {
            String string = iter.next();
            System.out.println("  " + string);
        }
    }

    private static void listCalendar(String title, List<Calendar> list)
    {
        if (list == null)
        {
            return;
        }
        System.out.println(title);
        Iterator<Calendar> iter = list.iterator();
        while (iter.hasNext())
        {
            Calendar calendar = iter.next();
            System.out.println("  " + format(calendar));
        }
    }

    private static String format(Object o)
    {
        if (o instanceof Calendar)
        {
            Calendar cal = (Calendar) o;
            return DateFormat.getDateInstance().format(cal.getTime());
        }
        else
        {
            return o.toString();
        }
    }

    private static void display(String title, Object value)
    {
        if (value != null)
        {
            System.out.println(title + " " + format(value));
        }
    }

    /**
     * This will print the usage for this program.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + ExtractMetadata.class.getName() + " <input-pdf>");
    }
}
