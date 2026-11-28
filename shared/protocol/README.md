# Protocol v0 (development contract only)

JSON envelope fields: version=0, id, senderId, recipientId, ciphertext (opaque nonempty string), expiresAt (positive safe integer epoch milliseconds). Unknown fields are rejected by the schema; client validators check the required fields and reject expired envelopes when supplied a current time. Ciphertext is a placeholder representation: this contract does not authenticate or encrypt anything, and must not be used to transmit real messages. No plaintext drafts are passed to it. Version 1 will define authenticated cryptography, encoding, fragmentation and routing. The fixtures are synthetic.

`fixtures.json` provides common examples, with now=1000. Native and web test suites should be expanded to consume these shared fixtures directly as the protocol evolves.
