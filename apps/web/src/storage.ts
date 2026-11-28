export interface Draft { id: string; text: string; createdAt: number }
const KEY = 'schoolday.development.outbox.v1';
export function loadDrafts(): Draft[] {
  try {
    const v: unknown = JSON.parse(localStorage.getItem(KEY) ?? '[]');
    return Array.isArray(v) ? v.filter((d): d is Draft => d && typeof d.id === 'string' && typeof d.text === 'string' && typeof d.createdAt === 'number') : [];
  } catch { return []; }
}
export function saveDrafts(drafts: Draft[]): void { localStorage.setItem(KEY, JSON.stringify(drafts)); }
