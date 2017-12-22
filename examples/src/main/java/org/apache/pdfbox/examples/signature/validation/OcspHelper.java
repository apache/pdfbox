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
package org.apache.pdfbox.examples.signature.validation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ContentVerifierProviderBuilder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;

/**
 * Helper Class for OCSP-Operations with bouncy castle.
 * 
 * @author Alexis Suter
 */
public class OcspHelper
{
    private static final Log LOG = LogFactory.getLog(OcspHelper.class);

    private final X509Certificate issuerCertificate;
    private final X509Certificate certificateToCheck;
    private final String ocspUrl;
    private DEROctetString encodedNonce;

    /**
     * @param checkCertificate Certificate to be OCSP-Checked
     * @param issuerCertificate Certificate of the issuer
     * @param ocspUrl where to fetch for OCSP
     */
    public OcspHelper(X509Certificate checkCertificate, X509Certificate issuerCertificate,
            String ocspUrl)
    {
        this.certificateToCheck = checkCertificate;
        this.issuerCertificate = issuerCertificate;
        this.ocspUrl = ocspUrl;
    }

    /**
     * Performs and verifies the OCSP-Request
     *
     * @return the OCSPResp, when the request was successful, else a corresponding exception will be
     * thrown.
     * @throws IOException
     * @throws OCSPException
     * @throws RevokedCertificateException
     */
    public OCSPResp getResponseOcsp() throws IOException, OCSPException, RevokedCertificateException
    {
        OCSPResp ocspResponse = performRequest();
        verifyOcspResponse(ocspResponse);
        return ocspResponse;
    }

    /**
     * Verifies the status and the response itself (including nonce), but not the signature.
     * 
     * @param ocspResponse to be verified
     * @throws OCSPException
     * @throws RevokedCertificateException
     */
    private void verifyOcspResponse(OCSPResp ocspResponse)
            throws OCSPException, RevokedCertificateException
    {
        verifyRespStatus(ocspResponse);

        BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
        if (basicResponse != null)
        {
            checkOcspSignature(basicResponse.getCerts()[0], basicResponse);

            checkNonce(basicResponse);

            SingleResp[] responses = basicResponse.getResponses();
            if (responses.length == 1)
            {
                SingleResp resp = responses[0];
                Object status = resp.getCertStatus();

                if (status instanceof RevokedStatus)
                {
                    throw new RevokedCertificateException("OCSP: Certificate is revoked.");
                }
                else if (status != CertificateStatus.GOOD)
                {
                    throw new OCSPException("OCSP: Status of Cert is unknown");
                }
            }
            else
            {
                throw new OCSPException(
                        "OCSP: Recieved " + responses.length + " responses instead of 1!");
            }
        }
    }

    /**
     * Checks whether the OCSP response is signed by the given certificate.
     * 
     * @param certificate the certificate to check the signature
     * @param basicResponse OCSP response containing the signature
     * @throws OCSPException when the signature is invalid or could not be checked
     */
    private void checkOcspSignature(X509CertificateHolder certificate, BasicOCSPResp basicResponse)
            throws OCSPException
    {
        try
        {
            ContentVerifierProvider verifier = new JcaX509ContentVerifierProviderBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(certificate);

            if (!basicResponse.isSignatureValid(verifier))
            {
                throw new OCSPException("OCSP-Signature is not valid!");
            }
        }
        catch (OperatorCreationException e)
        {
            throw new OCSPException("Error checking Ocsp-Signature", e);
        }
    }

    /**
     * Checks if the nonce in the response is correct
     * 
     * @param basicResponse Response to be checked
     * @throws OCSPException if nonce is wrong or inexistent
     */
    private void checkNonce(BasicOCSPResp basicResponse) throws OCSPException
    {
        Extension nonceExt = basicResponse.getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);
        if (nonceExt != null)
        {
            DEROctetString responseNonceString = (DEROctetString) nonceExt.getExtnValue();
            if (!responseNonceString.equals(encodedNonce))
            {
                throw new OCSPException("Invalid Nonce found in response!");
            }
        }
        else if (encodedNonce != null)
        {
            throw new OCSPException("Nonce not found in response!");
        }
    }

    /**
     * Performs the OCSP-Request, with given data.
     * 
     * @return the OCSPResp, that has been fetched from the ocspUrl
     * @throws IOException
     * @throws OCSPException
     */
    private OCSPResp performRequest() throws IOException, OCSPException
    {
        OCSPReq request = generateOCSPRequest();
        URL url = new URL(ocspUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestProperty("Content-Type", "application/ocsp-request");
        httpConnection.setRequestProperty("Accept", "application/ocsp-response");
        httpConnection.setDoOutput(true);
        try (OutputStream out = httpConnection.getOutputStream())
        {
            out.write(request.getEncoded());
        }

        if (httpConnection.getResponseCode() != 200)
        {
            throw new IOException("OCSP: Could not access url, ResponseCode: "
                    + httpConnection.getResponseCode());
        }
        // Get Response
        InputStream in = (InputStream) httpConnection.getContent();
        return new OCSPResp(in);
    }

    /**
     * Helper method to verify response status.
     * 
     * @param resp OCSP response
     * @throws OCSPException if the response status is not ok
     */
    public void verifyRespStatus(OCSPResp resp) throws OCSPException
    {
        String statusInfo = "";
        if (resp != null)
        {
            int status = resp.getStatus();
            switch (status)
            {
            case OCSPResponseStatus.INTERNAL_ERROR:
                statusInfo = "INTERNAL_ERROR";
                System.err.println("An internal error occurred in the OCSP Server!");
                break;
            case OCSPResponseStatus.MALFORMED_REQUEST:
                statusInfo = "MALFORMED_REQUEST";
                System.err.println("Your request did not fit the RFC 2560 syntax!");
                break;
            case OCSPResponseStatus.SIG_REQUIRED:
                statusInfo = "SIG_REQUIRED";
                System.err.println("Your request was not signed!");
                break;
            case OCSPResponseStatus.TRY_LATER:
                statusInfo = "TRY_LATER";
                System.err.println("The server was too busy to answer you!");
                break;
            case OCSPResponseStatus.UNAUTHORIZED:
                statusInfo = "UNAUTHORIZED";
                System.err.println("The server could not authenticate you!");
                break;
            case OCSPResponseStatus.SUCCESSFUL:
                break;
            default:
                statusInfo = "UNKNOWN";
                System.err.println("Unknown OCSPResponse status code! " + status);
            }
        }
        if (resp == null || resp.getStatus() != OCSPResponseStatus.SUCCESSFUL)
        {
            throw new OCSPException(statusInfo + "OCSP response unsuccessful! ");
        }
    }

    /**
     * Generates an OCSP request and generates the <code>CertificateID</code>.
     *
     * @return OCSP request, ready to fetch data
     * @throws OCSPException
     * @throws IOException
     */
    private OCSPReq generateOCSPRequest() throws OCSPException, IOException
    {
        Security.addProvider(new BouncyCastleProvider());

        // Generate the ID for the certificate we are looking for
        CertificateID certId;
        try
        {
            certId = new CertificateID(new SHA1DigestCalculator(),
                    new JcaX509CertificateHolder(issuerCertificate),
                    certificateToCheck.getSerialNumber());
        }
        catch (CertificateEncodingException e)
        {
            throw new IOException("Error creating CertificateID with the Certificate encoding", e);
        }

        OCSPReqBuilder builder = new OCSPReqBuilder();

        Extension responseExtension = new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_response,
                true, new DLSequence(OCSPObjectIdentifiers.id_pkix_ocsp_basic).getEncoded());

        Random rand = new Random();
        byte[] nonce = new byte[16];
        rand.nextBytes(nonce);
        encodedNonce = new DEROctetString(new DEROctetString(nonce));
        Extension nonceExtension = new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, true,
                encodedNonce);

        builder.setRequestExtensions(
                new Extensions(new Extension[] { responseExtension, nonceExtension }));

        builder.addRequest(certId);

        System.out.println("Nonce: " + Hex.getString(nonceExtension.getExtnValue().getEncoded()));

        return builder.build();
    }

    /**
     * Class to create SHA-1 Digest, used for creation of CertificateID.
     */
    private static class SHA1DigestCalculator implements DigestCalculator
    {
        private final ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier()
        {
            return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
        }

        @Override
        public OutputStream getOutputStream()
        {
            return bOut;
        }

        @Override
        public byte[] getDigest()
        {
            byte[] bytes = bOut.toByteArray();
            bOut.reset();

            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                return md.digest(bytes);
            }
            catch (NoSuchAlgorithmException e)
            {
                LOG.error("SHA-1 Algorithm not found", e);
                return null;
            }
        }
    }
}
