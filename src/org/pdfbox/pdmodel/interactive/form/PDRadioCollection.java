/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.util.BitFlagHelper;

/**
 * A class for handling the PDF field as a Radio Collection.
 * This class automatically keeps track of the child radio buttons
 * in the collection.
 *
 * @see PDCheckbox
 * @author sug
 * @version $Revision: 1.13 $
 */
public class PDRadioCollection extends PDChoiceButton
{
    /**
     * A Ff flag.
     */
    public static final int FLAG_RADIOS_IN_UNISON = 1 << 25;
    
    /** 
     * @param theAcroForm The acroForm for this field.
     * @param field The field that makes up the radio collection.
     * 
     * {@inheritDoc}
     */
    public PDRadioCollection( PDAcroForm theAcroForm, COSDictionary field)
    {
        super(theAcroForm,field);
    }
    
    /**
     * From the PDF Spec <br/>
     * If set, a group of radio buttons within a radio button field that
     * use the same value for the on state will turn on and off in unison; that is if
     * one is checked, they are all checked. If clear, the buttons are mutually exclusive
     * (the same behavior as HTML radio buttons).
     *
     * @param radiosInUnison The new flag for radiosInUnison.
     */
    public void setRadiosInUnison(boolean radiosInUnison)
    {
        BitFlagHelper.setFlag( getDictionary(), "Ff", FLAG_RADIOS_IN_UNISON, radiosInUnison );
    }

    /**
     *
     * @return true If the flag is set for radios in unison.
     */
    public boolean isRadiosInUnison()
    {
        return BitFlagHelper.getFlag( getDictionary(), "Ff", FLAG_RADIOS_IN_UNISON );
    }

    /**
     * This setValue method iterates the collection of radiobuttons
     * and checks or unchecks each radiobutton according to the
     * given value.
     * If the value is not represented by any of the radiobuttons,
     * then none will be checked.
     *
     * {@inheritDoc}
     */
    public void setValue(String value) throws IOException
    {
        getDictionary().setString( "V", value );
        List kids = getKids();
        for (int i = 0; i < kids.size(); i++)
        {
            PDCheckbox btn = (PDCheckbox)kids.get(i);
            if( btn.getOnValue().equals(value) )
            {
                btn.check();
            }
            else
            {
                btn.unCheck();
            }
        }
    }
    
    /**
     * getValue gets the fields value to as a string.
     *
     * @return The string value of this field.
     *
     * @throws IOException If there is an error getting the value.
     */
    public String getValue()throws IOException
    {
        String retval = null;
        List kids = getKids();
        for (int i = 0; i < kids.size(); i++)
        {
            PDCheckbox btn = (PDCheckbox)kids.get(i);
            if( btn.isChecked() )
            {
                retval = btn.getOnValue();
            }
        }
        if( retval == null )
        {
            retval = getDictionary().getNameAsString( "V" );
        }
        return retval;
    }


    /**
     * This will return a list of PDField objects that are part of this radio collection.
     *
     * @see PDField#getWidget()
     * @return A list of PDWidget objects.
     * @throws IOException if there is an error while creating the children objects.
     */
    public List getKids() throws IOException
    {
        List retval = null;
        COSArray kids = (COSArray)getDictionary().getDictionaryObject(COSName.KIDS);
        if( kids != null )
        {
            List kidsList = new ArrayList();
            for (int i = 0; i < kids.size(); i++)
            {
                kidsList.add( PDFieldFactory.createField( getAcroForm(), (COSDictionary)kids.getObject(i) ) );
            }
            retval = new COSArrayList( kidsList, kids );
        }
        return retval;
    }
}