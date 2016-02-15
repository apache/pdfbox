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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * The general property dictionaries from the build property dictionary.
 *
 * @see PDPropBuild
 * @author Thomas Chojecki
 */
public class PDPropBuildDataDict implements COSObjectable
{
    private final COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDPropBuildDataDict()
    {
        dictionary = new COSDictionary();
        // the specification claim to use direct objects
        dictionary.setDirect(true);
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDPropBuildDataDict(COSDictionary dict)
    {
        dictionary = dict;
        // the specification claim to use direct objects
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
     * The name of the software module that was used to create the signature.
     * @return the name of the software module
     */
    public String getName()
    {
        return dictionary.getNameAsString(COSName.NAME);
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
     * The build date of the software module. This string is normally produced by the compiler that
     * is used to compile the software, for example using the Date and Time preprocessor flags. As
     * such, this not likely to be in PDF Date format.
     *
     * @return the build date of the software module
     */
    public String getDate()
    {
        return dictionary.getString(COSName.DATE);
    }

    /**
     * The build date of the software module. This string is normally produced by the compiler.
     *
     * @param date is the build date of the software module
     */
    public void setDate(String date)
    {
        dictionary.setString(COSName.DATE, date);
    }

    /**
     * A text string indicating the version of the application implementation, as described by the
     * <code>Name</code> attribute in this dictionary. When set by Adobe Acrobat, this entry is in
     * the format: major.minor.micro (for example 7.0.7).
     * <p>
     * NOTE: Version value is specific for build data dictionary when used as the <code>App</code>
     * dictionary in a build properties dictionary.
     * </p>
     *
     * @param applicationVersion the application implementation version
     */
    public void setVersion(String applicationVersion)
    {
        dictionary.setString("REx", applicationVersion);
    }

    /**
     * A text string indicating the version of the application implementation, as described by the
     * <code>/Name</code> attribute in this dictionary. When set by Adobe Acrobat, this entry is in
     * the format: major.minor.micro (for example 7.0.7).
     *
     * @return the application implementation version
     */
    public String getVersion()
    {
        return dictionary.getString("REx");
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
     * The software module revision number, used to determinate the minimum version of software that
     * is required in order to process this signature.
     * <p>
     * NOTE: this entry is deprecated for PDF v1.7
     * </p>
     *
     * @return the revision of the software module
     */
    public long getMinimumRevision()
    {
        return dictionary.getLong(COSName.V);
    }

    /**
     * The software module revision number, used to determinate the minimum version of software that
     * is required in order to process this signature.
     * <p>
     * NOTE: this entry is deprecated for PDF v1.7
     * </p>
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
     * Indicates the operating system. The string format isn't specified yet. In its PDF Signature
     * Build Dictionary Specifications Adobe differently specifies the value type to store operating
     * system string:<ul>
     * <li>Specification for PDF v1.5 specifies type as string;</li>
     * <li>Specification for PDF v1.7 specifies type as array and provided example for
     * <code>/PropBuild</code> dictionary indicate it as array of names.</li>
     * </ul>
     * This method supports both types to retrieve the value.
     *
     * @return the operating system id or name.
     */
    public String getOS()
    {
        final COSBase cosBase = dictionary.getItem(COSName.OS);
        if (cosBase instanceof COSArray)
        {
            return ((COSArray) cosBase).getName(0);
        }
        // PDF v1.5 style
        return dictionary.getString(COSName.OS);
    }

    /**
     * Indicates the operating system. The string format isn't specified yet. Value will be stored
     * as first item of the array, as specified in PDF Signature Build Dictionary Specification for
     * PDF v1.7.
     *
     * @param os is a string with the system id or name.
     */
    public void setOS(String os)
    {
        if (os == null)
        {
            dictionary.removeItem(COSName.OS);
        }
        else
        {
            COSBase osArray = dictionary.getItem(COSName.OS);
            if (!(osArray instanceof COSArray))
            {
                osArray = new COSArray();
                osArray.setDirect(true);
                dictionary.setItem(COSName.OS, osArray);
            }
            ((COSArray) osArray).add(0, COSName.getPDFName(os));
        }
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
