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
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.examples.signature.cert.CertificateVerificationException;
import org.apache.pdfbox.examples.signature.cert.CertificateVerifier;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

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
     * @throws org.bouncycastle.tsp.TSPException
     * @throws java.security.GeneralSecurityException
     * @throws org.apache.pdfbox.examples.signature.cert.CertificateVerificationException
     */
    public static void main(String[] args) throws IOException, TSPException, GeneralSecurityException,
            CertificateVerificationException
    {
        // register BouncyCastle provider, needed for "exotic" algorithms
        Security.addProvider(SecurityProvider.getProvider());

        ShowSignature show = new ShowSignature();
        show.showSignature( args );
    }

    private void showSignature(String[] args) throws IOException, TSPException, GeneralSecurityException,
            CertificateVerificationException
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
                // use old-style document loading to disable leniency
                // see also https://www.pdf-insecurity.org/
                RandomAccessBufferedFileInputStream raFile = new RandomAccessBufferedFileInputStream(infile);
                // If your files are not too large, you can also download the PDF into a byte array
                // with IOUtils.toByteArray() and pass a RandomAccessBuffer() object to the
                // PDFParser constructor.
                PDFParser parser = new PDFParser(raFile, password);
                parser.setLenient(false);
                parser.parse();
                document = parser.getPDDocument();
                for (PDSignature sig : document.getSignatureDictionaries())
                {
                    COSDictionary sigDict = sig.getCOSObject();
                    COSString contents = (COSString) sigDict.getDictionaryObject(COSName.CONTENTS);

                    // download the signed content
                    FileInputStream fis = new FileInputStream(infile);
                    byte[] buf = null;
                    try
                    {
                        buf = sig.getSignedContent(fis); // alternatively, pass a byte array here
                    }
                    finally
                    {
                        fis.close();
                    }

                    System.out.println("Signature found");

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
                        }
                        else if (subFilter.equals("adbe.x509.rsa_sha1"))
                        {
                            // example: PDFBOX-2693.pdf
                            COSString certString = (COSString) sigDict.getDictionaryObject(COSName.CERT);
                            //TODO this could also be an array.
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

                            X509Certificate cert = (X509Certificate) certs.iterator().next();

                            // to verify signature, see code at
                            // https://stackoverflow.com/questions/43383859/

                            try
                            {
                                if (sig.getSignDate() != null)
                                {
                                    cert.checkValidity(sig.getSignDate().getTime());
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
                            if (CertificateVerifier.isSelfSigned(cert))
                            {
                                System.err.println("Certificate is self-signed, LOL!");
                            }
                            else
                            {
                                System.out.println("Certificate is not self-signed");

                                if (sig.getSignDate() != null)
                                {
                                    @SuppressWarnings("unchecked")
                                    Store<X509CertificateHolder> store = new JcaCertStore(certs);
                                    verifyCertificateChain(store, cert, sig.getSignDate().getTime());
                                }
                            }
                        }
                        else if (subFilter.equals("ETSI.RFC3161"))
                        {
                            // e.g. PDFBOX-1848, file_timestamped.pdf
                            verifyETSIdotRFC3161(buf, contents);
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
                        checkContentValueWithFile(infile, byteRange, contents);
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

    private void checkContentValueWithFile(File file, int[] byteRange, COSString contents) throws IOException
    {
        // https://stackoverflow.com/questions/55049270
        // comment by mkl: check whether gap contains a hex value equal
        // byte-by-byte to the Content value, to prevent attacker from using a literal string
        // to allow extra space
        RandomAccessBufferedFileInputStream raf = new RandomAccessBufferedFileInputStream(file);
        raf.seek(byteRange[1]);
        int c = raf.read();
        if (c != '<')
        {
            System.err.println("'<' expected at offset " + byteRange[1] + ", but got " + (char) c);
        }
        byte[] contentFromFile = raf.readFully(byteRange[2] - byteRange[1] - 2);
        byte[] contentAsHex = Hex.getString(contents.getBytes()).getBytes(Charsets.US_ASCII);
        if (contentFromFile.length != contentAsHex.length)
        {
            System.err.println("Raw content length from file is " +
                    contentFromFile.length +
                    ", but internal content string in hex has length " +
                    contentAsHex.length);
        }
        // Compare the two, we can't do byte comparison because of upper/lower case
        // also check that it is really hex
        for (int i = 0; i < contentFromFile.length; ++i)
        {
            try
            {
                if (Integer.parseInt(String.valueOf((char) contentFromFile[i]), 16) !=
                    Integer.parseInt(String.valueOf((char) contentAsHex[i]), 16))
                {
                    System.err.println("Possible manipulation at file offset " +
                                       (byteRange[1] + i + 1) + " in signature content");
                    break;
                }
            }
            catch (NumberFormatException ex)
            {
                System.err.println("Incorrect hex value");
                System.err.println("Possible manipulation at file offset " +
                                    (byteRange[1] + i + 1) + " in signature content");
                break;
            }
        }
        c = raf.read();
        if (c != '>')
        {
            System.err.println("'>' expected at offset " + byteRange[2] + ", but got " + (char) c);
        }
        raf.close();
    }

    private void verifyETSIdotRFC3161(byte[] buf, COSString contents)
            throws CertificateException, CMSException, IOException, OperatorCreationException,
            TSPException, NoSuchAlgorithmException, CertificateVerificationException
    {
        TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents.getBytes()));
        System.out.println("Time stamp gen time: " + timeStampToken.getTimeStampInfo().getGenTime());
        System.out.println("Time stamp tsa name: " + timeStampToken.getTimeStampInfo().getTsa().getName());
        
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream certStream = new ByteArrayInputStream(contents.getBytes());
        Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
        System.out.println("certs=" + certs);
        
        String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
        // compare the hash of the signed content with the hash in the timestamp
        if (Arrays.equals(MessageDigest.getInstance(hashAlgorithm).digest(buf),
                timeStampToken.getTimeStampInfo().getMessageImprintDigest()))
        {
            System.out.println("ETSI.RFC3161 timestamp signature verified");
        }
        else
        {
            System.err.println("ETSI.RFC3161 timestamp signature verification failed");
        }

        X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
        SigUtils.validateTimestampToken(timeStampToken);
                verifyCertificateChain(timeStampToken.getCertificates(),
                certFromTimeStamp,
                timeStampToken.getTimeStampInfo().getGenTime());
    }

    /**
     * Verify a PKCS7 signature.
     *
     * @param byteArray the byte sequence that has been signed
     * @param contents the /Contents field as a COSString
     * @param sig the PDF signature (the /V dictionary)
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws TSPException
     */
    private void verifyPKCS7(byte[] byteArray, COSString contents, PDSignature sig)
            throws CMSException, OperatorCreationException,
                   IOException, GeneralSecurityException, TSPException, CertificateVerificationException
    {
        // inspiration:
        // http://stackoverflow.com/a/26702631/535646
        // http://stackoverflow.com/a/9261365/535646
        CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
        CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
        @SuppressWarnings("unchecked")
        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        if (certificatesStore.getMatches(null).isEmpty())
        {
            throw new IOException("No certificates in signature");
        }
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        if (signers.isEmpty())
        {
            throw new IOException("No signers in signature");
        }
        SignerInformation signerInformation = signers.iterator().next();
        @SuppressWarnings("unchecked")
        Collection<X509CertificateHolder> matches =
                certificatesStore.getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
        if (matches.isEmpty())
        {
            throw new IOException("Signer '" + signerInformation.getSID().getIssuer() + 
                                  ", serial# " + signerInformation.getSID().getSerialNumber() + 
                                  " does not match any certificates");
        }
        X509CertificateHolder certificateHolder = matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        System.out.println("certFromSignedData: " + certFromSignedData);

        SigUtils.checkCertificateUsage(certFromSignedData);

        // Embedded timestamp
        TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
        if (timeStampToken != null)
        {
            // tested with QV_RCA1_RCA3_CPCPS_V4_11.pdf
            // https://www.quovadisglobal.com/~/media/Files/Repository/QV_RCA1_RCA3_CPCPS_V4_11.ashx
            // also 021496.pdf and 036351.pdf from digitalcorpora
            SigUtils.validateTimestampToken(timeStampToken);
            @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
            Collection<X509CertificateHolder> tstMatches =
                timeStampToken.getCertificates().getMatches((Selector<X509CertificateHolder>) timeStampToken.getSID());
            X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
            X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(tstCertHolder);
            // merge both stores using a set to remove duplicates
            HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<X509CertificateHolder>();
            certificateHolderSet.addAll(certificatesStore.getMatches(null));
            certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
            verifyCertificateChain(new CollectionStore<X509CertificateHolder>(certificateHolderSet),
                    certFromTimeStamp,
                    timeStampToken.getTimeStampInfo().getGenTime());
            SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
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

        if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().
                setProvider(SecurityProvider.getProvider()).build(certFromSignedData)))
        {
            System.out.println("Signature verified");
        }
        else
        {
            System.out.println("Signature verification failed");
        }

        if (CertificateVerifier.isSelfSigned(certFromSignedData))
        {
            System.err.println("Certificate is self-signed, LOL!");
        }
        else
        {
            System.out.println("Certificate is not self-signed");

            if (sig.getSignDate() != null)
            {
                verifyCertificateChain(certificatesStore, certFromSignedData, sig.getSignDate().getTime());
            }
            else
            {
                System.err.println("Certificate cannot be verified without signing time");
            }
        }
    }

    private void verifyCertificateChain(Store<X509CertificateHolder> certificatesStore,
            X509Certificate certFromSignedData, Date signDate)
            throws CertificateVerificationException, CertificateException
    {
        // Verify certificate chain (new since 11/2018)
        // Please post bad PDF files that succeed and
        // good PDF files that fail in
        // https://issues.apache.org/jira/browse/PDFBOX-3017
        Collection<X509CertificateHolder> certificateHolders = certificatesStore.getMatches(null);
        Set<X509Certificate> additionalCerts = new HashSet<X509Certificate>();
        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
        for (X509CertificateHolder certHolder : certificateHolders)
        {
            X509Certificate certificate = certificateConverter.getCertificate(certHolder);
            if (!certificate.equals(certFromSignedData))
            {
                additionalCerts.add(certificate);
            }
        }
        CertificateVerifier.verifyCertificate(certFromSignedData, additionalCerts, true, signDate);
        //TODO check whether the root certificate is in our trusted list.
        // For the EU, get a list here:
        // https://ec.europa.eu/digital-single-market/en/eu-trusted-lists-trust-service-providers
        // ( getRootCertificates() is not helpful because these are SSL certificates)
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

                InputStream input = cosStream.createInputStream();
                byte[] streamBytes = IOUtils.toByteArray(input);
                input.close();

                System.out.println(description + " (" + elements.indexOf(streamObj) + "): "
                        + Hex.getString(streamBytes));
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java " + ShowSignature.class.getName() +
                            " <password (usually empty)> <inputfile>" );
        // The password is for encrypted files and has nothing to do with the signature.
        // (A PDF can be both encrypted and signed)
    }
}
