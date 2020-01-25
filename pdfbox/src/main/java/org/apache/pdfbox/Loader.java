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

package org.apache.pdfbox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdfparser.FDFParser;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.util.XMLUtil;

/**
 * Utility methods to load different types of documents
 *
 */
public class Loader
{

    private Loader()
    {
    }
    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(String filename) throws IOException
    {
        FDFParser parser = new FDFParser(filename);
        return parser.parse();
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(File file) throws IOException
    {
        FDFParser parser = new FDFParser(file);
        return parser.parse();
    }

    /**
     * This will load a document from an input stream.
     *
     * @param input The stream that contains the document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(InputStream input) throws IOException
    {
        FDFParser parser = new FDFParser(input);
        return parser.parse();
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(String filename) throws IOException
    {
        return Loader.loadXFDF(new BufferedInputStream(new FileInputStream(filename)));
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(File file) throws IOException
    {
        return Loader.loadXFDF(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * This will load a document from an input stream.
     *
     * @param input The stream that contains the document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(InputStream input) throws IOException
    {
        return new FDFDocument(XMLUtil.parse(input));
    }

}
