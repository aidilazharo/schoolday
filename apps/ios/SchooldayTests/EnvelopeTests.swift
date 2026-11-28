import XCTest
@testable import Schoolday

final class EnvelopeTests: XCTestCase {
    func testContract() {
        XCTAssertTrue(Envelope(version: 0, id: "m1", senderId: "a", recipientId: "b", ciphertext: "fixture-only", expiresAt: 2000).isValid(now: 1000))
        XCTAssertFalse(Envelope(version: 1, id: "m1", senderId: "a", recipientId: "b", ciphertext: "fixture-only", expiresAt: 2000).isValid(now: 1000))
        XCTAssertFalse(Envelope(version: 0, id: "m1", senderId: "a", recipientId: "b", ciphertext: "fixture-only", expiresAt: 1000).isValid(now: 1000))
        XCTAssertFalse(Envelope(version: 0, id: "m1", senderId: "", recipientId: "b", ciphertext: "fixture-only", expiresAt: 2000).isValid(now: 1000))
    }
}
