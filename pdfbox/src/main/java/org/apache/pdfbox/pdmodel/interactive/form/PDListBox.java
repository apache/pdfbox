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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import java.io.IOException;

/**
 * A scrollable list box.
 * Contains several text items, one or more of which shall be selected as the field value.
 * @author John Hewson
 */
public final class PDListBox extends PDChoice
{
    /**
     * Creates a new list box field
     * @param acroForm the parent form
     * @param field the COS field
     */
    public PDListBox(PDAcroForm acroForm, COSDictionary field)
    {
        super(acroForm, field);
    }

    @Override
    public void setValue(String optionValue) throws IOException
    {
        COSArray options = (COSArray)getDictionary().getDictionaryObject(COSName.OPT);
        if(options.size() == 0)
        {
            throw new IllegalArgumentException("List box does not contain the given value");
        }

        int index = getSelectedIndex(optionValue);
        if (index == -1)
        {
            throw new IllegalArgumentException("List box does not contain the given value");
        }
        else
        {
            super.setValue(optionValue);
            selectMultiple(index);
        }
    }
}
