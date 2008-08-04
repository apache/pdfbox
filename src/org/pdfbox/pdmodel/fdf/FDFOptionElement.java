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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSString;

import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents an object that can be used in a Field's Opt entry to represent
 * an available option and a default appearance string.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class FDFOptionElement implements COSObjectable
{
    private COSArray option;

    /**
     * Default constructor.
     */
    public FDFOptionElement()
    {
        option = new COSArray();
        option.add( new COSString( "" ) );
        option.add( new COSString( "" ) );
    }

    /**
     * Constructor.
     *
     * @param o The option element.
     */
    public FDFOptionElement( COSArray o )
    {
        option = o;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return option;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSArray getCOSArray()
    {
        return option;
    }

    /**
     * This will get the string of one of the available options.  A required element.
     *
     * @return An available option.
     */
    public String getOption()
    {
        return ((COSString)option.getObject( 0 ) ).getString();
    }

    /**
     * This will set the string for an available option.
     *
     * @param opt One of the available options.
     */
    public void setOption( String opt )
    {
        option.set( 0, new COSString( opt ) );
    }

    /**
     * This will get the string of default appearance string.  A required element.
     *
     * @return A default appearance string.
     */
    public String getDefaultAppearanceString()
    {
        return ((COSString)option.getObject( 1 ) ).getString();
    }

    /**
     * This will set the default appearance string.
     *
     * @param da The default appearance string.
     */
    public void setDefaultAppearanceString( String da )
    {
        option.set( 1, new COSString( da ) );
    }
}