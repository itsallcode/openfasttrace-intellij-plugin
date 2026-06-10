# JetBrains Marketplace Release Checklist

The packaged plugin descriptor owns metadata that JetBrains can read from `META-INF/plugin.xml`, including the plugin name, version, vendor, compatibility baseline, website URL, description, and change notes.

When publishing or updating the JetBrains Marketplace listing, keep these Marketplace admin fields aligned with the repository:

* Website URL: `https://github.com/itsallcode/openfasttrace-intellij-plugin`
* Source code URL: `https://github.com/itsallcode/openfasttrace-intellij-plugin`
* Issue tracker URL: `https://github.com/itsallcode/openfasttrace-intellij-plugin/issues`
* Vendor: `itsallcode.org`

The Marketplace description is maintained in [description.html](description.html). The Marketplace change notes are rendered from the active Markdown release notes under [../changes](../changes/changelog.md) with Pandoc during the Gradle descriptor patching step. Keep Pandoc available on `PATH` in local release environments and CI packaging jobs, or set `PANDOC` to the executable path.
