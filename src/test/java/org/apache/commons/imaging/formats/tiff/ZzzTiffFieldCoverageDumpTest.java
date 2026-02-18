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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Runs last (Zzz...) so @AfterAll dumps coverage after other tiff tests. Print shows in terminal.
 */
class ZzzTiffFieldCoverageDumpTest {

    @AfterAll
    static void dumpCoverage() {
        int covered = 0;
        for (final boolean b : TiffField.coverageGetValueDescription) {
            if (b) covered++;
        }
        System.out.println("% Covered: " + (covered * 100.0) / TiffField.coverageGetValueDescription.length);
    }

    @Test
    void placeholder() {
    }
}
