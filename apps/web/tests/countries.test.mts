import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {countries,countryOptions,normalizeCountry} from '../src/countries.ts';
test('country list and default',()=>{const shared=JSON.parse(readFileSync(new URL('../../../shared/countries/iso3166.json',import.meta.url),'utf8'));assert.deepEqual([...countries],shared);assert.equal(new Set(countries).size,249);assert.equal(normalizeCountry(null),'MY');assert.equal(normalizeCountry('XX'),'MY');assert.equal(normalizeCountry('JP'),'JP');assert.equal(countryOptions('ms').length,249);assert.ok(countryOptions('ta').every(x=>x.name.length>0));});
