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
package org.apache.pdfbox.examples.signature.cert;

import java.util.Date;

/**
 * Exception to handle a revoked Certificate explicitly
 * 
 * @author Alexis Suter
 */
public class RevokedCertificateException extends Exception
{
    private static final long serialVersionUID = 3543946618794126654L;
    
    private final Date revocationTime;

    public RevokedCertificateException(String message)
    {
        super(message);
        this.revocationTime = null;
    }

    public RevokedCertificateException(String message, Date revocationTime)
    {
        super(message);
        this.revocationTime = revocationTime;
    }

    public Date getRevocationTime()
    {
        return revocationTime;
    }
}
