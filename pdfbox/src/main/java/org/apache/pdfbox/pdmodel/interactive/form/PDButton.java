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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * A button field represents an interactive control on the screen
 * that the user can manipulate with the mouse.
 *
 * @author sug
 */
public abstract class PDButton extends PDField
{

    /**
     * A Ff flag. If set, the field is a set of radio buttons
     */
    public static final int FLAG_RADIO = 1 << 15;
    /**
     * A Ff flag. If set, the field is a pushbutton.
     */
    public static final int FLAG_PUSHBUTTON = 1 << 16;
    /**
     * A Ff flag. If set, radio buttons individual fields, using the same
     * value for the on state will turn on and off in unison.
     */
    public static final int FLAG_RADIOS_IN_UNISON = 1 << 25;

    
    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroform.
     */
    PDButton(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    protected PDButton(PDAcroForm acroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(acroForm, field, parentNode);
    }

    /**
     * This will get the option values "Opt" entry of the pdf button.
     *
     * @return A list of java.lang.String values.
     */
    public List<String> getOptions()
    {
        List<String> retval = null;
        COSArray array = (COSArray)getDictionary().getDictionaryObject( COSName.OPT );
        if( array != null )
        {
            List<String> strings = new ArrayList<String>();
            for( int i=0; i<array.size(); i++ )
            {
                strings.add( ((COSString)array.getObject( i )).getString() );
            }
            retval = new COSArrayList<String>( strings, array );
        }
        return retval;
    }


    /**
     * Set the field options values.
     * 
     * The fields options represent the export value of each annotation in the field. 
     * It may be used to:
     * <ul>
     *  <li>represent the export values in non-Latin writing systems.</li>
     *  <li>allow radio buttons to be checked independently, even 
     *  if they have the same export value.</li>
     * </ul>
     * 
     * Providing an empty list or null will remove the entry.
     * 
     * @param options The list of options for the button.
     */
    public void setOptions( List<String> options )
    {
        if (options == null || options.isEmpty())
        {
            getDictionary().removeItem(COSName.OPT);
        }
        else
        {
            getDictionary().setItem(COSName.OPT, COSArrayList.converterToCOSArray( options ) );
        }
    }
}
