/**
 * Copyright (c) 2005, www.pdfbox.org
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
import org.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * This is the class that represents a file attachement.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDAnnotationFileAttachment extends PDAnnotation
{   
    /**
     * See get/setAttachmentName.
     */
    public static final String ATTACHMENT_NAME_PUSH_PIN = "PushPin";
    /**
     * See get/setAttachmentName.
     */
    public static final String ATTACHMENT_NAME_GRAPH = "Graph";
    /**
     * See get/setAttachmentName.
     */
    public static final String ATTACHMENT_NAME_PAPERCLIP = "Paperclip";
    /**
     * See get/setAttachmentName.
     */
    public static final String ATTACHMENT_NAME_TAG = "Tag";
    
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "FileAttachment";
    
    /**
     * Constructor.
     */
    public PDAnnotationFileAttachment()
    {
        super();
        getDictionary().setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
    }

    /**
     * Creates a Link annotation from a COSDictionary, expected to be
     * a correct object definition.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationFileAttachment(COSDictionary field)
    {
        super( field );
    }

    /**
     * Return the attached file.
     * 
     * @return The attached file.
     * 
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS( getDictionary().getDictionaryObject( "FS" ) );
    }
    
    /**
     * Set the attached file.
     * 
     * @param file The file that is attached.
     */
    public void setFile( PDFileSpecification file )
    {
        getDictionary().setItem( "FS", file );
    }
    
    /**
     * This is the name used to draw the type of attachment.  
     * See the ATTACHMENT_NAME_XXX constants.
     *  
     * @return The name that describes the visual cue for the attachment.
     */
    public String getAttachmentName()
    {
        return getDictionary().getNameAsString( "Name", ATTACHMENT_NAME_PUSH_PIN );
    }
    
    /**
     * Set the name used to draw the attachement icon.
     * See the ATTACHMENT_NAME_XXX constants.
     * 
     * @param name The name of the visual icon to draw.
     */
    public void setAttachementName( String name )
    {
        getDictionary().setName( "Name", name );
    }
}