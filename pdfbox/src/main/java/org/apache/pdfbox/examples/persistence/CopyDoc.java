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
package org.apache.pdfbox.examples.persistence;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;



import org.apache.pdfbox.pdfparser.PDFParser;

import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.exceptions.COSVisitorException;

/**
 * This is an example used to copy a documents contents from a source doc to destination doc
 * via an in-memory document representation.
 *
 * @author Michael Traut
 * @version $Revision: 1.7 $
 */
public class CopyDoc
{
    /**
     * Constructor.
     */
    public CopyDoc()
    {
        super();
    }

    /**
     * This will perform the document copy.
     *
     * @param in The filename used for input.
     * @param out The filename used for output.
     *
     * @throws IOException If there is an error parsing the document.
     * @throws COSVisitorException If there is an error while copying the document.
     */
    public void doIt(String in, String out) throws IOException, COSVisitorException
    {
        java.io.InputStream is = null;
        java.io.OutputStream os = null;
        COSWriter writer = null;
        try
        {
            is = new java.io.FileInputStream(in);
            PDFParser parser = new PDFParser(is);
            parser.parse();

            COSDocument doc = parser.getDocument();

            os = new java.io.FileOutputStream(out);
            writer = new COSWriter(os);

            writer.write(doc);

        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
            if( os != null )
            {
                os.close();
            }
            if( writer != null )
            {
                writer.close();
            }
        }
    }

    /**
     * This will copy a PDF document.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        CopyDoc app = new CopyDoc();
        try
        {
            if( args.length != 2 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1]);
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
        System.err.println( "usage: " + this.getClass().getName() + " <input-file> <output-file>" );
    }
}
