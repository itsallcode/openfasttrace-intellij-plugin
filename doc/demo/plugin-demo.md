# OpenFastTrace IntelliJ Plugin Demo

This script is for a short live demonstration of the plugin using the isolated example project in `doc/demo/example`.

## Preparation

1. Install the IntelliJ Presenter Plugin.
2. Install the OpenFastTrace IntelliJ plugin from the latest GitHub release as described below, or launch a sandbox IDE with `./gradlew manualTestIde`.
3. Open `doc/demo/example` as the IntelliJ project for the demo.
4. Open `Settings | Tools | OpenFastTrace`, select `Trace selected resources`, clear `Include IntelliJ source directories` and `Include IntelliJ test directories`, and set `Additional project-relative files or directories (one per line)` to:

   ```text
   doc/demo/example
   ```

   Apply the settings. The validation area should show no errors.
5. Open `excuse-of-the-day.md` and `excuse-of-the-day.sh`.
6. Run `Tools | OpenFastTrace | Trace Project` once. The starting demo project should trace green.

## Installing the Plugin from GitHub

The plugin is not available on the JetBrains Marketplace yet. Download it from the repository's GitHub releases page:

<https://github.com/itsallcode/openfasttrace-intellij-plugin/releases/latest>

Open the latest release and download the plugin distribution ZIP from the release assets. Use the plugin ZIP itself, for example `OpenFastTrace-<version>.zip`, not the GitHub-generated source-code ZIP.

Install it in the JetBrains IDE with `Settings | Plugins | gear icon | Install Plugin from Disk...`, select the downloaded ZIP file, confirm the installation, and restart the IDE when prompted.

For a local development build or rehearsal without installing a release artifact, launch a sandbox IDE with:

```sh
./gradlew manualTestIde
```

## Execution

> Today we are going to make one tiny change to a tiny project and let the IDE keep track of the requirement trace chain while we work.
> We use OpenFastTrace and the IntelliJ plugin to make our life easier.
> The example is deliberately small. But the concepts of working with requirement tracing translates nicely to larger projects.

### 1. Show Trace Scope Configuration

Open `Settings | Tools | OpenFastTrace` and show the selected-resource configuration from the preparation: `Trace selected resources`, both IntelliJ directory checkboxes cleared, and the additional paths set to `doc/demo/example`.

> The demonstration has a narrow scope. A Markdown specification and the shell implementation, both found under a `doc/demo/example` in this project.
> Every trace run now checks only the files we want in this example.

### 2. Show Syntax Highlighting

Open `excuse-of-the-day.md` and point out the highlighted OFT item IDs, `Needs:`, and `Covers:` entries.

> Let's first take look at a specification file.
> This is still plain Markdown. The IDE just stops treating the trace parts like wallpaper, which is helpful to identify OFT specification items in text.

### 3. Find the Existing Feature

Use `Go to Symbol` or the Symbols tab in `Search Everywhere` to search for `feat~excuse-of-the-day~1`, then open the result in the `Features` chapter.

> OFT IDs are project symbols now. No grep, no scrolling, no guessing which requirement document has the thing we need.

### 4. Add a Feature with a Live Template

Under `Features`, expand the `feat` live template and add `Homework Excuse` with the ID `feat~homework-excuse~1`, a short description, and `Needs: req`.

> The template does not write the requirement for us. It just saves us from remembering the OFT syntax.

### 5. Run the First Red Trace

Run `Tools | OpenFastTrace | Trace Project`. The trace should be red because `feat~homework-excuse~1` declares `Needs: req`, but no requirement covers it yet. Activate `feat~homework-excuse~1` in the trace output to jump back to the feature item.

> The moment we describe a new feature, OFT asks for the next link in the chain.
> Red here is early feedback, before we have written much.

### 6. Add Requirement Coverage with Completion

Under `User Requirements`, add `req~homework-excuse~1` with a short description, `Needs: scn`, and a `Covers:` entry. In the `Covers:` entry, type `feat~home` and invoke completion to select `feat~homework-excuse~1`.

> Completion uses the specification items already declared in the project. That is better than pasting IDs from memory and spending hours chasing typos.

### 7. Navigate from the Reference

Invoke `Go To Declaration` on the `feat~homework-excuse~1` `Covers:` entry, then navigate back to the requirement.

> A review often starts at a link. The IDE should answer "where does this point?" for us while we focus on the content of the specification.

### 8. Add Scenario and Design Items

Under `Scenarios`, expand the `scn` live template and add `scn~homework-excuse~1` with a short given-when-then story, a `Covers:` entry for `req~homework-excuse~1`, and `Needs: dsn`. Under `Design`, add `dsn~homework-excuse~1` with a short implementation idea, a `Covers:` entry for the scenario, and `Needs: impl`.

> Now the chain says: product idea, user rule, example, technical intent. It is small enough to read and strict enough to catch missing work.

### 9. Run the Implementation Red Trace

Run `Tools | OpenFastTrace | Trace Project`. The trace should be red because `dsn~homework-excuse~1` still lacks implementation coverage. Activate `dsn~homework-excuse~1` in the trace output to jump back to the design item.

> This is the useful kind of red. It says "you promised implementation coverage, and I cannot find it" and then gives us a link back to the broken promise.

### 10. Add the Coverage Tag

In `excuse-of-the-day.sh`, add the coverage tag `# [impl->dsn~homework-excuse~1]`. Type the target side partially and invoke completion after `impl->` to select `dsn~homework-excuse~1`, then point out the highlighted tag.

> The script stays ordinary shell. The OFT tag is a comment for Bash and evidence for the trace.

### 11. Run the Green Trace and Open the Guide

Run `Tools | OpenFastTrace | Trace Project` again. The trace should be green. Open `Help | OpenFastTrace User Guide` to show the built-in link to the user guide.

> Green means this tiny chain is connected end to end. And when someone forgets the exact OFT syntax, the guide is one Help-menu item away.

> In a few minutes we used the normal IDE moves developers and quality engineers already know: search, completion, navigation, templates, and trace runs to catch missing links.

## Reset After Rehearsal

Discard local edits in `doc/demo/example` before the next run of the demo.
