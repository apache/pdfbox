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
import org.apache.pdfbox.cos.COSString;

/**
 * A text field is a box or space for text fill-in data typically entered from a keyboard.
 * The text may be restricted to a single line or may be permitted to span multiple lines
 *
 * @author sug
 */
public final class PDTextField extends PDVariableText
{
    private static final int FLAG_MULTILINE = 1 << 12;
    private static final int FLAG_PASSWORD = 1 << 13;
    private static final int FLAG_FILE_SELECT = 1 << 20;
    private static final int FLAG_DO_NOT_SPELL_CHECK = 1 << 22;
    private static final int FLAG_DO_NOT_SCROLL = 1 << 23;
    private static final int FLAG_COMB = 1 << 24;
    private static final int FLAG_RICH_TEXT = 1 << 25;
    
    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    public PDTextField(PDAcroForm acroForm)
    {
        super( acroForm );
        dictionary.setItem(COSName.FT, COSName.TX);
    }

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDTextField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super( acroForm, field, parent);
    }

    /**
     * @return true if the field is multiline
     */
    public boolean isMultiline()
    {
        return dictionary.getFlag(COSName.FF, FLAG_MULTILINE);
    }

    /**
     * Set the multiline bit.
     *
     * @param multiline The value for the multiline.
     */
    public void setMultiline( boolean multiline )
    {
        dictionary.setFlag(COSName.FF, FLAG_MULTILINE, multiline);
    }

    /**
     * @return true if the field is a password field.
     */
    public boolean isPassword()
    {
        return dictionary.getFlag(COSName.FF, FLAG_PASSWORD);
    }

    /**
     * Set the password bit.
     *
     * @param password The value for the password.
     */
    public void setPassword( boolean password )
    {
        dictionary.setFlag(COSName.FF, FLAG_PASSWORD, password);
    }

    /**
     * @return true if the field is a file select field.
     */
    public boolean isFileSelect()
    {
        return dictionary.getFlag(COSName.FF, FLAG_FILE_SELECT);
    }

    /**
     * Set the file select bit.
     *
     * @param fileSelect The value for the fileSelect.
     */
    public void setFileSelect( boolean fileSelect )
    {
        dictionary.setFlag(COSName.FF, FLAG_FILE_SELECT, fileSelect);
    }

    /**
     * @return true if the field is not suppose to spell check.
     */
    public boolean doNotSpellCheck()
    {
        return dictionary.getFlag(COSName.FF, FLAG_DO_NOT_SPELL_CHECK);
    }

    /**
     * Set the doNotSpellCheck bit.
     *
     * @param doNotSpellCheck The value for the doNotSpellCheck.
     */
    public void setDoNotSpellCheck( boolean doNotSpellCheck )
    {
        dictionary.setFlag(COSName.FF, FLAG_DO_NOT_SPELL_CHECK, doNotSpellCheck);
    }

    /**
     * @return true if the field is not suppose to scroll.
     */
    public boolean doNotScroll()
    {
        return dictionary.getFlag(COSName.FF, FLAG_DO_NOT_SCROLL);
    }

    /**
     * Set the doNotScroll bit.
     *
     * @param doNotScroll The value for the doNotScroll.
     */
    public void setDoNotScroll( boolean doNotScroll )
    {
        dictionary.setFlag(COSName.FF, FLAG_DO_NOT_SCROLL, doNotScroll);
    }

    /**
     * @return true if the field is not suppose to comb the text display.
     */
    public boolean isComb()
    {
        return dictionary.getFlag(COSName.FF, FLAG_COMB);
    }

    /**
     * Set the comb bit.
     *
     * @param comb The value for the comb.
     */
    public void setComb( boolean comb )
    {
        dictionary.setFlag(COSName.FF, FLAG_COMB, comb);
    }

    /**
     * @return true if the field is a rich text field.
     */
    public boolean isRichText()
    {
        return dictionary.getFlag(COSName.FF, FLAG_RICH_TEXT);
    }

    /**
     * Set the richText bit.
     *
     * @param richText The value for the richText.
     */
    public void setRichText( boolean richText )
    {
        dictionary.setFlag(COSName.FF, FLAG_RICH_TEXT, richText);
    }
    
    /**
     * Returns the maximum number of characters of the text field.
     * 
     * @return the maximum number of characters, returns -1 if the value isn't present
     */
    public int getMaxLen()
    {
        return dictionary.getInt(COSName.MAX_LEN);
    }

    /**
     * Sets the maximum number of characters of the text field.
     * 
     * @param maxLen the maximum number of characters
     */
    public void setMaxLen(int maxLen)
    {
        dictionary.setInt(COSName.MAX_LEN, maxLen);
    }
    
    @Override
    public void setDefaultValue(String value)
    {
        if (value != null)
        {
            COSString fieldValue = new COSString(value);
            setInheritableAttribute(COSName.DV, fieldValue);
        }  
        else
        {
            removeInheritableAttribute(COSName.DV);
        }
    }
    
    @Override
    public String getDefaultValue()
    {
        COSBase fieldValue = getInheritableAttribute(COSName.DV);
        if (fieldValue instanceof COSString)
        {
            return ((COSString) fieldValue).getString();
        }
        return "";
    }    
    
    @Override
    public void setValue(String value) throws IOException
    {
        if (value != null && !value.isEmpty())
        {
            COSString fieldValue = new COSString(value);
            setInheritableAttribute(COSName.V, fieldValue);
        }  
        else
        {
            removeInheritableAttribute(COSName.V);
        }
        
        // TODO move appearance generation out of fields PD model
        updateFieldAppearances();
    }
    
    @Override
    public String getValue() throws IOException
    {
        String string = valueToString(getInheritableAttribute(COSName.V));
        if (string != null) 
        {
            return string;
        }
        return "";
    }
}
