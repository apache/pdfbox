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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents a certificate seed value dictionary that is in the seed value which puts
 * constraints on certificates when signing documents.
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
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.TYPE, COSName.SV_CERT);
        dictionary.setDirect(true);
    }

    /**
     * Constructor.
     *
     * @param dict The certificate seed value dictionary.
     */
    public PDSeedValueCertificate(final COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true);
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
        return dictionary.getFlag(COSName.FF, FLAG_SUBJECT);
    }

    /**
     * set true if subject shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Subject shall be enforced as a constraint.
     */
    public void setSubjectRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_SUBJECT, flag);
    }

    /**
     *
     * @return true if the Issuer is required
     */
    public boolean isIssuerRequired()
    {
        return dictionary.getFlag(COSName.FF, FLAG_ISSUER);
    }

    /**
     * set true if Issuer shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Issuer shall be enforced as a constraint.
     */
    public void setIssuerRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_ISSUER, flag);
    }

    /**
     *
     * @return true if the OID is required
     */
    public boolean isOIDRequired()
    {
        return dictionary.getFlag(COSName.FF, FLAG_OID);
    }

    /**
     * set true if OID shall be required as a constraint on signature.
     *
     * @param flag if true, the specified OID shall be enforced as a constraint.
     */
    public void setOIDRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_OID, flag);
    }

    /**
     *
     * @return true if the Subject DN is required
     */
    public boolean isSubjectDNRequired()
    {
        return dictionary.getFlag(COSName.FF, FLAG_SUBJECT_DN);
    }

    /**
     * set true if subject DN shall be required as a constraint on signature.
     *
     * @param flag if true, the specified Subject DN shall be enforced as a constraint.
     */
    public void setSubjectDNRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_SUBJECT_DN, flag);
    }

    /**
     *
     * @return true if the KeyUsage is required
     */
    public boolean isKeyUsageRequired()
    {
        return dictionary.getFlag(COSName.FF, FLAG_KEY_USAGE);
    }

    /**
     * set true if KeyUsage shall be required as a constraint on signature.
     *
     * @param flag if true, the specified KeyUsage shall be enforced as a constraint.
     */
    public void setKeyUsageRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_KEY_USAGE, flag);
    }

    /**
     *
     * @return true if the URL is required
     */
    public boolean isURLRequired()
    {
        return dictionary.getFlag(COSName.FF, FLAG_URL);
    }

    /**
     * set true if URL shall be required as a constraint on signature.
     *
     * @param flag if true, the specified URL shall be enforced as a constraint.
     */
    public void setURLRequired(final boolean flag)
    {
        dictionary.setFlag(COSName.FF, FLAG_URL, flag);
    }

    /**
     * Returns list of byte arrays that contains DER-encoded X.509v3 certificates
     */
    public List<byte[]> getSubject()
    {
        final COSArray array = dictionary.getCOSArray(COSName.SUBJECT);
        return array != null ? getListOfByteArraysFromCOSArray(array) : null;
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
    public void setSubject(final List<byte[]> subjects)
    {
        dictionary.setItem(COSName.SUBJECT, convertListOfByteArraysToCOSArray(subjects));
    }

    /**
     * (Optional) byte array containing DER-encoded X.509v3 certificate that is acceptable for
     * signing. works like {@link #setSubject(List)} but one byte array
     *
     * @param subject byte array containing DER-encoded X.509v3 certificate
     */
    public void addSubject(final byte[] subject)
    {
        COSArray array = dictionary.getCOSArray(COSName.SUBJECT);
        if (array == null)
        {
            array = new COSArray();
        }
        array.add(new COSString(subject));
        dictionary.setItem(COSName.SUBJECT, array);
    }

    /**
     * removes a subject from the list
     *
     * @param subject byte array containing DER-encoded X.509v3 certificate
     */
    public void removeSubject(final byte[] subject)
    {
        final COSArray array = dictionary.getCOSArray(COSName.SUBJECT);
        if (array != null)
        {
            array.remove(new COSString(subject));
        }
    }

    /**
     * Returns list of maps that contains subject distinguished names like [(cn: John Doe, o: Doe),
     * (cn: John Smith)] both keys are typically of the form 'cn', 'o', 'email', '2.5.4.43'; and
     * values are text strings.
     */
    public List<Map<String, String>> getSubjectDN()
    {
        final COSArray cosArray = dictionary.getCOSArray(COSName.SUBJECT_DN);
        if (cosArray != null)
        {
            final List<? extends COSBase> subjectDNList = cosArray.toList();
            final List<Map<String, String>> result = new LinkedList<>();
            for (final COSBase subjectDNItem : subjectDNList)
            {
                if (subjectDNItem instanceof COSDictionary)
                {
                    final COSDictionary subjectDNItemDict = (COSDictionary) subjectDNItem;
                    final Map<String, String> subjectDNMap = new HashMap<>();
                    for (final COSName key : subjectDNItemDict.keySet())
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
    public void setSubjectDN(final List<Map<String, String>> subjectDN)
    {
        final List<COSDictionary> subjectDNDict = new LinkedList<>();
        for (final Map<String, String> subjectDNItem : subjectDN)
        {
            final COSDictionary dict = new COSDictionary();
            subjectDNItem.forEach((key, value) -> dict.setItem(key, new COSString(value)));
            subjectDNDict.add(dict);
        }
        dictionary.setItem(COSName.SUBJECT_DN, new COSArray(subjectDNDict));
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
        final COSArray array = dictionary.getCOSArray(COSName.KEY_USAGE);
        if (array != null)
        {
            final List<String> keyUsageExtensions = new LinkedList<>();
            for (final COSBase item : array)
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
    public void setKeyUsage(final List<String> keyUsageExtensions)
    {
        dictionary.setItem(COSName.KEY_USAGE,
                COSArray.ofCOSStrings(keyUsageExtensions));
    }

    /**
     * (Optional; PDF 1.7) specifies an acceptable key-usage extension that must be presennt in the
     * signing certificate for works like {@link #setKeyUsage(List)} but takes only one string
     *
     * @param keyUsageExtension String that consist only of {0, 1, X}
     */
    public void addKeyUsage(final String keyUsageExtension)
    {
        final String allowedChars = "01X";
        for (int c = 0; c < keyUsageExtension.length(); c++)
        {
            if (allowedChars.indexOf(keyUsageExtension.charAt(c)) == -1)
            {
                throw new IllegalArgumentException("characters can only be 0, 1, X");
            }
        }
        COSArray array = dictionary.getCOSArray(COSName.KEY_USAGE);
        if (array == null)
        {
            array = new COSArray();
        }
        array.add(new COSString(keyUsageExtension));
        dictionary.setItem(COSName.KEY_USAGE, array);
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
    public void addKeyUsage(final char digitalSignature, final char nonRepudiation, final char keyEncipherment,
                            final char dataEncipherment, final char keyAgreement, final char keyCertSign, final char cRLSign,
                            final char encipherOnly, final char decipherOnly)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(digitalSignature);
        builder.append(nonRepudiation);
        builder.append(keyEncipherment);
        builder.append(dataEncipherment);
        builder.append(keyAgreement);
        builder.append(keyCertSign);
        builder.append(cRLSign);
        builder.append(encipherOnly);
        builder.append(decipherOnly);
        
        addKeyUsage(builder.toString());
    }

    /**
     * Removes a key usage extension
     *
     * @param keyUsageExtension ASCII string that consists of {0, 1, X}
     */
    public void removeKeyUsage(final String keyUsageExtension)
    {
        final COSArray array = dictionary.getCOSArray(COSName.KEY_USAGE);
        if (array != null)
        {
            array.remove(new COSString(keyUsageExtension));
        }
    }

    /**
     * Returns list of array of bytes of DER-encoded X.509v3 certificates
     */
    public List<byte[]> getIssuer()
    {
        final COSArray array = dictionary.getCOSArray(COSName.ISSUER);
        return array != null ? getListOfByteArraysFromCOSArray(array) : null;
    }

    /**
     * (Optional) A list of array of bytes containing DER-encoded X.509v3 certificates of acceptable
     * issuers. If the signer’s certificate chains up to any of the specified issuers (either
     * directly or indirectly), the certificate is considered acceptable for signing.
     *
     * @param issuers A list of byte array containing DER-encoded X.509v3 certificates
     */
    public void setIssuer(final List<byte[]> issuers)
    {
        dictionary.setItem(COSName.ISSUER, convertListOfByteArraysToCOSArray(issuers));
    }

    /**
     * array of bytes containing DER-encoded X.509v3 certificates of acceptable issuers. If the
     * signer’s certificate chains up to any of the specified issuers (either directly or
     * indirectly), the certificate is considered acceptable for signing.
     *
     * @param issuer A byte array containing DER-encoded X.509v3 certificate
     */
    public void addIssuer(final byte[] issuer)
    {
        COSArray array = dictionary.getCOSArray(COSName.ISSUER);
        if (array == null)
        {
            array = new COSArray();
        }
        array.add(new COSString(issuer));
        dictionary.setItem(COSName.ISSUER, array);
    }

    /**
     * Removes an issuer from the issuers list
     *
     * @param issuer A byte array containing DER-encoded X.509v3 certificate
     */
    public void removeIssuer(final byte[] issuer)
    {
        final COSArray array = dictionary.getCOSArray(COSName.ISSUER);
        if (array != null)
        {
            array.remove(new COSString(issuer));
        }
    }

    /**
     * Returns A list of array of bytes that contain Object Identifiers (OIDs) of the certificate
     * policies that must be present in the signing certificate
     */
    public List<byte[]> getOID()
    {
        final COSArray array = dictionary.getCOSArray(COSName.OID);
        return array != null ? getListOfByteArraysFromCOSArray(array) : null;
    }

    /**
     * (Optional) A list of byte arrays that contain Object Identifiers (OIDs) of the certificate
     * policies that must be present in the signing certificate. This field is only applicable if
     * the value of Issuer is not empty.
     *
     * @param oidByteStrings list of byte arrays that contain OIDs
     */
    public void setOID(final List<byte[]> oidByteStrings)
    {
        dictionary.setItem(COSName.OID, convertListOfByteArraysToCOSArray(oidByteStrings));
    }

    /**
     * works like {@link #setOID(List)} but for one object
     *
     * @param oid
     */
    public void addOID(final byte[] oid)
    {
        COSArray array = dictionary.getCOSArray(COSName.OID);
        if (array == null)
        {
            array = new COSArray();
        }
        array.add(new COSString(oid));
        dictionary.setItem(COSName.OID, array);
    }

    /**
     * removes an OID from the list
     *
     * @param oid
     */
    public void removeOID(final byte[] oid)
    {
        final COSArray array = dictionary.getCOSArray(COSName.OID);
        if (array != null)
        {
            array.remove(new COSString(oid));
        }
    }

    /**
     * returns String of the URL
     */
    public String getURL()
    {
        return dictionary.getString(COSName.URL);
    }

    /**
     * (Optional) A URL, the use for which is defined by the URLType entry.
     *
     * @param url String of the URL
     */
    public void setURL(final String url)
    {
        dictionary.setString(COSName.URL, url);
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
        return dictionary.getNameAsString(COSName.URL_TYPE);
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
     * Third parties can extend the use of this attribute with their own attribute values, which
     * must conform to the guidelines specified in
     * <a href="http://www.adobe.com/content/dam/acom/en/devnet/pdf/PDF32000_2008.pdf#page=681">PDF
     * Spec 1.7 Appendix E (PDF Name Registry)</a>
     * if urlType is not set the default is Browser for URL
     *
     * @param urlType String of the urlType
     */
    public void setURLType(final String urlType)
    {
        dictionary.setName(COSName.URL_TYPE, urlType);
    }

    private static List<byte[]> getListOfByteArraysFromCOSArray(final COSArray array)
    {
        final List<byte[]> result = new LinkedList<>();
        for (final COSBase item : array)
        {
            if (item instanceof COSString)
            {
                result.add(((COSString) item).getBytes());
            }
        }
        return result;
    }

    private static COSArray convertListOfByteArraysToCOSArray(final List<byte[]> strings)
    {
        final COSArray array = new COSArray();
        strings.forEach(s -> array.add(new COSString(s)));
        return array;
    }
}
