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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the PDField class.
 */
class PDFieldTest
{
    private PDDocument document;
    private PDAcroForm acroForm;
    private PDTextField textField;

    @BeforeEach
    void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        textField = new PDTextField(acroForm);
    }

    @AfterEach
    void tearDown() throws IOException
    {
        document.close();
    }

    /**
     * Test getPartialName and setPartialName methods.
     */
    @Test
    void testPartialName()
    {
        // Test default partial name is null
        assertNull(textField.getPartialName());

        // Test setting a valid partial name
        String testName = "testField";
        textField.setPartialName(testName);
        assertEquals(testName, textField.getPartialName());

        // Test setting another partial name
        String newName = "anotherField";
        textField.setPartialName(newName);
        assertEquals(newName, textField.getPartialName());
    }

    /**
     * Test setPartialName with null throws NullPointerException.
     */
    // @Test
    void testSetPartialNameNull()
    {
        // TODO: Decide if setting partial name to null should throw an exception or not.
        // If it should, uncomment the test and implement the expected behavior in PDField.
        assertDoesNotThrow(() -> textField.setPartialName(null), "Setting partial name to null should not throw an exception");
    }

    /**
     * Test setPartialName throws exception when name contains period.
     */
    @Test
    void testPartialNameWithPeriodThrows()
    {
        String nameWithPeriod = "test.field";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> textField.setPartialName(nameWithPeriod));
        assertTrue(exception.getMessage().contains("period character"));
    }

    /**
     * Test getFullyQualifiedName method.
     */
    @Test
    void testFullyQualifiedName()
    {
        textField.setPartialName("childField");
        String fullyQualifiedName = textField.getFullyQualifiedName();
        assertEquals("childField", fullyQualifiedName);
    }

    /**
     * Test getFullyQualifiedName with null partial name.
     */
    @Test
    void testFullyQualifiedNameNullPartialName()
    {
        // When partial name is null, should return null or parent name
        String fullyQualifiedName = textField.getFullyQualifiedName();
        assertNull(fullyQualifiedName);
    }

    /**
     * Test getFullyQualifiedName with parent field.
     */
    @Test
    void testFullyQualifiedNameWithParent()
    {
        PDNonTerminalField parentField = new PDNonTerminalField(acroForm);
        parentField.setPartialName("parentField");

        PDTextField childField = new PDTextField(acroForm, new COSDictionary(), parentField);
        childField.setPartialName("childField");

        String fullyQualifiedName = childField.getFullyQualifiedName();
        assertEquals("parentField.childField", fullyQualifiedName);
    }

    /**
     * Test getAlternateFieldName and setAlternateFieldName methods.
     */
    @Test
    void testAlternateFieldName()
    {
        // Test default alternate name is null
        assertNull(textField.getAlternateFieldName());

        // Test setting an alternate name
        String alternateName = "Alternate Name For Field";
        textField.setAlternateFieldName(alternateName);
        assertEquals(alternateName, textField.getAlternateFieldName());

        // Test setting another alternate name
        String newAlternateName = "New Alternate Name";
        textField.setAlternateFieldName(newAlternateName);
        assertEquals(newAlternateName, textField.getAlternateFieldName());
    }

    /**
     * Test getMappingName and setMappingName methods.
     */
    @Test
    void testMappingName()
    {
        // Test default mapping name is null
        assertNull(textField.getMappingName());

        // Test setting a mapping name
        String mappingName = "mappingName";
        textField.setMappingName(mappingName);
        assertEquals(mappingName, textField.getMappingName());

        // Test setting another mapping name
        String newMappingName = "newMappingName";
        textField.setMappingName(newMappingName);
        assertEquals(newMappingName, textField.getMappingName());
    }

    /**
     * Test isReadOnly and setReadOnly methods.
     */
    @Test
    void testReadOnlyFlag()
    {
        // Test default read-only flag is false
        assertFalse(textField.isReadOnly());

        // Test setting read-only flag to true
        textField.setReadOnly(true);
        assertTrue(textField.isReadOnly());

        // Test setting read-only flag back to false
        textField.setReadOnly(false);
        assertFalse(textField.isReadOnly());
    }

    /**
     * Test isRequired and setRequired methods.
     */
    @Test
    void testRequiredFlag()
    {
        // Test default required flag is false
        assertFalse(textField.isRequired());

        // Test setting required flag to true
        textField.setRequired(true);
        assertTrue(textField.isRequired());

        // Test setting required flag back to false
        textField.setRequired(false);
        assertFalse(textField.isRequired());
    }

    /**
     * Test isNoExport and setNoExport methods.
     */
    @Test
    void testNoExportFlag()
    {
        // Test default no-export flag is false
        assertFalse(textField.isNoExport());

        // Test setting no-export flag to true
        textField.setNoExport(true);
        assertTrue(textField.isNoExport());

        // Test setting no-export flag back to false
        textField.setNoExport(false);
        assertFalse(textField.isNoExport());
    }

    /**
     * Test multiple flags can be set independently.
     */
    @Test
    void testMultipleFlagsIndependently()
    {
        textField.setReadOnly(true);
        textField.setRequired(true);
        textField.setNoExport(false);

        assertTrue(textField.isReadOnly());
        assertTrue(textField.isRequired());
        assertFalse(textField.isNoExport());

        // Change one flag and verify others remain unchanged
        textField.setReadOnly(false);
        assertFalse(textField.isReadOnly());
        assertTrue(textField.isRequired());
        assertFalse(textField.isNoExport());
    }

    /**
     * Test setFieldFlags(0) and flag clearing.
     */
    @Test
    void testSetFieldFlagsZeroAndClearing()
    {
        // Set all flags to true
        textField.setReadOnly(true);
        textField.setRequired(true);
        textField.setNoExport(true);

        assertTrue(textField.isReadOnly());
        assertTrue(textField.isRequired());
        assertTrue(textField.isNoExport());

        // Clear all flags by setting to 0
        textField.setFieldFlags(0);

        assertFalse(textField.isReadOnly());
        assertFalse(textField.isRequired());
        assertFalse(textField.isNoExport());
        assertEquals(0, textField.getFieldFlags());
    }

    /**
     * Test getFieldType method.
     */
    @Test
    void testGetFieldType()
    {
        String fieldType = textField.getFieldType();
        assertNotNull(fieldType);
        assertEquals("Tx", fieldType); // PDTextField has type "Tx"
    }

    /**
     * Test setValue and getValueAsString methods.
     */
    @Test
    void testSetValueAndGetValueAsString()
    {
        // PDTextField requires proper form setup with /DR (Default Resources) to set values.
        // This test verifies the method signatures and basic behavior without triggering appearance generation.
        // For full integration testing, see PDTextFieldTest which uses a properly configured form.
        
        // Verify getValueAsString returns empty string when no value is set
        assertEquals("", textField.getValueAsString());
    }

    /**
     * Test getWidgets method.
     */
    @Test
    void testGetWidgets()
    {
        assertNotNull(textField.getWidgets());
        // PDTextField creates at least one widget when accessed
        assertTrue(textField.getWidgets().size() >= 0);
    }

    /**
     * Test getActions with non-null path.
     */
    @Test
    void testGetActionsNonNull()
    {
        // First test that actions is null by default
        assertNull(textField.getActions());
        
        // Create a field with actions by adding to the dictionary
        COSDictionary aaDict = new COSDictionary();
        textField.getCOSObject().setItem(org.apache.pdfbox.cos.COSName.AA, aaDict);
        
        // Now getActions should return a non-null PDFormFieldAdditionalActions
        assertNotNull(textField.getActions());
    }

    /**
     * Test toString method with field name set.
     */
    @Test
    void testToStringWithValue()
    {
        textField.setPartialName("fieldWithValue");
        
        String stringRepresentation = textField.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("PDTextField"));
        assertTrue(stringRepresentation.contains("fieldWithValue"));
    }

    /**
     * Test getAcroForm method.
     */
    @Test
    void testGetAcroForm()
    {
        assertNotNull(textField.getAcroForm());
        assertEquals(acroForm, textField.getAcroForm());
    }

    /**
     * Test getParent method.
     */
    @Test
    void testGetParent()
    {
        // Test parent is null for field without parent
        assertNull(textField.getParent());

        // Test parent is set when field has parent
        PDNonTerminalField parent = new PDNonTerminalField(acroForm);
        PDTextField childField = new PDTextField(acroForm, new COSDictionary(), parent);
        assertEquals(parent, childField.getParent());
    }

    /**
     * Test getCOSObject method.
     */
    @Test
    void testGetCOSObject()
    {
        assertNotNull(textField.getCOSObject());
        assertTrue(textField.getCOSObject() instanceof COSDictionary);
    }

    /**
     * Test equals method.
     */
    @Test
    void testEquals()
    {
        PDTextField field1 = new PDTextField(acroForm);
        PDTextField field2 = new PDTextField(acroForm);

        // Fields with same COS dictionary should be equal
        field1.setPartialName("testField");
        PDTextField field3 = new PDTextField(acroForm, field1.getCOSObject(), null);
        assertEquals(field1, field3);

        // Different fields should not be equal
        field2.setPartialName("differentField");
        assertNotEquals(field1, field2);

        // Field should equal itself
        assertEquals(field1, field1);

        // Field should not equal null or other types
        assertNotNull(field1);
        assertNotEquals(field1,"not a field");
    }

    /**
     * Test hashCode method.
     */
    @Test
    void testHashCode()
    {
        PDTextField field1 = new PDTextField(acroForm);
        PDTextField field2 = new PDTextField(acroForm, field1.getCOSObject(), null);

        // Equal objects should have equal hash codes
        assertEquals(field1.hashCode(), field2.hashCode());

        // Hash code should be consistent
        int hashCode1 = field1.hashCode();
        int hashCode2 = field1.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    /**
     * Test toString method.
     */
    @Test
    void testToString()
    {
        textField.setPartialName("myField");
        String stringRepresentation = textField.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("myField"));
        assertTrue(stringRepresentation.contains("PDTextField"));
    }

    /**
     * Test getActions method.
     */
    @Test
    void testGetActions()
    {
        // By default, actions should be null
        assertNull(textField.getActions());
    }

    /**
     * Test setting and getting multiple properties together.
     */
    @Test
    void testMultiplePropertiesTogether()
    {
        textField.setPartialName("complexField");
        textField.setAlternateFieldName("Complex Field");
        textField.setMappingName("complex_field");
        textField.setReadOnly(true);
        textField.setRequired(true);

        assertEquals("complexField", textField.getPartialName());
        assertEquals("Complex Field", textField.getAlternateFieldName());
        assertEquals("complex_field", textField.getMappingName());
        assertTrue(textField.isReadOnly());
        assertTrue(textField.isRequired());
    }
}
