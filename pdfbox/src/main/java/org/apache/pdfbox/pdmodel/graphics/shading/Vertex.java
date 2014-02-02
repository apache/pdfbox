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

package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.geom.Point2D;

/**
 * 
 * Helper class to deal with Vertices for type 4 and 5 shading.
 *
 * @author Tilman Hausherr
 */
class Vertex
{
    public byte flag; // used only with type 4 shading
    public Point2D point;
    public float[] color;

    public Vertex(byte flag, Point2D point, float[] color)
    {
        this.flag = flag;
        this.point = point;
        this.color = color.clone();
    }

    @Override
    public String toString()
    {
        String colorStr = "";
        for (float f : color)
        {
            if (colorStr.length() > 0)
            {
                colorStr += " ";
            }
            colorStr += String.format("%3.2f", f);
        }
        return "Vertex{" + flag + ": " + point + ", colors=[" + colorStr + "] }";
    }

}
