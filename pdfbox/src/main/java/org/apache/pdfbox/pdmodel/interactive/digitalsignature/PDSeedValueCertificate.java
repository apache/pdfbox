/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * this class represent certificate dictionary that is in the seed value which puts constraints on
 * certificates when signing documents
 *
 * @author Hossam Hazem
 */
public class PDSeedValueCertificate implements COSObjectable
{
    /**
     * A Ff flag.
     */
    public static final int FLAG_SUBJECT = 1;

    /**
     * A Ff flag.
     */
    public static final int FLAG_ISSUER = 1 << 1;

    /**
     * A Ff flag.
     */
    public static final int FLAG_OID = 1 << 2;

    /**
     * A Ff flag.
     */
    public static final int FLAG_SUBJECT_DN = 1 << 3;

    /**
     * A Ff flag.
     */
    public static final int FLAG_KEY_USAGE = 1 << 5;

    /**
     * A Ff flag.
     */
    public static final int FLAG_URL = 1 << 6;
    private final COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDSeedValueCertificate()
    {
        this.dictionary = new COSDictionary();
        this.dictionary.setItem(COSName.TYPE, COSName.SV_CERT);
        this.dictionary.setDirect(true);
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDSeedValueCertificate(COSDictionary dict)
    {
        this.dictionary = dict;
        this.dictionary.setDirect(true);
    }

    /**
     * Convert this standard java object to a COS dictionary.
     *
     * @return The COS dictionary that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     *
     * @return true if the Subject is required
     */
    public boolean isSubjectRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_SUBJECT);
    }

    /**
     * set true if subject shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Subject shall be enforced as a constraint.
     */
    public void setSubjectRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_SUBJECT, flag);
    }

    /**
     *
     * @return true if the Issuer is required
     */
    public boolean isIssuerRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_ISSUER);
    }

    /**
     * set true if Issuer shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Issuer shall be enforced as a constraint.
     */
    public void setIssuerRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_ISSUER, flag);
    }

    /**
     *
     * @return true if the OID is required
     */
    public boolean isOIDRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_OID);
    }

    /**
     * set true if OID shall be required as a constraint on signature.
     *
     * @param flag if true, the specified OID shall be enforced as a constraint.
     */
    public void setOIDRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_OID, flag);
    }

    /**
     *
     * @return true if the Subject DN is required
     */
    public boolean isSubjectDNRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_SUBJECT_DN);
    }

    /**
     * set true if subject DN shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Subject DN shall be enforced as a constraint.
     */
    public void setSubjectDNRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_SUBJECT_DN, flag);
    }

    /**
     *
     * @return true if the KeyUsage is required
     */
    public boolean isKeyUsageRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_KEY_USAGE);
    }

    /**
     * set true if KeyUsage shall be required as a constraint on signature.
     *
     * @param flag if true, the specified KeyUsage shall be enforced as a constraint.
     */
    public void setKeyUsageRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_KEY_USAGE, flag);
    }

    /**
     *
     * @return true if the URL is required
     */
    public boolean isURLRequired()
    {
        return this.getCOSObject().getFlag(COSName.FF, FLAG_URL);
    }

    /**
     * set true if URL shall be required as a constraint on signature.
     *
     * @param flag if true, the specified URL shall be enforced as a constraint.
     */
    public void setURLRequired(boolean flag)
    {
        this.getCOSObject().setFlag(COSName.FF, FLAG_URL, flag);
    }

    /**
     * Returns array of byte arrays that contains DER-encoded X.509v3 certificates
     */
    public List<byte[]> getSubject()
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.SUBJECT);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            return getListOfByteArraysFromCOSArray(array);
        }
        return null;
    }

    /**
     * (Optional) A list of byte arrays containing DER-encoded X.509v3 certificates that are
     * acceptable for signing. if
     * <b>Subject</b> is not null and {@link #isSubjectRequired()} is true then the subject
     * constraint is enforced on the subjects in this array subjects.
     *
     * @param subjects list of byte arrays containing DER-encoded X.509v3 certificates that are
     * acceptable for signing.
     */
    public void setSubject(List<byte[]> subjects)
    {
        COSArray array = new COSArray();
        for (byte[] subject : subjects)
        {
            array.add(new COSString(subject));
        }
        this.dictionary.setItem(COSName.SUBJECT, array);
    }

    /**
     * (Optional) byte array containing DER-encoded X.509v3 certificate that is acceptable for
     * signing. works like {@link #setSubject(List)} but one byte array
     *
     * @param subject byte array containing DER-encoded X.509v3 certificate
     */
    public void addSubject(byte[] subject)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.SUBJECT);
        COSArray array;
        if (base instanceof COSArray)
        {
            array = (COSArray) base;
        }
        else
        {
            array = new COSArray();
        }
        COSString string = new COSString(subject);
        array.add(string);
        this.dictionary.setItem(COSName.SUBJECT, array);
    }

    /**
     * removes a subject from the list
     *
     * @param subject byte array containing DER-encoded X.509v3 certificate
     */
    public void removeSubject(byte[] subject)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.SUBJECT);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            removeByteArrayFromCOSArray(subject, array);
        }
    }

    /**
     * Returns list of maps that contains subject distinguished names like [(cn: John Doe, o: Doe),
     * (cn: John Smith)] both keys are typically of the form 'cn', 'o', 'email', '2.5.4.43'; and
     * values are text strings.
     */
    public List<Map<String, String>> getSubjectDN()
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.SUBJECT_DN);
        if (base instanceof COSArray)
        {
            COSArray cosArray = (COSArray) base;
            List subjectDNList = cosArray.toList();
            List<Map<String, String>> result = new LinkedList<>();
            for (Object subjectDNItem : subjectDNList)
            {
                if (subjectDNItem instanceof COSDictionary)
                {
                    COSDictionary subjectDNItemDict = (COSDictionary) subjectDNItem;
                    Map<String, String> subjectDNMap = new HashMap<>();
                    for (COSName key : subjectDNItemDict.keySet())
                    {
                        subjectDNMap.put(key.getName(), subjectDNItemDict.getString(key));
                    }
                    result.add(subjectDNMap);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * (Optional; PDF 1.7) A list of maps, where each map contains key value pairs, that specify the
     * Subject Distinguished Name (DN) that must be present within the certificate for it to be
     * acceptable for signing. The certificate must at a minimum contain all the attributes
     * specified in one of the maps entered.
     *
     * @param subjectDN list of maps that contains subject distinguished names
     */
    public void setSubjectDN(List<Map<String, String>> subjectDN)
    {
        List<COSDictionary> subjectDNDict = new LinkedList<>();
        for (Map<String, String> subjectDNItem : subjectDN)
        {
            COSDictionary dict = new COSDictionary();
            for (Map.Entry<String, String> entry : subjectDNItem.entrySet())
            {
                dict.setItem(entry.getKey(), new COSString(entry.getValue()));
            }
            subjectDNDict.add(dict);
        }
        this.dictionary.setItem(COSName.SUBJECT_DN,
                COSArrayList.converterToCOSArray(subjectDNDict));
    }

    /**
     * Returns list of key usages of certificate strings where each string is 9 characters long and
     * each character is one of these values {0, 1, X} 0 for must not set, 1 for must set, X for
     * don't care. each index in the string represents a key usage:
     * <ol>
     * <li>digitalSignature</li>
     * <li>non-Repudiation</li>
     * <li>keyEncipherment</li>
     * <li>dataEncipherment</li>
     * <li>keyAgreement</li>
     * <li>keyCertSign</li>
     * <li>cRLSign</li>
     * <li>encipherOnly</li>
     * <li>decipherOnly</li>
     * </ol>
     */
    public List<String> getKeyUsage()
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.KEY_USAGE);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            List<String> keyUsageExtensions = new LinkedList<>();
            for (COSBase item : array)
            {
                if (item instanceof COSString)
                {
                    keyUsageExtensions.add(((COSString) item).getString());
                }
            }
            return keyUsageExtensions;
        }
        return null;
    }

    /**
     * (Optional; PDF 1.7) A List of ASCII strings, where each string specifies an acceptable
     * key-usage extension that must be present in the signing certificate. Multiple strings specify
     * a range of acceptable key-usage extensions; where each string 9 characters long and each
     * character is one of these values {0, 1, X} 0 for must not set, 1 for must set, X for don't
     * care. each index in the string represents a key usage:
     * <ol>
     * <li>digitalSignature</li>
     * <li>non-Repudiation</li>
     * <li>keyEncipherment</li>
     * <li>dataEncipherment</li>
     * <li>keyAgreement</li>
     * <li>keyCertSign</li>
     * <li>cRLSign</li>
     * <li>encipherOnly</li>
     * <li>decipherOnly</li>
     * </ol>
     *
     * @param keyUsageExtensions list of ASCII strings that consists only of {0, 1, X}
     */
    public void setKeyUsage(List<String> keyUsageExtensions)
    {
        this.dictionary.setItem(COSName.KEY_USAGE,
                COSArrayList.converterToCOSArray(keyUsageExtensions));
    }

    /**
     * (Optional; PDF 1.7) specifies an acceptable key-usage extension that must be presennt in the
     * signing certificate for works like {@link #setKeyUsage(List)} but takes only one string
     *
     * @param keyUsageExtension String that consist only of {0, 1, X}
     */
    public void addKeyUsage(String keyUsageExtension)
    {
        String allowedChars = "01X";
        for (int c = 0; c < keyUsageExtension.length(); c++)
        {
            if (allowedChars.indexOf(keyUsageExtension.charAt(c)) == -1)
            {
                throw new IllegalArgumentException("characters can only be 0, 1, X");
            }
        }
        COSBase base = this.dictionary.getDictionaryObject(COSName.KEY_USAGE);
        COSArray array;
        if (base instanceof COSArray)
        {
            array = (COSArray) base;
        }
        else
        {
            array = new COSArray();
        }
        COSString string = new COSString(keyUsageExtension);
        array.add(string);
        this.dictionary.setItem(COSName.KEY_USAGE, array);
    }

    /**
     * works like {@link #addKeyUsage(String)} but enters each character separately
     *
     * @param digitalSignature char that is one of {0, 1, X}
     * @param nonRepudiation char that is one of {0, 1, X}
     * @param keyEncipherment char that is one of {0, 1, X}
     * @param dataEncipherment char that is one of {0, 1, X}
     * @param keyAgreement char that is one of {0, 1, X}
     * @param keyCertSign char that is one of {0, 1, X}
     * @param cRLSign char that is one of {0, 1, X}
     * @param encipherOnly char that is one of {0, 1, X}
     * @param decipherOnly char that is one of {0, 1, X}
     */
    public void addKeyUsage(char digitalSignature, char nonRepudiation, char keyEncipherment,
            char dataEncipherment, char keyAgreement, char keyCertSign, char cRLSign,
            char encipherOnly, char decipherOnly)
    {
        String string = "" + digitalSignature + nonRepudiation + keyEncipherment + dataEncipherment
                + keyAgreement + keyCertSign + cRLSign + encipherOnly + decipherOnly;
        addKeyUsage(string);
    }

    /**
     * Removes a key usage extension
     *
     * @param keyUsageExtension ASCII string that consists of {0, 1, X}
     */
    public void removeKeyUsage(String keyUsageExtension)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.KEY_USAGE);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            Iterator<COSBase> iterator = array.iterator();
            while (iterator.hasNext())
            {
                COSBase item = iterator.next();
                if (item instanceof COSString)
                {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Returns list of array of bytes of DER-encoded X.509v3 certificates
     */
    public List<byte[]> getIssuer()
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.ISSUER);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            return getListOfByteArraysFromCOSArray(array);
        }
        return null;
    }

    /**
     * (Optional) A list of array of bytes containing DER-encoded X.509v3 certificates of acceptable
     * issuers. If the signer’s certificate chains up to any of the specified issuers (either
     * directly or indirectly), the certificate is considered acceptable for signing.
     *
     * @param issuers A list of byte array containing DER-encoded X.509v3 certificates
     */
    public void setIssuer(List<byte[]> issuers)
    {
        COSArray array = new COSArray();
        for (byte[] issuer : issuers)
        {
            array.add(new COSString(issuer));
        }
        this.dictionary.setItem(COSName.ISSUER, array);
    }

    /**
     * array of bytes containing DER-encoded X.509v3 certificates of acceptable issuers. If the
     * signer’s certificate chains up to any of the specified issuers (either directly or
     * indirectly), the certificate is considered acceptable for signing.
     *
     * @param issuer A byte array containing DER-encoded X.509v3 certificate
     */
    public void addIssuer(byte[] issuer)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.ISSUER);
        COSArray array;
        if (base instanceof COSArray)
        {
            array = (COSArray) base;
        }
        else
        {
            array = new COSArray();
        }
        COSString string = new COSString(issuer);
        array.add(string);
        this.dictionary.setItem(COSName.ISSUER, array);
    }

    /**
     * Removes an issuer from the issuers list
     *
     * @param issuer A byte array containing DER-encoded X.509v3 certificate
     */
    public void removeIssuer(byte[] issuer)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.ISSUER);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            removeByteArrayFromCOSArray(issuer, array);
        }
    }

    /**
     * Returns A list of array of bytes that contain Object Identifiers (OIDs) of the certificate
     * policies that must be present in the signing certificate
     */
    public List<byte[]> getOID()
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.OID);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            return getListOfByteArraysFromCOSArray(array);
        }
        return null;
    }

    /**
     * (Optional) A list of byte arrays that contain Object Identifiers (OIDs) of the certificate
     * policies that must be present in the signing certificate. This field is only applicable if
     * the value of Issuer is not empty.
     *
     * @param oidByteStrings list of byte arrays that contain OIDs
     */
    public void setOID(List<byte[]> oidByteStrings)
    {
        COSArray array = new COSArray();
        for (byte[] oid : oidByteStrings)
        {
            array.add(new COSString(oid));
        }
        this.dictionary.setItem(COSName.OID, array);
    }

    /**
     * works like {@link #setOID(List)} but for one object
     *
     * @param oid
     */
    public void addOID(byte[] oid)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.OID);
        COSArray array;
        if (base instanceof COSArray)
        {
            array = (COSArray) base;
        }
        else
        {
            array = new COSArray();
        }
        COSString string = new COSString(oid);
        array.add(string);
        this.dictionary.setItem(COSName.OID, array);
    }

    /**
     * removes an OID from the list
     *
     * @param oid
     */
    public void removeOID(byte[] oid)
    {
        COSBase base = this.dictionary.getDictionaryObject(COSName.OID);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            removeByteArrayFromCOSArray(oid, array);
        }
    }

    /**
     * returns String of the URL
     */
    public String getURL()
    {
        return this.dictionary.getString(COSName.URL);
    }

    /**
     * (Optional) A URL, the use for which is defined by the URLType entry.
     *
     * @param url String of the URL
     */
    public void setURL(String url)
    {
        this.dictionary.setString(COSName.URL, url);
    }

    /**
     * A name indicating the usage of the URL entry. There are standard uses and there can be
     * implementation-specific use for this URL. The following value specifies a valid standard
     * usage:
     * <ul>
     * <li>Browser, The URL references content that should be displayed in a web browser to allow
     * enrolling for a new credential if a matching credential is not found. The Ff attribute’s URL
     * bit is ignored for this usage.</li>
     * <li>ASSP, The URL references a signature web service that can be used for server-based
     * signing. If the Ff attribute’s URL bit indicates that this is a required constraint, this
     * implies that the credential used when signing must come from this server.</li>
     * </ul>
     *
     * @return string of URL type
     */
    public String getURLType()
    {
        return this.dictionary.getNameAsString(COSName.URL_TYPE);
    }

    /**
     * (Optional; PDF 1.7) A name indicating the usage of the URL entry. There are standard uses and
     * there can be implementation-specific uses for this URL. The following value specifies a valid
     * standard usage:
     * <ul>
     * <li>Browser, The URL references content that should be displayed in a web browser to allow
     * enrolling for a new credential if a matching credential is not found. The Ff attribute’s URL
     * bit is ignored for this usage.</li>
     * <li>ASSP, The URL references a signature web service that can be used for server-based
     * signing. If the Ff attribute’s URL bit indicates that this is a required constraint, this
     * implies that the credential used when signing must come from this server.</li>
     * </ul>
     * if urlType is not set the default is Browser for URL
     *
     * @param urlType
     */
    public void setURLType(String urlType)
    {
        this.dictionary.setName(COSName.URL_TYPE, urlType);
    }

    private static List<byte[]> getListOfByteArraysFromCOSArray(COSArray array)
    {
        List<byte[]> result = new LinkedList<>();
        for (COSBase item : array)
        {
            if (item instanceof COSString)
            {
                result.add(((COSString) item).getBytes());
            }
        }
        return result;
    }

    private static void removeByteArrayFromCOSArray(byte[] bytes, COSArray array)
    {
        COSString string = new COSString(bytes);
        Iterator<COSBase> iterator = array.iterator();
        while (iterator.hasNext())
        {
            COSBase item = iterator.next();
            if (item instanceof COSString)
            {
                if ((item).equals(string))
                {
                    iterator.remove();
                }
            }
        }
    }

}
