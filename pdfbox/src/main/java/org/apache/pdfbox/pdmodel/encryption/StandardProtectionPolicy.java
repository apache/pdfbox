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
 * This class represents the protection policy to add to a document
 * for password-based protection.
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
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 * @version $Revision: 1.3 $
 */
public class StandardProtectionPolicy extends ProtectionPolicy
{

    private AccessPermission permissions;

    private String ownerPassword = "";

    private String userPassword = "";


    /**
     * Creates an new instance of the standard protection policy
     * in order to protect a PDF document with passwords.
     *
     * @param ownerPass The owner's password.
     * @param userPass The users's password.
     * @param perms The access permissions given to the user.
     */
    public StandardProtectionPolicy(String ownerPass, String userPass, AccessPermission perms)
    {
        this.permissions = perms;
        this.userPassword = userPass;
        this.ownerPassword = ownerPass;
    }

    /**
     * Getter of the property <tt>permissions</tt>.
     *
     * @return Returns the permissions.
     */
    public AccessPermission getPermissions()
    {
        return permissions;
    }

    /**
     * Setter of the property <tt>permissions</tt>.
     *
     * @param perms The permissions to set.
     */
    public void setPermissions(AccessPermission perms)
    {
        this.permissions = perms;
    }

    /**
     * Getter of the property <tt>ownerPassword</tt>.
     *
     * @return Returns the ownerPassword.
     */
    public String getOwnerPassword()
    {
        return ownerPassword;
    }

    /**
     * Setter of the property <tt>ownerPassword</tt>.
     *
     * @param ownerPass The ownerPassword to set.
     */
    public void setOwnerPassword(String ownerPass)
    {
        this.ownerPassword = ownerPass;
    }

    /**
     * Getter of the property <tt>userPassword</tt>.
     *
     * @return Returns the userPassword.
     */
    public String getUserPassword()
    {
        return userPassword;
    }

    /**
     * Setter of the property <tt>userPassword</tt>.
     *
     * @param userPass The userPassword to set.
     */
    public void setUserPassword(String userPass)
    {
        this.userPassword = userPass;
    }
}
