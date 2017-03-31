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
import java.util.HashMap;
import java.util.Map;

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
    public static final SecurityHandlerFactory INSTANCE = new SecurityHandlerFactory();

    private final Map<String, Class<? extends SecurityHandler>> nameToHandler = new HashMap<>();

    private final Map<Class<? extends ProtectionPolicy>,
                      Class<? extends SecurityHandler>> policyToHandler = new HashMap<>();

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
     * @return a new SecurityHandler instance, or null if none is available
     */
    public SecurityHandler newSecurityHandlerForPolicy(ProtectionPolicy policy)
    {
        Class<? extends SecurityHandler> handlerClass = policyToHandler.get(policy.getClass());
        if (handlerClass == null)
        {
            return null;
        }

        Class<?>[] argsClasses = { policy.getClass() };
        Object[] args = { policy };
        return newSecurityHandler(handlerClass, argsClasses, args);
    }

    /**
     * Returns a new security handler for the given Filter name, or null none is available.
     * @param name the Filter name from the PDF encryption dictionary
     * @return a new SecurityHandler instance, or null if none is available
     */
    public SecurityHandler newSecurityHandlerForFilter(String name)
    {
        Class<? extends SecurityHandler> handlerClass = nameToHandler.get(name);
        if (handlerClass == null)
        {
            return null;
        }

        Class<?>[] argsClasses = { };
        Object[] args = { };
        return newSecurityHandler(handlerClass, argsClasses, args);
    }

    /* Returns a new security handler for the given parameters, or null none is available.
     *
     * @param handlerClass the handler class.
     * @param argsClasses the parameter array.
     * @param args array of objects to be passed as arguments to the constructor call.
     * @return a new SecurityHandler instance, or null if none is available.
     */
    private SecurityHandler newSecurityHandler(Class<? extends SecurityHandler> handlerClass, 
            Class<?>[] argsClasses, Object[] args)
    {
        try
        {
            Constructor<? extends SecurityHandler> ctor =
                    handlerClass.getDeclaredConstructor(argsClasses);
            return ctor.newInstance(args);
        }
        catch(NoSuchMethodException | IllegalAccessException | InstantiationException |
                InvocationTargetException e)
        {
            // should not happen in normal operation
            throw new RuntimeException(e);
        }
    }
}
