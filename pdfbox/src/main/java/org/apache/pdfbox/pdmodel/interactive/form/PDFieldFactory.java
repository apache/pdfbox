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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * Factory for creating instances of PDField.
 * @author sug
 * @author John Hewson
 */
public final class PDFieldFactory
{
 
    private PDFieldFactory()
    {
    }

    /**
     * Creates a COSField subclass from the given field.
     * @param form the form that the field is part of
     * @param field the dictionary representing a field element
     * @return the corresponding PDField instance
     */
    public static PDFieldTreeNode createField(PDAcroForm form, COSDictionary field, PDFieldTreeNode parentNode)
    {
        String fieldType = PDField.findFieldType(field);
        if (PDFieldTreeNode.FIELD_TYPE_CHOICE.equals(fieldType))
        {
            int flags = field.getInt(COSName.FF, 0);
            if ((flags & PDVariableText.FLAG_COMB) != 0)
            {
                return new PDComboBox(form, field, parentNode);
            }
            else
            {
                return new PDListBox(form, field, parentNode);
            }
        }
        else if (PDFieldTreeNode.FIELD_TYPE_TEXT.equals(fieldType))
        {
            return new PDTextField(form, field, parentNode);
        }
        else if (PDFieldTreeNode.FIELD_TYPE_SIGNATURE.equals(fieldType))
        {
            return new PDSignatureField(form, field, parentNode);
        }
        else if (PDFieldTreeNode.FIELD_TYPE_BUTTON.equals(fieldType))
        {
            int flags = field.getInt(COSName.FF, 0);
            // BJL: I have found that the radio flag bit is not always set
            // and that sometimes there is just a kids dictionary.
            // so, if there is a kids dictionary then it must be a radio button group.
            if ((flags & PDButton.FLAG_RADIO) != 0 || field.getDictionaryObject(COSName.KIDS) != null)
            {
                return new PDRadioButton(form, field, parentNode);
            }
            else if ((flags & PDButton.FLAG_PUSHBUTTON) != 0)
            {
                return new PDPushButton(form, field, parentNode);
            }
            else
            {
                return new PDCheckbox(form, field, parentNode);
            }
        }
        else
        {
            return new PDNonTerminalField(form, field, parentNode); 
        }
    }

}
