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

import java.io.IOException;

/**
 * A class for handling the PDF field as a signature.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDSignature extends PDField
{

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The dictionary for the signature.
     */
    public PDSignature( PDAcroForm theAcroForm, COSDictionary field)
    {
        super(theAcroForm,field);
    }

    /**
     * @see PDField#setValue(java.lang.String)
     *
     * @param value The new value for the field.
     *
     * @throws IOException If there is an error creating the appearance stream.
     */
    public void setValue(String value) throws IOException
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * @see PDField#setValue(java.lang.String)
     *
     * @return The string value of this field.
     *
     * @throws IOException If there is an error creating the appearance stream.
     */
    public String getValue() throws IOException
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * Return a string rep of this object.
     *
     * @return A string rep of this object.
     */
    public String toString()
    {
        return "PDSignature";
    }
}
