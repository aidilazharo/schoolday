import SwiftUI
enum GroupBrand{
 static let images:[String:String]=["school":"group_sk","class":"group_sk","staff":"group_sk","admin":"group_sk","secondary":"group_smk","university":"group_um","uni-course":"group_um"]
 static let names:[String:String]=["school":"SK Cyberjaya","secondary":"SMK Cyberjaya","university":"Universiti Malaya"]
 static func title(_ id:String,_ fallback:String)->String{names[id] ?? fallback}
}
struct GroupAvatar:View{
 let channel:String;let title:String;var size:CGFloat=44
 var body:some View{
  ZStack{
   RoundedRectangle(cornerRadius:size*0.22).fill(GroupBrand.images[channel]==nil ? Color.primary.opacity(0.06):Color.white)
   if let image=GroupBrand.images[channel]{Image(image).resizable().scaledToFit().padding(2)}
   else{Text(String(title.prefix(1)).uppercased()).font(.system(size:size*0.40,weight:.semibold))}
  }.frame(width:size,height:size).clipShape(RoundedRectangle(cornerRadius:size*0.22)).accessibilityHidden(true)
 }
}
