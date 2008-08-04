/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A class for handling the PDF field as a checkbox.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author sug
 * @version $Revision: 1.11 $
 */
public class PDCheckbox extends PDChoiceButton
{
    private static final COSName KEY = COSName.getPDFName("AS");
    private static final COSName OFF_VALUE = COSName.getPDFName("Off");

    private COSName value;

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The checkbox field dictionary
     */
    public PDCheckbox( PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
        COSDictionary ap = (COSDictionary) field.getDictionaryObject(COSName.getPDFName("AP"));
        if( ap != null )
        {
            COSBase n = ap.getDictionaryObject(COSName.getPDFName("N"));
    
            if( n instanceof COSDictionary )
            {
                List li = ((COSDictionary)n).keyList();
                for( int i=0; i<li.size(); i++ )
                {
                    COSName name = (COSName)li.get( i );
                    if( !name.equals( OFF_VALUE ))
                    {
                        value = name;
                    }
                }
                
            }
        }
        else
        {
            value = (COSName)getDictionary().getDictionaryObject( "V" );
        }
    }
    
    /**
     * This will tell if this radio button is currently checked or not.
     * 
     * @return true If the radio button is checked.
     */
    public boolean isChecked()
    {
        boolean retval = false;
        String onValue = getOnValue();
        COSName radioValue = (COSName)getDictionary().getDictionaryObject( KEY );
        if( radioValue != null && value != null && radioValue.getName().equals( onValue ) )
        {
            retval = true;
        }
        
        return retval;
    }

    /**
     * Checks the radiobutton.
     */
    public void check()
    {
        getDictionary().setItem(KEY, value);
    }

    /**
     * Unchecks the radiobutton.
     */
    public void unCheck()
    {
        getDictionary().setItem(KEY, OFF_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String newValue)
    {
        getDictionary().setName( "V", newValue );
        if( newValue == null )
        {
            getDictionary().setItem( KEY, OFF_VALUE );
        }
        else
        {
            getDictionary().setName( KEY, newValue );
        }
    }
    
    /**
     * This will get the value of the radio button.
     * 
     * @return The value of the radio button.
     */
    public String getOffValue()
    {
        return OFF_VALUE.getName();
    }
    
    /**
     * This will get the value of the radio button.
     * 
     * @return The value of the radio button.
     */
    public String getOnValue()
    {
        String retval = null;
        COSDictionary ap = (COSDictionary) getDictionary().getDictionaryObject(COSName.getPDFName("AP"));
        COSBase n = ap.getDictionaryObject(COSName.getPDFName("N"));

        //N can be a COSDictionary or a COSStream
        if( n instanceof COSDictionary )
        {
            Iterator li = ((COSDictionary)n).keyList().iterator();
            while( li.hasNext() )
            {
                Object key = li.next();
                if( !key.equals( OFF_VALUE) )
                {
                    retval = ((COSName)key).getName();
                }
            }
        }
        return retval;
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
        return getDictionary().getNameAsString( "V" );
    }

}