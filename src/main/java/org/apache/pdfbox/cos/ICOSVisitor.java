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

import org.apache.pdfbox.exceptions.COSVisitorException;

/**
 * An interface for visiting a PDF document at the type (COS) level.
 *
 * @author Michael Traut
 * @version $Revision: 1.6 $
 */
public interface ICOSVisitor
{
    /**
     * Notification of visit to Array object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromArray( COSArray obj ) throws COSVisitorException;

    /**
     * Notification of visit to boolean object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromBoolean( COSBoolean obj ) throws COSVisitorException;

    /**
     * Notification of visit to dictionary object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromDictionary( COSDictionary obj ) throws COSVisitorException;

    /**
     * Notification of visit to document object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromDocument( COSDocument obj ) throws COSVisitorException;

    /**
     * Notification of visit to float object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromFloat( COSFloat obj ) throws COSVisitorException;

    /**
     * Notification of visit to integer object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromInt( COSInteger obj ) throws COSVisitorException;

    /**
     * Notification of visit to name object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromName( COSName obj ) throws COSVisitorException;

    /**
     * Notification of visit to null object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromNull( COSNull obj ) throws COSVisitorException;

    /**
     * Notification of visit to stream object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromStream( COSStream obj ) throws COSVisitorException;

    /**
     * Notification of visit to string object.
     *
     * @param obj The Object that is being visited.
     * @return any Object depending on the visitor implementation, or null
     * @throws COSVisitorException If there is an error while visiting this object.
     */
    public Object visitFromString( COSString obj ) throws COSVisitorException;
}
