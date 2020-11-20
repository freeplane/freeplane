Freeplane
=========

[![Build Status](https://api.travis-ci.org/freeplane/freeplane.svg?branch=1.6.x)](https://travis-ci.org/freeplane/freeplane)

## Changes
### New artwork:
#### New splash screen:
<img src="https://user-images.githubusercontent.com/3975572/99742908-7f13ba80-2aa2-11eb-8201-995dde96c551.png" width="670"></img> 

#### New (macOS Big Sur-style) app icon:

<img src="https://user-images.githubusercontent.com/3975572/99742937-93f04e00-2aa2-11eb-8d64-e9a419b722f8.png" width="15%"></img> 

#### Old splash screen:

For comparison, below is the old splash screen. Though cute, it is (to my eye, anyway) less than aesthetically pleasing and looks out of place on my Mac.

<img width="670" alt="Old Splash Screen" src="https://user-images.githubusercontent.com/3975572/99743482-c3538a80-2aa3-11eb-85ce-c125b0232edc.png">

### Filterable icon selector:

Type a few letters to filter the list of icons (useful when you have a lot of icons). You can also use the arrow keys to navigate / select an icon, and then press Enter to apply the selected icon.

<img width="226" alt="Filterable icon selector" src="https://user-images.githubusercontent.com/3975572/99743781-61dfeb80-2aa4-11eb-9c69-d089784fb8b9.png">


### Removed icons from menu items
The icons that show up in the macOS menu bug me (enormously) and I haven’t found configuration-based a way to disable them. Thus I have disabled them in the relevant menu code.

Shown below is the Freeplane menu with icons (the default):

<img width="300" alt="Menu bar with icons" src="https://user-images.githubusercontent.com/3975572/99749319-47aa0b80-2aac-11eb-8a5b-af1cde853923.png">


Shown below is the Freeplane menu with icons removed (the way I prefer them):

<img width="278" alt="Menu bar without icons" src="https://user-images.githubusercontent.com/3975572/99749323-4973cf00-2aac-11eb-8b8d-ffa5ddbef566.png">

### Dark-Mode Title Bars

The out of the box Freeplane build produces the following when run in dark mode on macOS (note the title-bar color):

<img width="854" alt="Title bars not dark" src="https://user-images.githubusercontent.com/3975572/99749311-4547b180-2aac-11eb-9e48-2129af812d1f.png">

This is been fixed to appear as follows:

<img width="854" alt="Dark mode throughout" src="https://user-images.githubusercontent.com/3975572/99749302-3fea6700-2aac-11eb-8238-7eba50149e19.png">

### Minor Annoyances fixed
In the base Freeplane, the combination of **Format > Copy format** and **Format > Paste format** doesn’t include the *size* of icons in the formatting that is transferred from one node to another. This is fixed—though I haven’t been unable to understand the undo-model (yet)—and as a result the *icon size* part of **Format > Paste format** is not undoable. I will look into how to correctly invoke the “icon-size transferring” logic inside the undo manager at some point (though it isn’t a high priority, for me, at least).

## Building
I had endless trouble getting a working build environment on macOS Catalina. Turns out the build is super-sensitive to particular JDK and Gradle versions. 

What eventually worked for me was the following:

```lang-shell
export JAVA_HOME=~/Apps/mac/jdk-14.0.1.jdk/Contents/Home
export GRADLE_BIN=~/Apps/mac/gradle-6.5.1/bin
export PATH=${GRADLE_BIN}:${JAVA_HOME}/bin:${PATH}
```

The above is codified into `setupPaths.sh` which is called by the added `macDist.sh` script. Obviously you will need to update the paths in `setupPaths.sh` to match your local environment.

Once done, to build you should (hopefully) be able to run `./macDist.sh` from the top-level folder (on either `1.8.x` or `1.8.x_sr` branch) and wait for the resulting DMG to pop open in Finder.

If you run into build failures due to test failures in either `MenuBuildProcessFactoryTest` or `JMenuItemBuilderTest`, or have other Gradle weirdness with `freeplane_debughelper`, run `./fixupBrokenBuild.sh` which will comment out the offending tests and remove `freeplane_debughelper` from the list of Gradle targets. `freeplane_debughelper` is only needed for debugging Freeplane (though I managed to work around the lack of `freeplane_debughelper`... contact me if you want help).

## Dark-Mode Mindmap Template
For those who might be interested, I have created a Freeplane mindmap template that matches macOS’s dark mode.

### Sample Dark Mode Mindmap:
<img width="2560" alt="Dark mode mindmap - sample" src="https://user-images.githubusercontent.com/3975572/99745032-d9af1580-2aa6-11eb-9fbb-111bc0cb787f.png">

### Dark Mode “Style-Master” 
The file `<FREEPLANE_ROOT>/artwork/templates/_Freeplane.StyleMaster.Dark.mm` contains all of the styles needed to render the mindmap shown above. To apply, copy `_Freeplane.StyleMaster.Dark.mm` to your local system somewhere, and then in Freeplane select **Format > Manage styles > Apply map styles from...**, select `_Freeplane.StyleMaster.Dark.mm` (from whichever path you copied it to), and then press **Ok**.

<img width="1438" alt="Dark mode mindmap template" src="https://user-images.githubusercontent.com/3975572/99745463-b042b980-2aa7-11eb-8eb1-44f7a56ef05e.png">

#### Using Keyboard Maestro to apply styles
Not sure how many folks this will interest, but I find [Keyboard Maestro](https://www.keyboardmaestro.com/main/) (KM) indispensable and have a KM macro set up so that `⌃+S` shows the following macro palette, allowing single-key application of relevant styles:

<img width="708" alt="Keyboard Maestro Style macros" src="https://user-images.githubusercontent.com/3975572/99747445-876ef400-2aa8-11eb-8a7f-a1962dd9f12e.png">

If you are interested in the KM macros, feel free to contact me.

## Replacement Icons
As with the splash screen, the “out of the box” icons provided with Freeplane aren’t to my liking. In `<FREEPLANE_ROOT>/artwork/icons/replacementIcons` there are many alternative icons (of which a growing number are scalable vector graphics (SVGs)) which look good on a high-DPI Retina iMac or MacBook display.

To install icons, copy the contents of `<FREEPLANE_ROOT>/artwork/icons/replacementIcons` into `<USER_DIRECTORY>/icons`.

To open the Freeplane `<USER_DIRECTORY>` folder, in Freeplane, 
select **Tools > Open user directory**.

**NOTE:** Be sure to back up your existing icons if you are partial to them.

## Regex-Based Link Decoration
Currently conditionally applied styles cannot use regular expressions to match hyperlinks attached to mindmap nodes. A few years ago (when my knowledge of Freemind, now Freeplane) was more limited than it is today, I hacked together a regex-based approach to automatically “decorate” nodes based on the nodes’ hyperlinks. 

In order to set this up, copy `<FREEPLANE_ROOT>/artwork/icons/linkDecoration.ini` into your Freeplane `<USER_DIRECTORY>` folder. 

`linkDecoration.ini` is a plain-text file with a fairly simple syntax, as follows:

```lang-text
{REGEX}    | ICON_NAME
```

For example:

```lang-text
{^accord://}                       | Accordance
{^bear://x-callback-url/open-note} | Bear_Logo_app_red_note_notes
{^git}                             | git_server_computer_version_source_control.svg
{^http:}                           | world_web_globe-v2-16x16 # 0 0 255
{^https://}                        | world_web_globe-v2-16x16_secure_ssl
{^marginnote3app://}               | MarginNote_app_logo_mindmap_document_blue_deepwork # 0 0 255
{^things://}                       | Things
{^x-devonthink-item://}            | devonthink_research_library_archive_notes # 0 0 255
{^x-devonthink://search}           | devonthink_research_library_archive_notes_search # 0 0 255
{^\.\./.*/$}                       | folder
{^\./.*/$}                         | folder
{^tel:}                            | telephone
...

# Suffix-based rules (type-based)
{\.accord$}                        | Accordance
{\.bat$}                           | file_batch
{\.cmd$}                           | file_batch
{\.csv$}                           | file_csv
{\.DOC$}                           | file_ms_word
{\.doc$}                           | file_ms_word
{\.DOCX$}                          | file_ms_word
{\.docx$}                          | file_ms_word
{\.eml$}                           | email
{\.EPUB$}                          | ePub
{\.epub$}                          | ePub
...
```





## Upstream Notes
[Freeplane](http://freeplane.sourceforge.net) is a free and open source software application that supports thinking, sharing information and getting things done at work, in school and at home. The core of the software is tools for mind mapping (also known as concept mapping or information mapping) and using mapped information. Freeplane is written in Java using OSGi and Java Swing. It runs on any operating system that has a current version of Java installed. It can be run locally or portably from removable storage like a USB drive. 

We use github only as the main code repository, all other project parts are hosted at the [source forge](https://sourceforge.net/projects/freeplane/). So all bugs and feature requests are managed in a [separate issue tracker](https://sourceforge.net/p/freeplane/_list/tickets). There is a drop-down box for selecting different types of issues. Another source of inspiration are our [new forum](https://sourceforge.net/p/freeplane/discussion/758437/) and [old forum](https://sourceforge.net/p/freeplane/oldforum/). Some users write their ideas there. Every contributor and every team member freely decides what task he is going to work on, but for making the best decision we can communicate our self set goals in the above forum. It is supposed to enable early discussions and community feedback and also to motivate you and other people.

How to start hacking and contributing
=====================================

People new to the project can start with implementing a new small feature or doing some refactoring or documentation and not with the bug fixing. Not all bug reports seem to be equally important, and if there are important bugs they usually get fixed by people who broke the functionality. I think you should first get there :) . 

For internal developer discussions there is a private mailing list. It is held private to avoid spam mails. If you want to be subscribed let us know what mail address you are going to use with it. This list is the preferred way to ask all code related questions because they usually get answered soon and by the most competent team member.

Some documentation for new developers is availble in Freeplane wiki https://www.freeplane.org/wiki/index.php/How_to_build_Freeplane and also [here](https://www.freeplane.org/wiki/index.php/Category:Coding) and [here](https://www.freeplane.org/wiki/index.php/Category:Developer). It is written for new guys and should also be maintained by the new guys to stay up to date.

Looking forward to any questions and contributions,

Freeplane development team
