export interface Envelope { version: 0; id: string; senderId: string; recipientId: string; ciphertext: string; expiresAt: number }
export function isEnvelope(value: unknown, now: number): value is Envelope {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) return false;
  const v = value as Record<string, unknown>;
  const fields = ['version', 'id', 'senderId', 'recipientId', 'ciphertext', 'expiresAt'];
  return Object.keys(v).length === fields.length && fields.every(k => Object.hasOwn(v,k)) &&
    v.version === 0 && ['id','senderId','recipientId','ciphertext'].every(k => typeof v[k] === 'string' && (v[k] as string).length > 0) &&
    typeof v.expiresAt === 'number' && Number.isSafeInteger(v.expiresAt) && v.expiresAt > 0 && v.expiresAt > now;
}
