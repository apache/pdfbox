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
 * Vertex for Type 4 and Type 5 shadings.
 *
 * @author Tilman Hausherr
 */
class Vertex
{
    final Point2D point;
    final float[] color;

    Vertex(Point2D p, float[] c)
    {
        point = p;
        color = c.clone();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertex{ ").append(point).append(", colors=[");
        for (float f : color)
        {
            sb.append(String.format("%3.2f", f)).append(' ');
        }

        //remove last space if need
        if (color.length > 0 && sb.charAt(sb.length() - 1) == ' ')
        {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.append("] }").toString();
    }
}
