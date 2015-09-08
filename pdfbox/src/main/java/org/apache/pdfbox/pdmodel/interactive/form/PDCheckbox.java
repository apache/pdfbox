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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import java.io.IOException;
import java.util.List;

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

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The checkbox field dictionary
     */
    public PDCheckbox( PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
    }

    /**
     * This will tell if this radio button is currently checked or not.
     *
     * @return true If the radio button is checked.
     */
    public boolean isChecked()
    {
        return getDictionary().getNameAsString("V").compareTo(getOnValue()) == 0;
    }

    /**
     * Checks the radiobutton.
     */
    public void check()
    {
        setValue(getOnValue());
    }

    /**
     * Unchecks the radiobutton.
     */
    public void unCheck()
    {
        setValue(OFF_VALUE.getName());
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String newValue)
    {
        getDictionary().setName( "V", newValue );
        
        List<PDAnnotationWidget> widgets = getWidgets();
        for (PDAnnotationWidget widget : widgets)
        {
            if( newValue == null )
            {
                widget.getDictionary().setItem( KEY, OFF_VALUE );
            }
            else
            {
                widget.getDictionary().setName( KEY, newValue );
            }
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
        List<PDAnnotationWidget> widgets = getWidgets();
        for (PDAnnotationWidget widget : widgets)
        {
            COSDictionary ap = (COSDictionary) widget.getDictionary().getDictionaryObject(COSName.getPDFName("AP"));
            widget.getDictionary().setItem(KEY, COSName.getPDFName("Yes"));
            COSBase n = ap.getDictionaryObject(COSName.getPDFName("N"));

            //N can be a COSDictionary or a COSStream
            if( n instanceof COSDictionary )
            {
                for( COSName key :((COSDictionary)n).keySet() )
                {
                    if( !key.equals( OFF_VALUE) )
                    {
                        return key.getName();
                    }
                }
            }
        }
        // normally you would expect to get an empty string but as the
        // prior implementation returned null keep the behavior.
        return null;
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
