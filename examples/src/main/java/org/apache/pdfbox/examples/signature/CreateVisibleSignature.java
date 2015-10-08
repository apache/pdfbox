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
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * This is an example for visual signing a pdf with bouncy castle.

 * @see CreateSignature
 * @author Vakhtang Koroghlishvili
 */
public class CreateVisibleSignature implements SignatureInterface
{
    private static final BouncyCastleProvider BCPROVIDER = new BouncyCastleProvider();

    private final PrivateKey privKey;
    private final Certificate[] cert;
    private SignatureOptions options;

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
        privKey = (PrivateKey) keystore.getKey(alias, pin);
        cert = keystore.getCertificateChain(alias);
    }

    /**
     * Sign pdf file and create new file that ends with "_signed.pdf".
     *
     * @param documentFile The source pdf document file.
     * @param signatureProperties The signature properties.
     * @return the signed pdf document file.
     * @throws IOException
     */
    public File signPDF(File documentFile, PDVisibleSigProperties signatureProperties) throws IOException
    {
        if (documentFile == null || !documentFile.exists())
        {
            throw new IOException("Document for signing does not exist");
        }

        // creating output document and prepare the IO streams.
        String name = documentFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outputDocumentFile = new File(documentFile.getParent(), substring + "_signed.pdf");
        FileOutputStream fos = new FileOutputStream(outputDocumentFile);

        // load document
        PDDocument doc = PDDocument.load(documentFile);

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
        if (signatureProperties != null && signatureProperties.isVisualSignEnabled())
        {
            options = new SignatureOptions();
            options.setVisualSignature(signatureProperties);
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

        return outputDocumentFile;
    }

    /**
     * SignatureInterface implementation.
     *
     * This method will be called from inside of the pdfbox and create the pkcs7 signature.
     * The given InputStream contains the bytes that are given by the byte range.
     *
     * This method is for internal use only. <-- TODO this method should be private
     *
     * Use your favorite cryptographic library to implement pkcs7 signature creation.
     */
    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        try
        {
            List<Certificate> certList = new ArrayList<Certificate>();
            certList.add(cert[0]);
            Store certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            org.bouncycastle.asn1.x509.Certificate certificate =
                    org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(cert[0].getEncoded()));
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(certificate)));
            gen.addCertificates(certs);
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);
            return signedData.getEncoded();
        }
        catch (CertificateEncodingException e)
        {
            throw new IOException(e);
        }
        catch (CMSException e)
        {
            throw new IOException(e);
        }
        catch (OperatorCreationException e)
        {
            throw new IOException(e);
        }
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

            File document = new File(args[2]);

            CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());

            FileInputStream image = new FileInputStream(args[3]);

            PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(args[2], image, 1);
            visibleSig.xAxis(0).yAxis(0).zoom(-50).signatureFieldName("signature");

            PDVisibleSigProperties signatureProperties = new PDVisibleSigProperties();

            signatureProperties.signerName("name").signerLocation("location").signatureReason("Security").preferredSize(0)
                    .page(1).visualSignEnabled(true).setPdVisibleSignature(visibleSig).buildSignature();

            signing.signPDF(document, signatureProperties);
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
