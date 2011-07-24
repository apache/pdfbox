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

package org.apache.padaf.preflight.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.contentstream.ContentStreamException;
import org.apache.padaf.preflight.contentstream.StubOperator;
import org.apache.padaf.preflight.graphics.ICCProfileWrapper;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelper;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory;
import org.apache.padaf.preflight.graphics.color.ColorSpaces;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.operator.BeginText;
import org.apache.pdfbox.util.operator.Concatenate;
import org.apache.pdfbox.util.operator.EndText;
import org.apache.pdfbox.util.operator.GRestore;
import org.apache.pdfbox.util.operator.GSave;
import org.apache.pdfbox.util.operator.Invoke;
import org.apache.pdfbox.util.operator.MoveText;
import org.apache.pdfbox.util.operator.MoveTextSetLeading;
import org.apache.pdfbox.util.operator.NextLine;
import org.apache.pdfbox.util.operator.OperatorProcessor;
import org.apache.pdfbox.util.operator.SetCharSpacing;
import org.apache.pdfbox.util.operator.SetHorizontalTextScaling;
import org.apache.pdfbox.util.operator.SetLineCapStyle;
import org.apache.pdfbox.util.operator.SetLineDashPattern;
import org.apache.pdfbox.util.operator.SetLineJoinStyle;
import org.apache.pdfbox.util.operator.SetLineWidth;
import org.apache.pdfbox.util.operator.SetMatrix;
import org.apache.pdfbox.util.operator.SetNonStrokingCMYKColor;
import org.apache.pdfbox.util.operator.SetNonStrokingColor;
import org.apache.pdfbox.util.operator.SetNonStrokingColorSpace;
import org.apache.pdfbox.util.operator.SetNonStrokingRGBColor;
import org.apache.pdfbox.util.operator.SetStrokingCMYKColor;
import org.apache.pdfbox.util.operator.SetStrokingColor;
import org.apache.pdfbox.util.operator.SetStrokingColorSpace;
import org.apache.pdfbox.util.operator.SetStrokingRGBColor;
import org.apache.pdfbox.util.operator.SetTextFont;
import org.apache.pdfbox.util.operator.SetTextLeading;
import org.apache.pdfbox.util.operator.SetTextRenderingMode;
import org.apache.pdfbox.util.operator.SetTextRise;
import org.apache.pdfbox.util.operator.SetWordSpacing;

import static org.apache.padaf.preflight.ValidationConstants.*;
/**
 * This class inherits from org.apache.pdfbox.util.PDFStreamEngine to allow the
 * validation of specific rules in ContentStream.
 */
public abstract class ContentStreamEngine extends PDFStreamEngine {

	protected DocumentHandler documentHandler = null;

	protected Map<String,OperatorProcessor> contentStreamEngineOperators = new HashMap<String,OperatorProcessor>();

	public ContentStreamEngine(DocumentHandler _handler) {
		this.documentHandler = _handler;

		// ---- Graphics operators
		registerOperatorProcessor("w", new SetLineWidth());
		registerOperatorProcessor("cm", new Concatenate());

		registerOperatorProcessor("CS", new SetStrokingColorSpace());
		registerOperatorProcessor("cs", new SetNonStrokingColorSpace());
		registerOperatorProcessor("d", new SetLineDashPattern());
		registerOperatorProcessor("Do", new Invoke());

		registerOperatorProcessor("j", new SetLineJoinStyle());
		registerOperatorProcessor("J", new SetLineCapStyle());
		registerOperatorProcessor("K", new SetStrokingCMYKColor());
		registerOperatorProcessor("k", new SetNonStrokingCMYKColor());

		registerOperatorProcessor("rg", new SetNonStrokingRGBColor());
		registerOperatorProcessor("RG", new SetStrokingRGBColor());

		registerOperatorProcessor("SC", new SetStrokingColor());
		registerOperatorProcessor("SCN", new SetStrokingColor());
		registerOperatorProcessor("sc", new SetNonStrokingColor());
		registerOperatorProcessor("scn", new SetNonStrokingColor());

		// ---- Graphics state
		registerOperatorProcessor("Q", new GRestore());
		registerOperatorProcessor("q", new GSave());

		// ---- Text operators
		registerOperatorProcessor("BT", new BeginText());
		registerOperatorProcessor("ET", new EndText());
		registerOperatorProcessor("Tf", new SetTextFont());
		registerOperatorProcessor("Tr", new SetTextRenderingMode());
		registerOperatorProcessor("Tm", new SetMatrix());
		registerOperatorProcessor("Td", new MoveText());
		registerOperatorProcessor("T*", new NextLine());
		registerOperatorProcessor("TD", new MoveTextSetLeading());
		registerOperatorProcessor("Tc", new SetCharSpacing());
		registerOperatorProcessor("TL", new SetTextLeading());
		registerOperatorProcessor("Ts", new SetTextRise());
		registerOperatorProcessor("Tw", new SetWordSpacing());
		registerOperatorProcessor("Tz", new SetHorizontalTextScaling());

		// ---- Do not use the PDFBox Operator, because of the PageDrawer class cast
		// Or because the Operator doesn't exist
		StubOperator so = new StubOperator();
		registerOperatorProcessor("l", so);
		registerOperatorProcessor("re", so);
		registerOperatorProcessor("c", so);
		registerOperatorProcessor("y", so);
		registerOperatorProcessor("v", so);
		registerOperatorProcessor("n", so);
		registerOperatorProcessor("BI", so);
		registerOperatorProcessor("EI", so);
		registerOperatorProcessor("m", so);
		registerOperatorProcessor("W*", so);
		registerOperatorProcessor("W", so);
		registerOperatorProcessor("h", so);

		registerOperatorProcessor("Tj", so);
		registerOperatorProcessor("TJ", so);
		registerOperatorProcessor("'", so);
		registerOperatorProcessor("\"", so);

		registerOperatorProcessor("b", so);
		registerOperatorProcessor("B", so);
		registerOperatorProcessor("b*", so);
		registerOperatorProcessor("B*", so);

		registerOperatorProcessor("BDC", so);
		registerOperatorProcessor("BMC", so);
		registerOperatorProcessor("DP", so);
		registerOperatorProcessor("EMC", so);

		registerOperatorProcessor("d0", so);
		registerOperatorProcessor("d1", so);

		registerOperatorProcessor("f", so);
		registerOperatorProcessor("F", so);
		registerOperatorProcessor("f*", so);

		registerOperatorProcessor("g", so);
		registerOperatorProcessor("G", so);

		registerOperatorProcessor("M", so);
		registerOperatorProcessor("MP", so);

		registerOperatorProcessor("gs", so);
		registerOperatorProcessor("h", so);
		registerOperatorProcessor("i", so);

		registerOperatorProcessor("ri", so);
		registerOperatorProcessor("s", so);
		registerOperatorProcessor("S", so);
		registerOperatorProcessor("sh", so);
	}

	public final void registerOperatorProcessor( String operator, OperatorProcessor op )
	{
		super.registerOperatorProcessor(operator, op);
		contentStreamEngineOperators.put( operator, op );
	}


	/**
	 * Check operands of the "ri" operator. Operands must exist in the
	 * RenderingIntent list. (net.awl.edoc.pdfa.validation.utils.RenderingIntents)
	 * 
	 * @param operator
	 *          the "ri" operator
	 * @param arguments
	 *          the "ri" operands
	 * @throws ContentStreamException
	 *           ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY if the operand is invalid
	 */
	protected void validRenderingIntent(PDFOperator operator, List arguments)
	throws ContentStreamException {
		if ("ri".equals(operator.getOperation())) {
			if (!RenderingIntents.contains(arguments.get(0))) {
				throwContentStreamException("Unexpected value '" + arguments.get(0)
						+ "' for ri operand. ", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
			}
		}
	}

	/**
	 * Valid the number of graphic states if the operator is the Save Graphic state operator ("q")
	 * @param operator
	 * @throws ContentStreamException
	 */
	protected void validNumberOfGraphicStates(PDFOperator operator) throws ContentStreamException  {
		if ("q".equals(operator.getOperation())) {
			int numberOfGraphicStates = this.getGraphicsStack().size();
			if (numberOfGraphicStates > MAX_GRAPHIC_STATES) {
				throwContentStreamException("Too many graphic states", ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES);
			}
		}
	}

	/**
	 * Throw a ContentStreamException if the LZW filter is used in a InlinedImage.
	 * 
	 * @param operator
	 *          the InlinedImage object (BI to EI)
	 * @throws ContentStreamException
	 */
	protected void validImageFilter(PDFOperator operator)
	throws ContentStreamException {
		COSDictionary dict = operator.getImageParameters().getDictionary();
		// ---- Search a Filter declaration in the InlinedImage dictionary.
		// ---- The LZWDecode Filter is forbidden.
		String filter = dict.getNameAsString(STREAM_DICTIONARY_KEY_F);
		if (filter == null) {
			filter = dict.getNameAsString(STREAM_DICTIONARY_KEY_FILTER);
		}

		String errorCode = FilterHelper.isAuthorizedFilter(filter);
		if (errorCode != null) {
			// --- LZW is forbidden.
			if ( ERROR_SYNTAX_STREAM_INVALID_FILTER.equals(errorCode) ) {
				throwContentStreamException("LZW filter can't be used in a PDF/A File", ERROR_SYNTAX_STREAM_INVALID_FILTER);
			} else {
				throwContentStreamException("This filter isn't defined in the PDF Reference Third Edition.", ERROR_SYNTAX_STREAM_UNDEFINED_FILTER);
			}
		}
	}

	/**
	 * This method validates if the ColorSpace used by the InlinedImage is
	 * consistent with the color space defined in OutputIntent dictionaries.
	 * 
	 * @param operator
	 *          the InlinedImage object (BI to EI)
	 * @throws ContentStreamException
	 */
	protected void validImageColorSpace(PDFOperator operator)
	throws ContentStreamException, IOException {
		COSDictionary dict = operator.getImageParameters().getDictionary();

		COSDocument doc = this.documentHandler.getDocument().getDocument();
		COSBase csInlinedBase = dict.getItem(COSName
				.getPDFName(STREAM_DICTIONARY_KEY_COLOR_SPACE));

		ColorSpaceHelper csHelper = null;
		if (csInlinedBase != null) {

			if (COSUtils.isString(csInlinedBase, doc)) {
				// ---- In InlinedImage only DeviceGray/RGB/CMYK and restricted Indexed
				// color spaces
				// are allowed.
				String colorSpace = COSUtils.getAsString(csInlinedBase, doc);
				ColorSpaces cs = null;

				try {
					cs = ColorSpaces.valueOf(colorSpace);
				} catch (IllegalArgumentException e) {
					// ---- The color space is unknown.
					// ---- Try to access the resources dictionary, the color space can be
					// a reference.
					PDColorSpace pdCS = (PDColorSpace) this.getColorSpaces().get(
							colorSpace);
					if (pdCS != null) {
						cs = ColorSpaces.valueOf(pdCS.getName());
						csHelper = ColorSpaceHelperFactory.getColorSpaceHelper(pdCS,
								documentHandler, ColorSpaceRestriction.ONLY_DEVICE);
					}
				}

				if (cs == null) {
					throwContentStreamException("The ColorSpace is unknown",
							ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
				}
			}

			if (csHelper == null) {
				csHelper = ColorSpaceHelperFactory.getColorSpaceHelper(csInlinedBase,
						documentHandler, ColorSpaceRestriction.ONLY_DEVICE);
			}
			List<ValidationError> errors = new ArrayList<ValidationError>();
			try {
				if (!csHelper.validate(errors)) {
					ValidationError ve = errors.get(0);
					throwContentStreamException(ve.getDetails(), ve.getErrorCode());
				}
			} catch (ValidationException e) {
				throw new IOException(e.getMessage()); 
			}
		}
	}

	/**
	 * This method validates if the ColorOperator can be used with the color space
	 * defined in OutputIntent dictionaries.
	 * 
	 * @param operator
	 *          the color operator
	 * @throws ContentStreamException
	 */
	protected void checkColorOperators(String operation)
	throws ContentStreamException {
		if ("rg".equals(operation) || "RG".equals(operation)) {
			ICCProfileWrapper iccpw = documentHandler.getIccProfileWrapper();
			if (iccpw == null || !iccpw.isRGBColorSpace()) {
				throwContentStreamException("The operator \"" + operation
						+ "\" can't be used with CMYK Profile",
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB);
			}
		}

		if ("k".equals(operation) || "K".equals(operation)) {
			ICCProfileWrapper iccpw = documentHandler.getIccProfileWrapper();
			if (iccpw == null || !iccpw.isCMYKColorSpace()) {
				throwContentStreamException("The operator \"" + operation
						+ "\" can't be used with RGB Profile",
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK);
			}
		}

		if ("g".equals(operation) || "G".equals(operation)) {
			ICCProfileWrapper iccpw = documentHandler.getIccProfileWrapper();
			if (iccpw == null) {
				// ---- Gray is possible with RGB and CMYK color space
				throwContentStreamException("The operator \"" + operation
						+ "\" can't be used without Color Profile",
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING);
			}
		}

		if ("f".equals(operation) || "F".equals(operation)
				|| "f*".equals(operation) || "B".equals(operation)
				|| "B*".equals(operation) || "b".equals(operation)
				|| "b*".equals(operation)) {
			ICCProfileWrapper iccpw = documentHandler.getIccProfileWrapper();
			if (iccpw == null) {
				// ---- The default fill color needs an OutputIntent
				throwContentStreamException("The operator \"" + operation
						+ "\" can't be used without Color Profile",
						ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING);
			}
		}
	}

	/**
	 * This method validates if the ColorSpace used as operand is consistent with
	 * the color space defined in OutputIntent dictionaries.
	 * 
	 * @param operator
	 * @param arguments
	 * @throws IOException
	 */
	protected void checkSetColorSpaceOperators(PDFOperator operator,
			List<?> arguments) throws IOException {
		if (!("CS".equals(operator.getOperation()) || "cs".equals(operator
				.getOperation()))) {
			return;
		}

		String colorSpaceName = null;
		if (arguments.get(0) instanceof String) {
			colorSpaceName = (String) arguments.get(0);
		} else if (arguments.get(0) instanceof COSString) {
			colorSpaceName = ((COSString) arguments.get(0)).toString();
		} else if (arguments.get(0) instanceof COSName) {
			colorSpaceName = ((COSName) arguments.get(0)).getName();
		} else {
			throwContentStreamException("The operand doesn't have the expected type",
					ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
		}

		ColorSpaceHelper csHelper = null;
		ColorSpaces cs = null;
		try {
			cs = ColorSpaces.valueOf(colorSpaceName);
		} catch (IllegalArgumentException e) {
			// ---- The color space is unknown.
			// ---- Try to access the resources dictionary, the color space can be a
			// reference.
			PDColorSpace pdCS = (PDColorSpace) this.getColorSpaces().get(
					colorSpaceName);
			if (pdCS != null) {
				cs = ColorSpaces.valueOf(pdCS.getName());
				csHelper = ColorSpaceHelperFactory.getColorSpaceHelper(pdCS,
						documentHandler, ColorSpaceRestriction.NO_RESTRICTION);
			}
		}

		if (cs == null) {
			throwContentStreamException("The ColorSpace is unknown",
					ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
		}

		if (csHelper == null) {
			csHelper = ColorSpaceHelperFactory.getColorSpaceHelper(COSName
					.getPDFName(colorSpaceName), documentHandler,
					ColorSpaceRestriction.NO_RESTRICTION);
		}

		List<ValidationError> errors = new ArrayList<ValidationError>();
		try {
			if (!csHelper.validate(errors)) {
				ValidationError ve = errors.get(0);
				throwContentStreamException(ve.getDetails(), ve.getErrorCode());
			}
		} catch (ValidationException e) {
			//      throw new IOException(e.getMessage(), e); java 6
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Build a ContentStreamException using the given parameters
	 * 
	 * @param msg
	 *          exception details
	 * @param errorCode
	 *          the error code.
	 * @throws ContentStreamException
	 */
	protected void throwContentStreamException(String msg, String errorCode)
	throws ContentStreamException {
		ContentStreamException cex = new ContentStreamException(msg);
		cex.setValidationError(errorCode);
		throw cex;
	}
}
