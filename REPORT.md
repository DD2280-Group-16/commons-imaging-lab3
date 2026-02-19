<!--
- Licensed to the Apache Software Foundation (ASF) under one or more
- contributor license agreements. See the NOTICE file distributed with
- this work for additional information regarding copyright ownership.
- The ASF licenses this file to You under the Apache License, Version 2.0
- (the "License"); you may not use this file except in compliance with
- the License. You may obtain a copy of the License at
-
-      https://www.apache.org/licenses/LICENSE-2.0
-
- Unless required by applicable law or agreed to in writing, software
- distributed under the License is distributed on an "AS IS" BASIS,
- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
- See the License for the specific language governing permissions and
- limitations under the License.
-->

# Report for assignment 3

## Project

Name: Apache Commons Imaging 
URL: https://github.com/apache/commons-imaging

A pure Java library that reads and writes many image formats and metadata. 

## Onboarding experience

We initially opted for Karate as our project, but we switched to Apache Commons Imaging because the codebase is larger and already has many tests, which made it easier for us to work on complexity, coverage, and refactoring tasks.

Building the project was straightforward. We did not have to install many extra tools: with the Maven wrapper we only had to use the provided Maven (or adjust the version if needed), and that tooling is well documented. All other components were installed automatically by the build script, and the build finished without errors. Examples and tests run successfully on our systems. We plan to continue with this project.

## Complexity

### 1. What are your results for five complex functions?

- Did all methods (tools vs. manual count) get the same result?
- Are the results clear?

The complex functions used by the group to analyze were `guessFormat()`, `getGpsInfo()`, `getBufferedImage()` (in `PngImageParser.java`), `getValueDescription()`, and `readImageContents()`. The group started first by analyzing every function with the Lizard tool.

| Function                | CCN (Lizard)           |
| ----------------------- | ---------------------- |
| `guessFormat()`         | 33                     |
| `getGpsInfo()`          | 8                      |
| `getBufferedImage()`    | 27                     |
| `readImageContents()`   | 31–33 (varies by tool) |
| `getValueDescription()` | 37                     |

What was most interesting was when it came to the function `readImageContents()` where depending on the tool used, in this case Lizard and JaCoCo, the answer was a little different. In this example, Lizard outputed a CCN of 32, whereas JaCoCo got 31. This could most likely be due to how the tools handle the default case or ternary operations.

### 2. Are the functions just complex, or also long?

In our case, we see how the complexity of the functions chosen tend to increase in complexity when the NLOC increases. Most likely due to them be more complicated and making more decisions. For instance, `getGpsInfo()` had an NLOC of 27 CCN of around 8, while `guessFormat()` had an NLOC of around 110.

### 3. What is the purpose of the functions?

**`guessFormat()`:** The function identifies the image's format without having to rely on a file extension and instead, analyzing the first two bytes from the data, thereafter, comparing them to already known byte signatures.

**`getGpsInfo()`:** This function extracts the GPS coordinates from metadata. Metadata is information about the image. It does this by locating the GPS sub-directory and searching for the latitude/longitude reference fields and returning a `GpsInfo` object.

**`getBufferedImage()`:** Although there is no documentation regarding this function, this function decodes a PNG file into a `BufferedImage` object by decoding the pixels and important PNG chunks.

**`getValueDescription()`:** This is a helper function that formats an integer into a readable string by showing both its decimal and hexadecimal values.

**`readImageContents()`:** This is a helper function parses the raw binary structure of an image file into an object called `BmpImageContents`. This object is then used by other methods so it does not have to parse the binary structure if the image file itself.

### 4. Are exceptions taken into account in the given measurements?

In most cases, by counting exceptions as a terminating node in the flow graph rather than a separate node gave us the same results as for instance the Lizard tool which could most likely imply that Lizard does the same thing. Otherwise, we would be getting a lower complexity due to the increase in nodes.

### 5. Is the documentation clear w.r.t. all the possible outcomes?

Documentation is provided only for the functions `guessFormat()` and `getGpsInfo()`, but even in these examples, the documentation does not cover all of the possible branches, however, summarizes the possible returns rather than diving into detail in all the possible outcomes.

## Refactoring

Plan for refactoring complex code:

- Markus
  The high complexity are necessary to an extent since the function checks magic numbers to determine the file format of the input.
  The function could be divided into smaller functions so that the different "magic number" checks separately. Like first f1 would read the header of the input. F2 would then check the first 2 bytes, and f3 would check the more complex formats like JBIG2. This way the function would work the same way but would be more readable and less complex.

- Elin
  A lot of the CC comes from if-statements that does sanitization checks. This is needed because the function is prone to user errors, such as wrong images.
  To refactor, I would move the sanity checks to a different function, which are called first, and then, after the PNG has been confirmed to be not corrupt, it would continue to read the rest of the chunks.

- Ben
  The high complexity is somewhat needed since the function base the returns on what certain values in the fields are. After some small refactoring with moving out if-statements, the CCN is lower, but it is just if statements and other checks moved into separate functions. This however, makes understanding the flow of the function much more efficient and easier to read which in turn kind of makes the complexity of the function smaller.

- Ali
  The complexity comes from many instance checks and repeated logic for each array type. This is due to the strong typing and primitive types of Java where we have the same algorithm multiple times but the only thing that changes is the type of number we use.
  To fix this we can simplify by grouping cases: one path for Number (return toString()), one for “array of numbers” (short[], int[], long[], double[], byte[], float[], and char[]), and one for Object[]. For all array types the algorithm is the same, so a single helper that takes “how to format the element at index i” is enough. We keep separate handling only for null, String, Date, and unknown type.

- Oskar
  The original method had a Cyclomatic Complexity (CCN) of 32 because it violated the Single Responsibility Principle. It is responsible for palette length calculations, raw byte reading, offset validation, and resolving the correct pixel parser via multiple switch and if statements. To fix this, we can apply the Extract Method refactoring pattern by decomposing the monolithic function into private helper methods.

## Coverage

### Tools
We used two new/different tools, OpenClover and JoCoCO.
#### OpenClover
OpenClover were very easy to use with the Maven build environment. Using the commmand "./mvnw clean clover:setup test clover:aggregate clover:clover" OpenClover built the project and ran all the tests and then reported the results into a database accessable via .html files. The .html files enabled method searching and were overall very easy to navigate in.

#### JoCoCo
Similarly to OpenClover, JoCoCo was very easy to use with Maven since it is part of it. Using the command "mvn clean test jacoco:report" jacoco runs all the tests and then makes the report available in a .html file. The .html file made navigating easy.

### Your own coverage tool
We all made our own implementations of a coverage tool but they all are very similar and works almost the same.
- Markus
    - link: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/markus/diy-tool/src/main/java/org/apache/commons/imaging/Imaging.java
    - The "tool" is a simple array that keeps track of every reached branch so it supports all constructs and is very accurate since every branch check has been implemented manually.
- Elin
    - link: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/elin/implement-coverage-tool/src/main/java/org/apache/commons/imaging/formats/png/PngImageParser.java
    - The "tool" is a simple array that keeps track of every reached branch so it supports all constructs and is very accurate since every branch check has been implemented manually.
- Ben
    - link: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/ben/diy-tool/src/main/java/org/apache/commons/imaging/formats/tiff/TiffImageMetadata.java
    - The "tool" is a simple array that keeps track of every reached branch so it supports all constructs and is very accurate since every branch check has been implemented manually.
- Ali
    - link: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/ali/task-1-diy-branch-coverage/src/main/java/org/apache/commons/imaging/formats/tiff/TiffField.java
    - The "tool" is a simple array that keeps track of every reached branch so it supports all constructs and is very accurate since every branch check has been implemented manually.
- Oskar
    - link: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/oskar/diy-branch-cover/src/main/java/org/apache/commons/imaging/formats/bmp/CoverageTracker.java
    - The tool injects tracking markers tracking true or false depending on if a branch has been reached. Since it only tracks true or false it works on all constructs and is very accurate.

### Evaluation
- Markus
    - Detailed: Very detailed, counts every possible branch and returns the coverage procentage.
    - Limitations: Since all coverage has been implemented manually, if there are any changes to the code or any more branches added, those have to be manually covered.
    - Results: The tool are more sensitive than the existing tool resulting in a lower coverage procent.
- Elin
    - Detailed: Very detailed, counts every possible branch and returns the covered branches.
    - Limitations: Since all coverage has been implemented manually, if there are any changes to the code or any more branches added, those have to be manually covered.
    - Results: The tool are more sensitive than the existing tool resulting in a lower coverage procent.
- Ben
    - Detailed: Very detailed, counts every possible branch and returns the coverage procentage.
    - Limitations: Since all coverage has been implemented manually, if there are any changes to the code or any more branches added, those have to be manually covered.
    - Results: The tool are more sensitive than the existing tool resulting in a lower coverage procent.
- Ali
    - Detailed: One branch id (0–28) per outcome in getValueDescription(Object): null, Number, String, Date, each array type, truncation (i > 50, i > 0), and the Unknown type case.
    - Limitations: Only this method is instrumented, no line coverage.
    - Results: The results are not the same as JaCoCo because JaCoCo measures the whole project and counts branches differently, my tool only counts these 29 branches. Both show an improvement after adding tests.
- Oskar
    - Detailed: Very detailed, counts every possible branch and returns the coverage procentage.
    - Limitations: Since all coverage has been implemented manually, if there are any changes to the code or any more branches added, those have to be manually covered.
    - Results: The tool is much more sensitive than the existing tool resulting in a lower coverage procent.

## Coverage improvement
- Markus
    - Comments: "TODO(cmchen): add ability to sniff ICOs if possible", "TODO(cmchen): add ability to sniff TGAs if possible", "catching exception coverage", "branch id 0 coverage".
    - Old coverage: 74.7% with OpenClover and 54.4% with DIY tool.
    - New coverage: 78.3% OpenClover and 60.3% DIY tool
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/refactor-code-branch-markus/src/test/java/org/apache/commons/imaging/ImagingGuessFormatTest.java
    - Added 4 tests
- Elin
    - Comments: "PNG has to have chunks in order to be read", "PNGs are not allowed to have more than one header"
    - Old coverage: 66% with JoCoCO and 50% with DIY tool
    - New coverage: 70% with JaCoCo and 57% with DIY tool.
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/elin/implement-tests/src/test/java/org/apache/commons/imaging/formats/png/PngReadTest.java
    - Added 2 tests
- Ben
    - Comments: "not returning null", "returning null for invalid longitude/latitude gps fields"
    - Old coverage: 7% with JoCoCo and 6.66% with DIY tool.
    - New coverage: 78% with JoCoCo and 80% with DIY tool.
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/ben/create-tests-for-function-getGpsInfo/src/test/java/org/apache/commons/imaging/formats/jpeg/exif/GpsTest.java
    - Added 4 tests
- Ali
    - Comments: "Branch coverage for getValueDescription(Object): null (id 0), double[] truncation (16–18), float[] truncation (25–27), unknown type (28)."
    - Old coverage: "Cover procent of regular tool and DIY tool before implementation of tests,
    JaCoCo: 69% | DIY tool:  59.45%"
    - New coverage: cover procent of regular tool and DIY tool after implementation of tests,
    JaCoCo: 84% | DIY tool:  75.67%
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/ali/task-2-implement-tests/src/test/java/org/apache/commons/imaging/formats/tiff/TiffFieldGetValueDescriptionTest.java
    - Added 4 tests
- Oskar
    - Comments: "The parser must reject BMP files that specify an unknown or unsupported compression method", "The parser must reject BMP files with a header size smaller than the standard 40 bytes", "The parser must reject BMP files where the image data offset is smaller than the expected header and palette size", "The parser must reject BMP files where the calculated extra bytes padding exceeds the total file size".
    - Old coverage: 70% using JaCoCo and 35.42% using DIY tool.
    - New Coverage: 72% JoCoCo and 72.92% with DIY tool.
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/oskar/coverage-improvements/src/test/java/org/apache/commons/imaging/formats/bmp/BmpMoreCoverageTest.java
    - Added 4 tests

## Self-assessment: Way of working

See ESSENCE.md.

## Overall experience

This assignment was defined by not having that much time as compared to other assignments. This, unfortunelty, made the assignment a little less enjoyable but we still feel like we got great insight into a big open source project. We learned how complex a big project can be. 

## Statement of Contributions

Oskar

- `readImageContents` from `BmpImageParser.java`
- Up to P+

Elin

- `getBufferedImage` from `PngImageParser.java`
- Up to P+

Ben

- `getGpsInfo` from `TiffImageMetadata.java`
- Up to P+

Ali

- `getValueDescription` from `TiffFiel.java`
- Up to P

Markus

- `guessFormat` from `Imaging.java`
- Up to P+




