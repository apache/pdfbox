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
 * A choice field contains several text items,
 * one or more of which shall be selected as the field value.
 * @author sug
 * @author John Hewson
 */
public abstract class PDChoice extends PDVariableText
{
    PDChoice(PDAcroForm acroForm, COSDictionary field)
    {
        super(acroForm, field);
    }

    // returns the "Opt" index for the given string
    protected int getSelectedIndex(String optionValue)
    {
        int indexSelected = -1;
        COSArray options = (COSArray)getDictionary().getDictionaryObject(COSName.OPT);

        // YXJ: Changed the order of the loops. Acrobat produces PDF's
        // where sometimes there is 1 string and the rest arrays.
        // This code works either way.
        for (int i = 0; i < options.size() && indexSelected == -1; i++)
        {
            COSBase option = options.getObject(i);
            if (option instanceof COSArray)
            {
                COSArray keyValuePair = (COSArray)option;
                COSString key = (COSString)keyValuePair.getObject(0);
                COSString value = (COSString)keyValuePair.getObject(1);
                if (optionValue.equals(key.getString()) || optionValue.equals(value.getString()))
                {
                    // have the parent draw the appearance stream with the value
                    // but then use the key as the V entry
                    getDictionary().setItem(COSName.V, key);
                    indexSelected = i;
                }
            }
            else
            {
                COSString value = (COSString)option;
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
        COSArray indexArray = (COSArray)getDictionary().getDictionaryObject(COSName.I);
        if (indexArray != null)
        {
            indexArray.clear();
            indexArray.add(COSInteger.get(selectedIndex));
        }
    }
}
