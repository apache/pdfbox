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


import static org.apache.padaf.preflight.ValidatorConfig.ACRO_FORM_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.BOOKMARK_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.CATALOG_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.FILE_SPECIF_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.FONT_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.GRAPHIC_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.META_DATA_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.PAGE_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.STREAM_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.TRAILER_FILTER;
import static org.apache.padaf.preflight.ValidatorConfig.XREF_FILTER;

import org.apache.padaf.preflight.actions.ActionManagerFactory;
import org.apache.padaf.preflight.annotation.PDFAbAnnotationFactory;
import org.apache.padaf.preflight.helpers.AcroFormValidationHelper;
import org.apache.padaf.preflight.helpers.BookmarkValidationHelper;
import org.apache.padaf.preflight.helpers.CatalogValidationHelper;
import org.apache.padaf.preflight.helpers.FileSpecificationValidationHelper;
import org.apache.padaf.preflight.helpers.FontValidationHelper;
import org.apache.padaf.preflight.helpers.GraphicsValidationHelper;
import org.apache.padaf.preflight.helpers.MetadataValidationHelper;
import org.apache.padaf.preflight.helpers.PagesValidationHelper;
import org.apache.padaf.preflight.helpers.StreamValidationHelper;
import org.apache.padaf.preflight.helpers.TrailerValidationHelper;
import org.apache.padaf.preflight.helpers.XRefValidationHelper;

/**
 * This Factory Provide an instance of PdfAValidator.<BR />
 * If you call the <I>createValidatorInstance</I> without ValidationConfig, the instance will 
 * be created using the right default configuration. (static attributes in this factory)<BR />
 * If you call the <I>createValidatorInstance</I> with your own ValidationConfig, be careful setting 
 * helpers with the right priority. For a PDF/A here is the 3 first helpers to call :
 * <UL>
 * <li>CatalogValidationHelper to initialize the OCCProfileWrapper in the DocumentHandler
 * <li>StreamValidationHelper to check the length of stream before parse them.
 * <li>FontValidationHelper to store FontContainers in the DocumentHandler before validate Text operator
 * </UL>
 * In addition you can set a custom AnnotationValidatorFactory and a custom ActionManagerFactory. By default
 * the configuration object defines  the PDFAbAnnotationFactory and the ActionManagerFactory.
 */
public class PdfAValidatorFactory {

  public static final String PDF_A_1_b = "PDF/A-1b";
 
  /**
   * Create the Generic Configuration For the PDF/A-1B file
   * @return
   */
  public static ValidatorConfig getStandardPDFA1BConfiguration () {
	  ValidatorConfig pdfa1bStandardConfig = new ValidatorConfig();
	  pdfa1bStandardConfig.addPriorHelpers(STREAM_FILTER, StreamValidationHelper.class);
	  pdfa1bStandardConfig.addPriorHelpers(CATALOG_FILTER, CatalogValidationHelper.class);
	  pdfa1bStandardConfig.addPriorHelpers(FONT_FILTER, FontValidationHelper.class);
	  pdfa1bStandardConfig.addPriorHelpers(GRAPHIC_FILTER, GraphicsValidationHelper.class);

	  pdfa1bStandardConfig.addStandHelpers(TRAILER_FILTER, TrailerValidationHelper.class);
	  pdfa1bStandardConfig.addStandHelpers(XREF_FILTER, XRefValidationHelper.class);
	  pdfa1bStandardConfig.addStandHelpers(BOOKMARK_FILTER, BookmarkValidationHelper.class);
	  pdfa1bStandardConfig.addStandHelpers(ACRO_FORM_FILTER, AcroFormValidationHelper.class);
	  pdfa1bStandardConfig.addStandHelpers(FILE_SPECIF_FILTER, FileSpecificationValidationHelper.class);
	  // Page Helper must be called after the FontHelper to check
	  // if Fonts used by the page content are embedded in the PDF file.
	  pdfa1bStandardConfig.addStandHelpers(PAGE_FILTER, PagesValidationHelper.class);
	  pdfa1bStandardConfig.addStandHelpers(META_DATA_FILTER, MetadataValidationHelper.class);

	  pdfa1bStandardConfig.setActionFactory(ActionManagerFactory.class);
	  pdfa1bStandardConfig.setAnnotationFactory(PDFAbAnnotationFactory.class);
	  return pdfa1bStandardConfig;
  }

  /**
   * Return an implementation of PdfAValidator according to the given format using the 
   * default configuration linked to the format.
   * 
   * @param format
   *          "PDF/A-1b" for a PDF/A-1b validator.
   * @return
   * @throws ValidationException
   */
  public PdfAValidator createValidatorInstance(String format)
      throws ValidationException {
    if (PDF_A_1_b.equals(format)) {
      return new PdfA1bValidator(getStandardPDFA1BConfiguration());
    } else {
      throw new ValidationException("Unknown pdf format : " + format);
    }
  }
 

  /**
   * Return an implementation of PdfAValidator according to the given format.
   * 
   * @param format  "PDF/A-1b" for a PDF/A-1b validator.
   * @param conf Instance of ValidatorConfig to use customized Helper
   * @return
   * @throws ValidationException
   */
  public PdfAValidator createValidatorInstance(String format, ValidatorConfig conf)
      throws ValidationException {

	if (conf == null) {
	  return createValidatorInstance(format);
	}
  
    if (PDF_A_1_b.equals(format)) {
      return new PdfA1bValidator(conf);
    } else {
      throw new ValidationException("Unknown pdf format : " + format);
    }
  }
}