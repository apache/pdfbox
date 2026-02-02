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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents a pdf signature seed value dictionary.
 *
 * @author Thomas Chojecki
 */
public class PDSeedValue implements COSObjectable
{

    private static final List<String> allowedDigestNames = Arrays.asList(
            COSName.DIGEST_SHA1.getName(), //
            COSName.DIGEST_SHA256.getName(), //
            COSName.DIGEST_SHA384.getName(), //
            COSName.DIGEST_SHA512.getName(), //
            COSName.DIGEST_RIPEMD160.getName());

    /**
     * A Ff flag.
     */
    public static final int FLAG_FILTER = 1;

    /**
     * A Ff flag.
     */
    public static final int FLAG_SUBFILTER = 1 << 1;

    /**
     * A Ff flag.
     */
    public static final int FLAG_V = 1 << 2;

    /**
     * A Ff flag.
     */
    public static final int FLAG_REASON = 1 << 3;

    /**
     * A Ff flag.
     */
    public static final int FLAG_LEGAL_ATTESTATION = 1 << 4;

    /**
     * A Ff flag.
     */
    public static final int FLAG_ADD_REV_INFO = 1 << 5;

    /**
     * A Ff flag.
     */
    public static final int FLAG_DIGEST_METHOD = 1 << 6;

    private final COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDSeedValue()
    {
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.TYPE, COSName.SV);
        dictionary.setDirect(true); // the specification claim to use direct objects
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDSeedValue(COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true); // the specification claim to use direct objects
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
     * @return true if the Filter is required
     */
    public boolean isFilterRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_FILTER);
    }

    /**
     * set true if the filter shall be required.
     * 
     * @param flag if true, the specified Filter shall be used when signing.
     */
    public void setFilterRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_FILTER, flag);
    }

    /**
     *
     * @return true if the SubFilter is required
     */
    public boolean isSubFilterRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_SUBFILTER);
    }

    /**
     * set true if the subfilter shall be required.
     * 
     * @param flag if true, the first supported SubFilter in the array shall be used when signing.
     */
    public void setSubFilterRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_SUBFILTER, flag);
    }

    /**
    *
    * @return true if the DigestMethod is required
    */
    public boolean isDigestMethodRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_DIGEST_METHOD);
    }

    /**
     * set true if the DigestMethod shall be required.
     * 
     * @param flag if true, one digest from the array shall be used.
     */
    public void setDigestMethodRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_DIGEST_METHOD, flag);
    }

    /**
    *
    * @return true if the V entry is required
    */
    public boolean isVRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_V);
    }

    /**
     * set true if the V entry shall be required.
     * 
     * @param flag if true, the V entry shall be used.
     */
    public void setVRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_V, flag);
    }

    /**
    *
    * @return true if the Reason is required
    */
    public boolean isReasonRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_REASON);
    }

    /**
     * set true if the Reason shall be required.
     * 
     * @param flag if true, the Reason entry shall be used.
     */
    public void setReasonRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_REASON, flag);
    }

    /**
    *
    * @return true if the LegalAttestation is required
    */
    public boolean isLegalAttestationRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_LEGAL_ATTESTATION);
    }

    /**
     * set true if the LegalAttestation shall be required.
     * 
     * @param flag if true, the LegalAttestation entry shall be used.
     */
    public void setLegalAttestationRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_LEGAL_ATTESTATION, flag);
    }

    /**
    *
    * @return true if the AddRevInfo is required
    */
    public boolean isAddRevInfoRequired()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_ADD_REV_INFO);
    }

    /**
     * set true if the AddRevInfo shall be required.
     * 
     * @param flag if true, the AddRevInfo shall be used.
     */
    public void setAddRevInfoRequired(boolean flag)
    {
        getCOSObject().setFlag( COSName.FF, FLAG_ADD_REV_INFO, flag);
    }

    /**
     * If <b>Filter</b> is not null and the {@link #isFilterRequired()} indicates this entry is a
     * required constraint, then the signature handler specified by this entry shall be used when
     * signing; otherwise, signing shall not take place. If {@link #isFilterRequired()} indicates
     * that this is an optional constraint, this handler may be used if it is available. If it is
     * not available, a different handler may be used instead.
     *
     * @return the filter that shall be used by the signature handler
     */
    public String getFilter()
    {
        return dictionary.getNameAsString(COSName.FILTER);
    }

    /**
     * (Optional) The signature handler that shall be used to sign the signature field.
     *
     * @param filter is the filter that shall be used by the signature handler
     */
    public void setFilter(COSName filter)
    {
        dictionary.setItem(COSName.FILTER, filter);
    }

    /**
     * If <b>SubFilter</b> is not null and the {@link #isSubFilterRequired()} indicates this
     * entry is a required constraint, then the first matching encodings shall be used when
     * signing; otherwise, signing shall not take place. If {@link #isSubFilterRequired()}
     * indicates that this is an optional constraint, then the first matching encoding shall
     * be used if it is available. If it is not available, a different encoding may be used
     * instead.
     *
     * @return the subfilter that shall be used by the signature handler
     */
    public List<String> getSubFilter()
    {
        COSArray fields = dictionary.getCOSArray(COSName.SUB_FILTER);
        return fields != null ? fields.toCOSNameStringList() : Collections.emptyList();
    }

    /**
     * (Optional) An array of names indicating encodings to use when signing. The first name
     * in the array that matches an encoding supported by the signature handler shall be the
     * encoding that is actually used for signing.
     *
     * @param subfilter is the name that shall be used for encoding
     */
    public void setSubFilter(List<String> subfilter)
    {
        dictionary.setItem(COSName.SUB_FILTER,
                COSArray.ofCOSNames(subfilter));
    }

    /**
     * An array of names indicating acceptable digest algorithms to use when
     * signing. The value shall be one of <b>SHA1</b>, <b>SHA256</b>, <b>SHA384</b>,
     * <b>SHA512</b>, <b>RIPEMD160</b>. The default value is implementation-specific.
     *
     * @return the digest method that shall be used by the signature handler
     */
    public List<String> getDigestMethod()
    {
        COSArray fields = dictionary.getCOSArray(COSName.DIGEST_METHOD);
        return fields != null ? fields.toCOSNameStringList() : Collections.emptyList();
    }

    /**
     * <p>(Optional, PDF 1.7) An array of names indicating acceptable digest
     * algorithms to use when signing. The value shall be one of <b>SHA1</b>,
     * <b>SHA256</b>, <b>SHA384</b>, <b>SHA512</b>, <b>RIPEMD160</b>. The default
     * value is implementation-specific.</p>
     *
     * <p>This property is only applicable if the digital credential signing contains RSA
     * public/privat keys</p>
     *
     * @param digestMethod is a list of possible names of the digests, that should be
     * used for signing.
     */
    public void setDigestMethod(List<String> digestMethod)
    {
        // integrity check
        for (String digestName : digestMethod)
        {
            if (!allowedDigestNames.contains(digestName))
            {
                throw new IllegalArgumentException(
                        "Specified digest " + digestName + " isn't allowed.");
            }
        }
        dictionary.setItem(COSName.DIGEST_METHOD,
                COSArray.ofCOSNames(digestMethod));
    }

    /**
     * The minimum required capability of the signature field seed value
     * dictionary parser. A value of 1 specifies that the parser shall be able to
     * recognize all seed value dictionary entries in a PDF 1.5 file. A value of 2
     * specifies that it shall be able to recognize all seed value dictionary entries
     * specified.
     *
     * @return the minimum required capability of the signature field seed value
     * dictionary parser
     */
    public float getV()
    {
        return dictionary.getFloat(COSName.V);
    }

    /**
     * (Optional) The minimum required capability of the signature field seed value
     * dictionary parser. A value of 1 specifies that the parser shall be able to
     * recognize all seed value dictionary entries in a PDF 1.5 file. A value of 2
     * specifies that it shall be able to recognize all seed value dictionary entries
     * specified.
     *
     * @param minimumRequiredCapability is the minimum required capability of the
     * signature field seed value dictionary parser
     */
    public void setV(float minimumRequiredCapability)
    {
        dictionary.setFloat(COSName.V, minimumRequiredCapability);
    }

    /**
     * If the Reasons array is provided and {@link #isReasonRequired()} indicates that
     * Reasons is a required constraint, one of the reasons in the array shall be used
     * for the signature dictionary; otherwise signing shall not take place. If the
     * {@link #isReasonRequired()} indicates Reasons is an optional constraint, one of
     * the reasons in the array may be chose or a custom reason can be provided.
     *
     * @return the reasons that should be used by the signature handler
     */
    public List<String> getReasons()
    {
        COSArray fields = dictionary.getCOSArray(COSName.REASONS);
        return fields != null ? fields.toCOSNameStringList() : Collections.emptyList();
    }

    /**
     * (Optional) An array of text strings that specifying possible reasons for signing
     * a document. If specified, the reasons supplied in this entry replace those used
     * by conforming products.
     *
     * @param reasons is a list of possible text string that specifying possible reasons
     */
    public void setReasons(List<String> reasons)
    {
        dictionary.setItem(COSName.REASONS, COSArray.ofCOSStrings(reasons));
    }

    /**
     * <p>(Optional; PDF 1.6) A dictionary containing a single entry whose key is P
     * and whose value is an integer between 0 and 3. A value of 0 defines the
     * signatures as an author signature. The value 1 through 3 shall be used for
     * certification signatures and correspond to the value of P in a DocMDP transform
     * parameters dictionary.</p>
     *
     * <p>If this MDP key is not present or the MDP dictionary does not contain a P
     * entry, no rules shall be defined regarding the type of signature or its
     * permissions.</p>
     *
     * @return the mdp dictionary as PDSeedValueMDP
     */
    public PDSeedValueMDP getMDP()
    {
        COSDictionary dict = dictionary.getCOSDictionary(COSName.MDP);
        return dict != null ? new PDSeedValueMDP(dict) : null;
    }

    /**
     * <p>(Optional; PDF 1.6) A dictionary containing a single entry whose key is P
     * and whose value is an integer between 0 and 3. A value of 0 defines the
     * signatures as an author signature. The value 1 through 3 shall be used for
     * certification signatures and correspond to the value of P in a DocMDP transform
     * parameters dictionary.</p>
     *
     * <p>If this MDP key is not present or the MDP dictionary does not contain a P
     * entry, no rules shall be defined regarding the type of signature or its
     * permissions.</p>
     *
     * @param mdp dictionary
     */
    public void setMPD(PDSeedValueMDP mdp)
    {
        if (mdp != null)
        {
            dictionary.setItem(COSName.MDP, mdp.getCOSObject());
        }
    }

    /**
     * (Optional) A certificate seed value dictionary containing information about the certificate
     * to be used when signing.
     *
     * @return dictionary
     */
    public PDSeedValueCertificate getSeedValueCertificate()
    {
        COSDictionary certificate = dictionary.getCOSDictionary(COSName.CERT);
        return certificate != null ? new PDSeedValueCertificate(certificate) : null;
    }

    /**
     * (Optional) A certificate seed value dictionary containing information about the certificate
     * to be used when signing.
     *
     * @param certificate dictionary
     */
    public void setSeedValueCertificate(PDSeedValueCertificate certificate)
    {
        dictionary.setItem(COSName.CERT, certificate);
    }

    /**
     * <p>(Optional; PDF 1.6) A time stamp dictionary containing two entries. URL which
     * is a ASCII string specifying the URL to a rfc3161 conform timestamp server and Ff
     * to indicate if a timestamp is required or optional.</p>
     *
     * @return the timestamp dictionary as PDSeedValueTimeStamp
     */
    public PDSeedValueTimeStamp getTimeStamp()
    {
        COSDictionary dict = dictionary.getCOSDictionary(COSName.TIME_STAMP);
        return dict != null ? new PDSeedValueTimeStamp(dict) : null;
    }

    /**
     * <p>(Optional; PDF 1.6) A time stamp dictionary containing two entries. URL which
     * is a ASCII string specifying the URL to a rfc3161 conform timestamp server and Ff
     * to indicate if a timestamp is required or optional.</p>
     *
     * @param timestamp dictionary
     */
    public void setTimeStamp(PDSeedValueTimeStamp timestamp)
    {
        if (timestamp != null)
        {
            dictionary.setItem(COSName.TIME_STAMP, timestamp.getCOSObject());
        }
    }

    /**
     * (Optional, PDF 1.6) An array of text strings that specifying possible legal
     * attestations.
     *
     * @return the reasons that should be used by the signature handler
     */
    public List<String> getLegalAttestation()
    {
        COSArray fields = dictionary.getCOSArray(COSName.LEGAL_ATTESTATION);
        return fields != null ? fields.toCOSNameStringList() : Collections.emptyList();
    }

    /**
     * (Optional, PDF 1.6) An array of text strings that specifying possible legal
     * attestations.
     *
     * @param legalAttestation is a list of possible text string that specifying possible
     * legal attestations.
     */
    public void setLegalAttestation(List<String> legalAttestation)
    {
        dictionary.setItem(COSName.LEGAL_ATTESTATION,
                COSArray.ofCOSStrings(legalAttestation));
    }
}
