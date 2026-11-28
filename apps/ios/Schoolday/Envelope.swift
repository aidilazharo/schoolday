import Foundation

struct Envelope: Codable {
    let version: Int
    let id: String
    let senderId: String
    let recipientId: String
    let ciphertext: String
    let expiresAt: Int64

    func isValid(now: Int64) -> Bool {
        version == 0 && !id.isEmpty && !senderId.isEmpty && !recipientId.isEmpty &&
        !ciphertext.isEmpty && expiresAt > 0 && expiresAt <= 9_007_199_254_740_991 && expiresAt > now
    }
}
