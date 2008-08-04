/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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

import org.pdfbox.pdmodel.common.PDTextStream;
import org.pdfbox.cos.COSBase;

import java.io.IOException;

import java.util.Calendar;

/**
 * This class represents the additonal fields of a Markup type Annotation.
 * 
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public abstract class PDAnnotationMarkup extends PDAnnotation
{

    /*
     * The various values of the reply type as defined in the PDF 1.6 reference
     * Table 8.17
     */

    /**
     * Constant for an annotation reply type.
     */
    public static final String RT_REPLY = "R";

    /**
     * Constant for an annotation reply type.
     */
    public static final String RT_GROUP = "Group";

    /**
     * Constructor.
     */
    public PDAnnotationMarkup()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param dict
     *            The annotations dictionary.
     */
    public PDAnnotationMarkup( COSDictionary dict )
    {
        super( dict );
    }

    /**
     * Retrieve the string used as the title of the popup window shown when open
     * and active (by convention this identifies who added the annotation).
     * 
     * @return The title of the popup.
     */
    public String getTitlePopup()
    {
        return getDictionary().getString( "T" );
    }

    /**
     * Set the string used as the title of the popup window shown when open and
     * active (by convention this identifies who added the annotation).
     * 
     * @param t
     *            The title of the popup.
     */
    public void setTitlePopup( String t )
    {
        getDictionary().setString( "T", t );
    }

    /**
     * This will retrieve the popup annotation used for entering/editing the
     * text for this annotation.
     * 
     * @return the popup annotation.
     */
    public PDAnnotationPopup getPopup()
    {
        COSDictionary popup = (COSDictionary) getDictionary().getDictionaryObject( "Popup" );
        if (popup != null)
        {
            return new PDAnnotationPopup( popup );
        } 
        else
        {
            return null;
        }
    }

    /**
     * This will set the popup annotation used for entering/editing the text for
     * this annotation.
     * 
     * @param popup
     *            the popup annotation.
     */
    public void setPopup( PDAnnotationPopup popup )
    {
        getDictionary().setItem( "Popup", popup );
    }

    /**
     * This will retrieve the constant opacity value used when rendering the
     * annotation (excluing any popup).
     * 
     * @return the constant opacity value.
     */
    public float getConstantOpacity()
    {
        return getDictionary().getFloat( "CA", 1 );
    }

    /**
     * This will set the constant opacity value used when rendering the
     * annotation (excluing any popup).
     * 
     * @param ca
     *            the constant opacity value.
     */
    public void setConstantOpacity( float ca )
    {
        getDictionary().setFloat( "CA", ca );
    }

    /**
     * This will retrieve the rich text stream which is displayed in the popup
     * window.
     * 
     * @return the rich text stream.
     */
    public PDTextStream getRichContents()
    {
        COSBase rc = getDictionary().getDictionaryObject( "RC" );
        if (rc != null)
        {
            return PDTextStream.createTextStream( rc );
        } 
        else
        {
            return null;
        }
    }

    /**
     * This will set the rich text stream which is displayed in the popup window.
     * 
     * @param rc
     *            the rich text stream.
     */
    public void setRichContents( PDTextStream rc )
    {
        getDictionary().setItem( "RC", rc);
    }

    /**
     * This will retrieve the date and time the annotation was created.
     * 
     * @return the creation date/time.
     * @throws IOException
     *             if there is a format problem when converting the date.
     */
    public Calendar getCreationDate() throws IOException
    {
        return getDictionary().getDate( "CreationDate" );
    }

    /**
     * This will set the the date and time the annotation was created.
     * 
     * @param creationDate
     *            the date and time the annotation was created.
     */
    public void setCreationDate( Calendar creationDate )
    {
        getDictionary().setDate( "CreationDate", creationDate );
    }

    /**
     * This will retrieve the annotation to which this one is "In Reply To" the
     * actual relationship is specified by the RT entry.
     * 
     * @return the other annotation.
     * @throws IOException
     *             if there is an error with the annotation.
     */
    public PDAnnotation getInReplyTo() throws IOException
    {
        COSBase irt = getDictionary().getDictionaryObject( "IRT" );
        return PDAnnotation.createAnnotation( irt );
    }

    /**
     * This will set the annotation to which this one is "In Reply To" the
     * actual relationship is specified by the RT entry.
     * 
     * @param irt
     *            the annotation this one is "In Reply To".
     */
    public void setInReplyTo( PDAnnotation irt )
    {
        getDictionary().setItem( "IRT", irt );
    }

    /**
     * This will retrieve the short description of the subject of the annotation.
     * 
     * @return the subject.
     */
    public String getSubject()
    {
        return getDictionary().getString( "Subj" );
    }

    /**
     * This will set the short description of the subject of the annotation.
     * 
     * @param subj
     *            short description of the subject.
     */
    public void setSubject( String subj )
    {
        getDictionary().setString( "Subj", subj );
    }

    /**
     * This will retrieve the Reply Type (relationship) with the annotation in
     * the IRT entry See the RT_* constants for the available values.
     * 
     * @return the relationship.
     */
    public String getReplyType()
    {
        return getDictionary().getNameAsString( "RT", RT_REPLY );
    }

    /**
     * This will set the Reply Type (relationship) with the annotation in the
     * IRT entry See the RT_* constants for the available values.
     * 
     * @param rt
     *            the reply type.
     */
    public void setReplyType( String rt )
    {
        getDictionary().setName( "RT", rt );
    }

    /**
     * This will retrieve the intent of the annotation The values and meanings
     * are specific to the actual annotation See the IT_* constants for the
     * annotation classes.
     * 
     * @return the intent
     */
    public String getIntent()
    {
        return getDictionary().getNameAsString( "IT" );
    }

    /**
     * This will set the intent of the annotation The values and meanings are
     * specific to the actual annotation See the IT_* constants for the
     * annotation classes.
     * 
     * @param it
     *            the intent
     */
    public void setIntent( String it )
    {
        getDictionary().setName( "IT", it );
    }

}