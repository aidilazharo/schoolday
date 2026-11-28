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

Bahasa Melayu (default), English, Simplified Chinese and Tamil are available only in the signed-in Profile. Preferences are stored locally per account on each device. Stable database roles/channel IDs and user-authored messages are not translated. Translation keys live in shared/translations; native and web dictionaries are embedded so the UI can switch offline. Other Malaysian languages may be added later.

## Country and region

Select any of 249 ISO 3166-1 countries and territories, default Malaysia (MY). Country names are localized by the platform where available. Preference is stored locally per account, separately from language and school memberships. Country selection does not verify residency, grant access, configure radio frequencies or implement country-specific legal rules. Native selectors support search; browser uses its standard select control.

Country and language controls are exclusively inside Profile. Signed-out screens use Bahasa Melayu; new profiles default to Malaysia and Bahasa Melayu. Existing device-global preferences are not assigned to an account because ownership is unknown. Profile preferences use account-keyed browser localStorage / native preferences and are not synchronized between devices.

## Full names and nicknames

Each provisioned account has a full name (fictional names in the demo), displayed at sign-in and in Profile. The nickname is selected only from one or two whitespace-separated parts of that registered name, preserving spelling and original order. A one-part full name is valid and offers that name only. A nickname defaults to the first part, can be changed in Profile and is stored per account in SQLite; arbitrary nicknames are rejected by the storage layer. The profile displays the full name separately. Existing account IDs, roles, drafts and preferences are unchanged by the idempotent seed migration. Real registration and identity verification are not implemented; full names are provisioned, not editable in this demo. Nicknames are local, not synced across devices.

The chat list no longer displays the signed-in user's name or role above conversations. Profile remains available in web footer navigation and native header controls. Identity and permissions are unchanged.

## Spaces and opening points

Each existing membership-based space has a default Wall tab and a Chat tab. Local SQLite posts, comments and toggled likes are shared by sample users on the same installation who belong to that space. Existing chat drafts remain private local outboxes, not delivered messages. All provisioned accounts receive 100 points on migration; INSERT OR IGNORE preserves existing balances. Points are demonstration-only, not redeemable, transferable or FFMSP-backed. Future registration must provision a points account as part of registration. No remote synchronization, media, direct messages, moderation/reporting or post-to-chat sharing is implemented. Use fictional content only.

## Unified Space timeline

Supersedes separate Wall/Chat views. Posts and shared local chat messages are merged by timestamp and stable ID in one timeline, with a composer mode for Chat or Post. Existing drafts are included only for their owner and marked not sent; they are not silently published. Existing post IDs, comments, reactions, points and memberships are preserved. SQLite v4 seeds fictional messages and posts from all six accounts, restricted to existing memberships. New messages are shared locally on the same installation only, not transmitted. No media uploads, delivery receipts, cross-device sync or direct private messaging yet. Composer mode is not a timeline filter. No automatic publishing of private drafts.

## Remember local session

The signed-in demo account ID is retained locally across refresh, app relaunch and updates until Logout. Applies to both sample and provisioned normal test accounts. The saved ID is resolved against SQLite on startup; missing accounts clear the session. Passwords are not saved. Browser storage, iOS AppStorage and Android SharedPreferences are used. Clearing local data or reinstalling can remove the session. This is demo convenience only, not a secure production session; production requires authenticated, revocable tokens or device-bound keys.
