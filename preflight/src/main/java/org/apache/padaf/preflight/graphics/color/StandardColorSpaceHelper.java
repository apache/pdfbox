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

package org.apache.padaf.preflight.graphics.color;

import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE;
import static org.apache.padaf.preflight.ValidationConstants.MAX_DEVICE_N_LIMIT;

import java.io.IOException;
import java.util.List;
import java.util.Map;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.graphics.ICCProfileWrapper;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;

/**
 * This class doesn't define restrictions on ColorSpace. It checks only the
 * consistency of the Color space with the DestOutputIntent.
 */
public class StandardColorSpaceHelper implements ColorSpaceHelper {
	/**
	 * The color space object to check, this object is used to instantiate the
	 * pdcs object.
	 */
	protected COSBase csObject = null;
	/**
	 * The document handler which contains useful information to process the
	 * validation.
	 */
	protected DocumentHandler handler = null;
	/**
	 * The ICCProfile contained in the DestOutputIntent
	 */
	protected ICCProfileWrapper iccpw = null;
	/**
	 * High level object which represents the colors space to check.
	 */
	protected PDColorSpace pdcs = null;

	StandardColorSpaceHelper(COSBase _csObject, DocumentHandler _handler) {
		this.csObject = _csObject;
		this.handler = _handler;
		this.iccpw = this.handler.getIccProfileWrapper();
	}

	StandardColorSpaceHelper(PDColorSpace _cs, DocumentHandler _handler) {
		this.handler = _handler;
		this.pdcs = _cs;
		this.iccpw = this.handler.getIccProfileWrapper();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.graphics.color.ColorSpaceHelper#validate(java
	 * .util.List)
	 */
	public final boolean validate(List<ValidationError> result) throws ValidationException {
		// ---- Create a PDFBox ColorSpace object
		if (pdcs == null && csObject != null) {
			try {
				if (csObject instanceof COSObject) {
					pdcs = PDColorSpaceFactory.createColorSpace(((COSObject)csObject).getObject());
				} else {
					pdcs = PDColorSpaceFactory.createColorSpace(csObject);
				}
			} catch (IOException e) {
				throw new ValidationException("Unable to create a PDColorSpace : "
						+ e.getMessage(), e);
			}
		}

		if ( pdcs == null ) {
			throw new ValidationException(
					"Unable to create a PDColorSpace with the value null");
		}

		return processAllColorSpace(pdcs, result);
	}

	/**
	 * Method called by the validate method. According to the ColorSpace, a
	 * specific ColorSpace method is called.
	 * 
	 * @param pdcs
	 *          the color space object to check.
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the validation succeed, false otherwise.
	 */
	protected final boolean processAllColorSpace(PDColorSpace pdcs,
			List<ValidationError> result) {
		ColorSpaces cs = ColorSpaces.valueOf(pdcs.getName());
		switch (cs) {
		case DeviceRGB:
		case DeviceRGB_SHORT:
			return processRGBColorSpace(result);

		case DeviceCMYK:
		case DeviceCMYK_SHORT:
			return processCYMKColorSpace(result);

		case CalRGB:
		case CalGray:
		case Lab:
			return processCalibratedColorSpace(result);

		case DeviceGray:
		case DeviceGray_SHORT:
			return processGrayColorSpace(result);

		case ICCBased:
			return processICCBasedColorSpace(pdcs, result);

		case DeviceN:
			return processDeviceNColorSpace(pdcs, result);

		case Indexed:
		case Indexed_SHORT:
			return processIndexedColorSpace(pdcs, result);

		case Separation:
			return processSeparationColorSpace(pdcs, result);

		case Pattern:
			return processPatternColorSpace(result);

		default:
			result
			.add(new ValidationError(ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE, cs.getLabel() + " is unknown as ColorSpace"));
			return false;
		}
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * DeviceRGB.
	 * 
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processRGBColorSpace(List<ValidationError> result) {
		// ---- ICCProfile must contain a RGB Color Space
		if (iccpw == null || !iccpw.isRGBColorSpace()) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB, "DestOutputProfile is missing"));
			return false;
		}
		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * DeviceCYMK.
	 * 
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processCYMKColorSpace(List<ValidationError> result) {
		// ---- ICCProfile must contain a CYMK Color Space
		if (iccpw == null || !iccpw.isCMYKColorSpace()) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK, "DestOutputProfile is missing"));
			return false;
		}
		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is a
	 * Pattern.
	 * 
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processPatternColorSpace(List<ValidationError> result) {
		if (iccpw == null) {
			result
			.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
			return false;
		}
		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * DeviceGray.
	 * 
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processGrayColorSpace(List<ValidationError> result) {
		// ---- OutputIntent is mandatory
		if (iccpw == null) {
			result
			.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
			return false;
		}
		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is a
	 * Clibrated Color (CalGary, CalRGB, Lab).
	 * 
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processCalibratedColorSpace(List<ValidationError> result) {
		// ---- OutputIntent isn't mandatory
		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is a
	 * ICCBased color space. Because this kind of ColorSpace can have alternate
	 * color space, the processAllColorSpace is called to check this alternate
	 * color space. (Pattern is forbidden as Alternate Color Space)
	 * 
	 * @param pdcs
	 *          the color space object to check.
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processICCBasedColorSpace(PDColorSpace pdcs,
			List<ValidationError> result) {
		PDICCBased iccBased = (PDICCBased) pdcs;
		try {
			if (iccpw == null) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
				return false;
			}

			List<PDColorSpace> altCs = iccBased.getAlternateColorSpaces();
			for (PDColorSpace altpdcs : altCs) {
				if (altpdcs != null) {

					ColorSpaces altCsId = ColorSpaces.valueOf(altpdcs.getName());
					if (altCsId == ColorSpaces.Pattern) {
						result.add(new ValidationError(
								ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN, "Pattern is forbidden as AlternateColorSpace of a ICCBased"));
						return false;
					}

					if (!processAllColorSpace(altpdcs, result)) {
						return false;
					}
				}
			}
		} catch (IOException e) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE, "Unable to read ICCBase color space : " + e.getMessage()));
			return false;
		}

		return true;
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * DeviceN. Because this kind of ColorSpace can have alternate color space,
	 * the processAllColorSpace is called to check this alternate color space.
	 * (There are no restrictions on the Alternate Color space)
	 * 
	 * @param pdcs
	 *          the color space object to check.
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processDeviceNColorSpace(PDColorSpace pdcs,
			List<ValidationError> result) {
		PDDeviceN deviceN = (PDDeviceN) pdcs;
		try {
			if (iccpw == null) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
				return false;
			}

			PDColorSpace altColor = deviceN.getAlternateColorSpace();
			boolean res = true;
			if (altColor != null) {
				res = processAllColorSpace(altColor, result);
			}

			Map colorants = deviceN.getAttributes().getColorants();
			int numberOfColorants = 0;
			if (colorants != null) {
				numberOfColorants = colorants.size();
				for (Object col : colorants.values()) {
					if (col != null) {
						res = res && processAllColorSpace((PDColorSpace) col, result);
					}
				}
			}

			int numberOfComponents = deviceN.getNumberOfComponents();
			if (numberOfColorants > MAX_DEVICE_N_LIMIT || numberOfComponents > MAX_DEVICE_N_LIMIT ) {
				result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN, "DeviceN has too many tint components or colorants"));  
				res = false;
			}
			return res;
		} catch (IOException e) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE, "Unable to read DeviceN color space : " + e.getMessage()));
			return false;
		}
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * Indexed. Because this kind of ColorSpace can have a Base color space, the
	 * processAllColorSpace is called to check this base color space. (Indexed and
	 * Pattern can't be a Base color space)
	 * 
	 * @param pdcs
	 *          the color space object to check.
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processIndexedColorSpace(PDColorSpace pdcs,
			List<ValidationError> result) {
		PDIndexed indexed = (PDIndexed) pdcs;
		try {
			if (iccpw == null) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
				return false;
			}

			PDColorSpace based = indexed.getBaseColorSpace();
			ColorSpaces cs = ColorSpaces.valueOf(based.getName());
			if (cs == ColorSpaces.Indexed || cs == ColorSpaces.Indexed_SHORT) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED,"Indexed color space can't be used as Base color space"));
				return false;
			}
			if (cs == ColorSpaces.Pattern) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED,"Pattern color space can't be used as Base color space"));
				return false;
			}
			return processAllColorSpace(based, result);
		} catch (IOException e) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE, "Unable to read Indexed color space : " + e.getMessage()));
			return false;
		}
	}

	/**
	 * Method called by the processAllColorSpace if the ColorSpace to check is
	 * Separation. Because this kind of ColorSpace can have an alternate color
	 * space, the processAllColorSpace is called to check this alternate color
	 * space. (Indexed, Separation, DeviceN and Pattern can't be a Base color
	 * space)
	 * 
	 * @param pdcs
	 *          the color space object to check.
	 * @param result
	 *          the list of error to update if the validation fails.
	 * @return true if the color space is valid, false otherwise.
	 */
	protected boolean processSeparationColorSpace(PDColorSpace pdcs,
			List<ValidationError> result) {
		PDSeparation separation = (PDSeparation) pdcs;
		try {
			if (iccpw == null) {
				result.add(new ValidationError(
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING,"DestOutputProfile is missing"));
				return false;
			}

			PDColorSpace altCol = separation.getAlternateColorSpace();
			if (altCol != null) {
				ColorSpaces acs = ColorSpaces.valueOf(altCol.getName());
				switch (acs) {
				case Separation:
				case DeviceN:
				case Pattern:
				case Indexed:
				case Indexed_SHORT:
					result.add(new ValidationError(
							ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE, acs.getLabel() + " color space can't be used as alternate color space"));
					return false;
				default:
					return processAllColorSpace(altCol, result);
				}
			}

			return true;
		} catch (IOException e) {
			result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE, "Unable to read Separation color space : " + e.getMessage()));
			return false;
		}
	}
}
