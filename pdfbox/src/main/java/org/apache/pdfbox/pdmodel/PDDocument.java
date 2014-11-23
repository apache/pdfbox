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
package org.apache.pdfbox.pdmodel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.BaseParser;
import org.apache.pdfbox.pdfparser.NonSequentialPDFParser;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandlerFactory;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * This is the in-memory representation of the PDF document.
 * The #close() method must be called once the document is no longer needed.
 * 
 * @author Ben Litchfield
 */
public class PDDocument implements Closeable
{
    private COSDocument document;

    // cached values
    private PDDocumentInformation documentInformation;
    private PDDocumentCatalog documentCatalog;

    // the encryption will be cached here. When the document is decrypted then
    // the COSDocument will not have an "Encrypt" dictionary anymore and this object must be used
    private PDEncryption encryption;

    // holds a flag which tells us if we should remove all security from this documents.
    private boolean allSecurityToBeRemoved;

    // keep tracking customized documentId for the trailer. If null, a new id will be generated
    // this ID doesn't represent the actual documentId from the trailer
    private Long documentId;

    // the PDF parser
    private BaseParser parser;

    // the File to read incremental data from
    private File incrementalFile;

    // the access permissions of the document
    private AccessPermission accessPermission;
    
    /**
     * Creates an empty PDF document.
     * You need to add at least one page for the document to be valid.
     */
    public PDDocument()
    {
        document = new COSDocument();

        // First we need a trailer
        COSDictionary trailer = new COSDictionary();
        document.setTrailer(trailer);

        // Next we need the root dictionary.
        COSDictionary rootDictionary = new COSDictionary();
        trailer.setItem(COSName.ROOT, rootDictionary);
        rootDictionary.setItem(COSName.TYPE, COSName.CATALOG);
        rootDictionary.setItem(COSName.VERSION, COSName.getPDFName("1.4"));

        // next we need the pages tree structure
        COSDictionary pages = new COSDictionary();
        rootDictionary.setItem(COSName.PAGES, pages);
        pages.setItem(COSName.TYPE, COSName.PAGES);
        COSArray kidsArray = new COSArray();
        pages.setItem(COSName.KIDS, kidsArray);
        pages.setItem(COSName.COUNT, COSInteger.ZERO);
    }

    /**
     * This will add a page to the document. This is a convenience method, that will add the page to the root of the
     * hierarchy and set the parent of the page to the root.
     * 
     * @param page The page to add to the document.
     */
    public void addPage(PDPage page)
    {
        getPages().add(page);
    }

    /**
     * Add a signature.
     * 
     * @param sigObject is the PDSignatureField model
     * @param signatureInterface is a interface which provides signing capabilities
     * @throws IOException if there is an error creating required fields
     */
    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface) throws IOException
    {
        addSignature(sigObject, signatureInterface, new SignatureOptions());
    }

    /**
     * This will add a signature to the document.
     * 
     * @param sigObject is the PDSignatureField model
     * @param signatureInterface is a interface which provides signing capabilities
     * @param options signature options
     * @throws IOException if there is an error creating required fields
     */
    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface,
                             SignatureOptions options) throws IOException
    {
        // Reserve content
        // We need to reserve some space for the signature. Some signatures including
        // big certificate chain and we need enough space to store it.
        int preferedSignatureSize = options.getPreferedSignatureSize();
        if (preferedSignatureSize > 0)
        {
            sigObject.setContents(new byte[preferedSignatureSize]);
        }
        else
        {
            sigObject.setContents(new byte[0x2500]);
        }

        // Reserve ByteRange
        sigObject.setByteRange(new int[] { 0, 1000000000, 1000000000, 1000000000 });

        getDocument().setSignatureInterface(signatureInterface);

        //
        // Create SignatureForm for signature
        // and appending it to the document
        //

        // Get the first page
        PDDocumentCatalog catalog = getDocumentCatalog();
        int pageCount = catalog.getPages().getCount();
        if (pageCount == 0)
        {
            throw new IllegalStateException("Cannot sign an empty document");
        }

        int startIndex = Math.max(Math.min(options.getPage(), 0), pageCount - 1);
        PDPage page = catalog.getPages().get(startIndex);

        // Get the AcroForm from the Root-Dictionary and append the annotation
        PDAcroForm acroForm = catalog.getAcroForm();
        catalog.getCOSObject().setNeedToBeUpdate(true);

        if (acroForm == null)
        {
            acroForm = new PDAcroForm(this);
            catalog.setAcroForm(acroForm);
        }
        else
        {
            acroForm.getCOSObject().setNeedToBeUpdate(true);
        }

        // For invisible signatures, the annotation has a rectangle array with values [ 0 0 0 0 ]. This annotation is
        // usually attached to the viewed page when the signature is created. Despite not having an appearance, the
        // annotation AP and N dictionaries may be present in some versions of Acrobat. If present, N references the
        // DSBlankXObj (blank) XObject.

        // Create Annotation / Field for signature
        List<PDAnnotation> annotations = page.getAnnotations();

        List<PDFieldTreeNode> fields = acroForm.getFields();
        PDSignatureField signatureField = null;
        if(fields == null) 
        {
            fields = new ArrayList<PDFieldTreeNode>();
            acroForm.setFields(fields);
        }
        for (PDFieldTreeNode pdField : fields)
        {
            if (pdField instanceof PDSignatureField)
            {
                PDSignature signature = ((PDSignatureField) pdField).getSignature();
                if (signature != null && signature.getDictionary().equals(sigObject.getDictionary()))
                {
                    signatureField = (PDSignatureField) pdField;
                }
            }
        }
        if (signatureField == null)
        {
            signatureField = new PDSignatureField(acroForm);
            signatureField.setSignature(sigObject); // append the signature object
            signatureField.getWidget().setPage(page); // backward linking
        }

        // Set the AcroForm Fields
        List<PDFieldTreeNode> acroFormFields = acroForm.getFields();
        acroForm.getDictionary().setDirect(true);
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);

        boolean checkFields = false;
        for (PDFieldTreeNode field : acroFormFields)
        {
            if (field instanceof PDSignatureField)
            {
                if (((PDSignatureField) field).getCOSObject().equals(signatureField.getCOSObject()))
                {
                    checkFields = true;
                    signatureField.getCOSObject().setNeedToBeUpdate(true);
                    break;
                }
            }
        }
        if (!checkFields)
        {
            acroFormFields.add(signatureField);
        }

        // Get the object from the visual signature
        COSDocument visualSignature = options.getVisualSignature();

        // Distinction of case for visual and non-visual signature
        if (visualSignature == null) // non-visual signature
        {
            // Set rectangle for non-visual signature to 0 0 0 0
            signatureField.getWidget().setRectangle(new PDRectangle()); // rectangle array [ 0 0 0 0 ]
            // Clear AcroForm / Set DefaultRessource
            acroForm.setDefaultResources(null);
            // Set empty Appearance-Dictionary
            PDAppearanceDictionary ap = new PDAppearanceDictionary();

            COSStream apsStream = getDocument().createCOSStream();
            apsStream.createUnfilteredStream();
            PDAppearanceStream aps = new PDAppearanceStream(apsStream);
            COSDictionary cosObject = (COSDictionary) aps.getCOSObject();
            cosObject.setItem(COSName.SUBTYPE, COSName.FORM);
            cosObject.setItem(COSName.BBOX, new PDRectangle());

            ap.setNormalAppearance(aps);
            ap.getCOSObject().setDirect(true);
            signatureField.getWidget().setAppearance(ap);
        }
        else
        // visual signature
        {
            // Obtain visual signature object
            List<COSObject> cosObjects = visualSignature.getObjects();

            boolean annotNotFound = true;
            boolean sigFieldNotFound = true;
            COSDictionary acroFormDict = acroForm.getDictionary();
            for (COSObject cosObject : cosObjects)
            {
                if (!annotNotFound && !sigFieldNotFound)
                {
                    break;
                }

                COSBase base = cosObject.getObject();
                if (base != null && base instanceof COSDictionary)
                {
                    COSBase ft = ((COSDictionary) base).getItem(COSName.FT);
                    COSBase type = ((COSDictionary) base).getItem(COSName.TYPE);
                    COSBase apDict = ((COSDictionary) base).getItem(COSName.AP);

                    // Search for signature annotation
                    if (annotNotFound && COSName.ANNOT.equals(type))
                    {
                        COSDictionary cosBaseDict = (COSDictionary) base;

                        // Read and set the Rectangle for visual signature
                        COSArray rectAry = (COSArray) cosBaseDict.getItem(COSName.RECT);
                        PDRectangle rect = new PDRectangle(rectAry);
                        signatureField.getWidget().setRectangle(rect);
                        annotNotFound = false;
                    }

                    // Search for Signature-Field
                    if (sigFieldNotFound && COSName.SIG.equals(ft) && apDict != null)
                    {
                        COSDictionary cosBaseDict = (COSDictionary) base;

                        // read and set Appearance Dictionary
                        PDAppearanceDictionary ap = 
                                new PDAppearanceDictionary((COSDictionary)cosBaseDict.getDictionaryObject(COSName.AP));
                        ap.getCOSObject().setDirect(true);
                        signatureField.getWidget().setAppearance(ap);

                        // read and set AcroForm DefaultResource
                        COSBase dr = cosBaseDict.getItem(COSName.DR);
                        if (dr != null)
                        {
                            dr.setDirect(true);
                            dr.setNeedToBeUpdate(true);
                            acroFormDict.setItem(COSName.DR, dr);
                        }
                        sigFieldNotFound = false;
                    }
                }
            }

            if (annotNotFound || sigFieldNotFound)
            {
                throw new IllegalArgumentException("Template is missing required objects");
            }
        }

        // Get the annotations of the page and append the signature-annotation to it
        if (annotations == null)
        {
            annotations = new COSArrayList();
            page.setAnnotations(annotations);
        }

        // take care that page and acroforms do not share the same array (if so, we don't need to add it twice)
        if (!(annotations instanceof COSArrayList &&
              acroFormFields instanceof COSArrayList &&
              ((COSArrayList) annotations).toList().equals(((COSArrayList) acroFormFields).toList()) &&
              checkFields))
        {
            annotations.add(signatureField.getWidget());
        }
        page.getCOSObject().setNeedToBeUpdate(true);
    }

    /**
     * This will add a signature field to the document.
     * 
     * @param sigFields are the PDSignatureFields that should be added to the document
     * @param signatureInterface is a interface which provides signing capabilities
     * @param options signature options
     * @throws IOException if there is an error creating required fields
     */
    public void addSignatureField(List<PDSignatureField> sigFields, SignatureInterface signatureInterface,
            SignatureOptions options) throws IOException
    {
        PDDocumentCatalog catalog = getDocumentCatalog();
        catalog.getCOSObject().setNeedToBeUpdate(true);

        PDAcroForm acroForm = catalog.getAcroForm();
        if (acroForm == null)
        {
            acroForm = new PDAcroForm(this);
            catalog.setAcroForm(acroForm);
        }
        else
        {
            acroForm.getCOSObject().setNeedToBeUpdate(true);
        }

        COSDictionary acroFormDict = acroForm.getDictionary();
        acroFormDict.setDirect(true);
        acroFormDict.setNeedToBeUpdate(true);
        if (!acroForm.isSignaturesExist())
        {
            acroForm.setSignaturesExist(true); // 1 if at least one signature field is available
        }

        List<PDFieldTreeNode> field = acroForm.getFields();

        for (PDSignatureField sigField : sigFields)
        {
            PDSignature sigObject = sigField.getSignature();
            sigField.getCOSObject().setNeedToBeUpdate(true);

            // Check if the field already exist
            boolean checkFields = false;
            for (PDFieldTreeNode fieldNode : field)
            {
                if (fieldNode instanceof PDSignatureField)
                {
                    if (fieldNode.getCOSObject().equals(sigField.getCOSObject()))
                    {
                        checkFields = true;
                        sigField.getCOSObject().setNeedToBeUpdate(true);
                        break;
                    }
                }
            }

            if (!checkFields)
            {
                field.add(sigField);
            }

            // Check if we need to add a signature
            if (sigField.getSignature() != null)
            {
                sigField.getCOSObject().setNeedToBeUpdate(true);
                if (options == null)
                {

                }
                addSignature(sigField.getSignature(), signatureInterface, options);
            }
        }
    }

    /**
     * Remove the page from the document.
     * 
     * @param page The page to remove from the document.
     */
    public void removePage(PDPage page)
    {
        getPages().remove(page);
    }

    /**
     * Remove the page from the document.
     * 
     * @param pageNumber 0 based index to page number.
     */
    public void removePage(int pageNumber)
    {
        getPages().remove(pageNumber);
    }

    /**
     * This will import and copy the contents from another location. Currently the content stream is stored in a scratch
     * file. The scratch file is associated with the document. If you are adding a page to this document from another
     * document and want to copy the contents to this document's scratch file then use this method otherwise just use
     * the addPage method.
     * 
     * @param page The page to import.
     * @return The page that was imported.
     * 
     * @throws IOException If there is an error copying the page.
     */
    public PDPage importPage(PDPage page) throws IOException
    {
        PDPage importedPage = new PDPage(new COSDictionary(page.getCOSObject()));
        InputStream is = null;
        OutputStream os = null;
        try
        {
            PDStream src = page.getStream();
            if (src != null)
            {
                PDStream dest = new PDStream(document.createCOSStream());
                importedPage.setContents(dest);
                os = dest.createOutputStream();

                byte[] buf = new byte[10240];
                int amountRead;
                is = src.createInputStream();
                while ((amountRead = is.read(buf, 0, 10240)) > -1)
                {
                    os.write(buf, 0, amountRead);
                }
            }
            addPage(importedPage);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
            if (os != null)
            {
                os.close();
            }
        }
        return importedPage;

    }

    /**
     * Constructor that uses an existing document. The COSDocument that is passed in must be valid.
     * 
     * @param doc The COSDocument that this document wraps.
     */
    public PDDocument(COSDocument doc)
    {
        this(doc, null);
    }

    /**
     * Constructor that uses an existing document. The COSDocument that is passed in must be valid.
     * 
     * @param doc The COSDocument that this document wraps.
     * @param usedParser the parser which is used to read the pdf
     */
    public PDDocument(COSDocument doc, BaseParser usedParser)
    {
        this(doc, usedParser, null);
    }

    /**
     * Constructor that uses an existing document. The COSDocument that is passed in must be valid.
     * 
     * @param doc The COSDocument that this document wraps.
     * @param usedParser the parser which is used to read the pdf
     * @param permission he access permissions of the pdf
     * 
     */
    public PDDocument(COSDocument doc, BaseParser usedParser, AccessPermission permission)
    {
        document = doc;
        parser = usedParser;
        accessPermission = permission;
    }

    /**
     * This will get the low level document.
     * 
     * @return The document that this layer sits on top of.
     */
    public COSDocument getDocument()
    {
        return document;
    }

    /**
     * This will get the document info dictionary. This is guaranteed to not return null.
     * 
     * @return The documents /Info dictionary
     */
    public PDDocumentInformation getDocumentInformation()
    {
        if (documentInformation == null)
        {
            COSDictionary trailer = document.getTrailer();
            COSDictionary infoDic = (COSDictionary) trailer.getDictionaryObject(COSName.INFO);
            if (infoDic == null)
            {
                infoDic = new COSDictionary();
                trailer.setItem(COSName.INFO, infoDic);
            }
            documentInformation = new PDDocumentInformation(infoDic);
        }
        return documentInformation;
    }

    /**
     * This will set the document information for this document.
     * 
     * @param info The updated document information.
     */
    public void setDocumentInformation(PDDocumentInformation info)
    {
        documentInformation = info;
        document.getTrailer().setItem(COSName.INFO, info.getDictionary());
    }

    /**
     * This will get the document CATALOG. This is guaranteed to not return null.
     * 
     * @return The documents /Root dictionary
     */
    public PDDocumentCatalog getDocumentCatalog()
    {
        if (documentCatalog == null)
        {
            COSDictionary trailer = document.getTrailer();
            COSBase dictionary = trailer.getDictionaryObject(COSName.ROOT);
            if (dictionary instanceof COSDictionary)
            {
                documentCatalog = new PDDocumentCatalog(this, (COSDictionary) dictionary);
            }
            else
            {
                documentCatalog = new PDDocumentCatalog(this);
            }
        }
        return documentCatalog;
    }

    /**
     * This will tell if this document is encrypted or not.
     * 
     * @return true If this document is encrypted.
     */
    public boolean isEncrypted()
    {
        return document.isEncrypted();
    }

    /**
     * @deprecated Use {@link #getEncryption()} instead.
     *
     * @return The encryption dictionary(most likely a PDStandardEncryption object)
     */
    @Deprecated
    public PDEncryption getEncryptionDictionary()
    {
        return getEncryption();
    }

    /**
     * This will get the encryption dictionary for this document. This will still return the parameters if the document
     * was decrypted. As the encryption architecture in PDF documents is plugable this returns an abstract class,
     * but the only supported subclass at this time is a
     * PDStandardEncryption object.
     *
     * @return The encryption dictionary(most likely a PDStandardEncryption object)
     */
    public PDEncryption getEncryption()
    {
        if (encryption == null)
        {
            if (isEncrypted())
            {
                encryption = new PDEncryption(document.getEncryptionDictionary());
            }
        }
        return encryption;
    }

    /**
     * This will set the encryption dictionary for this document.
     * 
     * @param encryption The encryption dictionary(most likely a PDStandardEncryption object)
     * 
     * @throws IOException If there is an error determining which security handler to use.
     */
    public void setEncryptionDictionary(PDEncryption encryption) throws IOException
    {
        this.encryption = encryption;
    }

    /**
     * This will return the last signature.
     * 
     * @return the last signature as <code>PDSignatureField</code>.
     * @throws IOException if no document catalog can be found.
     */
    public PDSignature getLastSignatureDictionary() throws IOException
    {
        List<PDSignature> signatureDictionaries = getSignatureDictionaries();
        int size = signatureDictionaries.size();
        if (size > 0)
        {
            return signatureDictionaries.get(size - 1);
        }
        return null;
    }

    /**
     * Retrieve all signature fields from the document.
     * 
     * @return a <code>List</code> of <code>PDSignatureField</code>s
     * @throws IOException if no document catalog can be found.
     */
    public List<PDSignatureField> getSignatureFields() throws IOException
    {
        List<PDSignatureField> fields = new LinkedList<PDSignatureField>();
        PDAcroForm acroForm = getDocumentCatalog().getAcroForm();
        if (acroForm != null)
        {
            List<COSDictionary> signatureDictionary = document.getSignatureFields(false);
            for (COSDictionary dict : signatureDictionary)
            {
                fields.add(new PDSignatureField(acroForm, dict, null));
            }
        }
        return fields;
    }

    /**
     * Retrieve all signature dictionaries from the document.
     * 
     * @return a <code>List</code> of <code>PDSignatureField</code>s
     * @throws IOException if no document catalog can be found.
     */
    public List<PDSignature> getSignatureDictionaries() throws IOException
    {
        List<COSDictionary> signatureDictionary = document.getSignatureDictionaries();
        List<PDSignature> signatures = new LinkedList<PDSignature>();
        for (COSDictionary dict : signatureDictionary)
        {
            signatures.add(new PDSignature(dict));
        }
        return signatures;
    }

    /**
     * This will decrypt a document.
     *
     * @deprecated This method is provided for compatibility reasons only. User should use the new
     * security layer instead and the openProtection method especially.
     * 
     * @param password Either the user or owner password.
     *
     * @throws IOException If there is an error getting the stream data.
     */
    @Deprecated
    public void decrypt(String password) throws IOException
    {
        StandardDecryptionMaterial m = new StandardDecryptionMaterial(password);
        openProtection(m);
    }

    /**
     * This will <b>mark</b> a document to be encrypted. The actual encryption will occur when the document is saved.
     *
     * @deprecated This method is provided for compatibility reasons only. User should use the new security layer 
     * instead and the openProtection method especially.
     * 
     * @param ownerPassword The owner password to encrypt the document.
     * @param userPassword The user password to encrypt the document.

     * @throws IOException If there is an error accessing the data.
     */
    @Deprecated
    public void encrypt(String ownerPassword, String userPassword) throws IOException
    {
        if (!isEncrypted())
        {
            encryption = new PDEncryption();
        }

        getEncryption().setSecurityHandler(new StandardSecurityHandler(
                new StandardProtectionPolicy(ownerPassword, userPassword, new AccessPermission())));
    }

    /**
     * The owner password that was passed into the encrypt method. You should never use this method. This will not
     * longer be valid once encryption has occured.
     * 
     * @return The owner password passed to the encrypt method.
     * 
     * @deprecated Do not rely on this method anymore.
     */
    @Deprecated
    public String getOwnerPasswordForEncryption()
    {
        return null;
    }

    /**
     * The user password that was passed into the encrypt method. You should never use this method. This will not longer
     * be valid once encryption has occured.
     * 
     * @return The user password passed to the encrypt method.
     * 
     * @deprecated Do not rely on this method anymore.
     */
    @Deprecated
    public String getUserPasswordForEncryption()
    {
        return null;
    }

    /**
     * This will load a document from a file.
     * 
     * @param file The name of the file to load.
     * 
     * @return The document that was loaded.
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument loadLegacy(File file) throws IOException
    {
        return loadLegacy(file, false);
    }

    /**
     * This will load a document from a file. Allows for skipping corrupt pdf objects
     *
     * @param file The name of the file to load.
     * @param useScratchFiles enables the usage of a scratch file if set to true
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument loadLegacy(File file, boolean useScratchFiles) throws IOException
    {
        PDFParser parser = new PDFParser(new FileInputStream(file), useScratchFiles);
        parser.parse();
        PDDocument doc = parser.getPDDocument();
        doc.incrementalFile = file;
        return doc;
    }

    /**
     * This will load a document from an input stream.
     * 
     * @param input The stream that contains the document.
     * 
     * @return The document that was loaded.
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument loadLegacy(InputStream input) throws IOException
    {
        return loadLegacy(input, false);
    }

    /**
     * This will load a document from an input stream. Allows for skipping corrupt pdf objects
     * 
     * @param input The stream that contains the document.
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return The document that was loaded.
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument loadLegacy(InputStream input, boolean useScratchFiles) throws IOException
    {
        PDFParser parser = new PDFParser(input, useScratchFiles);
        parser.parse();
        return parser.getPDDocument();
    }
    /**
     * Parses PDF with non sequential parser.
     * 
     * @param file file to be loaded
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file) throws IOException
    {
        return load(file, "", false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param file file to be loaded
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, boolean useScratchFiles) throws IOException
    {
        return load(file, "", null, null, useScratchFiles);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password) throws IOException
    {
        return load(file, password, null, null, false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password, boolean useScratchFiles) throws IOException
    {
        return load(file, password, null, null, useScratchFiles);
    }

    /**
     * Parses PDF with non sequential parser.
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
        return load(file, password, keyStore, alias, false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(File file, String password, InputStream keyStore, String alias,
            boolean useScratchFiles) throws IOException
    {
        NonSequentialPDFParser parser = new NonSequentialPDFParser(file, password, keyStore, alias, useScratchFiles);
        parser.parse();
        return parser.getPDDocument();
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param input stream that contains the document.
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(InputStream input) throws IOException
    {
        return load(input, "", null, null, false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param input stream that contains the document.
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(InputStream input, boolean useScratchFiles) throws IOException
    {
        return load(input, "", null, null, useScratchFiles);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param input stream that contains the document.
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(InputStream input, String password)
            throws IOException
    {
        return load(input, password, null, null, false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param input stream that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(InputStream input, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return load(input, password, keyStore, alias, false);
    }

    /**
     * Parses PDF with non sequential parser.
     * 
     * @param input stream that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument load(InputStream input, String password, InputStream keyStore, 
            String alias, boolean useScratchFiles) throws IOException
    {
        NonSequentialPDFParser parser = new NonSequentialPDFParser(input, password, keyStore, alias, useScratchFiles);
        parser.parse();
        return parser.getPDDocument();
    }

    /**
     * Save the document to a file.
     * 
     * @param fileName The file to save as.
     *
     * @throws IOException if the output could not be written
     */
    public void save(String fileName) throws IOException
    {
        save(new File(fileName));
    }

    /**
     * Save the document to a file.
     * 
     * @param file The file to save as.
     *
     * @throws IOException if the output could not be written
     */
    public void save(File file) throws IOException
    {
        save(new FileOutputStream(file));
    }

    /**
     * This will save the document to an output stream.
     * 
     * @param output The stream to write to.
     *
     * @throws IOException if the output could not be written
     */
    public void save(OutputStream output) throws IOException
    {
        if (document == null)
        {
            throw new IOException("Cannot save a document which has been closed");
        }
        COSWriter writer = null;
        try
        {
            writer = new COSWriter(output);
            writer.write(this);
            writer.close();
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }

    /**
     * Save the pdf as incremental.
     *
     * @deprecated Use {@link #saveIncremental(OutputStream output)} instead.
     *
     * @param fileName the filename to be used
     * @throws IOException if the output could not be written
     */
    @Deprecated
    public void saveIncremental(String fileName) throws IOException
    {
        saveIncremental(new BufferedInputStream(new FileInputStream(fileName)),
                new BufferedOutputStream(new FileOutputStream(fileName, true)));
    }

    /**
     * Save the PDF as an incremental update, explicitly providing the original input stream again.
     *
     * Use of this method is discouraged, use {@link #saveIncremental(OutputStream)} instead.
     *
     * @param input stream to read, must contain the same data used in the call to load().
     * @param output stream to write
     * @throws IOException if the output could not be written
     */
    public void saveIncremental(InputStream input, OutputStream output) throws IOException
    {
        COSWriter writer = null;
        try
        {
            writer = new COSWriter(output, input);
            writer.write(this);
            writer.close();
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }

    /**
     * Save the PDF as an incremental update, if it was loaded from a File.
     * This method can only be used when the PDDocument was created by passing a File or filename
     * to one of the load() constructors.
     *
     * @param output stream to write
     * @throws IOException if the output could not be written
     */
    public void saveIncremental(OutputStream output) throws IOException
    {
        if (incrementalFile == null)
        {
            throw new IOException("PDDocument.load must be called with a File or String");
        }
        saveIncremental(new FileInputStream(incrementalFile), output);
    }

    /**
     * Returns the page at the given index.
     *
     * @param pageIndex the page index
     * @return the page at the given index.
     */
    public PDPage getPage(int pageIndex) // todo: REPLACE most calls to this method with BELOW method
    {
        return getDocumentCatalog().getPages().get(pageIndex);
    }

    // todo: new!
    public PDPageTree getPages()
    {
        return getDocumentCatalog().getPages();
    }

    /**
     * This will return the total page count of the PDF document.
     * 
     * @return The total number of pages in the PDF document.
     */
    public int getNumberOfPages()
    {
        return getDocumentCatalog().getPages().getCount();
    }

    /**
     * This will close the underlying COSDocument object.
     * 
     * @throws IOException If there is an error releasing resources.
     */
    @Override
    public void close() throws IOException
    {
        documentCatalog = null;
        documentInformation = null;
        encryption = null;
        if (document != null)
        {
            document.close();
            document = null;
        }
        if (parser != null)
        {
            parser.clearResources();
            parser = null;
        }
        accessPermission = null;
    }

    /**
     * Protects the document with the protection policy pp. The document content will be really encrypted when it will
     * be saved. This method only marks the document for encryption.
     *
     * @see org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
     * @see org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy
     * 
     * @param policy The protection policy.
     * 
     * @throws IOException if there isn't any suitable security handler.
     */
    public void protect(ProtectionPolicy policy) throws IOException
    {
        if (!isEncrypted())
        {
            encryption = new PDEncryption();
        }

        SecurityHandler securityHandler = SecurityHandlerFactory.INSTANCE.newSecurityHandlerForPolicy(policy);
        if (securityHandler == null)
        {
            throw new IOException("No security handler for policy " + policy);
        }

        getEncryption().setSecurityHandler(securityHandler);
    }

    /**
     * Tries to decrypt the document in memory using the provided decryption material.
     * 
     * @see org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial
     * @see org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial
     * 
     * @param decryptionMaterial The decryption material (password or certificate).
     *
     * @throws IOException If there is an error reading cryptographic information.
     */
    public void openProtection(DecryptionMaterial decryptionMaterial) throws IOException
    {
        if (isEncrypted())
        {
            SecurityHandler securityHandler = getEncryption().getSecurityHandler();
            securityHandler.decryptDocument(this, decryptionMaterial);
            accessPermission = securityHandler.getCurrentAccessPermission();
            document.dereferenceObjectStreams();
            document.setEncryptionDictionary(null);
            getDocumentCatalog();
        }
        else
        {
            throw new IOException("Document is not encrypted");
        }
    }

    /**
     * Returns the access permissions granted when the document was decrypted. If the document was not decrypted this
     * method returns the access permission for a document owner (ie can do everything). The returned object is in read
     * only mode so that permissions cannot be changed. Methods providing access to content should rely on this object
     * to verify if the current user is allowed to proceed.
     * 
     * @return the access permissions for the current user on the document.
     */
    public AccessPermission getCurrentAccessPermission()
    {
        if (accessPermission == null)
        {
            accessPermission = AccessPermission.getOwnerAccessPermission();
        }
        return accessPermission;
    }

    /**
     * Get the security handler that is used for document encryption.
     *
     * @deprecated Use {@link #getEncryption()}.
     * {@link org.apache.pdfbox.pdmodel.encryption.PDEncryption#getSecurityHandler()}
     *
     * @return The handler used to encrypt/decrypt the document.
     */
    @Deprecated
    public SecurityHandler getSecurityHandler()
    {
        if (isEncrypted() && getEncryption().hasSecurityHandler())
        {
            try
            {
                return getEncryption().getSecurityHandler();
            }
            catch (IOException e)
            {
                // will never happen because we checked hasSecurityHandler() first
                throw new RuntimeException(e);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * @deprecated Use protection policies instead.
     *
     * @param securityHandler security handler to be assigned to document
     * @return true if security handler was set
     */
    @Deprecated
    public boolean setSecurityHandler(SecurityHandler securityHandler)
    {
        if (isEncrypted())
        {
            return false;
        }
        encryption = new PDEncryption();
        getEncryption().setSecurityHandler(securityHandler);
        return true;
    }

    /**
     * Indicates if all security is removed or not when writing the pdf.
     * 
     * @return returns true if all security shall be removed otherwise false
     */
    public boolean isAllSecurityToBeRemoved()
    {
        return allSecurityToBeRemoved;
    }

    /**
     * Activates/Deactivates the removal of all security when writing the pdf.
     * 
     * @param removeAllSecurity remove all security if set to true
     */
    public void setAllSecurityToBeRemoved(boolean removeAllSecurity)
    {
        allSecurityToBeRemoved = removeAllSecurity;
    }

    /**
     * Provides the document ID.
     *
     * @return the dcoument ID
     */
    public Long getDocumentId()
    {
        return documentId;
    }

    /**
     * Sets the document ID to the given value.
     * 
     * @param docId the new document ID
     */
    public void setDocumentId(Long docId)
    {
        documentId = docId;
    }
}
