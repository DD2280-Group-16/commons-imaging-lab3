/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.formats.tiff;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.ByteOrder;

import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldTypeAscii;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for TiffField.getValueDescription(Object).
 * Calls getValueDescription via reflection (method is private).
 */
class TiffFieldGetValueDescriptionTest {

    private TiffField field;
    private Method getValueDescription;

    @BeforeEach
    void setUp() throws Exception {
        final FieldTypeAscii ft = new FieldTypeAscii(0, "Test");
        field = new TiffField(0, 0, ft, 0L, 0, new byte[1], ByteOrder.BIG_ENDIAN, 1);
        getValueDescription = TiffField.class.getDeclaredMethod("getValueDescription", Object.class);
        getValueDescription.setAccessible(true);
    }

    private String invoke(final Object value) throws Exception {
        return (String) getValueDescription.invoke(field, new Object[] { value });
    }

    /**
     * When value is null, getValueDescription returns null.
     */
    @Test
    void nullReturnsNull() throws Exception {
        assertNull(invoke(null));
    }

    /**
     * When value is a double array with more than 50 elements, getValueDescription truncates with "... (n)".
     */
    @Test
    void doubleArrayLongTruncates() throws Exception {
        double[] arr = new double[52];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        String out = invoke(arr);
        assertNotNull(out);
        assertTrue(out.contains("... (52)"));
    }
    

    /**
     * When value is an float array with more than 50 elements, getValueDescription truncates with "... (n)".
     */
    @Test
    void floatArrayLongTruncates() throws Exception {
        float[] arr = new float[52];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        String out = invoke(arr);
        assertNotNull(out);
        assertTrue(out.contains("... (52)"));
    }

    /**
     * When value is an unknown type, getValueDescription returns "Unknown: " plus the class name.
     */
    @Test
    void unknownTypeReturnsUnknownClassName() throws Exception {
        String out = invoke(new StringBuilder("x"));
        assertNotNull(out);
        assertTrue(out.startsWith("Unknown: "));
        assertTrue(out.contains("StringBuilder"));
    }
}
