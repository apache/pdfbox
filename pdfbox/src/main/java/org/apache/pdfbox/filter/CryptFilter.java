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
package org.apache.pdfbox.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 *
 * @author adam.nichols
 */
public class CryptFilter implements Filter
{
    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        COSName encryptionName = (COSName)options.getDictionaryObject(COSName.NAME);
        if(encryptionName == null || encryptionName.equals(COSName.IDENTITY)) 
        {
            // currently the only supported implementation is the Identity crypt filter
            Filter identityFilter = new IdentityFilter();
            identityFilter.decode(compressedData, result, options, filterIndex);
        }
        else 
        {
            throw new IOException("Unsupported crypt filter "+encryptionName.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        COSName encryptionName = (COSName)options.getDictionaryObject(COSName.NAME);
        if(encryptionName == null || encryptionName.equals(COSName.IDENTITY))
        {
            // currently the only supported implementation is the Identity crypt filter
            Filter identityFilter = new IdentityFilter();
            identityFilter.encode(rawData, result, options, filterIndex);
        }
        else
        {
            throw new IOException("Unsupported crypt filter "+encryptionName.getName());
        }
    }
}
