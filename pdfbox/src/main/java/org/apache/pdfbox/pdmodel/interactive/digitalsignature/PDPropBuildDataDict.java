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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * <p>This represents the general property dictionaries from the build property dictionary.</p>
 *
 * @see PDPropBuild
 * @author Thomas Chojecki
 * @version $Revision: 1.1 $
 */
public class PDPropBuildDataDict implements COSObjectable
{

    private COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDPropBuildDataDict()
    {
        dictionary = new COSDictionary();
        dictionary.setDirect(true); // the specification claim to use direct objects
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDPropBuildDataDict(COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true); // the specification claim to use direct objects
    }


    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return getDictionary();
    }

    /**
     * Convert this standard java object to a COS dictionary.
     *
     * @return The COS dictionary that matches this Java object.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * The name of the software module that was used to create the signature.
     * @return the name of the software module
     */
    public String getName()
    {
        return dictionary.getString(COSName.NAME);
    }

    /**
     * The name of the software module that was used to create the signature.
     *
     * @param name is the name of the software module
     */
    public void setName(String name)
    {
        dictionary.setName(COSName.NAME, name);
    }

    /**
     * The build date of the software module.
     *
     * @return the build date of the software module
     */
    public String getDate()
    {
        return dictionary.getString(COSName.DATE);
    }

    /**
     * The build date of the software module. This string is normally produced by the
     * compiler under C++.
     *
     * @param date is the build date of the software module
     */
    public void setDate(String date)
    {
        dictionary.setString(COSName.DATE, date);
    }

    /**
     * The software module revision number, corresponding to the Date attribute.
     *
     * @return the revision of the software module
     */
    public long getRevision()
    {
        return dictionary.getLong(COSName.R);
    }

    /**
     * The software module revision number, corresponding to the Date attribute.
     *
     * @param revision is the software module revision number
     */
    public void setRevision(long revision)
    {
        dictionary.setLong(COSName.R, revision);
    }

    /**
     * The software module revision number, used to determinate the minimum version
     * of software that is required in order to process this signature.
     *
     * @return the revision of the software module
     */
    public long getMinimumRevision()
    {
        return dictionary.getLong(COSName.V);
    }

    /**
     * The software module revision number, used to determinate the minimum version
     * of software that is required in order to process this signature.
     *
     * @param revision is the software module revision number
     */
    public void setMinimumRevision(long revision)
    {
        dictionary.setLong(COSName.V, revision);
    }

    /**
     * A flag that can be used by the signature handler or software module to
     * indicate that this signature was created with unrelease software.
     *
     * @return true if the software module or signature handler was a pre release.
     */
    public boolean getPreRelease()
    {
        return dictionary.getBoolean(COSName.PRE_RELEASE, false);
    }

    /**
     * A flag that can be used by the signature handler or software module to
     * indicate that this signature was created with unrelease software.
     *
     * @param preRelease is true if the signature was created with a unrelease
     *                   software, otherwise false.
     */
    public void setPreRelease(boolean preRelease)
    {
        dictionary.setBoolean(COSName.PRE_RELEASE, preRelease);
    }

    /**
     * Indicates the operation system. The format isn't specified yet.
     *
     * @return a the operation system id or name.
     */
    public String getOS()
    {
        return dictionary.getString(COSName.OS);
    }

    /**
     * Indicates the operation system. The format isn't specified yet.
     *
     * @param os is a string with the system id or name.
     */
    public void setOS(String os)
    {
        dictionary.setString(COSName.OS, os);
    }

    /**
     * If there is a LegalPDF dictionary in the catalog
     * of the PDF file and the NonEmbeddedFonts attribute in this dictionary
     * has a non zero value, and the viewing application has a preference
     * set to suppress the display of this warning then the value of this
     * attribute will be set to true.
     *
     * @return true if NonEFontNoWarn is set to true
     */
    public boolean getNonEFontNoWarn()
    {
        return dictionary.getBoolean(COSName.NON_EFONT_NO_WARN, true);
    }

    /*
     * setNonEFontNoWarn missing. Maybe not needed or should be self
     * implemented.
     *
     * Documentation says:
     * (Optional; PDF 1.5) If there is a LegalPDF dictionary in the catalog
     * of the PDF file and the NonEmbeddedFonts attribute in this dictionary
     * has a non zero value, and the viewing application has a preference
     * set to suppress the display of this warning then the value of this
     * attribute will be set to true.
     */

    /**
     * If true, the application was in trusted mode when signing took place.
     *
     * @return true if the application was in trusted mode while signing.
     *              default: false
     */
    public boolean getTrustedMode()
    {
        return dictionary.getBoolean(COSName.TRUSTED_MODE, false);
    }

    /**
     * If true, the application was in trusted mode when signing took place.
     *
     * @param trustedMode true if the application is in trusted mode.
     */
    public void setTrustedMode(boolean trustedMode)
    {
        dictionary.setBoolean(COSName.TRUSTED_MODE, trustedMode);
    }
}
