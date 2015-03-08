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

/**
 * This class represents the protection policy to apply to a document.
 *
 * Objects implementing this abstract class can be passed to the protect method of PDDocument
 * to protect a document.
 *
 * @see org.apache.pdfbox.pdmodel.PDDocument#protect(ProtectionPolicy)
 *
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 */
public abstract class ProtectionPolicy
{

    private static final int DEFAULT_KEY_LENGTH = 40;

    private int encryptionKeyLength = DEFAULT_KEY_LENGTH;

    /**
     * set the length in (bits) of the secret key that will be
     * used to encrypt document data.
     * The default value is 40 bits, which provides a low security level
     * but is compatible with old versions of Acrobat Reader.
     *
     * @param l the length in bits (must be 40, 128 or 256)
     */
    public void setEncryptionKeyLength(int l)
    {
        if(l!=40 && l!=128 && l!=256)
        {
            throw new IllegalArgumentException("Invalid key length '" + l + "' value must be 40, 128 or 256!");
        }
        encryptionKeyLength = l;
    }

    /**
     * Get the length of the secrete key that will be used to encrypt
     * document data.
     *
     * @return The length (in bits) of the encryption key.
     */
    public int getEncryptionKeyLength()
    {
        return encryptionKeyLength;
    }
}
