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
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * A choice field contains several text items, one or more of which shall be selected as the field value.
 * 
 * @author sug
 * @author John Hewson
 */
public abstract class PDChoice extends PDVariableText
{

    /**
     * A Ff flag.
     */
    public static final int FLAG_COMBO = 1 << 17;
    /**
     * A Ff flag.
     */
    public static final int FLAG_EDIT = 1 << 18;
    /**
     * A Ff flag.
     */
    public static final int FLAG_SORT = 1 << 19;
    /**
     * A Ff flag.
     */
    public static final int FLAG_MULTI_SELECT = 1 << 21;
    /**
     * A Ff flag.
     */
    public static final int FLAG_DO_NOT_SPELL_CHECK = 1 << 22;
    /**
     * A Ff flag.
     */
    public static final int FLAG_COMMIT_ON_SEL_CHANGE = 1 << 26;

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    protected PDChoice(PDAcroForm acroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(acroForm, field, parentNode);
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

    /**
     * This will set the options.
     *
     * @param values COSArray containing all possible options.
     */
    public void setOptions(COSArray values)
    {
        if (values != null)
        {
            getDictionary().setItem(COSName.OPT, values);
        }
        else
        {
            getDictionary().removeItem(COSName.OPT);
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
     * This will set the indices of the selected options "I".
     *
     * @param values COSArray containing the indices of all selected options.
     */
    public void setSelectedOptions(COSArray values)
    {
        if (values != null)
        {
            getDictionary().setItem(COSName.I, values);
        }
        else
        {
            getDictionary().removeItem(COSName.I);
        }
    }

    // returns the "Opt" index for the given string
    protected int getSelectedIndex(String optionValue)
    {
        int indexSelected = -1;
        COSArray options = getOptions();
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
                    indexSelected = i;
                }
            }
            else
            {
                COSString value = (COSString) option;
                if (optionValue.equals(value.getString()))
                {
                    indexSelected = i;
                }
            }
        }
        return indexSelected;
    }

    // implements "MultiSelect"
    protected void selectMultiple(int selectedIndex)
    {
        COSArray indexArray = getSelectedOptions();
        if (indexArray != null)
        {
            indexArray.clear();
            indexArray.add(COSInteger.get(selectedIndex));
        }
    }
}
