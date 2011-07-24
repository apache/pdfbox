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

import static org.junit.Assert.assertTrue;


import org.apache.padaf.preflight.PdfA1bValidator;
import org.apache.padaf.preflight.PdfAValidator;
import org.apache.padaf.preflight.PdfAValidatorFactory;
import org.apache.padaf.preflight.ValidationException;
import org.junit.Test;

public class TestPdfaValidationFactory {

  @Test
  public void createPDFA1bValidator() throws ValidationException {
    PdfAValidatorFactory fact = new PdfAValidatorFactory();
    PdfAValidator val = fact
        .createValidatorInstance(PdfAValidatorFactory.PDF_A_1_b);
    assertTrue(val instanceof PdfA1bValidator);
  }

  @Test(expected = ValidationException.class)
  public void createUnknownValidator() throws ValidationException {
    PdfAValidatorFactory fact = new PdfAValidatorFactory();
    fact.createValidatorInstance("UnknownPDFFormat");
  }
}
