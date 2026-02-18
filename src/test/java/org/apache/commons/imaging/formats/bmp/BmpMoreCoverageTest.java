/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.formats.bmp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.junit.jupiter.api.Test;

public class BmpMoreCoverageTest {

    @Test
    public void testUnknownCompressionException() throws Exception {
        // Requirement: The parser must reject BMP files that specify an unknown or unsupported compression method.
        File file = new File("src/test/resources/data/images/bmp/4/rle4.bmp");
        byte[] bytes = Files.readAllBytes(file.toPath());

        // Intentionally corrupt the Compression field.
        bytes[30] = 99; 

        // Pass the corrupted bytes to the parser and assert that it throws our expected Exception.
        ImagingException ex = assertThrows(ImagingException.class, () -> {
            Imaging.getBufferedImage(bytes);
        });

        // Verify the assertion matches the documented requirement.
        assertTrue(ex.getMessage().contains("Unknown Compression"));
    }

    @Test
    public void testInvalidBitmapHeaderSizeException() throws Exception {
        // Requirement: The parser must reject BMP files with a header size smaller than the standard 40 bytes.
        File file = new File("src/test/resources/data/images/bmp/4/rle4.bmp");
        byte[] bytes = Files.readAllBytes(file.toPath());

        // The 'bitmapHeaderSize' field is a 4-byte integer starting at offset 14.
        // Setting it to 39 will trigger the header size exception.
        bytes[14] = 39;
        bytes[15] = 0;
        bytes[16] = 0;
        bytes[17] = 0;

        ImagingException ex = assertThrows(ImagingException.class, () -> {
            Imaging.getBufferedImage(bytes);
        });

        // Assertion based on the documented requirement
        assertTrue(ex.getMessage().contains("Invalid/unsupported BMP file"));
    }

    @Test
    public void testDataOffsetTooSmallException() throws Exception {
        // Requirement: The parser must reject BMP files where the image data offset is smaller than the expected header and palette size.
        // This targets the left side of: if (extraBytes < 0 || extraBytes > bhi.fileSize)
        File file = new File("src/test/resources/data/images/bmp/4/rle4.bmp");
        byte[] bytes = Files.readAllBytes(file.toPath());

        // The 'bitmapDataOffset' field is a 4-byte integer starting at offset 10.
        // Setting it to 0 will cause the extraBytes calculation to be negative.
        bytes[10] = 0;
        bytes[11] = 0;
        bytes[12] = 0;
        bytes[13] = 0;

        ImagingException ex = assertThrows(ImagingException.class, () -> {
            Imaging.getBufferedImage(bytes);
        });

        // Assertion based on the documented requirement
        assertTrue(ex.getMessage().contains("invalid image data offset"));
    }

    @Test
    public void testDataOffsetExceedsFileSizeException() throws Exception {
        // Requirement: The parser must reject BMP files where the calculated extra bytes padding exceeds the total file size.
        // This targets the right side of: if (extraBytes < 0 || extraBytes > bhi.fileSize)
        File file = new File("src/test/resources/data/images/bmp/4/rle4.bmp");
        byte[] bytes = Files.readAllBytes(file.toPath());

        // Get the actual file size directly from the header (offset 2)
        int fileSize = (bytes[2] & 0xFF) | ((bytes[3] & 0xFF) << 8) | ((bytes[4] & 0xFF) << 16) | ((bytes[5] & 0xFF) << 24);

        // Set the offset to slightly larger than the file size to avoid integer overflow issues
        // while guaranteeing that extraBytes > fileSize evaluates to true.
        int newOffset = fileSize + 1024; 
        bytes[10] = (byte) (newOffset & 0xFF);
        bytes[11] = (byte) ((newOffset >> 8) & 0xFF);
        bytes[12] = (byte) ((newOffset >> 16) & 0xFF);
        bytes[13] = (byte) ((newOffset >> 24) & 0xFF);

        ImagingException ex = assertThrows(ImagingException.class, () -> {
            Imaging.getBufferedImage(bytes);
        });

        // Assertion based on the documented requirement
        assertTrue(ex.getMessage().contains("invalid image data offset"));
    }
}
