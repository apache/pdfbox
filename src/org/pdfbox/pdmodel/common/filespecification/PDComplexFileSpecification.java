/**
 * Copyright (c) 2004-2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.common.filespecification;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSStream;

/**
 * This represents a file specification.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDComplexFileSpecification extends PDFileSpecification
{
    private COSDictionary fs;

    /**
     * Default Constructor.
     */
    public PDComplexFileSpecification()
    {
        fs = new COSDictionary();
        fs.setName( "Type", "Filespec" );
    }

    /**
     * Constructor.
     *
     * @param dict The dictionary that fulfils this file specification.
     */
    public PDComplexFileSpecification( COSDictionary dict )
    {
        fs = dict;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return fs;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return fs;
    }

    /**
     * This will get the file name.
     *
     * @return The file name.
     */
    public String getFile()
    {
        return fs.getString( "F" );
    }

    /**
     * This will set the file name.
     *
     * @param file The name of the file.
     */
    public void setFile( String file )
    {
        fs.setString( "F", file );
    }
    
    /**
     * This will get the name representing a Dos file.
     *
     * @return The file name.
     */
    public String getFileDos()
    {
        return fs.getString( "DOS" );
    }

    /**
     * This will set name representing a dos file.
     *
     * @param file The name of the file.
     */
    public void setFileDos( String file )
    {
        fs.setString( "DOS", file );
    }
    
    /**
     * This will get the name representing a Mac file.
     *
     * @return The file name.
     */
    public String getFileMac()
    {
        return fs.getString( "Mac" );
    }

    /**
     * This will set name representing a Mac file.
     *
     * @param file The name of the file.
     */
    public void setFileMac( String file )
    {
        fs.setString( "Mac", file );
    }
    
    /**
     * This will get the name representing a Unix file.
     *
     * @return The file name.
     */
    public String getFileUnix()
    {
        return fs.getString( "Unix" );
    }

    /**
     * This will set name representing a Unix file.
     *
     * @param file The name of the file.
     */
    public void setFileUnix( String file )
    {
        fs.setString( "Unix", file );
    }
    
    /**
     * Tell if the underlying file is volatile and should not be cached by the
     * reader application.  Default: false
     * 
     * @param fileIsVolatile The new value for the volatility of the file.
     */
    public void setVolatile( boolean fileIsVolatile )
    {
        fs.setBoolean( "V", fileIsVolatile );
    }
    
    /**
     * Get if the file is volatile.  Default: false
     * 
     * @return True if the file is volatile attribute is set.
     */
    public boolean isVolatile()
    {
        return fs.getBoolean( "V", false );
    }
    
    /**
     * Get the embedded file.
     * 
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFile()
    {
        PDEmbeddedFile file = null;
        COSStream stream = (COSStream)fs.getObjectFromPath( "EF/F" );
        if( stream != null )
        {
            file = new PDEmbeddedFile( stream );
        }
        return file;
    }
    
    /**
     * Set the embedded file for this spec.
     * 
     * @param file The file to be embedded.
     */
    public void setEmbeddedFile( PDEmbeddedFile file )
    {
        COSDictionary ef = (COSDictionary)fs.getDictionaryObject( "EF" );
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( "EF", ef );
        }
        if( ef != null )
        {
            ef.setItem( "F", file );
        }
    }
    
    /**
     * Get the embedded dos file.
     * 
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileDos()
    {
        PDEmbeddedFile file = null;
        COSStream stream = (COSStream)fs.getObjectFromPath( "EF/DOS" );
        if( stream != null )
        {
            file = new PDEmbeddedFile( stream );
        }
        return file;
    }
    
    /**
     * Set the embedded dos file for this spec.
     * 
     * @param file The dos file to be embedded.
     */
    public void setEmbeddedFileDos( PDEmbeddedFile file )
    {
        COSDictionary ef = (COSDictionary)fs.getDictionaryObject( "DOS" );
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( "EF", ef );
        }
        if( ef != null )
        {
            ef.setItem( "DOS", file );
        }
    }
    
    /**
     * Get the embedded Mac file.
     * 
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileMac()
    {
        PDEmbeddedFile file = null;
        COSStream stream = (COSStream)fs.getObjectFromPath( "EF/Mac" );
        if( stream != null )
        {
            file = new PDEmbeddedFile( stream );
        }
        return file;
    }
    
    /**
     * Set the embedded Mac file for this spec.
     * 
     * @param file The Mac file to be embedded.
     */
    public void setEmbeddedFileMac( PDEmbeddedFile file )
    {
        COSDictionary ef = (COSDictionary)fs.getDictionaryObject( "Mac" );
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( "EF", ef );
        }
        if( ef != null )
        {
            ef.setItem( "Mac", file );
        }
    }
    
    /**
     * Get the embedded Unix file.
     * 
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileUnix()
    {
        PDEmbeddedFile file = null;
        COSStream stream = (COSStream)fs.getObjectFromPath( "EF/Unix" );
        if( stream != null )
        {
            file = new PDEmbeddedFile( stream );
        }
        return file;
    }
    
    /**
     * Set the embedded Unix file for this spec.
     * 
     * @param file The Unix file to be embedded.
     */
    public void setEmbeddedFileUnix( PDEmbeddedFile file )
    {
        COSDictionary ef = (COSDictionary)fs.getDictionaryObject( "Unix" );
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( "EF", ef );
        }
        if( ef != null )
        {
            ef.setItem( "Unix", file );
        }
    }
}