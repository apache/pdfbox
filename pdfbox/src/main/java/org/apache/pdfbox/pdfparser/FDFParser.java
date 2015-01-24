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
package org.apache.pdfbox.pdfparser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

public class FDFParser extends COSParser
{
    private static final Log LOG = LogFactory.getLog(FDFParser.class);

    private final RandomAccessBufferedFileInputStream raStream;

    private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    private File tempPDFFile;

    /**
     * Constructs parser for given file using memory buffer.
     * 
     * @param filename the filename of the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public FDFParser(String filename) throws IOException
    {
        this(new File(filename));
    }

    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public FDFParser(File file) throws IOException
    {
        super(EMPTY_INPUT_STREAM);
        fileLen = file.length();
        raStream = new RandomAccessBufferedFileInputStream(file);
        init();
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @throws IOException If something went wrong.
     */
    public FDFParser(InputStream input) throws IOException
    {
        super(EMPTY_INPUT_STREAM);
        tempPDFFile = createTmpFile(input);
        fileLen = tempPDFFile.length();
        raStream = new RandomAccessBufferedFileInputStream(tempPDFFile);
        init();
    }

    private void init() throws IOException
    {
        String eofLookupRangeStr = System.getProperty(SYSPROP_EOFLOOKUPRANGE);
        if (eofLookupRangeStr != null)
        {
            try
            {
                setEOFLookupRange(Integer.parseInt(eofLookupRangeStr));
            }
            catch (NumberFormatException nfe)
            {
                LOG.warn("System property " + SYSPROP_EOFLOOKUPRANGE
                        + " does not contain an integer value, but: '" + eofLookupRangeStr + "'");
            }
        }
        document = new COSDocument(false);
        pdfSource = new PushBackInputStream(raStream, 4096);
    }

    /**
     * Create a temporary file with the input stream. If the creation succeed, the {@linkplain #isTmpPDFFile} is set to
     * true. This Temporary file will be deleted at end of the parse method
     * 
     * @param input
     * @return the temporary file
     * @throws IOException If something went wrong.
     */
    private File createTmpFile(InputStream input) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            File tmpFile = File.createTempFile(TMP_FILE_PREFIX, ".pdf");
            fos = new FileOutputStream(tmpFile);
            IOUtils.copy(input, fos);
            return tmpFile;
        }
        finally
        {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * The initial parse will first parse only the trailer, the xrefstart and all xref tables to have a pointer (offset)
     * to all the pdf's objects. It can handle linearized pdfs, which will have an xref at the end pointing to an xref
     * at the beginning of the file. Last the root object is parsed.
     * 
     * @throws IOException If something went wrong.
     */
    private void initialParse() throws IOException
    {
        COSDictionary trailer = null;
        // parse startxref
        long startXRefOffset = getStartxrefOffset();
        if (startXRefOffset > 0)
        {
            trailer = parseXref(startXRefOffset);
        }
        else
        {
            trailer = searchXref(startXRefOffset);
        }
    
        // PDFBOX-1557 - ensure that all COSObject are loaded in the trailer
        // PDFBOX-1606 - after securityHandler has been instantiated
        for (COSBase trailerEntry : trailer.getValues())
        {
            if (trailerEntry instanceof COSObject)
            {
                COSObject tmpObj = (COSObject) trailerEntry;
                parseObjectDynamically(tmpObj, false);
            }
        }
        // parse catalog or root object
        COSObject root = (COSObject)trailer.getItem(COSName.ROOT);
    
        if (root == null)
        {
            throw new IOException("Missing root object specification in trailer.");
        }
    
        COSBase rootObject = parseObjectDynamically(root, false);
    
        // resolve all objects
        // A FDF doesn't have a catalog, all FDF fields are within the root object
        if (rootObject instanceof COSDictionary)
        {
            parseDictObjects((COSDictionary) rootObject, (COSName[]) null);
        }
    
        // PDFBOX-1922: read the version again now that all objects have been resolved
        readVersionInTrailer(trailer);
        initialParseDone = true;
    }

    /**
     * This will parse the stream and populate the COSDocument object.  This will close
     * the stream when it is done parsing.
     *
     * @throws IOException If there is an error reading from the stream or corrupt data
     * is found.
     */
    public void parse() throws IOException
    {
         // set to false if all is processed
         boolean exceptionOccurred = true; 
         try
         {
            if (!parseFDFHeader())
            {
                throw new IOException( "Error: Header doesn't contain versioninfo" );
            }
            initialParse();
            exceptionOccurred = false;
        }
        finally
        {
            IOUtils.closeQuietly(pdfSource);
            deleteTempFile();
    
            if (exceptionOccurred && document != null)
            {
                try
                {
                    document.close();
                    document = null;
                }
                catch (IOException ioe)
                {
                }
            }
        }
    }

    /**
     * This will get the FDF document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public FDFDocument getFDFDocument() throws IOException
    {
        return new FDFDocument( getDocument() );
    }

    /**
     * Remove the temporary file. A temporary file is created if this class is instantiated with an InputStream
     */
    private void deleteTempFile()
    {
        if (tempPDFFile != null)
        {
            try
            {
                if (!tempPDFFile.delete())
                {
                    LOG.warn("Temporary file '" + tempPDFFile.getName() + "' can't be deleted");
                }
            }
            catch (SecurityException e)
            {
                LOG.warn("Temporary file '" + tempPDFFile.getName() + "' can't be deleted", e);
            }
        }
    }

}
