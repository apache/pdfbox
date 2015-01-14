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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.interactive.form.FieldUtils.KeyValue;

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
     * @see PDFieldTreeNode#PDFieldTreeNode(PDAcroForm)
     *
     * @param theAcroForm The acroform.
     */
    protected PDChoice(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
    }

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
     * <p>
     * For a choice field the options array can either be an array
     * of text strings or an array of a two-element arrays.<br/>
     * The method always only returns either the text strings or,
     * in case of two-element arrays, an array of the first element of 
     * the two-element arrays
     * </p>   
     * <p>
     * Use {@link #getOptionsExportValues()} and {@link #getOptionsDisplayValues()}
     * to get the entries of two-element arrays.
     * </p>
     * 
     * @return List containing the export values.
     */
    public List<String> getOptions()
    {
        COSBase values = getDictionary().getDictionaryObject(COSName.OPT);
        if (values instanceof COSString)
        {
            List<String> array = new ArrayList<String>();
            array.add(((COSString) values).getString());
            return array;
        }
        else if (values instanceof COSArray)
        {
            // test if there is a single text or a two-element array 
            COSBase entry = ((COSArray) values).get(0);
            if (entry instanceof COSString)
            {
                return COSArrayList.convertCOSStringCOSArrayToList((COSArray)values);
            } 
            else
            {
                List<String> exportValues = new ArrayList<String>();
                int numItems = ((COSArray) values).size();
                for (int i=0;i<numItems;i++)
                {
                    COSArray pair = (COSArray) ((COSArray) values).get(i);
                    COSString displayValue = (COSString) pair.get(0);
                    exportValues.add(displayValue.getString());
                }
                return exportValues;
            }            
        }
        return Collections.<String>emptyList();
    }

    /**
     * This will set the display values - the 'Opt' key.
     * 
     * <p>
     * The Opt array specifies the list of options in the choice field either
     * as an array of text strings representing the display value 
     * or as an array of a two-element array where the
     * first element is the export value and the second the display value.
     * </p>
     * <p>
     * To set both the export and the display value use {@link #setOptions(List, List)}
     * </p> 
     *
     * @param displayValues List containing all possible options.
     */
    public void setOptions(List<String> displayValues)
    {
        if (displayValues != null && !displayValues.isEmpty())
        {
            if (isSort())
            {
                Collections.sort(displayValues);
            }
            getDictionary().setItem(COSName.OPT, COSArrayList.convertStringListToCOSStringCOSArray(displayValues));
        }
        else
        {
            getDictionary().removeItem(COSName.OPT);
        }
    }

    /**
     * This will set the display and export values - the 'Opt' key.
     *
     * <p>
     * This will set both, the export value and the display value
     * of the choice field. If either one of the parameters is null or an 
     * empty list is supplied the options will
     * be removed.
     * </p>
     * <p>
     * An {@link IllegalArgumentException} will be thrown if the
     * number of items in the list differ.
     * </p>
     *
     * @see #setOptions(List)
     * @param exportValues List containing all possible export values.
     * @param displayValues List containing all possible display values.
     */
    public void setOptions(List<String> exportValues, List<String> displayValues)
    {
        if (exportValues != null && displayValues != null && !exportValues.isEmpty() && !displayValues.isEmpty()) 
        {
            if (exportValues.size() != displayValues.size())
            {
                throw new IllegalArgumentException(
                        "The number of entries for exportValue and displayValue shall be the same.");
            }
            else
            {
                List<KeyValue> keyValuePairs = FieldUtils.toKeyValueList(exportValues, displayValues);

                if (isSort())
                {
                    FieldUtils.sortByValue(keyValuePairs);
                } 

                COSArray options = new COSArray();
                for (int i = 0; i<exportValues.size(); i++)
                {
                    COSArray entry = new COSArray();
                    entry.add(new COSString(keyValuePairs.get(i).getKey()));
                    entry.add(new COSString(keyValuePairs.get(i).getValue()));
                    options.add(entry);
                }
                getDictionary().setItem(COSName.OPT, options);
            }
        }
        else
        {
            getDictionary().removeItem(COSName.OPT);
        }      
    }

    /**
     * This will get the display values from the options.
     * 
     * <p>
     * For options with an array of text strings the display value and export value
     * are the same.<br/>
     * For options with an array of two-element arrays the display value is the 
     * second entry in the two-element array.
     * </p>
     * 
     * @return List containing all the display values.
     */
    public List<String> getOptionsDisplayValues()
    {
        COSBase values = getDictionary().getDictionaryObject(COSName.OPT);
        if (values instanceof COSString)
        {
            List<String> array = new ArrayList<String>();
            array.add(((COSString) values).getString());
            return array;
        }
        else if (values instanceof COSArray)
        {
            // test if there is a single text or a two-element array 
            COSBase entry = ((COSArray) values).get(0);
            if (entry instanceof COSString)
            {
                return COSArrayList.convertCOSStringCOSArrayToList((COSArray)values);
            } 
            else
            {
                List<String> displayValues = new ArrayList<String>();
                int numItems = ((COSArray) values).size();
                for (int i=0;i<numItems;i++)
                {
                    COSArray pair = (COSArray) ((COSArray) values).get(i);
                    COSString displayValue = (COSString) pair.get(1);
                    displayValues.add(displayValue.getString());
                }
                return displayValues;
            }
            
        }
        return Collections.<String>emptyList();
    }

    /**
     * This will get the export values from the options.
     * 
     * <p>
     * For options with an array of text strings the display value and export value
     * are the same.<br/>
     * For options with an array of two-element arrays the export value is the 
     * first entry in the two-element array.
     * </p>
     *
     * @return List containing all export values.
     */
    public List<String> getOptionsExportValues()
    {
        return getOptions();
    }
    
    
    /**
     * This will get the indices of the selected options "I".
     *
     * @return COSArray containing the indices of all selected options.
     */
    public List<Integer> getSelectedOptions()
    {
        COSBase value = getDictionary().getDictionaryObject(COSName.I);
        if (value != null)
        {
            return COSArrayList.convertIntegerCOSArrayToList((COSArray) value);
        }
        return Collections.<Integer>emptyList();
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
     * <p>
     * If set, the field’s option items shall be sorted alphabetically.
     * The sorting has to be done when writing the PDF. PDF Readers are supposed to
     * display the options in the order in which they occur in the Opt array. 
     * </p>
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
     * @see #isSort()
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
            // TODO handle MultiSelect option as this might need to set the 'I' key.
            int index = getSelectedIndex((String) value);
            if (index == -1)
            {
                throw new IllegalArgumentException("The list box does not contain the given value.");
            }
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
     * @param values the list of values
     * 
     */    
    public void setValue(List<String> values)
    {
        if (values != null)
        {
            if (!isMultiSelect())
            {
                throw new IllegalArgumentException("The list box does not allow multiple selection.");
            }
            // TODO handle MultiSelect option completely as this might need to set the 'I' key.
            getDictionary().setItem(COSName.V, COSArrayList.convertStringListToCOSStringCOSArray(values));
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
    public List<String> getValue()
    {
        COSBase value = getDictionary().getDictionaryObject( COSName.V);
        if (value instanceof COSString)
        {
            List<String> array = new ArrayList<String>();
            array.add(((COSString) value).getString());
            return array;
        }
        else if (value instanceof COSArray)
        {
            return COSArrayList.convertCOSStringCOSArrayToList((COSArray)value);
        }
        return Collections.<String>emptyList();
    }

    // returns the "Opt" index for the given string
    private int getSelectedIndex(String optionValue)
    {
        int indexSelected = -1;
        List<String> options = getOptions();

        for (int i = 0; i < options.size() && indexSelected == -1; i++)
        {
            String option = options.get(i);
            if (option.compareTo(optionValue) == 0)
            {
                return i;
            }
        }
        return indexSelected;
    }

    // TODO the implementation below is not in line 
    // with the specification nor does it allow multiple selections
    // deactivating for now as it's not part of the public API
    //
    // implements "MultiSelect"
    /*
    private void selectMultiple(int selectedIndex)
    {
        COSArray indexArray = getSelectedOptions();
        if (indexArray != null)
        {
            indexArray.clear();
            indexArray.add(COSInteger.get(selectedIndex));
        }
    }
    */
}
