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

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;

/**
 * A field in an interactive form.
 * Fields may be one of four types: button, text, choice, or signature.
 *
 * @author sug
 */
public abstract class PDTerminalField extends PDField
{
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     */
    protected PDTerminalField(PDAcroForm acroForm)
    {
        super(acroForm);
    }

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDTerminalField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * Set the actions of the field.
     * 
     * @param actions The field actions.
     */
    public void setActions(PDFormFieldAdditionalActions actions)
    {
        dictionary.setItem(COSName.AA, actions);
    }
    
    @Override
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) dictionary.getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        else if (parent != null)
        {
            retval = parent.getFieldFlags();
        }
        return retval;
    }
    
    @Override
    public String getFieldType()
    {
        String fieldType = dictionary.getNameAsString(COSName.FT);
        if (fieldType == null && parent != null)
        {
            fieldType = parent.getFieldType();
        }
        return fieldType;
    }
    
    /**
     * Update the fields appearance stream.
     * 
     * The fields appearance stream needs to be updated to reflect the new field
     * value. This will be done only if the NeedAppearances flag has not been set.
     * 
     * @throws IOException if the appearance couldn't be generated
     */
    protected void updateFieldAppearances() throws IOException
    {
        if (!acroForm.isNeedAppearances())
        {
            AppearanceGenerator.generateFieldAppearances(this);
        }
    }
}
