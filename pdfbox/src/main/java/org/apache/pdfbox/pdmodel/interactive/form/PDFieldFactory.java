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
import org.apache.pdfbox.pdmodel.common.COSObjectable;

import java.io.IOException;
import java.util.List;

/**
 * Factory for creating instances of PDField.
 * @author sug
 * @author John Hewson
 */
public final class PDFieldFactory
{
    // button flags
    private static final int FLAG_RADIO = 0x8000,
                             FLAG_PUSHBUTTON = 0x10000,
                             FLAG_RADIOS_IN_UNISON = 0x2000000;

    // choice flags
    private static final int FLAG_COMBO = 0x20000;

    private PDFieldFactory()
    {
    }

    /**
     * Creates a COSField subclass from the given field.
     * @param form the form that the field is part of
     * @param field the dictionary representing a field element
     * @return the corresponding PDField instance
     * @throws IOException if the field cannot be read
     */
    public static PDField createField(PDAcroForm form, COSDictionary field) throws IOException
    {
        String fieldType = PDField.findFieldType(field);
        if (isButton(form, field))
        {
            int flags = field.getInt(COSName.FF, 0);
            // BJL: I have found that the radio flag bit is not always set
            // and that sometimes there is just a kids dictionary.
            // so, if there is a kids dictionary then it must be a radio button group.
            // TODO JH: this is due to inheritance, we need proper support for "non-terminal fields"

            if ((flags & FLAG_RADIO) != 0 || field.getDictionaryObject(COSName.KIDS) != null)
            {
                return new PDRadioButton(form, field);
            }
            else if ((flags & FLAG_PUSHBUTTON) != 0)
            {
                return new PDPushButton(form, field);
            }
            else
            {
                return new PDCheckbox(form, field);
            }
        }
        else if ("Ch".equals(fieldType))
        {
            int flags = field.getInt(COSName.FF, 0);
            if ((flags & FLAG_COMBO) != 0)
            {
                return new PDComboBox(form, field);
            }
            else
            {
                return new PDListBox(form, field);
            }
        }
        else if ("Tx".equals(fieldType))
        {
            return new PDTextField(form, field);
        }
        else if ("Sig".equals(fieldType))
        {
            return new PDSignatureField(form, field);
        }
        else
        {
            // todo: inheritance and "non-terminal fields" are not supported yet
            return null;
        }
    }

    private static boolean isButton(PDAcroForm form, COSDictionary field) throws IOException
    {
        String fieldType = PDField.findFieldType(field);
        List<COSObjectable> kids = PDField.getKids(form, field);
        if (fieldType == null && kids != null && !kids.isEmpty())
        {
            // sometimes if it is a button the type is only defined by one of the kids entries
            // TODO JH: this is due to inheritance, we need proper support for "non-terminal fields"

            COSDictionary kid = (COSDictionary)kids.get(0).getCOSObject();
            return isButton(form, kid);
        }
        return "Btn".equals(fieldType);
    }
}
