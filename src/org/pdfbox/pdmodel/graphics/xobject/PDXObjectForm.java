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
package org.pdfbox.pdmodel.graphics.xobject;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.PDResources;
import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * A form xobject. 
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDXObjectForm extends PDXObject
{
    /**
     * The XObject subtype.
     */
    public static final String SUB_TYPE = "Form";
    
    /**
     * Standard constuctor. 
     * 
     * @param formStream The XObject is passed as a COSStream.
     */
    public PDXObjectForm(PDStream formStream) 
    {
        super( formStream );
        getCOSStream().setName( COSName.SUBTYPE, SUB_TYPE );
    }
    
    /**
     * Standard constuctor. 
     * 
     * @param formStream The XObject is passed as a COSStream.
     */
    public PDXObjectForm(COSStream formStream) 
    {
        super( formStream );
        getCOSStream().setName( COSName.SUBTYPE, SUB_TYPE );
    }
    
    /**
     * This will get the form type, currently 1 is the only form type.
     * 
     * @return The form type.
     */
    public int getFormType()
    {
        return getCOSStream().getInt( "FormType",1 );
    }
    
    /**
     * Set the form type.
     * 
     * @param formType The new form type.
     */
    public void setFormType( int formType )
    {
        getCOSStream().setInt( "FormType", formType );
    }
    
    /**
     * This will get the resources at this page and not look up the hierarchy.
     * This attribute is inheritable, and findResources() should probably used.
     * This will return null if no resources are available at this level.
     *
     * @return The resources at this level in the hierarchy.
     */
    public PDResources getResources()
    {
        PDResources retval = null;
        COSDictionary resources = (COSDictionary)getCOSStream().getDictionaryObject( COSName.RESOURCES );
        if( resources != null )
        {
            retval = new PDResources( resources );
        }
        return retval;
    }
    
    /**
     * This will set the resources for this page.
     *
     * @param resources The new resources for this page.
     */
    public void setResources( PDResources resources )
    {
        getCOSStream().setItem( COSName.RESOURCES, resources );
    }
    
    /**
     * An array of four numbers in the form coordinate system (see
     * below), giving the coordinates of the left, bottom, right, and top edges,
     * respectively, of the form XObject’s bounding box. These boundaries are used
     * to clip the form XObject and to determine its size for caching.
     *
     * @return The BBox of the form.
     */
    public PDRectangle getBBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray)getCOSStream().getDictionaryObject( COSName.BBOX );
        if( array != null )
        {
            retval = new PDRectangle( array );
        }
        return retval;
    }
    
    /**
     * This will set the BBox (bounding box) for this form.
     *
     * @param bbox The new BBox for this form.
     */
    public void setBBox(PDRectangle bbox)
    {
        if( bbox == null )
        {
            getCOSStream().removeItem( COSName.BBOX );
        }
        else
        {
            getCOSStream().setItem( COSName.BBOX, bbox.getCOSArray() );
        }
    }
}
