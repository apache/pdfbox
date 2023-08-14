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

import java.awt.Point;
import java.awt.geom.Point2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Point class with faster hashCode() to speed up the rendering of Gouraud shadings. Should only be
 * used for maps or sets when all elements are of this type, because the hashCode() method violates
 * its general contract "If two objects are equal according to the equals(Object) method, then
 * calling the hashCode method on each of the two objects must produce the same" when IntPoint is
 * mixed with Point, because IntPoint(x,y) would have a different hashCode than Point(x,y).
 *
 * @author Tilman Hausherr
 */
class IntPoint extends Point
{
    private static final Log LOG = LogFactory.getLog(IntPoint.class);

    IntPoint(int x, int y)
    {
        super(x, y);
    }

    @Override
    public int hashCode()
    {
        return 89 * (623 + this.x) + this.y;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            if (obj instanceof Point2D)
            {
                // hitting this branch means that the warning on top of the class wasn't read
                LOG.error("IntPoint should not be used together with its base class");
            }
            return false;
        }
        final IntPoint other = (IntPoint) obj;
        return this.x == other.x && this.y == other.y;
    }
}
