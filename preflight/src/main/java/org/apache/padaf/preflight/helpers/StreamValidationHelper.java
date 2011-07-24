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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.preflight.utils.FilterHelper;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * @author eric
 * 
 */
public class StreamValidationHelper extends AbstractValidationHelper {

	public StreamValidationHelper(ValidatorConfig cfg)
	throws ValidationException {
	  super(cfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.helpers.AbstractValidationHelper#innerValidate
	 * (net.awl.edoc.pdfa.validation.DocumentHandler)
	 */
	@Override
	public List<ValidationError> innerValidate(DocumentHandler handler)
	throws ValidationException {
		List<ValidationError> result = new ArrayList<ValidationError>(0);
		PDDocument pdfDoc = handler.getDocument();
		COSDocument cDoc = pdfDoc.getDocument();

		List<?> lCOSObj = cDoc.getObjects();
		for (Object o : lCOSObj) {
			COSObject cObj = (COSObject) o;

			// If this object represents a Stream, the Dictionary must contain the
			// Length key
			COSBase cBase = cObj.getObject();
			if (cBase instanceof COSStream) {
				// it is a stream
				result.addAll(validateStreamObject(handler, cObj));
			}
		}
		return result;
	}

	public List<ValidationError> validateStreamObject(DocumentHandler handler,
			COSObject cObj) throws ValidationException {
		List<ValidationError> result = new ArrayList<ValidationError>(0);
		COSStream streamObj = (COSStream) cObj.getObject();

		// ---- Check dictionary entries
		// ---- Only the Length entry is mandatory
		// ---- In a PDF/A file, F, FFilter and FDecodeParms are forbidden
		checkDictionaryEntries(streamObj, result);
		// ---- check stream length
		checkStreamLength(handler, cObj, result);
		// ---- Check the Filter value(s)
		checkFilters(streamObj, handler, result);

		return result;
	}

	/**
	 * This method checks if one of declared Filter is LZWdecode. If LZW is found,
	 * the result list is updated with an error code.
	 * 
	 * @param stream
	 * @param handler
	 * @param result
	 */
	protected void checkFilters(COSStream stream, DocumentHandler handler,
			List<ValidationError> result) {
		COSDocument cDoc = handler.getDocument().getDocument();
		COSBase bFilter = stream.getItem(COSName
				.getPDFName(STREAM_DICTIONARY_KEY_FILTER));
		if (bFilter != null) {
			if (COSUtils.isArray(bFilter, cDoc)) {
				COSArray afName = (COSArray) bFilter;
				for (int i = 0; i < afName.size(); ++i) {
					if (!FilterHelper.isAuthorizedFilter(afName.getString(i), result)) {
						return;
					}
				}
			} else if (bFilter instanceof COSName) {
				String fName = ((COSName) bFilter).getName();
				if (!FilterHelper.isAuthorizedFilter(fName, result)) {
					return;
				}
			} else {
				// ---- The filter type is invalid
				result.add(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
				"Filter should be a Name or an Array"));
			}
		} 
		//  else Filter entry is optional
	}

	private boolean readUntilStream(InputStream ra) throws IOException {
		boolean search = true;
		//    String stream = "";
		boolean maybe = false;
		int lastChar = -1;
		do {
			int c = ra.read();
			switch (c) {
			case 's':
				//      stream = "s";
				maybe = true;
				lastChar = c;
				break;
			case 't':
				//      if (maybe && stream.endsWith("s")) {
				if (maybe && lastChar == 's') {
					//          stream = stream + "t";
					lastChar = c;
				} else {
					maybe = false;
					lastChar = -1;
				}
				break;
			case 'r':
				// if (maybe && stream.endsWith("t")) {
				if (maybe && lastChar == 't') {
					//        stream = stream + "r";
					lastChar = c;
				} else {
					maybe = false;
					lastChar = -1;
				}
				break;
			case 'e':
				//      if (maybe && stream.endsWith("r")) {
				if (maybe && lastChar == 'r') {
					lastChar = c;
					//        stream = stream + "e";
				} else {
					maybe = false;
				}
				break;
			case 'a':
				//        if (maybe && stream.endsWith("e")) {
				if (maybe && lastChar == 'e') {
					lastChar = c;
					//        stream = stream + "a";
				} else {
					maybe = false;
				}
				break;
			case 'm':
				//        if (maybe && stream.endsWith("a")) {
				if (maybe && lastChar == 'a') {
					return true;
				} else {
					maybe = false;
				}
				break;
			case -1:
				search = false;
				break;
			default:
				maybe = false;
			break;
			}
		} while (search);
		return false;
	}

	protected void checkStreamLength(DocumentHandler handler, COSObject cObj,
			List<ValidationError> result) throws ValidationException {
		COSStream streamObj = (COSStream) cObj.getObject();
		int length = streamObj.getInt(COSName
				.getPDFName(STREAM_DICTIONARY_KEY_LENGHT));
		InputStream ra = null;
		try {
			ra = handler.getSource().getInputStream();
			Integer offset = (Integer) handler.getDocument().getDocument()
			.getXrefTable().get(new COSObjectKey(cObj));

			// ---- go to the beginning of the object
			long skipped = 0;
			while (skipped != offset) {
				long curSkip = ra.skip(offset - skipped);
				if (curSkip < 0) {
					throw new ValidationException(
							"Unable to skip bytes in the PDFFile to check stream length");
				}
				skipped += curSkip;
			}

			// ---- go to the stream key word
			if (readUntilStream(ra)) {
				int c = ra.read();
				if (c == '\r') {
					ra.read();
				} // else c is '\n' no more character to read


				// ---- Here is the true beginning of the Stream Content.
				// ---- Read the given length of bytes and check the 10 next bytes
				// ---- to see if there are endstream.
				byte[] buffer = new byte[1024];
				int nbBytesToRead = length;

				do {
					int cr = 0;
					if (nbBytesToRead > 1024) {
						cr = ra.read(buffer, 0, 1024);
					} else {
						cr = ra.read(buffer, 0, nbBytesToRead);
					}
					if (cr == -1) {
						result.add(new ValidationResult.ValidationError(
								ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
								"Stream length is invalide"));
						return;
					} else {
						nbBytesToRead = nbBytesToRead - cr;
					}
				} while (nbBytesToRead > 0);

				int len = "endstream".length() + 2;
				byte[] buffer2 = new byte[len];
				for (int i = 0; i < len; ++i) {
					buffer2[i] = (byte) ra.read();
				}

				// ---- check the content of 10 last characters
				String endStream = new String(buffer2);
				if (buffer2[0] == '\r' && buffer2[1] == '\n') {
					if (!endStream.contains("endstream")) {
						result.add(new ValidationResult.ValidationError(
								ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
								"Stream length is invalide"));
					}
				} else if (buffer2[0] == '\r' && buffer2[1] == 'e') {
					if (!endStream.contains("endstream")) {
						result.add(new ValidationResult.ValidationError(
								ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
								"Stream length is invalide"));
					}
				} else if (buffer2[0] == '\n' && buffer2[1] == 'e') {
					if (!endStream.contains("endstream")) {
						result.add(new ValidationResult.ValidationError(
								ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
								"Stream length is invalide"));
					}
				} else {
					result.add(new ValidationResult.ValidationError(
							ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
							"Stream length is invalide"));
				}

			} else {
				result.add(new ValidationResult.ValidationError(
						ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID,
						"Stream length is invalide"));
			}
		} catch (IOException e) {
			throw new ValidationException(
					"Unable to read a stream to validate it due to : " + e.getMessage(),
					e);
		} finally {
			if ( ra != null) {
				IOUtils.closeQuietly(ra);
			}
		}
	}

	/**
	 * Check dictionary entries. Only the Length entry is mandatory. In a PDF/A
	 * file, F, FFilter and FDecodeParms are forbidden
	 * 
	 * @param streamObj
	 * @param result
	 */
	protected void checkDictionaryEntries(COSStream streamObj,
			List<ValidationError> result) {
		boolean len = false;
		boolean f = false;
		boolean ffilter = false;
		boolean fdecParams = false;

		for (Object key : streamObj.keyList()) {
			if (!(key instanceof COSName)) {
				result.add(new ValidationResult.ValidationError(
						ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
						"Invalid key in The Stream dictionary"));
				return;
			}
			COSName cosName = (COSName) key;
			if (cosName.getName().equals(STREAM_DICTIONARY_KEY_LENGHT)) {
				len = true;
			}
			if (cosName.getName().equals(STREAM_DICTIONARY_KEY_F)) {
				f = true;
			}
			if (cosName.getName().equals(STREAM_DICTIONARY_KEY_FFILTER)) {
				ffilter = true;
			}
			if (cosName.getName().equals(STREAM_DICTIONARY_KEY_FDECODEPARAMS)) {
				fdecParams = true;
			}
		}

		if (!len) {
			result.add(new ValidationResult.ValidationError(
					ValidationConstants.ERROR_SYNTAX_STREAM_LENGTH_MISSING,
					"Stream length is missing"));
		}

		if (f || ffilter || fdecParams) {
			result
			.add(new ValidationResult.ValidationError(
					ValidationConstants.ERROR_SYNTAX_STREAM_FX_KEYS,
					"F, FFilter or FDecodeParms keys are present in the stream dictionary"));
		}
	}
}
