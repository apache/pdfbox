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
package org.apache.pdfbox.pdmodel.common.filespecification;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

/**
 * This represents a file specification.
 *
 * @author Ben Litchfield
 * 
 */
public class PDComplexFileSpecification extends PDFileSpecification
{
    private final COSDictionary fs;
    private COSDictionary efDictionary;

    /**
     * Default Constructor.
     */
    public PDComplexFileSpecification()
    {
        fs = new COSDictionary();
        fs.setItem( COSName.TYPE, COSName.FILESPEC );
    }

    /**
     * Constructor. Creates empty COSDictionary if dict is null.
     *
     * @param dict The dictionary that fulfils this file specification.
     */
    public PDComplexFileSpecification(final COSDictionary dict )
    {
        if (dict == null)
        {
            fs = new COSDictionary();
            fs.setItem( COSName.TYPE, COSName.FILESPEC );
        }
        else
        {
            fs = dict;
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return fs;
    }
    
    private COSDictionary getEFDictionary()
    {
        if (efDictionary == null && fs != null)
        {
            efDictionary = fs.getCOSDictionary(COSName.EF);           
        }
        return efDictionary;
    }

    private COSBase getObjectFromEFDictionary(final COSName key)
    {
        final COSDictionary ef = getEFDictionary();
        if (ef != null)
        {
            return ef.getDictionaryObject(key);
        }
        return null;
    }
    
    /**
     * <p>Preferred method for getting the filename.
     * It will determinate the recommended file name.</p>
     * <p>First of all we try to get the unicode filename if it exist.
     * If it doesn't exist we take a look at the DOS, MAC UNIX filenames.
     * If no one exist the required F entry will be returned.</p>
     *
     * @return The preferred file name.
     */
    public String getFilename()
    {
        String filename = getFileUnicode();
        if (filename == null)
        {
            filename = getFileDos();
        }
        if (filename == null)
        {
            filename = getFileMac();
        }
        if (filename == null)
        {
            filename = getFileUnix();
        }
        if (filename == null)
        {
            filename = getFile();
        }
        return filename;
    }

    /**
     * This will get the unicode file name.
     *
     * @return The file name.
     */
    public String getFileUnicode()
    {
        return fs.getString(COSName.UF);
    }

    /**
     * This will set the unicode file name. If you call this, then do not forget to also call
     * {@link #setFile(java.lang.String) setFile(String)} or the attachment will not be visible on
     * some viewers.
     *
     * @param file The name of the file.
     */
    public void setFileUnicode(final String file )
    {
        fs.setString( COSName.UF, file );
    }

    /**
     * This will get the file name.
     *
     * @return The file name.
     */
    @Override
    public String getFile()
    {
        return fs.getString( COSName.F );
    }

    /**
     * This will set the file name. You should also call
     * {@link #setFileUnicode(java.lang.String) setFileUnicode(String)} for cross-platform and
     * cross-language compatibility.
     *
     * @param file The name of the file.
     */
    @Override
    public void setFile(final String file )
    {
        fs.setString( COSName.F, file );
    }

    /**
     * This will get the name representing a Dos file.
     *
     * @return The file name.
     */
    public String getFileDos()
    {
        return fs.getString( COSName.DOS );
    }

    /**
     * This will get the name representing a Mac file.
     *
     * @return The file name.
     */
    public String getFileMac()
    {
        return fs.getString( COSName.MAC );
    }

    /**
     * This will get the name representing a Unix file.
     *
     * @return The file name.
     */
    public String getFileUnix()
    {
        return fs.getString( COSName.UNIX );
    }

    /**
     * Tell if the underlying file is volatile and should not be cached by the
     * reader application.  Default: false
     *
     * @param fileIsVolatile The new value for the volatility of the file.
     */
    public void setVolatile(final boolean fileIsVolatile )
    {
        fs.setBoolean( COSName.V, fileIsVolatile );
    }

    /**
     * Get if the file is volatile.  Default: false
     *
     * @return True if the file is volatile attribute is set.
     */
    public boolean isVolatile()
    {
        return fs.getBoolean( COSName.V, false );
    }

    /**
     * Get the embedded file.
     *
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFile()
    {        
        final COSBase base = getObjectFromEFDictionary(COSName.F);
        if (base instanceof COSStream)
        {
            return new PDEmbeddedFile((COSStream) base);
        }
        return null;
    }

    /**
     * Set the embedded file for this spec. You should also call
     * {@link #setEmbeddedFileUnicode(org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile) setEmbeddedFileUnicode(PDEmbeddedFile)}
     * for cross-platform and cross-language compatibility.
     *
     * @param file The file to be embedded.
     */
    public void setEmbeddedFile(final PDEmbeddedFile file )
    {
        COSDictionary ef = getEFDictionary();
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( COSName.EF, ef );
        }
        if( ef != null )
        {
            ef.setItem( COSName.F, file );
        }
    }

    /**
     * Get the embedded dos file.
     *
     * @return The embedded dos file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileDos()
    {        
        final COSBase base = getObjectFromEFDictionary( COSName.DOS );
        if (base instanceof COSStream)
        {
            return new PDEmbeddedFile((COSStream) base);
        }
        return null;
    }

    /**
     * Get the embedded Mac file.
     *
     * @return The embedded Mac file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileMac()
    {        
        final COSBase base = getObjectFromEFDictionary( COSName.MAC );
        if (base instanceof COSStream)
        {
            return new PDEmbeddedFile((COSStream) base);
        }
        return null;
    }

    /**
     * Get the embedded Unix file.
     *
     * @return The embedded file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileUnix()
    {        
        final COSBase base = getObjectFromEFDictionary( COSName.UNIX );
        if (base instanceof COSStream)
        {
            return new PDEmbeddedFile((COSStream) base);
        }
        return null;
    }

    /**
     * Get the embedded unicode file.
     *
     * @return The embedded unicode file for this file spec.
     */
    public PDEmbeddedFile getEmbeddedFileUnicode()
    {        
        final COSBase base = getObjectFromEFDictionary( COSName.UF );
        if (base instanceof COSStream)
        {
            return new PDEmbeddedFile((COSStream) base);
        }
        return null;
    }

    /**
     * Set the embedded Unicode file for this spec. If you call this, then do not forget to also
     * call
     * {@link #setEmbeddedFile(org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile) setEmbeddedFile(PDEmbeddedFile)}
     * or the attachment will not be visible on some viewers.
     *
     * @param file The Unicode file to be embedded.
     */
    public void setEmbeddedFileUnicode(final PDEmbeddedFile file )
    {
        COSDictionary ef = getEFDictionary();
        if( ef == null && file != null )
        {
            ef = new COSDictionary();
            fs.setItem( COSName.EF, ef );
        }
        if( ef != null )
        {
            ef.setItem( COSName.UF, file );
        }
    }
    
    /**
     * Set the file description.
     * 
     * @param description The file description
     */
    public void setFileDescription(final String description )
    {
        fs.setString( COSName.DESC, description );
    }

    /**
     * This will get the description.
     *
     * @return The file description.
     */
    public String getFileDescription()
    {
        return fs.getString( COSName.DESC );
    }

}

