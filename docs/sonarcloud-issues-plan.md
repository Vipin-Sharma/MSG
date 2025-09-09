# SonarCloud Cleanup Tasks

## Security Hotspots
- Review and optimize regex patterns in:
  - `ParameterMetadataExtractor.java` line ~87
  - `DeleteMicroserviceGenerator.java` line ~111

## Code Smells
- Replace usages of `@Deprecated` methods marked for removal.
- Consolidate runtime exception tests to a single invocation.
- Extract duplicate string literals into constants.
- Remove superfluous exceptions in `throws` clauses.
- Ensure utility classes have private constructors (e.g., `TestUtils`).
- Group similar tests using parameterized tests.
- Chain consecutive AssertJ `assertThat` statements.
- Remove unused local variables and assignments.
- Drop unused method parameters or annotate appropriately.

