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
package org.apache.pdfbox.pdmodel.common;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A wrapper for a COS dictionary including Type information.
 *
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 *
 */
public class PDTypedDictionaryWrapper extends PDDictionaryWrapper
{

    /**
     * Creates a new instance with a given type.
     * 
     * @param type the type (Type)
     */
    public PDTypedDictionaryWrapper(String type)
    {
        super();
        this.getCOSDictionary().setName(COSName.TYPE, type);
    }

    /**
     * Creates a new instance with a given COS dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDTypedDictionaryWrapper(COSDictionary dictionary)
    {
        super(dictionary);
    }


    /**
     * Gets the type.
     * 
     * @return the type
     */
    public String getType()
    {
        return this.getCOSDictionary().getNameAsString(COSName.TYPE);
    }

    // There is no setType(String) method because changing the Type would most
    // probably also change the type of PD object.
}
