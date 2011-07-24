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

import javax.activation.DataSource;

public interface PdfAValidator {

  /**
   * Compute the validation of the given PDF file. If the PDF is valid,
   * ValidationResult contains no error and the method isValid return true.
   * Otherwise, the ValidationResult contains at least one ValidationError and
   * the method isValid return false.
   * 
   * @param source
   *          DataSource which represents the PDF file.
   * @return an instance of ValidationResult
   * @throws ValidationException
   */
  ValidationResult validate(DataSource source) throws ValidationException;
  
  
  /**
   * Return the version qualified name of the product
   */
  String getFullName ();

}
