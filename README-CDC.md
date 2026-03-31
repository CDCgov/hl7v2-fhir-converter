# README for CDC-specific Topics
This document contains information specific to the CDC fork of the LinuxForHealth **hl7v2-fhir-converter** project.

## Known Issues
This section contains problematic issues related to this fork as it applies to how the package is used by CDC.

### ClassCastException: class ca.uhn.hl7v2.model.v27.segment.MSH cannot be cast to class ca.uhn.hl7v2.model.v26.segment.MSH
This error manifests itself when running multiple tests for ReportStream.  For example, when running `gov.cdc.prime.router.fhirengine.translator.HL7toFhirTranslatorTests` **test birth date extension addition** the error will occur at `HL7DataExtractor:310`

```
MSH msh = (MSH) message.get("MSH");
```

where it is trying to cast and instance of `ca.uhn.hl7v2.model.v27.segment.MSH` to a `ca.uhn.hl7v2.model.v26.segment.MSH`.  If we trace it back to the source of the call we find it in a debug statement in `HL7MessageEngine` at line 176 triggered by the call to `dataInput.getName()`:

```
LOGGER.debug("Successfully converted Message: {} , Message Control Id: {} to FHIR bundle resource with id {}",
  dataInput.getName(), dataInput.getId(), bundle.getId());
```

When the project is built for a release version it actually strips out the **debug** statements.

## Building a Release and Publishing to GitHub Packages

It is desirable to be able to distribute specific versions of this fork for use by the CDC (e.g., in the ReportStream project) by way of using GitHub Packages.  Documentation on how to do this with Gradle are published [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).  For our purposes, it boils down to two things - creating a (classic) token and modifying `build.gradle`.

### Create a Token

In order to be able to publish a jar file to the GitHub Packages registry for this project one must have created a **personal access token (classic)** within GitHub for their user.  Follow the instructions outlined [here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) and make sure you select at least **read** and **write** access for **packages**.  Make sure you save the token value to a safe place as you will need it later.

### Modify build.gradle

For now it is necessary to check out the code and modify `build.gradle` as part of a manual process to create and deploy the jar file.  Make the following changes to the file:

Change the version to the one to be used for this release.  For version 2.0.0 we would change

```
version = (findProperty('version') == 'unspecified') ? '1.0.1-SNAPSHOT' : version
```

to

```
version = (findProperty('version') == 'unspecified') ? '2.0.0' : version
```

Comment out this:

```
//        maven {
//            name = "OSSRH"
//            def releaseRepo = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//            def snapshotRepo = "https://oss.sonatype.org/content/repositories/snapshots/"
//            url = isReleaseVersion ? releaseRepo : snapshotRepo
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
```

and replace it with this:

```
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cdcgov/hl7v2-fhir-converter")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
```

Comment out this bit as it is not used:

```
//    publications {
//        register<MavenPublication>("gpr") {
//            from(components["java"])
//        }
//    }
```

And, finally, comment out this section:

```
//signing {
//    if (isReleaseVersion) {
//        def signingKeyId = System.getenv("signingKeyId")
//        def signingKey = System.getenv("signingKey")
//        def signingPassword = System.getenv("signingPassword")
//        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
//    }
//    sign publishing.publications.mavenPublication
//}
//
//tasks.withType(Sign).configureEach {
//    onlyIf { isReleaseVersion }
//}
```



### Build and Publish
Once the changes to `build.gradle` have been made the rest is straightforward.

Export the necessary environment variables to publish to GitHub Packages:

```
export GITHUB_USERNAME=<YOUR-USERNAME-IN-GITHUB>
export GITHUB_TOKEN=<THE-TOKEN-YOU-CREATED-AND-SAVED>
```

Now build the release:

```
./gradlew clean build
```

and then publish it:

```
./gradlew publish
...
Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.3/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 28s
9 actionable tasks: 3 executed, 6 up-to-date
```

If you log into GitHub you should be able to navigate to the project and see the published package.
