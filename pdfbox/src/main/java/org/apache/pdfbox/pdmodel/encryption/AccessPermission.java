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

/**
 * This class represents the access permissions to a document.
 * These permissions are specified in the PDF format specifications, they include:
 * <ul>
 * <li>print the document</li>
 * <li>modify the content of the document</li>
 * <li>copy or extract content of the document</li>
 * <li>add or modify annotations</li>
 * <li>fill in interactive form fields</li>
 * <li>extract text and graphics for accessibility to visually impaired people</li>
 * <li>assemble the document</li>
 * <li>print in degraded quality</li>
 * </ul>
 *
 * This class can be used to protect a document by assigning access permissions to recipients.
 * In this case, it must be used with a specific ProtectionPolicy.
 *
 *
 * When a document is decrypted, it has a currentAccessPermission property which is the access permissions
 * granted to the user who decrypted the document.
 *
 * @see ProtectionPolicy
 * @see org.apache.pdfbox.pdmodel.PDDocument#getCurrentAccessPermission()
 *
 * @author Ben Litchfield
 * @author Benoit Guillon
 *
 */

public class AccessPermission
{

    private static final int DEFAULT_PERMISSIONS = ~3; //bits 0 & 1 need to be zero
    private static final int PRINT_BIT = 3;
    private static final int MODIFICATION_BIT = 4;
    private static final int EXTRACT_BIT = 5;
    private static final int MODIFY_ANNOTATIONS_BIT = 6;
    private static final int FILL_IN_FORM_BIT = 9;
    private static final int EXTRACT_FOR_ACCESSIBILITY_BIT = 10;
    private static final int ASSEMBLE_DOCUMENT_BIT = 11;
    private static final int DEGRADED_PRINT_BIT = 12;

    private int bytes = DEFAULT_PERMISSIONS;

    private boolean readOnly = false;

    /**
     * Create a new access permission object.
     * By default, all permissions are granted.
     */
    public AccessPermission()
    {
        bytes = DEFAULT_PERMISSIONS;
    }

    /**
     * Create a new access permission object from a byte array.
     * Bytes are ordered most significant byte first.
     *
     * @param b the bytes as defined in PDF specs
     */

    public AccessPermission(byte[] b)
    {
        bytes = 0;
        bytes |= b[0] & 0xFF;
        bytes <<= 8;
        bytes |= b[1] & 0xFF;
        bytes <<= 8;
        bytes |= b[2] & 0xFF;
        bytes <<= 8;
        bytes |= b[3] & 0xFF;
    }

    /**
     * Creates a new access permission object from a single integer.
     *
     * @param permissions The permission bits.
     */
    public AccessPermission( int permissions )
    {
        bytes = permissions;
    }

    private boolean isPermissionBitOn( int bit )
    {
        return (bytes & (1 << (bit-1))) != 0;
    }

    private boolean setPermissionBit( int bit, boolean value )
    {
        int permissions = bytes;
        if( value )
        {
            permissions = permissions | (1 << (bit-1));
        }
        else
        {
            permissions = permissions & (~(1 << (bit - 1)));
        }
        bytes = permissions;

        return (bytes & (1 << (bit-1))) != 0;
    }




    /**
     * This will tell if the access permission corresponds to owner
     * access permission (no restriction).
     *
     * @return true if the access permission does not restrict the use of the document
     */
    public boolean isOwnerPermission()
    {
        return (this.canAssembleDocument()
                && this.canExtractContent()
                && this.canExtractForAccessibility()
                && this.canFillInForm()
                && this.canModify()
                && this.canModifyAnnotations()
                && this.canPrint()
                && this.canPrintDegraded()
                );
    }

    /**
     * returns an access permission object for a document owner.
     *
     * @return A standard owner access permission set.
     */

    public static AccessPermission getOwnerAccessPermission()
    {
        AccessPermission ret = new AccessPermission();
        ret.setCanAssembleDocument(true);
        ret.setCanExtractContent(true);
        ret.setCanExtractForAccessibility(true);
        ret.setCanFillInForm(true);
        ret.setCanModify(true);
        ret.setCanModifyAnnotations(true);
        ret.setCanPrint(true);
        ret.setCanPrintDegraded(true);
        return ret;
    }

    /**
     * This returns an integer representing the access permissions.
     * This integer can be used for public key encryption. This format
     * is not documented in the PDF specifications but is necessary for compatibility
     * with Adobe Acrobat and Adobe Reader.
     *
     * @return the integer representing access permissions
     */

    public int getPermissionBytesForPublicKey()
    {
        setPermissionBit(1, true);
        setPermissionBit(7, false);
        setPermissionBit(8, false);
        for(int i=13; i<=32; i++)
        {
            setPermissionBit(i, false);
        }
        return bytes;
    }

    /**
     * The returns an integer representing the access permissions.
     * This integer can be used for standard PDF encryption as specified
     * in the PDF specifications.
     *
     * @return the integer representing the access permissions
     */
    public int getPermissionBytes()
    {
        return bytes;
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowPrinting A boolean determining if the user can print.
     */
    public void setCanPrint( boolean allowPrinting )
    {
        if(!readOnly)
        {
            setPermissionBit( PRINT_BIT, allowPrinting );
        }
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowModifications A boolean determining if the user can modify the document.
     */
    public void setCanModify( boolean allowModifications )
    {
        if(!readOnly)
        {
            setPermissionBit( MODIFICATION_BIT, allowModifications );
        }
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowExtraction A boolean determining if the user can extract content
     *                        from the document.
     */
    public void setCanExtractContent( boolean allowExtraction )
    {
        if(!readOnly)
        {
            setPermissionBit( EXTRACT_BIT, allowExtraction );
        }
    }

    /**
     * This will tell if the user can add or modify text annotations and fill in interactive forms
     * fields and, if {@link #canModify() canModify()} returns true, create or modify interactive
     * form fields (including signature fields). Note that if
     * {@link #canFillInForm() canFillInForm()} returns true, it is still possible to fill in
     * interactive forms (including signature fields) even if this method here returns false.
     *
     * @return true If supplied with the user password they are allowed to modify annotations.
     */
    public boolean canModifyAnnotations()
    {
        return isPermissionBitOn( MODIFY_ANNOTATIONS_BIT );
    }

    /**
     * Set if the user can add or modify text annotations and fill in interactive forms fields and,
     * if {@link #canModify() canModify()} returns true, create or modify interactive form fields
     * (including signature fields). Note that if {@link #canFillInForm() canFillInForm()} returns
     * true, it is still possible to fill in interactive forms (including signature fields) even the
     * parameter here is false.
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowAnnotationModification A boolean determining the new setting.
     */
    public void setCanModifyAnnotations( boolean allowAnnotationModification )
    {
        if(!readOnly)
        {
            setPermissionBit( MODIFY_ANNOTATIONS_BIT, allowAnnotationModification );
        }
    }

    /**
     * This will tell if the user can fill in interactive form fields (including signature fields)
     * even if {@link #canModifyAnnotations() canModifyAnnotations()} returns false.
     *
     * @return true If supplied with the user password they are allowed to fill in form fields.
     */
    public boolean canFillInForm()
    {
        return isPermissionBitOn( FILL_IN_FORM_BIT );
    }

    /**
     * Set if the user can fill in interactive form fields (including signature fields) even if
     * {@link #canModifyAnnotations() canModifyAnnotations()} returns false. Therefore, if you want
     * to prevent a user from filling in interactive form fields, you need to call
     * {@link #setCanModifyAnnotations(boolean) setCanModifyAnnotations(false)} as well.
     *<p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowFillingInForm A boolean determining if the user can fill in interactive forms.
     */
    public void setCanFillInForm( boolean allowFillingInForm )
    {
        if(!readOnly)
        {
            setPermissionBit( FILL_IN_FORM_BIT, allowFillingInForm );
        }
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowExtraction A boolean determining if the user can extract content
     *                        from the document.
     */
    public void setCanExtractForAccessibility( boolean allowExtraction )
    {
        if(!readOnly)
        {
            setPermissionBit( EXTRACT_FOR_ACCESSIBILITY_BIT, allowExtraction );
        }
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param allowAssembly A boolean determining if the user can assemble the document.
     */
    public void setCanAssembleDocument( boolean allowAssembly )
    {
        if(!readOnly)
        {
            setPermissionBit( ASSEMBLE_DOCUMENT_BIT, allowAssembly );
        }
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
     * <p>
     * This method will have no effect if the object is in read only mode.
     *
     * @param canPrintDegraded A boolean determining if the user can print the
     *        document in a degraded format.
     */
    public void setCanPrintDegraded( boolean canPrintDegraded )
    {
        if(!readOnly)
        {
            setPermissionBit( DEGRADED_PRINT_BIT, canPrintDegraded );
        }
    }

    /**
     * Locks the access permission read only (ie, the setters will have no effects).
     * After that, the object cannot be unlocked.
     * This method is used for the currentAccessPermssion of a document to avoid
     * users to change access permission.
     */
    public void setReadOnly()
    {
        readOnly = true;
    }

    /**
     * This will tell if the object has been set as read only.
     *
     * @return true if the object is in read only mode.
     */

    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    /**
     * Indicates if any revision 3 access permission is set or not.
     * 
     * @return true if any revision 3 access permission is set
     */
    protected boolean hasAnyRevision3PermissionSet()
    {
        if (canFillInForm())
        {
            return true;
        }
        if (canExtractForAccessibility())
        {
            return true;
        }
        if (canAssembleDocument())
        {
            return true;
        }
        return canPrintDegraded();
    }
}
