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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A check box toggles between two states, on and off.
 *
 * @author Ben Litchfield
 * @author sug
 */
public final class PDCheckbox extends PDButton
{
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDCheckbox(PDAcroForm acroForm)
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
    PDCheckbox(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * This will tell if this radio button is currently checked or not.
     * This is equivalent to calling {@link #getValue()}.
     *
     * @return true If this field is checked.
     */
    public boolean isChecked()
    {
        return getValue();
    }

    /**
     * Checks the check box.
     */
    public void check() throws IOException
    {
        setValue("Yes"); // todo: need type safety
    }

    /**
     * Unchecks the check box.
     */
    public void unCheck()
    {
        dictionary.setItem(COSName.AS, COSName.OFF);
    }

    /**
     * Returns true if this field is checked.
     * 
     * @return True if checked
     */
    public boolean getValue()
    {
        COSBase value = getInheritableAttribute(COSName.V);
        return value instanceof COSName && value.equals(COSName.YES);
    }

    @Override
    public String getValueAsString()
    {
        return getValue() ? "Yes" : "Off";
    }
    
    @Override
    public void setValue(String value) throws IOException
    {
        dictionary.setName(COSName.V, value);
        
        // update the appearance state (AS)
        if (value == null)
        {
            dictionary.setItem(COSName.AS, COSName.OFF);
        }
        else
        {
            dictionary.setName(COSName.AS, value);
        }
        
        applyChange();
    }
}
