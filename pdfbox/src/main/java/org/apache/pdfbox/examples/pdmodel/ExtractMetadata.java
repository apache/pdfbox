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

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * This is an example on how to extract metadata from a PDF document.
 * <p>
 * Usage: java org.apache.pdfbox.examples.pdmodel.ExtractDocument &lt;input-pdf&gt;
 *
 * @version $Revision$
 */
public class ExtractMetadata
{
    private ExtractMetadata()
    {
        //utility class
    }

    /**
     * This is the main method.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            usage();
            System.exit(1);
        }
        else
        {
            PDDocument document = null;

            try
            {
                document = PDDocument.load( args[0] );
                if (document.isEncrypted()) 
                {
                    try
                    {
                        document.decrypt("");
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: The document is encrypted." );
                    }
                    catch( org.apache.pdfbox.exceptions.CryptographyException e )
                    {
                        e.printStackTrace();
                    }
                }
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata meta = catalog.getMetadata();
                if ( meta != null)
                {
                    XMPMetadata metadata = meta.exportXMPMetadata();
    
                    XMPSchemaDublinCore dc = metadata.getDublinCoreSchema();
                    if (dc != null)
                    {
                        display("Title:", dc.getTitle());
                        display("Description:", dc.getDescription());
                        list("Creators: ", dc.getCreators());
                        list("Dates:", dc.getDates());
                    }
    
                    XMPSchemaPDF pdf = metadata.getPDFSchema();
                    if (pdf != null)
                    {
                        display("Keywords:", pdf.getKeywords());
                        display("PDF Version:", pdf.getPDFVersion());
                        display("PDF Producer:", pdf.getProducer());
                    }
    
                    XMPSchemaBasic basic = metadata.getBasicSchema();
                    if (basic != null)
                    {
                        display("Create Date:", basic.getCreateDate());
                        display("Modify Date:", basic.getModifyDate());
                        display("Creator Tool:", basic.getCreatorTool());
                    }
                }
                else
                {
                    // The pdf doesn't contain any metadata, try to use the document information instead
                    PDDocumentInformation information = document.getDocumentInformation();
                    if ( information != null)
                    {
                        showDocumentInformation(information);
                    }
                }
                
            }
            finally
            {
                if( document != null )
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
    
    private static void list(String title, List list)
    {
        if (list == null)
        {
            return;
        }
        System.out.println(title);
        Iterator iter = list.iterator();
        while (iter.hasNext())
        {
            Object o = iter.next();
            System.out.println("  " + format(o));
        }
    }

    private static String format(Object o)
    {
        if (o instanceof Calendar)
        {
            Calendar cal = (Calendar)o;
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
        System.err.println( "Usage: java " + ExtractMetadata.class.getName() + " <input-pdf>" );
    }
}
