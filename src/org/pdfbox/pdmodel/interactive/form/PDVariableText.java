/**
 * Copyright (c) 2004-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.interactive.form;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.cos.COSString;
import org.pdfbox.util.BitFlagHelper;

import java.io.IOException;

/**
 * A class for handling PDF fields that display text.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public abstract class PDVariableText extends PDField
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

    private PDAppearance appearance;


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
    public PDVariableText( PDAcroForm theAcroForm )
    {
        super( theAcroForm );
    }
    
    /**
     * @see org.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The field's dictionary.
     */
    public PDVariableText( PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
        da = (COSString) field.getDictionaryObject(COSName.getPDFName("DA"));
    }

    /**
     * @see org.pdfbox.pdmodel.interactive.form.PDField#setValue(java.lang.String)
     *
     * @param value The new value for this text field.
     *
     * @throws IOException If there is an error calculating the appearance stream.
     */
    public void setValue(String value) throws IOException
    {
        COSString fieldValue = new COSString(value);
        getDictionary().setItem( COSName.getPDFName( "V" ), fieldValue );

        //hmm, not sure what the case where the DV gets set to the field
        //value, for now leave blank until we can come up with a case
        //where it needs to be in there
        //getDictionary().setItem( COSName.getPDFName( "DV" ), fieldValue );
        if(appearance == null)
        {
            this.appearance = new PDAppearance( getAcroForm(), this );
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
        return getDictionary().getString( "V" );
    }

    /**
     * @return true if the field is multiline
     */
    public boolean isMultiline()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_MULTILINE );
    }
    
    /**
     * Set the multiline bit.
     * 
     * @param multiline The value for the multiline.
     */
    public void setMultiline( boolean multiline )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_MULTILINE, multiline );
    }
    
    /**
     * @return true if the field is a password field.
     */
    public boolean isPassword()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_PASSWORD );
    }
    
    /**
     * Set the password bit.
     * 
     * @param password The value for the password.
     */
    public void setPassword( boolean password )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_PASSWORD, password );
    }
    
    /**
     * @return true if the field is a file select field.
     */
    public boolean isFileSelect()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_FILE_SELECT );
    }
    
    /**
     * Set the file select bit.
     * 
     * @param fileSelect The value for the fileSelect.
     */
    public void setFileSelect( boolean fileSelect )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_FILE_SELECT, fileSelect );
    }
    
    /**
     * @return true if the field is not suppose to spell check.
     */
    public boolean doNotSpellCheck()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_DO_NOT_SPELL_CHECK );
    }
    
    /**
     * Set the doNotSpellCheck bit.
     * 
     * @param doNotSpellCheck The value for the doNotSpellCheck.
     */
    public void setDoNotSpellCheck( boolean doNotSpellCheck )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_DO_NOT_SPELL_CHECK, doNotSpellCheck );
    }
    
    /**
     * @return true if the field is not suppose to scroll.
     */
    public boolean doNotScroll()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_DO_NOT_SCROLL );
    }
    
    /**
     * Set the doNotScroll bit.
     * 
     * @param doNotScroll The value for the doNotScroll.
     */
    public void setDoNotScroll( boolean doNotScroll )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_DO_NOT_SCROLL, doNotScroll );
    }
    
    /**
     * @return true if the field is not suppose to comb the text display.
     */
    public boolean shouldComb()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_COMB );
    }
    
    /**
     * Set the comb bit.
     * 
     * @param comb The value for the comb.
     */
    public void setComb( boolean comb )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_COMB, comb );
    }
    
    /**
     * @return true if the field is a rich text field.
     */
    public boolean isRichText()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_RICH_TEXT );
    }
    
    /**
     * Set the richText bit.
     * 
     * @param richText The value for the richText.
     */
    public void setRichText( boolean richText )
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_RICH_TEXT, richText );
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
        COSNumber number = (COSNumber)getDictionary().getDictionaryObject( COSName.getPDFName( "Q" ) );
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
        getDictionary().setItem( COSName.getPDFName( "Q" ), new COSInteger( q ) );
    }

}