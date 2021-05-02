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

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSUpdateInfo;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandlerFactory;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SigningSupport;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * This is the in-memory representation of the PDF document.
 * The #close() method must be called once the document is no longer needed.
 * 
 * @author Ben Litchfield
 */
public class PDDocument implements Closeable
{
    /**
     * For signing: large reserve byte range used as placeholder in the saved PDF until the actual
     * length of the PDF is known. You'll need to fetch (with
     * {@link PDSignature#getByteRange()} ) and reassign this yourself (with
     * {@link PDSignature#setByteRange(int[])} ) only if you call
     * {@link #saveIncrementalForExternalSigning(java.io.OutputStream) saveIncrementalForExternalSigning()}
     * twice.
     */
    private static final int[] RESERVE_BYTE_RANGE = new int[] { 0, 1000000000, 1000000000, 1000000000 };

    private static final Log LOG = LogFactory.getLog(PDDocument.class);

    /**
     * avoid concurrency issues with PDDeviceRGB and deadlock in COSNumber/COSInteger
     */
    static
    {
    	try
        {
            WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, 1, 1, 3, new Point(0, 0));
            PDDeviceRGB.INSTANCE.toRGBImage(raster);
        }
        catch (IOException ex)
        {
            LOG.debug("voodoo error", ex);
        }

        try
        {
            COSNumber.get("0");
            COSNumber.get("1");
        }
        catch (IOException ex)
        {
            //
        }
    }
    
    private final COSDocument document;

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

    // the pdf to be read
    private final RandomAccessRead pdfSource;

    // the access permissions of the document
    private AccessPermission accessPermission;
    
    // fonts to subset before saving
    private final Set<PDFont> fontsToSubset = new HashSet<PDFont>();

    // fonts to close when closing document
    private final Set<TrueTypeFont> fontsToClose = new HashSet<TrueTypeFont>();

    // Signature interface
    private SignatureInterface signInterface;

    // helper class used to create external signature
    private SigningSupport signingSupport;

    // document-wide cached resources
    private ResourceCache resourceCache = new DefaultResourceCache();

    // to make sure only one signature is added
    private boolean signatureAdded = false;

    /**
     * Creates an empty PDF document.
     * You need to add at least one page for the document to be valid.
     */
    public PDDocument()
    {
        this(MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Creates an empty PDF document.
     * You need to add at least one page for the document to be valid.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams 
     */
    public PDDocument(MemoryUsageSetting memUsageSetting)
    {
        ScratchFile scratchFile = null;
        try
        {
            scratchFile = new ScratchFile(memUsageSetting);
        }
        catch (IOException ioe)
        {
            LOG.warn("Error initializing scratch file: " + ioe.getMessage() +
                     ". Fall back to main memory usage only.");
            try
            {
                scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly());
            }
            catch (IOException ioe2) {}
        }
        
        document = new COSDocument(scratchFile);
        pdfSource = null;

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
     * @param source the parser which is used to read the pdf
     */
    public PDDocument(COSDocument doc, RandomAccessRead source)
    {
        this(doc, source, null);
    }

    /**
     * Constructor that uses an existing document. The COSDocument that is passed in must be valid.
     * 
     * @param doc The COSDocument that this document wraps.
     * @param source the parser which is used to read the pdf
     * @param permission he access permissions of the pdf
     * 
     */
    public PDDocument(COSDocument doc, RandomAccessRead source, AccessPermission permission)
    {
        document = doc;
        pdfSource = source;
        accessPermission = permission;
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
     * Add parameters of signature to be created externally using default signature options. See
     * {@link #saveIncrementalForExternalSigning(OutputStream)} method description on external
     * signature creation scenario details.
     * <p>
     * Only one signature may be added in a document. To sign several times,
     * load document, add signature, save incremental and close again.
     *
     * @param sigObject is the PDSignatureField model
     * @throws IOException if there is an error creating required fields
     * @throws IllegalStateException if one attempts to add several signature
     * fields.
     */
    public void addSignature(PDSignature sigObject) throws IOException
    {
        addSignature(sigObject, new SignatureOptions());
    }

    /**
     * Add parameters of signature to be created externally. See
     * {@link #saveIncrementalForExternalSigning(OutputStream)} method description on external
     * signature creation scenario details.
     * <p>
     * Only one signature may be added in a document. To sign several times,
     * load document, add signature, save incremental and close again.
     *
     * @param sigObject is the PDSignatureField model
     * @param options signature options
     * @throws IOException if there is an error creating required fields
     * @throws IllegalStateException if one attempts to add several signature
     * fields.
     */
    public void addSignature(PDSignature sigObject, SignatureOptions options) throws IOException
    {
        addSignature(sigObject, null, options);
    }

    /**
     * Add a signature to be created using the instance of given interface.
     * <p>
     * Only one signature may be added in a document. To sign several times,
     * load document, add signature, save incremental and close again.
     * 
     * @param sigObject is the PDSignatureField model
     * @param signatureInterface is an interface whose implementation provides
     * signing capabilities. Can be null if external signing if used.
     * @throws IOException if there is an error creating required fields
     * @throws IllegalStateException if one attempts to add several signature
     * fields.
     */
    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface) throws IOException
    {
        addSignature(sigObject, signatureInterface, new SignatureOptions());
    }

    /**
     * This will add a signature to the document. If the 0-based page number in the options
     * parameter is smaller than 0 or larger than max, the nearest valid page number will be used
     * (i.e. 0 or max) and no exception will be thrown.
     * <p>
     * Only one signature may be added in a document. To sign several times,
     * load document, add signature, save incremental and close again.
     *
     * @param sigObject is the PDSignatureField model
     * @param signatureInterface is an interface whose implementation provides
     * signing capabilities. Can be null if external signing if used.
     * @param options signature options
     * @throws IOException if there is an error creating required fields
     * @throws IllegalStateException if one attempts to add several signature
     * fields.
     */
    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface,
                             SignatureOptions options) throws IOException
    {
        if (signatureAdded)
        {
            throw new IllegalStateException("Only one signature may be added in a document");
        }
        signatureAdded = true;

        // Reserve content
        // We need to reserve some space for the signature. Some signatures including
        // big certificate chain and we need enough space to store it.
        int preferredSignatureSize = options.getPreferredSignatureSize();
        if (preferredSignatureSize > 0)
        {
            sigObject.setContents(new byte[preferredSignatureSize]);
        }
        else
        {
            sigObject.setContents(new byte[SignatureOptions.DEFAULT_SIGNATURE_SIZE]);
        }

        // Reserve ByteRange, will be overwritten in COSWriter
        sigObject.setByteRange(RESERVE_BYTE_RANGE);

        signInterface = signatureInterface;

        // Create SignatureForm for signature and append it to the document

        // Get the first valid page
        int pageCount = getNumberOfPages();
        if (pageCount == 0)
        {
            throw new IllegalStateException("Cannot sign an empty document");
        }

        int startIndex = Math.min(Math.max(options.getPage(), 0), pageCount - 1);
        PDPage page = getPage(startIndex);

        // Get the AcroForm from the Root-Dictionary and append the annotation
        PDDocumentCatalog catalog = getDocumentCatalog();
        PDAcroForm acroForm = catalog.getAcroForm(null);
        catalog.getCOSObject().setNeedToBeUpdated(true);

        if (acroForm == null)
        {
            acroForm = new PDAcroForm(this);
            catalog.setAcroForm(acroForm);
        }
        else
        {
            acroForm.getCOSObject().setNeedToBeUpdated(true);
        }

        PDSignatureField signatureField = null;
        COSBase cosFieldBase = acroForm.getCOSObject().getDictionaryObject(COSName.FIELDS);
        if (cosFieldBase instanceof COSArray)
        {
            COSArray fieldArray = (COSArray) cosFieldBase;
            fieldArray.setNeedToBeUpdated(true);
            signatureField = findSignatureField(acroForm.getFieldIterator(), sigObject);
        }
        else
        {
            acroForm.getCOSObject().setItem(COSName.FIELDS, new COSArray());
        }
        if (signatureField == null)
        {
            signatureField = new PDSignatureField(acroForm);
            // append the signature object
            signatureField.setValue(sigObject);
            // backward linking
            signatureField.getWidgets().get(0).setPage(page);
        }
        else
        {
            sigObject.getCOSObject().setNeedToBeUpdated(true);
        }

        // TODO This "overwrites" the settings of the original signature field which might not be intended by the user
        // better make it configurable (not all users need/want PDF/A but their own setting):

        // to conform PDF/A-1 requirement:
        // The /F key's Print flag bit shall be set to 1 and 
        // its Hidden, Invisible and NoView flag bits shall be set to 0
        signatureField.getWidgets().get(0).setPrinted(true);
        // This may be troublesome if several form fields are signed,
        // see thread from PDFBox users mailing list 17.2.2021 - 19.2.2021
        // https://mail-archives.apache.org/mod_mbox/pdfbox-users/202102.mbox/thread
        // better set the printed flag in advance

        // Set the AcroForm Fields
        List<PDField> acroFormFields = acroForm.getFields();
        acroForm.getCOSObject().setDirect(true);
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);

        boolean checkFields = checkSignatureField(acroForm.getFieldIterator(), signatureField);
        if (checkFields)
        {
            signatureField.getCOSObject().setNeedToBeUpdated(true);
        }
        else
        {
            acroFormFields.add(signatureField);
        }

        // Get the object from the visual signature
        COSDocument visualSignature = options.getVisualSignature();

        // Distinction of case for visual and non-visual signature
        if (visualSignature == null)
        {
            prepareNonVisibleSignature(signatureField);
            return;
        }

        prepareVisibleSignature(signatureField, acroForm, visualSignature);

        // Create Annotation / Field for signature
        List<PDAnnotation> annotations = page.getAnnotations();

        // Make /Annots a direct object to avoid problem if it is an existing indirect object: 
        // it would not be updated in incremental save, and if we'd set the /Annots array "to be updated" 
        // while keeping it indirect, Adobe Reader would claim that the document had been modified.
        page.setAnnotations(annotations);

        // Get the annotations of the page and append the signature-annotation to it
        // take care that page and acroforms do not share the same array (if so, we don't need to add it twice)
        if (!(annotations instanceof COSArrayList &&
              acroFormFields instanceof COSArrayList &&
              ((COSArrayList<?>) annotations).toList().equals(((COSArrayList<?>) acroFormFields).toList()) &&
              checkFields))
        {
            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            // use check to prevent the annotation widget from appearing twice
            if (checkSignatureAnnotation(annotations, widget))
            {
                widget.getCOSObject().setNeedToBeUpdated(true);
            }
            else
            {
                annotations.add(widget);
            }   
        }
        page.getCOSObject().setNeedToBeUpdated(true);
    }

    /**
     * Search acroform fields for signature field with specific signature dictionary.
     * 
     * @param fieldIterator iterator on all fields.
     * @param sigObject signature object (the /V part).
     * @return a signature field if found, or null if none was found.
     */
    private PDSignatureField findSignatureField(Iterator<PDField> fieldIterator, PDSignature sigObject)
    {
        PDSignatureField signatureField = null;
        while (fieldIterator.hasNext())
        {
            PDField pdField = fieldIterator.next();
            if (pdField instanceof PDSignatureField)
            {
                PDSignature signature = ((PDSignatureField) pdField).getSignature();
                if (signature != null && signature.getCOSObject().equals(sigObject.getCOSObject()))
                {
                    signatureField = (PDSignatureField) pdField;
                    break;
                }
            }
        }
        return signatureField;
    }

    /**
     * Check if the field already exists in the field list.
     *
     * @param fieldIterator iterator on all fields.
     * @param signatureField the signature field.
     * @return true if the field already existed in the field list, false if not.
     */
    private boolean checkSignatureField(Iterator<PDField> fieldIterator, PDSignatureField signatureField)
    {
        while (fieldIterator.hasNext())
        {
            PDField field = fieldIterator.next();
            if (field instanceof PDSignatureField
                    && field.getCOSObject().equals(signatureField.getCOSObject()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the widget already exists in the annotation list
     *
     * @param annotations the list of PDAnnotation fields.
     * @param widget the annotation widget.
     * @return true if the widget already existed in the annotation list, false if not.
     */
    private boolean checkSignatureAnnotation(List<PDAnnotation> annotations, PDAnnotationWidget widget)
    {
        for (PDAnnotation annotation : annotations)
        {
            if (annotation.getCOSObject().equals(widget.getCOSObject()))
            {
                return true;
            }
        }
        return false;
    }

    private void prepareVisibleSignature(PDSignatureField signatureField, PDAcroForm acroForm, 
            COSDocument visualSignature)
    {
        // Obtain visual signature object
        boolean annotNotFound = true;
        boolean sigFieldNotFound = true;
        for (COSObject cosObject : visualSignature.getObjects())
        {
            if (!annotNotFound && !sigFieldNotFound)
            {
                break;
            }
            
            COSBase base = cosObject.getObject();
            if (base instanceof COSDictionary)
            {
                COSDictionary cosBaseDict = (COSDictionary) base;

                // Search for signature annotation
                COSBase type = cosBaseDict.getDictionaryObject(COSName.TYPE);
                if (annotNotFound && COSName.ANNOT.equals(type))
                {
                    assignSignatureRectangle(signatureField, cosBaseDict);
                    annotNotFound = false;
                }

                // Search for signature field
                COSBase fieldType = cosBaseDict.getDictionaryObject(COSName.FT);
                COSBase apDict = cosBaseDict.getDictionaryObject(COSName.AP);
                if (sigFieldNotFound && COSName.SIG.equals(fieldType) && apDict instanceof COSDictionary)
                {
                    assignAppearanceDictionary(signatureField, (COSDictionary) apDict);
                    assignAcroFormDefaultResource(acroForm, cosBaseDict);
                    sigFieldNotFound = false;
                }
            }
        }
        
        if (annotNotFound || sigFieldNotFound)
        {
            throw new IllegalArgumentException("Template is missing required objects");
        }
    }

    private void assignSignatureRectangle(PDSignatureField signatureField, COSDictionary annotDict)
    {
        // Read and set the rectangle for visual signature
        COSArray rectArray = (COSArray) annotDict.getDictionaryObject(COSName.RECT);
        PDRectangle existingRectangle = signatureField.getWidgets().get(0).getRectangle();

        //in case of an existing field keep the original rect
        if (existingRectangle == null || existingRectangle.getCOSArray().size() != 4)
        {
            PDRectangle rect = new PDRectangle(rectArray);
            signatureField.getWidgets().get(0).setRectangle(rect);
        }
    }

    private void assignAppearanceDictionary(PDSignatureField signatureField, COSDictionary apDict)
    {
        // read and set Appearance Dictionary
        PDAppearanceDictionary ap = new PDAppearanceDictionary(apDict);
        apDict.setDirect(true);
        signatureField.getWidgets().get(0).setAppearance(ap);
    }

    private void assignAcroFormDefaultResource(PDAcroForm acroForm, COSDictionary newDict)
    {
        // read and set/update AcroForm default resource dictionary /DR if available
        COSBase newBase = newDict.getDictionaryObject(COSName.DR);
        if (newBase instanceof COSDictionary)
        {
            COSDictionary newDR = (COSDictionary) newBase;
            PDResources defaultResources = acroForm.getDefaultResources();
            if (defaultResources == null)
            {
                acroForm.getCOSObject().setItem(COSName.DR, newDR);
                newDR.setDirect(true);
                newDR.setNeedToBeUpdated(true);            
            }
            else
            {
                COSDictionary oldDR = defaultResources.getCOSObject();
                COSBase newXObjectBase = newDR.getItem(COSName.XOBJECT);
                COSBase oldXObjectBase = oldDR.getItem(COSName.XOBJECT);
                if (newXObjectBase instanceof COSDictionary &&
                    oldXObjectBase instanceof COSDictionary)
                {
                    ((COSDictionary) oldXObjectBase).addAll((COSDictionary) newXObjectBase);
                    oldDR.setNeedToBeUpdated(true);
                }
            }
        }
    }

    private void prepareNonVisibleSignature(PDSignatureField signatureField)
    {
        // "Signature fields that are not intended to be visible shall
        // have an annotation rectangle that has zero height and width."
        // Set rectangle for non-visual signature to rectangle array [ 0 0 0 0 ]
        signatureField.getWidgets().get(0).setRectangle(new PDRectangle());
        
        // The visual appearance must also exist for an invisible signature but may be empty.
        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        PDAppearanceStream appearanceStream = new PDAppearanceStream(this);
        appearanceStream.setBBox(new PDRectangle());
        appearanceDictionary.setNormalAppearance(appearanceStream);
        signatureField.getWidgets().get(0).setAppearance(appearanceDictionary);
    }

    /**
     * This will add a list of signature fields to the document.
     * 
     * @param sigFields are the PDSignatureFields that should be added to the document
     * @param signatureInterface is an interface whose implementation provides
     * signing capabilities. Can be null if external signing if used.
     * @param options signature options
     * @throws IOException if there is an error creating required fields
     * @deprecated The method is misleading, because only one signature may be
     * added in a document. The method will be removed in the future.
     */
    @Deprecated
    public void addSignatureField(List<PDSignatureField> sigFields, SignatureInterface signatureInterface,
            SignatureOptions options) throws IOException
    {
        PDDocumentCatalog catalog = getDocumentCatalog();
        catalog.getCOSObject().setNeedToBeUpdated(true);

        PDAcroForm acroForm = catalog.getAcroForm(null);
        if (acroForm == null)
        {
            acroForm = new PDAcroForm(this);
            catalog.setAcroForm(acroForm);
        }
        COSDictionary acroFormDict = acroForm.getCOSObject();
        acroFormDict.setDirect(true);
        acroFormDict.setNeedToBeUpdated(true);
        if (!acroForm.isSignaturesExist())
        {
            // 1 if at least one signature field is available
            acroForm.setSignaturesExist(true); 
        }

        List<PDField> acroformFields = acroForm.getFields();

        for (PDSignatureField sigField : sigFields)
        {
            sigField.getCOSObject().setNeedToBeUpdated(true);
            
            // Check if the field already exists
            boolean checkSignatureField = checkSignatureField(acroForm.getFieldIterator(), sigField);
            if (checkSignatureField)
            {
                sigField.getCOSObject().setNeedToBeUpdated(true);
            }
            else
            {
                acroformFields.add(sigField);
            }

            // Check if we need to add a signature
            if (sigField.getSignature() != null)
            {
                sigField.getCOSObject().setNeedToBeUpdated(true);
                if (options == null)
                {
                    // TODO ??
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
     * This will import and copy the contents from another location. Currently the content stream is
     * stored in a scratch file. The scratch file is associated with the document. If you are adding
     * a page to this document from another document and want to copy the contents to this
     * document's scratch file then use this method otherwise just use the {@link #addPage addPage()}
     * method.
     * <p>
     * Unlike {@link #addPage addPage()}, this method creates a new PDPage object. If your page has
     * annotations, and if these link to pages not in the target document, then the target document
     * might become huge. What you need to do is to delete page references of such annotations. See
     * <a href="http://stackoverflow.com/a/35477351/535646">here</a> for how to do this.
     * <p>
     * Inherited (global) resources are ignored because these can contain resources not needed for
     * this page which could bloat your document, see
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-28">PDFBOX-28</a> and related issues.
     * If you need them, call <code>importedPage.setResources(page.getResources());</code>
     * <p>
     * This method should only be used to import a page from a loaded document, not from a generated
     * document because these can contain unfinished parts, e.g. font subsetting information.
     *
     * @param page The page to import.
     * @return The page that was imported.
     *
     * @throws IOException If there is an error copying the page.
     */
    public PDPage importPage(PDPage page) throws IOException
    {
        PDPage importedPage = new PDPage(new COSDictionary(page.getCOSObject()), resourceCache);
        PDStream dest = new PDStream(this, page.getContents(), COSName.FLATE_DECODE);
        importedPage.setContents(dest);
        addPage(importedPage);
        importedPage.setCropBox(new PDRectangle(page.getCropBox().getCOSArray()));
        importedPage.setMediaBox(new PDRectangle(page.getMediaBox().getCOSArray()));
        importedPage.setRotation(page.getRotation());
        if (page.getResources() != null && !page.getCOSObject().containsKey(COSName.RESOURCES))
        {
            LOG.warn("inherited resources of source document are not imported to destination page");
            LOG.warn("call importedPage.setResources(page.getResources()) to do this");
        }
        return importedPage;
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
     * This will get the document info dictionary. If it doesn't exist, an empty document info
     * dictionary is created in the document trailer.
     * <p>
     * In PDF 2.0 this is deprecated except for two entries, /CreationDate and /ModDate. For any other
     * document level metadata, a metadata stream should be used instead, see
     * {@link PDDocumentCatalog#getMetadata()}.
     *
     * @return The documents /Info dictionary, never null.
     */
    public PDDocumentInformation getDocumentInformation()
    {
        if (documentInformation == null)
        {
            COSDictionary trailer = document.getTrailer();
            COSDictionary infoDic = trailer.getCOSDictionary(COSName.INFO);
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
     * <p>
     * In PDF 2.0 this is deprecated except for two entries, /CreationDate and /ModDate. For any other
     * document level metadata, a metadata stream should be used instead, see
     * {@link PDDocumentCatalog#setMetadata(org.apache.pdfbox.pdmodel.common.PDMetadata) PDDocumentCatalog#setMetadata(PDMetadata)}.
     *
     * @param info The updated document information.
     */
    public void setDocumentInformation(PDDocumentInformation info)
    {
        documentInformation = info;
        document.getTrailer().setItem(COSName.INFO, info.getCOSObject());
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
     * This will get the encryption dictionary for this document. This will still return the parameters if the document
     * was decrypted. As the encryption architecture in PDF documents is pluggable this returns an abstract class,
     * but the only supported subclass at this time is a
     * PDStandardEncryption object.
     *
     * @return The encryption dictionary(most likely a PDStandardEncryption object)
     */
    public PDEncryption getEncryption()
    {
        if (encryption == null && isEncrypted())
        {
            encryption = new PDEncryption(document.getEncryptionDictionary());
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
     * This will return the last signature from the field tree. Note that this may not be the
     * last in time when empty signature fields are created first but signed after other fields.
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
        List<PDSignatureField> fields = new ArrayList<PDSignatureField>();
        PDAcroForm acroForm = getDocumentCatalog().getAcroForm(null);
        if (acroForm != null)
        {
            for (PDField field : acroForm.getFieldTree())
            {
                if (field instanceof PDSignatureField)
                {
                    fields.add((PDSignatureField)field);
                }
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
        List<PDSignature> signatures = new ArrayList<PDSignature>();
        for (PDSignatureField field : getSignatureFields())
        {
            COSBase value = field.getCOSObject().getDictionaryObject(COSName.V);
            if (value != null)
            {
                signatures.add(new PDSignature((COSDictionary)value));
            }
        }
        return signatures;
    }

    /**
     * For internal PDFBox use when creating PDF documents: register a TrueTypeFont to make sure it
     * is closed when the PDDocument is closed to avoid memory leaks. Users don't have to call this
     * method, it is done by the appropriate PDFont classes.
     *
     * @param ttf
     */
    public void registerTrueTypeFontForClosing(TrueTypeFont ttf)
    {
        fontsToClose.add(ttf);
    }

    /**
     * Returns the list of fonts which will be subset before the document is saved.
     */
    Set<PDFont> getFontsToSubset()
    {
        return fontsToSubset;
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
    public static PDDocument load(File file, MemoryUsageSetting memUsageSetting)
            throws IOException
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
    public static PDDocument load(File file, String password)
            throws IOException
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
        @SuppressWarnings({"squid:S2095"}) // raFile not closed here, may be needed for signing
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
                                   InputStream keyStore, String alias,
                                   MemoryUsageSetting memUsageSetting) throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        try
        {
            PDFParser parser = new PDFParser(raFile, password, keyStore, alias, scratchFile);
            parser.parse();
            return parser.getPDDocument();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(scratchFile);
            throw ioe;
        }
    }

    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the
     * pdf. Unrestricted main memory will be used for buffering PDF streams.
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
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either
     * copied to main memory or to a temporary file to enable random access to the pdf.
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
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the
     * pdf. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     *
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument load(InputStream input, String password)
            throws IOException
    {
        return load(input, password, null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the
     * pdf. Unrestricted main memory will be used for buffering PDF streams.
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
    public static PDDocument load(InputStream input, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return load(input, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either
     * copied to main memory or to a temporary file to enable random access to the pdf.
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
    public static PDDocument load(InputStream input, String password, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        return load(input, password, null, null, memUsageSetting);
    }
    
    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either
     * copied to memory or to a temporary file to enable random access to the pdf.
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
            parser.parse();
            return parser.getPDDocument();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(scratchFile);
            throw ioe;
        }
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
    public static PDDocument load(byte[] input, String password)
            throws IOException
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
    public static PDDocument load(byte[] input, String password, InputStream keyStore, 
            String alias) throws IOException
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
    public static PDDocument load(byte[] input, String password, InputStream keyStore, 
            String alias, MemoryUsageSetting memUsageSetting) throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        RandomAccessRead source = new RandomAccessBuffer(input);
        PDFParser parser = new PDFParser(source, password, keyStore, alias, scratchFile);
        parser.parse();
        return parser.getPDDocument();
    }

    /**
     * Save the document to a file.
     * <p>
     * If encryption has been activated (with
     * {@link #protect(org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy) protect(ProtectionPolicy)}),
     * do not use the document after saving because the contents are now encrypted.
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
     * <p>
     * If encryption has been activated (with
     * {@link #protect(org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy) protect(ProtectionPolicy)}),
     * do not use the document after saving because the contents are now encrypted.
     * 
     * @param file The file to save as.
     *
     * @throws IOException if the output could not be written
     */
    public void save(File file) throws IOException
    {
        save(new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
     * This will save the document to an output stream.
     * <p>
     * If encryption has been activated (with
     * {@link #protect(org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy) protect(ProtectionPolicy)}),
     * do not use the document after saving because the contents are now encrypted.
     *
     * @param output The stream to write to. It will be closed when done. It is recommended to wrap
     * it in a {@link java.io.BufferedOutputStream}, unless it is already buffered.
     *
     * @throws IOException if the output could not be written
     */
    public void save(OutputStream output) throws IOException
    {
        if (document.isClosed())
        {
            throw new IOException("Cannot save a document which has been closed");
        }

        // subset designated fonts
        for (PDFont font : fontsToSubset)
        {
            font.subset();
        }
        fontsToSubset.clear();
        
        // save PDF
        COSWriter writer = new COSWriter(output);
        try
        {
            writer.write(this);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Save the PDF as an incremental update. This is only possible if the PDF was loaded from a
     * file or a stream, not if the document was created in PDFBox itself. There must be a path of
     * objects that have {@link COSUpdateInfo#isNeedToBeUpdated()} set, starting from the document
     * catalog. For signatures this is taken care by PDFBox itself.
     * <p>
     * Other usages of this method are for experienced users only. You will usually never need it.
     * It is useful only if you are required to keep the current revision and append the changes. A
     * typical use case is changing a signed file without invalidating the signature.
     *
     * @param output stream to write to. It will be closed when done. It
     * <i><b>must never</b></i> point to the source file or that one will be
     * harmed!
     * @throws IOException if the output could not be written
     * @throws IllegalStateException if the document was not loaded from a file or a stream.
     */
    
    public void saveIncremental(OutputStream output) throws IOException
    {
        COSWriter writer = null;
        try
        {
            if (pdfSource == null)
            {
                throw new IllegalStateException("document was not loaded from a file or a stream");
            }
            writer = new COSWriter(output, pdfSource);
            writer.write(this, signInterface);
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
     * Save the PDF as an incremental update. This is only possible if the PDF was loaded from a
     * file or a stream, not if the document was created in PDFBox itself. This allows to include
     * objects even if there is no path of objects that have
     * {@link COSUpdateInfo#isNeedToBeUpdated()} set so the incremental update gets smaller. Only
     * dictionaries are supported; if you need to update other objects classes, then add their
     * parent dictionary.
     * <p>
     * This method is for experienced users only. You will usually never need it. It is useful only
     * if you are required to keep the current revision and append the changes. A typical use case
     * is changing a signed file without invalidating the signature. To know which objects are
     * getting changed, you need to have some understanding of the PDF specification, and look at
     * the saved file with an editor to verify that you are updating the correct objects. You should
     * also inspect the page and document structures of the file with PDFDebugger.
     *
     * @param output stream to write to. It will be closed when done. It
     * <i><b>must never</b></i> point to the source file or that one will be harmed!
     * @param objectsToWrite objects that <b>must</b> be part of the incremental saving.
     * @throws IOException if the output could not be written
     * @throws IllegalStateException if the document was not loaded from a file or a stream.
     */
    public void saveIncremental(OutputStream output, Set<COSDictionary> objectsToWrite) throws IOException
    {
        if (pdfSource == null)
        {
            throw new IllegalStateException("document was not loaded from a file or a stream");
        }
        COSWriter writer = null;
        try
        {
            writer = new COSWriter(output, pdfSource, objectsToWrite);
            writer.write(this, signInterface);
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
     * <p>
     * <b>(This is a new feature for 2.0.3. The API for external signing might change based on feedback after release!)</b>
     * <p>
     * Save PDF incrementally without closing for external signature creation scenario. The general
     * sequence is:
     * <pre>
     *    PDDocument pdDocument = ...;
     *    OutputStream outputStream = ...;
     *    SignatureOptions signatureOptions = ...; // options to specify fine tuned signature options or null for defaults
     *    PDSignature pdSignature = ...;
     *
     *    // add signature parameters to be used when creating signature dictionary
     *    pdDocument.addSignature(pdSignature, signatureOptions);
     *    // prepare PDF for signing and obtain helper class to be used
     *    ExternalSigningSupport externalSigningSupport = pdDocument.saveIncrementalForExternalSigning(outputStream);
     *    // get data to be signed
     *    InputStream dataToBeSigned = externalSigningSupport.getContent();
     *    // invoke signature service
     *    byte[] signature = sign(dataToBeSigned);
     *    // set resulted CMS signature
     *    externalSigningSupport.setSignature(signature);
     *
     *    // last step is to close the document
     *    pdDocument.close();
     * </pre>
     * <p>
     * Note that after calling this method, only {@code close()} method may invoked for
     * {@code PDDocument} instance and only AFTER {@link ExternalSigningSupport} instance is used.
     * </p>
     *
     * @param output stream to write the final PDF. It will be closed when the
     * document is closed. It <i><b>must never</b></i> point to the source file
     * or that one will be harmed!
     * @return instance to be used for external signing and setting CMS signature
     * @throws IOException if the output could not be written
     * @throws IllegalStateException if the document was not loaded from a file or a stream or
     * signature options were not set.
     */
    public ExternalSigningSupport saveIncrementalForExternalSigning(OutputStream output) throws IOException
    {
        if (pdfSource == null)
        {
            throw new IllegalStateException("document was not loaded from a file or a stream");
        }
        // PDFBOX-3978: getLastSignatureDictionary() not helpful if signing into a template
        // that is not the last signature. So give higher priority to signature with update flag.
        PDSignature foundSignature = null;
        for (PDSignature sig : getSignatureDictionaries())
        {
            foundSignature = sig;
            if (sig.getCOSObject().isNeedToBeUpdated())
            {
                break;
            }
        }
        int[] byteRange = foundSignature.getByteRange();
        if (!Arrays.equals(byteRange, RESERVE_BYTE_RANGE))
        {
            throw new IllegalStateException("signature reserve byte range has been changed "
                    + "after addSignature(), please set the byte range that existed after addSignature()");
        }
        COSWriter writer = new COSWriter(output, pdfSource);
        writer.write(this);
        signingSupport = new SigningSupport(writer);
        return signingSupport;
    }

    /**
     * Returns the page at the given 0-based index.
     * <p>
     * This method is too slow to get all the pages from a large PDF document
     * (1000 pages or more). For such documents, use the iterator of
     * {@link PDDocument#getPages()} instead.
     *
     * @param pageIndex the 0-based page index
     * @return the page at the given index.
     */
    public PDPage getPage(int pageIndex) // todo: REPLACE most calls to this method with BELOW method
    {
        return getDocumentCatalog().getPages().get(pageIndex);
    }

    /**
     * Returns the page tree.
     * 
     * @return the page tree
     */
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
        if (!document.isClosed())
        {
            // Make sure that:
            // - first Exception is kept
            // - all IO resources are closed
            // - there's a way to see which errors occurred

            IOException firstException = null;

            // close resources and COSWriter
            if (signingSupport != null)
            {
                firstException = IOUtils.closeAndLogException(signingSupport, LOG, "SigningSupport", firstException);
            }

            // close all intermediate I/O streams
            firstException = IOUtils.closeAndLogException(document, LOG, "COSDocument", firstException);
            
            // close the source PDF stream, if we read from one
            if (pdfSource != null)
            {
                firstException = IOUtils.closeAndLogException(pdfSource, LOG, "RandomAccessRead pdfSource", firstException);
            }

            // close fonts
            for (TrueTypeFont ttf : fontsToClose)
            {
                firstException = IOUtils.closeAndLogException(ttf, LOG, "TrueTypeFont", firstException);
            }

            // rethrow first exception to keep method contract
            if (firstException != null)
            {
                throw firstException;
            }
        }
    }

    /**
     * Protects the document with a protection policy. The document content will be really
     * encrypted when it will be saved. This method only marks the document for encryption. It also
     * calls {@link #setAllSecurityToBeRemoved(boolean)} with a false argument if it was set to true
     * previously and logs a warning.
     * <p>
     * Do not use the document after saving, because the structures are encrypted.
     *
     * @see org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
     * @see org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy
     *
     * @param policy The protection policy.
     * @throws IOException if there isn't any suitable security handler.
     */
    public void protect(ProtectionPolicy policy) throws IOException
    {
        if (isAllSecurityToBeRemoved())
        {
            LOG.warn("do not call setAllSecurityToBeRemoved(true) before calling protect(), "
                    + "as protect() implies setAllSecurityToBeRemoved(false)");
            setAllSecurityToBeRemoved(false);
        }
        
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
     * @return the document ID
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
    
    /**
     * Returns the PDF specification version this document conforms to.
     *
     * @return the PDF version (e.g. 1.4f)
     */
    public float getVersion()
    {
        float headerVersionFloat = getDocument().getVersion();
        // there may be a second version information in the document catalog starting with 1.4
        if (headerVersionFloat >= 1.4f)
        {
            String catalogVersion = getDocumentCatalog().getVersion();
            float catalogVersionFloat = -1;
            if (catalogVersion != null)
            {
                try
                {
                    catalogVersionFloat = Float.parseFloat(catalogVersion);
                }
                catch(NumberFormatException exception)
                {
                    LOG.error("Can't extract the version number of the document catalog.", exception);
                }
            }
            // the most recent version is the correct one
            return Math.max(catalogVersionFloat, headerVersionFloat);
        }
        else
        {
            return headerVersionFloat;
        }
    }

    /**
     * Sets the PDF specification version for this document.
     *
     * @param newVersion the new PDF version (e.g. 1.4f)
     * 
     */
    public void setVersion(float newVersion)
    {
        float currentVersion = getVersion();
        // nothing to do?
        if (newVersion == currentVersion)
        {
            return;
        }
        // the version can't be downgraded
        if (newVersion < currentVersion)
        {
            LOG.error("It's not allowed to downgrade the version of a pdf.");
            return;
        }
        // update the catalog version if the document version is >= 1.4
        if (getDocument().getVersion() >= 1.4f)
        {
            getDocumentCatalog().setVersion(Float.toString(newVersion));
        }
        else
        {
            // versions < 1.4f have a version header only
            getDocument().setVersion(newVersion);
        }
    }

    /**
     * Returns the resource cache associated with this document, or null if there is none.
     * 
     * @return the resource cache or null.
     */
    public ResourceCache getResourceCache()
    {
        return resourceCache;
    }

    /**
     * Sets the resource cache associated with this document.
     * 
     * @param resourceCache A resource cache, or null.
     */
    public void setResourceCache(ResourceCache resourceCache)
    {
        this.resourceCache = resourceCache;
    }
}
