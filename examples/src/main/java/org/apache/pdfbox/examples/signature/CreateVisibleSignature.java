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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Enumeration;
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

    private SignatureOptions options;
    private PDVisibleSignDesigner visibleSignDesigner;
    private final PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();

    public void setVisibleSignatureProperties(String filename, int x, int y, int zoomPercent, 
            FileInputStream image, int page) 
            throws IOException
    {
        visibleSignDesigner = new PDVisibleSignDesigner(filename, image, page);
        visibleSignDesigner.xAxis(x).yAxis(y).zoom(zoomPercent).signatureFieldName("signature");
    }
    
    public void setSignatureProperties(String name, String location, String reason, int preferredSize, 
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
     */
    public CreateVisibleSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException
    {
        // grabs the first alias from the keystore and get the private key. An
        // alternative method or constructor could be used for setting a specific
        // alias that should be used.
        Enumeration<String> aliases = keystore.aliases();
        String alias = null;
        if (aliases.hasMoreElements())
        {
            alias = aliases.nextElement();
        }
        else
        {
            throw new IOException("Could not find alias");
        }
        setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
        setCertificate(keystore.getCertificateChain(alias)[0]);
    }

    /**
     * Sign pdf file and create new file that ends with "_signed.pdf".
     *
     * @param inputFile The source pdf document file.
     * @param signedFile The file to be signed.
     * @throws IOException
     */
    public void signPDF(File inputFile, File signedFile) throws IOException
    {
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
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
        // subfilter for basic and PAdES Part 2 signatures
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("signer name");
        signature.setLocation("signer location");
        signature.setReason("reason for signature");

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        if (visibleSignatureProperties != null && visibleSignatureProperties.isVisualSignEnabled())
        {
            options = new SignatureOptions();
            options.setVisualSignature(visibleSignatureProperties);
            doc.addSignature(signature, this, options);
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
        IOUtils.closeQuietly(options);
    }

    /**
     * Arguments are
     * [0] key store
     * [1] pin
     * [2] document that will be signed
     * [3] image of visible signature
     */
    public static void main(String[] args) throws KeyStoreException, CertificateException,
            IOException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        if (args.length != 4)
        {
            usage();
            System.exit(1);
        }
        else
        {
            File ksFile = new File(args[0]);
            KeyStore keystore = KeyStore.getInstance("PKCS12", BCPROVIDER);
            char[] pin = args[1].toCharArray();
            keystore.load(new FileInputStream(ksFile), pin);

            File documentFile = new File(args[2]);

            CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());

            FileInputStream image = new FileInputStream(args[3]);
            
            String name = documentFile.getName();
            String substring = name.substring(0, name.lastIndexOf('.'));
            File signedDocumentFile = new File(documentFile.getParent(), substring + "_signed.pdf");

            signing.setVisibleSignatureProperties (args[2], 0, 0, -50, image, 1);
            signing.setSignatureProperties ("name", "location", "Security", 0, 1, true);
            signing.signPDF(documentFile, signedDocumentFile);
        }
    }

    /**
     * This will print the usage for this program.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + CreateVisibleSignature.class.getName()
                + " <pkcs12-keystore-file> <pin> <input-pdf> <sign-image>");
    }
}
