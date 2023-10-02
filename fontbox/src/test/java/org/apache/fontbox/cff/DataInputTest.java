/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.cff;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class DataInputTest
{
    @Test
    void testReadBytes() throws IOException
    {
        byte[] data = { 0, -1, 2, -3, 4, -5, 6, -7, 8, -9 };
        DataInput dataInput = new DataInputByteArray(data);
        assertThrows(IOException.class, () -> dataInput.readBytes(20));
        assertArrayEquals(new byte[] { 0 }, dataInput.readBytes(1));
        assertArrayEquals(new byte[] { -1, 2, -3 }, dataInput.readBytes(3));
        dataInput.setPosition(6);
        assertArrayEquals(new byte[] { 6, -7, 8 }, dataInput.readBytes(3));
        assertThrows(IOException.class, () -> dataInput.readBytes(-1));
        assertThrows(IOException.class, () -> dataInput.readBytes(5));
    }

    @Test
    void testReadByte() throws IOException
    {
        byte[] data = { 0, -1, 2, -3, 4, -5, 6, -7, 8, -9 };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(0, dataInput.readByte());
        assertEquals(-1, dataInput.readByte());
        dataInput.setPosition(6);
        assertEquals(6, dataInput.readByte());
        assertEquals(-7, dataInput.readByte());
        dataInput.setPosition(dataInput.length() - 1);
        assertEquals(-9, dataInput.readByte());
        assertThrows(IOException.class, () -> dataInput.readByte());
    }

    @Test
    void testReadUnsignedByte() throws IOException
    {
        byte[] data = { 0, -1, 2, -3, 4, -5, 6, -7, 8, -9 };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(0, dataInput.readUnsignedByte());
        assertEquals(255, dataInput.readUnsignedByte());
        dataInput.setPosition(6);
        assertEquals(6, dataInput.readUnsignedByte());
        assertEquals(249, dataInput.readUnsignedByte());
        dataInput.setPosition(dataInput.length() - 1);
        assertEquals(247, dataInput.readUnsignedByte());
        assertThrows(IOException.class, () -> dataInput.readUnsignedByte());
    }

    @Test
    void testBasics() throws IOException
    {
        byte[] data = { 0, -1, 2, -3, 4, -5, 6, -7, 8, -9 };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(10, dataInput.length());
        assertTrue(dataInput.hasRemaining());
        assertThrows(IOException.class, () -> dataInput.setPosition(-1));
        int length = dataInput.length();
        assertThrows(IOException.class, () -> dataInput.setPosition(length));
    }

    @Test
    void testPeek() throws IOException
    {
        byte[] data = { 0, -1, 2, -3, 4, -5, 6, -7, 8, -9 };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(0, dataInput.peekUnsignedByte(0));
        assertEquals(251, dataInput.peekUnsignedByte(5));
        assertThrows(IOException.class, () -> dataInput.peekUnsignedByte(-1));
        int length = dataInput.length();
        assertThrows(IOException.class, () -> dataInput.peekUnsignedByte(length));
    }

    @Test
    void testReadShort() throws IOException
    {
        byte[] data = { 0x00, 0x0F, (byte) 0xAA, 0, (byte) 0xFE, (byte) 0xFF };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals((short) 0x000F, dataInput.readShort());
        assertEquals((short) 0xAA00, dataInput.readShort());
        assertEquals((short) 0xFEFF, dataInput.readShort());
        assertThrows(IOException.class, () -> dataInput.readShort());
    }

    @Test
    void testReadUnsignedShort() throws IOException
    {
        byte[] data = { 0x00, 0x0F, (byte) 0xAA, 0, (byte) 0xFE, (byte) 0xFF };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(0x000F, dataInput.readUnsignedShort());
        assertEquals(0xAA00, dataInput.readUnsignedShort());
        assertEquals(0xFEFF, dataInput.readUnsignedShort());
        assertThrows(IOException.class, () -> dataInput.readUnsignedShort());

        byte[] data2 = { 0x00 };
        DataInput dataInput2 = new DataInputByteArray(data2);
        assertThrows(IOException.class, () -> dataInput2.readUnsignedShort());
    }

    @Test
    void testReadInt() throws IOException
    {
        byte[] data = { 0x00, 0x0F, (byte) 0xAA, 0, (byte) 0xFE, (byte) 0xFF, 0x30,
                0x50 };
        DataInput dataInput = new DataInputByteArray(data);
        assertEquals(0x000FAA00, dataInput.readInt());
        assertEquals(0xFEFF3050, dataInput.readInt());
        assertThrows(IOException.class, () -> dataInput.readInt());

        byte[] data2 = { 0x00, 0x0F, (byte) 0xAA };
        DataInput dataInput2 = new DataInputByteArray(data2);
        assertThrows(IOException.class, () -> dataInput2.readInt());

    }
}
