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
