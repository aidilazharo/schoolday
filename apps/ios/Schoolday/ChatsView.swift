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
    private var drafts: [LocalDraft] {
        (try? JSONDecoder().decode([LocalDraft].self, from: stored)) ?? []
    }
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("schoolday").font(.largeTitle.bold())
                Spacer()
                Text("Local only").font(.caption).foregroundStyle(.secondary)
            }.padding()
            Divider()
            HStack {
                Text("Chats").font(.title2.bold())
                Spacer()
                Button("Clear") { confirmClear = true }.disabled(drafts.isEmpty)
            }.padding()
            if drafts.isEmpty {
                Spacer()
                Text("No conversations yet").font(.headline)
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
        .frame(maxWidth: 760)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .tint(.primary)
        .confirmationDialog("Clear local drafts?", isPresented: $confirmClear) {
            Button("Clear", role: .destructive) { stored = Data() }
        }
        .alert("Unable to save draft", isPresented: $error) { Button("OK", role: .cancel) {} }
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
