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

public class BmpErrorCoverageTest {

  @Test
  public void testUnknownCompressionException() throws Exception {
    CoverageTracker.reset();
    // Read a valid BMP image into a byte array
    File file = new File("src/test/resources/data/images/bmp/4/rle4.bmp");
    byte[] bytes = Files.readAllBytes(file.toPath());
    bytes[30] = 99; // Intentionally corrupt the Compression field.

    // Pass the corrupted bytes to the parser and assert that it throws our expected
    // Exception
    ImagingException ex = assertThrows(ImagingException.class, () -> {
      Imaging.getBufferedImage(bytes);
    });

    // Verify
    assertTrue(ex.getMessage().contains("Unknown Compression"));
    CoverageTracker.printCoverage();
  }
}
