import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {languages,translate,translateRoles,normalizeLanguage} from '../src/i18n.ts';
const shared=JSON.parse(readFileSync(new URL('../../../shared/translations/ui.json',import.meta.url),'utf8'));
test('all locales cover the shared keys',()=>{for(const [lang] of languages){for(const key of Object.keys(shared.en)){assert.equal(translate(lang,key),shared[lang][key]);assert.ok(translate(lang,key).length>0)}}});
test('defaults and multi-role labels',()=>{assert.equal(normalizeLanguage(null),'ms');assert.equal(normalizeLanguage('xx'),'ms');assert.equal(translateRoles('ms','Parent + Teacher'),'Ibu bapa + Guru');assert.equal(translate('zh','Open Wall'),'公开交流区')});
