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
 * The protection policy to add to a document for password-based protection.
 *
 * The following example shows how to protect a PDF document with password.
 * In this example, the document will be protected so that someone opening
 * the document with the user password <code>user_pwd</code> will not be
 * able to modify the document.
 *
 * <pre>
 * AccessPermission ap = new AccessPermission();
 * ap.setCanModify(false);
 * StandardProtectionPolicy policy = new StandardProtectionPolicy(owner_pwd, user_pwd, ap);
 * doc.protect(policy);
 * </pre>
 *
 * @author Benoit Guillon
 */
public final class StandardProtectionPolicy extends ProtectionPolicy
{
    private AccessPermission permissions;
    private String ownerPassword = "";
    private String userPassword = "";
    private boolean preferAES = false;

    /**
     * Creates an new instance of the standard protection policy
     * in order to protect a PDF document with passwords.
     *
     * @param ownerPassword The owner's password.
     * @param userPassword The users's password.
     * @param permissions The access permissions given to the user.
     */
    public StandardProtectionPolicy(String ownerPassword, String userPassword,
                                    AccessPermission permissions)
    {
        this.ownerPassword = ownerPassword;
        this.userPassword = userPassword;
        this.permissions = permissions;
    }

    /**
     * Returns the access permissions
     * @return the access permissions
     */
    public AccessPermission getPermissions()
    {
        return permissions;
    }

    /**
     * Sets the access permissions
     * @param permissions the new access permissions
     */
    public void setPermissions(AccessPermission permissions)
    {
        this.permissions = permissions;
    }

    /**
     * Returns the owner password.
     * @return the owner password
     */
    public String getOwnerPassword()
    {
        return ownerPassword;
    }

    /**
     * Sets the owner password
     * @param ownerPassword the new owner password
     */
    public void setOwnerPassword(String ownerPassword)
    {
        this.ownerPassword = ownerPassword;
    }

    /**
     * Returns the user password.
     * @return the user password
     */
    public String getUserPassword()
    {
        return userPassword;
    }

    /**
     * Sets the user password.
     * @param userPassword the new user password
     */
    public void setUserPassword(String userPassword)
    {
        this.userPassword = userPassword;
    }

    /**
     * Tell whether AES encryption is preferred when several encryption methods are available for
     * the chosen key length. The default is false. This setting is only relevant if the key length
     * is 128 bits.
     *
     * @return
     */
    public boolean isPreferAES()
    {
        return this.preferAES;
    }

    /**
     * Set whether AES encryption is preferred when several encryption methods are available for the
     * chosen key length. The default is false. This setting is only relevant if the key length is
     * 128 bits.
     *
     * @param preferAES
     */
    public void setPreferAES(boolean preferAES)
    {
        this.preferAES = preferAES;
    }
}
