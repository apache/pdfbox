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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents a digital signature that can be attached to a document. To learn more about
 * digital signatures, read
 * <a href="https://www.adobe.com/devnet-docs/acrobatetk/tools/DigSig/Acrobat_DigitalSignatures_in_PDF.pdf">Digital
 * Signatures in a PDF</a> by Adobe.
 *
 * @author Ben Litchfield
 * @author Thomas Chojecki
 */
public class PDSignature implements COSObjectable
{
    private final COSDictionary dictionary;

    /**
     * A signature filter value.
     */
    public static final COSName FILTER_ADOBE_PPKLITE = COSName.ADOBE_PPKLITE;

    /**
     * A signature filter value.
     */
    public static final COSName FILTER_ENTRUST_PPKEF = COSName.ENTRUST_PPKEF;

    /**
     * A signature filter value.
     */
    public static final COSName FILTER_CICI_SIGNIT = COSName.CICI_SIGNIT;

    /**
     * A signature filter value.
     */
    public static final COSName FILTER_VERISIGN_PPKVS = COSName.VERISIGN_PPKVS;

    /**
     * A signature subfilter value.
     */
    public static final COSName SUBFILTER_ADBE_X509_RSA_SHA1 = COSName.ADBE_X509_RSA_SHA1;

    /**
     * A signature subfilter value.
     */
    public static final COSName SUBFILTER_ADBE_PKCS7_DETACHED = COSName.ADBE_PKCS7_DETACHED;

    /**
     * A signature subfilter value.
     */
    public static final COSName SUBFILTER_ETSI_CADES_DETACHED = COSName.getPDFName("ETSI.CAdES.detached");

    /**
     * A signature subfilter value.
     */
    public static final COSName SUBFILTER_ADBE_PKCS7_SHA1 = COSName.ADBE_PKCS7_SHA1;

    /**
     * Default constructor.
     */
    public PDSignature()
    {
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.TYPE, COSName.SIG);
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDSignature(COSDictionary dict)
    {
        dictionary = dict;
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
     * Set the dictionary type.
     *
     * @param type is the dictionary type.
     */
    public void setType(COSName type)
    {
        dictionary.setItem(COSName.TYPE, type);
    }

    /**
     * Set the filter.
     *
     * @param filter the filter to be used
     */
    public void setFilter(COSName filter)
    {
        dictionary.setItem(COSName.FILTER, filter);
    }

    /**
     * Set a subfilter that specify the signature that should be used.
     *
     * @param subfilter the subfilter that shall be used.
     */
    public void setSubFilter(COSName subfilter)
    {
        dictionary.setItem(COSName.SUB_FILTER, subfilter);
    }

    /**
     * Sets the name of the person or authority signing the document. According to the PDF
     * specification, this value should be used only when it is not possible to extract the name
     * from the signature.
     *
     * @param name the name to be used
     */
    public void setName(String name)
    {
        dictionary.setString(COSName.NAME, name);
    }

    /**
     * Sets the CPU host name or physical location of the signing.
     *
     * @param location the location to be used
     */
    public void setLocation(String location)
    {
        dictionary.setString(COSName.LOCATION, location);
    }

    /**
     * Sets the reason for the signing, such as (I agree...).
     *
     * @param reason the reason to be used
     */
    public void setReason(String reason)
    {
        dictionary.setString(COSName.REASON, reason);
    }

    /**
     * Sets the contact info provided by the signer to enable a recipient to contact the signer to
     * verify the signature, e.g. a phone number.
     *
     * @param contactInfo the contact info to be used
     */
    public void setContactInfo(String contactInfo)
    {
        dictionary.setString(COSName.CONTACT_INFO, contactInfo);
    }

    /**
     * Set the sign date.
     *
     * @param cal the date to be used as sign date
     */
    public void setSignDate(Calendar cal)
    {
        dictionary.setDate(COSName.M, cal);
    }

    /**
     * Returns the filter.
     * @return the filter
     */
    public String getFilter()
    {
        return dictionary.getNameAsString(COSName.FILTER);
    }

    /**
     * Returns the subfilter.
     *
     * @return the subfilter
     */
    public String getSubFilter()
    {
        return dictionary.getNameAsString(COSName.SUB_FILTER);
    }

    /**
     * Returns the name of the person or authority signing the document. According to the PDF
     * specification, this value should be used only when it is not possible to extract the name
     * from the signature.
     *
     * @return the name
     */
    public String getName()
    {
        return dictionary.getString(COSName.NAME);
    }

    /**
     * Returns the CPU host name or physical location of the signing.
     *
     * @return the location
     */
    public String getLocation()
    {
        return dictionary.getString(COSName.LOCATION);
    }

    /**
     * Returns the reason for the signing, such as (I agree...).
     *
     * @return the reason
     */
    public String getReason()
    {
        return dictionary.getString(COSName.REASON);
    }

    /**
     * Returns the contact info provided by the signer to enable a recipient to contact the signer
     * to verify the signature, e.g. a phone number.
     *
     * @return the contact info
     */
    public String getContactInfo()
    {
        return dictionary.getString(COSName.CONTACT_INFO);
    }

    /**
     * Returns the sign date.
     *
     * @return the sign date
     */
    public Calendar getSignDate()
    {
        return dictionary.getDate(COSName.M);
    }

    /**
     * Sets the byte range.
     *
     * @param range the byte range to be used
     */
    public void setByteRange(int[] range)
    {
        if (range.length!=4)
        {
            return;
        }
        COSArray ary = new COSArray();
        for ( int i : range )
        {
            ary.add(COSInteger.get(i));
        }

        dictionary.setItem(COSName.BYTERANGE, ary);
        ary.setDirect(true);
    }

    /**
     * Read out the byterange from the file.
     *
     * @return an integer array with the byterange, or an empty array if there is none.
     */
    public int[] getByteRange()
    {
        COSArray byteRange = dictionary.getCOSArray(COSName.BYTERANGE);
        if (byteRange == null)
        {
            return new int[0];
        }
        int[] ary = new int[byteRange.size()];
        for (int i = 0; i<ary.length;++i)
        {
            ary[i] = byteRange.getInt(i);
        }
        return ary;
    }

    /**
     * Returns the /Contents string as a byte array, i.e. the embedded signature between the
     * byterange gap.
     *
     * @return a byte array containing the signature, or an empty array if there isn't any.
     */
    public byte[] getContents()
    {
        COSBase base = dictionary.getDictionaryObject(COSName.CONTENTS);
        if (base instanceof COSString)
        {
            return ((COSString) base).getBytes();
        }
        return new byte[0];
    }

    /**
     * Will return the embedded signature between the byterange gap.
     *
     * @param pdfFile The signed pdf file as InputStream. It will be closed in this method.
     * @return a byte array containing the signature
     * @throws IOException if the pdfFile can't be read
     */
    public byte[] getContents(InputStream pdfFile) throws IOException
    {
        int[] byteRange = getByteRange();
        int begin = byteRange[0]+byteRange[1]+1;
        int len = byteRange[2]-begin;

        return getConvertedContents(new COSFilterInputStream(pdfFile,new int[] {begin,len}));
    }

    /**
     * Will return the embedded signature between the byterange gap.
     *
     * @param pdfFile The signed pdf file as byte array
     * @return a byte array containing the signature
     * @throws IOException if the pdfFile can't be read
     */
    public byte[] getContents(byte[] pdfFile) throws IOException
    {
        int[] byteRange = getByteRange();
        int begin = byteRange[0]+byteRange[1]+1;
        int len = byteRange[2]-begin-1;

        return getConvertedContents(new ByteArrayInputStream(pdfFile, begin, len));
    }

    private byte[] getConvertedContents(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int readLen;
        while ((readLen = is.read(buffer)) != -1)
        {
            int writeLen = readLen;
            int start = 0;
            // Filter < and (
            if(buffer[0]==0x3C || buffer[0]==0x28)
            {
                ++start;
                --writeLen;
            }
            // Filter > and ) at the end
            if(buffer[readLen-1]==0x3E || buffer[readLen-1]==0x29)
            {
                --writeLen;
            }
            baos.write(buffer, start, writeLen);
        }
        is.close();

        return COSString.parseHex(baos.toString("ISO-8859-1")).getBytes();
    }

    /**
     * Sets the contents.
     *
     * @param bytes contents to be used
     */
    public void setContents(byte[] bytes)
    {
        COSString string = new COSString(bytes);
        string.setForceHexForm(true);
        dictionary.setItem(COSName.CONTENTS, string);
    }

    /**
     * Return the signed content of the document. This is not a PDF file, nor is it the PDF file
     * before signing, it is the byte sequence made of the input minus the area where the signature
     * bytes will be. See "The ByteRange and signature value" in the document
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/DigitalSignaturesInPDF.pdf#page=5">Digital
     * Signatures in a PDF</a>.
     *
     * @param pdfFile The signed pdf file as InputStream. It will be closed in this method.
     * @return a byte array containing only the signed part of the content
     * @throws IOException if the pdfFile can't be read
     */
    public byte[] getSignedContent(InputStream pdfFile) throws IOException
    {
        try (COSFilterInputStream fis = new COSFilterInputStream(pdfFile, getByteRange()))
        {
            return fis.toByteArray();
        }
    }

    /**
     * Return the signed content of the document. This is not a PDF file, nor is it the PDF file
     * before signing, it is the byte sequence made of the input minus the area where the signature
     * bytes will be. See "The ByteRange and signature value" in the document
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/DigitalSignaturesInPDF.pdf#page=5">Digital
     * Signatures in a PDF</a>.
     *
     * @param pdfFile The signed pdf file as byte array
     * @return a byte array containing only the signed part of the content
     * @throws IOException if the pdfFile can't be read
     */
    public byte[] getSignedContent(byte[] pdfFile) throws IOException
    {
        try (COSFilterInputStream fis = new COSFilterInputStream(pdfFile, getByteRange()))
        {
            return fis.toByteArray();
        }
    }

    /**
     * PDF signature build dictionary. Provides information about the signature handler.
     *
     * @return the pdf signature build dictionary.
     */
    public PDPropBuild getPropBuild()
    {
        PDPropBuild propBuild = null;
        COSDictionary propBuildDic = dictionary.getCOSDictionary(COSName.PROP_BUILD);
        if (propBuildDic != null)
        {
            propBuild = new PDPropBuild(propBuildDic);
        }
        return propBuild;
    }

    /**
     * PDF signature build dictionary. Provides information about the signature handler.
     *
     * @param propBuild the prop build
     */
    public void setPropBuild(PDPropBuild propBuild)
    {
        dictionary.setItem(COSName.PROP_BUILD, propBuild);
    }
}
