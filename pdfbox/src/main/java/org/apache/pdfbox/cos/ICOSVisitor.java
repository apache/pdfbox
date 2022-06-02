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
package org.apache.pdfbox.cos;

import java.io.IOException;

/**
 * An interface for visiting a PDF document at the type (COS) level.
 *
 * @author Michael Traut
 */
public interface ICOSVisitor
{
    /**
     * Notification of visit to Array object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromArray(COSArray obj) throws IOException;

    /**
     * Notification of visit to boolean object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromBoolean(COSBoolean obj) throws IOException;

    /**
     * Notification of visit to dictionary object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromDictionary(COSDictionary obj) throws IOException;

    /**
     * Notification of visit to document object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromDocument(COSDocument obj) throws IOException;

    /**
     * Notification of visit to float object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromFloat(COSFloat obj) throws IOException;

    /**
     * Notification of visit to integer object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromInt(COSInteger obj) throws IOException;

    /**
     * Notification of visit to name object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromName(COSName obj) throws IOException;

    /**
     * Notification of visit to null object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromNull(COSNull obj) throws IOException;

    /**
     * Notification of visit to stream object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromStream(COSStream obj) throws IOException;

    /**
     * Notification of visit to string object.
     *
     * @param obj The Object that is being visited.
     * @throws IOException If there is an error while visiting this object.
     */
    void visitFromString(COSString obj) throws IOException;
}
