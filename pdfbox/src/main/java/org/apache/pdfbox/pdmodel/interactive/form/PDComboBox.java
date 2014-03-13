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

import java.io.IOException;

/**
 * A combo box consisting of a drop-down list.
 * May be accompanied by an editable text box in which non-predefined values may be entered.
 * @author John Hewson
 */
public final class PDComboBox extends PDChoice
{
    private static final int FLAG_EDIT = 0x40000;

    /**
     * Creates a new combo box field
     * @param acroForm the parent form
     * @param field the COS field
     */
    public PDComboBox(PDAcroForm acroForm, COSDictionary field)
    {
        super(acroForm, field);
    }

    @Override
    public void setValue(String optionValue) throws IOException
    {
        boolean isEditable = (getFieldFlags() & FLAG_EDIT) != 0;
        int index = getSelectedIndex(optionValue);

        if (index == -1 && !isEditable)
        {
            throw new IllegalArgumentException("Combo box does not contain the given value");
        }

        super.setValue(optionValue);
        selectMultiple(index);
    }
}
