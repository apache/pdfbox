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
import org.apache.pdfbox.cos.COSName;

/**
 * A scrollable list box. Contains several text items, one or more of which shall be selected as the
 * field value.
 * 
 * @author John Hewson
 */
public final class PDListBox extends PDChoice
{
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDListBox(PDAcroForm acroForm)
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
    PDListBox(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * This will get the top index "TI" value.
     *
     * @return the top index, default value 0.
     */
    public int getTopIndex()
    {
        return getCOSObject().getInt(COSName.TI, 0);
    }

    /**
     * This will set top index "TI" value.
     *
     * @param topIndex the value for the top index, null will remove the value.
     */
    public void setTopIndex(Integer topIndex)
    {
        if (topIndex != null)
        {
            getCOSObject().setInt(COSName.TI, topIndex);
        }
        else
        {
            getCOSObject().removeItem(COSName.TI);
        }
    }
    
    @Override
    void constructAppearances() throws IOException
    {
        AppearanceGeneratorHelper apHelper;
        apHelper = new AppearanceGeneratorHelper(this);
        apHelper.setAppearanceValue("");
    }
}
