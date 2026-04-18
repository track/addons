# Contributing to Analyse Addons

Thanks for taking the time to help improve the Analyse addons. This page is the short version of how we work.

## Before you open a PR

> [!IMPORTANT]
> By submitting any contribution (pull request, patch, issue reproduction, or snippet) to this repository, you assign the intellectual property rights of that contribution to **VertCode Development E.E.** under the terms of [Section 3 of the LICENSE](LICENSE). Please make sure you own what you submit and are OK with that assignment before opening a PR.

Also:

- Check the [issue tracker](https://github.com/track/addons/issues) first. If an issue doesn't exist for what you want to change, open one. Large unsolicited PRs (including whole new addons) are unlikely to be merged without prior discussion.
- Keep PRs focused. One logical change per PR. Refactors and feature work in separate PRs please.
- Follow the code style below.

## Building locally

Requirements:

- JDK 21 or newer
- Git

```bash
git clone https://github.com/track/addons
cd addons
./gradlew build
```

Jar files land in each `modules/*/build/libs/` directory, named `analyse-addon-<name>-<version>.jar`.

### Resolving `net.analyse:analyse-api` locally

The addons depend on the `analyse-api` artifact at the version pinned in `gradle.properties`. Published releases are served from `https://repo.analyse.net/releases`. If you're working against an unreleased API version (for example, a feature branch in [`analyse-java`](https://github.com/track/analyse-java)), publish it to your local Maven repo first:

```bash
cd ../analyse-java
./gradlew :modules:api:publishToMavenLocal
```

The root `build.gradle` lists `mavenLocal()` first, so the local copy takes priority over the public repo.

To build everything and bundle the jars into a release zip:

```bash
./scripts/release.sh
```

## Running against your own server

The fastest inner loop is:

1. Run `./gradlew :modules:shopguiplus:build` (or whichever addon you're working on).
2. Copy the jar from `modules/<addon>/build/libs/analyse-addon-<addon>-<version>.jar` into your test server's `plugins/Analyse/addons/` folder.
3. Run `/analyse reload`, or restart the server.

## Adding a new addon

A new addon is a new module under `modules/`:

```
modules/<addon-name>/
├── build.gradle
└── src/main/
    ├── java/net/analyse/addon/<addon-name>/...
    └── resources/config.yml
```

The root `settings.gradle` auto-discovers modules, so you don't need to register the new folder anywhere. Copy an existing module (for example `shopguiplus`) and adjust package names, the `@AddonInfo` annotation, the `archiveBaseName`, and the listener.

The [README](README.md#creating-a-new-addon) has the full walkthrough.

## Code style

The full style guide lives in [`.cursor/rules/java-style.mdc`](.cursor/rules/java-style.mdc). The highlights:

- **Always use braces** for `if` statements, even single-line ones.
- **Add an empty line** after an `if` block before the next statement.
- **Do NOT** add an empty line between a variable declaration and its related `if` check.
- **Extract method calls into variables** before passing them into other calls.
- **Javadoc** on every method (public, private, or protected) except `@EventHandler` listeners.
- **Use `String.format`** for logger messages with variables, not string concatenation.
- **No comment banners** (no `// ==== SECTION ====`).
- **Lombok:** `@Getter` is fine; avoid class-level `@Setter`, write setters by hand when you need them.

Formatting is two spaces for indentation, UTF-8, LF line endings.

## Commit messages

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description
```

Types we use: `feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`.

Good examples:

```
feat(shopguiplus): track NOT_ENOUGH_MONEY result type behind a config flag
fix(votifier): resolve offline player UUID before dispatching the event
refactor(playerpoints): extract balance snapshot helper from the listener
docs(README): update install path to plugins/Analyse/addons
chore(deps): bump analyse-api to 1.1.0
```

Write descriptions in the imperative, no trailing period, start with lowercase. More examples and bad examples live in [`.cursor/rules/commit-101.mdc`](.cursor/rules/commit-101.mdc).

## PR checklist

Before you click "ready for review":

- [ ] The code compiles (`./gradlew build`).
- [ ] The code follows the style guide.
- [ ] You updated the README if your change affects install, config, or tracked events.
- [ ] You added an entry to `CHANGELOG.md` under **Unreleased**.
- [ ] Your commits follow Conventional Commits.
- [ ] The PR description links the issue it closes (`Closes #123`).

## Reporting bugs

Open a [bug report](https://github.com/track/addons/issues/new?template=bug_report.md). Include:

- Which addon is affected.
- The addon version and the Analyse plugin version.
- The third-party plugin version (for example, ShopGUIPlus 3.2.0).
- Server software and version (`/version` output is perfect).
- What you expected vs. what happened.
- A minimal reproduction.
- Relevant log lines (wrap them in a fenced code block, please don't attach a 50 MB log).

## Reporting security issues

Please do NOT open a public issue for security vulnerabilities. See [SECURITY.md](SECURITY.md) for the right channel.

## Questions

For usage questions, use [GitHub Discussions](https://github.com/track/addons/discussions) or the [Analyse Discord](https://analyse.net). Issues are for bugs and feature requests only.

Thanks again.
