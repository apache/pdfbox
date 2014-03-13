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
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;

import java.io.IOException;

/**
 * Base class for fields which use "Variable Text".
 * These fields construct an appearance stream dynamically at viewing time.
 *
 * @author Ben Litchfield
 */
public abstract class PDVariableText extends PDField    // TODO mixin, not really a field
{
    /**
     * A Ff flag.
     */
    public static final int FLAG_MULTILINE = 1 << 12;
    /**
     * A Ff flag.
     */
    public static final int FLAG_PASSWORD = 1 << 13;
    /**
     * A Ff flag.
     */
    public static final int FLAG_FILE_SELECT = 1 << 20;
    /**
     * A Ff flag.
     */
    public static final int FLAG_DO_NOT_SPELL_CHECK = 1 << 22;
    /**
     * A Ff flag.
     */
    public static final int FLAG_DO_NOT_SCROLL = 1 << 23;
    /**
     * A Ff flag.
     */
    public static final int FLAG_COMB = 1 << 24;
    /**
     * A Ff flag.
     */
    public static final int FLAG_RICH_TEXT = 1 << 25;


    /**
     * DA    Default appearance.
     */
    private COSString da;

    private PDAppearanceString appearance;


    /**
     * A Q value.
     */
    public static final int QUADDING_LEFT = 0;

    /**
     * A Q value.
     */
    public static final int QUADDING_CENTERED = 1;

    /**
     * A Q value.
     */
    public static final int QUADDING_RIGHT = 2;

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroform.
     */
    PDVariableText(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
    }

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The field's dictionary.
     */
    PDVariableText(PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
        da = (COSString) field.getDictionaryObject(COSName.DA);
    }

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#setValue(java.lang.String)
     *
     * @param value The new value for this text field.
     *
     * @throws IOException If there is an error calculating the appearance stream.
     */
    public void setValue(String value) throws IOException
    {
        COSString fieldValue = new COSString(value);
        getDictionary().setItem( COSName.V, fieldValue );

        //hmm, not sure what the case where the DV gets set to the field
        //value, for now leave blank until we can come up with a case
        //where it needs to be in there
        //getDictionary().setItem( COSName.getPDFName( "DV" ), fieldValue );
        if(appearance == null)
        {
            this.appearance = new PDAppearanceString( getAcroForm(), this );
        }
        appearance.setAppearanceValue(value);
    }

    /**
     * getValue gets the fields value to as a string.
     *
     * @return The string value of this field.
     *
     * @throws IOException If there is an error getting the value.
     */
    public String getValue() throws IOException
    {
        return getDictionary().getString( COSName.V );
    }

    /**
     * @return true if the field is multiline
     */
    public boolean isMultiline()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_MULTILINE );
    }

    /**
     * Set the multiline bit.
     *
     * @param multiline The value for the multiline.
     */
    public void setMultiline( boolean multiline )
    {
        getDictionary().setFlag( COSName.FF, FLAG_MULTILINE, multiline );
    }

    /**
     * @return true if the field is a password field.
     */
    public boolean isPassword()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_PASSWORD );
    }

    /**
     * Set the password bit.
     *
     * @param password The value for the password.
     */
    public void setPassword( boolean password )
    {
        getDictionary().setFlag( COSName.FF, FLAG_PASSWORD, password );
    }

    /**
     * @return true if the field is a file select field.
     */
    public boolean isFileSelect()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_FILE_SELECT );
    }

    /**
     * Set the file select bit.
     *
     * @param fileSelect The value for the fileSelect.
     */
    public void setFileSelect( boolean fileSelect )
    {
        getDictionary().setFlag( COSName.FF, FLAG_FILE_SELECT, fileSelect );
    }

    /**
     * @return true if the field is not suppose to spell check.
     */
    public boolean doNotSpellCheck()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_DO_NOT_SPELL_CHECK );
    }

    /**
     * Set the doNotSpellCheck bit.
     *
     * @param doNotSpellCheck The value for the doNotSpellCheck.
     */
    public void setDoNotSpellCheck( boolean doNotSpellCheck )
    {
        getDictionary().setFlag( COSName.FF, FLAG_DO_NOT_SPELL_CHECK, doNotSpellCheck );
    }

    /**
     * @return true if the field is not suppose to scroll.
     */
    public boolean doNotScroll()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_DO_NOT_SCROLL );
    }

    /**
     * Set the doNotScroll bit.
     *
     * @param doNotScroll The value for the doNotScroll.
     */
    public void setDoNotScroll( boolean doNotScroll )
    {
        getDictionary().setFlag( COSName.FF, FLAG_DO_NOT_SCROLL, doNotScroll );
    }

    /**
     * @return true if the field is not suppose to comb the text display.
     */
    public boolean shouldComb()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_COMB );
    }

    /**
     * Set the comb bit.
     *
     * @param comb The value for the comb.
     */
    public void setComb( boolean comb )
    {
        getDictionary().setFlag( COSName.FF, FLAG_COMB, comb );
    }

    /**
     * @return true if the field is a rich text field.
     */
    public boolean isRichText()
    {
        return getDictionary().getFlag( COSName.FF, FLAG_RICH_TEXT );
    }

    /**
     * Set the richText bit.
     *
     * @param richText The value for the richText.
     */
    public void setRichText( boolean richText )
    {
        getDictionary().setFlag( COSName.FF, FLAG_RICH_TEXT, richText );
    }

    /**
     * @return the DA element of the dictionary object
     */
    protected COSString getDefaultAppearance()
    {
        return da;
    }

    /**
     * This will get the 'quadding' or justification of the text to be displayed.
     * 0 - Left(default)<br/>
     * 1 - Centered<br />
     * 2 - Right<br />
     * Please see the QUADDING_CONSTANTS.
     *
     * @return The justification of the text strings.
     */
    public int getQ()
    {
        int retval = 0;
        COSNumber number = (COSNumber)getDictionary().getDictionaryObject( COSName.Q );
        if( number != null )
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
    public void setQ( int q )
    {
        getDictionary().setInt( COSName.Q, q );
    }
}
