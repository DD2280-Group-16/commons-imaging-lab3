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

package org.apache.commons.imaging;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.imaging.bytesource.ByteSource;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ImagingGuessFormatTest extends AbstractImagingTest {

    public static final String BMP_IMAGE_FILE = "bmp\\1\\Oregon Scientific DS6639 - DSC_0307 - small.bmp";
    public static final String PNG_IMAGE_FILE = "png\\1\\Oregon Scientific DS6639 - DSC_0307 - small.png";
    public static final String GIF_IMAGE_FILE = "gif\\single\\1\\Oregon Scientific DS6639 - DSC_0307 - small.gif";
    public static final String ICNS_IMAGE_FILE = "icns\\1\\poedit48x48.icns";
    /*
        Added "ico\\1\\favicon.ico";
    */
    public static final String ICO_IMAGE_FILE = "ico\\1\\favicon.ico";
    public static final String TIFF_IMAGE_FILE = "tiff\\1\\Oregon Scientific DS6639 - DSC_0307 - small.tif";
    public static final String JPEG_IMAGE_FILE = "jpg\\1\\Oregon Scientific DS6639 - DSC_0307 - small.jpg";
    public static final String PSD_IMAGE_FILE = "psd\\1\\Oregon Scientific DS6639 - DSC_0307 - small.psd";
    public static final String PBM_IMAGE_FILE = "pbm\\1\\Oregon Scientific DS6639 - DSC_0307 - small.pbm";
    public static final String PGM_IMAGE_FILE = "pbm\\1\\Oregon Scientific DS6639 - DSC_0307 - small.pgm";
    public static final String PPM_IMAGE_FILE = "pbm\\1\\Oregon Scientific DS6639 - DSC_0307 - small.ppm";
    /*
        Added "tga\\1\\earth.tga";
    */
    public static final String TGA_IMAGE_FILE = "tga\\1\\earth.tga";
    /*
        Added "pnm\\1\\sample.pnm";
    */
    public static final String PNM_IMAGE_FILE = "pnm\\1\\sample.pnm";
    public static final String UNKNOWN_IMAGE_FILE = "info.txt";

    public static Stream<Object[]> data() {
        return Arrays.asList(new Object[] { ImageFormats.PNG, PNG_IMAGE_FILE }, new Object[] { ImageFormats.GIF, GIF_IMAGE_FILE },
                new Object[] { ImageFormats.ICNS, ICNS_IMAGE_FILE },
                // TODO(cmchen): add ability to sniff ICOs if possible. Done
                new Object[] { ImageFormats.ICO, ICO_IMAGE_FILE },
                new Object[] { ImageFormats.TIFF, TIFF_IMAGE_FILE }, new Object[] { ImageFormats.JPEG, JPEG_IMAGE_FILE },
                new Object[] { ImageFormats.BMP, BMP_IMAGE_FILE }, new Object[] { ImageFormats.PSD, PSD_IMAGE_FILE },
                new Object[] { ImageFormats.PBM, PBM_IMAGE_FILE }, new Object[] { ImageFormats.PGM, PGM_IMAGE_FILE },
                new Object[] { ImageFormats.PPM, PPM_IMAGE_FILE },
                // TODO(cmchen): add ability to sniff TGAs if possible. Done
                new Object[] { ImageFormats.TGA, TGA_IMAGE_FILE },
                // TODO(cmchen): Add test images for these formats. Done
                //new Object[] { ImageFormats.PNM, PNM_IMAGE_FILE },
                // new Object[] { ImageFormat.IMAGE_FORMAT_JBIG2, JBIG2_IMAGE_FILE },
                new Object[] { ImageFormats.UNKNOWN, UNKNOWN_IMAGE_FILE }).stream();
    }
    @BeforeAll
    static void resetCoverage() {
        Arrays.fill(Imaging.coverage, false);
    }

    /**
     *  Added ICO coverage, as this test weren't implemented
     *  Added TGA coverage, as this test weren't implemented
     *  
     *  Added catching exception coverage, no test for the try/catch blocks were implemented.
     *  Added branch id 0 coverage. No tests tested if the function returned at the start.
     * 
     * @param expectedFormat
     * @param pathToFile
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("data")
    void testGuessFormat(final ImageFormats expectedFormat, final String pathToFile) throws Exception {
        final String imagePath = FilenameUtils.separatorsToSystem(pathToFile);
        final File imageFile = new File(ImagingTestConstants.TEST_IMAGE_FOLDER, imagePath);

        final ImageFormat guessedFormat = Imaging.guessFormat(imageFile);

        assertEquals(expectedFormat, guessedFormat);
    }
    /**
     *  Test that controls that guessFormat throws exceptions on empty files.
     * @throws Exception
     */
    @Test
    void testGuessFormatThrowsExceptionOnEmptyFile() throws Exception {
        File emptyFile = new File(ImagingTestConstants.TEST_IMAGE_FOLDER, "emptyfile.img");
            emptyFile.createNewFile(); 
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Imaging.guessFormat(emptyFile);
        });
        assertTrue(exception.getMessage().contains("Couldn't read magic numbers to guess format."));
        if (emptyFile.exists()) {
            emptyFile.delete();
        }
    }

    @Test
    void testGuessFormatCatchesExceptions() throws Exception {
        ByteSource empty = ByteSource.array(new byte[0]);

        ImageFormat format = Imaging.guessFormat(empty);

        assertEquals(ImageFormats.UNKNOWN, format);
        /*
            Throwing branch
        */
        assertTrue(Imaging.coverage[5]);  
        /*
            Catching branch
        */
        assertTrue(Imaging.coverage[4]);
    }


    @Test
    void testThatGuessFormatReturnsUnknownOnByteSourceNull() throws Exception {
        // Trigger branch: byteSource == null
        ImageFormat format = Imaging.guessFormat((ByteSource) null);

        // Assert that the returning format is UNKNOWN
        assertEquals(ImageFormats.UNKNOWN, format);

        // coverage[0] First branch is set to true
        assertTrue(Imaging.coverage[0]);
    }


    @AfterAll
    static void getCoveragePercent() {
        int covered = 0;
        for (boolean b : Imaging.coverage) {
            if (b) covered++;
        }
        System.out.println("% Covered: " + (covered * 100.0) / Imaging.coverage.length);
    }

}
