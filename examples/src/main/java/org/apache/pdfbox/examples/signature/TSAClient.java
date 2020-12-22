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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/**
 * Time Stamping Authority (TSA) Client [RFC 3161].
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 */
public class TSAClient
{
    private static final Log LOG = LogFactory.getLog(TSAClient.class);

    private static final DigestAlgorithmIdentifierFinder ALGORITHM_OID_FINDER =
            new DefaultDigestAlgorithmIdentifierFinder();

    private final URL url;
    private final String username;
    private final String password;
    private final MessageDigest digest;

    /**
     *
     * @param url the URL of the TSA service
     * @param username user name of TSA
     * @param password password of TSA
     * @param digest the message digest to use
     */
    public TSAClient(final URL url, final String username, final String password, final MessageDigest digest)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.digest = digest;
    }

    /**
     *
     * @param messageImprint imprint of message contents
     * @return the encoded time stamp token
     * @throws IOException if there was an error with the connection or data from the TSA server,
     *                     or if the time stamp response could not be validated
     */
    public byte[] getTimeStampToken(final byte[] messageImprint) throws IOException
    {
        digest.reset();
        final byte[] hash = digest.digest(messageImprint);

        // 32-bit cryptographic nonce
        final SecureRandom random = new SecureRandom();
        final int nonce = random.nextInt();

        // generate TSA request
        final TimeStampRequestGenerator tsaGenerator = new TimeStampRequestGenerator();
        tsaGenerator.setCertReq(true);
        final ASN1ObjectIdentifier oid = ALGORITHM_OID_FINDER.find(digest.getAlgorithm()).getAlgorithm();
        final TimeStampRequest request = tsaGenerator.generate(oid, hash, BigInteger.valueOf(nonce));

        // get TSA response
        final byte[] tsaResponse = getTSAResponse(request.getEncoded());

        final TimeStampResponse response;
        try
        {
            response = new TimeStampResponse(tsaResponse);
            response.validate(request);
        }
        catch (TSPException e)
        {
            throw new IOException(e);
        }
        
        final TimeStampToken token = response.getTimeStampToken();
        if (token == null)
        {
            // https://www.ietf.org/rfc/rfc3161.html#section-2.4.2
            throw new IOException("Response from " + url +
                    " does not have a time stamp token, status: " + response.getStatus() +
                    " (" + response.getStatusString() + ")");
        }

        return token.getEncoded();
    }

    // gets response data for the given encoded TimeStampRequest data
    // throws IOException if a connection to the TSA cannot be established
    private byte[] getTSAResponse(final byte[] request) throws IOException
    {
        LOG.debug("Opening connection to TSA server");

        // todo: support proxy servers
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/timestamp-query");

        LOG.debug("Established connection to TSA server");

        if (username != null && password != null && !username.isEmpty() && !password.isEmpty())
        {
            connection.setRequestProperty(username, password);
        }

        // read response
        try (OutputStream output = connection.getOutputStream())
        {
            output.write(request);
        }

        LOG.debug("Waiting for response from TSA server");

        final byte[] response;
        try (InputStream input = connection.getInputStream())
        {
            response = IOUtils.toByteArray(input);
        }

        LOG.debug("Received response from TSA server");

        return response;
    }
}
