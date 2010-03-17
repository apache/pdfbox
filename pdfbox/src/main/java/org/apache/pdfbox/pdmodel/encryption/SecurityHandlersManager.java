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

import java.lang.reflect.Constructor;
import java.security.Security;
import java.util.Hashtable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This class manages security handlers for the application. It follows the singleton pattern.
 * To be usable, security managers must be registered in it. Security managers are retrieved by
 * the application when necessary.
 *
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 *
 * @version $Revision: 1.3 $
 *
 */
public class SecurityHandlersManager
{

    /**
     * The unique instance of this manager.
     */
    private static SecurityHandlersManager instance;

    /**
     * hashtable used to index handlers regarding their name.
     * Basically this will be used when opening an encrypted
     * document to find the appropriate security handler to handle
     * security features of the document.
     */
    private Hashtable handlerNames = null;

    /**
     * Hashtable used to index handlers regarding the class of
     * protection policy they use.  Basically this will be used when
     * encrypting a document.
     */
    private Hashtable handlerPolicyClasses = null;

    /**
     * private constructor.
     */
    private SecurityHandlersManager()
    {
        handlerNames = new Hashtable();
        handlerPolicyClasses = new Hashtable();
        try
        {
            this.registerHandler(
                StandardSecurityHandler.FILTER,
                StandardSecurityHandler.class,
                StandardProtectionPolicy.class);
            this.registerHandler(
                PublicKeySecurityHandler.FILTER,
                PublicKeySecurityHandler.class,
                PublicKeyProtectionPolicy.class);
        }
        catch(Exception e)
        {
            System.err.println("SecurityHandlersManager strange error with builtin handlers: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * register a security handler.
     *
     * If the security handler was already registered an exception is thrown.
     * If another handler was previously registered for the same filter name or
     * for the same policy name, an exception is thrown
     *
     * @param filterName The name of the filter.
     * @param securityHandlerClass Security Handler class to register.
     * @param protectionPolicyClass Protection Policy class to register.
     *
     * @throws BadSecurityHandlerException If there is an error registering the security handler.
     */
    public void registerHandler(String filterName, Class securityHandlerClass, Class protectionPolicyClass)
        throws BadSecurityHandlerException
    {
        if(handlerNames.contains(securityHandlerClass) || handlerPolicyClasses.contains(securityHandlerClass))
        {
            throw new BadSecurityHandlerException("the following security handler was already registered: " +
                securityHandlerClass.getName());
        }

        if(SecurityHandler.class.isAssignableFrom(securityHandlerClass))
        {
            try
            {
                if(handlerNames.containsKey(filterName))
                {
                    throw new BadSecurityHandlerException("a security handler was already registered " +
                        "for the filter name " + filterName);
                }
                if(handlerPolicyClasses.containsKey(protectionPolicyClass))
                {
                    throw new BadSecurityHandlerException("a security handler was already registered " +
                        "for the policy class " + protectionPolicyClass.getName());
                }

                handlerNames.put(filterName, securityHandlerClass);
                handlerPolicyClasses.put(protectionPolicyClass, securityHandlerClass);
            }
            catch(Exception e)
            {
                throw new BadSecurityHandlerException(e);
            }
        }
        else
        {
            throw new BadSecurityHandlerException("The class is not a super class of SecurityHandler");
        }
    }


    /**
     * Get the singleton instance.
     *
     * @return The SecurityHandlersManager.
     */
    public static SecurityHandlersManager getInstance()
    {
        if(instance == null)
        {
            instance = new SecurityHandlersManager();
        }
        Security.addProvider(new BouncyCastleProvider());

        return instance;
    }

    /**
     * Get the security handler for the protection policy.
     *
     * @param policy The policy to get the security handler for.
     *
     * @return The appropriate security handler.
     *
     * @throws BadSecurityHandlerException If it is unable to create a SecurityHandler.
     */
    public SecurityHandler getSecurityHandler(ProtectionPolicy policy) throws BadSecurityHandlerException
    {

        Object found = handlerPolicyClasses.get(policy.getClass());
        if(found == null)
        {
            throw new BadSecurityHandlerException(
                "Cannot find an appropriate security handler for " + policy.getClass().getName());
        }
        Class handlerclass = (Class) found;
        Class[] argsClasses = {policy.getClass()};
        Object[] args = {policy};
        try
        {
            Constructor c = handlerclass.getDeclaredConstructor(argsClasses);
            SecurityHandler handler = (SecurityHandler)c.newInstance(args);
            return handler;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new BadSecurityHandlerException(
                "problem while trying to instanciate the security handler "+
                handlerclass.getName() + ": " + e.getMessage());
        }
    }



    /**
     * Retrieve the appropriate SecurityHandler for a the given filter name.
     * The filter name is an entry of the encryption dictionary of an encrypted document.
     *
     * @param filterName The filter name.
     *
     * @return The appropriate SecurityHandler if it exists.
     *
     * @throws BadSecurityHandlerException If the security handler does not exist.
     */
    public SecurityHandler getSecurityHandler(String filterName) throws BadSecurityHandlerException
    {
        Object found = handlerNames.get(filterName);
        if(found == null)
        {
            throw new BadSecurityHandlerException("Cannot find an appropriate security handler for " + filterName);
        }
        Class handlerclass = (Class) found;
        Class[] argsClasses = {};
        Object[] args = {};
        try
        {
            Constructor c = handlerclass.getDeclaredConstructor(argsClasses);
            SecurityHandler handler = (SecurityHandler)c.newInstance(args);
            return handler;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new BadSecurityHandlerException(
                "problem while trying to instanciate the security handler "+
                handlerclass.getName() + ": " + e.getMessage());
        }
    }
}
