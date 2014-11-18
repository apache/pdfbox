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
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.util.appearance.AppearanceGenerator;

/**
 * A field in an interactive form.
 * Fields may be one of four types: button, text, choice, or signature.
 *
 * @author sug
 */
public abstract class PDField extends PDFieldTreeNode
{
    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     */
    protected PDField(PDAcroForm theAcroForm)
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
    protected PDField(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(theAcroForm, field, parentNode);
    }

    /**
     * This will return a string representation of this field.
     * 
     * @return A string representation of this field.
     */
    @Override
    public String toString()
    {
        return "" + getDictionary().getDictionaryObject(COSName.V);
    }

    /**
     * Set the actions of the field.
     * 
     * @param actions The field actions.
     */
    public void setActions(PDFormFieldAdditionalActions actions)
    {
        getDictionary().setItem(COSName.AA, actions);
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
        else if (getParent() != null)
        {
            retval = getParent().getFieldFlags();
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFieldType()
    {
        String fieldType = getDictionary().getNameAsString(COSName.FT);
        if (fieldType == null && getParent() != null)
        {
            fieldType = getParent().getFieldType();
        }
        return fieldType;
    }
    
    /**
     * Update the fields appearance stream.
     * 
     * The fields appearance stream needs to be updated to reflect the new field
     * value. This will be done only if the NeedAppearances flag has not been set.
     */
    protected void updateFieldAppearances()
    {
        if (!getAcroForm().isNeedAppearances())
        {
            AppearanceGenerator.generateFieldAppearances(this);
        }
    }


}
