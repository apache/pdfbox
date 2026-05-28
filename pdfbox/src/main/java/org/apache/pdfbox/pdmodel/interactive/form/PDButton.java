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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

/**
 * A button field represents an interactive control on the screen
 * that the user can manipulate with the mouse.
 *
 * @author sug
 */
public abstract class PDButton extends PDTerminalField
{
    /**
     * A Ff flag. If set, the field is a set of radio buttons
     */
    static final int FLAG_RADIO = 1 << 15;
    
    /**
     * A Ff flag. If set, the field is a pushbutton.
     */
    static final int FLAG_PUSHBUTTON = 1 << 16;
    
    /**
     * A Ff flag. If set, radio buttons individual fields, using the same
     * value for the on state will turn on and off in unison.
     */
    static final int FLAG_RADIOS_IN_UNISON = 1 << 25;
    
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDButton(PDAcroForm acroForm)
    {
        super(acroForm);
        getCOSObject().setItem(COSName.FT, COSName.BTN);
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDButton(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }
    
    /**
     * Determines if push button bit is set.
     * 
     * @return true if type of button field is a push button.
     */
    public boolean isPushButton()
    {
        return getCOSObject().getFlag(COSName.FF, FLAG_PUSHBUTTON);
    }

    /**
     * Set the push button bit.
     *
     * @deprecated use {@link org.apache.pdfbox.pdmodel.interactive.form.PDPushButton} instead
     * @param pushbutton if true the button field is treated as a push button field.
     */
    @Deprecated
    public void setPushButton(boolean pushbutton)
    {
        getCOSObject().setFlag(COSName.FF, FLAG_PUSHBUTTON, pushbutton);
        if (pushbutton)
        {
            setRadioButton(false);
        }
    }

    /**
     * Determines if radio button bit is set.
     * 
     * @return true if type of button field is a radio button.
     */
    public boolean isRadioButton()
    {
        return getCOSObject().getFlag(COSName.FF, FLAG_RADIO);
    }

    /**
     * Set the radio button bit.
     *
     * @deprecated use {@link org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton} instead
     * @param radiobutton if true the button field is treated as a radio button field.
     */
    @Deprecated
    public void setRadioButton(boolean radiobutton)
    {
        getCOSObject().setFlag(COSName.FF, FLAG_RADIO, radiobutton);
        if (radiobutton)
        {
            setPushButton(false);
        }
    }
    
    /**
     * Returns the selected value.
     * 
     * <p>Off is the default value which will also be returned if the
     * value hasn't been set at all.
     * 
     * @return A non-null string.
     */
    public String getValue()
    {
        COSBase value = getInheritableAttribute(COSName.V);
        if (value instanceof COSName)
        {
            String stringValue = ((COSName)value).getName();
            List<String> exportValues = getExportValues();
            if (!exportValues.isEmpty())
            {
                try
                {
                    int idx = Integer.parseInt(stringValue, 10);
                    if (idx >= 0 && idx < exportValues.size())
                    {
                        return exportValues.get(idx);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    return stringValue;
                }
            }
            return stringValue;
        }
        else
        {
            // Off is the default value if there is nothing else set.
            // See PDF Spec.
            return "Off";
        }
    }

    /**
     * Sets the selected option given its name. It also tries to update the visual appearance,
     * unless {@link PDAcroForm#getNeedAppearances()} is true.
     *
     * @param value Name of option to select
     * @throws IOException if the value could not be set
     * @throws IllegalArgumentException if the value is not a valid option.
     */
    @Override
    public void setValue(String value) throws IOException
    {
        checkValue(value);
        
        // if there are export values/an Opt entry there is a different 
        // approach to setting the value
        boolean hasExportValues = getExportValues().size() > 0;

        if (hasExportValues) {
            updateByOption(value);
        }
        else
        {
            updateByValue(value);
        }
        
        applyChange();
    }

    /**
     * Set the selected option given its index, and try to update the visual appearance.
     * 
     * NOTE: this method is only usable if there are export values and used for 
     * radio buttons with FLAG_RADIOS_IN_UNISON not set.
     * 
     * @param index index of option to be selected
     * @throws IOException if the value could not be set
     * @throws IllegalArgumentException if the index provided is not a valid index.
     */
    public void setValue(int index) throws IOException
    {
        List<String> exportValues = getExportValues();
        if (exportValues.isEmpty() || index < 0 || index >= exportValues.size())
        {
            throw new IllegalArgumentException("index '" + index
                    + "' is not a valid index for the field " + getFullyQualifiedName()
                    + ", valid indices are from 0 to " + (exportValues.size() - 1));
        }

        updateByValue(String.valueOf(index));
                
        applyChange();
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

    /**
     * Sets the default value.
     *
     * @param value Name of option to select
     * @throws IllegalArgumentException if the value is not a valid option.
     */
    public void setDefaultValue(String value)
    {
        checkValue(value);        
        getCOSObject().setName(COSName.DV, value);
    }    
    
    @Override
    public String getValueAsString()
    {
        return getValue();
    }    
    
    
    /**
     * This will get the (optional) export values.
     * 
     * <p>The export values are defined in the field dictionaries /Opt key.</p>
     * 
     * <p>The option values are used to define the export values
     * for the field to 
     * <ul>
     *  <li>hold values in non-Latin writing systems as name objects, which represent the field value, are limited
     *      to PDFDocEncoding
     *  </li>
     *  <li>allow radio buttons having the same export value to be handled independently
     *  </li>
     * </ul>
     * 
     * @return List containing all possible export values. If there is no /Opt entry an empty list will be returned.
     * 
     * @see #getOnValues()
     */
    public List<String> getExportValues()
    {
        COSBase value = getInheritableAttribute(COSName.OPT);
        
        if (value instanceof COSString)
        {
            String stringValue = ((COSString)value).getString();
            if (stringValue.isEmpty())
            {
                return Collections.emptyList();
            }
            List<String> array = new ArrayList<String>();
            array.add(stringValue);
            return array;
        }
        else if (value instanceof COSArray)
        {
            return COSArrayList.convertCOSStringCOSArrayToList((COSArray)value);
        }
        return Collections.emptyList();
    }
    
    /**
     * This will set the export values.
     * 
     * @see #getExportValues()
     * @param values List containing all possible export values. Supplying null or an empty list will remove the Opt entry.
     */
    public void setExportValues(List<String> values)
    {
        COSArray cosValues;
        if (values != null && !values.isEmpty())
        {
            cosValues = COSArrayList.convertStringListToCOSStringCOSArray(values);
            getCOSObject().setItem(COSName.OPT, cosValues);
        }
        else
        {
            getCOSObject().removeItem(COSName.OPT);
        }
    }

    @Override
    void constructAppearances() throws IOException
    {
        for (PDAnnotationWidget widget : getWidgets())
        {
            PDAppearanceDictionary appearance = widget.getAppearance();
            if (appearance == null)
            {
                continue;
            }
            PDAppearanceEntry appearanceEntry = appearance.getNormalAppearance();
            COSName value = getCOSObject().getCOSName(COSName.V);
            if (((COSDictionary) appearanceEntry.getCOSObject()).containsKey(value))
            {
                widget.setAppearanceState(value);
            }
            else
            {
                widget.setAppearanceState(COSName.Off);
            }
        }
    }

    /**
     * Get the values to set individual buttons within a group to the on state.
     * 
     * <p>The On value could be an arbitrary string as long as it is within the limitations of
     * a PDF name object. The Off value shall always be 'Off'. If not set or not part of the normal
     * appearance keys 'Off' is the default</p>
     *
     * @return the potential values setting the check box to the On state. 
     *         If an empty Set is returned there is no appearance definition.
     */
    public Set<String> getOnValues()
    {
        // we need a set as the field can appear multiple times
        Set<String> onValues = new LinkedHashSet<String>();
        
        if (getExportValues().size() > 0)
        {
            onValues.addAll(getExportValues());
            return onValues;
        }
        
        List<PDAnnotationWidget> widgets = this.getWidgets();
        for (PDAnnotationWidget widget : widgets)
        {
        	onValues.add(getOnValueForWidget(widget));
        }        
        return onValues;
    }
    
    /*
     * Get the on value for an individual widget by it's index.
     */
    private String getOnValue(int index)
    {
        List<PDAnnotationWidget> widgets = this.getWidgets();
        if (index < widgets.size())
        {
            return getOnValueForWidget(widgets.get(index));
        }
        return "";
    }
 
    /*
     * Get the on value for an individual widget.
     */
    private String getOnValueForWidget(PDAnnotationWidget widget)
    {
        PDAppearanceDictionary apDictionary = widget.getAppearance();
        if (apDictionary != null) 
        {
            PDAppearanceEntry normalAppearance = apDictionary.getNormalAppearance();
            if (normalAppearance != null)
            {
                Set<COSName> entries = normalAppearance.getSubDictionary().keySet();
                for (COSName entry : entries)
                {
                    if (COSName.Off.compareTo(entry) != 0)
                    {
                        return entry.getName();
                    }
                }
            }
        }
        return "";
    }    
    
    
    /**
     * Checks value.
     *
     * @param value Name of radio button to select
     * @throws IllegalArgumentException if the value is not a valid option.
     */
    void checkValue(String value)
    {
        Set<String> onValues = getOnValues();
        if (COSName.Off.getName().compareTo(value) != 0 && !onValues.contains(value))
        {
            throw new IllegalArgumentException("value '" + value
                    + "' is not a valid option for the field " + getFullyQualifiedName()
                    + ", valid values are: " + onValues + " and " + COSName.Off.getName());
        }
    }

    private void updateByValue(String value) throws IOException
    {
        // Find the matching appearance key from the first widget that has it
        COSName matchingKey = null;

        // update the appearance state (AS)
        for (PDAnnotationWidget widget : getWidgets())
        {
            PDAppearanceDictionary appearance = widget.getAppearance();
            if (appearance == null)
            {
                continue;
            }
            PDAppearanceEntry appearanceEntry = widget.getAppearance().getNormalAppearance();
            COSDictionary appearanceDict = (COSDictionary) appearanceEntry.getCOSObject();

            // Find the matching appearance key by searching through the actual keys
            // and comparing their decoded names. This handles encoding differences:
            // the appearance key might be ISO-8859-1 encoded (e.g. /m#e4nnlich for "männlich")
            // while the value String is UTF-8.
            COSName widgetMatchingKey = findMatchingAppearanceKey(appearanceDict, value);

            // Save the first matching key to use for the V entry
            if (widgetMatchingKey != null && matchingKey == null)
            {
                matchingKey = widgetMatchingKey;
            }

            if (widgetMatchingKey != null)
            {
                // Use the exact COSName from the appearance dictionary to preserve encoding
                widget.setAppearanceState(widgetMatchingKey);
            }
            else
            {
                // Fall back to Off if no match found for this widget
                widget.setAppearanceState(COSName.Off);
            }
        }

        // Set the V entry once using the first matching key found
        if (matchingKey != null)
        {
            getCOSObject().setItem(COSName.V, matchingKey);
        }
        else
        {
            // Fall back to UTF-8 encoding if no match found in any widget
            getCOSObject().setName(COSName.V, value);
        }

    }

    /**
     * Find the appearance dictionary key that matches the given value String.
     * This method handles encoding differences - the value might be UTF-8 while
     * appearance keys in the PDF could be ISO-8859-1 or other encodings.
     *
     * @param appearanceDict the appearance dictionary with keys to search
     * @param value the value String to match against (typically UTF-8)
     * @return the matching COSName key, or null if no match found
     */
    private COSName findMatchingAppearanceKey(COSDictionary appearanceDict, String value)
    {
        // Search all keys in the appearance dictionary and compare their decoded names
        // COSName.getName() uses UTF-8 decoding with ISO-8859-1 fallback for non-UTF-8 bytes
        for (COSName key : appearanceDict.keySet())
        {
            if (value.equals(key.getName()))
            {
                return key;
            }
        }
        return null;
    }

    private void updateByOption(String value) throws IOException
    {
        List<PDAnnotationWidget> widgets = getWidgets();
        List<String> options = getExportValues();
        
        if (widgets.size() != options.size())
        {
            throw new IllegalArgumentException("The number of options doesn't match the number of widgets");
        }
        
        if (value.equals(COSName.Off.getName()))
        {
            updateByValue(value);
        }
        else
        {
            // the value is the index of the matching option
            int optionsIndex = options.indexOf(value);

            // get the values the options are pointing to as
            // this might not be numerical
            // see PDFBOX-3682
            if (optionsIndex != -1)
            {
                updateByValue(getOnValue(optionsIndex));
            }
        }
    }
    
}
