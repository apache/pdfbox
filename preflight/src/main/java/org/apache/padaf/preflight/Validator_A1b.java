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

package org.apache.padaf.preflight;

import javax.activation.FileDataSource;

import org.apache.padaf.preflight.ValidationResult.ValidationError;



/**
 * This class is a simple main class used to check the validity of a pdf file.
 * 
 * Usage : java net.awl.edoc.pdfa.Validator <file path>
 * 
 * @author gbailleul
 * 
 */
public class Validator_A1b {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out
			.println("Usage : java net.awl.edoc.pdfa.Validator <file path>");
			System.exit(1);
		}

		FileDataSource fd = new FileDataSource(args[0]);

		PdfA1bValidator validator = new PdfA1bValidator(PdfAValidatorFactory.getStandardPDFA1BConfiguration());

		ValidationResult result = validator.validate(fd);
		if (result.isValid()) {
			result.closePdf();
			System.out.println("The file " + args[0] + " is a valid PDF/A-1b file");
			System.exit(0);
		} else {
			System.out.println("The file" + args[0] + " is not valid, error(s) :");
			for (ValidationError error : result.getErrorsList()) {
				System.out.println(error.getErrorCode() + " : " + error.getDetails());
			}

			result.closePdf();
			System.exit(-1);
		}
	}
}
