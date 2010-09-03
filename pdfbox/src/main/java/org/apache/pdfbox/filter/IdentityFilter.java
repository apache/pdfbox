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

/**
 * The IdentityFilter filter just passes the data through without any modifications.
 * This is defined in section 7.6.5 of the PDF 1.7 spec and also stated in table
 * 26.
 * 
 * @author adam.nichols
 */
public class IdentityFilter implements Filter
{
    private static final int BUFFER_SIZE = 1024;
    
    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int amountRead = 0;
        while( (amountRead = compressedData.read( buffer, 0, BUFFER_SIZE )) != -1 )
        {
            result.write( buffer, 0, amountRead );
        }
        result.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int amountRead = 0;
        while( (amountRead = rawData.read( buffer, 0, BUFFER_SIZE )) != -1 )
        {
            result.write( buffer, 0, amountRead );
        }
        result.flush();
    }
}
