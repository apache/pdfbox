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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

/**
 * An example for timestamp-signing a PDF for PADeS-Specification. The document will be extended by
 * a signed TimeStamp (another kind of signature) (Signed TimeStamp and Hash-Value of the document
 * are signed by a Time Stamp Authority (TSA)).
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 * @author Alexis Suter
 */
public class CreateSignedTimeStamp implements SignatureInterface
{
    private static final Log LOG = LogFactory.getLog(CreateSignedTimeStamp.class);
    
    private final String tsaUrl;

    /**
     * Initialize the signed timestamp creator
     * 
     * @param tsaUrl The url where TS-Request will be done.
     */
    public CreateSignedTimeStamp(String tsaUrl)
    {
        this.tsaUrl = tsaUrl;
    }

    /**
     * Signs the given PDF file. Alters the original file on disk.
     * 
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException
    {
        signDetached(file, file);
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
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // sign
        PDDocument doc = PDDocument.load(inFile);
        signDetached(doc, fos);
        doc.close();
        fos.close();
    }

    /**
     * Prepares the TimeStamp-Signature and starts the saving-process.
     * 
     * @param document given Pdf
     * @param output Where the file will be written
     * @throws IOException
     */
    public void signDetached(PDDocument document, OutputStream output) throws IOException
    {
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

    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        ValidationTimeStamp validation;
        try
        {
            validation = new ValidationTimeStamp(tsaUrl);
            return validation.getTimeStampToken(content);
        }
        catch (NoSuchAlgorithmException e)
        {
            LOG.error("Hashing-Algorithm not found for TimeStamping", e);
        }
        return new byte[] {};
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

        // sign PDF
        CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsaUrl);

        File inFile = new File(args[0]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
        signing.signDetached(inFile, outFile);
    }

    private static void usage()
    {
        System.err.println("usage: java " + CreateSignedTimeStamp.class.getName() + " "
                + "<pdf_to_sign>\n" + "mandatory options:\n"
                + "  -tsa <url>    sign timestamp using the given TSA server\n");
    }
}
