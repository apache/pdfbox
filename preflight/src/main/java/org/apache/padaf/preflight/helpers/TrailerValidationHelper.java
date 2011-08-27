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

package org.apache.padaf.preflight.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.preflight.utils.PdfElementParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * @author eric
 * 
 */
public class TrailerValidationHelper extends AbstractValidationHelper {

  public TrailerValidationHelper(ValidatorConfig cfg)
  throws ValidationException {
	super(cfg);
  }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.helpers.AbstractValidationHelper#validate(
	 * net.awl.edoc.pdfa.validation.DocumentHandler)
	 */
	@Override
	public List<ValidationError> innerValidate(DocumentHandler handler)
	throws ValidationException {

		List<ValidationError> result = new ArrayList<ValidationError>(0);
		PDDocument pdfDoc = handler.getDocument();

		COSDictionary linearizedDict = isLinearizedPdf(pdfDoc);
		if (linearizedDict != null) {
			// it is a linearized PDF, check the linearized dictionary
			checkLinearizedDictionnary(linearizedDict, result);

			// if the pdf is a linearized pdf. the first trailer must be checked
			// and it must have the same ID than the last trailer.
			// According to the PDF version, trailers are available by the trailer key word (pdf <= 1.4)
			// or in the dictionary of the XRef stream ( PDF >= 1.5)
			String pdfVersion = pdfDoc.getDocument().getHeaderString();
			if ( pdfVersion != null && pdfVersion.matches("%PDF-1\\.[1-4]")) {
				checkTrailersForLinearizedPDF14(handler, result);
			} else {
				checkTrailersForLinearizedPDF15(handler, result);
			}

		} else {
			// If the PDF isn't a linearized one, only the last trailer must be checked
			checkMainTrailer(pdfDoc.getDocument(), pdfDoc.getDocument().getTrailer(), result);

		}

		return result;
	}

	/**
	 * Extracts and compares first and last trailers for PDF version between 1.1 and 1.4
	 * @param handler
	 * @param result
	 */
	protected void checkTrailersForLinearizedPDF14(DocumentHandler handler, List<ValidationError> result) {
		PDDocument pdfDoc = handler.getDocument();
		List<String> lTrailers = handler.getPdfExtractor().getAllTrailers();

		if (lTrailers.isEmpty()) {
			result.add(new ValidationResult.ValidationError(
					ValidationConstants.ERROR_SYNTAX_TRAILER,
			"There are no trailer in the PDF file"));
		} else {
			String firstTrailer = lTrailers.get(0);
			String lastTrailer = lTrailers.get(lTrailers.size() - 1);

			COSDictionary first = null;
			COSDictionary last = null;
			COSDocument cd = null;
			try {
				cd = new COSDocument();

				PdfElementParser parser1 = new PdfElementParser(cd, firstTrailer.getBytes());
				first = parser1.parseAsDictionary();

				PdfElementParser parser2 = new PdfElementParser(cd, lastTrailer.getBytes());
				last = parser2.parseAsDictionary();

				checkMainTrailer(pdfDoc.getDocument(), first, result);
				if (!compareIds(first, last, pdfDoc.getDocument())) {
					result.add(new ValidationResult.ValidationError(
							ValidationConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
					"ID is different in the first and the last trailer"));
				}

			} catch (IOException e) {
				result.add(new ValidationResult.ValidationError(
						ValidationConstants.ERROR_SYNTAX_TRAILER,
				"Unable to parse trailers of the linearized PDF"));
			} finally {
				COSUtils.closeDocumentQuietly(cd);
			}
		}
	}

	/**
	 * Accesses and compares First and Last trailers for a PDF version higher than 1.4.
	 * 
	 * @param handler
	 * @param result
	 */
	protected void checkTrailersForLinearizedPDF15(DocumentHandler handler, List<ValidationError> result) {
		PDDocument pdfDoc = handler.getDocument();
		try {
			COSDocument cosDocument = pdfDoc.getDocument();
			List<COSObject> xrefs = cosDocument.getObjectsByType(COSName.XREF);

			if (xrefs.isEmpty()) {
				// no XRef CosObject, may by this pdf file used the PDF 1.4 syntaxe
				checkTrailersForLinearizedPDF14(handler, result);

			} else {

				int min = Integer.MAX_VALUE;
				int max = Integer.MIN_VALUE;
				COSDictionary firstTrailer = null;
				COSDictionary lastTrailer = null;

				// Search First and Last trailers according to offset position.
				for(COSObject co : xrefs) {
					int offset = cosDocument.getXrefTable().get(new COSObjectKey(co));
					if (offset < min) {
						min = offset;
						firstTrailer = (COSDictionary)co.getObject();
					}

					if (offset > max) {
						max = offset;
						lastTrailer = (COSDictionary)co.getObject();
					}

				}

				checkMainTrailer(pdfDoc.getDocument(), firstTrailer, result);
				if (!compareIds(firstTrailer, lastTrailer, pdfDoc.getDocument())) {
					result.add(new ValidationResult.ValidationError(
							ValidationConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
					"ID is different in the first and the last trailer"));
				}
			}
		} catch (IOException e) {
			result.add(new ValidationResult.ValidationError(
					ValidationConstants.ERROR_SYNTAX_TRAILER,
					"Unable to check PDF Trailers due to : " + e.getMessage()));
		}
	}
	
  /**
   * Return true if the ID of the first dictionary is the same as the id of the
   * last dictionary Return false otherwise.
   * 
   * @param first
   * @param last
   * @return
   */
	protected boolean compareIds(COSDictionary first, COSDictionary last, COSDocument doc) {
		COSBase idFirst = first.getItem(COSName.getPDFName(TRAILER_DICTIONARY_KEY_ID));
		COSBase idLast = last.getItem(COSName.getPDFName(TRAILER_DICTIONARY_KEY_ID));


    if (idFirst == null || idLast == null) {
      return false;
    }

    // ---- cast two COSBase to COSArray.
    COSArray af = COSUtils.getAsArray(idFirst, doc);
    COSArray al = COSUtils.getAsArray(idLast, doc);

    // ---- if one COSArray is null, the PDF/A isn't valid
    if ((af == null) || (al == null)) {
      return false;
    }

    // ---- compare both arrays
    boolean isEqual = true;
    for (Object of : af.toList()) {
      boolean oneIsEquals = false;
      for (Object ol : al.toList()) {
        // ---- according to PDF Reference 1-4, ID is an array containing two
        // strings
				if (!oneIsEquals)
					oneIsEquals = ((COSString) ol).getString().equals(((COSString) of).getString());
      }
      isEqual = isEqual && oneIsEquals;
    }
    return isEqual;
  }

  /**
   * check if all keys are authorized in a trailer dictionary and if the type is
   * valid.
   * 
   * @param trailer
   * @param lErrors
   */
  protected void checkMainTrailer(COSDocument doc, COSDictionary trailer,
      List<ValidationError> lErrors) {
    boolean id = false;
    boolean root = false;
    boolean size = false;
    boolean prev = false;
    boolean info = false;
    boolean encrypt = false;

    for (Object key : trailer.keySet()) {
      if (!(key instanceof COSName)) {
        lErrors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
            "Invalid key in The trailer dictionary"));
        return;
      }

      String cosName = ((COSName) key).getName();
      if (cosName.equals(TRAILER_DICTIONARY_KEY_ENCRYPT)) {
        encrypt = true;
      }
      if (cosName.equals(TRAILER_DICTIONARY_KEY_SIZE)) {
        size = true;
      }
      if (cosName.equals(TRAILER_DICTIONARY_KEY_PREV)) {
        prev = true;
      }
      if (cosName.equals(TRAILER_DICTIONARY_KEY_ROOT)) {
        root = true;
      }
      if (cosName.equals(TRAILER_DICTIONARY_KEY_INFO)) {
        info = true;
      }
      if (cosName.equals(TRAILER_DICTIONARY_KEY_ID)) {
        id = true;
      }
    }

    // ---- PDF/A Trailer dictionary must contain the ID key
    if (!id) {
      lErrors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_SYNTAX_TRAILER_MISSING_ID,
          "The trailer dictionary doesn't contain ID"));
    } else {
      COSBase trailerId = trailer.getItem(COSName
          .getPDFName(TRAILER_DICTIONARY_KEY_ID));
      if (!COSUtils.isArray(trailerId, doc)) {
        lErrors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
            "The trailer dictionary contains an id but it isn't an array"));
      }
    }
    // ---- PDF/A Trailer dictionary mustn't contain the Encrypt key
    if (encrypt) {
      lErrors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_SYNTAX_TRAILER_ENCRYPT,
          "The trailer dictionary contains Encrypt"));
    }
    // ---- PDF Trailer dictionary must contain the Size key
    if (!size) {
      lErrors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_SYNTAX_TRAILER_MISSING_SIZE,
          "The trailer dictionary doesn't contain Size"));
    } else {
      COSBase trailerSize = trailer.getItem(COSName
          .getPDFName(TRAILER_DICTIONARY_KEY_SIZE));
      if (!COSUtils.isInteger(trailerSize, doc)) {
        lErrors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
            "The trailer dictionary contains a size but it isn't an integer"));
      }
    }

    // ---- PDF Trailer dictionary must contain the Root key
    if (!root) {
      lErrors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_SYNTAX_TRAILER_MISSING_ROOT,
          "The trailer dictionary doesn't contain Root"));
    } else {
      COSBase trailerRoot = trailer.getItem(COSName
          .getPDFName(TRAILER_DICTIONARY_KEY_ROOT));
      if (!COSUtils.isDictionary(trailerRoot, doc)) {
        lErrors
            .add(new ValidationResult.ValidationError(
                ValidationConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                "The trailer dictionary contains a root but it isn't a dictionary"));
      }
    }
    // ---- PDF Trailer dictionary may contain the Prev key
    if (prev) {
      COSBase trailerPrev = trailer.getItem(COSName
          .getPDFName(TRAILER_DICTIONARY_KEY_PREV));
      if (!COSUtils.isInteger(trailerPrev, doc)) {
        lErrors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
            "The trailer dictionary contains a prev but it isn't an integer"));
      }
    }
    // ---- PDF Trailer dictionary may contain the Info key
    if (info) {
      COSBase trailerInfo = trailer.getItem(COSName
          .getPDFName(TRAILER_DICTIONARY_KEY_INFO));
      if (!COSUtils.isDictionary(trailerInfo, doc)) {
        lErrors
            .add(new ValidationResult.ValidationError(
                ValidationConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                "The trailer dictionary contains an info but it isn't a dictionary"));
      }
    }
  }

  /**
   * According to the PDF Reference, A linearized PDF contain a dictionary as
   * first object (linearized dictionary) and only this one in the first
   * section.
   * 
   * @param document
   * @return
   */
  protected COSDictionary isLinearizedPdf(PDDocument document) {
    // ---- Get Ref to obj
    COSDocument cDoc = document.getDocument();
    List<?> lObj = cDoc.getObjects();
    for (Object object : lObj) {
      COSBase curObj = ((COSObject) object).getObject();
      if (curObj instanceof COSDictionary
          && ((COSDictionary) curObj).keySet().contains(
              COSName.getPDFName(DICTIONARY_KEY_LINEARIZED))) {
        return (COSDictionary) curObj;
      }
    }
    return null;
  }

  /**
   * Check if mandatory keys of linearized dictionary are present.
   * 
   * @param lErrors
   */
  protected void checkLinearizedDictionnary(COSDictionary linearizedDict,
      List<ValidationError> lErrors) {
    // ---- check if all keys are authorized in a linearized dictionary
    // ---- Linearized dictionary must contain the lhoent keys
    boolean l = false;
    boolean h = false;
    boolean o = false;
    boolean e = false;
    boolean n = false;
    boolean t = false;

    for (Object key : linearizedDict.keySet()) {
      if (!(key instanceof COSName)) {
        lErrors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
            "Invalid key in The Linearized dictionary"));
        return;
      }

      String cosName = ((COSName) key).getName();
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_L)) {
        l = true;
      }
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_H)) {
        h = true;
      }
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_O)) {
        o = true;
      }
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_E)) {
        e = true;
      }
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_N)) {
        n = true;
      }
      if (cosName.equals(DICTIONARY_KEY_LINEARIZED_T)) {
        t = true;
      }
    }

    if (!(l && h && o && e && t && n)) {
      lErrors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_SYNTAX_DICT_INVALID,
          "Invalid key in The Linearized dictionary"));
    }

    return;
  }
}
