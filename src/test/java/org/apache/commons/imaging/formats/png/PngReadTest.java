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

package org.apache.commons.imaging.formats.png;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.bytesource.ByteSource;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.internal.Debug;
import org.apache.commons.imaging.test.TestResources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class PngReadTest extends AbstractPngTest {

    @AfterAll
    public static void printFinalReport() {
        final int count = 28;
        final int hits = DiyTool.getLength();

        System.err.println("Total Reached: " + hits + " / " + count);
        System.err.printf("Percentage:    %.2f%%%n", (double) hits / count * 100);
    }

    @Test
    void test() throws Exception {
        Debug.debug("start");

        final List<File> images = getPngImages();
        for (final File imageFile : images) {

            Debug.debug("imageFile", imageFile);
            if (isInvalidPngTestFile(imageFile)) {
                assertThrows(Exception.class, () -> Imaging.getMetadata(imageFile), "Image read should have failed.");

                assertThrows(Exception.class, () -> Imaging.getImageInfo(imageFile), "Image read should have failed.");

                assertThrows(Exception.class, () -> Imaging.getBufferedImage(imageFile),
                        "Image read should have failed.");
            } else {
                final ImageMetadata metadata = Imaging.getMetadata(imageFile);
                assertFalse(metadata instanceof File); // Dummy check to avoid unused warning (it may be null)

                final ImageInfo imageInfo = Imaging.getImageInfo(imageFile);
                assertNotNull(imageInfo);

                Debug.debug("ICC profile", Imaging.getIccProfile(imageFile));

                final BufferedImage image = Imaging.getBufferedImage(imageFile);
                assertNotNull(image);
            }
        }
    }

    /**
     * Test reading EXIF from the 'eXIf' chunk in PNG file.
     *
     * @throws IOException      if it fails to read the test image
     * @throws ImagingException if it fails to read the test image
     */
    @Test
    void testReadExif() throws IOException, ImagingException {

        final String input = "/images/png/IMAGING-340/image-with-exif.png";
        final String file = PngReadTest.class.getResource(input).getFile();
        final PngImageParser parser = new PngImageParser();

        final PngImageMetadata pngMetadata = (PngImageMetadata) parser.getMetadata(new File(file));

        final TiffImageMetadata exifMetadata = pngMetadata.getExif();
        assertEquals("Glavo",
                exifMetadata.findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_ROOT)
                        .getFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION));

        final PngImageMetadata metadata = (PngImageMetadata) parser.getMetadata(new File(file));
        assertTrue(metadata.getTextualInformation().getItems().isEmpty());
        assertEquals("Glavo",
                metadata.getExif()
                        .findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_ROOT)
                        .getFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION));

    }

    /**
     * Test reading metadata from PNG file with UTF-8 characters in the text chunks.
     *
     * @see <a href=
     *      "https://issues.apache.org/jira/browse/IMAGING-342">IMAGING-342</a>
     * @throws IOException      if it fails to read the test image
     * @throws ImagingException if it fails to read the test image
     */
    @Test
    void testReadMetadataFromItxtChunk() throws IOException, ImagingException {

        final File file = TestResources.resourceToFile("/images/png/IMAGING-342/utf8-comment.png");
        final PngImageParser parser = new PngImageParser();

        final ImageMetadata metadata = parser.getMetadata(file);
        final List<?> items = metadata.getItems();

        assertEquals(1, items.size());

        final GenericImageMetadata.GenericImageMetadataItem item = (GenericImageMetadata.GenericImageMetadataItem) items
                .get(0);
        assertEquals("Comment", item.getKeyword());

        assertEquals("\u2192 UTF-8 Test", item.getText());

    }

    /**
     * If the PNG image data contains an invalid ICC Profile, previous versions
     * would simply rethrow the IAE. This test verifies we are instead raising the
     * documented {@literal ImageReadException}.
     *
     * <p>
     * See Google OSS Fuzz issue 33691
     * </p>
     *
     * @throws IOException if it fails to read the test image
     */
    @Test
    void testUncaughtExceptionOssFuzz33691() throws IOException {
        final File file = TestResources.resourceToFile(
                "/images/png/oss-fuzz-33691/clusterfuzz-testcase-minimized-ImagingPngFuzzer-6177282101215232");
        final PngImageParser parser = new PngImageParser();
        assertThrows(ImagingException.class,
                () -> parser.getBufferedImage(ByteSource.file(file), new PngImagingParameters()));
    }

    /**
     * Test that a PNG image using indexed color type but no PLTE chunks does not
     * throw a {@code NullPointerException}.
     *
     * <p>
     * See Google OSS Fuzz issue 37607
     * </p>
     *
     * @throws IOException if it fails to read the test image
     */
    @Test
    void testUncaughtExceptionOssFuzz37607() throws IOException {
        final File file = TestResources.resourceToFile(
                "/images/png/IMAGING-317/clusterfuzz-testcase-minimized-ImagingPngFuzzer-6242400830357504");
        final PngImageParser parser = new PngImageParser();
        assertThrows(ImagingException.class,
                () -> parser.getBufferedImage(ByteSource.file(file), new PngImagingParameters()));
    }

    /**
     * Tests that the PngImageParser throws an ImagingException when attempting to
     * parse a PNG
     * byte array that contains only the PNG signature and the IEND chunk,
     * i.e., no actual image data or other chunks.
     */
    @Test
    void testChunksIsEmpty() {
        final PngImageParser parser = new PngImageParser();

        final byte[] bytes = new byte[] {
                // PNG Signature
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,

                // IEND Chunk
                0, 0, 0, 0,
                'I', 'E', 'N', 'D',
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
        final ImagingException ex = assertThrows(ImagingException.class, () -> {
            parser.getBufferedImage(ByteSource.array(bytes), new PngImagingParameters());
        });

        assertTrue(ex.getMessage().contains("PNG: no chunks"));

    }

    /**
     * Tests that the PngImageParser throws an ImagingException
     * when a PNG file contains more than one IHDR (header) chunk.
     *
     * @throws ImagingException
     */
    @Test
    void testNoIhdrHeader() {
        final PngImageParser parser = new PngImageParser();

        final byte[] bytes = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,

                0, 0, 0, 13,
                'I', 'H', 'D', 'R',
                0, 0, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 2, 0,
                0, 0, 0, 0,

                0, 0, 0, 13,
                'I', 'H', 'D', 'R',
                0, 0, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 2, 0,
                0, 0, 0, 0,

                0, 0, 0, 0,
                'I', 'E', 'N', 'D',
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        final ImagingException ex = assertThrows(ImagingException.class, () -> {
            parser.getBufferedImage(ByteSource.array(bytes), new PngImagingParameters());
        });

        assertTrue(ex.getMessage().contains("PNG contains more than one Header"));
    }

    @Test
    void testMissingPNGData() {
        final PngImageParser parser = new PngImageParser();

        final byte[] bytes = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,

                0, 0, 0, 13,
                'I', 'H', 'D', 'R',
                0, 0, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 2, 0,
                0, 0, 0, 0,

                0, 0, 0, 0,
                'I', 'E', 'N', 'D',
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        final ImagingException ex = assertThrows(ImagingException.class, () -> {
            parser.getBufferedImage(ByteSource.array(bytes), new PngImagingParameters());
        });

        assertTrue(ex.getMessage().contains("PNG missing image data"));
    }

    @Test
    void testNoPLTEHeader() {
        final PngImageParser parser = new PngImageParser();

        final byte[] bytes = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,

                0, 0, 0, 13,
                'I', 'H', 'D', 'R',
                0, 0, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 2, 0,
                0, 0, 0, 0,

                0, 0, 0, 12,
                'P', 'L', 'T', 'E',
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,

                0, 0, 0, 12,
                'P', 'L', 'T', 'E',
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,

                0, 0, 0, 0,
                'I', 'E', 'N', 'D',
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        final ImagingException ex = assertThrows(ImagingException.class, () -> {
            parser.getBufferedImage(ByteSource.array(bytes), new PngImagingParameters());
        });

        assertTrue(ex.getMessage().contains("PNG contains more than one Palette"));
    }

}
