# Schoolday

Native-first, offline-first school communication platform. One monorepo; three independent clients. No React Native, Expo, Capacitor, Rust or mandatory messaging server.

| Platform | Stack | Build |
| --- | --- | --- |
| iOS / iPadOS | Swift, SwiftUI | XcodeGen + Xcode |
| Android | Kotlin, Jetpack Compose | Gradle, JDK 17, Android SDK 35 |
| Web | TypeScript, React, Vite | Node 22, npm |
| LoRa hardware | C++ | PlatformIO (hardware integration pending) |

## Run

Web: `cd apps/web && npm install && npm test && npm run build && npm run dev`.

iOS (macOS): `brew install xcodegen`, then `cd apps/ios && xcodegen generate && open Schoolday.xcodeproj`. Set signing in Xcode for physical devices. Minimum iOS 17.

Android: open `apps/android` in Android Studio (JDK 17), or install Gradle 8.11.1 and run `gradle -p apps/android :app:assembleDebug :app:testDebugUnitTest`. Generate a Gradle wrapper locally with `gradle -p apps/android wrapper --gradle-version 8.11.1` if needed.

## Web test deployment

The deploy-web workflow tests and builds the web client and publishes its dist directory to GitHub Pages at https://aidilazharo.github.io/schoolday/. In repository Settings > Pages, choose GitHub Actions as the source if not already configured. The site contains no backend or credentials. Never enter real student information in this development demo.

## Scope

Initial native chat UI with local-only draft/outbox persistence, shared envelope contract and unit tests. A saved message is NOT sent, encrypted or delivered. Browser offline shell caching is enabled only on production builds. No central messaging server is required for this prototype.

Next: authenticated identity/key exchange, secure encrypted storage, BLE/Wi-Fi adapters and device pairing, LoRa bridge, packet framing and routing, reliable acknowledgements, then role relationships, school/class walls, FFMSP and release automation. Do not transmit sensitive data until encryption and authorization are implemented and tested. Full feature parity and actual offline peer delivery are not implemented.

Protocol v0 uses JSON for inspectable interoperability fixtures; the radio format (e.g. CBOR) will be specified before transmission. See shared/protocol. Bundle identifiers are provisional. Native platform restrictions still apply.

See docs/architecture.md and firmware/esp32-lora/README.md.

## Local account testing (development only)

All three clients use SQLite: system SQLite on mobile and sql.js (SQLite WASM) with IndexedDB snapshots on web. Five fictional sample profiles are available via dropdown. Normal user login tests a provisioned local account: `member@example.invalid` / `SchooldayTest2026!`. This is a public development credential, not production authentication. The local password verifier uses PBKDF2-HMAC-SHA256 with 600,000 iterations. No account registration, remote authentication, verified school affiliation, encrypted storage or real inter-device communication is provided. Never enter real credentials or student data. Sample membership rules are fixed test data, not real school authorization. Each draft is scoped by account and channel. Previous anonymous outbox data is not imported, because its ownership cannot be determined. Web SQLite snapshots are intended for single-tab testing; multiple tabs can overwrite each other's snapshots. First use needs online access to load WASM. Desktop remains browser-based.

## Design restoration

Reconstructed monochrome text-only branding from the previous Schoolday design requirements; original historic prototype and logo files are not present in this repository. The web test client restores the compact chat sidebar and outline footer navigation for Chats, Calendar, Quiz, Rewards and Profile. Calendar, Quiz and Rewards are empty states until their features are implemented. Native clients share the monochrome text-only wordmark and adaptive chat/login layouts. No images or exact prior custom font have been recovered.

## Languages

Bahasa Melayu (default), English, Simplified Chinese and Tamil are available in the login and account sidebar. Preference is stored locally on each device, independent of account. Stable database roles/channel IDs and user-authored messages are not translated. Translation keys live in shared/translations; native and web dictionaries are embedded so the UI can switch offline. Other Malaysian languages may be added later.
