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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import java.io.IOException;

import java.util.List;

/**
 * This is the Factory for creating and returning the correct
 * field elements.
 *
 * @author sug
 * @version $Revision: 1.8 $
 */
public class PDFieldFactory
{
    private static final int RADIO_BITMASK = 32768;
    private static final int PUSHBUTTON_BITMASK = 65536;
    private static final int RADIOS_IN_UNISON_BITMASK = 33554432;

    private static final String FIELD_TYPE_BTN = "Btn";
    private static final String FIELD_TYPE_TX = "Tx";
    private static final String FIELD_TYPE_CH = "Ch";
    private static final String FIELD_TYPE_SIG = "Sig";

    /**
     * Utility class so no constructor.
     */
    private PDFieldFactory()
    {
        //do nothing.
    }

    /**
     * This method creates a COSField subclass from the given field.
     * The field is a PDF Dictionary object that must represent a
     * field element. - othewise null is returned
     *
     * @param acroForm The form that the field will be part of.
     * @param field The dictionary representing a field element
     *
     * @return a subclass to COSField according to the kind of field passed to createField
     * @throws IOException If there is an error determining the field type.
     */
    public static PDField createField( PDAcroForm acroForm, COSDictionary field) throws IOException
    {
        PDField pdField = new PDUnknownField( acroForm, field );
        if( isButton(pdField) )
        {
            int flags = pdField.getFieldFlags();
            //BJL, I have found that the radio flag bit is not always set
            //and that sometimes there is just a kids dictionary.
            //so, if there is a kids dictionary then it must be a radio button
            //group.
            COSArray kids = (COSArray)field.getDictionaryObject( COSName.getPDFName( "Kids" ) );
            if( kids != null || isRadio(flags) )
            {
                pdField = new PDRadioCollection( acroForm, field );
            }
            else if( isPushButton( flags ) )
            {
                pdField = new PDPushButton( acroForm, field );
            }
            else
            {
                pdField = new PDCheckbox( acroForm, field );
            }

        }
        else if (isChoiceField(pdField))
        {
            pdField = new PDChoiceField( acroForm, field );
        }
        else if (isTextbox(pdField))
        {
            pdField = new PDTextbox( acroForm, field );
        }
        else if( isSignature( pdField ) )
        {
            pdField = new PDSignatureField( acroForm, field );
        }
        else
        {
            //do nothing and return an unknown field type.
        }
        return pdField;
    }

    /**
     * This method determines if the given
     * field is a radiobutton collection.
     *
     * @param flags The field flags.
     *
     * @return the result of the determination
     */
    private static boolean isRadio( int flags )
    {
        return (flags & RADIO_BITMASK) > 0;
    }

    /**
     * This method determines if the given
     * field is a pushbutton.
     *
     * @param flags The field flags.
     *
     * @return the result of the determination
     */
    private static boolean isPushButton( int flags )
    {
        return (flags & PUSHBUTTON_BITMASK) > 0;
    }

    /**
     * This method determines if the given field is a choicefield
     * Choicefields are either listboxes or comboboxes.
     *
     * @param field the field to determine
     * @return the result of the determination
     */
    private static boolean isChoiceField(PDField field) throws IOException
    {
        return FIELD_TYPE_CH.equals(field.findFieldType());
    }

    /**
     * This method determines if the given field is a button.
     *
     * @param field the field to determine
     * @return the result of the determination
     *
     * @throws IOException If there is an error determining the field type.
     */
    private static boolean isButton(PDField field) throws IOException
    {
        String ft = field.findFieldType();
        boolean retval = FIELD_TYPE_BTN.equals( ft );
        List kids = field.getKids();
        if( ft == null && kids != null && kids.size() > 0)
        {
            //sometimes if it is a button the type is only defined by one
            //of the kids entries
            Object obj = kids.get( 0 );
            COSDictionary kidDict = null;
            if( obj instanceof PDField )
            {
                kidDict = ((PDField)obj).getDictionary();
            }
            else if( obj instanceof PDAnnotationWidget )
            {
                kidDict = ((PDAnnotationWidget)obj).getDictionary();
            }
            else
            {
                throw new IOException( "Error:Unexpected type of kids field:" + obj );
            }
            retval = isButton( new PDUnknownField( field.getAcroForm(), kidDict ) );
        }
        return retval;
    }

   /**
     * This method determines if the given field is a signature.
     *
     * @param field the field to determine
     * @return the result of the determination
     */
    private static boolean isSignature(PDField field) throws IOException
    {
        return FIELD_TYPE_SIG.equals(field.findFieldType());
    }

    /**
     * This method determines if the given field is a Textbox.
     *
     * @param field the field to determine
     * @return the result of the determination
     */
    private static boolean isTextbox(PDField field) throws IOException
    {
        return FIELD_TYPE_TX.equals(field.findFieldType());
    }
}
