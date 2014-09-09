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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * A class for handling the PDF field as a choicefield.
 *
 * @author sug
 * @version $Revision: 1.7 $
 */
public class PDChoiceField extends PDVariableText
{
    /**
     * A Ff flag.
     */
    public static final int FLAG_COMBO = 1 << 17;

    /**
     * A Ff flag.
     */
    public static final int FLAG_EDIT = 1 << 18;

    private PDAppearance appearance;

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm
     *            The acroForm for this field.
     * @param field
     *            The field for this choice field.
     */
    public PDChoiceField(PDAcroForm theAcroForm, COSDictionary field)
    {
        super(theAcroForm, field);
    }

    private void setListboxValue(String value) throws IOException
    {
        COSString fieldValue = new COSString(value);
        getDictionary().setItem(COSName.V, fieldValue);

        // hmm, not sure what the case where the DV gets set to the field
        // value, for now leave blank until we can come up with a case
        // where it needs to be in there
        // getDictionary().setItem( COSName.getPDFName( "DV" ), fieldValue );
        if (appearance == null)
        {
            this.appearance = new PDAppearance(getAcroForm(), this);
        }
        appearance.setAppearanceValue(value);
    }

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#setValue(java.lang.String)
     *
     * @param optionValue
     *            The new value for this text field.
     *
     * @throws IOException
     *             If there is an error calculating the appearance stream or the value in not one of the existing
     *             options.
     */
    public void setValue(String optionValue) throws IOException
    {
        int indexSelected = -1;
        COSArray options = (COSArray) getDictionary().getDictionaryObject(COSName.OPT);
        int fieldFlags = getFieldFlags();
        boolean isEditable = (FLAG_COMBO & fieldFlags) != 0 && (FLAG_EDIT & fieldFlags) != 0;

        if (options.size() == 0 && !isEditable)
        {
            throw new IOException("Error: You cannot set a value for a choice field if there are no options.");
        }
        else
        {
            // YXJ: Changed the order of the loops. Acrobat produces PDF's
            // where sometimes there is 1 string and the rest arrays.
            // This code works either way.
            for (int i = 0; i < options.size() && indexSelected == -1; i++)
            {
                COSBase option = options.getObject(i);
                if (option instanceof COSArray)
                {
                    COSArray keyValuePair = (COSArray) option;
                    COSString key = (COSString) keyValuePair.getObject(0);
                    COSString value = (COSString) keyValuePair.getObject(1);
                    if (optionValue.equals(key.getString()) || optionValue.equals(value.getString()))
                    {
                        // have the parent draw the appearance stream with the value
                        if ((FLAG_COMBO & fieldFlags) != 0)
                        {
                            super.setValue(value.getString());
                        }
                        else
                        {
                            COSArray indexEntries = new COSArray();
                            indexEntries.add(COSInteger.get((long) i));
                            getDictionary().setItem(COSName.I, indexEntries);
                            setListboxValue(value.getString());
                        }
                        // but then use the key as the V entry
                        getDictionary().setItem(COSName.V, key);
                        indexSelected = i;

                    }
                }
                else
                {
                    COSString value = (COSString) option;
                    if (optionValue.equals(value.getString()))
                    {
                        super.setValue(optionValue);
                        indexSelected = i;
                    }
                }
            }
        }
        if (indexSelected == -1 && isEditable)
        {
            super.setValue(optionValue);
        }
        else if (indexSelected == -1)
        {
            throw new IOException("Error: '" + optionValue + "' was not an available option.");
        }
        else
        {
            COSArray indexArray = (COSArray) getDictionary().getDictionaryObject(COSName.I);
            if (indexArray != null)
            {
                indexArray.clear();
                indexArray.add(COSInteger.get(indexSelected));
            }
        }
    }

    /**
     * This will get the indices of the selected options "I".
     * 
     * @return COSArray containing the indices of all selected options.
     */
    public COSArray getSelectedOptions()
    {
        return (COSArray) getDictionary().getDictionaryObject(COSName.I);
    }

    /**
     * This will get the top index "TI" value.
     * 
     * The value returned will be the first item to display in the listbox.
     * 
     * @return the top index, default value 0.
     */
    public int getTopIndex()
    {
        return getDictionary().getInt(COSName.getPDFName("TI"), 0);
    }

    /**
     * This will get the option values "Opt".
     *
     * @return COSArray containing all options.
     */
    public COSArray getOptions()
    {
        return (COSArray) getDictionary().getDictionaryObject(COSName.OPT);
    }

}
