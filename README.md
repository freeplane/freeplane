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

<img width="670" alt="Old Splash Screen" src="https://user-images.githubusercontent.com/3975572/99743482-c3538a80-2aa3-11eb-85ce-c125b0232edc.png">

#### Filterable icon selector (type a few letters to filter):

<img width="226" alt="Filterable icon selector" src="https://user-images.githubusercontent.com/3975572/99743781-61dfeb80-2aa4-11eb-9c69-d089784fb8b9.png">


## Building
I had endless trouble getting a working build environment on macOS Catalina. Turns out the build is super-sensitive to particular JDK and Gradle versions. 

What eventually worked for me was the following:

```lang-shell
export JAVA_HOME=~/Apps/mac/jdk-14.0.1.jdk/Contents/Home
export GRADLE_BIN=/Users/stuart/Apps/mac/gradle-6.5.1/bin
export PATH=${GRADLE_BIN}:${JAVA_HOME}/bin:${PATH}
```

The above is codified into `setupPaths.sh` which is called by the added `macDist.sh` script.

Thus, to build you should (hopefully) be able to checkout either branch `1.8.x` or `1.8.x_sr` and then run `./macDist.sh` from the top-level folder.

If you run into build failures due to test failures in either `MenuBuildProcessFactoryTest` or `JMenuItemBuilderTest`, or have other Gradle weirdness with `freeplane_debughelper`, run `./fixupBrokenBuild.sh` which will comment out the offending tests and remove `freeplane_debughelper` from the list of Gradle targets. `freeplane_debughelper` is only needed for debugging Freeplane (though I managed to work around the lack of `freeplane_debughelper`... contact me if you want help).

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
