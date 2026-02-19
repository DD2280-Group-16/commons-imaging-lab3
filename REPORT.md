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

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name:
URL:
One or two sentences describing it

## Onboarding experience

We initially opted for Karate as our project, but we switched to Apache Commons Imaging because the codebase is larger and already has many tests, which made it easier for us to work on complexity, coverage, and refactoring tasks.

Building the project was straightforward. We did not have to install many extra tools: with the Maven wrapper we only had to use the provided Maven (or adjust the version if needed), and that tooling is well documented. All other components were installed automatically by the build script, and the build finished without errors. Examples and tests run successfully on our systems. We plan to continue with this project.

## Complexity

1. What are your results for five complex functions?

- Did all methods (tools vs. manual count) get the same result?
- Are the results clear?

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

Document your experience in using a "new"/different coverage tool.
How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.
The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:
git diff ...
What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

1. How detailed is your coverage measurement?
2. What are the limitations of your own tool?
3. Are the results of your tool consistent with existing coverage tools?

## Coverage improvement

Show the comments that describe the requirements for the coverage.
Report of old coverage: [link]
Report of new coverage: [link]
Test cases added:
git diff ...
Number of test cases added: two per team member (P) or at least four (P+).

## Self-assessment: Way of working

Current state according to the Essence standard: ...
Was the self-assessment unanimous? Any doubts about certain items?
How have you improved so far?
Where is potential for improvement?

## Overall experience

What are your main take-aways from this project? What did you learn?
Is there something special you want to mention here?

