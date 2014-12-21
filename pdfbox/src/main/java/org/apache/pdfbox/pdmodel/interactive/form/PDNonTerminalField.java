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
package org.apache.pdfbox.pdmodel.interactive.form;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;

/**
 * A non terminal field in an interactive form.
 * 
 * A non terminal field is a node in the fields tree node whose descendants
 * are fields. 
 * 
 * The attributes such as FT (field type) or V (field value) do not logically
 * belong to the non terminal field but are inheritable attributes
 * for descendant terminal fields. 
 * 
 * @author Andreas Lehmkühler
 */
public class PDNonTerminalField extends PDFieldTreeNode
{
    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     */
    public PDNonTerminalField(PDAcroForm theAcroForm)
    {
        super(theAcroForm);
    }

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDNonTerminalField(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(theAcroForm, field, parentNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) getDictionary().getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        // There is no need to look up the parent hierarchy within a non terminal field
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFieldType()
    {
        // There is no need to look up the parent hierarchy within a non terminal field
        return getDictionary().getNameAsString(COSName.FT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        // There is no need to look up the parent hierarchy within a non terminal field
        return getDictionary().getNameAsString(COSName.V);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(String fieldValue)
    {
        // There is no need to look up the parent hierarchy within a non terminal field
        getDictionary().setString(COSName.V, fieldValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue()
    {
        // There is no need to look up the parent hierarchy within a non terminal field
        return getDictionary().getNameAsString(COSName.V);
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    public void setDefaultValue(String defaultValue)
    {
        // There is no need to look up the parent hierarchy within a non terminal field
        getDictionary().setString(COSName.V, defaultValue);
    }
    
}
