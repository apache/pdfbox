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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSObjectKey;

public final class COSUtils
{
    private static final Log LOGGER = LogFactory.getLog(COSUtils.class);

    private COSUtils()
    {
    }

    /**
     * return true if the elt is a COSDictionary or a reference to a COSDictionary
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSDictionary or a reference to it.
     */
    public static boolean isDictionary(COSBase elt, COSDocument doc)
    {
        return isClass(elt, doc, COSDictionary.class);
    }

    /**
     * return true if the elt is a COSString or a COSName or a reference to it.
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSString or a COSName or a reference to it.
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
                LOGGER.debug("Couldn't get COSObject from object pool - returning false", e);
                return false;
            }
        }

        return (elt instanceof COSString || elt instanceof COSName);
    }

    /**
     * return true if the elt is a COSStream or a reference to a COSStream
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSStream or a reference to it.
     */
    public static boolean isStream(COSBase elt, COSDocument doc)
    {
        return isClass(elt, doc, COSStream.class);
    }

    /**
     * return true if the elt is a COSInteger or a reference to a COSInteger
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSInteger or a reference to it.
     */
    public static boolean isInteger(COSBase elt, COSDocument doc)
    {
        return isClass(elt, doc, COSInteger.class);
    }

    /**
     * return true if the elt is of class or a reference to a that class.
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @param claz the class.
     * @return true if the object is a of that class or a reference to it.
     */
    private static boolean isClass(COSBase elt, COSDocument doc, Class claz)
    {
        if (elt instanceof COSObject)
        {
            try
            {
                COSObjectKey key = new COSObjectKey((COSObject) elt);
                COSObject obj = doc.getObjectFromPool(key);
                return (obj != null && claz.isInstance(obj.getObject()));
            }
            catch (IOException e)
            {
                LOGGER.debug("Couldn't get COSObject from object pool - returning false", e);
                return false;
            }
        }

        return claz.isInstance(elt);
    }

    /**
     * return true if elt is COSInteger or COSFloat
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSInteger, COSFloat or a reference to it.
     */
    public static boolean isNumeric(COSBase elt, COSDocument doc)
    {
        return isInteger(elt, doc) || isFloat(elt, doc);
    }

    /**
     * return true if the elt is a COSFloat or a reference to a COSFloat
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSFloat or a reference to it.
     */
    public static boolean isFloat(COSBase elt, COSDocument doc)
    {
        return isClass(elt, doc, COSFloat.class);
    }

    /**
     * return true if the elt is a COSArray or a reference to a COSArray
     * 
     * @param elt the object to check.
     * @param doc the document.
     * @return true if the object is a COSArray or a reference to it.
     */
    public static boolean isArray(COSBase elt, COSDocument doc)
    {
        return isClass(elt, doc, COSArray.class);
    }

    /**
     * Return the COSBase object as COSArray if the COSBase object is an instance of COSArray or a reference to a
     * COSArray object. In other cases, this method returns null;
     * 
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as COSArray if the object is a COSArray or a reference to it. Returns null otherwise.
     */
    public static COSArray getAsArray(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            return (COSArray) getCOSObjectAsClass((COSObject) cbase, cDoc, COSArray.class);
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
     * Return the COSBase object as String if the COSBase object is an instance of COSString or
     * COSName or a reference to it.
     *
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as String if the object is a COSString or COSName or reference to it.
     * Returns null otherwise.
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
                LOGGER.debug("Couldn't get COSObject from object pool - returning null", e);
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
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as COSDictionary if the object is a COSDictionary or a reference to it. Returns null otherwise.
     */
    public static COSDictionary getAsDictionary(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            return (COSDictionary) getCOSObjectAsClass((COSObject) cbase, cDoc, COSDictionary.class);
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
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as COSStream if the object is a COSStream or a reference to it. Returns null otherwise.
     */
    public static COSStream getAsStream(COSBase cbase, COSDocument cDoc)
    {
        if (cbase instanceof COSObject)
        {
            return (COSStream) getCOSObjectAsClass((COSObject) cbase, cDoc, COSStream.class);
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
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as Float if the object is a COSFloat or a reference to it. Returns null otherwise.
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
                else if (obj.getObject() instanceof COSNumber)
                {
                    return ((COSNumber) obj.getObject()).floatValue();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                LOGGER.debug("Couldn't get COSObject from object pool - returning null", e);
                return null;
            }
        }
        else if (cbase instanceof COSNumber)
        {
            return ((COSNumber) cbase).floatValue();
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
     * @param cbase the object to get.
     * @param cDoc the document.
     * @return the object as Integer if the object is a COSInteger or a reference to it. Returns null otherwise.
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
                else if (obj.getObject() instanceof COSNumber)
                {
                    return ((COSNumber) obj.getObject()).intValue();
                }
                else
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                LOGGER.debug("Couldn't get COSObject from object pool - returning null", e);
                return null;
            }
        }
        else if (cbase instanceof COSNumber)
        {
            return ((COSNumber) cbase).intValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Close the given Document. If the close method of the document throws an
     * exception, it is logged using a commons logger (Level : WARN)
     *
     * @param document the document.
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
            LOGGER.warn("Error occured during the close of a COSDocument : " + e.getMessage(), e);
        }
    }

    /**
     * Close the given Document. If the close method of the document throws an
     * exception, it is logged using a commons logger (Level : WARN)
      * 
     * @param document the document.
     */
    public static void closeDocumentQuietly(PDDocument document)
    {
        if (document != null)
        {
            closeDocumentQuietly(document.getDocument());
        }
    }
    
    /**
     * Return the COSObject object as class if the COSObject object is a reference to an object of
     * that class. If not, then this method returns null;
     *
     * @param cosObject the object to get.
     * @param cDoc the document.
     * @param claz the class.
     * @return the object as class if the object is a reference to that class. Returns null
     * otherwise.
     */
    private static COSBase getCOSObjectAsClass(COSObject cosObject, COSDocument cDoc, Class claz)
    {
        try
        {
            COSObjectKey key = new COSObjectKey(cosObject);
            COSObject obj = cDoc.getObjectFromPool(key);
            if (obj != null && claz.isInstance(obj.getObject()))
            {
                return obj.getObject();
            }
            else
            {
                return null;
            }
        }
        catch (IOException e)
        {
            LOGGER.debug("Couldn't get COSObject from object pool - returning null", e);
            return null;
        }
    }
}
