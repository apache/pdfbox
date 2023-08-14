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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This represents an embedded file in a file specification.
 *
 * @author Ben Litchfield
 */
public class PDEmbeddedFile extends PDStream
{

    /**
     * {@inheritDoc}
     */
    public PDEmbeddedFile( PDDocument document )
    {
        super( document );
        getCOSObject().setItem(COSName.TYPE, COSName.EMBEDDED_FILE);
    }

    /**
     * {@inheritDoc}
     */
    public PDEmbeddedFile( COSStream str )
    {
        super(str);
    }

    /**
     * {@inheritDoc}
     */
    public PDEmbeddedFile( PDDocument doc, InputStream str  ) throws IOException
    {
        super(doc, str);
        getCOSObject().setItem(COSName.TYPE, COSName.EMBEDDED_FILE);
    }

    /**
     * {@inheritDoc}
     * @param filter Filter to apply to the stream.
     *
     * @throws IOException {@inheritDoc}
     */
    public PDEmbeddedFile(PDDocument doc, InputStream input, COSName filter) throws IOException
    {
        super(doc, input, filter);
        getCOSObject().setItem(COSName.TYPE, COSName.EMBEDDED_FILE);
    }

    /**
     * Set the subtype for this embedded file.  This should be a mime type value.  Optional.
     *
     * @param mimeType The mimeType for the file.
     */
    public void setSubtype( String mimeType )
    {
        getCOSObject().setName(COSName.SUBTYPE, mimeType);
    }

    /**
     * Get the subtype(mimetype) for the embedded file.
     *
     * @return The type of embedded file.
     */
    public String getSubtype()
    {
        return getCOSObject().getNameAsString(COSName.SUBTYPE );
    }

    /**
     * Get the size of the embedded file.
     *
     * @return The size of the embedded file.
     */
    public int getSize()
    {
        return getCOSObject().getEmbeddedInt(COSName.PARAMS, COSName.SIZE);
    }

    /**
     * Set the size of the embedded file.
     *
     * @param size The size of the embedded file.
     */
    public void setSize( int size )
    {
        getCOSObject().setEmbeddedInt(COSName.PARAMS, COSName.SIZE, size);
    }

    /**
     * Get the creation date of the embedded file.
     *
     * @return The Creation date.
     * @throws IOException If there is an error while constructing the date.
     */
    public Calendar getCreationDate() throws IOException
    {
        return getCOSObject().getEmbeddedDate(COSName.PARAMS, COSName.CREATION_DATE);
    }

    /**
     * Set the creation date.
     *
     * @param creation The new creation date.
     */
    public void setCreationDate( Calendar creation )
    {
        getCOSObject().setEmbeddedDate(COSName.PARAMS, COSName.CREATION_DATE, creation);
    }

    /**
     * Get the mod date of the embedded file.
     *
     * @return The mod date.
     * @throws IOException If there is an error while constructing the date.
     */
    public Calendar getModDate() throws IOException
    {
        return getCOSObject().getEmbeddedDate(COSName.PARAMS, COSName.MOD_DATE);
    }

    /**
     * Set the mod date.
     *
     * @param mod The new creation mod.
     */
    public void setModDate( Calendar mod )
    {
        getCOSObject().setEmbeddedDate(COSName.PARAMS, COSName.MOD_DATE, mod);
    }

    /**
     * Get the check sum of the embedded file.
     *
     * @return The check sum of the file.
     */
    public String getCheckSum()
    {
        return getCOSObject().getEmbeddedString(COSName.PARAMS, COSName.CHECK_SUM);
    }

    /**
     * Set the check sum.
     *
     * @param checksum The checksum of the file.
     */
    public void setCheckSum( String checksum )
    {
        getCOSObject().setEmbeddedString(COSName.PARAMS, COSName.CHECK_SUM, checksum);
    }

    /**
     * Get the mac subtype.
     *
     * @return The mac subtype.
     */
    public String getMacSubtype()
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        return params != null ? params.getEmbeddedString(COSName.MAC, COSName.SUBTYPE) : null;
    }

    /**
     * Set the mac subtype.
     *
     * @param macSubtype The mac subtype.
     */
    public void setMacSubtype( String macSubtype )
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        if( params == null && macSubtype != null )
        {
            params = new COSDictionary();
            getCOSObject().setItem( COSName.PARAMS, params );
        }
        if( params != null )
        {
            params.setEmbeddedString(COSName.MAC, COSName.SUBTYPE, macSubtype);
        }
    }

    /**
     * Get the mac Creator.
     *
     * @return The mac Creator.
     */
    public String getMacCreator()
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        return params != null ? params.getEmbeddedString(COSName.MAC, COSName.CREATOR) : null;
    }

    /**
     * Set the mac Creator.
     *
     * @param macCreator The mac Creator.
     */
    public void setMacCreator( String macCreator )
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        if( params == null && macCreator != null )
        {
            params = new COSDictionary();
            getCOSObject().setItem( COSName.PARAMS, params );
        }
        if( params != null )
        {
            params.setEmbeddedString(COSName.MAC, COSName.CREATOR, macCreator);
        }
    }

    /**
     * Get the mac ResFork.
     *
     * @return The mac ResFork.
     */
    public String getMacResFork()
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        return params != null ? params.getEmbeddedString(COSName.MAC, COSName.RES_FORK) : null;
    }

    /**
     * Set the mac ResFork.
     *
     * @param macResFork The mac ResFork.
     */
    public void setMacResFork( String macResFork )
    {
        COSDictionary params = getCOSObject().getCOSDictionary(COSName.PARAMS);
        if( params == null && macResFork != null )
        {
            params = new COSDictionary();
            getCOSObject().setItem( COSName.PARAMS, params );
        }
        if( params != null )
        {
            params.setEmbeddedString(COSName.MAC, COSName.RES_FORK, macResFork);
        }
    }



}
