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
package org.apache.pdfbox.pdmodel.fdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

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
