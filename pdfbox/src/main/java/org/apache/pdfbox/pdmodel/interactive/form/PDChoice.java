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
     *  Ff-flags.
     */
    public  static final int FLAG_COMBO = 1 << 17;
    private static final int FLAG_SORT = 1 << 19;
    private static final int FLAG_MULTI_SELECT = 1 << 21;
    private static final int FLAG_DO_NOT_SPELL_CHECK = 1 << 22;
    private static final int FLAG_COMMIT_ON_SEL_CHANGE = 1 << 26;

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

    /**
     * Determines if Sort is set.
     * 
     * @return true if the options are sorted.
     */
    public boolean isSort()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_SORT );
    }

    /**
     * Set the Sort bit.
     *
     * @param sort The value for Sort.
     */
    public void setSort( boolean sort )
    {
        getDictionary().setFlag( COSName.FF, FLAG_SORT, sort );
    }

    /**
     * Determines if MultiSelect is set.
     * 
     * @return true if multi select is allowed.
     */
    public boolean isMultiSelect()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_MULTI_SELECT );
    }

    /**
     * Set the MultiSelect bit.
     *
     * @param multiSelect The value for MultiSelect.
     */
    public void setMultiSelect( boolean multiSelect )
    {
        getDictionary().setFlag( COSName.FF, FLAG_MULTI_SELECT, multiSelect );
    }

    /**
     * Determines if DoNotSpellCheck is set.
     * 
     * @return true if spell checker is disabled.
     */
    public boolean isDoNotSpellCheck()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_DO_NOT_SPELL_CHECK );
    }

    /**
     * Set the DoNotSpellCheck bit.
     *
     * @param doNotSpellCheck The value for DoNotSpellCheck.
     */
    public void setDoNotSpellCheck( boolean doNotSpellCheck )
    {
        getDictionary().setFlag( COSName.FF, FLAG_DO_NOT_SPELL_CHECK, doNotSpellCheck );
    }

    /**
     * Determines if CommitOnSelChange is set.
     * 
     * @return true if value shall be committed as soon as a selection is made.
     */
    public boolean isCommitOnSelChange()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_COMMIT_ON_SEL_CHANGE );
    }

    /**
     * Set the CommitOnSelChange bit.
     *
     * @param commitOnSelChange The value for CommitOnSelChange.
     */
    public void setCommitOnSelChange( boolean commitOnSelChange )
    {
        getDictionary().setFlag( COSName.FF, FLAG_COMMIT_ON_SEL_CHANGE, commitOnSelChange );
    }

    /**
     * Determines if Combo is set.
     * 
     * @return true if value the choice is a combo box..
     */
    public boolean isCombo()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_COMBO );
    }

    /**
     * Set the Combo bit.
     *
     * @param combo The value for Combo.
     */
    public void setCombo( boolean combo )
    {
        getDictionary().setFlag( COSName.FF, FLAG_COMBO, combo );
    }

    /**
     * setValue sets the entry "V" to the given value.
     * 
     * @param value the value
     * 
     */
    public void setValue(String value)
    {
        if (value != null)
        {
            getDictionary().setString(COSName.V, (String)value);
            int index = getSelectedIndex((String) value);
            if (index == -1)
            {
                throw new IllegalArgumentException("The list box does not contain the given value.");
            }
            selectMultiple(index);
        }
        else
        {
            getDictionary().removeItem(COSName.V);
        }
        // TODO create/update appearance
    }
    
    /**
     * setValue sets the entry "V" to the given value.
     * 
     * @param value the value
     * 
     */    
    public void setValue(String[] value)
    {
        if (value != null)
        {
            if (!isMultiSelect())
            {
                throw new IllegalArgumentException("The list box does not allow multiple selection.");
            }
            String[] stringValues = (String[])value;
            COSArray stringArray = new COSArray();
            for (int i =0; i<stringValues.length;i++)
            {
                stringArray.add(new COSString(stringValues[i]));
            }
            getDictionary().setItem(COSName.V, stringArray);
        }
        else
        {
            getDictionary().removeItem(COSName.V);
        }
        // TODO create/update appearance
    }
    

    /**
     * getValue gets the value of the "V" entry.
     * 
     * @return The value of this entry.
     * 
     */
    @Override
    public COSArray getValue()
    {
        COSBase value = getDictionary().getDictionaryObject( COSName.V);
        if (value instanceof COSString)
        {
            COSArray array = new COSArray();
            array.add(value);
            return array;
        }
        else if (value instanceof COSArray)
        {
            return (COSArray)value;
        }
        return null;
    }

    // returns the "Opt" index for the given string
    private int getSelectedIndex(String optionValue)
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
    private void selectMultiple(int selectedIndex)
    {
        COSArray indexArray = getSelectedOptions();
        if (indexArray != null)
        {
            indexArray.clear();
            indexArray.add(COSInteger.get(selectedIndex));
        }
    }
}
