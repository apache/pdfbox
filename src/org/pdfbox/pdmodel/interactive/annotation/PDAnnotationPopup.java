/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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

import java.io.IOException;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

/**
 * This is the class that represents a popup annotation.
 * Introduced in PDF 1.3 specification
 * 
 * @author Paul King
 * @version $Revision: 1.2 $
 */
public class PDAnnotationPopup extends PDAnnotation
{

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Popup";

    /**
     * Constructor.
     */
    public PDAnnotationPopup()
    {
        super();
        getDictionary()
                .setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
    }

    /**
     * Creates a popup annotation from a COSDictionary, expected to be a correct
     * object definition.
     * 
     * @param field
     *            the PDF objet to represent as a field.
     */
    public PDAnnotationPopup( COSDictionary field )
    {
        super( field );
    }

    /**
     * This will set inital state of the annotation, open or closed.
     * 
     * @param open
     *            Boolean value, true = open false = closed.
     */
    public void setOpen( boolean open )
    {
        getDictionary().setBoolean( "Open" , open );
    }

    /**
     * This will retrieve the initial state of the annotation, open Or closed
     * (default closed).
     * 
     * @return The initial state, true = open false = closed.
     */
    public boolean getOpen()
    {
        return getDictionary().getBoolean( "Open" , false );
    }

    /**
     * This will set the markup annotation which this popup relates to.
     * 
     * @param annot
     *            the markup annotation.
     */
    public void setParent( PDAnnotationMarkup annot )
    {
        getDictionary().setItem( COSName.PARENT, annot.getDictionary() );
    }

    /**
     * This will retrieve the markup annotation which this popup relates to.
     * 
     * @return The parent markup annotation.
     */
    public PDAnnotationMarkup getParent()
    {
        PDAnnotationMarkup am = null;
        try
        {
            am = (PDAnnotationMarkup) 
                PDAnnotation.createAnnotation( getDictionary().getDictionaryObject( "Parent", "P" ) );
        }
        catch (IOException ioe)
        {
            // Couldn't construct the annotation, so return null i.e. do nothing
        }
        return am;
    }

}