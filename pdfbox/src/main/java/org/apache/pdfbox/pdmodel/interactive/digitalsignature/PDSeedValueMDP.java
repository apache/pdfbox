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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This MDP dictionary is a part of the seed value dictionary and define
 * if a author signature or a certification signature should be use.
 *
 * @author Thomas Chojecki
 */
public class PDSeedValueMDP
{
    private COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDSeedValueMDP()
    {
        dictionary = new COSDictionary();
        dictionary.setDirect(true);
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDSeedValueMDP(COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true);
    }

    /**
     * Convert this standard java object to a COS dictionary.
     *
     * @return The COS dictionary that matches this Java object.
     */
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Return the P value.
     * 
     * @return the P value
     */
    public int getP()
    {
        return dictionary.getInt(COSName.P);
    }

    /**
     * Set the P value.
     * 
     * @param p the value to be set as P
     */
    public void setP(int p)
    {
        if (p < 0 || p > 3)
        {
            throw new IllegalArgumentException("Only values between 0 and 3 nare allowed.");
        }
        dictionary.setInt(COSName.P, p);
    }
}
