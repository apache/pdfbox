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
package org.apache.pdfbox.pdmodel.interactive.action.type;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This is the implementation of an URI dictionary.
 *
 * @version $Revision: 1.0 $
 *
 */
public class PDURIDictionary implements COSObjectable
{

    private COSDictionary uriDictionary;

    /**
     * Constructor.
     * 
     */
    public PDURIDictionary()
    {
        this.uriDictionary = new COSDictionary();
    }

    /**
     * Constructor.
     * 
     * @param dictionary the corresponding dictionary
     */
    public PDURIDictionary(COSDictionary dictionary)
    {
        this.uriDictionary = dictionary;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return this.uriDictionary;
    }

    /**
     * Returns the corresponding dictionary.
     * @return the dictionary
     */
    public COSDictionary getDictionary()
    {
        return this.uriDictionary;
    }

    /**
     * This will get the base URI to be used in resolving relative URI references.
     * URI actions within the document may specify URIs in partial form, to be interpreted
     * relative to this base address. If no base URI is specified, such partial URIs
     * will be interpreted relative to the location of the document itself.
     * The use of this entry is parallel to that of the body element &lt;BASE&gt;, as described
     * in the HTML 4.01 Specification.
     *
     * @return The URI entry of the specific URI dictionary.
     */
    public String getBase()
    {
        return this.getDictionary().getString("Base");
    }

    /**
     * This will set the base URI to be used in resolving relative URI references.
     * URI actions within the document may specify URIs in partial form, to be interpreted
     * relative to this base address. If no base URI is specified, such partial URIs
     * will be interpreted relative to the location of the document itself.
     * The use of this entry is parallel to that of the body element &lt;BASE&gt;, as described
     * in the HTML 4.01 Specification.
     *
     * @param base The the base URI to be used.
     */
    public void setBase(String base)
    {
        this.getDictionary().setString("Base", base);
    }

}
