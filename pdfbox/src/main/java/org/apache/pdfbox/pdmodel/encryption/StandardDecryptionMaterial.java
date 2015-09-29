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
 *
 * Represents the necessary information to decrypt a document protected by
 * the standard security handler (password protection).
 *
 * This is only composed of a password.
 *
 * @author Benoit Guillon
 *
 */

public class StandardDecryptionMaterial extends DecryptionMaterial
{

    private String password = null;

    /**
     * Create a new standard decryption material with the given password.
     *
     * @param pwd The password.
     */
    public StandardDecryptionMaterial(String pwd)
    {
        password = pwd;
    }

    /**
     * Returns the password.
     *
     * @return The password used to decrypt the document.
     */
    public String getPassword()
    {
        return password;
    }

}
