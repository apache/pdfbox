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
import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Manages security handlers for the application.
 * It follows the singleton pattern.
 * To be usable, security managers must be registered in it.
 * Security managers are retrieved by the application when necessary.
 *
 * @author Benoit Guillon
 * @author John Hewson
 */
public final class SecurityHandlerFactory
{
    /** Singleton instance */
    public static SecurityHandlerFactory INSTANCE = new SecurityHandlerFactory();

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Map<String, Class<? extends SecurityHandler>> nameToHandler =
            new HashMap<String, Class<? extends SecurityHandler>>();

    private final Map<Class<? extends ProtectionPolicy>,
                      Class<? extends SecurityHandler>> policyToHandler =
            new HashMap<Class<? extends ProtectionPolicy>,
                        Class<? extends SecurityHandler>>();

    private SecurityHandlerFactory()
    {
        registerHandler(StandardSecurityHandler.FILTER,
                        StandardSecurityHandler.class,
                        StandardProtectionPolicy.class);

        registerHandler(PublicKeySecurityHandler.FILTER,
                        PublicKeySecurityHandler.class,
                        PublicKeyProtectionPolicy.class);
    }

    /**
     * Registers a security handler.
     *
     * If the security handler was already registered an exception is thrown.
     * If another handler was previously registered for the same filter name or
     * for the same policy name, an exception is thrown
     *
     * @param name the name of the filter
     * @param securityHandler security handler class to register
     * @param protectionPolicy protection policy class to register
     */
    public void registerHandler(String name,
                                Class<? extends SecurityHandler> securityHandler,
                                Class<? extends ProtectionPolicy> protectionPolicy)
    {
        if (nameToHandler.containsKey(name))
        {
            throw new IllegalStateException("The security handler name is already registered");
        }

        nameToHandler.put(name, securityHandler);
        policyToHandler.put(protectionPolicy, securityHandler);
    }

    /**
     * Returns a new security handler for the given protection policy, or null none is available.
     * @param policy the protection policy for which to create a security handler
     * @return a new SecurityHandler instance, or null none is available
     */
    public SecurityHandler newSecurityHandlerForPolicy(ProtectionPolicy policy)
    {
        Class<? extends SecurityHandler> handlerClass = policyToHandler.get(policy.getClass());
        if (handlerClass == null)
        {
            return null;
        }

        Class[] argsClasses = { policy.getClass() };
        Object[] args = { policy };
        try
        {
            Constructor<? extends SecurityHandler> ctor =
                    handlerClass.getDeclaredConstructor(argsClasses);
            return ctor.newInstance(args);
        }
        catch(NoSuchMethodException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(IllegalAccessException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(InstantiationException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(InvocationTargetException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a new security handler for the given Filter name, or null none is available.
     * @param name the Filter name from the PDF encryption dictionary
     * @return a new SecurityHandler instance, or null none is available
     */
    public SecurityHandler newSecurityHandler(String name)
    {
        Class<? extends SecurityHandler> handlerClass = nameToHandler.get(name);
        if (handlerClass == null)
        {
            return null;
        }

        Class[] argsClasses = { };
        Object[] args = { };
        try
        {
            Constructor<? extends SecurityHandler> ctor =
                    handlerClass.getDeclaredConstructor(argsClasses);
            return ctor.newInstance(args);
        }
        catch(NoSuchMethodException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(IllegalAccessException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(InstantiationException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
        catch(InvocationTargetException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
    }
}
