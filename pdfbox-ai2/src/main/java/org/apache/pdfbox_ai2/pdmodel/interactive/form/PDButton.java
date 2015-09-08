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
package org.apache.pdfbox_ai2.pdmodel.interactive.form;

import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.cos.COSString;
import org.apache.pdfbox_ai2.pdmodel.common.COSArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.pdfbox_ai2.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox_ai2.pdmodel.interactive.annotation.PDAppearanceDictionary;

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
        dictionary.setItem(COSName.FT, COSName.BTN);
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
        return dictionary.getFlag(COSName.FF, FLAG_PUSHBUTTON);
    }

    /**
     * Set the push button bit.
     *
     * @param pushbutton if true the button field is treated as a push button field.
     */
    public void setPushButton(boolean pushbutton)
    {
        dictionary.setFlag(COSName.FF, FLAG_PUSHBUTTON, pushbutton);
    }

    /**
     * Determines if radio button bit is set.
     * 
     * @return true if type of button field is a push button.
     */
    public boolean isRadioButton()
    {
        return dictionary.getFlag(COSName.FF, FLAG_RADIO);
    }

    /**
     * Set the radio button bit.
     *
     * @param radiobutton if true the button field is treated as a radio button field.
     */
    public void setRadioButton(boolean radiobutton)
    {
        dictionary.setFlag(COSName.FF, FLAG_RADIO, radiobutton);
    }
    
    /**
     * This will get the export values.
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
     * </p>
     * 
     * @return List containing all possible export values. If there is no Opt entry an empty list will be returned.
     */
    public List<String> getExportValues()
    {
        COSBase value = getInheritableAttribute(COSName.OPT);
        if (value instanceof COSString)
        {
            List<String> array = new ArrayList<String>();
            array.add(((COSString) value).getString());
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
            dictionary.setItem(COSName.OPT, cosValues);
        }
        else
        {
            dictionary.removeItem(COSName.OPT);
        }
    }
    
    @Override
    void constructAppearances() throws IOException
    {
        for (PDAnnotationWidget widget : getWidgets())
        {
            PDAppearanceDictionary appearance = widget.getAppearance();
            if (appearance == null || appearance.getNormalAppearance() == null)
            {
                // TODO: implement appearance generation for radio buttons
                throw new UnsupportedOperationException("Appearance generation is not implemented yet, see PDFBOX-2849");
            }
        }
    }  

    
}
