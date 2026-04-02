# Contributing

Thank you for your interest in improving this project.

## How to contribute

1. **Fork** the repository and create a branch for your change.
2. **Describe** the problem or feature in the pull request so reviewers can follow your intent.
3. **Keep changes focused** — one logical change per pull request is easier to review.
4. **Match existing style** — Kotlin formatting, naming, and structure consistent with the current codebase.

## Development setup

- Android Studio (recent stable) with Android SDK 34.
- Open the project root that contains `settings.gradle.kts` / `app/`.
- Add your own `model.tflite` and `labels.txt` under `app/src/main/assets/` (see README). Large binaries are usually gitignored; do not commit proprietary models without permission.

## Pull request checklist

- [ ] App builds (`./gradlew assembleDebug` or Build → Make Project).
- [ ] You have the right to license any code or assets you submit under this project’s [LICENSE](LICENSE).

Questions and small fixes are welcome via issues and pull requests.
