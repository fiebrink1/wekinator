# Wekinator

This is my fork of Rebecca Fiebrink's Wekinator (www.wekinator.org).

I haven't changed anything significant about the UI or behavior. I've primarily cleaned up
outdated dependencies and updated some things to work with modern Java and OS X.

Changes:
- added argument parsing support to allow launching in performance mode with a --project arg
- updated XStream to the latest version to work on modern versions of java
- cleaned up exception handling
- upgraded to the latest version of JavaOSC
- added a gradle build so other projects can build with Wekinator as a dependency
- added the ability to package Wekinator as a standalone jar
