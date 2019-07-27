/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.graphic;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceNAttributes;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceNProcess;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_DEVICE_N_LIMIT;

/**
 * This class doesn't define restrictions on ColorSpace. It checks only the consistency of the Color space with the
 * DestOutputIntent.
 */
public class StandardColorSpaceHelper implements ColorSpaceHelper
{
    /**
     * The context which contains useful information to process the validation.
     */
    protected PreflightContext context = null;
    /**
     * The ICCProfile contained in the DestOutputIntent
     */
    protected ICCProfileWrapper iccpw = null;
    /**
     * High level object which represents the colors space to check.
     */
    protected PDColorSpace pdcs = null;

    protected StandardColorSpaceHelper(PreflightContext _context, PDColorSpace _cs)
    {
        this.context = _context;
        this.pdcs = _cs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pdfbox.preflight.graphic.color.ColorSpaceHelper#validate(java .util.List)
     */
    @Override
    public final void validate() throws ValidationException
    {
        if (pdcs == null)
        {
            throw new ValidationException("Unable to create a PDColorSpace with the value null");
        }

        this.iccpw = ICCProfileWrapper.getOrSearchICCProfile(context);
        processAllColorSpace(pdcs);
    }

    /**
     * Method called by the validate method. According to the ColorSpace, a specific ColorSpace method is called.
     * 
     * @param colorSpace the color space object to check.
     */
    protected final void processAllColorSpace(PDColorSpace colorSpace)
    {
        ColorSpaces cs = ColorSpaces.valueOf(colorSpace.getName());

        switch (cs)
        {
        case DeviceRGB:
        case RGB:
            processRGBColorSpace(colorSpace);
            break;
        case DeviceCMYK:
        case CMYK:
            processCYMKColorSpace(colorSpace);
            break;
        case CalRGB:
        case CalGray:
        case Lab:
            processCalibratedColorSpace(colorSpace);
            break;
        case DeviceGray:
        case G:
            processGrayColorSpace(colorSpace);
            break;
        case ICCBased:
            processICCBasedColorSpace(colorSpace);
            break;
        case DeviceN:
            processDeviceNColorSpace(colorSpace);
            break;
        case Indexed:
        case I:
            processIndexedColorSpace(colorSpace);
            break;
        case Separation:
            processSeparationColorSpace(colorSpace);
            break;
        case Pattern:
            processPatternColorSpace(colorSpace);
            break;
        default:
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE, cs.getLabel()
                    + " is unknown as ColorSpace"));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is DeviceRGB.
     * 
     */
    protected void processRGBColorSpace(PDColorSpace colorSpace)
    {
        if (!processDefaultColorSpace(colorSpace))
        {
            if (iccpw == null)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                        "DestOutputProfile is missing"));
            }
            else if (!iccpw.isRGBColorSpace())
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB,
                        "DestOutputProfile isn't RGB ColorSpace"));
            }
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is DeviceCYMK.
     * 
     */
    protected void processCYMKColorSpace(PDColorSpace colorSpace)
    {
        if (!processDefaultColorSpace(colorSpace))
        {
            if (iccpw == null)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                        "DestOutputProfile is missing"));
            }
            else if (!iccpw.isCMYKColorSpace())
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK,
                        "DestOutputProfile isn't CMYK ColorSpace"));
            }
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is a Pattern.
     * @param colorSpace 
     */
    protected void processPatternColorSpace(PDColorSpace colorSpace)
    {
        if (iccpw == null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                    "DestOutputProfile is missing"));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is DeviceGray.
     * 
     */
    protected void processGrayColorSpace(PDColorSpace colorSpace)
    {
        if (!processDefaultColorSpace(colorSpace) && iccpw == null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                    "DestOutputProfile is missing"));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is a calibrated color (CalGray, CalRGB, Lab).
     * @param colorSpace 
     * 
     */
    protected void processCalibratedColorSpace(PDColorSpace colorSpace)
    {
        // ---- OutputIntent isn't mandatory
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is a ICCBased color space. Because this kind
     * of ColorSpace can have alternate color space, the processAllColorSpace is called to check this alternate color
     * space. (Pattern is forbidden as Alternate Color Space)
     * 
     * @param colorSpace
     *            the color space object to check.
     */
    protected void processICCBasedColorSpace(PDColorSpace colorSpace)
    {
        PDICCBased iccBased = (PDICCBased) colorSpace;
        try
        {
            InputStream is = iccBased.getPDStream().createInputStream();
            // check that ICC profile loads (PDICCBased also does this, but catches the exception)
            // PDFBOX-2819: load ICC profile as a stream, not as a byte array because of java error
            ICC_Profile iccp = ICC_Profile.getInstance(is);
            is.close();
            PDColorSpace altpdcs = iccBased.getAlternateColorSpace();
            if (altpdcs != null)
            {
                ColorSpaces altCsId = ColorSpaces.valueOf(altpdcs.getName());
                if (altCsId == ColorSpaces.Pattern)
                {
                    context.addValidationError(new ValidationError(
                            ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN,
                            "Pattern is forbidden as AlternateColorSpace of a ICCBased"));
                }

                /*
                 * According to the ISO-19005-1:2005
                 * 
                 * A conforming reader shall render ICCBased colour spaces as specified by the ICC specification,
                 * and shall not use the Alternate colour space specified in an ICC profile stream dictionary
                 * 
                 * We don't check the alternate ColorSpaces
                 */
                // PDFBOX-4611, PDFBOX-4607: Yes we do because Adobe Reader chokes on it
                // and because VeraPDF and PDF-Tools do it.
                if (!validateICCProfileNEntry(iccBased.getPDStream().getCOSObject(), iccp))
                {
                    return;
                }
                if (!validateICCProfileVersion(iccp))
                {
                    return;
                }
                validateICCProfileAlternateEntry(iccBased);
            }
        }
        catch (IllegalArgumentException e)
        {
            // this is not a ICC_Profile
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED,
                    "ICCBased color space is invalid: " + e.getMessage(), e));
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                    "Unable to read ICCBase color space: " + e.getMessage(), e));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is DeviceN. Because this kind of ColorSpace
     * can have alternate color space, the processAllColorSpace is called to check this alternate color space. (There
     * are no restrictions on the Alternate Color space)
     * 
     * @param colorSpace
     *            the color space object to check.
     */
    protected void processDeviceNColorSpace(PDColorSpace colorSpace)
    {
        PDDeviceN deviceN = (PDDeviceN) colorSpace;
        try
        {
            if (iccpw == null)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                        "DestOutputProfile is missing"));
                return;
            }

            COSBase cosAlt = ((COSArray)colorSpace.getCOSObject()).getObject(2);
            PDColorSpace altColor = PDColorSpace.create(cosAlt);
            if (altColor != null)
            {
                processAllColorSpace(altColor);
            }

            int numberOfColorants = 0;
            PDDeviceNAttributes attr = deviceN.getAttributes();
            if (attr != null)
            {
                final Map<String, PDSeparation> colorants = attr.getColorants();
                numberOfColorants = colorants.size();
                for (PDSeparation col : colorants.values())
                {
                    if (col != null)
                    {
                        processAllColorSpace(col);
                    }
                }
                PDDeviceNProcess process = attr.getProcess();
                if (process != null)
                {
                    processAllColorSpace(process.getColorSpace());
                }
            }
            int numberOfComponents = deviceN.getNumberOfComponents();
            if (numberOfColorants > MAX_DEVICE_N_LIMIT || numberOfComponents > MAX_DEVICE_N_LIMIT)
            {
                context.addValidationError(new ValidationError(
                        ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN,
                        "DeviceN has too many tint components or colorants"));
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                    "Unable to read DeviceN color space : " + e.getMessage(), e));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is Indexed. Because this kind of ColorSpace
     * can have a Base color space, the processAllColorSpace is called to check this base color space. (Indexed and
     * Pattern can't be a Base color space)
     * 
     * @param colorSpace
     *            the color space object to check.
     */
    protected void processIndexedColorSpace(PDColorSpace colorSpace)
    {
        PDIndexed indexed = (PDIndexed) colorSpace;
        PDColorSpace based = indexed.getBaseColorSpace();
        ColorSpaces cs = ColorSpaces.valueOf(based.getName());
        if (cs == ColorSpaces.Indexed || cs == ColorSpaces.I)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED,
                    "Indexed color space can't be used as Base color space"));
            return;
        }
        if (cs == ColorSpaces.Pattern)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED,
                    "Pattern color space can't be used as Base color space"));
            return;
        }
        processAllColorSpace(based);
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is Separation. Because this kind of
     * ColorSpace can have an alternate color space, the processAllColorSpace is called to check this alternate color
     * space. (Indexed, Separation, DeviceN and Pattern can't be a Base color space)
     * 
     * @param colorSpace
     *            the color space object to check.
     */
    protected void processSeparationColorSpace(PDColorSpace colorSpace)
    {
        try
        {
            COSBase cosAlt = ((COSArray)colorSpace.getCOSObject()).getObject(2);
            PDColorSpace altCol = PDColorSpace.create(cosAlt);
            if (altCol != null)
            {
                ColorSpaces acs = ColorSpaces.valueOf(altCol.getName());
                switch (acs)
                {
                case Separation:
                case DeviceN:
                case Pattern:
                case Indexed:
                case I:
                    context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE, acs
                            .getLabel() + " color space can't be used as alternate color space"));
                    break;
                default:
                    processAllColorSpace(altCol);
                }
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                    "Unable to read Separation color space : " + e.getMessage(), e));
        }
    }

    /**
     * Look up in the closest PDResources objects if there are a default ColorSpace. If there are, check that is a
     * authorized ColorSpace.
     * 
     * @param colorSpace
     * @return true if the default colorspace is a right one, false otherwise.
     */
    protected boolean processDefaultColorSpace(PDColorSpace colorSpace)
    {
        boolean result = false;

        // get default color space
        PreflightPath vPath = context.getValidationPath();
        PDResources resources = vPath.getClosestPathElement(PDResources.class);
        if (resources != null)
        {
            PDColorSpace defaultCS = null;

            try
            {
                if (colorSpace.getName().equals(ColorSpaces.DeviceCMYK.getLabel()) &&
                    resources.hasColorSpace(COSName.DEFAULT_CMYK))
                {
                    defaultCS = resources.getColorSpace(COSName.DEFAULT_CMYK);
                }
                else if (colorSpace.getName().equals(ColorSpaces.DeviceRGB.getLabel()) &&
                         resources.hasColorSpace(COSName.DEFAULT_RGB))
                {
                    defaultCS = resources.getColorSpace(COSName.DEFAULT_RGB);
                }
                else if (colorSpace.getName().equals(ColorSpaces.DeviceGray.getLabel()) &&
                         resources.hasColorSpace(COSName.DEFAULT_GRAY))
                {
                    defaultCS = resources.getColorSpace(COSName.DEFAULT_GRAY);
                }
            }
            catch (IOException e)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                        "Unable to read default color space : " + e.getMessage(), e));
            }

            if (defaultCS != null)
            {
                // defaultCS is valid if the number of errors hasn't changed
                int nbOfErrors = context.getDocument().getResult().getErrorsList().size();
                processAllColorSpace(defaultCS);
                int newNbOfErrors = context.getDocument().getResult().getErrorsList().size();
                result = (nbOfErrors == newNbOfErrors);
            }

        }

        return result;
    }

    private boolean validateICCProfileVersion(ICC_Profile iccp)
    {
        PreflightConfiguration config = context.getConfig();

        // check the ICC Profile version (6.2.2)
        if (iccp.getMajorVersion() == 2)
        {
            if (iccp.getMinorVersion() > 0x40)
            {
                // in PDF 1.4, max version is 02h.40h (meaning V 3.5)
                // see the ICCProfile specification (ICC.1:1998-09)page 13 - §6.1.3 :
                // The current profile version number is "2.4.0" (encoded as 02400000h")
                ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT,
                        "Invalid version of the ICCProfile");
                error.setWarning(config.isLazyValidation());
                context.addValidationError(error);
                return false;
            }
            // else OK
        }
        else if (iccp.getMajorVersion() > 2)
        {
            // in PDF 1.4, max version is 02h.40h (meaning V 3.5)
            // see the ICCProfile specification (ICC.1:1998-09)page 13 - §6.1.3 :
            // The current profile version number is "2.4.0" (encoded as 02400000h"
            ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT,
                    "Invalid version of the ICCProfile");
            error.setWarning(config.isLazyValidation());
            context.addValidationError(error);
            return false;
        }
        // else seems less than 2, so correct
        return true;
    }

    private boolean validateICCProfileNEntry(COSStream stream, ICC_Profile iccp)
            throws IOException
    {
        COSDictionary streamDict = (COSDictionary) stream.getCOSObject();
        if (!streamDict.containsKey(COSName.N))
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile is mandatory"));
            return false;
        }
        COSBase nValue = streamDict.getItem(COSName.N);
        if (!(nValue instanceof COSNumber))
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile must be a number, but is " + nValue));
            return false;
        }
        int nNumberValue = ((COSNumber) nValue).intValue();
        if (nNumberValue != 1 && nNumberValue != 3 && nNumberValue != 4)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile must be 1, 3 or 4, but is " + nNumberValue));
            return false;
        }
        if (iccp.getNumComponents() != nNumberValue)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile is " + nNumberValue + " but the ICC profile has " + iccp.getNumComponents() + " components"));
            return false;
        }
        return true;
    }

    private void validateICCProfileAlternateEntry(PDICCBased iccBased) throws IOException
    {
        PDColorSpace altCS = iccBased.getAlternateColorSpace();
        if (altCS != null && altCS.getNumberOfComponents() != iccBased.getNumberOfComponents())
        {
            // https://github.com/veraPDF/veraPDF-library/issues/773
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile is different (" + iccBased.getNumberOfComponents()
                    + ") than alternate entry colorspace component count ("
                    + altCS.getNumberOfComponents() + ")"));
        }
    }
}
