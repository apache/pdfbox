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
 * Decrypts data encrypted by a security handler, reproducing the data as it was before encryption.
 * @author Adam Nichols
 */
final class CryptFilter extends Filter
{
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        COSName encryptionName = (COSName) parameters.getDictionaryObject(COSName.NAME);
        if(encryptionName == null || encryptionName.equals(COSName.IDENTITY)) 
        {
            // currently the only supported implementation is the Identity crypt filter
            Filter identityFilter = new IdentityFilter();
            identityFilter.decode(encoded, decoded, parameters, index);
            return new DecodeResult(parameters);
        }
        throw new IOException("Unsupported crypt filter " + encryptionName.getName());
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        COSName encryptionName = (COSName) parameters.getDictionaryObject(COSName.NAME);
        if(encryptionName == null || encryptionName.equals(COSName.IDENTITY))
        {
            // currently the only supported implementation is the Identity crypt filter
            Filter identityFilter = new IdentityFilter();
            identityFilter.encode(input, encoded, parameters);
        }
        else
        {
            throw new IOException("Unsupported crypt filter " + encryptionName.getName());
        }
    }
}
