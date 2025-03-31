# How to produce a `css4j-dom4j` release

Please follow these steps to produce a new release of css4j-dom4j.

## Requirements

- The [Git version control system](https://git-scm.com/downloads) is required to
obtain the sources. Any recent version should suffice.

- Java 11 or later. You can install it from your favourite package manager or by
downloading from [Adoptium](https://adoptium.net/).

- The [`generate_directory_index_caddystyle.py`](https://gist.github.com/carlosame/bd5b68c4eb8e0817d9beb1dcfb4de43d)
script and a recent version of [Python](https://www.python.org/) (required to
run it). The script is necessary to create the index files in the bare-bones
Maven repository currently used by css4j-dom4j.

## Steps

1) If your local copy of the css4j-dom4j Git repository exactly matches the current
`master` HEAD, use that copy to execute the `gradlew` commands shown later,
otherwise create a new clone of the `git@github.com:css4j/css4j-dom4j.git`
repository with `git clone` and use it.

For reference, let your copy of the css4j-dom4j release code be at
`/path/to/css4j-dom4j`.

2) Execute `./changes.sh <new-version>` to create a `CHANGES.txt` file with the
changes from the latest tag. For example if you are releasing `6.0.1`:

```shell
./changes.sh 6.0.1
```

Edit the resulting `CHANGES.txt` as convenient and use it as the basis to update
the `RELEASE_NOTES.md` document. Paste the list of changes in under the
`## Detail of changes` section.

3) In the `master` branch of your local copy of the css4j-dom4j Git repository,
bump the `version` in the [`build.gradle`](build.gradle) file or remove the
`-SNAPSHOT` suffix as necessary.

Commit the changes (`build.gradle` + `RELEASE_NOTES.md`) to the Git repository.

4) If there is an issue tracking the release, close it (could be done adding a
'closes...' to the message in the previously described commit).

5) To check that everything is fine, build the code:

```shell
cd /path/to/css4j-dom4j
./gradlew build
```

6) Clone the `git@github.com:css4j/css4j.github.io.git` repository (which
contains a bare-bones Maven repository) and let `/path/to/css4j.github.io` be
its location.

7) From your copy of the css4j-dom4j release code, write the new artifacts into
the local copy of the bare-bones Maven repository with:

```shell
cd /path/to/css4j-dom4j
./gradlew publish -PmavenReleaseRepoUrl="file:///path/to/css4j.github.io/maven"
```

8) Produce the necessary directory indexes in the local copy of the bare-bones
Maven repository using [`generate_directory_index_caddystyle.py`](https://gist.github.com/carlosame/bd5b68c4eb8e0817d9beb1dcfb4de43d):

```shell
cd /path/to/css4j.github.io/maven/io/sf/carte
generate_directory_index_caddystyle.py -r css4j-dom4j
```

If the changes to the `css4j.github.io` repository look correct, commit them but
do not push yet.

9) Clone the [css4j-dist](https://github.com/css4j/css4j-dist) repository and
execute `./gradlew mergedJavadoc`. Move the javadocs from `build/docs/javadoc`
to `/path/to/css4j.github.io/api/latest`:

```shell
rm -fr /path/to/css4j.github.io/api/latest
mkdir /path/to/css4j.github.io/api/latest
mv /path/to/css4j-dist/build/docs/javadoc/* /path/to/css4j.github.io/api/latest
```

If the changes to the `css4j.github.io` repo look correct, commit them with a
description like "Latest modular Javadocs after css4j-dom4j 6.0.1" and push.

Check whether the ["Examples" CI](https://github.com/css4j/css4j.github.io/actions/workflows/examples.yml)
triggered by that commit to the `css4j.github.io` repository completed
successfully. A failure could mean that the jar file is not usable with Java 8,
for example.

10) Create a `v<version>` tag in the css4j-dom4j Git repository. For example:

```shell
cd /path/to/css4j-dom4j
git tag -s v6.0.1 -m "Release 6.0.1"
git push origin v6.0.1
```

or `git tag -a` instead of `-s` if you do not plan to sign the tag. But it is
generally a good idea to sign a release tag.

Alternatively, you could create the new tag when drafting the Github release
(next step).

11) Draft a new Github release at https://github.com/css4j/css4j-dom4j/releases

Summarize the most important changes in the release description, then create a
`## Detail of changes` section and paste the contents of the `CHANGES.txt` file
under it.

Add to the Github release the _jar_ files from this release.

12) Verify that the new [Github packages](https://github.com/orgs/css4j/packages?repo_name=css4j-dom4j)
were created successfully by the [Gradle Package](https://github.com/css4j/css4j-dom4j/actions/workflows/gradle-publish.yml)
task.

13) In your local copy of the [css4j-dist](https://github.com/css4j/css4j-dist)
repository, update the css4j-dom4j version number in the
[maven/install-css4j.sh](https://github.com/css4j/css4j-dist/blob/master/maven/install-css4j.sh)
script. Commit the change, push and look for the completion of that project's
CI.
