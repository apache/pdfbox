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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * A class for handling the PDF field as a choicefield.
 *
 * @author sug
 * @version $Revision: 1.7 $
 */
public class PDChoiceField extends PDVariableText
{

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The field for this choice field.
     */
    public PDChoiceField( PDAcroForm theAcroForm, COSDictionary field)
    {
        super(theAcroForm, field);
    }

    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#setValue(java.lang.String)
     *
     * @param optionValue The new value for this text field.
     *
     * @throws IOException If there is an error calculating the appearance stream or the value in not one
     *   of the existing options.
     */
    public void setValue(String optionValue) throws IOException
    {
        int indexSelected = -1;
        COSArray options = (COSArray)getDictionary().getDictionaryObject( "Opt" );
        if( options.size() == 0 )
        {
            throw new IOException( "Error: You cannot set a value for a choice field if there are no options." );
        }
        else
        {
            // YXJ: Changed the order of the loops. Acrobat produces PDF's
            // where sometimes there is 1 string and the rest arrays.
            // This code works either way.
            for( int i=0; i<options.size() && indexSelected == -1; i++ ) {
                COSBase option = options.getObject( i );
                if( option instanceof COSArray )
                {
                    COSArray keyValuePair = (COSArray)option;
                    COSString key = (COSString)keyValuePair.getObject( 0 );
                    COSString value = (COSString)keyValuePair.getObject( 1 );
                    if( optionValue.equals( key.getString() ) || optionValue.equals( value.getString() ) )
                    {
                        //have the parent draw the appearance stream with the value
                        super.setValue( value.getString() );
                        //but then use the key as the V entry
                        getDictionary().setItem( COSName.getPDFName( "V" ), key );
                        indexSelected = i;
                    }
                }
                else
                {
                    COSString value = (COSString)option;
                    if( optionValue.equals( value.getString() ) )
                    {
                        super.setValue( optionValue );
                        indexSelected = i;
                    }
                }
            }
        }
        if( indexSelected == -1 )
        {
            throw new IOException( "Error: '" + optionValue + "' was not an available option.");
        }
        else
        {
            COSArray indexArray = (COSArray)getDictionary().getDictionaryObject( "I" );
            if( indexArray != null )
            {
                indexArray.clear();
                indexArray.add( COSInteger.get( indexSelected ) );
            }
        }
    }

}
