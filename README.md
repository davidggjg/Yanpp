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

## Building the APK

```bash
cd morphe-manager
./gradlew assembleDebug
```

The APK is produced at `morphe-manager/app/build/outputs/apk/debug/`.

A GitHub Actions workflow (`.github/workflows/build-apk.yml`) builds this
automatically on every push/PR and uploads the APK as a build artifact.

Building a signed release build additionally requires a keystore, which is
not included in this repository — see `morphe-manager/app/build.gradle.kts`
for the expected signing environment variables.
