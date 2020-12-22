/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test Class of SynchronizedMetaDataValidation (for 6-7-3 Isartor Tests)
 * 
 * @author Germain Costenobel
 * 
 */
class TestSynchronizedMetadataValidation
{

    protected PDDocument doc;
    protected PDDocumentInformation dico;
    protected XMPMetadata metadata;
    protected String title, author, subject, keywords, creator, producer;
    protected Calendar creationDate, modifyDate;
    protected static SynchronizedMetaDataValidation sync;
    protected List<ValidationError> ve;

    @BeforeAll
    public static void initSynchronizedMetadataValidation()
    {
        sync = new SynchronizedMetaDataValidation();
    }

    @BeforeEach
    public void initNewDocumentInformation() throws Exception
    {
        doc = new PDDocument();
        dico = doc.getDocumentInformation();
        metadata = XMPMetadata.createXMPMetadata();
    }

    /**
     * Check detection of a null Document
     * 
     * @throws ValidationException
     */
    @Test
    void TestNullDocument() throws ValidationException
    {
        assertThrows(ValidationException.class, () -> {
            sync.validateMetadataSynchronization(null, metadata);
        });
    }

    /**
     * Check detection of null metadata
     * 
     * @throws ValidationException
     */
    @Test
    void TestNullMetaData() throws ValidationException
    {
        assertThrows(ValidationException.class, () -> {
            sync.validateMetadataSynchronization(doc, null);
        });
    }

    /**
     * Check the detection of a PDF document without any information
     * 
     * @throws Exception
     */
    @Test
    void TestDocumentWithoutInformation() throws Exception
    {
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test without any information
            assertEquals(0, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Check the detection of a completely empty XMP document (without any schemas)
     * 
     * @throws Exception
     */
    @Test
    void testEmptyXMP() throws Exception
    {
        initValues();

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

        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test Detection of an Empty XMP (without any schemas)
            for (final ValidationError valid : ve)
            {
                assertEquals(PreflightConstants.ERROR_METADATA_MISMATCH, valid.getErrorCode());
            }
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Check the detection of a XMP with empty common schemas
     * 
     * @throws Exception
     */
    @Test
    void testEmptyXMPSchemas() throws Exception
    {
        initValues();

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

        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test Detection of absent XMP values
            assertEquals(8, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Check detection of a null value in array (for Subject and author properties)
     * 
     * @throws Exception
     */
    @Test
    void testNullArrayValue() throws Exception
    {
        // building temporary XMP metadata

        final DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();

        // AUTHOR
        dico.setAuthor("dicoAuthor");
        assertThrows(IllegalArgumentException.class, () -> {
            dc.addCreator(null);
        });
        
        // SUBJECT
        dico.setSubject("dicoSubj");
        assertThrows(IllegalArgumentException.class, () -> {
            dc.addSubject(null);
        });

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test unsychronized value
            assertEquals(2, ve.size());

        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * in XMP, Subject and Author must be embedded in a single entry text array This function check the detection of
     * multiple entries for these properties
     * 
     * @throws Exception
     */
    @Test
    void testBadSizeOfArrays() throws Exception
    {
        // building temporary XMP metadata

        final DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
        final AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
        final XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();

        // Writing info in XMP and Document Information dictionary
        // TITLE
        dico.setTitle("dicoTitle");
        dc.setTitle("x-default", "XMPTitle");
        // AUTHOR
        dico.setAuthor("dicoAuthor");
        dc.addCreator("XMPAuthor");
        dc.addCreator("2ndCreator");
        // SUBJECT
        dico.setSubject("dicoSubj");
        dc.addSubject("XMPSubj");
        dc.addSubject("2ndSubj");
        // KEYWORDS
        dico.setKeywords("DicoKeywords");
        pdf.setKeywords("XMPkeywords");
        // CREATOR
        dico.setCreator("DicoCreator");
        xmp.setCreatorTool("XMPCreator");
        // PRODUCER
        dico.setProducer("DicoProducer");
        pdf.setProducer("XMPProducer");
        // CREATION DATE
        dico.setCreationDate(Calendar.getInstance());
        final GregorianCalendar XMPCreate = new GregorianCalendar(2008, 11, 05);
        xmp.setCreateDate(XMPCreate);
        // MODIFY DATE
        dico.setModificationDate(Calendar.getInstance());
        final GregorianCalendar XMPModify = new GregorianCalendar(2009, 10, 15);
        xmp.setModifyDate(XMPModify);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test unsychronized value
            assertEquals(8, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Check the detection of unsynchronized information between Document Information dictionary and XMP
     * 
     * @throws Exception
     */
    @Test
    void testAllInfoUnsynchronized() throws Exception
    {
        // building temporary XMP metadata

        final DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
        final AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
        final XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();

        // Writing info in XMP and Document Information dictionary
        // TITLE
        dico.setTitle("dicoTitle");
        dc.setTitle("x-default", "XMPTitle");
        // AUTHOR
        dico.setAuthor("dicoAuthor");
        dc.addCreator("XMPAuthor");
        // SUBJECT
        dico.setSubject("dicoSubj");
        dc.addSubject("XMPSubj");
        // KEYWORDS
        dico.setKeywords("DicoKeywords");
        pdf.setKeywords("XMPkeywords");
        // CREATOR
        dico.setCreator("DicoCreator");
        xmp.setCreatorTool("XMPCreator");
        // PRODUCER
        dico.setProducer("DicoProducer");
        pdf.setProducer("XMPProducer");
        // CREATION DATE
        dico.setCreationDate(Calendar.getInstance());
        final GregorianCalendar XMPCreate = new GregorianCalendar(2008, 11, 05);
        xmp.setCreateDate(XMPCreate);
        // MODIFY DATE
        dico.setModificationDate(Calendar.getInstance());
        final GregorianCalendar XMPModify = new GregorianCalendar(2009, 10, 15);
        xmp.setModifyDate(XMPModify);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test unsychronized value
            assertEquals(8, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Check reaction when metadata are well-formed
     * 
     * @throws Exception
     */
    @Test
    void testAllInfoSynchronized() throws Exception
    {
        initValues();

        // building temporary XMP metadata
        final DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
        final XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();
        final AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
        // Writing info in XMP and Document Information dictionary
        // TITLE
        dico.setTitle(title);
        dc.setTitle("x-default", title);
        // AUTHOR
        dico.setAuthor(author);
        dc.addCreator(author);
        // SUBJECT
        dico.setSubject(subject);
        dc.addDescription("x-default", subject);
        // KEYWORDS
        dico.setKeywords(keywords);
        pdf.setKeywords(keywords);
        // CREATOR
        dico.setCreator(creator);
        xmp.setCreatorTool(creator);
        // PRODUCER
        dico.setProducer(producer);
        pdf.setProducer(producer);
        // CREATION DATE
        dico.setCreationDate(creationDate);
        xmp.setCreateDate(creationDate);
        // MODIFY DATE
        dico.setModificationDate(modifyDate);
        xmp.setModifyDate(modifyDate);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Checking all is synchronized
            assertEquals(0, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }

    }
    /**
     * Check if schemaAccessException Generator is ok
     * 
     * @throws Exception
     */
    @Test
    void checkSchemaAccessException() throws Exception
    {
        final Throwable cause = new Throwable();
        assertSame(cause, sync.schemaAccessException("test", cause).getCause());
    }

    /**
     * Check reaction when metadata are well-formed
     * 
     * @throws Exception
     */
    @Test
    void testBadPrefixSchemas() throws Exception
    {
        initValues();

        // building temporary XMP metadata
        final DublinCoreSchema dc = new DublinCoreSchema(metadata, "dctest");
        metadata.addSchema(dc);
        final XMPBasicSchema xmp = new XMPBasicSchema(metadata, "xmptest");
        metadata.addSchema(xmp);
        final AdobePDFSchema pdf = new AdobePDFSchema(metadata, "pdftest");
        metadata.addSchema(pdf);

        // Writing info in XMP and Document Information dictionary
        // TITLE
        dico.setTitle(title);
        dc.setTitle("x-default", title);
        // AUTHOR
        dico.setAuthor(author);
        dc.addCreator(author);
        // SUBJECT
        dico.setSubject(subject);
        dc.addDescription("x-default", subject);
        // KEYWORDS
        dico.setKeywords(keywords);
        pdf.setKeywords(keywords);
        // CREATOR
        dico.setCreator(creator);
        xmp.setCreatorTool(creator);
        // PRODUCER
        dico.setProducer(producer);
        pdf.setProducer(producer);
        // CREATION DATE
        dico.setCreationDate(creationDate);
        xmp.setCreateDate(creationDate);
        // MODIFY DATE
        dico.setModificationDate(modifyDate);
        xmp.setModifyDate(modifyDate);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            for (final ValidationError valid : ve)
            {
                assertEquals(PreflightConstants.ERROR_METADATA_WRONG_NS_PREFIX, valid.getErrorCode());
            }
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Check reaction when metadata are well-formed
     * 
     * @throws Exception
     */
    @Test
    void testdoublePrefixSchemas() throws Exception
    {
        initValues();

        // building temporary XMP metadata
        final DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
        final DublinCoreSchema dc2 = new DublinCoreSchema(metadata, "dctest");
        metadata.addSchema(dc2);
        final XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();
        final XMPBasicSchema xmp2 = new XMPBasicSchema(metadata, "xmptest");
        metadata.addSchema(xmp2);
        final AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
        final AdobePDFSchema pdf2 = new AdobePDFSchema(metadata, "pdftest");
        metadata.addSchema(pdf2);

        // write some temp info in 'false' schemas
        dc2.setCoverage("tmpcover");
        xmp2.setCreatorTool("tmpcreator");
        pdf2.setKeywords("tmpkeys");

        // Writing info in XMP and Document Information dictionary
        // TITLE
        dico.setTitle(title);
        dc.setTitle("x-default", title);
        // AUTHOR
        dico.setAuthor(author);
        dc.addCreator(author);
        // SUBJECT
        dico.setSubject(subject);
        dc.addDescription("x-default", subject);
        // KEYWORDS
        dico.setKeywords(keywords);
        pdf.setKeywords(keywords);
        // CREATOR
        dico.setCreator(creator);
        xmp.setCreatorTool(creator);
        // PRODUCER
        dico.setProducer(producer);
        pdf.setProducer(producer);
        // CREATION DATE
        dico.setCreationDate(creationDate);
        xmp.setCreateDate(creationDate);
        // MODIFY DATE
        dico.setModificationDate(modifyDate);
        xmp.setModifyDate(modifyDate);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            assertTrue(ve.isEmpty());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Tests that two date values, which are from different time zones but
     * really identical, are detected as such.
     *
     * @throws Exception
     */
    @Test
    void testPDFBox4292() throws Exception
    {
        initValues();

        final Calendar cal1 = org.apache.pdfbox.util.DateConverter.toCalendar("20180817115837+02'00'");
        final Calendar cal2 = org.apache.xmpbox.DateConverter.toCalendar("2018-08-17T09:58:37Z");

        final XMPBasicSchema xmp = metadata.createAndAddXMPBasicSchema();

        dico.setCreationDate(cal1);
        xmp.setCreateDate(cal2);
        dico.setModificationDate(cal1);
        xmp.setModifyDate(cal2);

        // Launching synchronization test
        try
        {
            ve = sync.validateMetadataSynchronization(doc, metadata);
            // Test unsychronized value
            assertEquals(0, ve.size());
        }
        catch (final ValidationException e)
        {
            throw new Exception(e.getMessage());
        }
    }

    @AfterEach
    public void checkErrors() throws Exception
    {
        try
        {
            doc.close();
        }
        catch (final IOException e)
        {
            throw new Exception("Error while closing PDF Document");
        }
        /*
         * Iterator<ValidationError> it=ve.iterator(); while(it.hasNext()){ ValidationError tmp=it.next();
         * System.out.println("Error:"+ tmp.getDetails()+"\n code: "+tmp.getErrorCode()); }
         */
    }

    private void initValues()
    {
        title = "TITLE";
        author = "AUTHOR(S)";
        subject = "SUBJECTS";
        keywords = "KEYWORD(S)";
        creator = "CREATOR";
        producer = "PRODUCER";
        creationDate = Calendar.getInstance();
        modifyDate = Calendar.getInstance();

        // PDFBOX-4292: because xmp keeps the milliseconds before writing to XML,
        // but COS doesn't, tests would fail when calendar values are compared
        // so reset the milliseconds. 
        creationDate.set(Calendar.MILLISECOND, 0);
        modifyDate.set(Calendar.MILLISECOND, 0);
    }
}
