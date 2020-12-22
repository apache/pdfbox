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
package org.apache.pdfbox.pdmodel.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for creating MessageDigest instances.
 * @author John Hewson
 */
final class MessageDigests
{
    private MessageDigests()
    {
    }
    
    /**
     * @return MD5 message digest
     */
    static MessageDigest getMD5()
    {
        try
        {
            return MessageDigest.getInstance("MD5");
        }
        catch (final NoSuchAlgorithmException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * @return SHA-1 message digest
     */
    static MessageDigest getSHA1()
    {
        try
        {
            return MessageDigest.getInstance("SHA-1");
        }
        catch (final NoSuchAlgorithmException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * @return SHA-256 message digest
     */
    static MessageDigest getSHA256()
    {
        try
        {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (final NoSuchAlgorithmException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
