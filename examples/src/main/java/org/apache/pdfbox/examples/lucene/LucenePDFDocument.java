/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * This class is used to create a document for the lucene search engine. This should easily plug into the IndexPDFFiles
 * that comes with the lucene project. This class will populate the following fields.
 * <table>
 * <tr>
 * <th>Lucene Field Name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>path</td>
 * <td>File system path if loaded from a file</td>
 * </tr>
 * <tr>
 * <td>url</td>
 * <td>URL to PDF document</td>
 * </tr>
 * <tr>
 * <td>contents</td>
 * <td>Entire contents of PDF document, indexed but not stored</td>
 * </tr>
 * <tr>
 * <td>summary</td>
 * <td>First 500 characters of content</td>
 * </tr>
 * <tr>
 * <td>modified</td>
 * <td>The modified date/time according to the url or path</td>
 * </tr>
 * <tr>
 * <td>uid</td>
 * <td>A unique identifier for the Lucene document.</td>
 * </tr>
 * <tr>
 * <td>CreationDate</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>Creator</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>Keywords</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>ModificationDate</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>Producer</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>Subject</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * <tr>
 * <td>Trapped</td>
 * <td>From PDF meta-data if available</td>
 * </tr>
 * </table>
 * 
 * @author Ben Litchfield
 * 
 */
public class LucenePDFDocument
{
    private static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

    // given caveat of increased search times when using
    // MICROSECOND, only use SECOND by default
    private static final DateTools.Resolution DATE_TIME_RES = DateTools.Resolution.SECOND;

    private PDFTextStripper stripper = null;

    /** not Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED_NOT_INDEXED = new FieldType();

    static
    {
        TYPE_STORED_NOT_INDEXED.setIndexed(false);
        TYPE_STORED_NOT_INDEXED.setStored(true);
        TYPE_STORED_NOT_INDEXED.setTokenized(true);
        TYPE_STORED_NOT_INDEXED.freeze();
    }

    /**
     * Constructor.
     */
    public LucenePDFDocument()
    {
    }

    /**
     * Set the text stripper that will be used during extraction.
     * 
     * @param aStripper The new pdf text stripper.
     */
    public void setTextStripper(PDFTextStripper aStripper)
    {
        stripper = aStripper;
    }

    private static String timeToString(long time)
    {
        return DateTools.timeToString(time, DATE_TIME_RES);
    }

    private void addKeywordField(Document document, String name, String value)
    {
        if (value != null)
        {
            document.add(new StringField(name, value, Field.Store.YES));
        }
    }

    private void addTextField(Document document, String name, Reader value)
    {
        if (value != null)
        {
            document.add(new TextField(name, value));
        }
    }

    private void addTextField(Document document, String name, String value)
    {
        if (value != null)
        {
            document.add(new TextField(name, value, Field.Store.YES));
        }
    }

    private void addTextField(Document document, String name, Date value)
    {
        if (value != null)
        {
            addTextField(document, name, DateTools.dateToString(value, DATE_TIME_RES));
        }
    }

    private void addTextField(Document document, String name, Calendar value)
    {
        if (value != null)
        {
            addTextField(document, name, value.getTime());
        }
    }

    private static void addUnindexedField(Document document, String name, String value)
    {
        if (value != null)
        {
            document.add(new Field(name, value, TYPE_STORED_NOT_INDEXED));
        }
    }

    private void addUnstoredKeywordField(Document document, String name, String value)
    {
        if (value != null)
        {
            document.add(new Field(name, value, TextField.TYPE_NOT_STORED));
        }
    }

    /**
     * Convert the PDF stream to a lucene document.
     * 
     * @param is The input stream.
     * @return The input stream converted to a lucene document.
     * @throws IOException If there is an error converting the PDF.
     */
    public Document convertDocument(InputStream is) throws IOException
    {
        Document document = new Document();
        addContent(document, is, "<inputstream>");
        return document;

    }

    /**
     * This will take a reference to a PDF document and create a lucene document.
     * 
     * @param file A reference to a PDF document.
     * @return The converted lucene document.
     * 
     * @throws IOException If there is an exception while converting the document.
     */
    public Document convertDocument(File file) throws IOException
    {
        Document document = new Document();

        // Add the url as a field named "url". Use an UnIndexed field, so
        // that the url is just stored with the document, but is not searchable.
        addUnindexedField(document, "path", file.getPath());
        addUnindexedField(document, "url", file.getPath().replace(FILE_SEPARATOR, '/'));

        // Add the last modified date of the file a field named "modified". Use a
        // Keyword field, so that it's searchable, but so that no attempt is made
        // to tokenize the field into words.
        addKeywordField(document, "modified", timeToString(file.lastModified()));

        String uid = createUID(file);

        // Add the uid as a field, so that index can be incrementally maintained.
        // This field is not stored with document, it is indexed, but it is not
        // tokenized prior to indexing.
        addUnstoredKeywordField(document, "uid", uid);

        FileInputStream input = null;
        try
        {
            input = new FileInputStream(file);
            addContent(document, input, file.getPath());
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

        // return the document

        return document;
    }

    /**
     * Convert the document from a PDF to a lucene document.
     * 
     * @param url A url to a PDF document.
     * @return The PDF converted to a lucene document.
     * @throws IOException If there is an error while converting the document.
     */
    public Document convertDocument(URL url) throws IOException
    {
        Document document = new Document();
        URLConnection connection = url.openConnection();
        connection.connect();
        // Add the url as a field named "url". Use an UnIndexed field, so
        // that the url is just stored with the document, but is not searchable.
        addUnindexedField(document, "url", url.toExternalForm());

        // Add the last modified date of the file a field named "modified". Use a
        // Keyword field, so that it's searchable, but so that no attempt is made
        // to tokenize the field into words.
        addKeywordField(document, "modified", timeToString(connection.getLastModified()));

        String uid = createUID(url, connection.getLastModified());

        // Add the uid as a field, so that index can be incrementally maintained.
        // This field is not stored with document, it is indexed, but it is not
        // tokenized prior to indexing.
        addUnstoredKeywordField(document, "uid", uid);

        InputStream input = null;
        try
        {
            input = connection.getInputStream();
            addContent(document, input, url.toExternalForm());
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

        // return the document
        return document;
    }

    /**
     * This will get a lucene document from a PDF file.
     * 
     * @param is The stream to read the PDF from.
     * 
     * @return The lucene document.
     * 
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument(InputStream is) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument(is);
    }

    /**
     * This will get a lucene document from a PDF file.
     * 
     * @param file The file to get the document for.
     * 
     * @return The lucene document.
     * 
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument(File file) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument(file);
    }

    /**
     * This will get a lucene document from a PDF file.
     * 
     * @param url The file to get the document for.
     * 
     * @return The lucene document.
     * 
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument(URL url) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument(url);
    }

    /**
     * This will add the contents to the lucene document.
     * 
     * @param document The document to add the contents to.
     * @param is The stream to get the contents from.
     * @param documentLocation The location of the document, used just for debug messages.
     * 
     * @throws IOException If there is an error parsing the document.
     */
    private void addContent(Document document, InputStream is, String documentLocation) throws IOException
    {
        PDDocument pdfDocument = null;
        try
        {
            pdfDocument = PDDocument.load(is, "");

            // create a writer where to append the text content.
            StringWriter writer = new StringWriter();
            if (stripper == null)
            {
                stripper = new PDFTextStripper();
            }
            stripper.writeText(pdfDocument, writer);

            // Note: the buffer to string operation is costless;
            // the char array value of the writer buffer and the content string
            // is shared as long as the buffer content is not modified, which will
            // not occur here.
            String contents = writer.getBuffer().toString();

            StringReader reader = new StringReader(contents);

            // Add the tag-stripped contents as a Reader-valued Text field so it will
            // get tokenized and indexed.
            addTextField(document, "contents", reader);

            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            if (info != null)
            {
                addTextField(document, "Author", info.getAuthor());
                try
                {
                    addTextField(document, "CreationDate", info.getCreationDate());
                }
                catch (IOException io)
                {
                    // ignore, bad date but continue with indexing
                }
                addTextField(document, "Creator", info.getCreator());
                addTextField(document, "Keywords", info.getKeywords());
                try
                {
                    addTextField(document, "ModificationDate", info.getModificationDate());
                }
                catch (IOException io)
                {
                    // ignore, bad date but continue with indexing
                }
                addTextField(document, "Producer", info.getProducer());
                addTextField(document, "Subject", info.getSubject());
                addTextField(document, "Title", info.getTitle());
                addTextField(document, "Trapped", info.getTrapped());
            }
            int summarySize = Math.min(contents.length(), 500);
            String summary = contents.substring(0, summarySize);
            // Add the summary as an UnIndexed field, so that it is stored and returned
            // with hit documents for display.
            addUnindexedField(document, "summary", summary);
        }
        catch (InvalidPasswordException e)
        {
            // they didn't suppply a password and the default of "" was wrong.
            throw new IOException("Error: The document(" + documentLocation + ") is encrypted and will not be indexed.");
        }
        finally
        {
            if (pdfDocument != null)
            {
                pdfDocument.close();
            }
        }
    }

    /**
     * Create an UID for the given file using the given time.
     * 
     * @param url the file we have to create an UID for
     * @param time the time to used to the UID
     * 
     * @return the created UID
     */
    public static String createUID(URL url, long time)
    {
        return url.toExternalForm().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(time);
    }

    /**
     * Create an UID for the given file.
     * 
     * @param file the file we have to create an UID for
     * 
     * @return the created UID
     */
    public static String createUID(File file)
    {
        return file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + timeToString(file.lastModified());
    }
}
