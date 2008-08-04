/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.encryption;

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