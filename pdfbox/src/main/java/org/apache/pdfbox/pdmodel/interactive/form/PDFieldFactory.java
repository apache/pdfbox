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
import org.apache.pdfbox.cos.COSName;

/**
 * A PDField factory.
 */
final class PDFieldFactory
{

    private static final String FIELD_TYPE_TEXT = "Tx";
    private static final String FIELD_TYPE_BUTTON = "Btn";
    private static final String FIELD_TYPE_CHOICE = "Ch";
    private static final String FIELD_TYPE_SIGNATURE = "Sig";
    
    private PDFieldFactory()
    {
    }
    
    /**
     * Creates a COSField subclass from the given field.
     *
     * @param form the form that the field is part of
     * @param field the dictionary representing a field element
     * @param parent the parent node of the node to be created 
     * @return the corresponding PDField instance
     */
    static PDField createField(PDAcroForm form, COSDictionary field, PDNonTerminalField parent)
    {
        String fieldType = findFieldType(field);
        
        // Test if we have a non terminal field first as it might have
        // properties which do apply to other fields
        // A non terminal fields has Kids entries which do have
        // a field name (other than annotations)
        if (field.containsKey(COSName.KIDS))
        {
            COSArray kids = (COSArray) field.getDictionaryObject(COSName.KIDS);
            if (kids != null && kids.size() > 0)
            {
                for (int i = 0; i < kids.size(); i++)
                {
                    COSBase kid = kids.getObject(i);
                    if (kid instanceof COSDictionary && ((COSDictionary) kid).getString(COSName.T) != null)
                    {
                        return new PDNonTerminalField(form, field, parent);
                    }
                }
            }
        } 
        
        if (FIELD_TYPE_CHOICE.equals(fieldType))
        {
            return createChoiceSubType(form, field, parent);
        }
        else if (FIELD_TYPE_TEXT.equals(fieldType))
        {
            return new PDTextField(form, field, parent);
        }
        else if (FIELD_TYPE_SIGNATURE.equals(fieldType))
        {
            return new PDSignatureField(form, field, parent);
        }
        else if (FIELD_TYPE_BUTTON.equals(fieldType))
        {
            return createButtonSubType(form, field, parent);
        }
        else
        {
            // an erroneous non-field object, see PDFBOX-2885
            return null;
        }
    }

    private static PDField createChoiceSubType(PDAcroForm form, COSDictionary field,
                                               PDNonTerminalField parent)
    {
        int flags = field.getInt(COSName.FF, 0);
        if ((flags & PDChoice.FLAG_COMBO) != 0)
        {
            return new PDComboBox(form, field, parent);
        }
        else
        {
            return new PDListBox(form, field, parent);
        }
    }

    private static PDField createButtonSubType(PDAcroForm form, COSDictionary field,
                                               PDNonTerminalField parent)
    {
        int flags = field.getInt(COSName.FF, 0);
        // BJL: I have found that the radio flag bit is not always set
        // and that sometimes there is just a kids dictionary.
        // so, if there is a kids dictionary then it must be a radio button group.
        if ((flags & PDButton.FLAG_RADIO) != 0)
        {
            return new PDRadioButton(form, field, parent);
        }
        else if ((flags & PDButton.FLAG_PUSHBUTTON) != 0)
        {
            return new PDPushButton(form, field, parent);
        }
        else
        {
            return new PDCheckBox(form, field, parent);
        }
    }

    private static String findFieldType(COSDictionary dic)
    {
        String retval = dic.getNameAsString(COSName.FT);
        if (retval == null)
        {
            COSBase base = dic.getDictionaryObject(COSName.PARENT, COSName.P);
            if (base instanceof COSDictionary)
            {
                retval = findFieldType((COSDictionary) base);
            }
        }
        return retval;
    }
}
