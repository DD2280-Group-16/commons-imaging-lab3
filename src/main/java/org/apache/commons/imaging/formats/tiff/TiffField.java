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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.formats.tiff;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.Allocator;
import org.apache.commons.imaging.common.BinaryFunctions;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.AbstractFieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

/**
 * A TIFF field in a TIFF directory. Immutable.
 */
public class TiffField {

    /**
     * Represents an oversized field value that is stored outside the IFD entry.
     */
    public final class OversizeValueElement extends AbstractTiffElement {

        /**
         * Constructs a new instance.
         *
         * @param offset the offset.
         * @param length the length.
         */
        public OversizeValueElement(final int offset, final int length) {
            super(offset, length);
        }

        @Override
        public String getElementDescription() {
            return "OversizeValueElement, tag: " + getTagInfo().name + ", fieldType: " + getFieldType().getName();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(TiffField.class.getName());

    /** Number of branches in getValueDescription(Object) for DIY coverage (Task 1). One ID per branch outcome (null, Number, String, ...). */
    private static final int NUM_BRANCHES_GET_VALUE_DESCRIPTION = 74;
    /** Branch coverage flags for getValueDescription(Object). Written by tests; dump at end. */
    public static final boolean[] coverageGetValueDescription = new boolean[NUM_BRANCHES_GET_VALUE_DESCRIPTION];

    /**
     * Resets branch coverage for getValueDescription (call before a test run).
     */
    public static void resetCoverageGetValueDescription() {
        Arrays.fill(coverageGetValueDescription, false);
    }

    /**
     * Writes which branches were taken to the given writer (call at end of test run).
     *
     * @param out where to print (e.g. System.out or a file).
     */
    public static void dumpCoverageGetValueDescription(final PrintWriter out) {
        int taken = 0;
        for (int i = 0; i < coverageGetValueDescription.length; i++) {
            if (coverageGetValueDescription[i]) {
                taken++;
                out.println("  Branch " + i + ": taken");
            }
        }
        out.println("TiffField.getValueDescription coverage: " + taken + "/" + NUM_BRANCHES_GET_VALUE_DESCRIPTION
                + " (" + (100.0 * taken / NUM_BRANCHES_GET_VALUE_DESCRIPTION) + "%)");
    }

    private final TagInfo tagInfo;
    private final int tag;
    private final int directoryType;
    private final AbstractFieldType abstractFieldType;
    private final long count;
    private final long offset;
    private final byte[] value;
    private final ByteOrder byteOrder;
    private final int sortHint;

    /**
     * Constructs a new instance.
     *
     * @param tag the tag number.
     * @param directoryType the directory type.
     * @param abstractFieldType the field type.
     * @param count the count.
     * @param offset the offset.
     * @param value the value bytes.
     * @param byteOrder the byte order.
     * @param sortHint the sort hint.
     */
    public TiffField(final int tag, final int directoryType, final AbstractFieldType abstractFieldType, final long count, final long offset, final byte[] value,
            final ByteOrder byteOrder, final int sortHint) {

        this.tag = tag;
        this.directoryType = directoryType;
        this.abstractFieldType = abstractFieldType;
        this.count = count;
        this.offset = offset;
        this.value = value;
        this.byteOrder = byteOrder;
        this.sortHint = sortHint;

        tagInfo = TiffTags.getTag(directoryType, tag);
    }

    /**
     * Dumps field information to the logger.
     */
    public void dump() {
        try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
            dump(pw);
            pw.flush();
            sw.flush();
            LOGGER.fine(sw.toString());
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Dumps field information to a PrintWriter.
     *
     * @param pw the PrintWriter.
     */
    public void dump(final PrintWriter pw) {
        dump(pw, null);
    }

    /**
     * Dumps field information to a PrintWriter with an optional prefix.
     *
     * @param pw the PrintWriter.
     * @param prefix the prefix string, or null.
     */
    public void dump(final PrintWriter pw, final String prefix) {
        if (prefix != null) {
            pw.print(prefix + ": ");
        }

        pw.println(toString());
        pw.flush();
    }

    /**
     * Gets a copy of the raw value of the field.
     *
     * @return the value of the field, in the byte order of the field.
     */
    public byte[] getByteArrayValue() {
        return BinaryFunctions.copyOfStart(value, getBytesLength());
    }

    /**
     * Gets the field's byte order.
     *
     * @return the byte order
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Gets the length of the field's value.
     *
     * @return the length, in bytes.
     */
    public int getBytesLength() {
        return (int) count * abstractFieldType.getSize();
    }

    /**
     * Gets the field's count, derived from bytes 4-7.
     *
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * Gets a description of this field without the value.
     *
     * @return the description.
     */
    public String getDescriptionWithoutValue() {
        return getTag() + " (0x" + Integer.toHexString(getTag()) + ": " + getTagInfo().name + "): ";
    }

    /**
     * Gets the directory type.
     *
     * @return the directory type.
     */
    public int getDirectoryType() {
        return directoryType;
    }

    /**
     * Gets the field value as a double array.
     *
     * @return the double array value.
     * @throws ImagingException if the value cannot be converted.
     */
    public double[] getDoubleArrayValue() throws ImagingException {
        final Object o = getValue();
        // if (o == null)
        // return null;

        if (o instanceof Number) {
            return new double[] { ((Number) o).doubleValue() };
        }
        if (o instanceof Number[]) {
            final Number[] numbers = (Number[]) o;
            final double[] result = Allocator.doubleArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i].doubleValue());
            return result;
        }
        if (o instanceof short[]) {
            final short[] numbers = (short[]) o;
            final double[] result = Allocator.doubleArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i]);
            return result;
        }
        if (o instanceof int[]) {
            final int[] numbers = (int[]) o;
            final double[] result = Allocator.doubleArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i]);
            return result;
        }
        if (o instanceof float[]) {
            final float[] numbers = (float[]) o;
            final double[] result = Allocator.doubleArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i]);
            return result;
        }
        if (o instanceof double[]) {
            final double[] numbers = (double[]) o;
            return Arrays.copyOf(numbers, numbers.length);
        }

        throw new ImagingException("Unknown value: " + o + " for: " + getTagInfo().getDescription());
        // return null;
    }

    /**
     * Gets the field value as a single double.
     *
     * @return the double value.
     * @throws ImagingException if the value is missing or cannot be converted.
     */
    public double getDoubleValue() throws ImagingException {
        final Object o = getValue();
        if (o == null) {
            throw new ImagingException("Missing value: " + getTagInfo().getDescription());
        }

        return ((Number) o).doubleValue();
    }

    /**
     * Gets the field's type, derived from bytes 2-3.
     *
     * @return the field's type, as a {@code FieldType} object.
     */
    public AbstractFieldType getFieldType() {
        return abstractFieldType;
    }

    /**
     * Gets the field type name.
     *
     * @return the field type name.
     */
    public String getFieldTypeName() {
        return getFieldType().getName();
    }

    /**
     * Gets the field value as an int array.
     *
     * @return the int array value.
     * @throws ImagingException if the value cannot be converted.
     */
    public int[] getIntArrayValue() throws ImagingException {
        final Object o = getValue();
        // if (o == null)
        // return null;

        if (o instanceof Number) {
            return new int[] { ((Number) o).intValue() };
        }
        if (o instanceof Number[]) {
            final Number[] numbers = (Number[]) o;
            final int[] result = Allocator.intArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i].intValue());
            return result;
        }
        if (o instanceof short[]) {
            final short[] numbers = (short[]) o;
            final int[] result = Allocator.intArray(numbers.length);
            Arrays.setAll(result, i -> 0xffff & numbers[i]);
            return result;
        }
        if (o instanceof int[]) {
            final int[] numbers = (int[]) o;
            return Arrays.copyOf(numbers, numbers.length);
        }
        if (o instanceof long[]) {
            final long[] numbers = (long[]) o;
            final int[] iNumbers = new int[numbers.length];
            for (int i = 0; i < iNumbers.length; i++) {
                iNumbers[i] = (int) numbers[i];
            }
            return iNumbers;
        }

        throw new ImagingException("Unknown value: " + o + " for: " + getTagInfo().getDescription());
        // return null;
    }

    /**
     * Gets the field value as a single int.
     *
     * @return the int value.
     * @throws ImagingException if the value is missing or cannot be converted.
     */
    public int getIntValue() throws ImagingException {
        final Object o = getValue();
        if (o == null) {
            throw new ImagingException("Missing value: " + getTagInfo().getDescription());
        }

        return ((Number) o).intValue();
    }

    /**
     * Gets the int value or sum of array values.
     *
     * @return the int value or sum.
     * @throws ImagingException if an error occurs.
     */
    public int getIntValueOrArraySum() throws ImagingException {
        final Object o = getValue();
        // if (o == null)
        // return -1;

        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o instanceof Number[]) {
            final Number[] numbers = (Number[]) o;
            int sum = 0;
            for (final Number number : numbers) {
                sum += number.intValue();
            }
            return sum;
        }
        if (o instanceof short[]) {
            final short[] numbers = (short[]) o;
            int sum = 0;
            for (final short number : numbers) {
                sum += number;
            }
            return sum;
        }
        if (o instanceof int[]) {
            final int[] numbers = (int[]) o;
            int sum = 0;
            for (final int number : numbers) {
                sum += number;
            }
            return sum;
        }

        throw new ImagingException("Unknown value: " + o + " for: " + getTagInfo().getDescription());
        // return -1;
    }

    /**
     * Gets the value of the field in the form of an array of eight-byte (long) integers.
     *
     * @return an valid array of size zero or larger giving signed long integer values.
     * @throws ImagingException if the field instance is of an incompatible type or does not contain a valid data element.
     */
    public long[] getLongArrayValue() throws ImagingException {
        final Object o = getValue();
        if (o instanceof Number) {
            return new long[] { ((Number) o).longValue() };
        }
        if (o instanceof Number[]) {
            final Number[] numbers = (Number[]) o;
            final long[] result = Allocator.longArray(numbers.length);
            Arrays.setAll(result, i -> numbers[i].longValue());
            return result;
        }
        if (o instanceof short[]) {
            final short[] numbers = (short[]) o;
            final long[] result = Allocator.longArray(numbers.length);
            Arrays.setAll(result, i -> 0xffff & numbers[i]);
            return result;
        }
        if (o instanceof int[]) {
            final int[] numbers = (int[]) o;
            final long[] result = Allocator.longArray(numbers.length);
            Arrays.setAll(result, i -> 0xFFFFffffL & numbers[i]);
            return result;
        }
        if (o instanceof long[]) {
            final long[] numbers = (long[]) o;
            return Arrays.copyOf(numbers, numbers.length);
        }

        throw new ImagingException("Unknown value: " + o + " for: " + getTagInfo().getDescription());
    }

    /**
     * Gets the value of the field in the form of an eight-byte (long) integer.
     *
     * @return a signed long integer value.
     * @throws ImagingException if the field instance is of an incompatible type or does not contain a valid data element.
     */
    public long getLongValue() throws ImagingException {
        final Object o = getValue();
        if (o == null) {
            throw new ImagingException("Missing value: " + getTagInfo().getDescription());
        }
        return ((Number) o).longValue();
    }

    /**
     * Gets the TIFF field's offset/value field, derived from bytes 8-11.
     *
     * @return the field's offset in a {@code long} of 4 packed bytes, or its inlined value &lt;= 4 bytes long encoded in the field's byte order.
     */
    public long getOffset() {
        return (int) offset;
    }

    /**
     * Gets the oversized value element if this field has one.
     *
     * @return the oversized value element, or null.
     */
    public AbstractTiffElement getOversizeValueElement() {
        if (isLocalValue()) {
            return null;
        }

        return new OversizeValueElement((int) getOffset(), value.length);
    }

    /**
     * Gets the sort hint for ordering fields.
     *
     * @return the sort hint.
     */
    public int getSortHint() {
        return sortHint;
    }

    /**
     * Gets the field's string value.
     *
     * @return the string value.
     * @throws ImagingException if an error occurs.
     */
    public String getStringValue() throws ImagingException {
        final Object o = getValue();
        if (o == null) {
            return null;
        }
        if (!(o instanceof String)) {
            throw new ImagingException("Expected String value(" + getTagInfo().getDescription() + "): " + o);
        }
        return (String) o;
    }

    /**
     * Gets the field's tag, derived from bytes 0-1.
     *
     * @return the tag, as an {@code int} in which only the lowest 2 bytes are set.
     */
    public int getTag() {
        return tag;
    }

    /**
     * Gets tag information.
     *
     * @return the tag info.
     */
    public TagInfo getTagInfo() {
        return tagInfo;
    }

    /**
     * Gets the tag name.
     *
     * @return the tag name.
     */
    public String getTagName() {
        if (getTagInfo() == TiffTagConstants.TIFF_TAG_UNKNOWN) {
            return getTagInfo().name + " (0x" + Integer.toHexString(getTag()) + ")";
        }
        return getTagInfo().name;
    }

    /**
     * Gets the value of the field.
     *
     * @return the value of the field.
     * @throws ImagingException if an error occurs.
     */
    public Object getValue() throws ImagingException {
        // System.out.print("getValue");
        return getTagInfo().getValue(this);
    }

    /**
     * Gets a description of the field's value.
     *
     * @return the value description.
     */
    public String getValueDescription() {
        try {
            return getValueDescription(getValue());
        } catch (final ImagingException e) {
            return "Invalid value: " + e.getMessage();
        }
    }

    private String getValueDescription(final Object o) {
        /*
            Branch id: 0
            Branch id: 1
        */
        if (o == null) {
            coverageGetValueDescription[0] = true;
            return null;
        } else {
            coverageGetValueDescription[1] = true;
        }

        /*
            Branch id: 2
            Branch id: 3
        */
        if (o instanceof Number) {
            coverageGetValueDescription[2] = true;
            return o.toString();
        } else {
            coverageGetValueDescription[3] = true;
        }

        /*
            Branch id: 4
            Branch id: 5
        */
        if (o instanceof String) {
            coverageGetValueDescription[4] = true;
            return "'" + o.toString().trim() + "'";
        } else {
            coverageGetValueDescription[5] = true;
        }

        /*
            Branch id: 6
            Branch id: 7
        */
        if (o instanceof Date) {
            coverageGetValueDescription[6] = true;
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);
            return df.format((Date) o);
        } else {
            coverageGetValueDescription[7] = true;
        }

        /*
            Branch id: 8
            Branch id: 9
        */
        if (o instanceof Object[]) {
            coverageGetValueDescription[8] = true;
            final Object[] objects = (Object[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 10
                Branch id: 11
            */
            if (objects.length > 0) {
                coverageGetValueDescription[10] = true;
            } else {
                coverageGetValueDescription[11] = true;
            }

            for (int i = 0; i < objects.length; i++) {
                final Object object = objects[i];

                /*
                    Branch id: 12
                    Branch id: 13
                */
                if (i > 50) {
                    coverageGetValueDescription[12] = true;
                    result.append("... (").append(objects.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[13] = true;
                }

                /*
                    Branch id: 14
                    Branch id: 15
                */
                if (i > 0) {
                    coverageGetValueDescription[14] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[15] = true;
                }

                result.append(object.toString());
            }
            return result.toString();
        } else {
            coverageGetValueDescription[9] = true;
        }

        /*
            Branch id: 16
            Branch id: 17
        */
        if (o instanceof short[]) {
            coverageGetValueDescription[16] = true;
            final short[] values = (short[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 18
                Branch id: 19
            */
            if (values.length > 0) {
                coverageGetValueDescription[18] = true;
            } else {
                coverageGetValueDescription[19] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final short sVal = values[i];

                /*
                    Branch id: 20
                    Branch id: 21
                */
                if (i > 50) {
                    coverageGetValueDescription[20] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[21] = true;
                }

                /*
                    Branch id: 22
                    Branch id: 23
                */
                if (i > 0) {
                    coverageGetValueDescription[22] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[23] = true;
                }

                result.append(sVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[17] = true;
        }

        /*
            Branch id: 24
            Branch id: 25
        */
        if (o instanceof int[]) {
            coverageGetValueDescription[24] = true;
            final int[] values = (int[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 26
                Branch id: 27
            */
            if (values.length > 0) {
                coverageGetValueDescription[26] = true;
            } else {
                coverageGetValueDescription[27] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final int iVal = values[i];

                /*
                    Branch id: 28
                    Branch id: 29
                */
                if (i > 50) {
                    coverageGetValueDescription[28] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[29] = true;
                }

                /*
                    Branch id: 30
                    Branch id: 31
                */
                if (i > 0) {
                    coverageGetValueDescription[30] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[31] = true;
                }

                result.append(iVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[25] = true;
        }

        /*
            Branch id: 32
            Branch id: 33
        */
        if (o instanceof long[]) {
            coverageGetValueDescription[32] = true;
            final long[] values = (long[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 34
                Branch id: 35
            */
            if (values.length > 0) {
                coverageGetValueDescription[34] = true;
            } else {
                coverageGetValueDescription[35] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final long lVal = values[i];

                /*
                    Branch id: 36
                    Branch id: 37
                */
                if (i > 50) {
                    coverageGetValueDescription[36] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[37] = true;
                }

                /*
                    Branch id: 38
                    Branch id: 39
                */
                if (i > 0) {
                    coverageGetValueDescription[38] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[39] = true;
                }

                result.append(lVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[33] = true;
        }

        /*
            Branch id: 40
            Branch id: 41
        */
        if (o instanceof double[]) {
            coverageGetValueDescription[40] = true;
            final double[] values = (double[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 42
                Branch id: 43
            */
            if (values.length > 0) {
                coverageGetValueDescription[42] = true;
            } else {
                coverageGetValueDescription[43] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final double dVal = values[i];

                /*
                    Branch id: 44
                    Branch id: 45
                */
                if (i > 50) {
                    coverageGetValueDescription[44] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[45] = true;
                }

                /*
                    Branch id: 46
                    Branch id: 47
                */
                if (i > 0) {
                    coverageGetValueDescription[46] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[47] = true;
                }

                result.append(dVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[41] = true;
        }

        /*
            Branch id: 48
            Branch id: 49
        */
        if (o instanceof byte[]) {
            coverageGetValueDescription[48] = true;
            final byte[] values = (byte[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 50
                Branch id: 51
            */
            if (values.length > 0) {
                coverageGetValueDescription[50] = true;
            } else {
                coverageGetValueDescription[51] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final byte bVal = values[i];

                /*
                    Branch id: 52
                    Branch id: 53
                */
                if (i > 50) {
                    coverageGetValueDescription[52] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[53] = true;
                }

                /*
                    Branch id: 54
                    Branch id: 55
                */
                if (i > 0) {
                    coverageGetValueDescription[54] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[55] = true;
                }

                result.append(bVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[49] = true;
        }

        /*
            Branch id: 56
            Branch id: 57
        */
        if (o instanceof char[]) {
            coverageGetValueDescription[56] = true;
            final char[] values = (char[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 58
                Branch id: 59
            */
            if (values.length > 0) {
                coverageGetValueDescription[58] = true;
            } else {
                coverageGetValueDescription[59] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final char cVal = values[i];

                /*
                    Branch id: 60
                    Branch id: 61
                */
                if (i > 50) {
                    coverageGetValueDescription[60] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[61] = true;
                }

                /*
                    Branch id: 62
                    Branch id: 63
                */
                if (i > 0) {
                    coverageGetValueDescription[62] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[63] = true;
                }

                result.append(cVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[57] = true;
        }

        /*
            Branch id: 64
            Branch id: 65
        */
        if (o instanceof float[]) {
            coverageGetValueDescription[64] = true;
            final float[] values = (float[]) o;
            final StringBuilder result = new StringBuilder();

            /*
                Branch id: 66
                Branch id: 67
            */
            if (values.length > 0) {
                coverageGetValueDescription[66] = true;
            } else {
                coverageGetValueDescription[67] = true;
            }

            for (int i = 0; i < values.length; i++) {
                final float fVal = values[i];

                /*
                    Branch id: 68
                    Branch id: 69
                */
                if (i > 50) {
                    coverageGetValueDescription[68] = true;
                    result.append("... (").append(values.length).append(")");
                    break;
                } else {
                    coverageGetValueDescription[69] = true;
                }

                /*
                    Branch id: 70
                    Branch id: 71
                */
                if (i > 0) {
                    coverageGetValueDescription[70] = true;
                    result.append(", ");
                } else {
                    coverageGetValueDescription[71] = true;
                }

                result.append(fVal);
            }
            return result.toString();
        } else {
            coverageGetValueDescription[65] = true;
        }

        /*
            Branch id: 72
        */
        coverageGetValueDescription[72] = true;
        return "Unknown: " + o.getClass().getName();
    }


    /**
     * Indicates whether the field's value is inlined into the offset field.
     *
     * @return true if the value is inlined
     */
    public boolean isLocalValue() {
        return count * abstractFieldType.getSize() <= TiffConstants.ENTRY_MAX_VALUE_LENGTH;
    }

    @Override
    public String toString() {
        return getTag() + " (0x" + Integer.toHexString(getTag()) + ": " + getTagInfo().name + "): " + getValueDescription() + " (" + getCount() + " "
                + getFieldType().getName() + ")";
    }
}
