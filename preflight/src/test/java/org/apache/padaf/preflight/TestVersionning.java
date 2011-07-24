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

import org.apache.padaf.preflight.PdfAValidator;
import org.apache.padaf.preflight.PdfAValidatorFactory;
import org.junit.Test;

public class TestVersionning {
	
	
	@Test
	public void testGetVersion () throws Exception {
		PdfAValidatorFactory factory = new PdfAValidatorFactory();
		PdfAValidator validator = factory.createValidatorInstance(PdfAValidatorFactory.PDF_A_1_b);
		
		System.err.println(">> "+validator.getFullName());
	}

}
