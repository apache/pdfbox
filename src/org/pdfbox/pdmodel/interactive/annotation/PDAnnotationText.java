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
 * This is the class that represents a text annotation.
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public class PDAnnotationText extends PDAnnotationMarkup
{

    /*
     * The various values of the Text as defined in the PDF 1.6 reference Table
     * 8.19
     */

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_COMMENT = "Comment";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_KEY = "Key";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_NOTE = "Note";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_HELP = "Help";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_NEW_PARAGRAPH = "NewParagraph";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_PARAGRAPH = "Paragraph";

    /**
     * Constant for the name of a text annotation.
     */
    public static final String NAME_INSERT = "Insert";

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Text";

    /**
     * Constructor.
     */
    public PDAnnotationText()
    {
        super();
        getDictionary()
                .setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
    }

    /**
     * Creates a Text annotation from a COSDictionary, expected to be a correct
     * object definition.
     * 
     * @param field
     *            the PDF objet to represent as a field.
     */
    public PDAnnotationText( COSDictionary field )
    {
        super( field );
    }

    /**
     * This will set inital state of the annotation, open or closed.
     * 
     * @param open
     *            Boolean value, true = open false = closed
     */
    public void setOpen( boolean open )
    {
        getDictionary().setBoolean( COSName.getPDFName( "Open" ), open );
    }

    /**
     * This will retrieve the initial state of the annotation, open Or closed
     * (default closed).
     * 
     * @return The initial state, true = open false = closed
     */
    public boolean getOpen()
    {
        return getDictionary().getBoolean( COSName.getPDFName( "Open" ), false );
    }

    /**
     * This will set the name (and hence appearance, AP taking precedence) For
     * this annotation. See the NAME_XXX constants for valid values.
     * 
     * @param name
     *            The name of the annotation
     */
    public void setName( String name )
    {
        getDictionary().setName( COSName.NAME, name );
    }

    /**
     * This will retrieve the name (and hence appearance, AP taking precedence)
     * For this annotation. The default is NOTE.
     * 
     * @return The name of this annotation, see the NAME_XXX constants.
     */
    public String getName()
    {
        return getDictionary().getNameAsString( COSName.NAME, NAME_NOTE );
    }

}