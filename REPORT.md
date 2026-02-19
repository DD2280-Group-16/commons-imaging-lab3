# Report for assignment 3
This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.
## Project
Name:
URL:
One or two sentences describing it
## Onboarding experience
Did it build and run as documented?
See the assignment for details; if everything works out of the box,
there is no need to write much here. If the first project(s) you picked
ended up being unsuitable, you can describe the "onboarding experience"
for each project, along with reason(s) why you changed to a different one.
## Complexity
1. What are your results for five complex functions?
* Did all methods (tools vs. manual count) get the same result?
* Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?
## Refactoring
Plan for refactoring complex code:
Estimated impact of refactoring (lower CC, but other drawbacks?).
Carried out refactoring (optional, P+):
git diff ...
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
    - Old coverage: 76.8% with OpenClover and 54.4% with DIY tool.
    - New coverage: 82.3% OpenClover and 61.9% DIY tool
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/refactor-code-branch-markus/src/test/java/org/apache/commons/imaging/ImagingGuessFormatTest.java
    - Added 4 tests
- Elin
    - Comments: "PNG has to have chunks in order to be read", "PNGs are not allowed to have more than one header"
    - Old coverage: 66% with JoCoCO and 0% with DIY tool
    - New coverage: 70% with JaCoCo and 14% with DIY tool.
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
    JaCoCo: 69% | DIY tool: 58.62068965517241%"
    - New coverage: cover procent of regular tool and DIY tool after implementation of tests,
    JaCoCo: 84% | DIY tool: 79.3103448275862%
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/ali/task-2-implement-tests/src/test/java/org/apache/commons/imaging/formats/tiff/TiffFieldGetValueDescriptionTest.java
    - Added 4 tests
- Oskar
    - Comments: "The parser must reject BMP files that specify an unknown or unsupported compression method", "The parser must reject BMP files with a header size smaller than the standard 40 bytes", "The parser must reject BMP files where the image data offset is smaller than the expected header and palette size", "The parser must reject BMP files where the calculated extra bytes padding exceeds the total file size".
    - Old coverage: 70% using JaCoCo and 35.42% using DIY tool.
    - New Coverage: 72% JoCoCo and 72.92% with DIY tool.
    - Test cases added: https://github.com/DD2280-Group-16/commons-imaging-lab3/blob/oskar/coverage-improvements/src/test/java/org/apache/commons/imaging/formats/bmp/BmpMoreCoverageTest.java
    - Added 4 tests

## Self-assessment: Way of working
Current state according to the Essence standard: ...
Was the self-assessment unanimous? Any doubts about certain items?
How have you improved so far?
Where is potential for improvement?
## Overall experience
What are your main take-aways from this project? What did you learn?
Is there something special you want to mention here?
