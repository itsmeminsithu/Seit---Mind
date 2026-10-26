import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const src=readFileSync(new URL('../app/page.tsx',import.meta.url),'utf8');
const css=readFileSync(new URL('../app/globals.css',import.meta.url),'utf8');
for(const view of ['home','explore','progress','profile','settings','about','player','complete']){
 test(`navigation has isolated ${view} screen`,()=>assert.ok(src.includes(`displayedView==="${view}"`)));
}
test('user settings only render on settings screen',()=>{const start=src.indexOf('displayedView==="settings"&&<section');const end=src.indexOf('displayedView==="about"&&<section');assert.ok(start>0&&end>start);const settings=src.slice(start,end);assert.ok(settings.includes('Share card privacy'));assert.ok(settings.includes('Daily reminder'));assert.ok(settings.includes('Display name'));});
test('home avoids editing controls',()=>{const start=src.indexOf('displayedView==="home"&&');const end=src.indexOf('displayedView==="explore"&&');assert.ok(start>0&&end>start);assert.ok(!src.slice(start,end).includes('type="file"'));assert.ok(!src.slice(start,end).includes('Share card privacy'));});
test('player audio survives tab navigation',()=>assert.ok(src.includes('<audio ref={audio}')&&src.indexOf('<audio ref={audio}')>src.indexOf('displayedView==="complete"')));
test('responsive bottom navigation',()=>assert.ok(css.includes('.mobileNav{display:none}')&&css.includes('.mobileNav{position:fixed')));
test('developer photo only in about screen',()=>assert.ok(src.includes('displayedView==="about"&&')&&src.includes('src="/developer-avatar.jpg"')));
