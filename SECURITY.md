<!---
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--->

# Security Scan Guidance

This document is for AI security scanners and vulnerability researchers to
understand the security model of Apache PDFBox and to avoid false positives.

The canonical, human-authored version of this information is published at
<https://pdfbox.apache.org/security.html>.

## Intended Use

Apache PDFBox is a Java library (`pdfbox.jar`) for creating, manipulating,
and extracting content from PDF documents. It is accessed through its public
Java API by other Java applications. It also ships command-line utilities
built on top of that API.

## Security Model

### Processing untrusted PDFs

PDFBox supports processing of untrusted (potentially malicious or malformed)
PDF files **to a limited degree**:

- **In scope**: Remote code execution, privilege escalation, unauthorized data
  access or escape from an embedding application's sandbox or security boundary,
  caused by processing an untrusted PDF document. These are genuine vulnerabilities
  and should be reported privately.

- **Known limitations (not vulnerabilities)**: Malformed PDFs may cause
  unchecked exceptions such as `NullPointerException` or `StackOverflowError`,
  or may consume significant memory or CPU relative to document size. General
  parser and resource exhaustion issues are treated as robustness limitations
  rather than security vulnerabilities. Pure resource exhaustion findings
  without privilege escalation or unauthorized access are generally out of scope
  (e.g. tenant starvation, indexing-service disruption from a tiny crafted input).

### Deployment and sandboxing

Applications embedding PDFBox are expected to apply appropriate operational
controls — such as timeouts, memory limits, process isolation, and sandboxing
— when processing untrusted documents at scale or in multi-tenant environments.
Resource exhaustion risks in those contexts are the responsibility of the
embedding application, not of PDFBox itself.

### Encryption and cryptography

PDFBox uses the Java Cryptography Architecture (JCA) and the Bouncy Castle
libraries to implement PDF encryption (RC4, AES-128, AES-256) and digital
signatures. Vulnerabilities in these dependencies should be reported to their
respective projects; PDFBox will incorporate fixes in its releases as needed.
Issues in how PDFBox *uses* those libraries (e.g. incorrect key derivation,
bypass of access permissions) are in scope.

### Classpath and configuration trust boundary

PDFBox is a library. The embedding application is assumed to control its own
JVM environment, including classpath, JVM arguments, installed security
providers, classloaders, and dependency resolution. Attacks that require the
attacker to influence any of these are **out of scope**.

### Command-line utilities

The command-line tools (`PDFToText`, `PDFDebugger`, etc.) are convenience
wrappers around the library API. Their security scope follows the library
scope above.

## Previously Disclosed Vulnerabilities

For a full list of disclosed CVEs, see <https://pdfbox.apache.org/security.html>.

Scanners should check there before reporting a finding to avoid duplicate reports.

## Reporting a Vulnerability

**Do not open a public JIRA issue for an undisclosed vulnerability.**

Report undisclosed vulnerabilities by sending a plain-text email to:

```
security@apache.org
```

Send one email per vulnerability. The PDFBox security team will work with
you privately to confirm and resolve the issue before public disclosure.

The typical handling process is:

1. Reporter sends details to <security@apache.org>.
2. The PDFBox security team acknowledges receipt and works privately with the
   reporter to validate and fix the issue.
3. A new release is prepared that includes the fix.
4. The vulnerability and its fix are publicly announced on the blog and in the CVE database.

For more detail on the Apache vulnerability handling process, see
<https://www.apache.org/security/committers.html>.
