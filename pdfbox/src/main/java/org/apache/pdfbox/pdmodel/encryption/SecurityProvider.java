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
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;

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
     * 
     * @throws IOException if the default provider can't be instantiated
     */
    public static Provider getProvider() throws IOException
    {
        // TODO synchronize access
        if (provider == null)
        {
            try
            {
                Class<Provider> providerClass = (Class<Provider>) Class
                        .forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                provider = providerClass.getDeclaredConstructor().newInstance();
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                   NoSuchMethodException | SecurityException | IllegalArgumentException | 
                   InvocationTargetException ex)
            {
                throw new IOException(ex);
            }
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
