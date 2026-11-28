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
  const schoolLogo=page.locator('.chat-item').filter({hasText:'SK Cyberjaya'}).locator('img');assert.equal(await schoolLogo.count(),1);assert.ok(await schoolLogo.evaluate(img=>img.complete&&img.naturalWidth===80&&img.naturalHeight===80));
  const wide=width>=768&&height>=480;
  assert.equal(await page.locator('.module-icon-nav:visible').count(),0);
  assert.equal(await page.locator('.workspace-tools [data-module=Classroom]').isVisible(),wide);
  if(wide){const left=await page.locator('.sidebar .pane-header').boundingBox();const right=await page.locator('.wide-workspace-header').boundingBox();assert.ok(Math.abs(left.y+left.height-right.y-right.height)<1,'header edges must align');await page.locator('.workspace-tools [data-module=Classroom]').click();assert.equal(await page.locator('.workspace-tools [aria-current=page]').getAttribute('data-module'),'Classroom');await page.locator('.workspace-tools [data-module=Chats]').click();}
  assert.equal(await page.locator('.chat-panel').isVisible(),wide,name+' initial pane');
  await page.locator('.module-nav .chat-item').first().click();
  assert.equal(await page.locator('.sidebar').isVisible(),wide,name+' sidebar after chat selection');
  assert.ok(await page.locator('.chat-panel').isVisible());
  await noOverflow(page,name);await page.screenshot({path:'responsive-screenshots/'+name+'-chat.png'});
  await page.locator('.group-settings-toggle:visible').click();
  await page.locator('.group-settings').waitFor({state:'visible'});
  await page.locator('[data-group-section=members]').click();
  assert.ok(await page.locator('.group-member').count()>0,name+' roster missing');
  await page.getByLabel('Search members').fill('no matching sample xyz');
  assert.equal(await page.locator('.group-member').count(),0);
  await page.getByLabel('Search members').fill('');
  if(await page.locator('.group-menu-back').isVisible())await page.locator('.group-menu-back').click();
  await page.locator('[data-group-section=election]').click();
  assert.equal(await page.locator('.group-disabled').isDisabled(),true);
  const ballot=page.locator('input[name=demo-candidate]').first();
  if(await ballot.count()){await ballot.check();await page.getByRole('button',{name:'Submit preview vote'}).click();assert.ok(await page.getByRole('button',{name:'Preview vote recorded'}).isDisabled());}
  await noOverflow(page,name+' group settings');await page.screenshot({path:'responsive-screenshots/'+name+'-group-settings.png'});
  await page.getByRole('button',{name:'Close group settings'}).click();
  if(!wide)await page.locator('.chat-panel .back').click();
  await page.locator('.footer-nav button').nth(1).click();
  const toolbar=wide?'.workspace-tools':'.module-list';
  if(!wide){await page.locator('.mobile-module-sheet').waitFor({state:'visible'});assert.ok(await page.locator('.record-list').isVisible(),name+' records retained behind picker');const sheet=await page.locator('.mobile-module-sheet').boundingBox();const footer=await page.locator('.footer-nav').boundingBox();assert.ok(sheet.y+sheet.height<=footer.y+1,name+' sheet covers footer');assert.ok(footer.y+footer.height<=height+1,name+' footer offscreen');await page.locator('.footer-nav button').nth(2).click();assert.ok(await page.locator('.mobile-module-sheet [data-module=Calendar]').isVisible(),name+' footer cannot switch categories');await page.locator('.footer-nav button').nth(1).click();assert.ok(await page.locator('.mobile-module-sheet [data-module=Quiz]').isVisible());await page.screenshot({path:'responsive-screenshots/'+name+'-module-picker.png'});await page.keyboard.press('Escape');await page.locator('.mobile-module-sheet').waitFor({state:'hidden'});await page.locator('.mobile-modules').click();await page.locator('.mobile-module-sheet').waitFor({state:'visible'});}
  assert.ok(await page.locator(toolbar+' [data-module=Gate]').evaluate(el=>el.classList.contains('unfinished-module')));
  await page.locator(toolbar+' [data-module=Quiz]').click();
  if(wide)assert.equal(await page.locator(toolbar+' [aria-current=page]').getAttribute('data-module'),'Quiz');
  if(!wide){await page.locator('.mobile-module-sheet').waitFor({state:'hidden'});await page.locator('.module-nav .chat-item').first().click();}
  await page.locator('.quiz-card').first().waitFor({state:'visible'});
  assert.equal(await page.locator('.sidebar').isVisible(),wide);
  await noOverflow(page,name+' quiz');await page.screenshot({path:'responsive-screenshots/'+name+'-quiz.png'});
  if(wide){await page.setViewportSize({width:390,height:844});assert.ok(await page.locator('.chat-panel').isVisible(),name+' resize preserves detail');assert.ok(!(await page.locator('.sidebar').isVisible()));}
  await page.close();
 }
 const uni=await browser.newPage({viewport:{width:1440,height:900}});await uni.goto(url);await uni.locator('#sample option[value=university]').waitFor({state:'attached'});await uni.locator('#sample').selectOption('university');await uni.locator('.module-nav .chat-item').first().waitFor({state:'visible'});assert.equal(await uni.locator('.module-nav .chat-item').count(),3);assert.ok(await uni.locator('.chat-item').filter({hasText:'Universiti Malaya'}).locator('img').evaluate(img=>img.complete&&img.naturalWidth===80));await uni.locator('.footer-nav button').nth(1).click();assert.equal(await uni.locator('.workspace-tools [data-module=Gate]').count(),0);await uni.close();
 const secondary=await browser.newPage({viewport:{width:390,height:844}});await secondary.goto(url);await secondary.locator('#sample option[value=child15]').waitFor({state:'attached'});await secondary.locator('#sample').selectOption('child15');const secondaryLogo=secondary.locator('.chat-item').filter({hasText:'SMK Cyberjaya'}).locator('img');await secondaryLogo.waitFor({state:'visible'});assert.ok(await secondaryLogo.evaluate(img=>img.complete&&img.naturalWidth===80));await secondary.close();
 console.log('Responsive smoke checks passed for '+sizes.length+' viewports');
}finally{await browser?.close();server.kill()}
