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

import java.util.HashSet;
import java.util.Set;

public class CoverageTracker {
  public static final Set<Integer> branchesHit = new HashSet<>();
  public static final int TOTAL_BRANCHES = 48;

  public static void markBranch(int branchId) {
    branchesHit.add(branchId);
  }

  public static void printCoverage() {
    double coverage = (double) branchesHit.size() / TOTAL_BRANCHES * 100;
    System.out.println("--------------------------------------------------");
    System.out.println("CUSTOM COVERAGE TOOL REPORT:");
    System.out.println("Branches Hit: " + branchesHit.size() + " out of " + TOTAL_BRANCHES);
    System.out.println("Branch Coverage: " + String.format("%.2f", coverage) + "%");
    System.out.println("--------------------------------------------------");
  }

  public static void reset() {
    branchesHit.clear();
  }
}
