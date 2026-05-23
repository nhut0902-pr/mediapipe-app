# AppLock Project Rules & Guidelines

This document contains persistent project rules, context, and developer instructions for Google AI Studio coding agents working on this AppLock codebase.

## 1. Automatic Versioning Rule (MANDATORY)
*   **Trigger**: Whenever you implement a new feature, make modifications to the UI, or fix a bug in this application.
*   **Action**: You MUST automatically increment the `versionCode` by 1 and update `versionName` (e.g., 1.0 -> 1.1) in `/app/build.gradle.kts`.
*   **Reason**: The project uses an automated GitHub Actions CI/CD workflow (`build-apk.yml`) triggered on tag creation. The workflow reads `versionCode` and `versionName` directly from `/app/build.gradle.kts` to generate the raw `version.json` metadata on push. To ensure seamless auto-updates without manual configuration, the version config in the source code must always be kept fresh.

## 2. GitHub Releases Auto-Update Flow
*   Raw version metadata is fetched from: `https://raw.githubusercontent.com/nhut0902-pr/mediapipe-app/main/version.json`
*   Releases and APK binaries are hosted in the same repository.
*   When updates occur, `UpdateDialog`, `UpdateViewModel`, and `DownloadHelper` interact with the system's `DownloadManager` and `FileProvider` to carry out the upgrade seamlessly. Check for and preserve this plumbing in all screen updates.
