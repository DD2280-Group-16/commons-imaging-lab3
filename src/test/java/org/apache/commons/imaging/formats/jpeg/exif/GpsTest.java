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
package org.apache.commons.imaging.formats.jpeg.exif;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.internal.Debug;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GpsTest extends AbstractExifTest {

    public static Stream<File> data() throws Exception {
        return getImagesWithExifData().stream();
    }

    @ParameterizedTest
    @MethodSource("data")
    void test(final File imageFile) throws Exception {
        if (imageFile.getParentFile().getName().toLowerCase().equals("@broken")) {
            return;
        }

        final JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(imageFile);
        if (null == metadata) {
            return;
        }

        final TiffImageMetadata exifMetadata = metadata.getExif();
        if (null == exifMetadata) {
            return;
        }

        final TiffImageMetadata.GpsInfo gpsInfo = exifMetadata.getGpsInfo();
        exifMetadata.printGetGpsInfoCoverage(); // Print branch coverage
        if (null == gpsInfo) {
            return;
        }

        // TODO we should assert something here.
        Debug.debug("imageFile " + imageFile);
        Debug.debug("gpsInfo " + gpsInfo);
        Debug.debug("gpsInfo longitude as degrees east " + gpsInfo.getLongitudeAsDegreesEast());
        Debug.debug("gpsInfo latitude as degrees north " + gpsInfo.getLatitudeAsDegreesNorth());
        Debug.debug();

    }

    /**
     * @throws Exception if it cannot open the images.
     */
    @Test
    void testReadMetadata() throws Exception {
        final File imageFile = new File(GpsTest.class.getResource("/images/jpeg/exif/2024-04-30_G012.JPG").getFile());
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) Imaging.getMetadata(imageFile);
        final TiffField gpsHPosErrorField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_HOR_POSITIONING_ERROR);
        final RationalNumber gpsHPosError = (RationalNumber) gpsHPosErrorField.getValue();
        assertEquals(0.014, gpsHPosError.doubleValue());
    }


    // ===================== Added Tests to Increase Branch Coverage ======================

    @Test
    void getGpsInfoReturnsNotNull() throws Exception {

        final File src = new File(GpsTest.class.getResource("/images/jpeg/exif/2024-04-30_G012.JPG").getFile());

        // build or create an output set (copy existing EXIF if present)
        TiffOutputSet outputSet = null;
        final JpegImageMetadata jm = (JpegImageMetadata) Imaging.getMetadata(src);
        if (jm != null) {
            final TiffImageMetadata exif = jm.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }

        // create a GPS directory (convenience method that creates all required GPS tags)
        outputSet.setGpsInDegrees(-74.0, 40.7167); // longitude, latitude

        // optional: modify or remove fields directly:
        // final TiffOutputDirectory gpsDir = outputSet.getOrCreateGPSDirectory();
        // gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
        final File dst = Files.createTempFile("gps-added-", ".jpg").toFile();
        dst.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dst))) {
            new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
        }

        // read metadata back - gpsDirectory will now be present
        final JpegImageMetadata jm2 = (JpegImageMetadata) Imaging.getMetadata(dst);
        final TiffImageMetadata exif2 = jm2 == null ? null : jm2.getExif();
        assertNotNull(exif2);
        assertNotNull(exif2.findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_GPS));

        // now calling getGpsInfo() will proceed past the gpsDirectory-null check
        final TiffImageMetadata.GpsInfo info = exif2.getGpsInfo();
        exif2.printGetGpsInfoCoverage();
        assertNotNull(info);

    }





    @Test
    void getGpsInfoReturnsNullForNullLatitudeRefField() throws Exception {
        final File src = new File(GpsTest.class.getResource("/images/jpeg/exif/2024-04-30_G012.JPG").getFile());

        // prepare output set (copy existing EXIF or create new)
        TiffOutputSet outputSet = null;
        final JpegImageMetadata jm = (JpegImageMetadata) Imaging.getMetadata(src);
        if (jm != null) {
            final TiffImageMetadata exif = jm.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }

        // add a proper GPS directory first
        outputSet.setGpsInDegrees(-74.0, 40.7167);

        // remove the latitude REF tag so findField(GPS_LATITUDE_REF) returns null
        final TiffOutputDirectory gpsDir = outputSet.getOrCreateGpsDirectory();
        gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);

        // write to temp file and re-read metadata
        final File dst = Files.createTempFile("gps-null-latref-", ".jpg").toFile();
        dst.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dst))) {
            new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
        }

        final JpegImageMetadata jm2 = (JpegImageMetadata) Imaging.getMetadata(dst);
        final TiffImageMetadata exif2 = jm2 == null ? null : jm2.getExif();
        assertNotNull(exif2);

        // this should be NULL because latitudeRefField is missing -> coverage[2] branch
        assertNull(exif2.getGpsInfo());
        exif2.printGetGpsInfoCoverage();
    }






    @Test
    void getGpsInfoReturnsNullForNullLatitudeField() throws Exception {
        final File src = new File(GpsTest.class.getResource("/images/jpeg/exif/2024-04-30_G012.JPG").getFile());

        // prepare output set (copy existing EXIF or create new)
        TiffOutputSet outputSet = null;
        final JpegImageMetadata jm = (JpegImageMetadata) Imaging.getMetadata(src);
        if (jm != null) {
            final TiffImageMetadata exif = jm.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }

        // add a proper GPS directory first
        outputSet.setGpsInDegrees(-74.0, 40.7167);

        // remove the latitude REF tag so findField(GPS_LATITUDE) returns null
        final TiffOutputDirectory gpsDir = outputSet.getOrCreateGpsDirectory();
        gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_LATITUDE);

        // write to temp file and re-read metadata
        final File dst = Files.createTempFile("gps-null-lat-", ".jpg").toFile();
        dst.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dst))) {
            new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
        }

        final JpegImageMetadata jm2 = (JpegImageMetadata) Imaging.getMetadata(dst);
        final TiffImageMetadata exif2 = jm2 == null ? null : jm2.getExif();
        assertNotNull(exif2);

        // this should be NULL because latitudeField is missing -> coverage[3] branch
        assertNull(exif2.getGpsInfo());
        exif2.printGetGpsInfoCoverage();
    }





     @Test
    void getGpsInfoReturnsNullForNullLongitudeRefField() throws Exception {
        final File src = new File(GpsTest.class.getResource("/images/jpeg/exif/2024-04-30_G012.JPG").getFile()); // File to derive Metadata from

        // prepare output set (copy existing EXIF or create new), output set is write-only.
        TiffOutputSet outputSet = null;
        final JpegImageMetadata jm = (JpegImageMetadata) Imaging.getMetadata(src);
        if (jm != null) {
            final TiffImageMetadata exif = jm.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        //Create new output set
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }

        // add a proper GPS directory first
        outputSet.setGpsInDegrees(-74.0, 40.7167);

        // remove the latitude REF tag so findField(GPS_LONGITUDE) returns null
        final TiffOutputDirectory gpsDir = outputSet.getOrCreateGpsDirectory();
        gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);

        // write to temp file and re-read metadata
        final File dst = Files.createTempFile("gps-null-longref-", ".jpg").toFile();
        dst.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(dst))) {
            new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
        }

        final JpegImageMetadata jm2 = (JpegImageMetadata) Imaging.getMetadata(dst); // Parsed object of the metadata from file
        final TiffImageMetadata exif2 = jm2 == null ? null : jm2.getExif(); // Object we can get the GPS info from.
        assertNotNull(exif2);

        // this should be NULL because longitudeRefField is missing -> coverage[4] branch
        assertNull(exif2.getGpsInfo());
        exif2.printGetGpsInfoCoverage();
    }

}
