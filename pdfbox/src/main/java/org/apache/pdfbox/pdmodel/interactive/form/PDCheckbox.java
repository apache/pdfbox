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
        super( acroForm );
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDCheckbox( PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super( acroForm, field, parent);
    }

    /**
     * This will tell if this radio button is currently checked or not.
     *
     * @return true If the radio button is checked.
     * @throws IOException 
     */
    public boolean isChecked() throws IOException
    {
        String onValue = getOnValue();
        String fieldValue = null;
        try
        {
            fieldValue = getValue();
        }
        catch (IOException e)
        {
            // getting there means that the field value
            // doesn't have a supported type.
            // Ignoring as that will also mean that the field is not checked.
            // Setting the value explicitly as Code Analysis (Sonar) doesn't like
            // empty catch blocks.
            return false;
        }
        COSName radioValue = (COSName)dictionary.getDictionaryObject(COSName.AS);
        if( radioValue != null && fieldValue != null && radioValue.getName().equals( onValue ) )
        {
            return true;
        }

        return false;
    }

    /**
     * Checks the check box.
     */
    public void check()
    {
        String onValue = getOnValue();
        setValue(onValue);
        dictionary.setItem(COSName.AS, COSName.getPDFName(onValue));
    }

    /**
     * Unchecks the check box.
     */
    public void unCheck()
    {
        dictionary.setItem(COSName.AS, COSName.OFF);
    }

    /**
     * This will get the value assigned to the OFF state.
     *
     * @return The value of the check box.
     */
    public String getOffValue()
    {
        return COSName.OFF.getName();
    }

    /**
     * This will get the value assigned to the ON state.
     *
     * @return The value of the check box.
     */
    public String getOnValue()
    {
        COSDictionary ap = (COSDictionary) dictionary.getDictionaryObject(COSName.AP);
        COSBase n = ap.getDictionaryObject(COSName.N);

        //N can be a COSDictionary or a COSStream
        if( n instanceof COSDictionary )
        {
            for( COSName key :((COSDictionary)n).keySet() )
            {
                if( !key.equals( COSName.OFF) )
                {
                    return key.getName();
                }
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

    @Override
    public void setValue(String value)
    {
        if (value == null)
        {
            dictionary.removeItem(COSName.V);
            dictionary.setItem(COSName.AS, COSName.OFF);
        }
        else
        {
            COSName nameValue = COSName.getPDFName(value);
            dictionary.setItem(COSName.V, nameValue);
            dictionary.setItem(COSName.AS, nameValue);
        }
    }
}
