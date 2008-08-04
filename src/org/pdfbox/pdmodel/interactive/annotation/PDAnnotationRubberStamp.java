/**
 * Copyright (c) 2003, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.interactive.annotation;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

/**
 * This is the class that represents a rubber stamp annotation.
 * Introduced in PDF 1.3 specification
 * 
 * @author Paul King
 * @version $Revision: 1.2 $
 */
public class PDAnnotationRubberStamp extends PDAnnotationMarkup
{
    
    /*
     * The various values of the rubber stamp as defined in 
     * the PDF 1.6 reference Table 8.28
     */
    
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_APPROVED = "Approved";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_EXPERIMENTAL = "Experimental";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_NOT_APPROVED = "NotApproved";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_AS_IS = "AsIs";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_EXPIRED = "Expired";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_NOT_FOR_PUBLIC_RELEASE = "NotForPublicRelease";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FOR_PUBLIC_RELEASE = "ForPublicRelease";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_DRAFT = "Draft";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FOR_COMMENT = "ForComment";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_TOP_SECRET = "TopSecret";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_DEPARTMENTAL = "Departmental";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_CONFIDENTIAL = "Confidential";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FINAL = "Final";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_SOLD = "Sold";
    
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Stamp";
    
    /**
     * Constructor.
     */
    public PDAnnotationRubberStamp()
    {
        super();
        getDictionary().setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
    }

    /**
     * Creates a Rubber Stamp annotation from a COSDictionary, expected to be
     * a correct object definition.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationRubberStamp(COSDictionary field)
    {
        super( field );
    }

    /**
     * This will set the name (and hence appearance, AP taking precedence)
     * For this annotation.   See the NAME_XXX constants for valid values.
     *
     * @param name The name of the rubber stamp.
     */
    public void setName( String name )
    {
        getDictionary().setName(COSName.NAME, name);
    }

    /**
     * This will retrieve the name (and hence appearance, AP taking precedence)
     * For this annotation.  The default is DRAFT.
     * 
     * @return The name of this rubber stamp, see the NAME_XXX constants.
     */
    public String getName()
    {
        return getDictionary().getNameAsString(COSName.NAME, NAME_DRAFT);
    }
}