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

package org.apache.padaf.preflight.xmp;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.xmp.SynchronizedMetaDataValidation;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.AdobePDFSchema;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.XMPBasicSchema;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Class of SynchronizedMetaDataValidation (for 6-7-3 Isartor Tests)
 * 
 * @author Germain Costenobel
 * 
 */
public class TestSynchronizedMetadataValidation {

  protected PDDocument doc;
  protected PDDocumentInformation dico;
  protected XMPMetadata metadata;
  protected String title, author, subject, keywords, creator, producer;
  protected Calendar creationDate, modifyDate;
  protected static SynchronizedMetaDataValidation sync;
  protected List<ValidationError> ve;

  @BeforeClass
  public static void initSynchronizedMetadataValidation() {
    sync = new SynchronizedMetaDataValidation();
  }

  @Before
  public void initNewDocumentInformation() throws Exception {

    try {
      doc = new PDDocument();
      dico = doc.getDocumentInformation();
      metadata = new XMPMetadata();
    } catch (IOException e) {
      throw new Exception("Failed to create temporary test PDF/XMP Document");
    }

  }

  /**
   * Check detection of a null Document
   * 
   * @throws ValidationException
   */
  @Test(expected = ValidationException.class)
  public void TestNullDocument() throws ValidationException {
    sync.validateMetadataSynchronization(null, metadata);
  }

  /**
   * Check detection of null metadata
   * 
   * @throws ValidationException
   */
  @Test(expected = ValidationException.class)
  public void TestNullMetaData() throws ValidationException {
    sync.validateMetadataSynchronization(doc, null);
  }

  /**
   * Check the detection of a PDF document without any information
   * 
   * @throws Exception
   */
  @Test
  public void TestDocumentWithoutInformation() throws Exception {
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test without any information
      Assert.assertEquals(0, ve.size());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }
  }

  /**
   * Check the detection of a completely empty XMP document (without any
   * schemas)
   * 
   * @throws Exception
   */
  @Test
  public void testEmptyXMP() throws Exception {
    title = "TITLE";
    author = "AUTHOR(S)";
    subject = "SUBJECTS";
    keywords = "KEYWORD(S)";
    creator = "CREATOR";
    producer = "PRODUCER";
    creationDate = Calendar.getInstance();
    modifyDate = Calendar.getInstance();

    // Writing info in Document Information dictionary
    // TITLE
    dico.setTitle(title);
    // AUTHOR
    dico.setAuthor(author);
    // SUBJECT
    dico.setSubject(subject);
    // KEYWORDS
    dico.setKeywords(keywords);
    // CREATOR
    dico.setCreator(creator);
    // PRODUCER
    dico.setProducer(producer);
    // CREATION DATE
    dico.setCreationDate(creationDate);
    // MODIFY DATE
    dico.setModificationDate(modifyDate);

    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test Detection of an Empty XMP (without any schemas)
      for (ValidationError valid : ve) {
        Assert.assertEquals(ValidationConstants.ERROR_METADATA_MISMATCH, valid
            .getErrorCode());
      }
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }
  }

  /**
   * Check the detection of a XMP with empty common schemas
   * 
   * @throws Exception
   */
  @Test
  public void testEmptyXMPSchemas() throws Exception {
    title = "TITLE";
    author = "AUTHOR(S)";
    subject = "SUBJECTS";
    keywords = "KEYWORD(S)";
    creator = "CREATOR";
    producer = "PRODUCER";
    creationDate = Calendar.getInstance();
    modifyDate = Calendar.getInstance();

    // building temporary XMP metadata (but empty)
    metadata.createAndAddDublinCoreSchema();
    metadata.createAndAddAdobePDFSchema();
    metadata.createAndAddXMPBasicSchema();

    // Writing info in Document Information dictionary
    // TITLE
    dico.setTitle(title);
    // AUTHOR
    dico.setAuthor(author);
    // SUBJECT
    dico.setSubject(subject);
    // KEYWORDS
    dico.setKeywords(keywords);
    // CREATOR
    dico.setCreator(creator);
    // PRODUCER
    dico.setProducer(producer);
    // CREATION DATE
    dico.setCreationDate(creationDate);
    // MODIFY DATE
    dico.setModificationDate(modifyDate);

    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test Detection of absent XMP values
      Assert.assertEquals(8, ve.size());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  /**
   * Check detection of a null value in array (for Subject and author
   * properties)
   * 
   * @throws Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullArrayValue() throws Exception {
    // building temporary XMP metadata

    DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();

    // AUTHOR
    dico.setAuthor("dicoAuthor");
    dc.addToCreatorValue(null);

    // SUBJECT
    dico.setSubject("dicoSubj");
    dc.addToSubjectValue(null);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test unsychronized value
      Assert.assertEquals(2, ve.size());

    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }
  }

  /**
   * in XMP, Subject and Author must be embedded in a single entry text array
   * This function check the detection of multiple entries for these properties
   * 
   * @throws Exception
   */
  @Test
  public void testBadSizeOfArrays() throws Exception {
    // building temporary XMP metadata

    DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
    AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
    XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();

    // Writing info in XMP and Document Information dictionary
    // TITLE
    dico.setTitle("dicoTitle");
    dc.addToTitleValue("x-default", "XMPTitle");
    // AUTHOR
    dico.setAuthor("dicoAuthor");
    dc.addToCreatorValue("XMPAuthor");
    dc.addToCreatorValue("2ndCreator");
    // SUBJECT
    dico.setSubject("dicoSubj");
    dc.addToSubjectValue("XMPSubj");
    dc.addToSubjectValue("2ndSubj");
    // KEYWORDS
    dico.setKeywords("DicoKeywords");
    pdf.setKeywordsValue("XMPkeywords");
    // CREATOR
    dico.setCreator("DicoCreator");
    xmp.setCreatorToolValue("XMPCreator");
    // PRODUCER
    dico.setProducer("DicoProducer");
    pdf.setProducerValue("XMPProducer");
    // CREATION DATE
    dico.setCreationDate(Calendar.getInstance());
    GregorianCalendar XMPCreate = new GregorianCalendar(2008, 11, 05);
    xmp.setCreateDateValue(XMPCreate);
    // MODIFY DATE
    dico.setModificationDate(Calendar.getInstance());
    GregorianCalendar XMPModify = new GregorianCalendar(2009, 10, 15);
    xmp.setModifyDateValue(XMPModify);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test unsychronized value
      Assert.assertEquals(8, ve.size());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  /**
   * Check the detection of unsynchronized information between Document
   * Information dictionary and XMP
   * 
   * @throws Exception
   */
  @Test
  public void testAllInfoUnsynchronized() throws Exception {
    // building temporary XMP metadata

    DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
    AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
    XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();

    // Writing info in XMP and Document Information dictionary
    // TITLE
    dico.setTitle("dicoTitle");
    dc.addToTitleValue("x-default", "XMPTitle");
    // AUTHOR
    dico.setAuthor("dicoAuthor");
    dc.addToCreatorValue("XMPAuthor");
    // SUBJECT
    dico.setSubject("dicoSubj");
    dc.addToSubjectValue("XMPSubj");
    // KEYWORDS
    dico.setKeywords("DicoKeywords");
    pdf.setKeywordsValue("XMPkeywords");
    // CREATOR
    dico.setCreator("DicoCreator");
    xmp.setCreatorToolValue("XMPCreator");
    // PRODUCER
    dico.setProducer("DicoProducer");
    pdf.setProducerValue("XMPProducer");
    // CREATION DATE
    dico.setCreationDate(Calendar.getInstance());
    GregorianCalendar XMPCreate = new GregorianCalendar(2008, 11, 05);
    xmp.setCreateDateValue(XMPCreate);
    // MODIFY DATE
    dico.setModificationDate(Calendar.getInstance());
    GregorianCalendar XMPModify = new GregorianCalendar(2009, 10, 15);
    xmp.setModifyDateValue(XMPModify);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Test unsychronized value
      Assert.assertEquals(8, ve.size());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  /**
   * Check reaction when metadata are well-formed
   * 
   * @throws Exception
   */
  @Test
  public void testAllInfoSynhcronized() throws Exception {
    title = "TITLE";
    author = "AUTHOR(S)";
    subject = "SUBJECTS";
    keywords = "KEYWORD(S)";
    creator = "CREATOR";
    producer = "PRODUCER";
    creationDate = Calendar.getInstance();
    modifyDate = Calendar.getInstance();

    // building temporary XMP metadata
    DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
    XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();
    AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
    // Writing info in XMP and Document Information dictionary
    // TITLE
    dico.setTitle(title);
    dc.addToTitleValue("x-default", title);
    // AUTHOR
    dico.setAuthor(author);
    dc.addToCreatorValue(author);
    // SUBJECT
    dico.setSubject(subject);
    dc.addToDescriptionValue("x-default", subject);
    // KEYWORDS
    dico.setKeywords(keywords);
    pdf.setKeywordsValue(keywords);
    // CREATOR
    dico.setCreator(creator);
    xmp.setCreatorToolValue(creator);
    // PRODUCER
    dico.setProducer(producer);
    pdf.setProducerValue(producer);
    // CREATION DATE
    dico.setCreationDate(creationDate);
    xmp.setCreateDateValue(creationDate);
    // MODIFY DATE
    dico.setModificationDate(modifyDate);
    xmp.setModifyDateValue(modifyDate);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      // Checking all is synchronized
      Assert.assertEquals(0, ve.size());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  /**
   * Check if FormatAccessException Generator is ok
   * 
   * @throws Exception
   */
  @Test
  public void checkformatAccessException() throws Exception {
    Throwable cause = new Throwable();
    Assert.assertSame(cause, sync.formatAccessException("test", "test", cause)
        .getCause());
  }

  /**
   * Check if SchemaAccessException Generator is ok
   * 
   * @throws Exception
   */
  @Test
  public void checkSchemaAccessException() throws Exception {
    Throwable cause = new Throwable();
    Assert.assertSame(cause, sync.SchemaAccessException("test", cause)
        .getCause());
  }

  /**
   * Check reaction when metadata are well-formed
   * 
   * @throws Exception
   */
  @Test
  public void testBadPrefixSchemas() throws Exception {
    title = "TITLE";
    author = "AUTHOR(S)";
    subject = "SUBJECTS";
    keywords = "KEYWORD(S)";
    creator = "CREATOR";
    producer = "PRODUCER";
    creationDate = Calendar.getInstance();
    modifyDate = Calendar.getInstance();

    // building temporary XMP metadata
    DublinCoreSchema dc = new DublinCoreSchema(metadata, "dctest");
    metadata.addSchema(dc);
    XMPBasicSchema xmp = new XMPBasicSchema(metadata, "xmptest");
    metadata.addSchema(xmp);
    AdobePDFSchema pdf = new AdobePDFSchema(metadata, "pdftest");
    metadata.addSchema(pdf);

    // Writing info in XMP and Document Information dictionary
    // TITLE
    dico.setTitle(title);
    dc.addToTitleValue("x-default", title);
    // AUTHOR
    dico.setAuthor(author);
    dc.addToCreatorValue(author);
    // SUBJECT
    dico.setSubject(subject);
    dc.addToDescriptionValue("x-default", subject);
    // KEYWORDS
    dico.setKeywords(keywords);
    pdf.setKeywordsValue(keywords);
    // CREATOR
    dico.setCreator(creator);
    xmp.setCreatorToolValue(creator);
    // PRODUCER
    dico.setProducer(producer);
    pdf.setProducerValue(producer);
    // CREATION DATE
    dico.setCreationDate(creationDate);
    xmp.setCreateDateValue(creationDate);
    // MODIFY DATE
    dico.setModificationDate(modifyDate);
    xmp.setModifyDateValue(modifyDate);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      for (ValidationError valid : ve) {
        Assert.assertEquals(ValidationConstants.ERROR_METADATA_WRONG_NS_PREFIX,
            valid.getErrorCode());
      }
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  /**
   * Check reaction when metadata are well-formed
   * 
   * @throws Exception
   */
  @Test
  public void testdoublePrefixSchemas() throws Exception {
    title = "TITLE";
    author = "AUTHOR(S)";
    subject = "SUBJECTS";
    keywords = "KEYWORD(S)";
    creator = "CREATOR";
    producer = "PRODUCER";
    creationDate = Calendar.getInstance();
    modifyDate = Calendar.getInstance();

    // building temporary XMP metadata
    DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
    DublinCoreSchema dc2 = new DublinCoreSchema(metadata, "dctest");
    metadata.addSchema(dc2);
    XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();
    XMPBasicSchema xmp2 = new XMPBasicSchema(metadata, "xmptest");
    metadata.addSchema(xmp2);
    AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
    AdobePDFSchema pdf2 = new AdobePDFSchema(metadata, "pdftest");
    metadata.addSchema(pdf2);

    // write some temp info in 'false' schemas
    dc2.setCoverageValue("tmpcover");
    xmp2.setCreatorToolValue("tmpcreator");
    pdf2.setKeywordsValue("tmpkeys");

    // Writing info in XMP and Document Information dictionary
    // TITLE
    dico.setTitle(title);
    dc.addToTitleValue("x-default", title);
    // AUTHOR
    dico.setAuthor(author);
    dc.addToCreatorValue(author);
    // SUBJECT
    dico.setSubject(subject);
    dc.addToDescriptionValue("x-default", subject);
    // KEYWORDS
    dico.setKeywords(keywords);
    pdf.setKeywordsValue(keywords);
    // CREATOR
    dico.setCreator(creator);
    xmp.setCreatorToolValue(creator);
    // PRODUCER
    dico.setProducer(producer);
    pdf.setProducerValue(producer);
    // CREATION DATE
    dico.setCreationDate(creationDate);
    xmp.setCreateDateValue(creationDate);
    // MODIFY DATE
    dico.setModificationDate(modifyDate);
    xmp.setModifyDateValue(modifyDate);

    // Launching synchronization test
    try {
      ve = sync.validateMetadataSynchronization(doc, metadata);
      Assert.assertTrue(ve.isEmpty());
    } catch (ValidationException e) {
      throw new Exception(e.getMessage());
    }

  }

  @After
  public void checkErrors() throws Exception {
    try {
      doc.close();
    } catch (IOException e) {
      throw new Exception("Error while closing PDF Document");
    }
    /*
     * Iterator<ValidationError> it=ve.iterator(); while(it.hasNext()){
     * ValidationError tmp=it.next(); System.out.println("Error:"+
     * tmp.getDetails()+"\n code: "+tmp.getErrorCode()); }
     */
  }

}
