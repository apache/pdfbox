/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.utils;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

public class COSUtils
{

    public static final Logger LOGGER = Logger.getLogger(COSUtils.class);

    /**
     * return true if the elt is a COSDictionary or a reference to a COSDictionary
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isDictionary(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && obj.getObject() instanceof COSDictionary);
            }
            catch (IOException e)
            {
                return false;
            }
        }
        return (elt instanceof COSDictionary);
    }

    /**
     * return true if the elt is a COSString or a reference to a COSString
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isString(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && (obj.getObject() instanceof COSString || obj.getObject() instanceof COSName));
            }
            catch (IOException e)
            {
                return false;
            }
        }

        return (elt instanceof COSString || elt instanceof COSName);
    }

    /**
     * return true if the elt is a COSStream or a reference to a COSStream
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isStream(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && obj.getObject() instanceof COSStream);
            }
            catch (IOException e)
            {
                return false;
            }
        }

        return (elt instanceof COSStream);
    }

    /**
     * return true if the elt is a COSInteger or a reference to a COSInteger
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isInteger(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && obj.getObject() instanceof COSInteger);
            }
            catch (IOException e)
            {
                return false;
            }
        }

        return (elt instanceof COSInteger);
    }

    /**
     * return true if elt is COSInteger or COSFloat
     * 
     * @param elt
     * @param doc
     * @return boolean
     */
    public static boolean isNumeric(COSBase elt, COSDocument doc)
    {
        return isInteger(elt, doc) || isFloat(elt, doc);
    }

    /**
     * return true if the elt is a COSInteger or a reference to a COSInteger
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isFloat(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && obj.getObject() instanceof COSFloat);
            }
            catch (IOException e)
            {
                return false;
            }
        }

        return (elt instanceof COSFloat);
    }

    /**
     * return true if the elt is a COSArray or a reference to a COSArray
     * 
     * @param elt
     * @param doc
     * @return
     */
    public static boolean isArray(COSBase elt, COSDocument doc)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && obj.getObject() instanceof COSArray);
            }
            catch (IOException e)
            {
                return false;
            }
        }

        return (elt instanceof COSArray);
    }

    /**
     * Return the COSBase object as COSArray if the COSBase object is an instance of COSArray or a reference to a
     * COSArray object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static COSArray getAsArray(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj != null && obj.getObject() instanceof COSArray)
                {
                    return (COSArray) obj.getObject();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSArray)
        {
            return (COSArray) cbase;
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the COSBase object as COSString if the COSBase object is an instance of COSString or a reference to a
     * COSString object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static String getAsString(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj != null && obj.getObject() instanceof COSString)
                {
                    return ((COSString) obj.getObject()).getString();
                }
                else if (obj != null && obj.getObject() instanceof COSName)
                {
                    return ((COSName) obj.getObject()).getName();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSString)
        {
            return ((COSString) cbase).getString();
        }
        else if (cbase instanceof COSName)
        {
            return ((COSName) cbase).getName();
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the COSBase object as COSDictionary if the COSBase object is an instance of COSDictionary or a reference
     * to a COSDictionary object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static COSDictionary getAsDictionary(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj != null && obj.getObject() instanceof COSDictionary)
                {
                    return (COSDictionary) obj.getObject();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSDictionary)
        {
            return (COSDictionary) cbase;
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the COSBase object as COSStream if the COSBase object is an instance of COSStream or a reference to a
     * COSStream object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static COSStream getAsStream(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj != null && obj.getObject() instanceof COSStream)
                {
                    return (COSStream) obj.getObject();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSStream)
        {
            return (COSStream) cbase;
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the COSBase object as Float if the COSBase object is an instance of COSFloat or a reference to a COSFloat
     * object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static Float getAsFloat(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj == null)
                {
                    return null;
                }
                else if (obj.getObject() instanceof COSFloat)
                {
                    return ((COSFloat) obj.getObject()).floatValue();
                }
                else if (obj.getObject() instanceof COSInteger)
                {
                    return (float) ((COSInteger) obj.getObject()).intValue();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSFloat)
        {
            return ((COSFloat) cbase).floatValue();
        }
        else if (cbase instanceof COSInteger)
        {
            return (float) ((COSInteger) cbase).intValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Return the COSBase object as Integer if the COSBase object is an instance of COSInteger or a reference to a
     * COSInteger object. In other cases, this method returns null;
     * 
     * @param cbase
     * @param cDoc
     * @return
     */
    public static Integer getAsInteger(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) cbase);
                COSObject obj = cDoc.getObjectFromPool(key);
                if (obj == null)
                {
                    return null;
                }
                else if (obj.getObject() instanceof COSInteger)
                {
                    return ((COSInteger) obj.getObject()).intValue();
                }
                else if (obj.getObject() instanceof COSFloat)
                {
                    return ((COSFloat) obj.getObject()).intValue();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                return null;
            }
        }
        else if (cbase instanceof COSInteger)
        {
            return ((COSInteger) cbase).intValue();
        }
        else if (cbase instanceof COSFloat)
        {
            return ((COSFloat) cbase).intValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Close the given Document. If the close method of the document throws an exception, it is logged using a log4j
     * logger (Level : WARN)
     * 
     * @param document
     */
    public static void closeDocumentQuietly(COSDocument document)
    {
        try
        {
            if (document != null)
            {
                document.close();
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Error occured during the close of a COSDocument : " + e.getMessage());
        }
    }

    /**
     * Close the given Document. If the close method of the document throws an exception, it is logged using a log4j
     * logger (Level : WARN)
     * 
     * @param document
     */
    public static void closeDocumentQuietly(PDDocument document)
    {
        if (document != null)
        {
            closeDocumentQuietly(document.getDocument());
        }
    }
}
