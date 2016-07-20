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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This is an example for visual signing a pdf with bouncy castle.

 * @see CreateSignature
 * @author Vakhtang Koroghlishvili
 */
public class CreateVisibleSignature extends CreateSignatureBase
{
    private static final BouncyCastleProvider BCPROVIDER = new BouncyCastleProvider();

    private SignatureOptions signatureOptions;
    private PDVisibleSignDesigner visibleSignDesigner;
    private final PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();

    public void setvisibleSignDesigner(String filename, int x, int y, int zoomPercent, 
            FileInputStream imageStream, int page) 
            throws IOException
    {
        visibleSignDesigner = new PDVisibleSignDesigner(filename, imageStream, page);
        visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent);
    }
    
    public void setVisibleSignatureProperties(String name, String location, String reason, int preferredSize, 
            int page, boolean visualSignEnabled) throws IOException
    {
        visibleSignatureProperties.signerName(name).signerLocation(location).signatureReason(reason).
                preferredSize(preferredSize).page(page).visualSignEnabled(visualSignEnabled).
                setPdVisibleSignature(visibleSignDesigner).buildSignature();
    }

    /**
     * Initialize the signature creator with a keystore (pkcs12) and pin that
     * should be used for the signature.
     *
     * @param keystore is a pkcs12 keystore.
     * @param pin is the pin for the keystore / private key
     * @throws KeyStoreException if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     * @throws CertificateException if the certificate is not valid as signing time
     * @throws IOException if no certificate could be found
     */
    public CreateVisibleSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException
    {
        super(keystore, pin);
    }

    /**
     * Sign pdf file and create new file that ends with "_signed.pdf".
     *
     * @param inputFile The source pdf document file.
     * @param signedFile The file to be signed.
     * @param tsaClient optional TSA client
     * @throws IOException
     */
    public void signPDF(File inputFile, File signedFile, TSAClient tsaClient) throws IOException
    {
        setTsaClient(tsaClient);

        if (inputFile == null || !inputFile.exists())
        {
            throw new IOException("Document for signing does not exist");
        }

        // creating output document and prepare the IO streams.
        FileOutputStream fos = new FileOutputStream(signedFile);

        // load document
        PDDocument doc = PDDocument.load(inputFile);

        // create signature dictionary
        PDSignature signature = new PDSignature();

        // default filter
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        
        // subfilter for basic and PAdES Part 2 signatures
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        
        if (visibleSignatureProperties != null)
        {
            signature.setName(visibleSignatureProperties.getSignerName());
            signature.setLocation(visibleSignatureProperties.getSignerLocation());
            signature.setReason(visibleSignatureProperties.getSignatureReason());
        }

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled())
        {
            signatureOptions = new SignatureOptions();
            signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
            signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
            doc.addSignature(signature, this, signatureOptions);
        }
        else
        {
            doc.addSignature(signature, this);
        }

        // write incremental (only for signing purpose)
        doc.saveIncremental(fos);
        doc.close();
        
        // do not close options before saving, because some COSStream objects within options 
        // are transferred to the signed document.
        IOUtils.closeQuietly(signatureOptions);
    }

    /**
     * Arguments are
     * [0] key store
     * [1] pin
     * [2] document that will be signed
     * [3] image of visible signature
     * @param args
     * @throws java.security.KeyStoreException
     * @throws java.security.cert.CertificateException
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.UnrecoverableKeyException
     */
    public static void main(String[] args) throws KeyStoreException, CertificateException,
            IOException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        // generate with
        // keytool -storepass 123456 -storetype PKCS12 -keystore file.p12 -genkey -alias client -keyalg RSA
        if (args.length < 4)
        {
            usage();
            System.exit(1);
        }

        String tsaUrl = null;
        for(int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-tsa"))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                tsaUrl = args[i];
            }
        }

        File ksFile = new File(args[0]);
        KeyStore keystore = KeyStore.getInstance("PKCS12", BCPROVIDER);
        char[] pin = args[1].toCharArray();
        keystore.load(new FileInputStream(ksFile), pin);

        // TSA client
        TSAClient tsaClient = null;
        if (tsaUrl != null)
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        }

        File documentFile = new File(args[2]);

        CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());

        FileInputStream imageStream = new FileInputStream(args[3]);

        String name = documentFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));
        File signedDocumentFile = new File(documentFile.getParent(), substring + "_signed.pdf");

        // page is 1-based here
        int page = 1;
        signing.setvisibleSignDesigner (args[2], 0, 0, -50, imageStream, page);
        imageStream.close();
        signing.setVisibleSignatureProperties ("name", "location", "Security", 0, page, true);
        signing.signPDF(documentFile, signedDocumentFile, tsaClient);
    }

    /**
     * This will print the usage for this program.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + CreateVisibleSignature.class.getName()
                + " <pkcs12-keystore-file> <pin> <input-pdf> <sign-image>\n" + "" +
                           "options:\n" +
                           "  -tsa <url>    sign timestamp using the given TSA server");
    }
}
