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
package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * load document and write with all streams decoded.
 *
 * @author Michael Traut
 */
public class WriteDecodedDoc
{

    private static final String PASSWORD = "-password";
    private static final String SKIPIMAGES = "-skipImages";

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
     * @param password The password to open the document.
     * @param skipImages Whether to skip decoding images.
     *
     * @throws IOException if the output could not be written
     */
    public void doIt(String in, String out, String password, boolean skipImages)
            throws IOException
    {
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(new File(in), password);
            doc.setAllSecurityToBeRemoved(true);
            for (COSObject cosObject : doc.getDocument().getObjects())
            {
                COSBase base = cosObject.getObject();
                if (base instanceof COSStream)
                {
                    COSStream stream = (COSStream) base;
                    if (skipImages &&
                        COSName.XOBJECT.equals(stream.getItem(COSName.TYPE)) && 
                        COSName.IMAGE.equals(stream.getItem(COSName.SUBTYPE)))
                    {
                        continue;
                    }
                    byte[] bytes;
                    try
                    {
                        bytes = new PDStream(stream).toByteArray();
                    }
                    catch (IOException ex)
                    {
                        System.err.println("skip " + 
                                cosObject.getObjectNumber() + " " + 
                                cosObject.getGenerationNumber() + " obj: " + 
                                ex.getMessage());
                        continue;
                    }
                    stream.removeItem(COSName.FILTER);
                    OutputStream streamOut = stream.createOutputStream();
                    streamOut.write(bytes);
                    streamOut.close();
                }
            }
            doc.getDocumentCatalog();
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
     * <br>
     * see usage() for commandline
     *
     * @param args command line arguments
     * @throws java.io.IOException if the output could not be written
     */
    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        WriteDecodedDoc app = new WriteDecodedDoc();
        String password = "";
        String pdfFile = null;
        String outputFile = null;
        boolean skipImages = false;
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
            else if (args[i].equals( SKIPIMAGES ))
            {
                skipImages = true;
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
            if (outputFile == null)
            {
                outputFile = calculateOutputFilename(pdfFile);
            }
            app.doIt(pdfFile, outputFile, password, skipImages);
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
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar WriteDecodedDoc [options] <inputfile> [outputfile]\n"
                + "\nOptions:\n"
                + "  -password <password> : Password to decrypt the document\n"
                + "  -skipImages          : Don't uncompress images\n"
                + "  <inputfile>          : The PDF document to be decompressed\n"
                + "  [outputfile]         : The filename for the decompressed pdf\n";
       
        System.err.println(message);
        System.exit(1);
    }
}
