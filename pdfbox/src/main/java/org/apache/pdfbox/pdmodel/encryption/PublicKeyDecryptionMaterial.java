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

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * This class holds necessary information to decrypt a PDF document
 * protected by the public key security handler.
 *
 * To decrypt such a document, we need:
 * <ul>
 * <li>a valid X509 certificate which correspond to one of the recipient of the document</li>
 * <li>the private key corresponding to this certificate
 * <li>the password to decrypt the private key if necessary</li>
 * </ul>
 *
 * @author Benoit Guillon
 * 
 */

public class PublicKeyDecryptionMaterial extends DecryptionMaterial
{
    private final String password;
    private final KeyStore keyStore;
    private final String alias;

    /**
     * Create a new public key decryption material.
     *
     * @param keystore The keystore were the private key and the certificate are
     * @param a The alias of the private key and the certificate.
     *   If the keystore contains only 1 entry, this parameter can be left null.
     * @param pwd The password to extract the private key from the keystore.
     */

    public PublicKeyDecryptionMaterial(KeyStore keystore, String a, String pwd)
    {
        keyStore = keystore;
        alias = a;
        password = pwd;
    }


    /**
     * Returns the certificate contained in the keystore.
     *
     * @return The certificate that will be used to try to open the document.
     *
     * @throws KeyStoreException If there is an error accessing the certificate.
     */

    public X509Certificate getCertificate() throws KeyStoreException
    {
        if(keyStore.size() == 1)
        {
            Enumeration<String> aliases = keyStore.aliases();
            String keyStoreAlias = aliases.nextElement();
            return (X509Certificate)keyStore.getCertificate(keyStoreAlias);
        }
        else
        {
            if(keyStore.containsAlias(alias))
            {
                return (X509Certificate)keyStore.getCertificate(alias);
            }
            throw new KeyStoreException("the keystore does not contain the given alias");
        }
    }

    /**
     * Returns the password given by the user and that will be used
     * to open the private key.
     *
     * @return The password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * returns The private key that will be used to open the document protection.
     * @return The private key.
     * @throws KeyStoreException If there is an error accessing the key.
     */
    public Key getPrivateKey() throws KeyStoreException
    {
        try
        {
            if(keyStore.size() == 1)
            {
                Enumeration<String> aliases = keyStore.aliases();
                String keyStoreAlias = aliases.nextElement();
                return keyStore.getKey(keyStoreAlias, password.toCharArray());
            }
            else
            {
                if(keyStore.containsAlias(alias))
                {
                    return keyStore.getKey(alias, password.toCharArray());
                }
                throw new KeyStoreException("the keystore does not contain the given alias");
            }
        }
        catch(UnrecoverableKeyException ex)
        {
            throw new KeyStoreException("the private key is not recoverable", ex);
        }
        catch(NoSuchAlgorithmException ex)
        {
            throw new KeyStoreException("the algorithm necessary to recover the key is not available", ex);
        }
    }
}
