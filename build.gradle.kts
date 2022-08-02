plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

version = 0.3
group = "com.wekimini"

repositories {
    mavenCentral()
}

dependencies {
    listOf(
        "FastDTW1.1.0.jar",
        "JSON.jar",
        "swing-worker-1.2.jar",
        "weka.jar",
        "swing-layout/swing-layout-1.0.4.jar",

        //"javaosc.jar", // This wasn't used anyway, because it was replaced by a forked version of the source code.
        //"commons-cli-1.4.jar", // Added by me for args parsing, replaced by 1.5.0 from Maven.

        // These are commented out because the build succeeds without them. There's
        // a possibility that there are runtime dependencies on something in here,
        // e.g. through reflection.
        //"absolutelayout/AbsoluteLayout.jar",

        // These were the jars for XStream (xml serialization of java objects). There seemed to be a bug in XStream that
        // required the xmlpull and xpp jars, but those were obsoleted when I upgrade the XStream jar to 1.4.18, I
        // think. Anyway, it seems to work fine with xstream 1.4.19 pulled from Maven.
        //"xstream-1.4.18.jar",
        //"xmlpull-1.1.3.1.jar",
        //"xpp3_min-1.1.4c.jar",
    ).forEach {
        implementation(files("lib/$it"))
    }
    implementation("com.thoughtworks.xstream:xstream:1.4.19")
    implementation("com.illposed.osc:javaosc-core:0.8")
    implementation("commons-cli:commons-cli:1.5.0") // 1.5.0 is available

    /* implementation("org.jdesktop:swing-worker:1.1") // jar was 1.2, but that doesn't seem to be available */
    /* implementation("net.java.dev.swing-layout:swing-layout:1.0.2") */
}

// run ./gradlew shadowJar to generate build/libs/wekinator-all.jar. This is a
// jar that contains all dependencies, so it can be moved to another system and
// run with java -jar.
project.setProperty("mainClassName", "wekimini.WekiMiniRunner") // for shadowJar

// This puts the entire java source tree into the src/main/resources folder of
// the output jar. This is because resource files like icon images are mixed in
// with the java source, and this one-line change allows the project to build
// without moving a bunch of files and changing a bunch of hardcoded filepaths
// in code. (At least, I think that's what this does.)
sourceSets { main { resources { srcDirs("src/main/java") } } }
