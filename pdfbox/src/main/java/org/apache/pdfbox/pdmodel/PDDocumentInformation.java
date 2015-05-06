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
package org.apache.pdfbox.pdmodel;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This is the document metadata.  Each getXXX method will return the entry if
 * it exists or null if it does not exist.  If you pass in null for the setXXX
 * method then it will clear the value.
 *
 * @author Ben Litchfield
 * @author Gerardo Ortiz
 *
 */
public class PDDocumentInformation implements COSObjectable
{
    private final COSDictionary info;

    /**
     * Default Constructor.
     */
    public PDDocumentInformation()
    {
        info = new COSDictionary();
    }

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param dic The underlying dictionary.
     */
    public PDDocumentInformation( COSDictionary dic )
    {
        info = dic;
    }

    /**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return info;
    }    
    
    /**
     * Return the properties String value.
     * <p>
     * Allows to retrieve the
     * low level date for validation purposes.
     * </p> 
     * 
     * @param propertyKey the dictionaries key
     * @return the properties value
     */
     public Object getPropertyStringValue(String propertyKey)
     {
         return info.getString(propertyKey);
     }    

    /**
     * This will get the title of the document.  This will return null if no title exists.
     *
     * @return The title of the document.
     */
    public String getTitle()
    {
        return info.getString( COSName.TITLE );
    }

    /**
     * This will set the title of the document.
     *
     * @param title The new title for the document.
     */
    public void setTitle( String title )
    {
        info.setString( COSName.TITLE, title );
    }

    /**
     * This will get the author of the document.  This will return null if no author exists.
     *
     * @return The author of the document.
     */
    public String getAuthor()
    {
        return info.getString( COSName.AUTHOR );
    }

    /**
     * This will set the author of the document.
     *
     * @param author The new author for the document.
     */
    public void setAuthor( String author )
    {
        info.setString( COSName.AUTHOR, author );
    }

    /**
     * This will get the subject of the document.  This will return null if no subject exists.
     *
     * @return The subject of the document.
     */
    public String getSubject()
    {
        return info.getString( COSName.SUBJECT );
    }

    /**
     * This will set the subject of the document.
     *
     * @param subject The new subject for the document.
     */
    public void setSubject( String subject )
    {
        info.setString( COSName.SUBJECT, subject );
    }

    /**
     * This will get the keywords of the document.  This will return null if no keywords exists.
     *
     * @return The keywords of the document.
     */
    public String getKeywords()
    {
        return info.getString( COSName.KEYWORDS );
    }

    /**
     * This will set the keywords of the document.
     *
     * @param keywords The new keywords for the document.
     */
    public void setKeywords( String keywords )
    {
        info.setString( COSName.KEYWORDS, keywords );
    }

    /**
     * This will get the creator of the document.  This will return null if no creator exists.
     *
     * @return The creator of the document.
     */
    public String getCreator()
    {
        return info.getString( COSName.CREATOR );
    }

    /**
     * This will set the creator of the document.
     *
     * @param creator The new creator for the document.
     */
    public void setCreator( String creator )
    {
        info.setString( COSName.CREATOR, creator );
    }

    /**
     * This will get the producer of the document.  This will return null if no producer exists.
     *
     * @return The producer of the document.
     */
    public String getProducer()
    {
        return info.getString( COSName.PRODUCER );
    }

    /**
     * This will set the producer of the document.
     *
     * @param producer The new producer for the document.
     */
    public void setProducer( String producer )
    {
        info.setString( COSName.PRODUCER, producer );
    }

    /**
     * This will get the creation date of the document.  This will return null if no creation date exists.
     *
     * @return The creation date of the document.
     */
    public Calendar getCreationDate()
    {
        return info.getDate( COSName.CREATION_DATE );
    }

    /**
     * This will set the creation date of the document.
     *
     * @param date The new creation date for the document.
     */
    public void setCreationDate( Calendar date )
    {
        info.setDate( COSName.CREATION_DATE, date );
    }

    /**
     * This will get the modification date of the document.  This will return null if no modification date exists.
     *
     * @return The modification date of the document.
     */
    public Calendar getModificationDate()
    {
        return info.getDate( COSName.MOD_DATE );
    }

    /**
     * This will set the modification date of the document.
     *
     * @param date The new modification date for the document.
     */
    public void setModificationDate( Calendar date )
    {
        info.setDate( COSName.MOD_DATE, date );
    }

    /**
     * This will get the trapped value for the document.
     * This will return null if one is not found.
     *
     * @return The trapped value for the document.
     */
    public String getTrapped()
    {
        return info.getNameAsString( COSName.TRAPPED );
    }

    /**
     * This will get the keys of all metadata information fields for the document.
     *
     * @return all metadata key strings.
     * @since Apache PDFBox 1.3.0
     */
    public Set<String> getMetadataKeys()
    {
        Set<String> keys = new TreeSet<String>();
        for (COSName key : info.keySet())
        {
            keys.add(key.getName());
        }
        return keys;
    }

    /**
     *  This will get the value of a custom metadata information field for the document.
     *  This will return null if one is not found.
     *
     * @param fieldName Name of custom metadata field from pdf document.
     *
     * @return String Value of metadata field
     */
    public String getCustomMetadataValue(String fieldName)
    {
        return info.getString( fieldName );
    }

    /**
     * Set the custom metadata value.
     *
     * @param fieldName The name of the custom metadata field.
     * @param fieldValue The value to the custom metadata field.
     */
    public void setCustomMetadataValue( String fieldName, String fieldValue )
    {
        info.setString( fieldName, fieldValue );
    }

    /**
     * This will set the trapped of the document.  This will be
     * 'True', 'False', or 'Unknown'.
     *
     * @param value The new trapped value for the document.
     */
    public void setTrapped( String value )
    {
        if( value != null &&
            !value.equals( "True" ) &&
            !value.equals( "False" ) &&
            !value.equals( "Unknown" ) )
        {
            throw new RuntimeException( "Valid values for trapped are " +
                                        "'True', 'False', or 'Unknown'" );
        }

        info.setName( COSName.TRAPPED, value );
    }
}
