import SwiftUI
import CoreImage
import CoreImage.CIFilterBuiltins
import UIKit

private enum FeatureQuery {
    static let children = "SELECT u.id,u.name,d.age,p.balance,COALESCE(q.enabled,0) FROM family_links f JOIN users u ON u.id=f.child_id JOIN demographics d ON d.user_id=u.id JOIN points_accounts p ON p.user_id=u.id LEFT JOIN quiz_permissions q ON q.child_id=u.id JOIN roles r ON r.user_id=f.parent_id AND r.role='Parent' WHERE f.parent_id=? ORDER BY d.age"
    static let parents = "SELECT u.name FROM family_links f JOIN users u ON u.id=f.parent_id WHERE f.child_id=? ORDER BY u.name"
    static let quiz = "SELECT q.id,q.prompt,q.a,q.b,q.c,q.d,COALESCE(t.status,''),COALESCE(p.enabled,0) FROM demographics d JOIN quiz_questions q ON d.age BETWEEN q.min_age AND q.max_age LEFT JOIN quiz_attempts t ON t.user_id=d.user_id AND t.question_id=q.id LEFT JOIN quiz_permissions p ON p.child_id=d.user_id WHERE d.user_id=? ORDER BY q.id"
    static let activity = "SELECT reason,delta FROM point_activity WHERE user_id=? ORDER BY created_at DESC,id DESC"
    static let requests = "SELECT r.actor,r.target,r.kind,r.status,u.name FROM identity_requests r JOIN users u ON u.id=CASE WHEN r.actor=? THEN r.target ELSE r.actor END WHERE r.actor=? OR r.target=?"
    static let avatar = "SELECT style FROM avatars WHERE user_id=?"
    static let token = "SELECT token FROM identity_tokens WHERE user_id=?"
    static let age = "SELECT age FROM demographics WHERE user_id=?"
    static let resolve = "SELECT user_id FROM identity_tokens WHERE token=?"
}
struct FamilyFeatures:View {
    let db:LocalDatabase
    let user:Account
    let screen:String
    var onChange:()->Void = {}
    @State private var revision=0
    @State private var choice="star"
    @State private var child=""
    @State private var amount="10"
    @State private var code=""
    @State private var purpose="friend"
    @State private var feedback=""
    @State private var pending:String?
    @State private var confirmAction:(()->Void)?
    private let styles=["initials","star","leaf","book","cat","rocket"]
    private let symbols=["person.crop.circle","star","leaf","book","cat","paperplane"]
    private func rows(_ sql:String,_ args:[String]?=nil)->[[String]] {
        _=revision
        return (try? db.query(sql,args ?? [user.id])) ?? []
    }
    private var balance:Int{_ = revision; return (try? db.points(user)) ?? 0}
    private var token:String{rows(FeatureQuery.token).first?.first ?? ""}
    private var payload:String{"schoolday:identity:v1:"+token}
    private var current:String{rows(FeatureQuery.avatar).first?.first ?? "initials"}
    private var kids:[[String]]{rows(FeatureQuery.children)}
    private var age:Int{Int(rows(FeatureQuery.age).first?.first ?? "") ?? 0}
    private func act(_ kind:String,_ target:String="",_ amount:Int=0,_ value:String=""){
        do{
            _=try db.query("INSERT INTO feature_actions VALUES(?,?,?,?,?,?,?)",[UUID().uuidString,user.id,kind,target,String(amount),value,String(Int64(Date().timeIntervalSince1970*1000))])
            revision+=1;onChange();feedback="Saved locally."
        }catch{feedback="Action rejected. Check permissions, balance or answer."}
    }
    private func ask(_ message:String,_ action:@escaping ()->Void){pending=message;confirmAction=action}
    private func qrImage()->UIImage?{
        guard !token.isEmpty else{return nil}
        let filter=CIFilter.qrCodeGenerator()
        filter.message=Data(payload.utf8)
        filter.correctionLevel="M"
        guard let output=filter.outputImage?.transformed(by:CGAffineTransform(scaleX:8,y:8)),let image=CIContext().createCGImage(output,from:output.extent) else{return nil}
        return UIImage(cgImage:image)
    }
    private func printQR(){
        guard let image=qrImage() else{return}
        let renderer=UIGraphicsImageRenderer(size:CGSize(width:image.size.width+64,height:image.size.height+64))
        let padded=renderer.image{ context in UIColor.white.setFill();context.fill(CGRect(origin:.zero,size:CGSize(width:image.size.width+64,height:image.size.height+64)));image.draw(at:CGPoint(x:32,y:32))}
        let controller=UIPrintInteractionController.shared
        controller.printingItem=padded
        controller.present(animated:true,completionHandler:nil)
    }
    private func useCode(){
        let prefix="schoolday:identity:v1:"
        let entered=code.trimmingCharacters(in:.whitespacesAndNewlines)
        guard entered.hasPrefix(prefix) else{feedback="Invalid identity";return}
        let value=String(entered.dropFirst(prefix.count))
        guard let target=rows(FeatureQuery.resolve,[value]).first?.first else{feedback="Unknown identity";return}
        act(purpose,target,0,value)
    }
    var body:some View{
        Group{
            if screen=="Rewards"{rewards}
            if screen=="Profile"{profile}
            if screen=="Quiz"{quiz}
            if !feedback.isEmpty{Text(feedback).font(.caption)}
        }.confirmationDialog(pending ?? "",isPresented:Binding(get:{pending != nil},set:{if !$0{pending=nil}}),titleVisibility:.visible){
            Button("Confirm"){let action=confirmAction;confirmAction=nil;pending=nil;action?()}
            Button("Cancel",role:.cancel){confirmAction=nil;pending=nil}
        }
    }
    private var rewards:some View{
        Group{
            Section("Points balance"){Text(String(balance)).font(.largeTitle.bold())}
            if age>=18{Section("Buy points"){
                Text("Demo only · No real payment").font(.caption)
                ForEach([100,200,500],id:\.self){n in Button("\(n) points · RM \(n/10)"){ask("Simulate purchase?"){act("buy","",n)}}}
            }}
            if !kids.isEmpty{Section("Transfer to children"){
                Picker("Child",selection:$child){Text("Select child").tag("");ForEach(kids,id:\.self){c in Text(c[1]+" · "+c[3]+" points").tag(c[0])}}
                TextField("Points",text:$amount).keyboardType(.numberPad)
                Button("Transfer points"){if let n=Int(amount){ask("Transfer \(n) points?"){act("transfer",child,n)}}}.disabled(child.isEmpty || (Int(amount) ?? 0)<1 || (Int(amount) ?? 0)>balance)
            }}
            Section("Activity"){ForEach(Array(rows(FeatureQuery.activity).enumerated()),id:\.offset){item in HStack{Text(item.element[0]);Spacer();Text(item.element[1])}}}
        }
    }
    private var profile:some View{
        Group{
            Section("Avatar"){
                Image(systemName:symbols[styles.firstIndex(of:current) ?? 0]).font(.system(size:44))
                Picker("Avatar",selection:$choice){ForEach(styles,id:\.self){style in Label(style,systemImage:symbols[styles.firstIndex(of:style) ?? 0]).tag(style)}}
                Text("10 points will be deducted. Balance: \(balance)").font(.caption)
                Button("Save avatar · 10 points"){ask("Deduct 10 points?"){act("avatar","",0,choice)}}.disabled(choice==current || balance<10)
            }
            Section("QR identity"){
                if let image=qrImage(){Image(uiImage:image).interpolation(.none).resizable().scaledToFit().frame(maxWidth:240,maxHeight:240).padding(12).background(Color.white)}
                Text("Local demo identifier, not proof of identity.").font(.caption)
                Button("Print QR",action:printQR)
                Button("Copy identity code"){UIPasteboard.general.string=payload}
                Picker("Use identity",selection:$purpose){Text("Friend request").tag("friend");Text("Parent link request").tag("parent");Text("Demo attendance").tag("attendance")}
                TextField("Paste scanned QR identity",text:$code).textInputAutocapitalization(.never).autocorrectionDisabled()
                Button("Continue",action:useCode).disabled(code.isEmpty)
                Text("Parent requests require verification. Staff-only attendance is a demo.").font(.caption)
                ForEach(rows(FeatureQuery.requests,[user.id,user.id,user.id]),id:\.self){r in
                    VStack(alignment:.leading){Text(r[4]+" · "+r[2]+" · "+r[3]);if r[1]==user.id && r[2]=="friend" && r[3]=="pending"{Button("Accept"){act("accept",r[0])}}}
                }
            }
            if !kids.isEmpty{Section("Linked children"){
                ForEach(kids,id:\.self){c in VStack(alignment:.leading){Text(c[1]).font(.headline);Text("Age "+c[2]+" · "+c[3]+" points").font(.caption);Toggle("Allow quiz skip · 5 points",isOn:Binding(get:{c[4]=="1"},set:{act("permission",c[0],$0 ? 1:0)}))}}
            }}
            if !rows(FeatureQuery.parents).isEmpty{Section("Linked parents"){ForEach(rows(FeatureQuery.parents),id:\.self){p in Text(p[0])}}}
        }
    }
    private var quiz:some View{
        Section("Quiz · age \(age)"){
            ForEach(rows(FeatureQuery.quiz),id:\.self){q in VStack(alignment:.leading,spacing:12){
                Text(q[1]).font(.headline)
                if !q[6].isEmpty{Text(q[6]=="correct" ? "Completed":"Skipped")}
                else{
                    ForEach(0..<4,id:\.self){j in Button(q[j+2]){act("answer",q[0],j)}.buttonStyle(.bordered)}
                    if q[7]=="1"{Button("Skip · 5 points"){ask("Deduct 5 points?"){act("skip",q[0])}}.disabled(balance<5)}
                    else{Text("Quiz skip not enabled by parent.").font(.caption)}
                }
            }}
        }
    }
}
