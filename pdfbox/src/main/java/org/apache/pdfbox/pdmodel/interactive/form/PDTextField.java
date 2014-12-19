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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.PDTextStream;

/**
 * A text field is a box or space for text fill-in data typically entered from a keyboard.
 * The text may be restricted to a single line or may be permitted to span multiple lines
 *
 * @author sug
 */
public final class PDTextField extends PDVariableText
{
    /**
     * @see PDFieldTreeNode#PDFieldTreeNode(PDAcroForm)
     *
     * @param theAcroForm The acroform.
     */
    public PDTextField(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
    }

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDTextField(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super( theAcroForm, field, parentNode);
    }
    
    /**
     * Returns the maximum number of characters of the text field.
     * 
     * @return the maximum number of characters, returns -1 if the value isn't present
     */
    public int getMaxLen()
    {
        return getDictionary().getInt(COSName.MAX_LEN);
    }

    /**
     * Sets the maximum number of characters of the text field.
     * 
     * @param maxLen the maximum number of characters
     */
    public void setMaxLen(int maxLen)
    {
        getDictionary().setInt(COSName.MAX_LEN, maxLen);
    }

    /**
     * setValue sets the default value for the field.
     * 
     * @param value the default value
     * 
     */
    public void setDefaultValue(String value)
    {
        if (value != null)
        {
            COSString fieldValue = new COSString(value);
            setInheritableAttribute(COSName.DV, fieldValue);
            // TODO stream instead of string
        }  
        else
        {
            removeInheritableAttribute(COSName.DV);
        }
    }
    
    /**
     * getValue gets the value of the "V" entry.
     * 
     * @return The value of this entry.
     * 
     */
    @Override
    public String getDefaultValue()
    {
        COSBase fieldValue = getInheritableAttribute(getDictionary(), COSName.DV);
        if (fieldValue instanceof COSString)
        {
            return ((COSString) fieldValue).getString();
        }
        // TODO handle PDTextStream, IOException in case of wrong type
        return null;
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
            COSString fieldValue = new COSString(value);
            setInheritableAttribute(COSName.V, fieldValue);
            // TODO stream instead of string
        }  
        else
        {
            removeInheritableAttribute(COSName.DV);
        }
        
        // TODO move appearance generation out of fields PD model
        updateFieldAppearances();
    }

    /**
     * getValue gets the value of the "V" entry.
     * 
     * @return The value of this entry.
     * @throws IOException 
     * 
     */
    @Override
    public String getValue() throws IOException
    {
        PDTextStream textStream = getAsTextStream(getInheritableAttribute(getDictionary(), COSName.V));

        if (textStream != null) 
        {
            return textStream.getAsString();
        }
        return null;
    }
}
