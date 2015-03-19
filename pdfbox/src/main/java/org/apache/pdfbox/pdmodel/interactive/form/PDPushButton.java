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
     * @see PDFieldTreeNode#PDFieldTreeNode(PDAcroForm)
     *
     * @param theAcroForm The acroform.
     */
    public PDPushButton(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
        setPushButton(true);
    }
    
    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDPushButton( PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(theAcroForm, field, parentNode);
    }
    
    /**
     * Get the fields default value.
     * 
     * A push button field does not have a field value.
     * 
     * @return This will always return an empty string.
     */
    @Override
    public String getDefaultValue()
    {
        // PushButton fields don't support the "DV" entry.
        return "";
    }    

    /**
     * Set the fields default value.
     * 
     * A push button field does not have a field value.
     * 
     * @param defaultValue The field doesn't support setting any value
     * @throws IllegalArgumentException when trying to set a value other than null
     */
    @Override
    public void setDefaultValue(String defaultValue)
    {
        if (defaultValue != null && !defaultValue.isEmpty())
        {
            throw new IllegalArgumentException("A PDPushButton shall not use the DV entry in the field dictionary");
        }
    }

    /**
     * Get the fields options.
     * 
     * A push button field does not have option value.
     * 
     * @return This will always return an empty List.
     */
    @Override
    public List<String> getOptions()
    {
        return Collections.<String>emptyList();
    }

    /**
     * Set the fields options.
     * 
     * A push button field does not have a option values.
     * 
     * @param values The field doesn't support setting any option value
     * @throws IllegalArgumentException when trying to set the a value other than null or an empty list.
     */
    @Override
    public void setOptions(List<String> values)
    {
        if (values != null && !values.isEmpty())
        {
            throw new IllegalArgumentException("A PDPushButton shall not use the Opt entry in the field dictionary");
        }
    }
    
    /**
     * Get the fields value.
     * 
     * A push button field does not have field value.
     * 
     * @return This will always return an empty String.
     */    
    @Override
    public String getValue()
    {
        // PushButton fields don't support the "V" entry.
        return "";
    }

    /**
     * Set the fields value.
     * 
     * A push button field does not have a field value.
     * 
     * @param fieldValue The field doesn't support setting any field value.
     * @throws IllegalArgumentException when trying to set the a value other than null or an empty String.
     */
    @Override
    public void setValue(String fieldValue)
    {
        if (fieldValue != null && !fieldValue.isEmpty())
        {
            throw new IllegalArgumentException("A PDPushButton shall not use the V entry in the field dictionary");
        }
    }
}
