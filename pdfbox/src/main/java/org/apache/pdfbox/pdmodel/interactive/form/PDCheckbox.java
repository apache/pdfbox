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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

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
        setValue(true);
    }

    /**
     * Unchecks the check box.
     */
    public void unCheck() throws IOException
    {
        setValue(false);
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

    /**
     * Returns the default value, if any.
     *
     * @return True if checked, false if not checked, null if missing.
     */
    public Boolean getDefaultValue()
    {
        COSBase value = getInheritableAttribute(COSName.DV);
        if (value == null)
        {
            return null;
        }
        return value instanceof COSName && value.equals(COSName.YES);
    }

    @Override
    public String getValueAsString()
    {
        return getValue() ? "Yes" : "Off";
    }

    /**
     * Sets the checked value of this field.
     *
     * @param value True if checked
     * @throws IOException if the value could not be set
     */
    public void setValue(boolean value) throws IOException
    {
        COSName name = value ? COSName.YES : COSName.Off;
        dictionary.setItem(COSName.V, name);
        
        // update the appearance state (AS)
        dictionary.setItem(COSName.AS, name);
        
        applyChange();
    }

    /**
     * Sets the default value.
     *
     * @param value True if checked
     * @throws IOException if the value could not be set
     */
    public void setDefaultValue(boolean value) throws IOException
    {
        COSName name = value ? COSName.YES : COSName.Off;
        dictionary.setItem(COSName.DV, name);
    }

    /**
     * Get the value which sets the check box to the On state.
     * 
     * <p>The On value should be 'Yes' but other values are possible
     * so we need to look for that. On the other hand the Off value shall
     * always be 'Off'. If not set or not part of the normal appearance keys
     * 'Off' is the default</p>
     *
     * @returns the value setting the check box to the On state. 
     *          If an empty string is returned there is no appearance definition.
     * @throws IOException if the value could not be set
     */
    public String getOnValue()
    {
        PDAnnotationWidget widget = this.getWidgets().get(0);
        PDAppearanceDictionary apDictionary = widget.getAppearance();
        
        String onValue = "";
        if (apDictionary != null) 
        {
            PDAppearanceEntry normalAppearance = apDictionary.getNormalAppearance();
            if (normalAppearance != null)
            {
                Set<COSName> entries = normalAppearance.getSubDictionary().keySet();
                for (COSName entry : entries)
                {
                    // Specifically looking for the string 'Off' as the off value
                    // Can't use COSName.OFF here as this will be 'OFF' in uppercase
                    // but for form fields the correct value is 'Off' in mixed case
                    if ("Off".compareTo(entry.getName()) != 0)
                    {
                        onValue = entry.getName();
                    }
                }
            }
        }
        return onValue;
    }
    
    /**
     * Get the values which sets the check box to the On state.
     * 
     * <p>This is a convenience function to provide a similar method to 
     * {@link PDRadioButton} </p>
     *
     * @see #getOnValue()
     * @returns the value setting the check box to the On state. 
     *          If an empty List is returned there is no appearance definition.
     * @throws IOException if the value could not be set
     */
    public List<String> getOnValues()
    {
        String onValue = getOnValue();
        
        if (onValue.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            ArrayList<String> onValues = new ArrayList<String>();
            onValues.add(onValue);
            return onValues;
        }
    }
    
}
