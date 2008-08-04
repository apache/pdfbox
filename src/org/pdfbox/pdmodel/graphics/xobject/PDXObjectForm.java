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
