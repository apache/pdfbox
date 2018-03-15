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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This is the used for the FlateDecode filter.
 *
 * @author Ben Litchfield
 * @author Marcel Kammer
 */
public class FlateFilter implements Filter
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(FlateFilter.class);

    private static final int BUFFER_SIZE = 0x4000;

    /**
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex )
    throws IOException
    {
        COSBase baseObj = options.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        COSDictionary dict = null;
        if (baseObj instanceof COSDictionary)
        {
            dict = (COSDictionary) baseObj;
        }
        else if (baseObj instanceof COSArray)
        {
            COSArray paramArray = (COSArray) baseObj;
            if (filterIndex < paramArray.size())
            {
                dict = (COSDictionary) paramArray.getObject(filterIndex);
            }
        }
        else if (baseObj != null)
        {
            throw new IOException("Error: Expected COSArray or COSDictionary and not "
                    + baseObj.getClass().getName());
        }

        int predictor = -1;
        if (dict != null)
        {
            predictor = dict.getInt(COSName.PREDICTOR);
        }
        try
        {
            // Decode data using given predictor
            if (predictor > 1)
            {
                int colors = Math.min(dict.getInt(COSName.COLORS, 1), 32);
                int bitsPerPixel = dict.getInt(COSName.BITS_PER_COMPONENT, 8);
                int columns = dict.getInt(COSName.COLUMNS, 1);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decompress(compressedData, baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Predictor.decodePredictor(predictor, colors, bitsPerPixel, columns, bais, result);
                result.flush();
                baos.reset();
                bais.reset();
            }
            else
            {
                decompress(compressedData, result);
            }
        }
        catch (DataFormatException exception)
        {
            // if the stream is corrupt a DataFormatException may occur
            LOG.error("FlateFilter: stop reading corrupt stream due to a DataFormatException");
            // re-throw the exception, caller has to handle it
            IOException io = new IOException();
            io.initCause(exception);
            throw io;
        }
    }

    // Use Inflater instead of InflateInputStream to avoid an EOFException due to a probably 
    // missing Z_STREAM_END, see PDFBOX-1232 for details
    private void decompress(InputStream in, OutputStream out) throws IOException, DataFormatException 
    { 
        byte[] buf = new byte[2048]; 
        // skip zlib header
        in.read(buf,0,2);
        int read = in.read(buf); 
        if(read > 0) 
        { 
            // use nowrap mode to bypass zlib-header and checksum to avoid a DataFormatException
            Inflater inflater = new Inflater(true); 
            inflater.setInput(buf,0,read); 
            byte[] res = new byte[1024];
            boolean dataWritten = false;
            while(true) 
            { 
                int resRead = 0;
                try
                {
                    resRead = inflater.inflate(res);
                }
                catch(DataFormatException exception)
                {
                    if (dataWritten)
                    {
                        // some data could be read -> don't throw an exception
                        LOG.warn("FlateFilter: premature end of stream due to a DataFormatException");
                        break;
                    }
                    else
                    {
                        // nothing could be read -> re-throw exception
                        throw exception;
                    }
                }
                if(resRead != 0) 
                { 
                    out.write(res,0,resRead); 
                    dataWritten = true;
                    continue; 
                } 
                if(inflater.finished() || inflater.needsDictionary() || in.available() == 0) 
                {
                    break;
                } 
                read = in.read(buf); 
                inflater.setInput(buf,0,read); 
            }
            inflater.end();
        }
        out.close();
    } 
    
    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options, int filterIndex )
    throws IOException
    {
        DeflaterOutputStream out = new DeflaterOutputStream(result);
        int amountRead = 0;
        int mayRead = rawData.available();
        if (mayRead > 0)
        {
            byte[] buffer = new byte[Math.min(mayRead,BUFFER_SIZE)];
            while ((amountRead = rawData.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
            {
                out.write(buffer, 0, amountRead);
            }
        }
        out.close();
        result.flush();
    }
}
