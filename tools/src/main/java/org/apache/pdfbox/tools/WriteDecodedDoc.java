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
import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * load document and write with all streams decoded.
 *
 * @author Michael Traut
 */
@Command(name = "writedecodeddoc", header = "Writes a PDF document with all streams decoded", versionProvider = Version.class, mixinStandardHelpOptions = true)
public class WriteDecodedDoc implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-password", description = "the password to decrypt the document", arity = "0..1", interactive = true)
    private String password;

    @Option(names = "-skipImages", description = "don't uncompress images")
    private boolean skipImages;

    @Parameters(paramLabel = "inputfile", index="0", description = "the PDF document to be decompressed")
    private File infile;

    @Parameters(paramLabel = "outputfile", arity = "0..1", description = "the PDF file to save to.")
    private File outfile;

    /**
     * Constructor.
     */
    public WriteDecodedDoc()
    {
        SYSERR = System.err;
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
        try (PDDocument doc = Loader.loadPDF(new File(in), password))
        {
            doc.setAllSecurityToBeRemoved(true);
            COSDocument cosDocument = doc.getDocument();
            cosDocument.getXrefTable().keySet().stream()
                    .forEach(o -> processObject(cosDocument.getObjectFromPool(o), skipImages));
            doc.getDocumentCatalog();
            doc.getDocument().setIsXRefStream(false);
            doc.save(out, CompressParameters.NO_COMPRESSION);
        }
    }

    private void processObject(COSObject cosObject, boolean skipImages)
    {
        COSBase base = cosObject.getObject();
        if (base instanceof COSStream)
        {
            COSStream stream = (COSStream) base;
            if (skipImages && COSName.XOBJECT.equals(stream.getItem(COSName.TYPE))
                    && COSName.IMAGE.equals(stream.getItem(COSName.SUBTYPE)))
            {
                return;
            }
            try
            {
                byte[] bytes = new PDStream(stream).toByteArray();
                stream.removeItem(COSName.FILTER);
                try (OutputStream streamOut = stream.createOutputStream())
                {
                    streamOut.write(bytes);
                }
            }
            catch (IOException ex)
            {
                SYSERR.println("skip " + cosObject.getKey() + " obj: " + ex.getMessage());
            }
        }

    }
    /**
     * This will write a PDF document with completely decoded streams.
     * <br>
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");
        
        int exitCode = new CommandLine(new WriteDecodedDoc()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        String outputFilename;

        if (outfile == null)
        {
            outputFilename = calculateOutputFilename(infile.getAbsolutePath());
        }
        else
        {
            outputFilename = outfile.getAbsolutePath();
        }

        try {
            doIt(infile.getAbsolutePath(), outputFilename, password, skipImages);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error writing decoded PDF [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
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
}
