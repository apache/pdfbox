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

package org.apache.pdfbox.pdmodel.encryption;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * This class is a specialized view of the encryption dictionary of a PDF document.
 * It contains a low level dictionary (COSDictionary) and provides the methods to
 * manage its fields.
 *
 * The available fields are the ones who are involved by standard security handler
 * and public key security handler.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 *
 * @version $Revision: 1.7 $
 */
public class PDEncryptionDictionary
{
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION0_UNDOCUMENTED_UNSUPPORTED = 0;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION1_40_BIT_ALGORITHM = 1;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION2_VARIABLE_LENGTH_ALGORITHM = 2;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION3_UNPUBLISHED_ALGORITHM = 3;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION4_SECURITY_HANDLER = 4;

    /**
     * The default security handler.
     */
    public static final String DEFAULT_NAME = "Standard";

    /**
     * The default length for the encryption key.
     */
    public static final int DEFAULT_LENGTH = 40;

    /**
     * The default version, according to the PDF Reference.
     */
    public static final int DEFAULT_VERSION = VERSION0_UNDOCUMENTED_UNSUPPORTED;

    /**
     * COS encryption dictionary.
     */
    protected COSDictionary encryptionDictionary = null;

    /**
     * creates a new empty encryption dictionary.
     */
    public PDEncryptionDictionary()
    {
        encryptionDictionary = new COSDictionary();
    }

    /**
     * creates a new encryption dictionary from the low level dictionary provided.
     * @param d the low level dictionary that will be managed by the newly created object
     */
    public PDEncryptionDictionary(COSDictionary d)
    {
        encryptionDictionary = d;
    }

    /**
     * This will get the dictionary associated with this encryption dictionary.
     *
     * @return The COS dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return encryptionDictionary;
    }

    /**
     * Sets the filter entry of the encryption dictionary.
     *
     * @param filter The filter name.
     */
    public void setFilter(String filter)
    {
        encryptionDictionary.setItem( COSName.FILTER, COSName.getPDFName( filter ) );
    }

    /**
     * Get the name of the filter.
     *
     * @return The filter name contained in this encryption dictionary.
     */
    public String getFilter()
    {
        return encryptionDictionary.getNameAsString( COSName.FILTER );
    }

    /**
     * Get the name of the subfilter.
     *
     * @return The subfilter name contained in this encryption dictionary.
     */
    public String getSubFilter()
    {
        return encryptionDictionary.getNameAsString( COSName.SUB_FILTER );
    }

    /**
     * Set the subfilter entry of the encryption dictionary.
     *
     * @param subfilter The value of the subfilter field.
     */
    public void setSubFilter(String subfilter)
    {
        encryptionDictionary.setName( COSName.SUB_FILTER, subfilter );
    }

    /**
     * This will set the V entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.13.  <br /><br/>
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param version The new encryption version.
     */
    public void setVersion(int version)
    {
        encryptionDictionary.setInt( COSName.V, version );
    }

    /**
     * This will return the V entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.13.
     *
     * @return The encryption version to use.
     */
    public int getVersion()
    {
        return encryptionDictionary.getInt( COSName.V, 0 );
    }

    /**
     * This will set the number of bits to use for the encryption algorithm.
     *
     * @param length The new key length.
     */
    public void setLength(int length)
    {
        encryptionDictionary.setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the Length entry of the encryption dictionary.<br /><br />
     * The length in <b>bits</b> for the encryption algorithm.  This will return a multiple of 8.
     *
     * @return The length in bits for the encryption algorithm
     */
    public int getLength()
    {
        return encryptionDictionary.getInt( COSName.LENGTH, 40 );
    }

    /**
     * This will set the R entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.14.  <br /><br/>
     *
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param revision The new encryption version.
     */
    public void setRevision(int revision)
    {
        encryptionDictionary.setInt( COSName.R, revision );
    }

    /**
     * This will return the R entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.14.
     *
     * @return The encryption revision to use.
     */
    public int getRevision()
    {
        return encryptionDictionary.getInt( COSName.R, DEFAULT_VERSION );
    }

     /**
     * This will set the O entry in the standard encryption dictionary.
     *
     * @param o A 32 byte array or null if there is no owner key.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setOwnerKey(byte[] o) throws IOException
    {
        COSString owner = new COSString();
        owner.append( o );
        encryptionDictionary.setItem( COSName.O, owner );
    }

    /**
     * This will get the O entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no owner key.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public byte[] getOwnerKey() throws IOException
    {
        byte[] o = null;
        COSString owner = (COSString)encryptionDictionary.getDictionaryObject( COSName.O );
        if( owner != null )
        {
            o = owner.getBytes();
        }
        return o;
    }

    /**
     * This will set the U entry in the standard encryption dictionary.
     *
     * @param u A 32 byte array.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setUserKey(byte[] u) throws IOException
    {
        COSString user = new COSString();
        user.append( u );
        encryptionDictionary.setItem( COSName.U, user );
    }

    /**
     * This will get the U entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no user key.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public byte[] getUserKey() throws IOException
    {
        byte[] u = null;
        COSString user = (COSString)encryptionDictionary.getDictionaryObject( COSName.U );
        if( user != null )
        {
            u = user.getBytes();
        }
        return u;
    }

    /**
     * This will set the permissions bit mask.
     *
     * @param permissions The new permissions bit mask
     */
    public void setPermissions(int permissions)
    {
        encryptionDictionary.setInt( COSName.P, permissions );
    }

    /**
     * This will get the permissions bit mask.
     *
     * @return The permissions bit mask.
     */
    public int getPermissions()
    {
        return encryptionDictionary.getInt( COSName.P, 0 );
    }

    /**
     * Will get the EncryptMetaData dictionary info.
     * 
     * @return true if EncryptMetaData is explicitly set to false (the default is true)
     */
    public boolean isEncryptMetaData()
    {
        // default is true (see 7.6.3.2 Standard Encryption Dictionary PDF 32000-1:2008)
        boolean encryptMetaData = true;
        
        COSBase value = encryptionDictionary.getDictionaryObject(COSName.ENCRYPT_META_DATA);
        
        if (value instanceof COSBoolean) {
            encryptMetaData = ((COSBoolean)value).getValue();
        }
        
        return encryptMetaData;
    }
    
    /**
     * This will set the Recipients field of the dictionary. This field contains an array
     * of string.
     * @param recipients the array of bytes arrays to put in the Recipients field.
     * @throws IOException If there is an error setting the data.
     */
    public void setRecipients(byte[][] recipients) throws IOException
    {
        COSArray array = new COSArray();
        for(int i=0; i<recipients.length; i++)
        {
            COSString recip = new COSString();
            recip.append(recipients[i]);
            recip.setForceLiteralForm(true);
            array.add(recip);
        }
        encryptionDictionary.setItem(COSName.RECIPIENTS, array);
    }

    /**
     * Returns the number of recipients contained in the Recipients field of the dictionary.
     *
     * @return the number of recipients contained in the Recipients field.
     */
    public int getRecipientsLength()
    {
        COSArray array = (COSArray)encryptionDictionary.getItem(COSName.RECIPIENTS);
        return array.size();
    }

    /**
     * returns the COSString contained in the Recipients field at position i.
     *
     * @param i the position in the Recipients field array.
     *
     * @return a COSString object containing information about the recipient number i.
     */
    public COSString getRecipientStringAt(int i)
    {
        COSArray array = (COSArray)encryptionDictionary.getItem(COSName.RECIPIENTS);
        return (COSString)array.get(i);
    }
    
    /**
     * Returns the standard crypt filter.
     * 
     * @return the standard crypt filter if available.
     */
    public PDCryptFilterDictionary getStdCryptFilterDictionary() 
    {
        return getCryptFilterDictionary(COSName.STD_CF);
    }

    /**
     * Returns the crypt filter with the given name.
     * 
     * @param cryptFilterName the name of the crypt filter
     * 
     * @return the crypt filter with the given name if available
     */
    public PDCryptFilterDictionary getCryptFilterDictionary(COSName cryptFilterName) 
    {
        COSDictionary cryptFilterDictionary = (COSDictionary)encryptionDictionary.getDictionaryObject( COSName.CF );
        if (cryptFilterDictionary != null)
        {
            COSDictionary stdCryptFilterDictionary = (COSDictionary)cryptFilterDictionary.getDictionaryObject(cryptFilterName);
            if (stdCryptFilterDictionary != null)
            {
                return new PDCryptFilterDictionary(stdCryptFilterDictionary);
            }
        }
        return null;
    }
    
    /**
     * Returns the name of the filter which is used for de/encrypting streams.
     * Default value is "Identity".
     * 
     * @return the name of the filter
     */
    public COSName getStreamFilterName() 
    {
        COSName stmF = (COSName)encryptionDictionary.getDictionaryObject( COSName.STM_F );
        if (stmF == null)
        {
            stmF = COSName.IDENTITY;
        }
        return stmF;
    }

    /**
     * Returns the name of the filter which is used for de/encrypting strings.
     * Default value is "Identity".
     * 
     * @return the name of the filter
     */
    public COSName getStringFilterName() 
    {
        COSName strF = (COSName)encryptionDictionary.getDictionaryObject( COSName.STR_F );
        if (strF == null)
        {
            strF = COSName.IDENTITY;
        }
        return strF;
    }

}

    