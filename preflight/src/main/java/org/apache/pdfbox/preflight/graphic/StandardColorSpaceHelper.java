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
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_DEVICE_N_LIMIT;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceNAttributes;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

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
     * @see net.awl.edoc.pdfa.validation.graphics.color.ColorSpaceHelper#validate(java .util.List)
     */
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
     * @param pdcs
     *            the color space object to check.
     * @param result
     *            the list of error to update if the validation fails.
     * @return true if the validation succeed, false otherwise.
     */
    protected final void processAllColorSpace(PDColorSpace pdcs)
    {
        ColorSpaces cs = ColorSpaces.valueOf(pdcs.getName());

        switch (cs)
        {
        case DeviceRGB:
        case DeviceRGB_SHORT:
            processRGBColorSpace(pdcs);
            break;
        case DeviceCMYK:
        case DeviceCMYK_SHORT:
            processCYMKColorSpace(pdcs);
            break;
        case CalRGB:
        case CalGray:
        case Lab:
            processCalibratedColorSpace(pdcs);
            break;
        case DeviceGray:
        case DeviceGray_SHORT:
            processGrayColorSpace(pdcs);
            break;
        case ICCBased:
            processICCBasedColorSpace(pdcs);
            break;
        case DeviceN:
            processDeviceNColorSpace(pdcs);
            break;
        case Indexed:
        case Indexed_SHORT:
            processIndexedColorSpace(pdcs);
            break;
        case Separation:
            processSeparationColorSpace(pdcs);
            break;
        case Pattern:
            processPatternColorSpace(pdcs);
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
    protected void processRGBColorSpace(PDColorSpace pdcs)
    {
        if (!processDefaultColorSpace(pdcs))
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
    protected void processCYMKColorSpace(PDColorSpace pdcs)
    {
        if (!processDefaultColorSpace(pdcs))
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
     */
    protected void processPatternColorSpace(PDColorSpace pdcs)
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
    protected void processGrayColorSpace(PDColorSpace pdcs)
    {
        if (!processDefaultColorSpace(pdcs) && iccpw == null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                    "DestOutputProfile is missing"));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is a Clibrated Color (CalGary, CalRGB, Lab).
     * 
     */
    protected void processCalibratedColorSpace(PDColorSpace pdcs)
    {
        // ---- OutputIntent isn't mandatory
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is a ICCBased color space. Because this kind
     * of ColorSpace can have alternate color space, the processAllColorSpace is called to check this alternate color
     * space. (Pattern is forbidden as Alternate Color Space)
     * 
     * @param pdcs
     *            the color space object to check.
     */
    protected void processICCBasedColorSpace(PDColorSpace pdcs)
    {
        PDICCBased iccBased = (PDICCBased) pdcs;
        try
        {
            ICC_Profile iccp = ICC_Profile.getInstance(iccBased.getPDStream().getByteArray());
            if (iccp == null)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED,
                        "Unable to read ICCBase color space "));
                return;
            }
            List<PDColorSpace> altCs = iccBased.getAlternateColorSpaces();
            for (PDColorSpace altpdcs : altCs)
            {
                if (altpdcs != null)
                {

                    ColorSpaces altCsId = ColorSpaces.valueOf(altpdcs.getName());
                    if (altCsId == ColorSpaces.Pattern)
                    {
                        context.addValidationError(new ValidationError(
                                ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN,
                                "Pattern is forbidden as AlternateColorSpace of a ICCBased"));
                        return;
                    }

                    /*
                     * According to the ISO-19005-1:2005
                     * 
                     * A conforming reader shall render ICCBased colour spaces as specified by the ICC specification,
                     * and shall not use the Alternate colour space specified in an ICC profile stream dictionary
                     * 
                     * We don't check the alternate ColorSpaces
                     */
                }
            }
        }        
        catch (IllegalArgumentException e)
        {
            // this is not a ICC_Profile
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED,
                    "ICCBase color space is invalid. Caused By: " + e.getMessage()));
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                    "Unable to read ICCBase color space. Caused by : " + e.getMessage()));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is DeviceN. Because this kind of ColorSpace
     * can have alternate color space, the processAllColorSpace is called to check this alternate color space. (There
     * are no restrictions on the Alternate Color space)
     * 
     * @param pdcs
     *            the color space object to check.
     */
    protected void processDeviceNColorSpace(PDColorSpace pdcs)
    {
        PDDeviceN deviceN = (PDDeviceN) pdcs;
        try
        {
            if (iccpw == null)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,
                        "DestOutputProfile is missing"));
                return;
            }

            PDColorSpace altColor = deviceN.getAlternateColorSpace();
            if (altColor != null)
            {
                processAllColorSpace(altColor);
            }

            int numberOfColorants = 0;
            PDDeviceNAttributes attr = deviceN.getAttributes();
            if (attr != null)
            {
                Map colorants = attr.getColorants();
                if (colorants != null)
                {
                    numberOfColorants = colorants.size();
                    for (Object col : colorants.values())
                    {
                        if (col != null)
                        {
                            processAllColorSpace((PDColorSpace) col);
                        }
                    }
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
                    "Unable to read DeviceN color space : " + e.getMessage()));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is Indexed. Because this kind of ColorSpace
     * can have a Base color space, the processAllColorSpace is called to check this base color space. (Indexed and
     * Pattern can't be a Base color space)
     * 
     * @param pdcs
     *            the color space object to check.
     */
    protected void processIndexedColorSpace(PDColorSpace pdcs)
    {
        PDIndexed indexed = (PDIndexed) pdcs;
        try
        {
            PDColorSpace based = indexed.getBaseColorSpace();
            ColorSpaces cs = ColorSpaces.valueOf(based.getName());
            if (cs == ColorSpaces.Indexed || cs == ColorSpaces.Indexed_SHORT)
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
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE,
                    "Unable to read Indexed color space : " + e.getMessage()));
        }
    }

    /**
     * Method called by the processAllColorSpace if the ColorSpace to check is Separation. Because this kind of
     * ColorSpace can have an alternate color space, the processAllColorSpace is called to check this alternate color
     * space. (Indexed, Separation, DeviceN and Pattern can't be a Base color space)
     * 
     * @param pdcs
     *            the color space object to check.
     */
    protected void processSeparationColorSpace(PDColorSpace pdcs)
    {
        PDSeparation separation = (PDSeparation) pdcs;
        try
        {

            PDColorSpace altCol = separation.getAlternateColorSpace();
            if (altCol != null)
            {
                ColorSpaces acs = ColorSpaces.valueOf(altCol.getName());
                switch (acs)
                {
                case Separation:
                case DeviceN:
                case Pattern:
                case Indexed:
                case Indexed_SHORT:
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
                    "Unable to read Separation color space : " + e.getMessage()));
        }
    }

    /**
     * Look up in the closest PDResources objects if there are a default ColorSpace. If there are, check that is a
     * authorized ColorSpace.
     * 
     * @param pdcs
     * @return true if the default colorspace is a right one, false otherwise.
     */
    protected boolean processDefaultColorSpace(PDColorSpace pdcs)
    {
        boolean result = false;

        // get default color space
        PreflightPath vPath = context.getValidationPath();
        PDResources resources = vPath.getClosestPathElement(PDResources.class);
        if (resources != null && resources.getColorSpaces() != null)
        {
            PDColorSpace defaultCS = null;

            Map<String, PDColorSpace> colorsSpaces = resources.getColorSpaces();
            if (pdcs.getName().equals(ColorSpaces.DeviceCMYK.getLabel()))
            {
                defaultCS = colorsSpaces.get("DefaultCMYK");
            }
            else if (pdcs.getName().equals(ColorSpaces.DeviceRGB.getLabel()))
            {
                defaultCS = colorsSpaces.get("DefaultRGB");
            }
            else if (pdcs.getName().equals(ColorSpaces.DeviceGray.getLabel()))
            {
                defaultCS = colorsSpaces.get("DefaultGray");
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
}
