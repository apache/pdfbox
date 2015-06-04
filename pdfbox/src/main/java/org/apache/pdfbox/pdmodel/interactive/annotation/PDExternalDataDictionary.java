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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents an external data dictionary.
 * 
 */
public class PDExternalDataDictionary implements COSObjectable
{

    private final COSDictionary dataDictionary;

    /**
     * Constructor.
     */
    public PDExternalDataDictionary()
    {
        this.dataDictionary = new COSDictionary();
        this.dataDictionary.setName(COSName.TYPE, "ExData");
    }

    /**
     * Constructor.
     * 
     * @param dictionary Dictionary
     */
    public PDExternalDataDictionary(COSDictionary dictionary)
    {
        this.dataDictionary = dictionary;
    }

    /**
     * returns the dictionary.
     *
     * @return the dictionary
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return this.dataDictionary;
    }

    /**
     * returns the type of the external data dictionary. It must be "ExData", if present
     * 
     * @return the type of the external data dictionary
     */
    public String getType()
    {
        return this.getCOSObject().getNameAsString(COSName.TYPE, "ExData");
    }

    /**
     * returns the subtype of the external data dictionary.
     * 
     * @return the subtype of the external data dictionary
     */
    public String getSubtype()
    {
        return this.getCOSObject().getNameAsString(COSName.SUBTYPE);
    }

    /**
     * This will set the subtype of the external data dictionary.
     * 
     * @param subtype the subtype of the external data dictionary
     */
    public void setSubtype(String subtype)
    {
        this.getCOSObject().setName(COSName.SUBTYPE, subtype);
    }

}
