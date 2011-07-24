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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.FileDataSource;

import junit.framework.Assert;

import org.apache.padaf.preflight.PdfAValidator;
import org.apache.padaf.preflight.PdfAValidatorFactory;
import org.apache.padaf.preflight.ValidationResult;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestInvalidDirectory {
  
  
  protected static File directory;
  
  protected static PdfAValidator validator = null;
  
  protected File target = null;
  
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    // create validator
    validator = new PdfAValidatorFactory().createValidatorInstance(PdfAValidatorFactory.PDF_A_1_b);
  }
  
  public TestInvalidDirectory (File file) {
    this.target = file;
  }
  
  @Test
  public void validate () throws Exception {
  	ValidationResult result = null;
  	try {
  		result = validator.validate(new FileDataSource(target));
  		Assert.assertFalse(result.isValid());
  	} finally {
  		if (result != null) {
  			result.closePdf();
  		}
  	}
  }
  
  @Parameters
  public static Collection<Object[]> initializeParameters() throws Exception {
    // check directory
    File directory = null;
    String pdfPath = System.getProperty("pdfa.invalid", null);
    if ("${user.pdfa.invalid}".equals(pdfPath)) {pdfPath=null;}
    if (pdfPath!=null) {
      directory = new File(pdfPath);
      if (!directory.exists()) throw new Exception ("directory does not exists : "+directory.getAbsolutePath());
      if (!directory.isDirectory()) throw new Exception ("not a directory : "+directory.getAbsolutePath());
    } else {
      System.err.println("System property 'pdfa.invalid' not defined, will not run TestValidaDirectory");
    }
    // create list
    if (directory==null) {
      return new ArrayList<Object[]>(0);
    } else {
      File [] files = directory.listFiles();
      List<Object[]> data = new ArrayList<Object[]>(files.length);
      for (File file : files) {
        if (file.isFile()) {
          data.add(new Object [] {file});
        }
      }
      return data;
    }
  }
  
  
  
}
