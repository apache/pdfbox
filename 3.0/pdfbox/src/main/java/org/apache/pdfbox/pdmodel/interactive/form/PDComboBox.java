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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A combo box consisting of a drop-down list.
 * May be accompanied by an editable text box in which non-predefined values may be entered.
 * 
 * @author John Hewson
 */
public final class PDComboBox extends PDChoice
{
    private static final int FLAG_EDIT = 1 << 18;
    
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDComboBox(PDAcroForm acroForm)
    {
        super(acroForm);
        setCombo(true);
    }    

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDComboBox(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * Determines if Edit is set.
     * 
     * @return true if the combo box shall include an editable text box as well as a drop-down list.
     */
    public boolean isEdit()
    {
        return getCOSObject().getFlag(COSName.FF, FLAG_EDIT);
    }

    /**
     * Set the Edit bit.
     *
     * @param edit The value for Edit.
     */
    public void setEdit(boolean edit)
    {
        getCOSObject().setFlag(COSName.FF, FLAG_EDIT, edit);
    }
    
    @Override
    void constructAppearances() throws IOException
    {
        AppearanceGeneratorHelper apHelper;
        apHelper = new AppearanceGeneratorHelper(this);
        List<String> values = getValue();
        
        if (!values.isEmpty())
        {
            apHelper.setAppearanceValue(values.get(0));
        }
        else
        {
            apHelper.setAppearanceValue("");
        }
    }
}
