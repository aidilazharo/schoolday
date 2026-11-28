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

## Family and points demo

Eight local accounts including children ages 8, 11 and 15 linked to the two sample parents. Shared SQLite migration v5 adds transactional point actions, avatar presets (10 points), age-based quiz answers and parent-authorized skips (5 points), local QR identifiers, friend requests and staff attendance. RM10/100 points purchases are simulations for adults only. No real payment, FFMSP, verified identity or remote sync. Parent requests never grant access automatically. Existing balances are preserved. QR tokens are opaque local identifiers, not authentication secrets. New feature labels and questions are English until localized. Camera scanning is not implemented; paste a decoded identity code. All sample names and ages are fictional.

## Unified message interaction and adaptive profile

One composer sends shared text to the existing social content table. Short messages render as bubbles, longer messages and expanded discussion threads widen automatically. Every shared message supports likes and expandable comments. Migration v6 copies previous shared messages into the social table with stable chat-prefixed IDs and excludes duplicates from the legacy query. Private unsent drafts remain private and non-interactive. Existing posts, reactions, comments, membership and points are retained. Profile occupies the left pane on wide screens while the active conversation remains visible; compact screens show one pane. Media and private replies remain future work.

Conversation rows span the entire chat pane on all screen widths, with a small 12-unit inset on each side. Incoming messages align left and outgoing messages right; the readable bubble-width limit remains independent of row width.

## Context-aware workspace

At widths of 768 and above, a narrow left navigation/list pane and larger right detail pane are always retained. Chats render only in Chats; Profile, Rewards, Quiz, Calendar and Certificates replace the right detail pane. Compact screens use list/detail navigation. Teachers and school administrators can save local private quiz, certificate and event drafts. These are text-based authoring drafts, not published quizzes, issued certificates or scheduled events. SQLite v7 enforces staff-only creation. No permissions, points or chat data are reset.

## Grouped modules and preview workspace

Five categories (Connect, Learn, Campus, Achievements, Me) expose 39 role-filtered modules. Left panel selects a module and lists its saved draft previews, chat spaces or sample records; right panel shows the active editor or record detail. New saved quiz/certificate/event drafts refresh the left list. Draft details are read-only; create new drafts on the right. Unfinished/partial modules have red top lines and text labels. Future module screens are UI scaffolds with sample records and disabled commit actions, not complete production flows. Quiz Unlock offers a dismissible age-based question pop-up, respects current parent skip permission for preview, and never locks a phone or charges points. Family Controls is a non-persisted preview for parents. Existing quiz skip permission remains in Profile. Essential communications are not gated. OS restrictions, real calls, sync and new preview localization are pending.

## Quiz-based recreational access (local demonstration)

SQLite v8 adds parent-configured policies (games, entertainment, social apps; 15, 30 or 60 minutes), validated against linked minor accounts. The existing quiz bank grants a timed demo access window only on a correct age-appropriate answer or an already parent-enabled skip with sufficient demo balance. A new policy invalidates prior grants. Skip is simulated without debit because no actual phone access is provided. Policies and grants persist locally, not across devices. Family Controls and Quiz Unlock remain red/partial. No device lock, app suspension, screen overlay, accessibility service or permissions are enabled. Never deploy demo authentication for real children. Actual iOS app shielding requires Family Controls approval, Managed Settings, a Device Activity monitor, guardian authorization and real-device tests. Android requires an explicitly authorized parental-control design or managed-device policy, safe allowlists, revocation and device testing. Neither platform should replace system authentication or gate emergency access, accessibility or essential communication.

## Responsive usability contract

Web uses a split list/detail layout when the viewport is at least 768 CSS px wide and 480 px high; short landscape phone windows and narrow desktop windows use a single pane. Native iOS uses width plus vertical size class, Android uses available width plus configuration height. Active workspace category is highlighted, Android preserves navigation and editor text across activity recreation, and wide-to-narrow web resizing retains the detail pane. Touch targets, wrapping, keyboard-sensitive spacing, QR sizing and iPad print popover anchoring have been adjusted. The web deployment workflow runs Chromium smoke tests at seven viewport sizes and uploads screenshots. These are browser viewport simulations, not physical iOS/Android device or accessibility certification. Device-level Quiz Unlock, actual calls, radio transport, camera scanning and future module workflows remain incomplete; UI scaffolds are not full working features.

## Two-tier icon navigation

Five primary categories remain at the bottom. Community replaces the Campus display label (stable internal category ID retained). Selecting a category displays a horizontally scrollable icon toolbar immediately above the main navigation; selecting a module updates its record list and detail view. Every module has a short icon label in English, Malay, Chinese and Tamil; detailed preview labels may still be English. The red top border marks partial/unimplemented modules, including on the toolbar. Existing role filters, stable module IDs, saved records and wide/single-pane behaviour are preserved. Institution-neutral labels include Academic administration, Institution administration, Live classes, Practical training, and co-curricular activities. CI browser checks run on Ubuntu 24.04 because the pinned Playwright release cannot install Chromium on Ubuntu 26.04.

## Workspace tool placement

Wide layouts put advanced tools in a category-specific toolbar at the top of the right detail pane: live classes and offline transport, quiz editing, assessment, progress, research, training, event planning, institutions, administration, academic administration, certificate design, credentials, family controls and security. Everyday modules remain above the main navigation in the left panel. Compact layouts retain all permitted modules in the scrollable left/menu toolbar so they remain reachable without requiring a desktop. The selected module always determines the left record list and right details; red indicators and role filtering are unchanged. SwiftUI navigation was decomposed into smaller views to address the previous compiler type-check timeout. Responsive checks cover toolbar placement and switching.

## Single navigation footer and aligned split header

Wide layouts display the five primary categories as the only left footer and all permitted modules in a single horizontal toolbar integrated with the right detail header. Left and right headers share a 66-unit height; a continuous neutral divider separates the panels. Red lines appear on unfinished module icons rather than across the pane boundaries. Compact layouts use a module chooser in the record area when a category is selected; the footer remains one row. The category button in the compact header reopens the chooser. Saved records and permission filters are unchanged. Browser checks verify header alignment, category switching, module access and responsive layout.

## University sample account

Nur Alya Sofea, age 21, is a fictional university student sample with Student role, University Wall, Research Methodology and Open Wall memberships. SQLite migration v9 adds the sample without changing existing accounts or balances, seeds example discussions and a local QR identity. Adult students cannot see Quiz Unlock; the underlying policy/grant database guards also require minors. University academic workflows remain development previews; no real university enrolment is implied.

## Mobile module picker

Compact screens retain the record list and the single five-category footer. Tapping a category or current module label opens a dismissible bottom sheet with one module per row, a selected checkmark and red top marker on unfinished icons. The picker scrolls independently and closes on selection; on web it is a native modal dialog, iOS uses a sheet and Android uses Material3 ModalBottomSheet. Wide screens retain their aligned right header toolbar. Responsive checks exercise dismissal, reopening and selection at compact viewport sizes.

## Persistent mobile footer during module selection

The module picker is non-modal and contained above the permanent footer on compact screens. The records and picker scroll independently; footer categories remain clickable while it is open. Selecting the active category again or using Close dismisses the picker. Switching categories updates the picker without leaving the footer. Desktop and wide tablet headers are unchanged. Web viewport checks verify the picker does not overlap the footer and that switching categories works while it is open.

## Group settings and governance UI

Each chat has a settings icon. On compact devices the settings page uses list/detail navigation; wide workspaces show a settings list and detail when space permits. The member directory reads existing SQLite memberships, scoped to the signed-in member and selected group, with search and a member-action preview. Group information, invitations, roles, administrator election/recall, polls, permissions, rules, announcements, calls, files, events, tasks, quizzes, attendance, certificates, notes, search, notifications, activity, safety, archive and leave have dedicated preview screens. Election defaults are 24 hours, 90-day terms, 3 seats, 50% quorum; the ballot is a disposable UI simulation, not an authenticated vote, and cannot change permissions. Institutional/family authority is not elected away. No actual membership mutation, invitations, moderation, real voting, admin assignment, or synchronization is implemented. Red markers identify unfinished actions; local roster access is read-only. Browser checks verify member search, ballot simulation, and return to chat across viewports. New detailed UI labels are currently English; the main settings label is localized.

## Institution group avatars

User-supplied SK Cyberjaya, SMK Cyberjaya and Universiti Malaya logos are normalized to 80×80 JPEGs with preserved aspect ratio, white padding and no network dependency. The chat list displays 44-unit avatars, chat headers 36-unit avatars, and group settings 56-unit avatars. Existing primary-school demo spaces use SK Cyberjaya, university and research course use Universiti Malaya, and migration v10 adds an SMK Cyberjaya sample group for the secondary student and two linked sample parents. Existing sample memberships and history are preserved. These are fictional demo affiliations, not verified enrollment or institutional endorsement. Generic groups retain initial avatars. Logos are packaged into each app.
