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

import java.io.File;
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

    private static final String PASSWORD = "-password";
    private static final String NONSEQ = "-nonSeq";

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
     * 
     * @deprecated use {@link WriteDecodedDoc#doIt(String, String, String)} instead.
     */
    public void doIt(String in, String out) throws IOException, COSVisitorException
    {
        doIt(in, out, "", false);
    }
    
    /**
     * This will perform the document reading, decoding and writing.
     *
     * @param in The filename used for input.
     * @param out The filename used for output.
     * @param password The password to open the document.
     * @param useNonSeqParser use the non sequential parser
     *
     * @throws IOException If there is an error parsing the document.
     * @throws COSVisitorException If there is an error while copying the document.
     */
    public void doIt(String in, String out, String password, boolean useNonSeqParser) 
    throws IOException, COSVisitorException
    {
        PDDocument doc = null;
        try
        {
            if (useNonSeqParser) 
            {
                doc = PDDocument.loadNonSeq(new File(in), null, password);
                doc.setAllSecurityToBeRemoved(true);
            }
            else
            {
                doc = PDDocument.load( in );
                if( doc.isEncrypted() )
                {
                    try
                    {
                        doc.decrypt( password );
                        doc.setAllSecurityToBeRemoved(true);
                    }
                    catch( InvalidPasswordException e )
                    {
                        if (password.trim().length() == 0)
                        {
                            System.err.println( "Password needed!!" );
                        }
                        else
                        {
                            System.err.println( "Wrong password!!" );
                        }
                        return;
                    }
                    catch( org.apache.pdfbox.exceptions.CryptographyException e )
                    {
                        e.printStackTrace();
                        return;
                    }
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
        String password = "";
        boolean useNonSeqParser = false;
        String pdfFile = null;
        String outputFile = null;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else
                if( args[i].equals( NONSEQ ) )
                {
                    useNonSeqParser = true;
                }
                else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }
        if( pdfFile == null )
        {
            usage();
        }
        else
        {
            try
            {
                if (outputFile == null)
                {
                    outputFile = calculateOutputFilename(pdfFile);
                }
                app.doIt(pdfFile, outputFile, password, useNonSeqParser);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static String calculateOutputFilename(String filename) 
    {
        String outputFilename;
        if (filename.toLowerCase().endsWith(".pdf"))
        {
            outputFilename = filename.substring(0,filename.length()-4);
        }
        else
        {
            outputFilename = filename;
        }
        outputFilename += "_unc.pdf";
        return outputFilename;
    }
    
    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println(
                "usage: java -jar pdfbox-app-x.y.z.jar WriteDecodedDoc [OPTIONS] <input-file> [output-file]\n" +
                "  -password <password>      Password to decrypt the document\n" +
                "  -nonSeq                   Enables the new non-sequential parser\n" +
                "  <input-file>              The PDF document to be decompressed\n" +
                "  [output-file]             The filename for the decompressed pdf\n"
                );
    }
}
