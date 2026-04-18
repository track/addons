# Security policy

## Supported versions

Only the latest minor release of the Analyse addons receives security updates. Older versions should be upgraded.

| Version | Supported |
| --- | --- |
| 1.x (latest) | Yes |
| &lt; 1.0 | No |

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

Instead, email us at **security@analyse.net** with:

- A description of the issue and where you found it (which addon, file, class, etc.).
- The impact (what can an attacker do with this?).
- Steps to reproduce, ideally including a minimal proof of concept.
- Your preferred credit name if the report leads to a fix and you want public credit.

You should receive an acknowledgement within **2 business days**. We aim to have a triaged response with either a fix, a workaround, or a timeline within **14 days**.

## Scope

Reports about the following are in scope:

- The addons in this repository (ShopGUIPlus, Votifier, PlayerPoints, and any future addons).
- Interaction between these addons and the Analyse plugin or SDK.

The Analyse plugins themselves live in a separate repository with its own [security policy](https://github.com/Analyse-net/analyse-java/security/policy). The Analyse dashboard, API, and website are covered by a separate security policy at [analyse.net/security](https://analyse.net/security).

## Out of scope

- Denial-of-service caused by a server operator running an addon with obviously invalid configuration.
- Vulnerabilities in the third-party plugins these addons integrate with (ShopGUIPlus, NuVotifier, PlayerPoints). Please report those to their respective maintainers.
- Vulnerabilities that require the attacker to already have operator access on the same Minecraft server.
- Social engineering of Analyse staff.

## Disclosure

We follow coordinated disclosure. Once a fix has shipped, and after a reasonable window for operators to update (typically 7 days for low-severity, 30 days for high-severity), we publish a GitHub Security Advisory crediting the reporter.

Thank you for helping keep Analyse users safe.
