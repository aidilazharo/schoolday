import sk from './assets/group_sk.jpg';
import smk from './assets/group_smk.jpg';
import um from './assets/group_um.jpg';
const images:Record<string,string>={sk,smk,um};
const keys:Record<string,string>={"school":"sk","class":"sk","staff":"sk","admin":"sk","secondary":"smk","university":"um","uni-course":"um"};
const names:Record<string,string>={"school":"SK Cyberjaya","secondary":"SMK Cyberjaya","university":"Universiti Malaya"};
export function groupTitle(id:string,fallback:string){return names[id]??fallback}
export function GroupAvatar({channel,title,size=44}:{channel:string;title:string;size?:number}){
 const key=keys[channel],src=images[key];
 return <span className={'group-avatar'+(src?' institution-logo':'')} style={{width:size,height:size}} aria-hidden="true">{src?<img src={src} alt="" width={size} height={size}/>:title.charAt(0).toUpperCase()}</span>
}
