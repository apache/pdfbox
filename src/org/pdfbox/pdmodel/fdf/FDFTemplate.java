/**
 * Copyright (c) 2004-2005, www.pdfbox.org
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

import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents an FDF template that is part of the FDF page.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class FDFTemplate implements COSObjectable
{
    private COSDictionary template;

    /**
     * Default constructor.
     */
    public FDFTemplate()
    {
        template = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param t The FDF page template.
     */
    public FDFTemplate( COSDictionary t )
    {
        template = t;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return template;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return template;
    }

    /**
     * This is the template reference.
     *
     * @return The template reference.
     */
    public FDFNamedPageReference getTemplateReference()
    {
        FDFNamedPageReference retval = null;
        COSDictionary dict = (COSDictionary)template.getDictionaryObject( "TRef" );
        if( dict != null )
        {
            retval = new FDFNamedPageReference( dict );
        }
        return retval;
    }

    /**
     * This will set the template reference.
     *
     * @param tRef The template reference.
     */
    public void setTemplateReference( FDFNamedPageReference tRef )
    {
        template.setItem( "TRef", tRef );
    }

    /**
     * This will get a list of fields that are part of this template.
     *
     * @return A list of fields.
     */
    public List getFields()
    {
        List retval = null;
        COSArray array = (COSArray)template.getDictionaryObject( "Fields" );
        if( array != null )
        {
            List fields = new ArrayList();
            for( int i=0; i<array.size(); i++ )
            {
                fields.add( new FDFField( (COSDictionary)array.getObject( i ) ) );
            }
            retval = new COSArrayList( fields, array );
        }
        return retval;
    }

    /**
     * This will set a list of fields for this template.
     *
     * @param fields The list of fields to set for this template.
     */
    public void setFields( List fields )
    {
        template.setItem( "Fields", COSArrayList.converterToCOSArray( fields ) );
    }

    /**
     * A flag telling if the fields imported from the template may be renamed if there are conflicts.
     *
     * @return A flag telling if the fields can be renamed.
     */
    public boolean shouldRename()
    {
        return template.getBoolean( "Rename", false );
    }

    /**
     * This will set if the fields can be renamed.
     *
     * @param value The flag value.
     */
    public void setRename( boolean value )
    {
        template.setBoolean( "Rename", value );
    }
}