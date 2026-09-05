# EkataYan Frontend

## Project Overview

EkataYan is an Android frontend/mobile application for AI-powered travel planning. This repository contains the mobile client only. Repository code and configuration are authoritative for implementation facts; update this guide when verified project-wide facts or durable instructions change.

## Repository Scope

- Keep Android UI, navigation, client-side state, and future frontend integration code in this repository.
- The EkataYan backend is maintained in a separate GitHub repository. Do not assume backend source is available here and do not implement backend services here unless the user explicitly requests it.
- Never store secrets, credentials, tokens, passwords, API keys, or sensitive environment values in this file or source control.

## Tech Stack

Verified from the repository:

- Kotlin and Gradle Kotlin DSL; official Kotlin code style.
- Single Android application module: `:app`.
- Jetpack Compose with Material 3 and the Compose BOM.
- Navigation Compose for the app navigation graph.
- Hilt for dependency injection and Hilt-injected AndroidX ViewModels; KSP performs annotation processing.
- Edge-to-edge `ComponentActivity`; the manifest uses `adjustResize` for software-keyboard insets.
- Java 17 source/target compatibility, minimum SDK 24, and compile/target SDK 37.
- JUnit 4 local tests and AndroidX JUnit, Espresso, and Compose UI instrumentation-test dependencies.

Dependency versions are centralized in `gradle/libs.versions.toml`. Do not duplicate version numbers in module build files.

## Architecture

- Preserve the existing single-activity, Compose, Hilt, ViewModel, and Navigation Compose architecture unless the user explicitly requests an architectural change.
- `MainActivity` enables edge-to-edge rendering and hosts `EkataYanTheme` and `EkataYanApp`.
- `EkataYanNavHost` owns the central navigation graph. Feature packages expose route constants and `NavGraphBuilder` extension functions.
- Features follow the established `FeatureNavigation.kt` -> `FeatureRoute.kt` -> `FeatureScreen.kt` -> `FeatureViewModel.kt` pattern.
- Route composables obtain Hilt ViewModels and pass state plus event callbacks to screen composables. Keep screen composables independent of `NavController`; navigation is expressed through callbacks.
- Keep UI state and user-event handling in the feature ViewModel when state must survive recomposition. Keep reusable, presentation-only composables stateless where practical.
- The Login screen is the current start destination. Login and Sign Up are implemented authentication UI features; Home, Planner, Trips, Expenses, and Profile are presently placeholder-backed features.

## Project Structure

- `app/src/main/java/com/ekatayan/app/app/`: application composition and navigation host.
- `app/src/main/java/com/ekatayan/app/feature/<feature>/`: feature navigation, route, screen, and ViewModel files.
- `app/src/main/java/com/ekatayan/app/core/designsystem/`: shared Compose components and theme definitions.
- `app/src/main/res/`: strings, colors, themes, vector/raster assets, launcher resources, and Android XML configuration.
- `app/src/test/`: local JVM tests.
- `app/src/androidTest/`: device/emulator instrumentation and Compose UI tests.

Add code to the narrowest appropriate feature or core package. Do not place feature-specific UI in the shared design-system package unless it is genuinely reusable.

## Coding Conventions

- Follow official Kotlin formatting and existing package naming under `com.ekatayan.app`.
- Use PascalCase for composables, classes, and files; camelCase for functions and properties; and uppercase snake case for route constants.
- Give public screen composables an optional trailing `modifier: Modifier = Modifier` where consistent with surrounding code.
- Hoist screen state and actions through parameters. Keep navigation wiring in navigation/route layers.
- Put user-visible text in Android string resources rather than introducing new hard-coded UI strings.
- Reuse version-catalog aliases, existing resources, theme values, and shared components before adding equivalents.
- Keep edits focused and preserve unrelated user changes in a dirty working tree.

## UI / UX Guidelines

- Treat a user-provided Figma node as the visual source of truth and adapt it to responsive Compose layouts rather than copying absolute coordinates.
- Reuse existing local Figma-derived assets. Do not replace established backgrounds, logos, or icons unless explicitly requested.
- Preserve edge-to-edge safe-inset handling, responsive scrolling, and keyboard accessibility on form screens.
- Prefer `dp` for layout and `sp` for text. Support different phone sizes and allow vertical scrolling where content can be obscured.
- Separate static visual content from frequently changing form state when it improves recomposition or measurement performance without changing appearance.
- Reuse the shared authentication primitives in `core/designsystem/component/AuthComponents.kt` for matching login/signup backgrounds, branding, fields, actions, dividers, and social buttons.
- Use lightweight Compose controls when default Material minimum sizes prevent matching an approved design, while retaining usable semantics and interaction targets.
- Avoid duplicate IME/system-bar inset handling, focus-triggered work, and unnecessary whole-screen recomposition. The current Activity configuration is `adjustResize`.

## API & Backend Integration

- No networking library, data layer, API client, authentication implementation, or verified backend contract currently exists in this repository.
- When frontend work needs backend functionality, keep transport/data integration separate from UI and ViewModel presentation code.
- Do not invent endpoints or contracts. Obtain or clearly document the required endpoint, HTTP method, request fields, response fields, error behavior, authentication requirements, and external backend dependency before implementation.
- Preserve verified API contracts and existing integration patterns once they are introduced. Record stable cross-repository integration decisions here without including secrets.

## Git & GitHub Workflow

- The repository uses Git and currently has an `origin` GitHub remote. No CI workflow, pull-request template, or formal branching/commit convention is present.
- Do not commit, push, create branches, rewrite history, or open pull requests unless the user explicitly requests that action.
- Never discard or overwrite unrelated working-tree changes. Inspect `git status` before editing and report files changed.
- Do not infer a workflow rule from existing branch or commit names; document one here only when the user establishes it.

## Testing & Build Rules

- Use the Gradle wrapper (`./gradlew` on Unix-like systems or `.\gradlew.bat` on Windows).
- Run `assembleDebug` after implementation changes unless the user requests a different or more targeted verification. Fix compilation errors introduced by the change.
- Add or update local tests for testable logic and instrumentation/Compose UI tests for Android behavior when the change warrants them.
- Existing checked-in tests are starter smoke tests only; do not treat them as meaningful feature coverage.
- No repository CI or lint workflow is currently configured. Do not claim checks ran unless they were actually executed.

## Important Project Decisions

- The Android frontend and backend are separate repositories.
- The established feature boundary is Navigation/Route/Screen/ViewModel.
- Navigation remains callback-driven below the route/navigation layer.
- Figma-based UI work should preserve supplied assets and responsive/keyboard-aware behavior.
- Login and Sign Up share authentication presentation primitives and local assets; keep matching visuals centralized instead of duplicating them per feature.
- Until backend authentication is introduced, the primary Login and Sign Up actions navigate to Home and clear authentication destinations from the back stack; the Login/Sign Up text links continue to navigate between those screens.

## Codex Working Rules

- Read this file before significant repository changes and follow the relevant instructions.
- Maintain this file when the user gives a durable project-wide rule, convention, architectural decision, workflow, or stable integration requirement.
- Do not record temporary tasks, one-off visual adjustments, conversational history, guesses, duplicated rules, or sensitive information.
- When a newer explicit instruction conflicts with an older rule, apply the newer instruction and update this guide so contradictions do not remain.
- Remove an established rule only when the user explicitly requests removal or a newer instruction clearly replaces it.
- If repository state proves an entry outdated, update it from verified code/configuration rather than preserving the assumption.
- Inspect existing code and reusable components before adding new structures or dependencies. Do not change architecture, navigation, Hilt patterns, assets, or backend behavior outside the requested scope.
- Use focused edits, verify proportionally to risk, and report verification results. Do not commit or push without explicit authorization.
