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
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDResources;

/**
 * Base class for fields which use "Variable Text".
 * These fields construct an appearance stream dynamically at viewing time.
 *
 * @author Ben Litchfield
 */
public abstract class PDVariableText extends PDTerminalField
{
    static final int QUADDING_LEFT = 0;
    static final int QUADDING_CENTERED = 1;
    static final int QUADDING_RIGHT = 2;

    /**
     * @see PDTerminalField#PDTerminalField(PDAcroForm)
     *
     * @param acroForm The acroform.
     */
    PDVariableText(PDAcroForm acroForm)
    {
        super(acroForm);
    }

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node
     */
    PDVariableText(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }

    /**
     * Get the default appearance.
     * 
     * This is an inheritable attribute.
     * 
     * The default appearance contains a set of default graphics and text operators
     * to define the field’s text size and color.
     * 
     * @return the DA element of the dictionary object
     */
    public String getDefaultAppearance()
    {
        COSString defaultAppearance = (COSString) getInheritableAttribute(COSName.DA);
        return defaultAppearance.getString();
    }

    /**
     * Get the default appearance.
     *
     * This is an inheritable attribute.
     *
     * The default appearance contains a set of default graphics and text operators
     * to define the field’s text size and color.
     *
     * @return the DA element of the dictionary object
     */
    PDDefaultAppearanceString getDefaultAppearanceString() throws IOException
    {
        COSString da = (COSString) getInheritableAttribute(COSName.DA);
        PDResources dr = getAcroForm().getDefaultResources();
        return new PDDefaultAppearanceString(da, dr);
    }

    /**
     * Set the default appearance.
     * 
     * This will set the local default appearance for the variable text field only not 
     * affecting a default appearance in the parent hierarchy.
     * 
     * Providing null as the value will remove the local default appearance.
     * 
     * @param daValue a string describing the default appearance
     */
    public void setDefaultAppearance(String daValue)
    {
        getCOSObject().setString(COSName.DA, daValue);
    }

    /**
     * Get the default style string.
     * 
     * The default style string defines the default style for
     * rich text fields.
     * 
     * @return the DS element of the dictionary object
     */
    public String getDefaultStyleString()
    {
        COSString defaultStyleString = (COSString) getCOSObject().getDictionaryObject(COSName.DS);
        return defaultStyleString.getString();
    }

    /**
     * Set the default style string.
     * 
     * Providing null as the value will remove the default style string.
     * 
     * @param defaultStyleString a string describing the default style.
     */
    public void setDefaultStyleString(String defaultStyleString)
    {
        if (defaultStyleString != null)
        {
            getCOSObject().setItem(COSName.DS, new COSString(defaultStyleString));
        }
        else
        {
            getCOSObject().removeItem(COSName.DS);
        }
    }    

    /**
     * This will get the 'quadding' or justification of the text to be displayed.
     * 
     * This is an inheritable attribute.
     * 
     * 0 - Left(default)<br>
     * 1 - Centered<br>
     * 2 - Right<br>
     * Please see the QUADDING_CONSTANTS.
     *
     * @return The justification of the text strings.
     */
    public int getQ()
    {
        int retval = 0;

        COSNumber number = (COSNumber)getInheritableAttribute(COSName.Q);
        
        if (number != null)
        {
            retval = number.intValue();
        }
        return retval;
    }

    /**
     * This will set the quadding/justification of the text.  See QUADDING constants.
     *
     * @param q The new text justification.
     */
    public void setQ(int q)
    {
        getCOSObject().setInt(COSName.Q, q);
    }
    
    /**
     * Get the fields rich text value.
     * 
     * @return the rich text value string
     * @throws IOException if the field dictionary entry is not a text type
     */
    public String getRichTextValue() throws IOException
    {
        return getStringOrStream(getInheritableAttribute(COSName.RV));
    }
    
    /**
     * Set the fields rich text value.
     * 
     * <p>
     * Setting the rich text value will not generate the appearance
     * for the field.
     * <br>
     * You can set {@link PDAcroForm#setNeedAppearances(Boolean)} to
     * signal a conforming reader to generate the appearance stream.
     * </p>
     * 
     * Providing null as the value will remove the default style string.
     * 
     * @param richTextValue a rich text string
     */
    public void setRichTextValue(String richTextValue)
    {
        if (richTextValue != null)
        {
            getCOSObject().setItem(COSName.RV, new COSString(richTextValue));
        }
        else
        {
            getCOSObject().removeItem(COSName.RV);
        }        
    }

    /**
     * Get a text as text stream.
     *
     * Some dictionary entries allow either a text or a text stream.
     *
     * @param base the potential text or text stream
     * @return the text stream
     */
    protected final String getStringOrStream(COSBase base)
    {
        if (base == null)
        {
            return "";
        }
        else if (base instanceof COSString)
        {
            return ((COSString)base).getString();
        }
        else if (base instanceof COSStream)
        {
            return ((COSStream)base).toTextString();
        }
        else
        {
            return "";
        }
    }
}
