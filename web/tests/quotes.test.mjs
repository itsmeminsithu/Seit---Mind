import test from 'node:test';import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';import {quoteForDay,quoteForMood,dayNumber} from '../lib/quotes.mjs';
const quotes=JSON.parse(readFileSync(new URL('../data/quotes.json',import.meta.url),'utf8'));
test('exactly 100 unique English/Myanmar reflective quotes',()=>{assert.equal(quotes.length,100);assert.equal(new Set(quotes.map(q=>q.en)).size,100);assert.ok(quotes.every(q=>q.mm.length>12))});
test('daily quote covers 100 unique items across 100 consecutive days',()=>{const ids=new Set(Array.from({length:100},(_,i)=>quoteForDay(quotes,new Date(Date.UTC(2026,0,i+1)).toISOString().slice(0,10)).id));assert.equal(ids.size,100)});
test('mood maps to a suitable category',()=>{assert.equal(quoteForMood(quotes,'2026-10-26','positive').category,'gratitude');assert.equal(quoteForMood(quotes,'2026-10-26','calm').category,'peace');assert.equal(quoteForMood(quotes,'2026-10-26','focused').category,'growth');assert.equal(quoteForMood(quotes,'2026-10-26','refreshed').category,'rest')});
test('day validation rejects malformed input',()=>{assert.throws(()=>dayNumber('2026-02-30'));assert.throws(()=>dayNumber('today'))});
