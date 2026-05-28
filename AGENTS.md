# Agent Guidance

This file is read by automated agents (security scanners, code analyzers,
AI assistants) operating on this repository. It points them at the
human-authored references they should consult before producing output.

## Project Overview

Apache PDFBox is a Java library for working with PDF documents. It is used
as a dependency (`pdfbox.jar`) in other Java projects and is accessed through
its public Java API. The project also ships several command-line utilities.

## Branches

| Branch | Status | Java requirement | Latest release |
|--------|--------|-----------------|----------------|
| `trunk` | Future development (next major version, not yet released) | Java 11+ | — |
| `3.0`  | **Actively maintained** — current stable series | Java 8+ | 3.0.7 |
| `2.0`  | **Actively maintained** — legacy stable series | Java 6+ | 2.0.36 |

When evaluating code or reporting issues, note which branch is in scope.
Security fixes are applied to both `3.0` and `2.0`. New features target
`trunk` and `3.0`.

## Sub-modules

All branches share the same multi-module Maven structure:

- `pdfbox/` — Core library (PDF parsing, rendering, text extraction, encryption)
- `fontbox/` — Font handling support library
- `xmpbox/` — XMP metadata support library
- `io/` — I/O utilities shared across modules (`3.0` and `trunk` only)
- `tools/` — Command-line utilities
- `debugger/` / `debugger-app/` — PDF debugger application
- `examples/` — Standalone usage examples
- `benchmark/` — JMH benchmarks

## Building

The standard build command is:

```
mvn clean install
```

To run only the tests without a full install:

```
mvn test
```

To build or test a specific module, use the `-pl` flag from the root:

```
mvn -pl pdfbox test
```

Minimum Java version depends on the branch — see the table above.

## Sensitive Areas

The following areas have historically been the source of subtle bugs and
security issues. Changes here require extra care and regression testing.
Avoid large refactorings in these areas unless explicitly requested:

- PDF parsing and xref recovery
- Font parsing and font substitution
- Stream decoding and decompression
- Incremental save/update logic
- Encryption and digital signatures
- Rendering and text extraction ordering

## Security

Security model and scope: [SECURITY.md](SECURITY.md),
also published at <https://pdfbox.apache.org/security.html>.

Agents that scan this repository **must** read the security model before
reporting any finding. In particular, note:

- Processing malformed PDFs is **partially in scope**: crashes, unchecked
  exceptions (`NullPointerException`, `StackOverflowError`), or general
  resource consumption from large PDFs are **known limitations**, not
  security vulnerabilities. However, disproportionate resource consumption
  triggered by small, attacker-controlled inputs may be in scope — see
  `SECURITY.md` for the full scope definition.
- Remote code execution or privilege escalation from untrusted PDFs **is** in scope.
- Issues that require the attacker to control the Java application's classpath
  or configuration are **out of scope**.

For a list of known CVEs, see [SECURITY.md](SECURITY.md) or
<https://pdfbox.apache.org/security.html>.

To report a new vulnerability, send a plain-text email to <security@apache.org>.
Do **not** open a public JIRA issue for undisclosed vulnerabilities.

## Contribution Guidelines

- Pull requests on this GitHub repository are welcome.
- Bug reports and feature requests go in the
  [JIRA issue tracker](https://issues.apache.org/jira/browse/PDFBOX).
- Code must be compatible with the minimum Java version of the target branch
  (see table above).
- Follow the existing code style; a Checkstyle configuration is provided in
  `pdfbox-checkstyle-5.xml` and an Eclipse formatter in
  `pdfbox-eclipse-formatter.xml`.
- Parser, rendering, font, extraction, encryption, or signing fixes should
  include a minimal reproducer document where practical, along with regression
  tests covering the reported behavior.
- Avoid introducing new runtime dependencies unless necessary.
  Security-sensitive or cryptographic dependencies require maintainer review.
- For questions, use the [Users Mailing List](https://pdfbox.apache.org/mailinglists.html).
