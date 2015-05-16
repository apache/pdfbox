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
import java.util.List;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

/**
 * Radio button fields contain a set of related buttons that can each be on or off.
 *
 * @author sug
 */
public final class PDRadioButton extends PDButton
{
    /**
     * A Ff flag.
     */
    private static final int FLAG_NO_TOGGLE_TO_OFF = 1 << 14;
    
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDRadioButton(PDAcroForm acroForm)
    {
        super(acroForm);
        setRadioButton(true);
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDRadioButton(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * From the PDF Spec <br/>
     * If set, a group of radio buttons within a radio button field that use the same value for the on state will turn
     * on and off in unison; that is if one is checked, they are all checked. If clear, the buttons are mutually
     * exclusive (the same behavior as HTML radio buttons).
     *
     * @param radiosInUnison The new flag for radiosInUnison.
     */
    public void setRadiosInUnison(boolean radiosInUnison)
    {
        dictionary.setFlag(COSName.FF, FLAG_RADIOS_IN_UNISON, radiosInUnison);
    }

    /**
     *
     * @return true If the flag is set for radios in unison.
     */
    public boolean isRadiosInUnison()
    {
        return dictionary.getFlag(COSName.FF, FLAG_RADIOS_IN_UNISON);
    }

    /**
     * This will get the export value.
     * <p>
     * A RadioButton might have an export value to allow field values
     * which can not be encoded as PDFDocEncoding or for the same export value 
     * being assigned to multiple RadioButtons in a group.<br/>
     * To define an export value the RadioButton must define options {@link #setOptions(List)}
     * which correspond to the individual items within the RadioButton.</p>
     * <p>
     * The method will either return the value from the options entry or in case there
     * is no such entry the fields value</p>
     * 
     * @return the export value of the field.
     * @throws IOException in case the fields value can not be retrieved
     */
    public String getExportValue() throws IOException
    {
        List<String> options = getOptions();
        if (options.isEmpty())
        {
            return getValue();
        }
        else
        {
            String fieldValue = getValue();
            List<PDAnnotationWidget> kids = getWidgets();
            int idx = 0;
            for (COSObjectable kid : kids)
            {
                // fixme: this is always false, because it's kids are always widgets, not fields.
                /*if (kid instanceof PDCheckbox)
                {
                    PDCheckbox btn = (PDCheckbox) kid;
                    if (btn.getOnValue().equals(fieldValue))
                    {
                        break;
                    }
                    idx++;
                }*/
            }
            if (idx <= options.size())
            {
                return options.get(idx);
            }
        }
        return "";
    }

    /**
     * Returns the selected value. May be empty if NoToggleToOff is set but there is no value
     * selected.
     * 
     * @return A non-null string.
     */
    public String getValue()
    {
        COSBase value = getInheritableAttribute(COSName.V);
        if (value instanceof COSName)
        {
            return ((COSName)value).getName();
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns the default value, if any.
     *
     * @return A non-null string.
     */
    public String getDefaultValue()
    {
        COSBase value = getInheritableAttribute(COSName.DV);
        if (value instanceof COSName)
        {
            return ((COSName)value).getName();
        }
        else
        {
            return "";
        }
    }

    @Override
    public String getValueAsString()
    {
        return getValue();
    }

    /**
     * Sets the selected radio button, given its name.
     * 
     * @param value Name of radio button to select
     * @throws IOException if the value could not be set
     */
    public void setValue(String value) throws IOException
    {
        dictionary.setName(COSName.V, value);
        // update the appearance state (AS)
        for (PDAnnotationWidget widget : getWidgets())
        {
            PDAppearanceEntry appearanceEntry = widget.getAppearance().getNormalAppearance();
            if (((COSDictionary)appearanceEntry.getCOSObject()).containsKey(value))
            {
                widget.getCOSObject().setName(COSName.AS, value);
            }
            else
            {
                widget.getCOSObject().setItem(COSName.AS, COSName.OFF);
            }
        }
        applyChange();
    }

    /**
     * Sets the default value.
     *
     * @param value Name of radio button to select
     * @throws IOException if the value could not be set
     */
    public void setDefaultValue(String value) throws IOException
    {
        dictionary.setName(COSName.DV, value);
    }
}
