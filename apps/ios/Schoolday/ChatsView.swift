import SwiftUI

struct LocalDraft: Codable, Identifiable {
    let id: UUID
    let text: String
    let createdAt: Date
}

struct ChatsView: View {
    @AppStorage("schoolday.development.outbox") private var stored = Data()
    @State private var text = ""
    @State private var confirmClear = false
    @State private var error = false
    @State private var opened = false
    private var drafts: [LocalDraft] {
        (try? JSONDecoder().decode([LocalDraft].self, from: stored)) ?? []
    }
    var body: some View {
        GeometryReader { geometry in
            let wide = geometry.size.width >= 768
            HStack(spacing: 0) {
                if wide || !opened {
                    sidebar.frame(width: wide ? min(geometry.size.width * 0.32, 420) : geometry.size.width)
                }
                if wide { Divider() }
                if wide || opened { conversation(wide: wide) }
            }
        }
        .tint(.primary)
        .confirmationDialog("Clear local drafts?", isPresented: $confirmClear) {
            Button("Clear", role: .destructive) { stored = Data() }
        }
        .alert("Unable to save draft", isPresented: $error) { Button("OK", role: .cancel) {} }
    }
    private var sidebar: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text("schoolday").font(.largeTitle.bold())
                Spacer()
                Text("Local only").font(.caption).foregroundStyle(.secondary)
            }.padding()
            Divider()
            Text("Chats").font(.title2.bold()).padding()
            Button { opened = true } label: {
                HStack(spacing: 12) {
                    Text("S").font(.headline).frame(width: 44, height: 44).background(.thinMaterial, in: Circle())
                    VStack(alignment: .leading) {
                        Text("Local outbox").font(.headline)
                        Text(drafts.last?.text ?? "No saved messages").font(.caption).foregroundStyle(.secondary).lineLimit(1)
                    }
                    Spacer()
                    Image(systemName: "chevron.right").font(.caption)
                }.padding().contentShape(Rectangle())
            }.buttonStyle(.plain)
            Divider()
            Spacer(minLength: 0)
        }.frame(maxHeight: .infinity)
    }
    private func conversation(wide: Bool) -> some View {
        VStack(spacing: 0) {
            HStack {
                VStack(alignment: .leading) {
                    Text("Local outbox").font(.headline)
                    Text("Local only").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                Button("Clear") { confirmClear = true }.disabled(drafts.isEmpty)
                if !wide { Button("Back") { opened = false } }
            }.padding()
            Divider()
            if drafts.isEmpty {
                Spacer()
                Text("No saved messages").font(.headline)
                Text("Save a local test message below.").foregroundStyle(.secondary)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(alignment: .trailing, spacing: 12) {
                        ForEach(drafts) { draft in
                            VStack(alignment: .trailing, spacing: 6) {
                                Text(draft.text)
                                Text("Saved locally · Not sent").font(.caption).foregroundStyle(.secondary)
                            }.padding().background(.thinMaterial, in: RoundedRectangle(cornerRadius: 14))
                            .frame(maxWidth: .infinity, alignment: .trailing)
                        }
                    }.padding()
                }
            }
            Divider()
            HStack {
                TextField("Message", text: $text)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: text) { _, value in if value.count > 500 { text = String(value.prefix(500)) } }
                Button("Save") { save() }.buttonStyle(.borderedProminent)
                    .disabled(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }.padding()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    private func save() {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        do {
            stored = try JSONEncoder().encode(drafts + [LocalDraft(id: UUID(), text: trimmed, createdAt: Date())])
            text = ""
        } catch { self.error = true }
    }
}
