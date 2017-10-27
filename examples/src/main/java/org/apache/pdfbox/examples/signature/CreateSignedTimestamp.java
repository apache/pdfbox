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
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

/**
 * An example for timestamp-singing a PDF for PADeS-Specification. The document will only be
 * extended by a signed Timestamp (Signed Timestamp and Hash-Value of the document are signed by a
 * Time Stamp Authority (TSA)).
 *
 * @author Alexis Suter
 */
public class CreateSignedTimestamp extends CreateSignedTimestampBase
{

    /**
     * Initialize the signed timestamp creator
     */
    public CreateSignedTimestamp()
    {
    }

    /**
     * Signs the given PDF file. Alters the original file on disk.
     * 
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException
    {
        signDetached(file, file, null);
    }

    /**
     * Signs the given PDF file.
     * 
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException
    {
        signDetached(inFile, outFile, null);
    }

    /**
     * Signs the given PDF file.
     * 
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @param tsaClient optional TSA client
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile, TSAClient tsaClient) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // sign
        try (PDDocument doc = PDDocument.load(inFile))
        {
            signDetached(doc, fos, tsaClient);
        }
    }

    public void signDetached(PDDocument document, OutputStream output, TSAClient tsaClient)
            throws IOException
    {
        setTsaClient(tsaClient);

        int accessPermissions = SigUtils.getMDPPermission(document);
        if (accessPermissions == 1)
        {
            throw new IllegalStateException(
                    "No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setType(COSName.DOC_TIME_STAMP);
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(COSName.getPDFName("ETSI.RFC3161"));

        // No certification allowed because /Reference not allowed in signature directory
        // see ETSI EN 319 142-1 Part 1 and ETSI TS 102 778-4
        // http://www.etsi.org/deliver/etsi_en%5C319100_319199%5C31914201%5C01.01.00_30%5Cen_31914201v010100v.pdf
        // http://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.01_60/ts_10277804v010101p.pdf

        // register signature dictionary and sign interface
        document.addSignature(signature, this);

        // write incremental (only for signing purpose)
        document.saveIncremental(output);
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        if (args.length != 3)
        {
            usage();
            System.exit(1);
        }

        String tsaUrl = null;
        if (args[1].equals("-tsa"))
        {
            tsaUrl = args[2];
        }
        else
        {
            usage();
            System.exit(1);
        }

        // TSA client
        TSAClient tsaClient = null;
        if (tsaUrl != null)
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        }

        // sign PDF
        CreateSignedTimestamp signing = new CreateSignedTimestamp();

        File inFile = new File(args[0]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
        signing.signDetached(inFile, outFile, tsaClient);
    }

    private static void usage()
    {
        System.err.println("usage: java " + CreateSignedTimestamp.class.getName() + " "
                + "<pdf_to_sign>\n" + "options:\n"
                + "  -tsa <url>    sign timestamp using the given TSA server\n");
    }
}
