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

package org.apache.pdfbox.examples.signature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URISyntaxException;

import java.nio.file.Files;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Hex;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

/**
 * An example for timestamp-signing a PDF for PADeS-Specification. The document will only be changed
 * in its existing signature by a signed timestamp (A timestamp and the Hash-Value of the document
 * are signed by a Time Stamp Authority (TSA)).
 *
 * This method only changes the unsigned parameters of a signature, so that it is kept valid.
 *
 * Use case: sign offline to avoid zero-day attacks against the signing machine. Once the signature
 * is there and the pdf is transferred to a network connected machine, one is likely to want to add
 * a timestamp. (Ralf Hauser)
 *
 * @author Alexis Suter
 */
public class CreateEmbeddedTimeStamp
{
    private final String tsaUrl;
    private PDDocument document;
    private PDSignature signature;
    private byte[] changedEncodedSignature;

    public CreateEmbeddedTimeStamp(String tsaUrl)
    {
        this.tsaUrl = tsaUrl;
    }

    /**
     * Embeds the given PDF file with signed timestamp(s). Alters the original file on disk.
     * 
     * @param file the PDF file to sign and to overwrite
     * @throws IOException
     * @throws URISyntaxException
     */
    public void embedTimeStamp(File file) throws IOException, URISyntaxException
    {
        embedTimeStamp(file, file);
    }

    /**
     * Embeds signed timestamp(s) into existing signatures of the given document
     * 
     * @param inFile The pdf file possibly containing signatures
     * @param outFile Where the changed document will be saved
     * @throws IOException
     * @throws URISyntaxException
     */
    public void embedTimeStamp(File inFile, File outFile) throws IOException, URISyntaxException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        // sign
        try (PDDocument doc = Loader.loadPDF(inFile))
        {
            document = doc;
            processTimeStamping(inFile, outFile);
        }
    }

    /**
     * Processes the time-stamping of the signature.
     * 
     * @param inFile The existing PDF file
     * @param outFile Where the new file will be written to
     * @throws IOException
     * @throws URISyntaxException
     */
    private void processTimeStamping(File inFile, File outFile) throws IOException, URISyntaxException
    {
        try
        {
            byte[] documentBytes = Files.readAllBytes(inFile.toPath());
            processRelevantSignatures(documentBytes);

            if (changedEncodedSignature == null)
            {
                throw new IllegalStateException("No signature");
            }
            try (FileOutputStream output = new FileOutputStream(outFile))
            {
                embedNewSignatureIntoDocument(documentBytes, output);
            }
        }
        catch (IOException | NoSuchAlgorithmException | CMSException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Create changed Signature with embedded TimeStamp from TSA
     * 
     * @param documentBytes byte[] of the input file
     * @throws IOException
     * @throws CMSException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    private void processRelevantSignatures(byte[] documentBytes)
            throws IOException, CMSException, NoSuchAlgorithmException, URISyntaxException
    {
        signature = SigUtils.getLastRelevantSignature(document);
        if (signature == null)
        {
            return;
        }

        byte[] sigBlock = signature.getContents(documentBytes);
        CMSSignedData signedData = new CMSSignedData(sigBlock);

        System.out.println("INFO: Byte Range: " + Arrays.toString(signature.getByteRange()));

        if (tsaUrl != null && tsaUrl.length() > 0)
        {
            ValidationTimeStamp validation = new ValidationTimeStamp(tsaUrl);
            signedData = validation.addSignedTimeStamp(signedData);
        }

        byte[] newEncoded = Hex.getBytes(signedData.getEncoded());
        int maxSize = signature.getByteRange()[2] - signature.getByteRange()[1];
        System.out.println(
                "INFO: New Signature has Size: " + newEncoded.length + " maxSize: " + maxSize);

        if (newEncoded.length > maxSize - 2)
        {
            throw new IOException(
                    "New Signature is too big for existing Signature-Placeholder. Max Place: "
                    + maxSize);
        }
        else
        {
            changedEncodedSignature = newEncoded;
        }
    }

    /**
     * Embeds the new signature into the document, by copying the rest of the document
     * 
     * @param docBytes byte array of the document
     * @param output target, where the file will be written
     * @throws IOException
     */
    private void embedNewSignatureIntoDocument(byte[] docBytes, OutputStream output)
            throws IOException
    {
        int[] byteRange = signature.getByteRange();
        output.write(docBytes, byteRange[0], byteRange[1] + 1);
        output.write(changedEncodedSignature);
        int addingLength = byteRange[2] - byteRange[1] - 2 - changedEncodedSignature.length;
        byte[] zeroes = Hex.getBytes(new byte[(addingLength + 1) / 2]);
        output.write(zeroes);
        output.write(docBytes, byteRange[2] - 1, byteRange[3] + 1);
    }

    public static void main(String[] args) throws IOException, URISyntaxException
    {
        if (args.length != 3)
        {
            usage();
            System.exit(1);
        }

        String tsaUrl = null;
        for (int i = 0; i < args.length; i++)
        {
            if ("-tsa".equals(args[i]))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                    System.exit(1);
                }
                tsaUrl = args[i];
            }
        }

        File inFile = new File(args[0]);
        System.out.println("Input File: " + args[0]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_eTs.pdf");
        System.out.println("Output File: " + outFile.getAbsolutePath());

        // Embed TimeStamp
        CreateEmbeddedTimeStamp signing = new CreateEmbeddedTimeStamp(tsaUrl);
        signing.embedTimeStamp(inFile, outFile);
    }

    private static void usage()
    {
        System.err.println("usage: java " + CreateEmbeddedTimeStamp.class.getName() + " "
                + "<pdf_to_sign>\n" + "mandatory option:\n"
                + "  -tsa <url>    sign timestamp using the given TSA server\n");
    }
}
