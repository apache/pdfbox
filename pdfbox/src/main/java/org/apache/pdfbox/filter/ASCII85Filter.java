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

import org.apache.pdfbox.io.ASCII85InputStream;
import org.apache.pdfbox.io.ASCII85OutputStream;

import org.apache.pdfbox.cos.COSDictionary;

/**
 * This is the used for the ASCIIHexDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class ASCII85Filter implements Filter
{
    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        ASCII85InputStream is = null;
        try
        {
            is = new ASCII85InputStream(compressedData);
            byte[] buffer = new byte[1024];
            int amountRead = 0;
            while( (amountRead = is.read( buffer, 0, 1024) ) != -1 )
            {
                result.write(buffer, 0, amountRead);
            }
            result.flush();
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        ASCII85OutputStream os = new ASCII85OutputStream(result);
        byte[] buffer = new byte[1024];
        int amountRead = 0;
        while( (amountRead = rawData.read( buffer, 0, 1024 )) != -1 )
        {
            os.write( buffer, 0, amountRead );
        }
        os.close();
        result.flush();
    }
}
