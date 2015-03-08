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

import java.security.cert.X509Certificate;

/**
 * Represents a recipient in the public key protection policy.
 *
 * @see PublicKeyProtectionPolicy
 *
 * @author Benoit Guillon
 *
 */
public class PublicKeyRecipient
{
    private X509Certificate x509;

    private AccessPermission permission;

    /**
     * Returns the X509 certificate of the recipient.
     *
     * @return The X509 certificate
     */
    public X509Certificate getX509()
    {
        return x509;
    }

    /**
     * Set the X509 certificate of the recipient.
     *
     * @param aX509 The X509 certificate
     */
    public void setX509(X509Certificate aX509)
    {
        this.x509 = aX509;
    }

    /**
     * Returns the access permission granted to the recipient.
     *
     * @return The access permission object.
     */
    public AccessPermission getPermission()
    {
        return permission;
    }

    /**
     * Set the access permission granted to the recipient.
     *
     * @param permissions The permission to set.
     */
    public void setPermission(AccessPermission permissions)
    {
        this.permission = permissions;
    }
}
