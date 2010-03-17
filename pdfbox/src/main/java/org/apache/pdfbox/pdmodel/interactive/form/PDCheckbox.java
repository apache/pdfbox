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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import java.io.IOException;

/**
 * A class for handling the PDF field as a checkbox.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author sug
 * @version $Revision: 1.11 $
 */
public class PDCheckbox extends PDChoiceButton
{
    private static final COSName KEY = COSName.getPDFName("AS");
    private static final COSName OFF_VALUE = COSName.getPDFName("Off");

    private COSName value;

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The checkbox field dictionary
     */
    public PDCheckbox( PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
        COSDictionary ap = (COSDictionary) field.getDictionaryObject(COSName.getPDFName("AP"));
        if( ap != null )
        {
            COSBase n = ap.getDictionaryObject(COSName.getPDFName("N"));

            if( n instanceof COSDictionary )
            {
                for( COSName name : ((COSDictionary)n).keySet() )
                {
                    if( !name.equals( OFF_VALUE ))
                    {
                        value = name;
                    }
                }

            }
        }
        else
        {
            value = (COSName)getDictionary().getDictionaryObject( "V" );
        }
    }

    /**
     * This will tell if this radio button is currently checked or not.
     *
     * @return true If the radio button is checked.
     */
    public boolean isChecked()
    {
        boolean retval = false;
        String onValue = getOnValue();
        COSName radioValue = (COSName)getDictionary().getDictionaryObject( KEY );
        if( radioValue != null && value != null && radioValue.getName().equals( onValue ) )
        {
            retval = true;
        }

        return retval;
    }

    /**
     * Checks the radiobutton.
     */
    public void check()
    {
        getDictionary().setItem(KEY, value);
    }

    /**
     * Unchecks the radiobutton.
     */
    public void unCheck()
    {
        getDictionary().setItem(KEY, OFF_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String newValue)
    {
        getDictionary().setName( "V", newValue );
        if( newValue == null )
        {
            getDictionary().setItem( KEY, OFF_VALUE );
        }
        else
        {
            getDictionary().setName( KEY, newValue );
        }
    }

    /**
     * This will get the value of the radio button.
     *
     * @return The value of the radio button.
     */
    public String getOffValue()
    {
        return OFF_VALUE.getName();
    }

    /**
     * This will get the value of the radio button.
     *
     * @return The value of the radio button.
     */
    public String getOnValue()
    {
        String retval = null;
        COSDictionary ap = (COSDictionary) getDictionary().getDictionaryObject(COSName.getPDFName("AP"));
        COSBase n = ap.getDictionaryObject(COSName.getPDFName("N"));

        //N can be a COSDictionary or a COSStream
        if( n instanceof COSDictionary )
        {
            for( COSName key :((COSDictionary)n).keySet() )
            {
                if( !key.equals( OFF_VALUE) )
                {
                    retval = key.getName();
                }
            }
        }
        return retval;
    }

    /**
     * getValue gets the fields value to as a string.
     *
     * @return The string value of this field.
     *
     * @throws IOException If there is an error getting the value.
     */
    public String getValue() throws IOException
    {
        return getDictionary().getNameAsString( "V" );
    }

}
