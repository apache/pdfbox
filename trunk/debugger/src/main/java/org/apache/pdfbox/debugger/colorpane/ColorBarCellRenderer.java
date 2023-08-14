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
package org.apache.pdfbox.debugger.colorpane;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Khyrul Bashar.
 */

/**
 * ColorBarCellRenderer class that says how to render color bar columns
 */
public class ColorBarCellRenderer implements TableCellRenderer
{
    @Override
    public Component getTableCellRendererComponent(
            JTable jTable, Object o, boolean b, boolean b2, int i, int i2)
    {
        JLabel colorBar = new JLabel();
        colorBar.setOpaque(true);
        colorBar.setBackground((Color) o);
        return colorBar;
    }
}
