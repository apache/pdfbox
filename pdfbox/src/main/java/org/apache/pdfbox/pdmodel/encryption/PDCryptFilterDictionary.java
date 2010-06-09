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

package org.apache.pdfbox.pdmodel.encryption;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This class is a specialized view of the crypt filter dictionary of a PDF document.
 * It contains a low level dictionary (COSDictionary) and provides the methods to
 * manage its fields.
 *
 *
 * @version $Revision: 1.0 $
 */
public class PDCryptFilterDictionary
{

    /**
     * COS crypt filter dictionary.
     */
    protected COSDictionary cryptFilterDictionary = null;

    /**
     * creates a new empty crypt filter dictionary.
     */
    public PDCryptFilterDictionary()
    {
        cryptFilterDictionary = new COSDictionary();
    }

    /**
     * creates a new crypt filter dictionary from the low level dictionary provided.
     * @param d the low level dictionary that will be managed by the newly created object
     */
    public PDCryptFilterDictionary(COSDictionary d)
    {
        cryptFilterDictionary = d;
    }

    /**
     * This will get the dictionary associated with this crypt filter dictionary.
     *
     * @return The COS dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return cryptFilterDictionary;
    }

    /**
     * This will set the number of bits to use for the crypt filter algorithm.
     *
     * @param length The new key length.
     */
    public void setLength(int length)
    {
        cryptFilterDictionary.setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the Length entry of the crypt filter dictionary.<br /><br />
     * The length in <b>bits</b> for the crypt filter algorithm. This will return a multiple of 8.
     *
     * @return The length in bits for the encryption algorithm
     */
    public int getLength()
    {
        return cryptFilterDictionary.getInt( COSName.LENGTH, 40 );
    }

     /**
     * This will set the crypt filter method. 
     * Allowed values are: NONE, V2, AESV2
     *
     * @param cfm name of the crypt filter method.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setCryptFilterMethod(COSName cfm) throws IOException
    {
        cryptFilterDictionary.setItem( COSName.CFM, cfm );
    }

    /**
     * This will return the crypt filter method. 
     * Allowed values are: NONE, V2, AESV2
     *
     * @return the name of the crypt filter method.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public COSName getCryptFilterMethod() throws IOException
    {
        return (COSName)cryptFilterDictionary.getDictionaryObject( COSName.CFM );
    }

}
