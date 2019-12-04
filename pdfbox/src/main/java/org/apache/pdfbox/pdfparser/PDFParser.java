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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
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
        this(source, "", ScratchFile.getMainMemoryOnlyInstance());
    }

    /**
     * Constructor.
     * 
     * @param source input representing the pdf.
     * @param scratchFile use a {@link ScratchFile} for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, ScratchFile scratchFile) throws IOException
    {
        this(source, "", scratchFile);
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
        this(source, decryptionPassword, ScratchFile.getMainMemoryOnlyInstance());
    }

    /**
     * Constructor.
     * 
     * @param source input representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param scratchFile use a {@link ScratchFile} for temporary storage.
     *
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, String decryptionPassword, ScratchFile scratchFile)
            throws IOException
    {
        this(source, decryptionPassword, null, null, scratchFile);
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
        this(source, decryptionPassword, keyStore, alias, ScratchFile.getMainMemoryOnlyInstance());
    }

    /**
     * Constructor.
     * 
     * @param source input representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * @param scratchFile buffer handler for temporary storage; it will be closed on
     *        {@link COSDocument#close()}
     *
     * @throws IOException If something went wrong.
     */
    public PDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore,
                     String alias, ScratchFile scratchFile) throws IOException
    {
        super(source, decryptionPassword, keyStore, alias);
        fileLen = source.length();
        init(scratchFile);
    }
    
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(byte[] input) throws IOException
    {
        return load(input, "");
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(byte[] input, String password) throws IOException
    {
        return load(input, password, null, null);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(byte[] input, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return load(input, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering input stream and PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(byte[] input, String password, InputStream keyStore, String alias,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        RandomAccessRead source = new RandomAccessBuffer(input);
        PDFParser parser = new PDFParser(source, password, keyStore, alias, scratchFile);
        return parser.parse();
    }

    private void init(ScratchFile scratchFile)
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
        document = new COSDocument(scratchFile, this);
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
     */
    public static PDDocument load(File file) throws IOException
    {
        return load(file, "", MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, MemoryUsageSetting memUsageSetting) throws IOException
    {
        return load(file, "", null, null, memUsageSetting);
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
     */
    public static PDDocument load(File file, String password) throws IOException
    {
        return load(file, password, null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        return load(file, password, null, null, memUsageSetting);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return load(file, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password, InputStream keyStore, String alias,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        @SuppressWarnings({ "squid:S2095" }) // raFile not closed here, may be needed for signing
        RandomAccessBufferedFileInputStream raFile = new RandomAccessBufferedFileInputStream(file);
        try
        {
            return load(raFile, password, keyStore, alias, memUsageSetting);
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(raFile);
            throw ioe;
        }
    }

    private static PDDocument load(RandomAccessBufferedFileInputStream raFile, String password,
            InputStream keyStore, String alias, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        try
        {
            PDFParser parser = new PDFParser(raFile, password, keyStore, alias, scratchFile);
            return parser.parse();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(scratchFile);
            throw ioe;
        }
    }

    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     * 
     * @param input stream that contains the document. Don't forget to close it after loading.
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input) throws IOException
    {
        return load(input, "", null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either copied to main memory
     * or to a temporary file to enable random access to the pdf.
     * 
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param memUsageSetting defines how memory is used for buffering input stream and PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        return load(input, "", null, null, memUsageSetting);
    }

    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     *
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, String password) throws IOException
    {
        return load(input, password, null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, String password, InputStream keyStore,
            String alias) throws IOException
    {
        return load(input, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either copied to main memory
     * or to a temporary file to enable random access to the pdf.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param memUsageSetting defines how memory is used for buffering input stream and PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, String password,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        return load(input, password, null, null, memUsageSetting);
    }

    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either copied to memory or to
     * a temporary file to enable random access to the pdf.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering input stream and PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, String password, InputStream keyStore,
            String alias, MemoryUsageSetting memUsageSetting) throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        try
        {
            RandomAccessRead source = scratchFile.createBuffer(input);
            PDFParser parser = new PDFParser(source, password, keyStore, alias, scratchFile);
            return parser.parse();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(scratchFile);
            throw ioe;
        }
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
                throw new IOException( "Error: Header doesn't contain versioninfo" );
            }
    
            if (!initialParseDone)
            {
                initialParse();
            }
            exceptionOccurred = false;
            PDDocument pdDocument = new PDDocument(getDocument(), source, getAccessPermission());
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

}
