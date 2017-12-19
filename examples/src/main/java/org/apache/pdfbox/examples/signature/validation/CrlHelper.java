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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;

/**
 * Helper class to get CRL (Certificate revocation list) from given crlUrl and check if Certificate
 * has been revoked.
 *
 * @author Alexis Suter
 */
public final class CrlHelper
{
    private CrlHelper()
    {
    }

    /**
     * Performs the CRL-Request and checks if the given certificate has been revoked.
     * 
     * @param crlUrl to get the CRL from
     * @param cert to be checked if it is inside the CRL
     * @return CRL-Response; might be very big depending on the issuer. 
     * @throws CRLException if an Error occurred getting the CRL, or parsing it.
     * @throws RevokedCertificateException
     */
    public static byte[] performCrlRequestAndCheck(String crlUrl, X509Certificate cert)
            throws CRLException, RevokedCertificateException
    {
        try
        {
            URL url = new URL(crlUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (con.getResponseCode() != 200)
            {
                throw new IOException("Unsuccessful CRL request. Status: " + con.getResponseCode()
                        + " Url: " + crlUrl);
            }

            CertificateFactory certFac = new CertificateFactory();
            X509CRL crl = (X509CRL) certFac.engineGenerateCRL(con.getInputStream());
            if (crl.isRevoked(cert))
            {   
                throw new RevokedCertificateException("The Certificate was found on the CRL and is revoked!");
            }
            return crl.getEncoded();
        }
        catch (IOException e)
        {
            throw new CRLException(e);
        }
    }

}
