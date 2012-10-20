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
package org.apache.pdfbox.pdmodel.encryption;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;

import java.io.IOException;

/**
 * This class holds information that is related to the standard PDF encryption.
 *
 * See PDF Reference 1.4 section "3.5 Encryption"
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 * @deprecated Made deprecated by the new security layer of PDFBox. Use SecurityHandlers instead.
 */
public class PDStandardEncryption extends PDEncryptionDictionary
{
    /**
     * The 'Filter' name for this security handler.
     */
    public static final String FILTER_NAME = "Standard";

    /**
     * The default revision of one is not specified.
     */
    public static final int DEFAULT_REVISION = 3;

    /**
     * Encryption revision 2.
     */
    public static final int REVISION2 = 2;
    /**
     * Encryption revision 3.
     */
    public static final int REVISION3 = 3;
    /**
     * Encryption revision 4.
     */
    public static final int REVISION4 = 4;

    /**
     * The default set of permissions which is to allow all.
     */
    public static final int DEFAULT_PERMISSIONS = 0xFFFFFFFF ^ 3;//bits 0 & 1 need to be zero

    private static final int PRINT_BIT = 3;
    private static final int MODIFICATION_BIT = 4;
    private static final int EXTRACT_BIT = 5;
    private static final int MODIFY_ANNOTATIONS_BIT = 6;
    private static final int FILL_IN_FORM_BIT = 9;
    private static final int EXTRACT_FOR_ACCESSIBILITY_BIT = 10;
    private static final int ASSEMBLE_DOCUMENT_BIT = 11;
    private static final int DEGRADED_PRINT_BIT = 12;

    /**
     * Default constructor that uses Version 2, Revision 3, 40 bit encryption,
     * all permissions allowed.
     */
    public PDStandardEncryption()
    {
        super();
        encryptionDictionary.setItem( COSName.FILTER, COSName.getPDFName( FILTER_NAME ) );
        setVersion( PDEncryptionDictionary.VERSION1_40_BIT_ALGORITHM  );
        setRevision( PDStandardEncryption.REVISION2 );
        setPermissions( DEFAULT_PERMISSIONS );
    }

    /**
     * Constructor from existing dictionary.
     *
     * @param dict The existing encryption dictionary.
     */
    public PDStandardEncryption( COSDictionary dict )
    {
        super( dict );
    }

    /**
     * This will return the R entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.14.
     *
     * @return The encryption revision to use.
     */
    public int getRevision()
    {
        int revision = DEFAULT_VERSION;
        COSNumber cosRevision = (COSNumber)encryptionDictionary.getDictionaryObject( COSName.getPDFName( "R" ) );
        if( cosRevision != null )
        {
            revision = cosRevision.intValue();
        }
        return revision;
    }

    /**
     * This will set the R entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.14.  <br /><br/>
     *
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param revision The new encryption version.
     */
    public void setRevision( int revision )
    {
        encryptionDictionary.setInt( COSName.getPDFName( "R" ), revision );
    }

    /**
     * This will get the O entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no owner key.
     */
    public byte[] getOwnerKey()
    {
       byte[] o = null;
       COSString owner = (COSString)encryptionDictionary.getDictionaryObject( COSName.getPDFName( "O" ) );
       if( owner != null )
       {
           o = owner.getBytes();
       }
       return o;
    }

    /**
     * This will set the O entry in the standard encryption dictionary.
     *
     * @param o A 32 byte array or null if there is no owner key.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setOwnerKey( byte[] o ) throws IOException
    {
       COSString owner = new COSString();
       owner.append( o );
       encryptionDictionary.setItem( COSName.getPDFName( "O" ), owner );
    }

    /**
     * This will get the U entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no user key.
     */
    public byte[] getUserKey()
    {
       byte[] u = null;
       COSString user = (COSString)encryptionDictionary.getDictionaryObject( COSName.getPDFName( "U" ) );
       if( user != null )
       {
           u = user.getBytes();
       }
       return u;
    }

    /**
     * This will set the U entry in the standard encryption dictionary.
     *
     * @param u A 32 byte array.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setUserKey( byte[] u ) throws IOException
    {
       COSString user = new COSString();
       user.append( u );
       encryptionDictionary.setItem( COSName.getPDFName( "U" ), user );
    }

    /**
     * This will get the permissions bit mask.
     *
     * @return The permissions bit mask.
     */
    public int getPermissions()
    {
        int permissions = 0;
        COSInteger p = (COSInteger)encryptionDictionary.getDictionaryObject( COSName.getPDFName( "P" ) );
        if( p != null )
        {
            permissions = p.intValue();
        }
        return permissions;
    }

    /**
     * This will set the permissions bit mask.
     *
     * @param p The new permissions bit mask
     */
    public void setPermissions( int p )
    {
        encryptionDictionary.setInt( COSName.getPDFName( "P" ), p );
    }

    private boolean isPermissionBitOn( int bit )
    {
        return (getPermissions() & (1 << (bit-1))) != 0;
    }

    private boolean setPermissionBit( int bit, boolean value )
    {
        int permissions = getPermissions();
        if( value )
        {
            permissions = permissions | (1 << (bit-1));
        }
        else
        {
            permissions = permissions & (0xFFFFFFFF ^ (1 << (bit-1)));
        }
        setPermissions( permissions );

        return (getPermissions() & (1 << (bit-1))) != 0;
    }

    /**
     * This will tell if the user can print.
     *
     * @return true If supplied with the user password they are allowed to print.
     */
    public boolean canPrint()
    {
        return isPermissionBitOn( PRINT_BIT );
    }

    /**
     * Set if the user can print.
     *
     * @param allowPrinting A boolean determining if the user can print.
     */
    public void setCanPrint( boolean allowPrinting )
    {
        setPermissionBit( PRINT_BIT, allowPrinting );
    }

    /**
     * This will tell if the user can modify contents of the document.
     *
     * @return true If supplied with the user password they are allowed to modify the document
     */
    public boolean canModify()
    {
        return isPermissionBitOn( MODIFICATION_BIT );
    }

    /**
     * Set if the user can modify the document.
     *
     * @param allowModifications A boolean determining if the user can modify the document.
     */
    public void setCanModify( boolean allowModifications )
    {
        setPermissionBit( MODIFICATION_BIT, allowModifications );
    }

    /**
     * This will tell if the user can extract text and images from the PDF document.
     *
     * @return true If supplied with the user password they are allowed to extract content
     *              from the PDF document
     */
    public boolean canExtractContent()
    {
        return isPermissionBitOn( EXTRACT_BIT );
    }

    /**
     * Set if the user can extract content from the document.
     *
     * @param allowExtraction A boolean determining if the user can extract content
     *                        from the document.
     */
    public void setCanExtractContent( boolean allowExtraction )
    {
        setPermissionBit( EXTRACT_BIT, allowExtraction );
    }

    /**
     * This will tell if the user can add/modify text annotations, fill in interactive forms fields.
     *
     * @return true If supplied with the user password they are allowed to modify annotations.
     */
    public boolean canModifyAnnotations()
    {
        return isPermissionBitOn( MODIFY_ANNOTATIONS_BIT );
    }

    /**
     * Set if the user can modify annotations.
     *
     * @param allowAnnotationModification A boolean determining if the user can modify annotations.
     */
    public void setCanModifyAnnotations( boolean allowAnnotationModification )
    {
        setPermissionBit( MODIFY_ANNOTATIONS_BIT, allowAnnotationModification );
    }

    /**
     * This will tell if the user can fill in interactive forms.
     *
     * @return true If supplied with the user password they are allowed to fill in form fields.
     */
    public boolean canFillInForm()
    {
        return isPermissionBitOn( FILL_IN_FORM_BIT );
    }

    /**
     * Set if the user can fill in interactive forms.
     *
     * @param allowFillingInForm A boolean determining if the user can fill in interactive forms.
     */
    public void setCanFillInForm( boolean allowFillingInForm )
    {
        setPermissionBit( FILL_IN_FORM_BIT, allowFillingInForm );
    }

    /**
     * This will tell if the user can extract text and images from the PDF document
     * for accessibility purposes.
     *
     * @return true If supplied with the user password they are allowed to extract content
     *              from the PDF document
     */
    public boolean canExtractForAccessibility()
    {
        return isPermissionBitOn( EXTRACT_FOR_ACCESSIBILITY_BIT );
    }

    /**
     * Set if the user can extract content from the document for accessibility purposes.
     *
     * @param allowExtraction A boolean determining if the user can extract content
     *                        from the document.
     */
    public void setCanExtractForAccessibility( boolean allowExtraction )
    {
        setPermissionBit( EXTRACT_FOR_ACCESSIBILITY_BIT, allowExtraction );
    }

    /**
     * This will tell if the user can insert/rotate/delete pages.
     *
     * @return true If supplied with the user password they are allowed to extract content
     *              from the PDF document
     */
    public boolean canAssembleDocument()
    {
        return isPermissionBitOn( ASSEMBLE_DOCUMENT_BIT );
    }

    /**
     * Set if the user can insert/rotate/delete pages.
     *
     * @param allowAssembly A boolean determining if the user can assemble the document.
     */
    public void setCanAssembleDocument( boolean allowAssembly )
    {
        setPermissionBit( ASSEMBLE_DOCUMENT_BIT, allowAssembly );
    }

    /**
     * This will tell if the user can print the document in a degraded format.
     *
     * @return true If supplied with the user password they are allowed to print the
     *              document in a degraded format.
     */
    public boolean canPrintDegraded()
    {
        return isPermissionBitOn( DEGRADED_PRINT_BIT );
    }

    /**
     * Set if the user can print the document in a degraded format.
     *
     * @param allowAssembly A boolean determining if the user can print the
     *        document in a degraded format.
     */
    public void setCanPrintDegraded( boolean allowAssembly )
    {
        setPermissionBit( DEGRADED_PRINT_BIT, allowAssembly );
    }
}
