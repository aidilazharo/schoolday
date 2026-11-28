// Preserve spelling, case and original order; do not invent names.
export function nicknameOptions(fullName:string):string[]{
 const words=fullName.trim().split(/\s+/u).filter(Boolean);
 return [...new Set([...words,...words.flatMap((word,index)=>words.slice(index+1).map(next=>word+' '+next))])];
}
