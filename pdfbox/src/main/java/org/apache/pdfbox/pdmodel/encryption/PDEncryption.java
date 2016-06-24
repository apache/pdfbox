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
 * @author Ben Litchfield
 * @author Benoit Guillon
 */
public class PDEncryption
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

    private final COSDictionary dictionary;
    private SecurityHandler securityHandler;

    /**
     * creates a new empty encryption dictionary.
     */
    public PDEncryption()
    {
        dictionary = new COSDictionary();
    }

    /**
     * creates a new encryption dictionary from the low level dictionary provided.
     * @param dictionary a COS encryption dictionary
     */
    public PDEncryption(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
        securityHandler = SecurityHandlerFactory.INSTANCE.newSecurityHandlerForFilter(getFilter());
    }

    /**
     * Returns the security handler specified in the dictionary's Filter entry.
     * @return a security handler instance
     * @throws IOException if there is no security handler available which matches the Filter
     */
    public SecurityHandler getSecurityHandler() throws IOException
    {
        if (securityHandler == null)
        {
            throw new IOException("No security handler for filter " + getFilter());
        }
        return securityHandler;
    }

    /**
     * Sets the security handler used in this encryption dictionary
     * @param securityHandler new security handler
     */
    public void setSecurityHandler(SecurityHandler securityHandler)
    {
        this.securityHandler = securityHandler;
        // TODO set Filter (currently this is done by the security handlers)
    }

    /**
     * Returns true if the security handler specified in the dictionary's Filter is available.
     * @return true if the security handler is available
     */
    public boolean hasSecurityHandler()
    {
        return securityHandler == null;
    }

    /**
     * This will get the dictionary associated with this encryption dictionary.
     *
     * @return The COS dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * Sets the filter entry of the encryption dictionary.
     *
     * @param filter The filter name.
     */
    public void setFilter(String filter)
    {
        dictionary.setItem(COSName.FILTER, COSName.getPDFName(filter));
    }

    /**
     * Get the name of the filter.
     *
     * @return The filter name contained in this encryption dictionary.
     */
    public final String getFilter()
    {
        return dictionary.getNameAsString( COSName.FILTER );
    }

    /**
     * Get the name of the subfilter.
     *
     * @return The subfilter name contained in this encryption dictionary.
     */
    public String getSubFilter()
    {
        return dictionary.getNameAsString( COSName.SUB_FILTER );
    }

    /**
     * Set the subfilter entry of the encryption dictionary.
     *
     * @param subfilter The value of the subfilter field.
     */
    public void setSubFilter(String subfilter)
    {
        dictionary.setName(COSName.SUB_FILTER, subfilter);
    }

    /**
     * This will set the V entry of the encryption dictionary.<br><br>
     * See PDF Reference 1.4 Table 3.13.  <br><br>
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param version The new encryption version.
     */
    public void setVersion(int version)
    {
        dictionary.setInt(COSName.V, version);
    }

    /**
     * This will return the V entry of the encryption dictionary.<br><br>
     * See PDF Reference 1.4 Table 3.13.
     *
     * @return The encryption version to use.
     */
    public int getVersion()
    {
        return dictionary.getInt( COSName.V, 0 );
    }

    /**
     * This will set the number of bits to use for the encryption algorithm.
     *
     * @param length The new key length.
     */
    public void setLength(int length)
    {
        dictionary.setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the Length entry of the encryption dictionary.<br><br>
     * The length in <b>bits</b> for the encryption algorithm.  This will return a multiple of 8.
     *
     * @return The length in bits for the encryption algorithm
     */
    public int getLength()
    {
        return dictionary.getInt( COSName.LENGTH, 40 );
    }

    /**
     * This will set the R entry of the encryption dictionary.<br><br>
     * See PDF Reference 1.4 Table 3.14.  <br><br>
     *
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param revision The new encryption version.
     */
    public void setRevision(int revision)
    {
        dictionary.setInt(COSName.R, revision);
    }

    /**
     * This will return the R entry of the encryption dictionary.<br><br>
     * See PDF Reference 1.4 Table 3.14.
     *
     * @return The encryption revision to use.
     */
    public int getRevision()
    {
        return dictionary.getInt( COSName.R, DEFAULT_VERSION );
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
        dictionary.setItem(COSName.O, new COSString(o));
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
        COSString owner = (COSString) dictionary.getDictionaryObject( COSName.O );
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
        dictionary.setItem(COSName.U, new COSString(u));
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
        COSString user = (COSString) dictionary.getDictionaryObject( COSName.U );
        if( user != null )
        {
            u = user.getBytes();
        }
        return u;
    }

    /**
     * This will set the OE entry in the standard encryption dictionary.
     *
     * @param oe A 32 byte array or null if there is no owner encryption key.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setOwnerEncryptionKey(byte[] oe) throws IOException
    {
        dictionary.setItem( COSName.OE, new COSString(oe) );
    }

    /**
     * This will get the OE entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no owner encryption key.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public byte[] getOwnerEncryptionKey() throws IOException
    {
        byte[] oe = null;
        COSString ownerEncryptionKey = (COSString)dictionary.getDictionaryObject( COSName.OE );
        if( ownerEncryptionKey != null )
        {
            oe = ownerEncryptionKey.getBytes();
        }
        return oe;
    }

    /**
     * This will set the UE entry in the standard encryption dictionary.
     *
     * @param ue A 32 byte array or null if there is no user encryption key.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setUserEncryptionKey(byte[] ue) throws IOException
    {
        dictionary.setItem( COSName.UE, new COSString(ue) );
    }

    /**
     * This will get the UE entry in the standard encryption dictionary.
     *
     * @return A 32 byte array or null if there is no user encryption key.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public byte[] getUserEncryptionKey() throws IOException
    {
        byte[] ue = null;
        COSString userEncryptionKey = (COSString)dictionary.getDictionaryObject( COSName.UE );
        if( userEncryptionKey != null )
        {
            ue = userEncryptionKey.getBytes();
        }
        return ue;
    }

    /**
     * This will set the permissions bit mask.
     *
     * @param permissions The new permissions bit mask
     */
    public void setPermissions(int permissions)
    {
        dictionary.setInt(COSName.P, permissions);
    }

    /**
     * This will get the permissions bit mask.
     *
     * @return The permissions bit mask.
     */
    public int getPermissions()
    {
        return dictionary.getInt( COSName.P, 0 );
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
        
        COSBase value = dictionary.getDictionaryObject(COSName.ENCRYPT_META_DATA);
        
        if (value instanceof COSBoolean)
        {
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
        for (byte[] recipient : recipients)
        {
            COSString recip = new COSString(recipient);
            array.add(recip);
        }
        dictionary.setItem(COSName.RECIPIENTS, array);
    }

    /**
     * Returns the number of recipients contained in the Recipients field of the dictionary.
     *
     * @return the number of recipients contained in the Recipients field.
     */
    public int getRecipientsLength()
    {
        COSArray array = (COSArray) dictionary.getItem(COSName.RECIPIENTS);
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
        COSArray array = (COSArray) dictionary.getItem(COSName.RECIPIENTS);
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
        COSDictionary cryptFilterDictionary = (COSDictionary) dictionary.getDictionaryObject( COSName.CF );
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
     * Sets the crypt filter with the given name.
     * 
     * @param cryptFilterName the name of the crypt filter
     * @param cryptFilterDictionary the crypt filter to set
     */
    public void setCryptFilterDictionary(COSName cryptFilterName, PDCryptFilterDictionary cryptFilterDictionary)
    {
        COSDictionary cfDictionary = (COSDictionary)dictionary.getDictionaryObject( COSName.CF );
        if (cfDictionary == null)
        {
            cfDictionary = new COSDictionary();
            dictionary.setItem(COSName.CF, cfDictionary);
        }
        
        cfDictionary.setItem(cryptFilterName, cryptFilterDictionary.getCOSDictionary());
    }
    
    /**
     * Sets the standard crypt filter.
     * 
     * @param cryptFilterDictionary the standard crypt filter to set
     */
    public void setStdCryptFilterDictionary(PDCryptFilterDictionary cryptFilterDictionary)
    {
        setCryptFilterDictionary(COSName.STD_CF, cryptFilterDictionary);
    }
    
    /**
     * Returns the name of the filter which is used for de/encrypting streams.
     * Default value is "Identity".
     * 
     * @return the name of the filter
     */
    public COSName getStreamFilterName() 
    {
        COSName stmF = (COSName) dictionary.getDictionaryObject( COSName.STM_F );
        if (stmF == null)
        {
            stmF = COSName.IDENTITY;
        }
        return stmF;
    }

    /**
     * Sets the name of the filter which is used for de/encrypting streams.
     * 
     * @param streamFilterName the name of the filter
     */
    public void setStreamFilterName(COSName streamFilterName)
    {
        dictionary.setItem(COSName.STM_F, streamFilterName);
    }

    /**
     * Returns the name of the filter which is used for de/encrypting strings.
     * Default value is "Identity".
     * 
     * @return the name of the filter
     */
    public COSName getStringFilterName() 
    {
        COSName strF = (COSName) dictionary.getDictionaryObject( COSName.STR_F );
        if (strF == null)
        {
            strF = COSName.IDENTITY;
        }
        return strF;
    }

    /**
     * Sets the name of the filter which is used for de/encrypting strings.
     * 
     * @param stringFilterName the name of the filter
     */
    public void setStringFilterName(COSName stringFilterName)
    {
        dictionary.setItem(COSName.STR_F, stringFilterName);
    }

    /**
     * Set the Perms entry in the encryption dictionary.
     *
     * @param perms A 16 byte array.
     *
     * @throws IOException If there is an error setting the data.
     */
    public void setPerms(byte[] perms) throws IOException
    {
        dictionary.setItem( COSName.PERMS, new COSString(perms) );
    }

    /**
     * Get the Perms entry in the encryption dictionary.
     *
     * @return A 16 byte array or null if there is no Perms entry.
     *
     * @throws IOException If there is an error accessing the data.
     */
    public byte[] getPerms() throws IOException
    {
        byte[] perms = null;
        COSString permsCosString = (COSString)dictionary.getDictionaryObject( COSName.PERMS );
        if( permsCosString != null )
        {
            perms = permsCosString.getBytes();
        }
        return perms;
    }

    /**
     * remove CF, StmF, and StrF entries. This is to be called if V is not 4 or 5.
     */
    public void removeV45filters()
    {
        dictionary.setItem(COSName.CF, null);
        dictionary.setItem(COSName.STM_F, null);
        dictionary.setItem(COSName.STR_F, null);
    }
}
