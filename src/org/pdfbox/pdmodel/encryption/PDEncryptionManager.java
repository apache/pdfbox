/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
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