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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

public class PDFParser extends COSParser
{
    private static final Log LOG = LogFactory.getLog(PDFParser.class);

    private String password = "";
    private InputStream keyStoreInputStream = null;
    private String keyAlias = null;

    private PDEncryption encryption = null;    
    private AccessPermission accessPermission;

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
        super(source);
        fileLen = source.length();
        password = decryptionPassword;
        keyStoreInputStream = keyStore;
        keyAlias = alias;
        init(scratchFile);
    }
    
    private void init(ScratchFile scratchFile) throws IOException
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
        document = new COSDocument(scratchFile);
    }
    
    /**
     * This will get the PD document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public PDDocument getPDDocument() throws IOException
    {
        PDDocument doc = new PDDocument(getDocument(), source, accessPermission);
        doc.setEncryptionDictionary(encryption);
        return doc;
    }

    /**
     * The initial parse will first parse only the trailer, the xrefstart and all xref tables to have a pointer (offset)
     * to all the pdf's objects. It can handle linearized pdfs, which will have an xref at the end pointing to an xref
     * at the beginning of the file. Last the root object is parsed.
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If something went wrong.
     */
    protected void initialParse() throws InvalidPasswordException, IOException
    {
        COSDictionary trailer = null;
        // parse startxref
        long startXRefOffset = getStartxrefOffset();
        boolean rebuildTrailer = false;
        if (startXRefOffset > -1)
        {
            try
            {
                trailer = parseXref(startXRefOffset);
            }
            catch (IOException exception)
            {
                if (isLenient())
                {
                    rebuildTrailer = true;
                }
                else
                {
                    throw exception;
                }
            }
        }
        else if (isLenient())
        {
            rebuildTrailer = true;
        }
        if (rebuildTrailer)
        {
            trailer = rebuildTrailer();
        }
        // prepare decryption if necessary
        prepareDecryption();
    
        COSBase base = parseTrailerValuesDynamically(trailer);
        if (!(base instanceof COSDictionary))
        {
            throw new IOException("Expected root dictionary, but got this: " + base);
        }
        COSDictionary root = (COSDictionary) base;
        // in some pdfs the type value "Catalog" is missing in the root object
        if (isLenient() && !root.containsKey(COSName.TYPE))
        {
            root.setItem(COSName.TYPE, COSName.CATALOG);
        }
        COSObject catalogObj = document.getCatalog();
        if (catalogObj != null && catalogObj.getObject() instanceof COSDictionary)
        {
            parseDictObjects((COSDictionary) catalogObj.getObject(), (COSName[]) null);
            
            COSBase infoBase = trailer.getDictionaryObject(COSName.INFO);
            if (infoBase instanceof COSDictionary)
            {
                parseDictObjects((COSDictionary) infoBase, (COSName[]) null);
            }
            
            document.setDecrypted();
        }
        initialParseDone = true;
    }

    /**
     * This will parse the stream and populate the COSDocument object.  This will close
     * the keystore stream when it is done parsing.
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If there is an error reading from the stream or corrupt data
     * is found.
     */
    public void parse() throws InvalidPasswordException, IOException
    {
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
        }
        finally
        {
            IOUtils.closeQuietly(keyStoreInputStream);
    
            if (exceptionOccurred && document != null)
            {
                IOUtils.closeQuietly(document);
                document = null;
            }
        }
    }

    /**
     * Prepare for decryption.
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException if something went wrong
     */
    private void prepareDecryption() throws InvalidPasswordException, IOException
    {
        COSBase trailerEncryptItem = document.getTrailer().getItem(COSName.ENCRYPT);
        if (trailerEncryptItem != null && !(trailerEncryptItem instanceof COSNull))
        {
            if (trailerEncryptItem instanceof COSObject)
            {
                COSObject trailerEncryptObj = (COSObject) trailerEncryptItem;
                parseDictionaryRecursive(trailerEncryptObj);
            }
            try
            {
                encryption = new PDEncryption(document.getEncryptionDictionary());
                DecryptionMaterial decryptionMaterial;
                if (keyStoreInputStream != null)
                {
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(keyStoreInputStream, password.toCharArray());
    
                    decryptionMaterial = new PublicKeyDecryptionMaterial(ks, keyAlias, password);
                }
                else
                {
                    decryptionMaterial = new StandardDecryptionMaterial(password);
                }
    
                securityHandler = encryption.getSecurityHandler();
                securityHandler.prepareForDecryption(encryption, document.getDocumentID(),
                        decryptionMaterial);
                accessPermission = securityHandler.getCurrentAccessPermission();
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new IOException("Error (" + e.getClass().getSimpleName()
                        + ") while creating security handler for decryption", e);
            }
        }
    }

    /**
     * Resolves all not already parsed objects of a dictionary recursively.
     * 
     * @param dictionaryObject dictionary to be parsed
     * @throws IOException if something went wrong
     * 
     */
    private void parseDictionaryRecursive(COSObject dictionaryObject) throws IOException
    {
        parseObjectDynamically(dictionaryObject, true);
        COSDictionary dictionary = (COSDictionary)dictionaryObject.getObject();
        for(COSBase value : dictionary.getValues())
        {
            if (value instanceof COSObject)
            {
                COSObject object = (COSObject)value;
                if (object.getObject() == null)
                {
                    parseDictionaryRecursive(object);
                }
            }
        }
    }

}
