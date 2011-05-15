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
package org.apache.pdfbox;

import java.io.IOException;

import java.util.Iterator;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.exceptions.COSVisitorException;

import org.apache.pdfbox.exceptions.InvalidPasswordException;

/**
 * load document and write with all streams decoded.
 *
 * @author Michael Traut
 * @version $Revision: 1.8 $
 */
public class WriteDecodedDoc
{

    /**
     * Constructor.
     */
    public WriteDecodedDoc()
    {
        super();
    }

    /**
     * This will perform the document reading, decoding and writing.
     *
     * @param in The filename used for input.
     * @param out The filename used for output.
     *
     * @throws IOException If there is an error parsing the document.
     * @throws COSVisitorException If there is an error while copying the document.
     */
    public void doIt(String in, String out) throws IOException, COSVisitorException
    {
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( in );
            if( doc.isEncrypted() )
            {
                try
                {
                    doc.decrypt( "" );
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

            for (Iterator<COSObject> i = doc.getDocument().getObjects().iterator(); i.hasNext();)
            {
                COSBase base = ((COSObject) i.next()).getObject();
                if (base instanceof COSStream)
                {
                    // just kill the filters
                    COSStream cosStream = (COSStream)base;
                    cosStream.getUnfilteredStream();
                    cosStream.setFilters(null);
                }
            }
            doc.save( out );
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
     * This will write a PDF document with completely decoded streams.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        WriteDecodedDoc app = new WriteDecodedDoc();
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
        System.err.println( "usage: java -jar pdfbox-app-x.y.z.jar WriteDecodedDoc <input-file> <output-file>" );
    }
}
