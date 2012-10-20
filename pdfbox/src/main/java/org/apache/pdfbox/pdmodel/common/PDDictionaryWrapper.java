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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

/**
 * A wrapper for a COS dictionary.
 *
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 *
 */
public class PDDictionaryWrapper implements COSObjectable
{

    private final COSDictionary dictionary;

    /**
     * Default constructor
     */
    public PDDictionaryWrapper()
    {
        this.dictionary = new COSDictionary();
    }

    /**
     * Creates a new instance with a given COS dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDDictionaryWrapper(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }


    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return this.dictionary;
    }

    /**
     * Gets the COS dictionary.
     * 
     * @return the COS dictionary
     */
    protected COSDictionary getCOSDictionary()
    {
        return this.dictionary;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof PDDictionaryWrapper)
        {
            return this.dictionary.equals(((PDDictionaryWrapper) obj).dictionary);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.dictionary.hashCode();
    }

}
