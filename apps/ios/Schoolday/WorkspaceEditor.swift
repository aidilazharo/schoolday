import SwiftUI
struct WorkspaceEditor:View {
    let db:LocalDatabase
    let user:Account
    let kind:String
    let lang:String
    var onSaved:()->Void = {}
    @State private var title=""
    @State private var details=""
    @State private var drafts:[[String]]=[]
    @State private var failed=false
    private func t(_ key:String)->String{SchooldayLanguage.text(key,lang)}
    private var heading:String{kind=="Quiz" ? "Quiz editor":kind=="Certificates" ? "Certificate studio":"Event planner"}
    private var hint:String{kind=="Quiz" ? "Question and answer choices":kind=="Certificates" ? "Recipient and achievement":"Date, time and location"}
    var body:some View{
        Form{
            Section(t(heading)){
                Text(t("Draft only · Not published")).font(.caption)
                TextField(t("Title"),text:$title)
                TextField(t(hint),text:$details,axis:.vertical).lineLimit(4...10)
                Button(t("Save draft"),action:save).disabled(title.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || details.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || title.count>120 || details.count>4000)
            }
        }.task(id:kind){reload()}.alert(t("Unable to save locally."),isPresented:$failed){Button(t("OK"),role:.cancel){}}
    }
    private func reload(){do{drafts=try db.query("SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id",[user.id,kind])}catch{failed=true}}
    private func save(){do{_=try db.query("INSERT INTO workspace_drafts VALUES(?,?,?,?,?,?)",[UUID().uuidString,user.id,kind,title.trimmingCharacters(in:.whitespacesAndNewlines),details.trimmingCharacters(in:.whitespacesAndNewlines),String(Int64(Date().timeIntervalSince1970*1000))]);title="";details="";reload();onSaved()}catch{failed=true}}
}
