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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

public class PDFParser extends COSParser
{
    private static final Log LOG = LogFactory.getLog(PDFParser.class);

    /**
     * Constructor.
     * Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param source source representing the pdf.
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source) throws IOException
    {
        this(source, "");
    }

    /**
     * Constructor.
     * Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param source input representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, String decryptionPassword) throws IOException
    {
        this(source, decryptionPassword, null, null);
    }

    /**
     * Constructor.
     * Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param source input representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     *
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore,
            String alias) throws IOException
    {
        this(source, decryptionPassword, keyStore, alias, null);
    }

    /**
     * Constructor.
     * 
     * @param source input representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param streamCacheCreateFunction a function to create an instance of the stream cache
     *
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore,
            String alias, StreamCacheCreateFunction streamCacheCreateFunction) throws IOException
    {
        super(source, decryptionPassword, keyStore, alias, streamCacheCreateFunction);
    }

    /**
     * The initial parse will first parse only the trailer, the xrefstart and all xref tables to have a pointer (offset)
     * to all the pdf's objects. It can handle linearized pdfs, which will have an xref at the end pointing to an xref
     * at the beginning of the file. Last the root object is parsed.
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If something went wrong.
     */
    protected void initialParse() throws IOException
    {
        COSDictionary trailer = retrieveTrailer();
    
        COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
        if (root == null)
        {
            throw new IOException("Missing root object specification in trailer.");
        }
        // in some pdfs the type value "Catalog" is missing in the root object
        if (isLenient() && !root.containsKey(COSName.TYPE))
        {
            root.setItem(COSName.TYPE, COSName.CATALOG);
        }
        // check pages dictionaries
        checkPages(root);
        document.setDecrypted();
        initialParseDone = true;
    }

    /**
     * This will parse the stream and populate the PDDocument object. This will close the keystore stream when it is
     * done parsing. Lenient mode is active by default.
     *
     * @return the populated PDDocument
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If there is an error reading from the stream or corrupt data is found.
     */
    public PDDocument parse() throws IOException
    {
        return parse(true);
    }

    /**
     * This will parse the stream and populate the PDDocument object. This will close the keystore stream when it is
     * done parsing.
     *
     * @param lenient activate leniency if set to true
     * @return the populated PDDocument
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If there is an error reading from the stream or corrupt data is found.
     */
    public PDDocument parse(boolean lenient) throws IOException
    {
        setLenient(lenient);
        // set to false if all is processed
        boolean exceptionOccurred = true;
        try
        {
            // PDFBOX-1922 read the version header and rewind
            if (!parsePDFHeader() && !parseFDFHeader())
            {
                if (lenient)
                {
                    LOG.warn("Error: Header doesn't contain versioninfo");
                }
                else
                {
                    throw new IOException("Error: Header doesn't contain versioninfo");
                }
            }
    
            if (!initialParseDone)
            {
                initialParse();
            }
            exceptionOccurred = false;
            PDDocument pdDocument = createDocument();
            pdDocument.setEncryptionDictionary(getEncryption());
            return pdDocument;
        }
        finally
        {
            if (exceptionOccurred && document != null)
            {
                IOUtils.closeQuietly(document);
                document = null;
            }
        }
    }

    /**
     * Create the resulting document. Maybe overwritten if the parser uses another class as document.
     * 
     * @return the resulting document
     * @throws IOException if the method is called before parsing the document
     */
    protected PDDocument createDocument() throws IOException
    {
        return new PDDocument(document, source, getAccessPermission());
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     * 
     * @deprecated use {@link Loader#loadPDF(File)} instead
     */
    @Deprecated
    public static PDDocument load(File file) throws IOException
    {
        return Loader.loadPDF(file);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     * 
     * @deprecated use {@link Loader#loadPDF(File, String)} instead
     */
    @Deprecated
    public static PDDocument load(File file, String password) throws IOException
    {
        return Loader.loadPDF(file, password);
    }

}
