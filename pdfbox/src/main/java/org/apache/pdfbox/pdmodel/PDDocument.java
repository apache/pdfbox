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

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandlersManager;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 * <p>
 * This class implements the {@link Pageable} interface, but since PDFBox
 * version 1.3.0 you should be using the {@link PDPageable} adapter instead
 * (see <a href="https://issues.apache.org/jira/browse/PDFBOX-788">PDFBOX-788</a>).
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.47 $
 */
public class PDDocument implements Pageable
{

    private COSDocument document;

    //cached values
    private PDDocumentInformation documentInformation;
    private PDDocumentCatalog documentCatalog;
    
    //The encParameters will be cached here.  When the document is decrypted then
    //the COSDocument will not have an "Encrypt" dictionary anymore and this object
    //must be used.
    private PDEncryptionDictionary encParameters = null;

    /**
     * The security handler used to decrypt / encrypt the document.
     */
    private SecurityHandler securityHandler = null;


    /**
     * This assocates object ids with a page number.  It's used to determine
     * the page number for bookmarks (or page numbers for anything else for
     * which you have an object id for that matter). 
     */
    private Map<String, Integer> pageMap = null;
    
    /**
     * This will hold a flag which tells us if we should remove all security
     * from this documents
     */
    private boolean allSecurityToBeRemoved = false;
    
    /**
     * Constructor, creates a new PDF Document with no pages.  You need to add
     * at least one page for the document to be valid.
     *
     * @throws IOException If there is an error creating this document.
     */
    public PDDocument() throws IOException
    {
        document = new COSDocument();

        //First we need a trailer
        COSDictionary trailer = new COSDictionary();
        document.setTrailer( trailer );

        //Next we need the root dictionary.
        COSDictionary rootDictionary = new COSDictionary();
        trailer.setItem( COSName.ROOT, rootDictionary );
        rootDictionary.setItem( COSName.TYPE, COSName.CATALOG );
        rootDictionary.setItem( COSName.VERSION, COSName.getPDFName( "1.4" ) );

        //next we need the pages tree structure
        COSDictionary pages = new COSDictionary();
        rootDictionary.setItem( COSName.PAGES, pages );
        pages.setItem( COSName.TYPE, COSName.PAGES );
        COSArray kidsArray = new COSArray();
        pages.setItem( COSName.KIDS, kidsArray );
        pages.setItem( COSName.COUNT, COSInteger.ZERO );
    }

    private void generatePageMap() 
    {
        pageMap = new HashMap<String,Integer>();
        // these page nodes could be references to pages, 
        // or references to arrays which have references to pages
        // or references to arrays which have references to arrays which have references to pages
        // or ... (I think you get the idea...)
        processListOfPageReferences(getDocumentCatalog().getPages().getKids());
    }
    
    private void processListOfPageReferences(List<Object> pageNodes)
    {
        for(int i=0; i < pageNodes.size(); ++i) 
        {
            Object pageOrArray = pageNodes.get(i);
            if(pageOrArray instanceof PDPage)
            {
                List pageArray = ((((PDPage)pageOrArray).getParent()).getKids());
                parseCatalogObject((COSObject)pageArray.get(i));
            }
            else if(pageOrArray instanceof PDPageNode)
            {
                processListOfPageReferences(((PDPageNode)pageOrArray).getKids());
            }
        }
    }
             
    /**
     * This will either add the page passed in, or, if it's a pointer to an array
     * of pages, it'll recursivly call itself and process everything in the list.
     */
    private void parseCatalogObject(COSObject thePageOrArrayObject) 
    {
        COSBase arrayCountBase = thePageOrArrayObject.getItem(COSName.COUNT);
        int arrayCount = -1;
        if(arrayCountBase instanceof COSInteger)
        {
            arrayCount = ((COSInteger)arrayCountBase).intValue();
        }
 
        COSBase kidsBase = thePageOrArrayObject.getItem(COSName.KIDS);
        int kidsCount = -1;
        if(kidsBase instanceof COSArray)
        {
            kidsCount = ((COSArray)kidsBase).size();
        }
     
        if(arrayCount == -1 || kidsCount == -1) 
        {
            // these cases occur when we have a page, not an array of pages
            String objStr = String.valueOf(thePageOrArrayObject.getObjectNumber().intValue());
            String genStr = String.valueOf(thePageOrArrayObject.getGenerationNumber().intValue());
            getPageMap().put(objStr+","+genStr, new Integer(getPageMap().size()+1));
        } 
        else 
        {
            // we either have an array of page pointers, or an array of arrays
            if(arrayCount == kidsCount) 
            {
                // process the kids... they're all references to pages
                COSArray kidsArray = ((COSArray)kidsBase);
                for(int i=0; i<kidsArray.size(); ++i) 
                {
                    COSObject thisObject = (COSObject)kidsArray.get(i);
                    String objStr = String.valueOf(thisObject.getObjectNumber().intValue());
                    String genStr = String.valueOf(thisObject.getGenerationNumber().intValue());
                    getPageMap().put(objStr+","+genStr, new Integer(getPageMap().size()+1));
                }
            } 
            else 
            {
                // this object is an array of references to other arrays
                COSArray list = null;
                if(kidsBase instanceof COSArray)
                {
                    list = ((COSArray)kidsBase);
                }
                if(list != null) 
                {
                    for(int arrayCounter=0; arrayCounter < list.size(); ++arrayCounter) 
                    {
                        parseCatalogObject((COSObject)list.get(arrayCounter));
                    }
                }
            }
        }
    }
 
    /**
     * This will return the Map containing the mapping from object-ids to pagenumbers.
     * 
     * @return the pageMap
     */
    public final Map<String,Integer> getPageMap() 
    {
        if (pageMap == null)
        {
            generatePageMap();
        }
        return pageMap;
    }

    /**
     * This will add a page to the document.  This is a convenience method, that
     * will add the page to the root of the hierarchy and set the parent of the
     * page to the root.
     *
     * @param page The page to add to the document.
     */
    public void addPage( PDPage page )
    {
        PDPageNode rootPages = getDocumentCatalog().getPages();
        rootPages.getKids().add( page );
        page.setParent( rootPages );
        rootPages.updateCount();
    }

    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface) throws IOException, SignatureException
    {
        SignatureOptions defaultOptions = new SignatureOptions();
        defaultOptions.setPage(1);
        addSignature(sigObject, signatureInterface,defaultOptions);
    }
    
    /**
     * This will add a signature to the document. 
     *
     * @param sigObject is the PDSignature model
     * @param signatureInterface is a interface which provides signing capabilities
     * @param options 
     * @throws IOException if there is an error creating required fields
     */
    public void addSignature(PDSignature sigObject, SignatureInterface signatureInterface, SignatureOptions options) throws IOException, SignatureException
    {
        // Reserve content
        // We need to reserve some space for the signature. Some signatures including
        // big certificate chain and we need enough space to store it.
        int preferedSignatureSize = options.getPreferedSignatureSize();
        if (preferedSignatureSize > 0)
        {
            sigObject.setContents(new byte[preferedSignatureSize * 2 + 2]);
        }
        else
        {
            sigObject.setContents(new byte[0x2500 * 2 + 2]);
        }
        
        // Reserve ByteRange
        sigObject.setByteRange(new int[] {0,1000000000,1000000000,1000000000});
      
        getDocument().setSignatureInterface(signatureInterface);
      
        // #########################################
        // # Create SignatureForm for signature    #
        // # and appending it to the document      #
        // #########################################

        // Get the first page
        PDDocumentCatalog root = getDocumentCatalog();
        PDPageNode rootPages = root.getPages();
        List<PDPage> kids = new ArrayList<PDPage>();
        rootPages.getAllKids(kids);
  
        int size = (int)rootPages.getCount();
        PDPage page = null;
        if (size == 0)
        {
            throw new SignatureException(SignatureException.INVALID_PAGE_FOR_SIGNATURE, "The PDF file has no pages");
        }
        if (options.getPage()>size)
        {
            page = kids.get(size-1);
        }
        else if(options.getPage()<=0)
        {
            page = kids.get(0);
        }
        else
        {
            page = kids.get(options.getPage()-1);
        }
      

        // Get the AcroForm from the Root-Dictionary and append the annotation
        PDAcroForm acroForm = root.getAcroForm();
        root.getCOSObject().setNeedToBeUpdate(true); 
        
        if (acroForm==null) 
        {
            acroForm = new PDAcroForm(this);
            root.setAcroForm(acroForm);
        } 
        else 
        {
            acroForm.getCOSObject().setNeedToBeUpdate(true);
        }
      
        /*
         * For invisible signatures, the annotation has a rectangle array with values [ 0 0 0 0 ]. 
         * This annotation is usually attached to the viewed page when the signature is created. 
         * Despite not having an appearance, the annotation AP and N dictionaries may be present 
         * in some versions of Acrobat. If present, N references the DSBlankXObj (blank) XObject.
         */

        // Create Annotation / Field for signature
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setSignature(sigObject);              // append the signature object
        signatureField.getWidget().setPage(page);            // backward linking
      
        // Set the AcroForm Fields
        List acroFormFields = acroForm.getFields();
        COSDictionary acroFormDict = acroForm.getDictionary();
        acroFormDict.setDirect(true);
        acroFormDict.setInt(COSName.SIG_FLAGS, 3);
        acroFormFields.add(signatureField);

        // Get the object from the visual signature
        COSDocument visualSignature = options.getVisualSignature();

        // Distinction of case for visual and non-visual signature
        if (visualSignature == null) // non-visual signature
        {
            // Set rectangle for non-visual signature to 0 0 0 0
            signatureField.getWidget().setRectangle(new PDRectangle());  // rectangle array [ 0 0 0 0 ]
            // Clear AcroForm / Set DefaultRessource
            acroFormDict.setItem(COSName.DR, null);
            // Set empty Appearance-Dictionary
            PDAppearanceDictionary ap = new PDAppearanceDictionary();
            COSStream apsStream = new COSStream(getDocument().getScratchFile());
            apsStream.createUnfilteredStream();
            PDAppearanceStream aps = new PDAppearanceStream(apsStream);
            COSDictionary cosObject = (COSDictionary)aps.getCOSObject();
            cosObject.setItem(COSName.SUBTYPE, COSName.FORM);
            cosObject.setItem(COSName.BBOX, new PDRectangle());
            
            ap.setNormalAppearance(aps);
            ap.getDictionary().setDirect(true);
            signatureField.getWidget().setAppearance(ap);
        }
        else // visual signature
        {
            // Obtain visual signature object
            List<COSObject> cosObjects = visualSignature.getObjects();
    
            boolean annotNotFound = true;
            boolean sigFieldNotFound = true;

            for ( COSObject cosObject : cosObjects )
            {
                COSBase base = cosObject.getObject();
                if (base != null && base instanceof COSDictionary)
                {
                    COSBase ft = ((COSDictionary)base).getItem(COSName.FT);
                    COSBase type = ((COSDictionary)base).getItem(COSName.TYPE);
                    COSBase apDict = ((COSDictionary)base).getItem(COSName.AP);
            
                    // Search for signature annotation
                    if (annotNotFound && COSName.ANNOT.equals(type))
                    {
                        COSDictionary cosBaseDict = (COSDictionary)base;
              
                        // Read and set the Rectangle for visual signature
                        COSArray rectAry = (COSArray)cosBaseDict.getItem(COSName.RECT);
                        PDRectangle rect = new PDRectangle(rectAry);
                        signatureField.getWidget().setRectangle(rect);
                        annotNotFound = false;
                    }
            
                    // Search for Signature-Field
                    if (sigFieldNotFound && COSName.SIG.equals(ft) && apDict != null)
                    {
                        COSDictionary cosBaseDict = (COSDictionary)base;
              
                        // Appearance Dictionary auslesen und setzen
                        PDAppearanceDictionary ap = new PDAppearanceDictionary((COSDictionary)cosBaseDict.getItem(COSName.AP));
                        ap.getDictionary().setDirect(true);
                        signatureField.getWidget().setAppearance(ap);
              
                        // AcroForm DefaultRessource auslesen und setzen
                        COSBase dr = cosBaseDict.getItem(COSName.DR);
                        dr.setDirect(true);
                        dr.setNeedToBeUpdate(true);
                        acroFormDict.setItem(COSName.DR, dr);
                        sigFieldNotFound=false;
                    }
                }
            }
        
            if (annotNotFound || sigFieldNotFound )
            {
                throw new SignatureException(SignatureException.VISUAL_SIGNATURE_INVALID, "Could not read all needed objects from template");
            }
        }
      
        // Get the annotations of the page and append the signature-annotation to it
        List annotations = page.getAnnotations();
        if (annotations == null) 
        {
            annotations = new COSArrayList();
            page.setAnnotations(annotations);
        }
        // take care that page and acroforms do not share the same array (if so, we don't need to add it twice)
        if (!(annotations instanceof COSArrayList) 
                || !(acroFormFields instanceof COSArrayList) 
                || !((COSArrayList)annotations).toList().equals(((COSArrayList)acroFormFields).toList()))
        {
            annotations.add(signatureField.getWidget());
        }
        page.getCOSObject().setNeedToBeUpdate(true);
    }

    /**
     * Remove the page from the document.
     *
     * @param page The page to remove from the document.
     *
     * @return true if the page was found false otherwise.
     */
    public boolean removePage( PDPage page )
    {
        PDPageNode parent = page.getParent();
        boolean retval = parent.getKids().remove( page );
        if( retval )
        {
            //do a recursive updateCount starting at the root
            //of the document
            getDocumentCatalog().getPages().updateCount();
        }
        return retval;
    }

    /**
     * Remove the page from the document.
     *
     * @param pageNumber 0 based index to page number.
     * @return true if the page was found false otherwise.
     */
    public boolean removePage( int pageNumber )
    {
        boolean removed = false;
        List allPages = getDocumentCatalog().getAllPages();
        if( allPages.size() > pageNumber)
        {
            PDPage page = (PDPage)allPages.get( pageNumber );
            removed = removePage( page );
        }
        return removed;
    }

    /**
     * This will import and copy the contents from another location.  Currently
     * the content stream is stored in a scratch file.  The scratch file is
     * associated with the document.  If you are adding a page to this document
     * from another document and want to copy the contents to this document's
     * scratch file then use this method otherwise just use the addPage method.
     *
     * @param page The page to import.
     * @return The page that was imported.
     *
     * @throws IOException If there is an error copying the page.
     */
    public PDPage importPage( PDPage page ) throws IOException
    {
        PDPage importedPage = new PDPage( new COSDictionary( page.getCOSDictionary() ) );
        InputStream is = null;
        OutputStream os = null;
        try
        {
            PDStream src = page.getContents();
            if(src != null)
            {
                PDStream dest = new PDStream( new COSStream( src.getStream(), document.getScratchFile() ) );
                importedPage.setContents( dest );
                os = dest.createOutputStream();

                byte[] buf = new byte[10240];
                int amountRead = 0;
                is = src.createInputStream();
                while((amountRead = is.read(buf,0,10240)) > -1)
                {
                    os.write(buf, 0, amountRead);
                }
            }
            addPage( importedPage );
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
            if( os != null )
            {
                os.close();
            }
        }
        return importedPage;

    }

    /**
     * Constructor that uses an existing document.  The COSDocument that
     * is passed in must be valid.
     *
     * @param doc The COSDocument that this document wraps.
     */
    public PDDocument( COSDocument doc )
    {
        document = doc;
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
     * This will get the document info dictionary.  This is guaranteed to not return null.
     *
     * @return The documents /Info dictionary
     */
    public PDDocumentInformation getDocumentInformation()
    {
        if( documentInformation == null )
        {
            COSDictionary trailer = document.getTrailer();
            COSDictionary infoDic = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
            if( infoDic == null )
            {
                infoDic = new COSDictionary();
                trailer.setItem( COSName.INFO, infoDic );
            }
            documentInformation = new PDDocumentInformation( infoDic );
        }
        return documentInformation;
    }

    /**
     * This will set the document information for this document.
     *
     * @param info The updated document information.
     */
    public void setDocumentInformation( PDDocumentInformation info )
    {
        documentInformation = info;
        document.getTrailer().setItem( COSName.INFO, info.getDictionary() );
    }

    /**
     * This will get the document CATALOG.  This is guaranteed to not return null.
     *
     * @return The documents /Root dictionary
     */
    public PDDocumentCatalog getDocumentCatalog()
    {
        if( documentCatalog == null )
        {
            COSDictionary trailer = document.getTrailer();
            COSBase dictionary = trailer.getDictionaryObject( COSName.ROOT );
            if (dictionary instanceof COSDictionary) {
                documentCatalog =
                    new PDDocumentCatalog(this, (COSDictionary) dictionary);
            } else {
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
     * This will get the encryption dictionary for this document.  This will still
     * return the parameters if the document was decrypted.  If the document was
     * never encrypted then this will return null.  As the encryption architecture
     * in PDF documents is plugable this returns an abstract class, but the only
     * supported subclass at this time is a PDStandardEncryption object.
     *
     * @return The encryption dictionary(most likely a PDStandardEncryption object)
     *
     * @throws IOException If there is an error determining which security handler to use.
     */
    public PDEncryptionDictionary getEncryptionDictionary() throws IOException
    {
        if( encParameters == null )
        {
            if( isEncrypted() )
            {
                encParameters = new PDEncryptionDictionary(document.getEncryptionDictionary());
            }
        }
        return encParameters;
    }

    /**
     * This will set the encryption dictionary for this document.
     *
     * @param encDictionary The encryption dictionary(most likely a PDStandardEncryption object)
     *
     * @throws IOException If there is an error determining which security handler to use.
     */
    public void setEncryptionDictionary( PDEncryptionDictionary encDictionary ) throws IOException
    {
        encParameters = encDictionary;
    }

    public PDSignature getSignatureDictionary() throws IOException 
    {
      COSDictionary signatureDictionary = document.getLastSignatureDictionary();

      if (signatureDictionary!= null)
      {
        return new PDSignature(signatureDictionary);
      }
      return null;
    }
    
    /**
     * This will determine if this is the user password.  This only applies when
     * the document is encrypted and uses standard encryption.
     *
     * @param password The plain text user password.
     *
     * @return true If the password passed in matches the user password used to encrypt the document.
     *
     * @throws IOException If there is an error determining if it is the user password.
     * @throws CryptographyException If there is an error in the encryption algorithms.
     *
     * @deprecated
     */
    @Deprecated
    public boolean isUserPassword( String password ) throws IOException, CryptographyException
    {
            return false;
        /*boolean retval = false;
        if( password == null )
        {
            password = "";
        }
        PDFEncryption encryptor = new PDFEncryption();
        PDEncryptionDictionary encryptionDictionary = getEncryptionDictionary();
        if( encryptionDictionary == null )
        {
            throw new IOException( "Error: Document is not encrypted" );
        }
        else
        {
            if( encryptionDictionary instanceof PDStandardEncryption )
            {
                COSString documentID = (COSString)document.getDocumentID().get(0);
                PDStandardEncryption standard = (PDStandardEncryption)encryptionDictionary;
                retval = encryptor.isUserPassword(
                    password.getBytes("ISO-8859-1"),
                    standard.getUserKey(),
                    standard.getOwnerKey(),
                    standard.getPermissions(),
                    documentID.getBytes(),
                    standard.getRevision(),
                    standard.getLength()/8 );
            }
            else
            {
                throw new IOException( "Error: Encyption dictionary is not 'Standard'" +
                    encryptionDictionary.getClass().getName() );
            }
        }
        return retval;*/
    }

    /**
     * This will determine if this is the owner password.  This only applies when
     * the document is encrypted and uses standard encryption.
     *
     * @param password The plain text owner password.
     *
     * @return true If the password passed in matches the owner password used to encrypt the document.
     *
     * @throws IOException If there is an error determining if it is the user password.
     * @throws CryptographyException If there is an error in the encryption algorithms.
     *
     * @deprecated
     */
    @Deprecated
    public boolean isOwnerPassword( String password ) throws IOException, CryptographyException
    {
            return false;
        /*boolean retval = false;
        if( password == null )
        {
            password = "";
        }
        PDFEncryption encryptor = new PDFEncryption();
        PDEncryptionDictionary encryptionDictionary = getEncryptionDictionary();
        if( encryptionDictionary == null )
        {
            throw new IOException( "Error: Document is not encrypted" );
        }
        else
        {
            if( encryptionDictionary instanceof PDStandardEncryption )
            {
                COSString documentID = (COSString)document.getDocumentID().get( 0 );
                PDStandardEncryption standard = (PDStandardEncryption)encryptionDictionary;
                retval = encryptor.isOwnerPassword(
                    password.getBytes("ISO-8859-1"),
                    standard.getUserKey(),
                    standard.getOwnerKey(),
                    standard.getPermissions(),
                    documentID.getBytes(),
                    standard.getRevision(),
                    standard.getLength()/8 );
            }
            else
            {
                throw new IOException( "Error: Encyption dictionary is not 'Standard'" +
                    encryptionDictionary.getClass().getName() );
            }
        }
        return retval;*/
    }

    /**
     * This will decrypt a document. This method is provided for compatibility reasons only. User should use
     * the new security layer instead and the openProtection method especially.
     *
     * @param password Either the user or owner password.
     *
     * @throws CryptographyException If there is an error decrypting the document.
     * @throws IOException If there is an error getting the stream data.
     * @throws InvalidPasswordException If the password is not a user or owner password.
     *
     */
    public void decrypt( String password ) throws CryptographyException, IOException, InvalidPasswordException
    {
        try
        {
            StandardDecryptionMaterial m = new StandardDecryptionMaterial(password);
            this.openProtection(m);
            document.dereferenceObjectStreams();
        }
        catch(BadSecurityHandlerException e)
        {
            throw new CryptographyException(e);
        }
    }

    /**
     * This will tell if the document was decrypted with the master password.  This
     * entry is invalid if the PDF was not decrypted.
     *
     * @return true if the pdf was decrypted with the master password.
     *
     * @deprecated use <code>getCurrentAccessPermission</code> instead
     */
    @Deprecated
    public boolean wasDecryptedWithOwnerPassword()
    {
        return false;
    }

    /**
     * This will <b>mark</b> a document to be encrypted.  The actual encryption
     * will occur when the document is saved.
     * This method is provided for compatibility reasons only. User should use
     * the new security layer instead and the openProtection method especially.
     *
     * @param ownerPassword The owner password to encrypt the document.
     * @param userPassword The user password to encrypt the document.
     *
     * @throws CryptographyException If an error occurs during encryption.
     * @throws IOException If there is an error accessing the data.
     *
     */
    public void encrypt( String ownerPassword, String userPassword )
        throws CryptographyException, IOException
    {
        try
        {
            StandardProtectionPolicy policy =
                new StandardProtectionPolicy(ownerPassword, userPassword, new AccessPermission());
            this.protect(policy);
        }
        catch(BadSecurityHandlerException e)
        {
            throw new CryptographyException(e);
        }
    }


    /**
     * The owner password that was passed into the encrypt method. You should
     * never use this method.  This will not longer be valid once encryption
     * has occured.
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
     * The user password that was passed into the encrypt method.  You should
     * never use this method.  This will not longer be valid once encryption
     * has occured.
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
     * Internal method do determine if the document will be encrypted when it is saved.
     *
     * @return True if encrypt has been called and the document
     *              has not been saved yet.
     *
     * @deprecated Do not rely on this method anymore. It is the responsibility of
     * COSWriter to hold this state
     */
    @Deprecated
    public boolean willEncryptWhenSaving()
    {
        return false;
    }

    /**
     * This shoule only be called by the COSWriter after encryption has completed.
     *
     * @deprecated Do not rely on this method anymore. It is the responsability of
     * COSWriter to hold this state.
     */
    @Deprecated
    public void clearWillEncryptWhenSaving()
    {
        //method is deprecated.
    }

    /**
     * This will load a document from a url.
     *
     * @param url The url to load the PDF from.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( URL url ) throws IOException
    {
        return load( url.openStream() );
    }
    /**
     * This will load a document from a url. Used for skipping corrupt
     * pdf objects
     *
     * @param url The url to load the PDF from.
     * @param force When true, the parser will skip corrupt pdf objects and 
     * will continue parsing at the next object in the file
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load(URL url, boolean force) throws IOException
    {
        return load(url.openStream(), force);
    }

    /**
     * This will load a document from a url.
     *
     * @param url The url to load the PDF from.
     * @param scratchFile A location to store temp PDFBox data for this document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( URL url, RandomAccess scratchFile ) throws IOException
    {
        return load( url.openStream(), scratchFile );
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( String filename ) throws IOException
    {
        return load( new FileInputStream( filename ) );
    }
    
    /**
     * This will load a document from a file. Allows for skipping corrupt pdf
     * objects
     *
     * @param filename The name of the file to load.
     * @param force When true, the parser will skip corrupt pdf objects and 
     * will continue parsing at the next object in the file
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load(String filename, boolean force) throws IOException
    {
        return load(new FileInputStream( filename ), force);
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     * @param scratchFile A location to store temp PDFBox data for this document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( String filename, RandomAccess scratchFile ) throws IOException
    {
        return load( new FileInputStream( filename ), scratchFile );
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
    public static PDDocument load( File file ) throws IOException
    {
        return load( new FileInputStream( file ) );
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     * @param scratchFile A location to store temp PDFBox data for this document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( File file, RandomAccess scratchFile ) throws IOException
    {
        return load( new FileInputStream( file ), scratchFile );
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
    public static PDDocument load( InputStream input ) throws IOException
    {
        return load( input, null );
    }

    /**
     * This will load a document from an input stream.
     * Allows for skipping corrupt pdf objects
     *
     * @param input The stream that contains the document.
     * @param force When true, the parser will skip corrupt pdf objects and 
     * will continue parsing at the next object in the file
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load(InputStream input, boolean force) throws IOException
    {
        return load(input, null, force);
    }
    
    /**
     * This will load a document from an input stream.
     *
     * @param input The stream that contains the document.
     * @param scratchFile A location to store temp PDFBox data for this document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( InputStream input, RandomAccess scratchFile ) throws IOException
    {
        PDFParser parser = new PDFParser( new BufferedInputStream( input ) , scratchFile );
        parser.parse();
        return parser.getPDDocument();
    }
    
    /**
     * This will load a document from an input stream. Allows for skipping corrupt pdf objects
     * 
     * @param input The stream that contains the document.
     * @param scratchFile A location to store temp PDFBox data for this document.
     * @param force When true, the parser will skip corrupt pdf objects and 
     * will continue parsing at the next object in the file
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load(InputStream input, RandomAccess scratchFile, boolean force) throws IOException
    {
        PDFParser parser = new PDFParser( new BufferedInputStream( input ), scratchFile, force);
        parser.parse();
        return parser.getPDDocument();
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void save( String fileName ) throws IOException, COSVisitorException
    {
        save( new FileOutputStream( fileName ) );
    }

    /**
     * This will save the document to an output stream.
     *
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing the document.
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void save( OutputStream output ) throws IOException, COSVisitorException
    {
        //update the count in case any pages have been added behind the scenes.
        getDocumentCatalog().getPages().updateCount();
        COSWriter writer = null;
        try
        {
            writer = new COSWriter( output );
            writer.write( this );
            writer.close();
        }
        finally
        {
            if( writer != null )
            {
                writer.close();
            }
        }
    }

    public void saveIncremental( String fileName ) throws IOException, COSVisitorException
    {
        saveIncremental( new FileInputStream( fileName ) , new FileOutputStream( fileName , true) );
    }
    
    public void saveIncremental( FileInputStream input, OutputStream output ) throws IOException, COSVisitorException
    {
        //update the count in case any pages have been added behind the scenes.
        getDocumentCatalog().getPages().updateCount();
        COSWriter writer = null;
        try
        {
            // Sometimes the original file will be missing a newline at the end
            // In order to avoid having %%EOF the first object on the same line
            // as the %%EOF, we put a newline here.  If there's already one at
            // the end of the file, an extra one won't hurt. PDFBOX-1051
            output.write("\r\n".getBytes());
            writer = new COSWriter( output, input );
            writer.write( this );
            writer.close();
        }
        finally
        {
            if( writer != null )
            {
                writer.close();
            }
        }
    }

    /**
     * This will return the total page count of the PDF document.  Note: This method
     * is deprecated in favor of the getNumberOfPages method.  The getNumberOfPages is
     * a required interface method of the Pageable interface.  This method will
     * be removed in a future version of PDFBox!!
     *
     * @return The total number of pages in the PDF document.
     * @deprecated Use the getNumberOfPages method instead!
     */
    @Deprecated
    public int getPageCount()
    {
        return getNumberOfPages();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfPages()
    {
        PDDocumentCatalog cat = getDocumentCatalog();
        return (int)cat.getPages().getCount();
    }

    /**
     * Returns the format of the page at the given index when using a
     * default printer job returned by  {@link PrinterJob#getPrinterJob()}.
     *
     * @deprecated Use the {@link PDPageable} adapter class
     * @param i page index, zero-based
     * @return page format
     * @throws IndexOutOfBoundsException if the page index is invalid
     */
    @Deprecated
    public PageFormat getPageFormat(int pageIndex)
    {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            return new PDPageable(this, printerJob).getPageFormat(pageIndex);
        } catch (PrinterException e) {
            throw new RuntimeException(e);
        }
    } 

    /**
     * {@inheritDoc}
     */
    public Printable getPrintable(int pageIndex)
    {
        return (Printable)getDocumentCatalog().getAllPages().get( pageIndex );
    }

    /**
     * @see PDDocument#print()
     *
     * @param printJob The printer job.
     *
     * @throws PrinterException If there is an error while sending the PDF to
     * the printer, or you do not have permissions to print this document.
     */
    public void print(PrinterJob printJob) throws PrinterException
    {
        print(printJob, false);
    }

    /**
     * This will send the PDF document to a printer.  The printing functionality
     * depends on the org.apache.pdfbox.pdfviewer.PageDrawer functionality.  The PageDrawer
     * is a work in progress and some PDFs will print correctly and some will
     * not.  This is a convenience method to create the java.awt.print.PrinterJob.
     * The PDDocument implements the java.awt.print.Pageable interface and
     * PDPage implementes the java.awt.print.Printable interface, so advanced printing
     * capabilities can be done by using those interfaces instead of this method.
     *
     * @throws PrinterException If there is an error while sending the PDF to
     * the printer, or you do not have permissions to print this document.
     */
    public void print() throws PrinterException
    {
        print( PrinterJob.getPrinterJob() );
    }

    /**
     * This will send the PDF to the default printer without prompting the user
     * for any printer settings.
     *
     * @see PDDocument#print()
     *
     * @throws PrinterException If there is an error while printing.
     */
    public void silentPrint() throws PrinterException
    {
        silentPrint( PrinterJob.getPrinterJob() );
    }

    /**
     * This will send the PDF to the default printer without prompting the user
     * for any printer settings.
     *
     * @param printJob A printer job definition.
     * @see PDDocument#print()
     *
     * @throws PrinterException If there is an error while printing.
     */
    public void silentPrint( PrinterJob printJob ) throws PrinterException
    {
        print(printJob, true);
    }

    private void print(PrinterJob job, boolean silent) throws PrinterException {
        if (job == null) {
            throw new PrinterException("The given printer job is null.");
        } else {
            job.setPageable(new PDPageable(this, job));
            if (silent || job.printDialog()) {
                job.print();
            }
        }
    }

    /**
     * This will close the underlying COSDocument object.
     *
     * @throws IOException If there is an error releasing resources.
     */
    public void close() throws IOException
    {
        document.close();
    }


    /**
     * Protects the document with the protection policy pp. The document content will be really encrypted
     * when it will be saved. This method only marks the document for encryption.
     *
     * @see org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
     * @see org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy
     *
     * @param pp The protection policy.
     *
     * @throws BadSecurityHandlerException If there is an error during protection.
     */
    public void protect(ProtectionPolicy pp) throws BadSecurityHandlerException
    {
        SecurityHandler handler = SecurityHandlersManager.getInstance().getSecurityHandler(pp);
        securityHandler = handler;
    }

    /**
     * Tries to decrypt the document in memory using the provided decryption material.
     *
     *  @see org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial
     *  @see org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial
     *
     * @param pm The decryption material (password or certificate).
     *
     * @throws BadSecurityHandlerException If there is an error during decryption.
     * @throws IOException If there is an error reading cryptographic information.
     * @throws CryptographyException If there is an error during decryption.
     */
    public void openProtection(DecryptionMaterial pm)
        throws BadSecurityHandlerException, IOException, CryptographyException
    {
        PDEncryptionDictionary dict = this.getEncryptionDictionary();
        if(dict.getFilter() != null)
        {
            securityHandler = SecurityHandlersManager.getInstance().getSecurityHandler(dict.getFilter());
            securityHandler.decryptDocument(this, pm);
            document.dereferenceObjectStreams();
            document.setEncryptionDictionary( null );
        }
        else
        {
            throw new RuntimeException("This document does not need to be decrypted");
        }
    }

    /**
     * Returns the access permissions granted when the document was decrypted.
     * If the document was not decrypted this method returns the access permission
     * for a document owner (ie can do everything).
     * The returned object is in read only mode so that permissions cannot be changed.
     * Methods providing access to content should rely on this object to verify if the current
     * user is allowed to proceed.
     *
     * @return the access permissions for the current user on the document.
     */

    public AccessPermission getCurrentAccessPermission()
    {
        if(this.securityHandler == null)
        {
            return AccessPermission.getOwnerAccessPermission();
        }
        return securityHandler.getCurrentAccessPermission();
    }

    /**
     * Get the security handler that is used for document encryption.
     *
     * @return The handler used to encrypt/decrypt the document.
     */
    public SecurityHandler getSecurityHandler()
    {
        return securityHandler;
    }

    public boolean isAllSecurityToBeRemoved() {
        return allSecurityToBeRemoved;
    }

    public void setAllSecurityToBeRemoved(boolean allSecurityToBeRemoved) {
        this.allSecurityToBeRemoved = allSecurityToBeRemoved;
    }

}
