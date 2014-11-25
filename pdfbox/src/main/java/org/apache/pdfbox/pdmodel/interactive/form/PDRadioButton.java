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

import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * Radio button fields contain a set of related buttons that can each be on or off.
 *
 * @author sug
 */
public final class PDRadioButton extends PDButton
{

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDRadioButton(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(theAcroForm, field, parentNode);
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
        getDictionary().setFlag(COSName.FF, FLAG_RADIOS_IN_UNISON, radiosInUnison);
    }

    /**
     *
     * @return true If the flag is set for radios in unison.
     */
    public boolean isRadiosInUnison()
    {
        return getDictionary().getFlag(COSName.FF, FLAG_RADIOS_IN_UNISON);
    }

    
    @Override
    public COSName getDefaultValue()
    {
        return getDictionary().getCOSName(COSName.DV);
    }
    
    
    @Override
    public COSName getValue()
    {
        return getDictionary().getCOSName(COSName.V);
    }

    
    public void setValue(COSName value)
    {
        if (value == null)
        {
            getDictionary().removeItem(COSName.V);
        }
        else
        {
            getDictionary().setItem(COSName.V, (COSName) value);
            List<COSObjectable> kids = getKids();
            for (COSObjectable kid : kids)
            {
                if (kid instanceof PDCheckbox)
                {
                    PDCheckbox btn = (PDCheckbox) kid;
                    if (btn.getOnValue().equals(value))
                    {
                        btn.check();
                    }
                    else
                    {
                        btn.unCheck();
                    }
                }
            }
        }
    }

}
