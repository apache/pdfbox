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
    PDButton(PDAcroForm acroForm, COSDictionary field)
    {
        super(acroForm, field);
    }

    /**
     * This will get the option values "Opt" entry of the pdf button.
     *
     * @return A list of java.lang.String values.
     */
    public List getOptions()
    {
        List retval = null;
        COSArray array = (COSArray)getDictionary().getDictionaryObject( COSName.getPDFName( "Opt" ) );
        if( array != null )
        {
            List strings = new ArrayList();
            for( int i=0; i<array.size(); i++ )
            {
                strings.add( ((COSString)array.getObject( i )).getString() );
            }
            retval = new COSArrayList( strings, array );
        }
        return retval;
    }

    /**
     * This will will set the list of options for this button.
     *
     * @param options The list of options for the button.
     */
    public void setOptions( List options )
    {
        getDictionary().setItem(
            COSName.getPDFName( "Opt" ),
            COSArrayList.converterToCOSArray( options ) );
    }
}
