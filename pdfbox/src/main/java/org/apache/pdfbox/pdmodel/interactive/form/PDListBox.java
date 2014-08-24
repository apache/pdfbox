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
import org.apache.pdfbox.cos.COSString;

/**
 * A scrollable list box.
 * Contains several text items, one or more of which shall be selected as the field value.
 * @author John Hewson
 */
public final class PDListBox extends PDChoice
{
    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDListBox(PDAcroForm acroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(acroForm, field, parentNode);
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

    /**
     * setValue sets the entry "V" to the given value.
     * 
     * @param value the value
     * 
     */
    @Override
    public void setValue(Object value)
    {
        if ((getFieldFlags() & FLAG_EDIT) != 0)
        {
            throw new IllegalArgumentException("The combo box isn't editable.");
        }
        if (value != null)
        {
            if (value instanceof String)
            {
                int index = getSelectedIndex((String)value);
                if (index == -1)
                {
                    throw new IllegalArgumentException("The combo box does not contain the given value.");
                }
                selectMultiple(index);
            }
            // TODO multiple values
        }
        else
        {
            getDictionary().removeItem(COSName.V);
        }
    }

}
