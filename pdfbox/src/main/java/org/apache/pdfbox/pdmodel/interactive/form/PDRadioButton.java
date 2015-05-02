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
    public static final int FLAG_NO_TOGGLE_TO_OFF = 1 << 14;
    
    /**
     * @see PDFieldTreeNode#PDFieldTreeNode(PDAcroForm)
     *
     * @param theAcroForm The acroform.
     */
    public PDRadioButton(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
        setRadioButton(true);
    }
    
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
        getCOSObject().setFlag(COSName.FF, FLAG_RADIOS_IN_UNISON, radiosInUnison);
    }

    /**
     *
     * @return true If the flag is set for radios in unison.
     */
    public boolean isRadiosInUnison()
    {
        return getCOSObject().getFlag(COSName.FF, FLAG_RADIOS_IN_UNISON);
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
            List<COSObjectable> kids = getKids();
            int idx = 0;
            for (COSObjectable kid : kids)
            {
                if (kid instanceof PDCheckbox)
                {
                    PDCheckbox btn = (PDCheckbox) kid;
                    if (btn.getOnValue().equals(fieldValue))
                    {
                        break;
                    }
                    idx++;
                }
            }
            if (idx <= options.size())
            {
                return options.get(idx);
            }
        }
        return "";
    }
    
    @Override
    public String getValue() throws IOException
    {
        COSBase attribute = getInheritableAttribute(COSName.V);
        
        if (attribute == null)
        {
            return "";
        }
        else if (attribute instanceof COSName)
        {
            return ((COSName) attribute).getName();
        }
        else
        {
            throw new IOException("Expected a COSName entry but got " + attribute.getClass().getName());
        }
    }

    /**
     * Set the field value.
     * 
     * The field value holds a name object which is corresponding to the 
     * appearance state of the child field being in the on state.
     * 
     * The default value is Off.
     * 
     * @param fieldValue the COSName object to set the field value.
     */
    @Override
    public void setValue(String fieldValue)
    {
        if (fieldValue == null)
        {
            removeInheritableAttribute(COSName.V);
        }
        else
        {
            COSName nameForValue = COSName.getPDFName(fieldValue);
            setInheritableAttribute(COSName.V, nameForValue);
            List<COSObjectable> kids = getKids();
            for (COSObjectable kid : kids)
            {
                if (kid instanceof PDAnnotationWidget)
                {
                    PDAppearanceEntry appearanceEntry = ((PDAnnotationWidget) kid).getAppearance()
                            .getNormalAppearance();
                    
                    if (((COSDictionary) appearanceEntry.getCOSObject()).containsKey(nameForValue))
                    {
                        ((COSDictionary) kid.getCOSObject()).setName(COSName.AS, fieldValue);
                    }
                    else
                    {
                        ((COSDictionary) kid.getCOSObject()).setItem(COSName.AS, PDButton.OFF);
                    }
                }
            }
        }
    }
}
