/**
 * Copyright (c) 2004, www.pdfbox.org
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
package org.pdfbox.pdmodel.fdf;

import java.io.IOException;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;

import org.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * This represents an FDF named page reference that is part of the FDF field.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class FDFNamedPageReference implements COSObjectable
{
    private COSDictionary ref;

    /**
     * Default constructor.
     */
    public FDFNamedPageReference()
    {
        ref = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param r The FDF named page reference dictionary.
     */
    public FDFNamedPageReference( COSDictionary r )
    {
        ref = r;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return ref;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return ref;
    }

    /**
     * This will get the name of the referenced page.  A required parameter.
     *
     * @return The name of the referenced page.
     */
    public String getName()
    {
        return ref.getString( "Name" );
    }

    /**
     * This will set the name of the referenced page.
     *
     * @param name The referenced page name.
     */
    public void setName( String name )
    {
        ref.setString( "Name", name );
    }

    /**
     * This will get the file specification of this reference.  An optional parameter.
     *
     * @return The F entry for this dictionary.
     * 
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFileSpecification() throws IOException
    {
        return PDFileSpecification.createFS( ref.getDictionaryObject( "F" ) );
    }

    /**
     * This will set the file specification for this named page reference.
     *
     * @param fs The file specification to set.
     */
    public void setFileSpecification( PDFileSpecification fs )
    {
        ref.setItem( "F", fs );
    }
}