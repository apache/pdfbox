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
package org.apache.pdfbox.pdmodel.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * A PDStream represents a stream in a PDF document.  Streams are tied to a single
 * PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDMemoryStream extends PDStream
{
    private byte[] data;

    /**
     * This will create a new PDStream object.
     *
     * @param buffer The data for this in memory stream.
     */
    public PDMemoryStream( byte[] buffer )
    {
        data = buffer;
    }



    /**
     * If there are not compression filters on the current stream then this
     * will add a compression filter, flate compression for example.
     */
    public void addCompression()
    {
        //no compression to add
    }



    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        throw new UnsupportedOperationException( "not supported for memory stream" );
    }

    /**
     * This will get a stream that can be written to.
     *
     * @return An output stream to write data to.
     *
     * @throws IOException If an IO error occurs during writing.
     */
    public OutputStream createOutputStream() throws IOException
    {
        throw new UnsupportedOperationException( "not supported for memory stream" );
    }

    /**
     * This will get a stream that can be read from.
     *
     * @return An input stream that can be read from.
     *
     * @throws IOException If an IO error occurs during reading.
     */
    public InputStream createInputStream() throws IOException
    {
        return new ByteArrayInputStream( data );
    }

    /**
     * This will get a stream with some filters applied but not others.  This is useful
     * when doing images, ie filters = [flate,dct], we want to remove flate but leave dct
     *
     * @param stopFilters A list of filters to stop decoding at.
     * @return A stream with decoded data.
     * @throws IOException If there is an error processing the stream.
     */
    public InputStream getPartiallyFilteredStream( List stopFilters ) throws IOException
    {
        return createInputStream();
    }

    /**
     * Get the cos stream associated with this object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSStream getStream()
    {
        throw new UnsupportedOperationException( "not supported for memory stream" );
    }

    /**
     * This will get the length of the filtered/compressed stream.  This is readonly in the
     * PD Model and will be managed by this class.
     *
     * @return The length of the filtered stream.
     */
    public int getLength()
    {
        return data.length;
    }

    /**
     * This will get the list of filters that are associated with this stream.  Or
     * null if there are none.
     * @return A list of all encoding filters to apply to this stream.
     */
    public List getFilters()
    {
        return null;
    }

    /**
     * This will set the filters that are part of this stream.
     *
     * @param filters The filters that are part of this stream.
     */
    public void setFilters( List filters )
    {
        throw new UnsupportedOperationException( "not supported for memory stream" );
    }

    /**
     * Get the list of decode parameters.  Each entry in the list will refer to
     * an entry in the filters list.
     *
     * @return The list of decode parameters.
     *
     * @throws IOException if there is an error retrieving the parameters.
     */
    public List getDecodeParams() throws IOException
    {
        return null;
    }

    /**
     * This will set the list of decode params.
     *
     * @param decodeParams The list of decode params.
     */
    public void setDecodeParams( List decodeParams )
    {
        //do nothing
    }

    /**
     * This will get the file specification for this stream.  This is only
     * required for external files.
     *
     * @return The file specification.
     */
    public PDFileSpecification getFile()
    {
        return null;
    }

    /**
     * Set the file specification.
     * @param f The file specification.
     */
    public void setFile( PDFileSpecification f )
    {
        //do nothing.
    }

    /**
     * This will get the list of filters that are associated with this stream.  Or
     * null if there are none.
     * @return A list of all encoding filters to apply to this stream.
     */
    public List getFileFilters()
    {
        return null;
    }

    /**
     * This will set the filters that are part of this stream.
     *
     * @param filters The filters that are part of this stream.
     */
    public void setFileFilters( List filters )
    {
        //do nothing.
    }

    /**
     * Get the list of decode parameters.  Each entry in the list will refer to
     * an entry in the filters list.
     *
     * @return The list of decode parameters.
     *
     * @throws IOException if there is an error retrieving the parameters.
     */
    public List getFileDecodeParams() throws IOException
    {
        return null;
    }

    /**
     * This will set the list of decode params.
     *
     * @param decodeParams The list of decode params.
     */
    public void setFileDecodeParams( List decodeParams )
    {
        //do nothing
    }

    /**
     * This will copy the stream into a byte array.
     *
     * @return The byte array of the filteredStream
     * @throws IOException When getFilteredStream did not work
     */
    public byte[] getByteArray() throws IOException
    {
        return data;
    }

    /**
     * Get the metadata that is part of the document catalog.  This will
     * return null if there is no meta data for this object.
     *
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        return null;
    }

    /**
     * Set the metadata for this object.  This can be null.
     *
     * @param meta The meta data for this object.
     */
    public void setMetadata( PDMetadata meta )
    {
        //do nothing
    }
}
