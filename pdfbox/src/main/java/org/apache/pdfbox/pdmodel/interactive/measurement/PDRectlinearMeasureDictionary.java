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
package org.apache.pdfbox.pdmodel.interactive.measurement;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This class represents a rectlinear measure dictionary.
 * 
 */
public class PDRectlinearMeasureDictionary extends PDMeasureDictionary
{

    /**
     * The subtype of the rectlinear measure dictionary.
     */
    public static final String SUBTYPE = "RL";

    /**
     * Constructor.
     */
    public PDRectlinearMeasureDictionary()
    {
        this.setSubtype(SUBTYPE);
    }

    /**
     * Constructor.
     * 
     * @param dictionary the corresponding dictionary
     */
    public PDRectlinearMeasureDictionary(final COSDictionary dictionary)
    {
        super(dictionary);
    }

    /**
     * This will return the scale ration.
     * 
     * @return the scale ratio.
     */
    public String getScaleRatio()
    {
        return this.getCOSObject().getString(COSName.R);
    }

    /**
     * This will set the scale ration.
     * 
     * @param scaleRatio the scale ratio.
     */
    public void setScaleRatio(final String scaleRatio)
    {
        this.getCOSObject().setString(COSName.R, scaleRatio);
    }

    /**
     * This will return the changes along the x-axis.
     * 
     * @return changes along the x-axis
     */
    public PDNumberFormatDictionary[] getChangeXs()
    {
        final COSArray x = (COSArray)this.getCOSObject().getDictionaryObject("X");
        if (x != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[x.size()];
            for (int i = 0; i < x.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) x.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the changes along the x-axis.
     * 
     * @param changeXs changes along the x-axis
     */
    public void setChangeXs(final PDNumberFormatDictionary[] changeXs)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary changeX : changeXs)
        {
            array.add(changeX);
        }
        this.getCOSObject().setItem("X", array);
    }

    /**
     * This will return the changes along the y-axis.
     * 
     * @return changes along the y-axis
     */
    public PDNumberFormatDictionary[] getChangeYs()
    {
        final COSArray y = (COSArray)this.getCOSObject().getDictionaryObject("Y");
        if (y != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[y.size()];
            for (int i = 0; i < y.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) y.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the changes along the y-axis.
     * 
     * @param changeYs changes along the y-axis
     */
    public void setChangeYs(final PDNumberFormatDictionary[] changeYs)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary changeY : changeYs)
        {
            array.add(changeY);
        }
        this.getCOSObject().setItem("Y", array);
    }

    /**
     * This will return the distances.
     * 
     * @return distances
     */
    public PDNumberFormatDictionary[] getDistances()
    {
        final COSArray d = (COSArray)this.getCOSObject().getDictionaryObject("D");
        if (d != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[d.size()];
            for (int i = 0; i < d.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) d.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the distances.
     * 
     * @param distances distances
     */
    public void setDistances(final PDNumberFormatDictionary[] distances)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary distance : distances)
        {
            array.add(distance);
        }
        this.getCOSObject().setItem("D", array);
    }

    /**
     * This will return the areas.
     * 
     * @return areas
     */
    public PDNumberFormatDictionary[] getAreas()
    {
        final COSArray a = (COSArray)this.getCOSObject().getDictionaryObject(COSName.A);
        if (a != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[a.size()];
            for (int i = 0; i < a.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) a.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the areas.
     * 
     * @param areas areas
     */
    public void setAreas(final PDNumberFormatDictionary[] areas)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary area : areas)
        {
            array.add(area);
        }
        this.getCOSObject().setItem(COSName.A, array);
    }

    /**
     * This will return the angles.
     * 
     * @return angles
     */
    public PDNumberFormatDictionary[] getAngles()
    {
        final COSArray t = (COSArray)this.getCOSObject().getDictionaryObject("T");
        if (t != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[t.size()];
            for (int i = 0; i < t.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) t.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the angles.
     * 
     * @param angles angles
     */
    public void setAngles(final PDNumberFormatDictionary[] angles)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary angle : angles)
        {
            array.add(angle);
        }
        this.getCOSObject().setItem("T", array);
    }

    /**
     * This will return the sloaps of a line.
     * 
     * @return the sloaps of a line
     */
    public PDNumberFormatDictionary[] getLineSloaps()
    {
        final COSArray s = (COSArray)this.getCOSObject().getDictionaryObject("S");
        if (s != null)
        {
            final PDNumberFormatDictionary[] retval =
                new PDNumberFormatDictionary[s.size()];
            for (int i = 0; i < s.size(); i++)
            {
                final COSDictionary dic = (COSDictionary) s.get(i);
                retval[i] = new PDNumberFormatDictionary(dic);
            }
            return retval;
        }
        return null;
    }

    /**
     * This will set the sloaps of a line.
     * 
     * @param lineSloaps the sloaps of a line
     */
    public void setLineSloaps(final PDNumberFormatDictionary[] lineSloaps)
    {
        final COSArray array = new COSArray();
        for (final PDNumberFormatDictionary lineSloap : lineSloaps)
        {
            array.add(lineSloap);
        }
        this.getCOSObject().setItem("S", array);
    }

    /**
     * This will return the origin of the coordinate system.
     * 
     * @return the origin
     */
    public float[] getCoordSystemOrigin()
    {
        final COSArray o = (COSArray)this.getCOSObject().getDictionaryObject("O");
        if (o != null)
        {
            return o.toFloatArray();
        }
        return null;
    }

    /**
     * This will set the origin of the coordinate system.
     * 
     * @param coordSystemOrigin the origin
     */
    public void setCoordSystemOrigin(final float[] coordSystemOrigin)
    {
        final COSArray array = new COSArray();
        array.setFloatArray(coordSystemOrigin);
        this.getCOSObject().setItem("O", array);
    }

    /**
     * This will return the CYX factor.
     * 
     * @return CYX factor
     */
    public float getCYX()
    {
        return this.getCOSObject().getFloat("CYX");
    }

    /**
     * This will set the CYX factor.
     * 
     * @param cyx CYX factor
     */
    public void setCYX(final float cyx)
    {
        this.getCOSObject().setFloat("CYX", cyx);
    }

}
