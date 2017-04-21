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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;

/**
 * This will read a document from the filesystem, decrypt it and do something with the signature.
 *
 * @author Ben Litchfield
 */
public final class ShowSignature
{
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private ShowSignature()
    {
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws IOException If there is an error reading the file.
     * @throws CertificateException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.InvalidKeyException
     * @throws java.security.NoSuchProviderException
     * @throws java.security.SignatureException
     */
    public static void main(String[] args) throws IOException, CertificateException,
                                                  NoSuchAlgorithmException, InvalidKeyException, 
                                                  NoSuchProviderException, SignatureException
    {
        ShowSignature show = new ShowSignature();
        show.showSignature( args );
    }

    private void showSignature(String[] args) throws IOException, CertificateException,
                                                     NoSuchAlgorithmException, InvalidKeyException,
                                                     NoSuchProviderException, SignatureException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            String password = args[0];
            String infile = args[1];
            try (PDDocument document = PDDocument.load(new File(infile), password))
            {
                for (PDSignature sig : document.getSignatureDictionaries())
                {
                    COSDictionary sigDict = sig.getCOSObject();
                    COSString contents = (COSString) sigDict.getDictionaryObject(COSName.CONTENTS);

                    // download the signed content
                    FileInputStream fis = new FileInputStream(infile);
                    byte[] buf = null;
                    try
                    {
                        buf = sig.getSignedContent(fis);
                    }
                    finally
                    {
                        fis.close();
                    }

                    System.out.println("Signature found");
                    System.out.println("Name:     " + sig.getName());
                    System.out.println("Modified: " + sdf.format(sig.getSignDate().getTime()));
                    String subFilter = sig.getSubFilter();
                    if (subFilter != null)
                    {
                        switch (subFilter)
                        {
                            case "adbe.pkcs7.detached":
                                verifyPKCS7(buf, contents, sig);

                                //TODO check certificate chain, revocation lists, timestamp...
                                break;
                            case "adbe.pkcs7.sha1":
                            {
                                // example: PDFBOX-1452.pdf
                                COSString certString = (COSString) sigDict.getDictionaryObject(
                                        COSName.CONTENTS);
                                byte[] certData = certString.getBytes();
                                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                                ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
                                Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                                System.out.println("certs=" + certs);
                                byte[] hash = MessageDigest.getInstance("SHA1").digest(buf);
                                verifyPKCS7(hash, contents, sig);

                                //TODO check certificate chain, revocation lists, timestamp...
                                break;
                            }
                            case "adbe.x509.rsa_sha1":
                            {
                                // example: PDFBOX-2693.pdf
                                COSString certString = (COSString) sigDict.getDictionaryObject(
                                        COSName.getPDFName("Cert"));
                                byte[] certData = certString.getBytes();
                                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                                ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
                                Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                                System.out.println("certs=" + certs);

                                //TODO verify signature
                                break;
                            }
                            default:
                                System.err.println("Unknown certificate type: " + subFilter);
                                break;
                        }
                    }
                    else
                    {
                        throw new IOException("Missing subfilter for cert dictionary");
                    }
                }
            }
            catch (CMSException | OperatorCreationException ex)
            {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Verify a PKCS7 signature.
     *
     * @param byteArray the byte sequence that has been signed
     * @param contents the /Contents field as a COSString
     * @param sig the PDF signature (the /V dictionary)
     * @throws CertificateException
     * @throws CMSException
     * @throws StoreException
     * @throws OperatorCreationException
     */
    private void verifyPKCS7(byte[] byteArray, COSString contents, PDSignature sig)
            throws CMSException, CertificateException, StoreException, OperatorCreationException
    {
        // inspiration:
        // http://stackoverflow.com/a/26702631/535646
        // http://stackoverflow.com/a/9261365/535646
        CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
        CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
        Store certificatesStore = signedData.getCertificates();
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        SignerInformation signerInformation = signers.iterator().next();
        Collection matches = certificatesStore.getMatches(signerInformation.getSID());
        X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        System.out.println("certFromSignedData: " + certFromSignedData);
        certFromSignedData.checkValidity(sig.getSignDate().getTime());
        
        if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certFromSignedData)))
        {
            System.out.println("Signature verified");
        }
        else
        {
            System.out.println("Signature verification failed");
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java " + ShowSignature.class.getName() +
                            "<password> <inputfile>" );
    }
}
