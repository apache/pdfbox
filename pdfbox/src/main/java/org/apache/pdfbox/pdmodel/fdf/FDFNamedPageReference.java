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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * This represents an FDF named page reference that is part of the FDF field.
 *
 * @author Ben Litchfield
 */
public class FDFNamedPageReference implements COSObjectable
{
    private final COSDictionary ref;

    /**
     * Default constructor.
     */
    public FDFNamedPageReference()
    {
        ref = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param r The FDF named page reference dictionary.
     */
    public FDFNamedPageReference(COSDictionary r)
    {
        ref = r;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return ref;
    }

    /**
     * This will get the name of the referenced page. A required parameter.
     *
     * @return The name of the referenced page.
     */
    public String getName()
    {
        return ref.getString(COSName.NAME);
    }

    /**
     * This will set the name of the referenced page.
     *
     * @param name The referenced page name.
     */
    public void setName(String name)
    {
        ref.setString(COSName.NAME, name);
    }

    /**
     * This will get the file specification of this reference. An optional parameter.
     *
     * @return The F entry for this dictionary.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFileSpecification() throws IOException
    {
        return PDFileSpecification.createFS(ref.getDictionaryObject(COSName.F));
    }

    /**
     * This will set the file specification for this named page reference.
     *
     * @param fs The file specification to set.
     */
    public void setFileSpecification(PDFileSpecification fs)
    {
        ref.setItem(COSName.F, fs);
    }
}
