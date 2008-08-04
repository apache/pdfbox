/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

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