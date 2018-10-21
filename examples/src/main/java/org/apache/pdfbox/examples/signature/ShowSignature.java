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
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;

/**
 * This will get the signature(s) from the document, do some verifications and
 * show the signature(s) and the certificates. This is a complex topic - the
 * code here is an example and not a production-ready solution.
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
     * @throws java.security.NoSuchProviderException
     * @throws org.bouncycastle.tsp.TSPException
     */
    public static void main(String[] args) throws IOException, CertificateException,
                                                  NoSuchAlgorithmException,
                                                  NoSuchProviderException,
                                                  TSPException
    {
        // register BouncyCastle provider, needed for "exotic" algorithms
        Security.addProvider(SecurityProvider.getProvider());

        ShowSignature show = new ShowSignature();
        show.showSignature( args );
    }

    private void showSignature(String[] args) throws IOException, CertificateException,
                                                     NoSuchAlgorithmException,
                                                     NoSuchProviderException,
                                                     TSPException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            String password = args[0];
            File infile = new File(args[1]);
            PDDocument document = null;
            try
            {
                document = PDDocument.load(infile, password);
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

                    int[] byteRange = sig.getByteRange();
                    if (byteRange.length != 4)
                    {
                        System.err.println("Signature byteRange must have 4 items");
                    }
                    else
                    {
                        long fileLen = infile.length();
                        long rangeMax = byteRange[2] + (long) byteRange[3];
                        // multiply content length with 2 (because it is in hex in the PDF) and add 2 for < and >
                        int contentLen = contents.getString().length() * 2 + 2;
                        if (fileLen != rangeMax || byteRange[0] != 0 || byteRange[1] + contentLen != byteRange[2])
                        {
                            // a false result doesn't necessarily mean that the PDF is a fake
                            // see this answer why:
                            // https://stackoverflow.com/a/48185913/535646
                            System.out.println("Signature does not cover whole document");
                        }
                        else
                        {
                            System.out.println("Signature covers whole document");
                        }
                    }

                    if (sig.getName() != null)
                    {
                        System.out.println("Name:     " + sig.getName());
                    }
                    if (sig.getSignDate() != null)
                    {
                        System.out.println("Modified: " + sdf.format(sig.getSignDate().getTime()));
                    }
                    String subFilter = sig.getSubFilter();
                    if (subFilter != null)
                    {
                        if (subFilter.equals("adbe.pkcs7.detached") || 
                            subFilter.equals("ETSI.CAdES.detached"))
                        {
                            verifyPKCS7(buf, contents, sig);
                            
                            //TODO check certificate chain, revocation lists, timestamp...
                        }
                        else if (subFilter.equals("adbe.pkcs7.sha1"))
                        {
                            // example: PDFBOX-1452.pdf
                            byte[] certData = contents.getBytes();
                            CertificateFactory factory = CertificateFactory.getInstance("X.509");
                            ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
                            Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                            System.out.println("certs=" + certs);

                            byte[] hash = MessageDigest.getInstance("SHA1").digest(buf);
                            verifyPKCS7(hash, contents, sig);
                            
                            //TODO check certificate chain, revocation lists, timestamp...
                        }
                        else if (subFilter.equals("adbe.x509.rsa_sha1"))
                        {
                            // example: PDFBOX-2693.pdf
                            COSString certString = (COSString) sigDict.getDictionaryObject(COSName.CERT);
                            if (certString == null)
                            {
                                System.err.println("The /Cert certificate string is missing in the signature dictionary");
                                return;
                            }
                            byte[] certData = certString.getBytes();
                            CertificateFactory factory = CertificateFactory.getInstance("X.509");
                            ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
                            Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                            System.out.println("certs=" + certs);
                            
                            // to verify signature, see code at
                            // https://stackoverflow.com/questions/43383859/
                        }
                        else if (subFilter.equals("ETSI.RFC3161"))
                        {
                            TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents.getBytes()));
                            System.out.println("Time stamp gen time: " + timeStampToken.getTimeStampInfo().getGenTime());
                            System.out.println("Time stamp tsa name: " + timeStampToken.getTimeStampInfo().getTsa().getName());

                            CertificateFactory factory = CertificateFactory.getInstance("X.509");
                            ByteArrayInputStream certStream = new ByteArrayInputStream(contents.getBytes());
                            Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                            System.out.println("certs=" + certs);

                            //TODO verify signature
                        }
                        else
                        {
                            System.err.println("Unknown certificate type: " + subFilter);
                        }
                    }
                    else
                    {
                        throw new IOException("Missing subfilter for cert dictionary");
                    }
                }
                analyseDSS(document);
            }
            catch (CMSException ex)
            {
                throw new IOException(ex);
            }
            catch (OperatorCreationException ex)
            {
                throw new IOException(ex);
            }
            finally
            {
                if (document != null)
                {
                    document.close();
                }
            }
            System.out.println("Analyzed: " + args[1]);
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
     * @throws IOException
     */
    private void verifyPKCS7(byte[] byteArray, COSString contents, PDSignature sig)
            throws CMSException, CertificateException, StoreException, OperatorCreationException,
                   NoSuchAlgorithmException, NoSuchProviderException, TSPException, IOException
    {
        // inspiration:
        // http://stackoverflow.com/a/26702631/535646
        // http://stackoverflow.com/a/9261365/535646
        CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
        CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
        @SuppressWarnings("unchecked")
        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        SignerInformation signerInformation = signers.iterator().next();
        @SuppressWarnings("unchecked")
        Collection<X509CertificateHolder> matches =
                certificatesStore.getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
        X509CertificateHolder certificateHolder = matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        System.out.println("certFromSignedData: " + certFromSignedData);

        SigUtils.checkCertificateUsage(certFromSignedData);

        if (signerInformation.getUnsignedAttributes() != null)
        {            
            AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();

            // https://stackoverflow.com/questions/1647759/how-to-validate-if-a-signed-jar-contains-a-timestamp
            Attribute attribute = unsignedAttributes.get(
                    PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            ASN1Object obj = (ASN1Object) attribute.getAttrValues().getObjectAt(0);
            CMSSignedData signedTSTData = new CMSSignedData(obj.getEncoded());
            TimeStampToken timeStampToken = new TimeStampToken(signedTSTData);

            // https://stackoverflow.com/questions/42114742/
            Collection<X509CertificateHolder> tstMatches =
                    timeStampToken.getCertificates().getMatches(timeStampToken.getSID());
            X509CertificateHolder holder = tstMatches.iterator().next();
            X509Certificate tstCert = new JcaX509CertificateConverter().getCertificate(holder);
            SignerInformationVerifier siv = new JcaSimpleSignerInfoVerifierBuilder().setProvider(SecurityProvider.getProvider()).build(tstCert);
            timeStampToken.validate(siv);
            System.out.println("TimeStampToken validated");
        }

        try
        {
            if (sig.getSignDate() != null)
            {
                certFromSignedData.checkValidity(sig.getSignDate().getTime());
                System.out.println("Certificate valid at signing time");
            }
            else
            {
                System.err.println("Certificate cannot be verified without signing time");
            }
        }
        catch (CertificateExpiredException ex)
        {
            System.err.println("Certificate expired at signing time");
        }
        catch (CertificateNotYetValidException ex)
        {
            System.err.println("Certificate not yet valid at signing time");
        }

        // usually not available
        if (signerInformation.getSignedAttributes() != null)
        {
            // From SignedMailValidator.getSignatureTime()
            Attribute signingTime = signerInformation.getSignedAttributes().get(CMSAttributes.signingTime);
            if (signingTime != null)
            {
                Time timeInstance = Time.getInstance(signingTime.getAttrValues().getObjectAt(0));
                try
                {
                    certFromSignedData.checkValidity(timeInstance.getDate());
                    System.out.println("Certificate valid at signing time: " + timeInstance.getDate());
                }
                catch (CertificateExpiredException ex)
                {
                    System.err.println("Certificate expired at signing time");
                }
                catch (CertificateNotYetValidException ex)
                {
                    System.err.println("Certificate not yet valid at signing time");
                }
            }
        }

        if (isSelfSigned(certFromSignedData))
        {
            System.err.println("Certificate is self-signed, LOL!");
        }
        else
        {
            System.out.println("Certificate is not self-signed");
            // todo rest of chain
        }

        if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().
                setProvider(SecurityProvider.getProvider()).build(certFromSignedData)))
        {
            System.out.println("Signature verified");
        }
        else
        {
            System.out.println("Signature verification failed");
        }
    }

    /**
     * Analyzes the DSS-Dictionary (Document Security Store) of the document. Which is used for
     * signature validation. The DSS is defined in PAdES Part 4 - Long Term Validation.
     *
     * @param document PDDocument, to get the DSS from
     */
    private void analyseDSS(PDDocument document) throws IOException
    {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSBase dssElement = catalog.getCOSObject().getDictionaryObject("DSS");

        if (dssElement instanceof COSDictionary)
        {
            COSDictionary dss = (COSDictionary) dssElement;
            System.out.println("DSS Dictionary: " + dss);
            COSBase certsElement = dss.getDictionaryObject("Certs");
            if (certsElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) certsElement, "Cert");
            }
            COSBase ocspsElement = dss.getDictionaryObject("OCSPs");
            if (ocspsElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) ocspsElement, "Ocsp");
            }
            COSBase crlElement = dss.getDictionaryObject("CRLs");
            if (crlElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) crlElement, "CRL");
            }
            // TODO: go through VRIs (which indirectly point to the DSS-Data)
        }
    }

    /**
     * Go through the elements of a COSArray containing each an COSStream to print in Hex.
     *
     * @param elements COSArray of elements containing a COS Stream
     * @param description to append on Print
     * @throws IOException
     */
    private void printStreamsFromArray(COSArray elements, String description) throws IOException
    {
        for (COSBase baseElem : elements)
        {
            COSObject streamObj = (COSObject) baseElem;
            if (streamObj.getObject() instanceof COSStream)
            {
                COSStream cosStream = (COSStream) streamObj.getObject();

                COSInputStream input = cosStream.createInputStream();
                byte[] streamBytes = IOUtils.toByteArray(input);

                System.out.println(description + " (" + elements.indexOf(streamObj) + "): "
                        + Hex.getString(streamBytes));
            }
        }
    }

    // https://svn.apache.org/repos/asf/cxf/tags/cxf-2.4.1/distribution/src/main/release/samples/sts_issue_operation/src/main/java/demo/sts/provider/cert/CertificateVerifier.java
    
    /**
     * Checks whether given X.509 certificate is self-signed.
     */
    private boolean isSelfSigned(X509Certificate cert)
            throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException
    {
        try
        {
            // Try to verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        }
        catch (SignatureException sigEx)
        {
            return false;
        }
        catch (InvalidKeyException keyEx)
        {
            return false;
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
