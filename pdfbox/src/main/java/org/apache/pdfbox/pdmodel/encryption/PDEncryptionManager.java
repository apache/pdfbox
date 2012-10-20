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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will handle loading of the different security handlers.
 *
 * See PDF Reference 1.4 section "3.5 Encryption"
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 * @deprecated Made deprecated by the new security layer of PDFBox. Use SecurityHandlers instead.
 */

public class PDEncryptionManager
{
    private static Map handlerMap = Collections.synchronizedMap( new HashMap() );

    static
    {
        registerSecurityHandler( PDStandardEncryption.FILTER_NAME, PDStandardEncryption.class );
    }

    private PDEncryptionManager()
    {
    }

    /**
     * This will allow the user to register new security handlers when unencrypting a
     * document.
     *
     * @param filterName As described in the encryption dictionary.
     * @param handlerClass A subclass of PDEncryptionDictionary that has a constructor that takes
     *        a COSDictionary.
     */
    public static void registerSecurityHandler( String filterName, Class handlerClass )
    {
        handlerMap.put( COSName.getPDFName( filterName ), handlerClass );
    }

    /**
     * This will get the correct security handler for the encryption dictionary.
     *
     * @param dictionary The encryption dictionary.
     *
     * @return An implementation of PDEncryptionDictionary(PDStandardEncryption for most cases).
     *
     * @throws IOException If a security handler could not be found.
     */
    public static PDEncryptionDictionary getEncryptionDictionary( COSDictionary dictionary )
        throws IOException
    {
        Object retval = null;
        if( dictionary != null )
        {
            COSName filter = (COSName)dictionary.getDictionaryObject( COSName.FILTER );
            Class handlerClass = (Class)handlerMap.get( filter );
            if( handlerClass == null )
            {
                throw new IOException( "No handler for security handler '" + filter.getName() + "'" );
            }
            else
            {
                try
                {
                    Constructor ctor = handlerClass.getConstructor( new Class[] {
                        COSDictionary.class
                    } );
                    retval = ctor.newInstance( new Object[] {
                        dictionary
                    } );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IOException( e.getMessage() );
                }
                catch( InstantiationException e )
                {
                    throw new IOException( e.getMessage() );
                }
                catch( IllegalAccessException e )
                {
                    throw new IOException( e.getMessage() );
                }
                catch( InvocationTargetException e )
                {
                    throw new IOException( e.getMessage() );
                }
            }
        }
        return (PDEncryptionDictionary)retval;

    }
}
