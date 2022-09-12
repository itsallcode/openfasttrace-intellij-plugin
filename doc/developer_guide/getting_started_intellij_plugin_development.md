# Getting Started with IntelliJ Plugin Development

It's always the first time doing something new and writing IntelliJ plugins is no difference.

## Preconditions

You will need a couple of things before you can start developing an IntelliJ plugin:

* IntelliJ IDEA community edition, preferably build from source, so that you can debug into IDEA
* DevKit Plug-in (preinstalled)
* IntelliJ SDK

### Building IntelliJ IDEA From the Sources

While it sounds tempting to start with the downloaded binaries from IntelliJ IDEA, you are missing out on debug support
if you don't build from the sources.

You can find [build instructions for IntelliJ](https://github.com/JetBrains/intellij-community/tree/master) on GitHub.

#### Master or Release?

This is a recurring question when building someone else's sources. The answer mainly depends on how stable the master
branch is and how adventurous you are. In case of doubt, stick with the latest release.

#### Cloning Master Branch

To check out the master branch of IntelliJ IDEA and additional required Android modules issue the following commands

```shell
cd ~/git
git clone --depth=1 https://github.com/JetBrains/intellij-community.git
cd intellij-community/
./getPlugins.sh
```

#### Building IntelliJ With IntelliJ

At this point the build becomes a little self-referencing since you need a current IntelliJ to build IntelliJ accoring
to the official build instructions.

First indexing will take quite a while. Be patient.

Select `Build` &rarr; `Build Project` from the menu to start the build. IntelliJ will tell you that the JDK 17 is
missing unless you already installed it. Fortunately it also suggests to fix that automatically by downloading the JDK.

#### Running IntelliJ From the Sources

Select `Run` &rarr; `Run...` from the menu and pick the `IDEA` entry.

### Skipping Import From Another IDEA Installation

When you first run the freshly built IDEA, it will ask you whether you want to import settings from another IDEA installation.

I would advise against it. When developing a plugin, you want your IDE to be as clean and bare-bone as it gets.

#### Creating the Plugin Skeleton Project

You new IDEA will greet you with a couple of options to start with. One of them is creating a new Project. Select that option and pick "IDE Plugin".

You can create the skeleton project inside an existing Git repository directory if you want. This is especially useful if you want to start from a checked out (but basically empty, except for README and license) GitHub repo.

IDEA will warn you that the directory is not empty, but as long as there are no conflicts, it will work just fine.