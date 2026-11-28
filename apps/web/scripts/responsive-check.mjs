import {chromium} from 'playwright';
import assert from 'node:assert/strict';
import {mkdir} from 'node:fs/promises';
import {spawn} from 'node:child_process';
const server=spawn('npm',['run','preview','--','--host','127.0.0.1','--port','4173'],{stdio:'inherit'});
const url='http://127.0.0.1:4173/schoolday/';
let browser;
async function ready(){for(let n=0;n<60;n++){try{if((await fetch(url)).ok)return}catch{}await new Promise(r=>setTimeout(r,500))}throw Error('Preview server unavailable')}
const sizes=[['phone-small',320,568],['phone',390,844],['phone-landscape',844,390],['tablet-portrait',768,1024],['tablet-landscape',1024,768],['desktop',1440,900],['desktop-window',600,750]];
async function noOverflow(page,label){assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1),label+' page overflow');assert.ok(await page.locator('.chat-panel').evaluate(el=>el.scrollWidth<=el.clientWidth+1),label+' detail overflow')}
try{
 await ready();browser=await chromium.launch({headless:true});await mkdir('responsive-screenshots',{recursive:true});
 for(const [name,width,height] of sizes){
  const page=await browser.newPage({viewport:{width,height}});await page.goto(url);
  await page.waitForFunction(()=>{const e=document.querySelector('#sample');return e&&!e.disabled&&e.options.length>1});await page.locator('#sample').selectOption('student');
  await page.locator('.module-nav .chat-item').first().waitFor({state:'visible'});
  const wide=width>=768&&height>=480;
  assert.equal(await page.locator('.module-icon-nav:visible').count(),0);
  assert.equal(await page.locator('.workspace-tools [data-module=Classroom]').isVisible(),wide);
  if(wide){const left=await page.locator('.sidebar .pane-header').boundingBox();const right=await page.locator('.wide-workspace-header').boundingBox();assert.ok(Math.abs(left.y+left.height-right.y-right.height)<1,'header edges must align');await page.locator('.workspace-tools [data-module=Classroom]').click();assert.equal(await page.locator('.workspace-tools [aria-current=page]').getAttribute('data-module'),'Classroom');await page.locator('.workspace-tools [data-module=Chats]').click();}
  assert.equal(await page.locator('.chat-panel').isVisible(),wide,name+' initial pane');
  await page.locator('.module-nav .chat-item').first().click();
  assert.equal(await page.locator('.sidebar').isVisible(),wide,name+' sidebar after chat selection');
  assert.ok(await page.locator('.chat-panel').isVisible());
  await noOverflow(page,name);await page.screenshot({path:'responsive-screenshots/'+name+'-chat.png'});
  if(!wide)await page.locator('.chat-panel .back').click();
  await page.locator('.footer-nav button').nth(1).click();
  const toolbar=wide?'.workspace-tools':'.module-grid';
  assert.ok(await page.locator(toolbar+' [data-module=Gate]').evaluate(el=>el.classList.contains('unfinished-module')));
  await page.locator(toolbar+' [data-module=Quiz]').click();
  if(wide)assert.equal(await page.locator(toolbar+' [aria-current=page]').getAttribute('data-module'),'Quiz');
  if(!wide)await page.locator('.module-nav .chat-item').first().click();
  await page.locator('.quiz-card').first().waitFor({state:'visible'});
  assert.equal(await page.locator('.sidebar').isVisible(),wide);
  await noOverflow(page,name+' quiz');await page.screenshot({path:'responsive-screenshots/'+name+'-quiz.png'});
  if(wide){await page.setViewportSize({width:390,height:844});assert.ok(await page.locator('.chat-panel').isVisible(),name+' resize preserves detail');assert.ok(!(await page.locator('.sidebar').isVisible()));}
  await page.close();
 }
 const uni=await browser.newPage({viewport:{width:1440,height:900}});await uni.goto(url);await uni.locator('#sample option[value=university]').waitFor({state:'attached'});await uni.locator('#sample').selectOption('university');await uni.locator('.module-nav .chat-item').first().waitFor({state:'visible'});assert.equal(await uni.locator('.module-nav .chat-item').count(),3);await uni.locator('.footer-nav button').nth(1).click();assert.equal(await uni.locator('.workspace-tools [data-module=Gate]').count(),0);await uni.close();
 console.log('Responsive smoke checks passed for '+sizes.length+' viewports');
}finally{await browser?.close();server.kill()}
