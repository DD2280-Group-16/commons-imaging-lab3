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

### 1. What are your results for five complex functions?
- Did all methods (tools vs. manual count) get the same result?
- Are the results clear?

The complex functions used by the group to analyze were `guessFormat()`, `getGpsInfo()`, `getBufferedImage()` (in `PngImageParser.java`), `getValueDescription()`, and `readImageContents()`. The group started first by analyzing every function with the Lizard tool.

| Function | CCN (Lizard) |
|---|---|
| `guessFormat()` | 33 |
| `getGpsInfo()` | 8 |
| `getBufferedImage()` | 27 |
| `readImageContents()` | 31–33 (varies by tool) |
| `getValueDescription()` | ... |

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
