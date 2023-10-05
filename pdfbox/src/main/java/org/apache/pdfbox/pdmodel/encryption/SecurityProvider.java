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

import java.security.Provider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Singleton which provides a security provider.
 * 
 */
public class SecurityProvider
{
    private static Provider provider = null;

    private SecurityProvider()
    {
    }

    /**
     * Returns the provider to be used for advanced encrypting/decrypting. Default is the BouncyCastleProvider.
     * 
     * @return the security provider
     */
    public static Provider getProvider()
    {
        // TODO synchronize access
        if (provider == null)
        {
            provider = new BouncyCastleProvider();
        }
        return provider;
    }

    /**
     * Set the provider to be used for advanced encrypting/decrypting.
     * 
     * @param provider the security provider
     */
    public static void setProvider(Provider provider)
    {
        SecurityProvider.provider = provider;
    }
}
