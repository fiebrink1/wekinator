/* plugins { */
/*   id("java") */
/* } */


// The below seems to be the way to import the ant build so I can run its tasks
// from gradle, but I'm not sure it's producing things in a way that can be
// depended on by other gradle builds.
ant.importBuild("build.xml") { oldTargetName -> when (oldTargetName) {
    "init" -> "ant_init"
    else -> oldTargetName
  }
}

// This seems to be necessary to let the project depending on wekinator
// reference this gradle project.
configurations {
  create("wekijar")
}

// Trying to produce artifacts that can be used by other gradle builds.
sourceSets {
  main {
    output.dir(
  }
}

// Trying to register a task that can be referenced by other gradle builds.
tasks.register("build")
tasks.named("build") {
  dependsOn("standalone-jar")
}
