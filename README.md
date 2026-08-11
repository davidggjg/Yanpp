<div align="center">
  <img src="morphe-manager/assets/morphe_app_icon.png" width="128" alt="Morphe icon" />
  <h1>Morphe</h1>
  <p>A single, self-contained monorepo for the Morphe app patching ecosystem.</p>
</div>

## What this is

Morphe lets you patch and enhance apps on your device (remove ads, unlock
features, tweak behavior). This repository bundles every piece the app
needs to build, as local sibling projects in one repo, so nothing is
fetched from other GitHub repos at build time:

| Directory                     | What it is                                              |
| ------------------------------ | -------------------------------------------------------- |
| `morphe-manager`               | The Android app (Manager) — this is what produces the APK |
| `morphe-patcher`                | Applies patches to target app APKs                       |
| `morphe-library`                | Shared library used by the patcher, CLI, and manager      |
| `morphe-patches`                | The patch definitions bundle                              |
| `morphe-patches-library`        | Shared patch/extension code used by `morphe-patches`       |
| `morphe-patches-gradle-plugin`  | Gradle settings plugin used by `morphe-patches`            |
| `morphe-patches-template`       | Starter template for writing new patches                  |
| `morphe-cli`                    | Desktop command-line interface                            |
| `microg-re`                     | microG re-implementation (GmsCore replacement components) |
| `docs`                          | Project documentation                                     |

Each Gradle project already knows how to look for its sibling projects
(`morphe-manager` includes `../morphe-patcher`, `morphe-patches` includes
`../morphe-patcher`, `../morphe-patches-library`, and
`../morphe-patches-gradle-plugin`, etc.) via Gradle composite builds, so as
long as they all live next to each other — which they now do, flattened at
the root of this repo — a build resolves everything locally instead of
downloading `app.morphe:*` artifacts from GitHub Packages.

`morphe-library` is the one exception: it's built with Android Gradle
Plugin 8.9.3, while `morphe-manager` uses 9.3.1, and Gradle refuses to mix
AGP versions inside one composite build. So instead of joining the
composite build, it's published to `mavenLocal()` first (a separate Gradle
process, so the AGP mismatch doesn't matter), and `morphe-manager` picks it
up from there.

## Building the APK

```bash
cd morphe-library
./gradlew publishToMavenLocal

cd ../morphe-manager
./gradlew assembleRelease
```

The signed APK is produced at `morphe-manager/app/build/outputs/apk/release/`.

A GitHub Actions workflow (`.github/workflows/build-apk.yml`) builds this
automatically on every push/PR and uploads the APK as a build artifact.

### About the signing key

`morphe-manager/app/keystore.jks` is a self-signed release key generated
for this app, with its passwords in `morphe-manager/app/keystore.properties`
right next to it, so the build is fully self-contained and doesn't need any
secrets configured elsewhere. Android only requires that every update to an
already-installed app be signed with the *same* key — this one is just as
valid as any other for that purpose.

The trade-off: because the key lives in this repository, anyone with read
access to it can extract the key. That's fine as long as this repo stays
private. If it's ever made public, treat the key as compromised — delete
`app/keystore.jks` and `app/keystore.properties` and let the build fall
back to debug signing (or generate a fresh keystore) before publishing
further releases.
