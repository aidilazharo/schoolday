# Native architecture

Each client independently implements the shared contract. No cross-language FFI is required. UI, storage and platform adapters remain separate. Keep common design tokens, protocol schemas, fixtures and security requirements in shared/.

## Delivery

Local outbox -> authenticated encryption -> versioned envelope -> transport selection -> peer or consented relay -> authenticated acknowledgement. A local save is never a delivery acknowledgement. Transport readiness is not evidence of recipient reachability.

BLE, local Wi-Fi and internet transports must be implemented with native APIs or supported browser APIs. External LoRa radio hardware is required. Use hardware-specific discovery and a common wire format; don't put images or videos on LoRa. Mesh routing must enforce quotas, expiry, hop limits, deduplication, replay protection and consent. iOS/Android background execution is limited; web is not a dependable unattended relay.

## Security

No production cryptography is implemented. Use established authenticated encryption and audited protocol libraries, not home-grown crypto. Establish identities, key rotation, membership/revocation and secure key storage before enabling peer transfer. Local development drafts use ordinary application storage; never use real student data. No raw private messages should be written to diagnostic logs.

## Shared contract and parity

Changes to the versioned protocol and access rules must include positive/negative fixtures and tests in every client. Reject unsupported versions. Do not assume binary wire compatibility simply because JSON fixtures match. Keep school/parent/teacher relationship permissions and FFMSP state transitions explicitly specified and tested before implementation.

## Delivery plan

1. Native clients, local outbox, builds and web deployment.
2. Physical device discovery, link permissions and external LoRa bridge.
3. Authenticated transport protocol, encrypted outbox and relay routing.
4. School and class walls, profiles and multi-role relationships.
5. FFMSP wallet, events, quizzes and governance.
6. Security, interoperability, physical hardware and performance validation.

## Adaptive layout

Use compact single-pane navigation below 768 CSS px / pt / dp of available width; split view above that threshold. Web uses a wider sidebar from 1200 px. Test narrow phones, landscape, split-screen tablets, desktop resizing, keyboard and large text. Desktop support is via browser, not a separate desktop executable. Local draft storage is unchanged.
