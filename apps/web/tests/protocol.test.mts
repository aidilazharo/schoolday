import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { isEnvelope } from '../src/protocol.ts';
const fixtures = JSON.parse(readFileSync(new URL('../../../shared/protocol/fixtures.json', import.meta.url), 'utf8'));
test('shared valid and invalid envelopes', () => {
  for (const v of fixtures.valid) assert.equal(isEnvelope(v, fixtures.now), true);
  for (const v of fixtures.invalid) assert.equal(isEnvelope(v, fixtures.now), false);
  assert.equal(isEnvelope(null, fixtures.now), false);
  assert.equal(isEnvelope({...fixtures.valid[0], extra: true}, fixtures.now), false);
});
