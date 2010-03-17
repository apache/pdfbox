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

import org.apache.pdfbox.cos.COSDictionary;

/**
 * This class represents a form field with an unknown type.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDUnknownField extends PDField
{
    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm, COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The field's dictionary.
     */
    public PDUnknownField( PDAcroForm theAcroForm, COSDictionary field)
    {
        super( theAcroForm, field);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String value) throws IOException
    {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() throws IOException
    {
        return null;
    }

}
