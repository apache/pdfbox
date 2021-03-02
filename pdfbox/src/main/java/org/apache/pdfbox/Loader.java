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

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.FDFParser;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
        return Loader.loadFDF(new File(filename));
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
        try (RandomAccessRead readBuffer = new RandomAccessReadBufferedFile(file))
        {
            FDFParser parser = new FDFParser(readBuffer);
            return parser.parse();
        }
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
        try (RandomAccessRead readBuffer = new RandomAccessReadBuffer(input))
        {
            FDFParser parser = new FDFParser(readBuffer);
            return parser.parse();
        }
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
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input) throws IOException
    {
        return Loader.loadPDF(input, "");
    }
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password) throws IOException
    {
        return Loader.loadPDF(input, password, null, null);
    }
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return Loader.loadPDF(input, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }
    /**
     * Parses a PDF.
     * 
     * @param input byte array that contains the document.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering input stream and PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password, InputStream keyStore, String alias,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        RandomAccessRead source = null;
        try
        {
            // RandomAccessRead is not closed here, may be needed for signing
            source = new RandomAccessReadBuffer(input);
            PDFParser parser = new PDFParser(source, password, keyStore, alias, memUsageSetting);
            return parser.parse();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(source);
            throw ioe;
        }
    }
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file) throws IOException
    {
        return Loader.loadPDF(file, "", MemoryUsageSetting.setupMainMemoryOnly());
    }
    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, MemoryUsageSetting memUsageSetting) throws IOException
    {
        return Loader.loadPDF(file, "", null, null, memUsageSetting);
    }
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password) throws IOException
    {
        return Loader.loadPDF(file, password, null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }
    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        return Loader.loadPDF(file, password, null, null, memUsageSetting);
    }
    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return Loader.loadPDF(file, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }
    /**
     * Parses a PDF.
     * 
     * @param file file to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password, InputStream keyStore, String alias,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        RandomAccessRead raFile = null;
        try
        {
            // RandomAccessRead is not closed here, may be needed for signing
            raFile = new RandomAccessReadBufferedFile(file);
            return Loader.loadPDF(raFile, password, keyStore, alias, memUsageSetting);
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(raFile);
            throw ioe;
        }
    }

    /**
     * Parses a PDF.
     * 
     * @param raFile random access read representing the pdf to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used PDF streams
     * 
     * @return loaded document
     * 
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(RandomAccessRead raFile, String password,
            InputStream keyStore, String alias, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        PDFParser parser = new PDFParser(raFile, password, keyStore, alias, memUsageSetting);
        return parser.parse();
    }
    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     * 
     * @param input stream that contains the document. Don't forget to close it after loading.
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input) throws IOException
    {
        return Loader.loadPDF(input, "", null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }
    
    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either copied to main memory
     * or to a temporary file to enable random access to the pdf.
     * 
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input, MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        return Loader.loadPDF(input, "", null, null, memUsageSetting);
    }
    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     *
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input, String password) throws IOException
    {
        return Loader.loadPDF(input, password, null, null, MemoryUsageSetting.setupMainMemoryOnly());
    }
    /**
     * Parses a PDF. The given input stream is copied to the memory to enable random access to the pdf. Unrestricted
     * main memory will be used for buffering PDF streams.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * 
     * @return loaded document
     * 
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input, String password, InputStream keyStore,
            String alias) throws IOException
    {
        return Loader.loadPDF(input, password, keyStore, alias, MemoryUsageSetting.setupMainMemoryOnly());
    }
    
    /**
     * Parses a PDF. Depending on the memory settings parameter the given input stream is either copied to main memory
     * or to a temporary file to enable random access to the pdf.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input, String password,
            MemoryUsageSetting memUsageSetting) throws IOException
    {
        return Loader.loadPDF(input, password, null, null, memUsageSetting);
    }
    
    /**
     * Parses a PDF. The given input stream is copied to memory to enable random access to the pdf.
     *
     * @param input stream that contains the document. Don't forget to close it after loading.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     * @return loaded document
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(InputStream input, String password, InputStream keyStore,
            String alias, MemoryUsageSetting memUsageSetting) throws IOException
    {
        RandomAccessRead source = null;
        try
        {
            // RandomAccessRead is not closed here, may be needed for signing
            source = new RandomAccessReadBuffer(input);
            PDFParser parser = new PDFParser(source, password, keyStore, alias, memUsageSetting);
            return parser.parse();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(source);
            throw ioe;
        }
    }

}
