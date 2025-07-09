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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfwriter.COSWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the in-memory representation of the FDF document. You need to call close() on this object when you are done
 * using it!!
 *
 * @author Ben Litchfield
 */
public class FDFDocument implements Closeable
{
    private static final Logger LOG = LogManager.getLogger(FDFDocument.class);

    private final COSDocument document;

    private final RandomAccessRead fdfSource;

    /**
     * Constructor, creates a new FDF document.
     *
     */
    public FDFDocument()
    {
        fdfSource = null;
        document = new COSDocument();
        document.getDocumentState().setParsing(false);
        document.setVersion(1.2f);

        // First we need a trailer
        document.setTrailer(new COSDictionary());

        // Next we need the root dictionary.
        FDFCatalog catalog = new FDFCatalog();
        setCatalog(catalog);
    }

    /**
     * Constructor that uses an existing document. The COSDocument that is passed in must be valid.
     *
     * @param doc The COSDocument that this document wraps.
     * @param source The source that will be closed when this document gets closed, can be null.
     */
    public FDFDocument(COSDocument doc, RandomAccessRead source)
    {
        document = doc;
        document.getDocumentState().setParsing(false);
        fdfSource = source;
    }

    /**
     * This will create an FDF document from an XFDF XML document.
     *
     * @param doc The XML document that contains the XFDF data.
     * @throws IOException If there is an error reading from the dom.
     */
    public FDFDocument(Document doc) throws IOException
    {
        this();
        Element xfdf = doc.getDocumentElement();
        if (!xfdf.getNodeName().equals("xfdf"))
        {
            throw new IOException("Error while importing xfdf document, "
                    + "root should be 'xfdf' and not '" + xfdf.getNodeName() + "'");
        }
        FDFCatalog cat = new FDFCatalog(xfdf);
        setCatalog(cat);
    }

    /**
     * This will write this element as an XML document.
     *
     * @param output The stream to write the xml to.
     *
     * @throws IOException If there is an error writing the XML.
     */
    public void writeXML(Writer output) throws IOException
    {
        output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        output.write("<xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:space=\"preserve\">\n");

        getCatalog().writeXML(output);

        output.write("</xfdf>\n");
    }

    /**
     * This will get the low level document.
     *
     * @return The document that this layer sits on top of.
     */
    public COSDocument getDocument()
    {
        return document;
    }

    /**
     * This will get the FDF Catalog. This is guaranteed to not return null.
     *
     * @return The documents /Root dictionary
     */
    public FDFCatalog getCatalog()
    {
        FDFCatalog retval;
        COSDictionary trailer = document.getTrailer();
        COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
        if (root == null)
        {
            retval = new FDFCatalog();
            setCatalog(retval);
        }
        else
        {
            retval = new FDFCatalog(root);
        }
        return retval;
    }

    /**
     * This will set the FDF catalog for this FDF document.
     *
     * @param cat The FDF catalog.
     */
    public final void setCatalog(FDFCatalog cat)
    {
        COSDictionary trailer = document.getTrailer();
        trailer.setItem(COSName.ROOT, cat);
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     */
    public void save(File fileName) throws IOException
    {
        try (FileOutputStream fos = new FileOutputStream(fileName))
        {
            save(fos);
        }
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     */
    public void save(String fileName) throws IOException
    {
        save(new File(fileName));
    }

    /**
     * This will save the document to an output stream.
     *
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing the document.
     */
    public void save(OutputStream output) throws IOException
    {
        COSWriter writer = new COSWriter(output);
        writer.write(this);
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     */
    public void saveXFDF(File fileName) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)))
        {
            saveXFDF(writer);
        }
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     */
    public void saveXFDF(String fileName) throws IOException
    {
        saveXFDF(new File(fileName));
    }

    /**
     * This will save the document to an output stream and close the stream.
     *
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing the document.
     */
    public void saveXFDF(Writer output) throws IOException
    {
        try
        {
            writeXML(output);
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
        }
    }

    /**
     * This will close the underlying COSDocument object.
     *
     * @throws IOException If there is an error releasing resources.
     */
    @Override
    public void close() throws IOException
    {
        if (!document.isClosed())
        {
            IOException firstException = null;

            // close all intermediate I/O streams
            firstException = IOUtils.closeAndLogException(document, LOG, "COSDocument", firstException);

            // close the source PDF stream, if we read from one
            if (fdfSource != null)
            {
                firstException = IOUtils.closeAndLogException(fdfSource, LOG, "RandomAccessRead pdfSource", firstException);
            }

            // rethrow first exception to keep method contract
            if (firstException != null)
            {
                throw firstException;
            }
        }
    }
}
