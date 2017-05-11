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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;

/**
 * A pushbutton is a purely interactive control that responds immediately to user
 * input without retaining a permanent value.
 *
 * @author sug
 */
public class PDPushButton extends PDButton
{
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDPushButton(PDAcroForm acroForm)
    {
        super(acroForm);
        setPushButton(true);
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDPushButton(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    @Override
    public List<String> getExportValues()
    {
        return Collections.emptyList();
    }
    
    @Override
    public void setExportValues(List<String> values)
    {
        if (values != null && !values.isEmpty())
        {
            throw new IllegalArgumentException("A PDPushButton shall not use the Opt entry in the field dictionary");
        }
    }
    
    @Override
    public String getValue()
    {
        return "";
    }

    @Override
    public String getDefaultValue()
    {
        return "";
    }

    @Override
    public String getValueAsString()
    {
        return getValue();
    }
    
    @Override
    void constructAppearances() throws IOException
    {
        // TODO: add appearance handler to generate/update appearance
    } 
}
