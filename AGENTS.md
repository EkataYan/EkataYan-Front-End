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
- Features retain the `FeatureNavigation.kt` -> `FeatureRoute.kt` -> `FeatureScreen.kt` boundary, with Android ViewModels in `viewmodel/` and local data access in `data/repository/`. The dependency direction is UI -> ViewModel -> Repository -> local data; preserve the existing Hilt injection and navigation scopes.
- Route composables obtain Hilt ViewModels and pass state plus event callbacks to screen composables. Keep screen composables independent of `NavController`; navigation is expressed through callbacks.
- Keep UI state and user-event handling in the feature ViewModel when state must survive recomposition. Keep reusable, presentation-only composables stateless where practical.
- Splash is the current start destination. After its 1.5-second display delay, it leads to Welcome only until the user completes Get Started once; subsequent launches skip Welcome and open Login. This first-run state uses Preferences DataStore (with migration from the former private onboarding SharedPreferences) and resets when app data is cleared or the app is uninstalled. Welcome's Get Started opens the existing Login destination; startup destinations are removed from the back stack as the user continues. Welcome is static and does not require a ViewModel. The repository includes Home, Trips, Wishlist, Group Hub, booking, expenses, profile, notifications, and Business Partner feature packages; inspect each feature before assuming it is a placeholder.

## Project Structure

- `app/src/main/java/com/ekatayan/app/app/`: application composition and navigation host.
- `app/src/main/java/com/ekatayan/app/ui/<feature>/`: migrated feature navigation, routes, and Compose screens.
- `app/src/main/java/com/ekatayan/app/viewmodel/`: Android ViewModels and presentation state.
- `app/src/main/java/com/ekatayan/app/data/`: models, local demo catalogs/data sources, repositories, and Hilt repository bindings.
- `app/src/main/java/com/ekatayan/app/data/local/database/`: Room database version 1, normalized entities, DAOs, snapshots, and domain mappers for Wishlist, Trips, Group Hub, and Business Partner local data.
- `app/src/main/java/com/ekatayan/app/data/local/preferences/`: shared Preferences DataStore used for small frontend preferences and onboarding state.
- `app/src/main/java/com/ekatayan/app/utils/`: shared date parsing and calendar helpers.
- Splash and Welcome use `ui/splash/` and `ui/welcome/`, respectively, with Navigation -> Route -> Screen separation; these static startup screens do not require ViewModels or repositories.
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

- Profile uses Retrofit/OkHttp through `data/remote/` and `ProfileRepository` for the confirmed `GET /api/users/me` and `PATCH /api/users/me` endpoints. The PATCH request supports `display_name`, `phone`, `bio`, `home_city`, `language`, and `interests`; email and avatar are not updated by this contract. `EkataYanApiService` centralizes the verified Flask contracts for trips, members, itineraries, expenses, chat, notifications, weather, and storage, but their existing UI repositories remain local until each screen is migrated to asynchronous UUID-backed state. Profile DTOs map into app models; never expose network DTOs to screens.
- `UserSessionProvider` is the shared session boundary used by the Profile authorization interceptor and is backed by encrypted session storage. Supabase Login/Sign Up and refresh populate its tokens, authenticated user email, and full name. Successful backend profile reads and updates refresh the cached display name so stale local data cannot override the server; Profile obtains email from the session boundary. Never insert test tokens into app configuration.
- The Flask backend defaults to the production HTTPS endpoint through `BACKEND_BASE_URL` in the app Gradle configuration. A Gradle property or ignored `local.properties` entry may override it when explicitly needed. Never configure user tokens or passwords through BuildConfig.
- The profile response has display name, bio, home city, language, interests and an avatar storage path; it does not provide email or travel statistics. Retain the avatar placeholder until an authorized image-download contract exists.
- When frontend work needs backend functionality, keep transport/data integration separate from UI and ViewModel presentation code.
- Do not invent endpoints or contracts. Obtain or clearly document the required endpoint, HTTP method, request fields, response fields, error behavior, authentication requirements, and external backend dependency before implementation.
- Preserve verified API contracts and existing integration patterns once they are introduced. Record stable cross-repository integration decisions here without including secrets.
- Home weather uses the authenticated Flask weather endpoint through `WeatherRepository`; it queries the current profile home city and never stores provider credentials in Android. AI Planner creates a real trip, then `ItineraryRepository` calls the authenticated Gemini-backed itinerary endpoint and renders the complete database-persisted itinerary graph returned by Flask.

## Git & GitHub Workflow

- The repository uses Git and currently has an `origin` GitHub remote. No CI workflow, pull-request template, or formal branching/commit convention is present.
- Do not commit, push, create branches, rewrite history, or open pull requests unless the user explicitly requests that action.
- Never discard or overwrite unrelated working-tree changes. Inspect `git status` before editing and report files changed.
- Do not infer a workflow rule from existing branch or commit names; document one here only when the user establishes it.

## Testing & Build Rules

- Use the Gradle wrapper (`./gradlew` on Unix-like systems or `.\gradlew.bat` on Windows).
- Run `assembleDebug` after implementation changes unless the user requests a different or more targeted verification. Fix compilation errors introduced by the change.
- Add or update local tests for testable logic and instrumentation/Compose UI tests for Android behavior when the change warrants them.
- Local tests cover selected Business Partner, Login, Notifications, and Trips logic; they are not comprehensive app coverage. Business Partner also has a Compose flow test whose runtime compatibility depends on the device and existing Espresso version.
- No repository CI or lint workflow is currently configured. Do not claim checks ran unless they were actually executed.

## Important Project Decisions

- The Android frontend and backend are separate repositories.
- The established feature boundary is Navigation/Route/Screen/ViewModel.
- Navigation remains callback-driven below the route/navigation layer.
- Figma-based UI work should preserve supplied assets and responsive/keyboard-aware behavior.
- Login and Sign Up share authentication presentation primitives and local assets; keep matching visuals centralized instead of duplicating them per feature.
- Supabase email Sign Up sends the entered name and phone as `full_name` and `phone` user-metadata keys. The backend profile trigger uses those keys to provision `profiles.display_name` and `profiles.phone`, including when email confirmation delays creation of an authenticated app session.
- Login navigates to Home only after Supabase authentication stores a valid session. Sign Up does the same when Supabase returns a session; when email confirmation is required, it stays unauthenticated and does not navigate to Home. The Login/Sign Up text links continue to navigate between those screens.

## Codex Working Rules

- Read this file before significant repository changes and follow the relevant instructions.
- Maintain this file when the user gives a durable project-wide rule, convention, architectural decision, workflow, or stable integration requirement.
- Do not record temporary tasks, one-off visual adjustments, conversational history, guesses, duplicated rules, or sensitive information.
- When a newer explicit instruction conflicts with an older rule, apply the newer instruction and update this guide so contradictions do not remain.
- Remove an established rule only when the user explicitly requests removal or a newer instruction clearly replaces it.
- If repository state proves an entry outdated, update it from verified code/configuration rather than preserving the assumption.
- Inspect existing code and reusable components before adding new structures or dependencies. Do not change architecture, navigation, Hilt patterns, assets, or backend behavior outside the requested scope.
- Use focused edits, verify proportionally to risk, and report verification results. Do not commit or push without explicit authorization.

## Business Partner Frontend

- Business Partner is entered through `onPartnershipClick` in the Home quick-action row, immediately after Group Hub. Do not add it to the traveller bottom navigation.
- Register all partner destinations in the existing `EkataYanNavHost`. `PARTNER_HOME_ROUTE` is the partner dashboard; `HOME_ROUTE` remains traveller Home.
- The traveller access-denied dialog has exactly two actions: Go Back dismisses it on Partner Entry; Cancel exits the partner flow to traveller Home. Application Submitted's Back to Home also exits to traveller Home.
- One host-scoped `BusinessPartnerViewModel` and singleton `LocalBusinessPartnerRepository` own the profile, onboarding, document references, listing drafts/listings, bookings, hours, and links. Durable fields are stored in Room and survive process death/restart; form drafts, picker errors, filters, and passwords remain transient.
- Business Partner remains a frontend demo. Registration, login, verification, files, listings, bookings, and analytics do not contact a backend or upload data.
- Partner image selection uses Android Photo Picker contracts; document selection uses OpenDocument. Store URI references, use bounded preview decoding, and never request broad storage permission. Add Photos has a plus/text action with no adjacent photo icon.
- Partner dialogs and menus reuse the Wishlist popup surface/border convention: white background, dark text, light-blue border, and rounded corners. All partner input text is explicitly dark, including in dark device mode.
- Business Partner listings retain structured category-specific values and simple availability in the existing draft/listing models. Availability is an optional inclusive date range with optional daily activity time slots; it is not a booking or inventory engine. Do not collapse transport, vacation rentals, and gear/equipment into one generic form.
- Registration and profile editing share required city, district, country, and owner/manager contact fields. Verification remains local-only; do not promise review turnaround times without a real verification workflow.

- Internal Business Partner headers use page titles rather than repeated EkataYan branding; public/auth and onboarding branding remains.
- Partner Sign Out clears login and transient editing state while retaining locally persisted account data. Delete Account resets the local Room-backed partner data only; real account deletion requires backend integration. Both exit through the public Partner Entry route and clear protected destinations from the back stack.

## Local Frontend Persistence

- Room 2.8.4 is the structured local source of truth. `EkataYanDatabase` uses schema version 1 and is provided with its DAOs through the existing Hilt/KSP setup; generated schemas are kept under `app/schemas/`.
- Wishlist groups, covers, and destination membership; Trips; Group Hub groups, members, messages, reactions, themes, backgrounds, and attachment references; and Business Partner profiles, images, hours, links, document references, listings, listing fields, availability, bookings, and local session flags are persisted in normalized Room tables.
- Preferences DataStore 1.2.1 stores lightweight settings and the welcome-completion flag. Existing encrypted authentication session storage remains separate and unchanged.
- Static destination catalogue records are not duplicated in Room. Wishlist membership stores destination IDs, and Home observes the same Wishlist repository for heart state. Home's upcoming-trip card observes the persisted Trips repository.
- Initial Wishlist, Trips, and Group Hub demo records use persistent feature seed markers. Empty tables after user deletion are not treated as a first run, so deleted sample records are not reinserted. Business Partner demo records are guarded by the persisted `demoLoaded` flag.
- URI strings and metadata are stored instead of image/document binaries. OpenDocument flows retain read access through `DocumentRepository`; Photo Picker flows make a best-effort persistable read grant and rely on the platform Photo Picker grant behavior when the provider does not support it. Broad storage permission is not requested.
