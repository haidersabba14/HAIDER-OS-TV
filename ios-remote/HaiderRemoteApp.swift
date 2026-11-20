import SwiftUI

@main
struct HaiderRemoteApp: App {
    var body: some Scene {
        WindowGroup { RemoteView() }
    }
}

struct RemoteView: View {
    @AppStorage("receiverIP") private var ip = "192.168.100.63"
    @AppStorage("receiverPin") private var pin = ""
    @State private var status = "أدخل عنوان التلفزيون ورمز الاقتران"
    @State private var busy = false
    @State private var showSettings = false

    private let bg = Color(red: 0.965, green: 0.947, blue: 0.91)
    private let ink = Color(red: 0.055, green: 0.063, blue: 0.083)
    private let gold = Color(red: 0.77, green: 0.61, blue: 0.34)

    var body: some View {
        ZStack {
            bg.ignoresSafeArea()
            ScrollView {
                VStack(spacing: 22) {
                    HStack {
                        VStack(alignment: .trailing, spacing: 3) {
                            Text("HAIDER").font(.system(size: 15, weight: .medium, design: .rounded)).tracking(5)
                            Text("REMOTE").font(.system(size: 32, weight: .black, design: .rounded))
                        }
                        Spacer()
                        Button { showSettings = true } label: {
                            Image(systemName: "gearshape").font(.system(size: 21))
                                .frame(width: 51, height: 51)
                                .background(ink, in: RoundedRectangle(cornerRadius: 16))
                                .foregroundColor(gold)
                        }
                    }
                    .foregroundColor(ink)

                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Circle().fill(status == "تم إرسال الأمر" ? .green : gold).frame(width: 9, height: 9)
                            Text(status).font(.caption).lineLimit(2)
                        }
                        Text("Receiver: \(ip) · Port 8765")
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    HStack(spacing: 12) {
                        remoteButton("house.fill", "HOME", label: "الرئيسية")
                        remoteButton("arrow.uturn.backward", "BACK", label: "رجوع")
                    }

                    VStack(spacing: 7) {
                        remoteButton("chevron.up", "UP", label: "أعلى", wide: true)
                        HStack(spacing: 7) {
                            remoteButton("chevron.left", "LEFT", label: "يسار", wide: true)
                            Button { send("OK") } label: {
                                Text("OK").font(.system(size: 23, weight: .bold, design: .rounded))
                                    .frame(maxWidth: .infinity).frame(height: 82)
                                    .background(LinearGradient(colors: [Color(red: 0.96, green: 0.82, blue: 0.52), gold], startPoint: .topLeading, endPoint: .bottomTrailing), in: RoundedRectangle(cornerRadius: 23))
                                    .foregroundColor(ink)
                            }
                            remoteButton("chevron.right", "RIGHT", label: "يمين", wide: true)
                        }
                        remoteButton("chevron.down", "DOWN", label: "أسفل", wide: true)
                    }
                    .padding(15)
                    .background(ink, in: RoundedRectangle(cornerRadius: 32))

                    HStack(spacing: 12) {
                        remoteButton("speaker.plus.fill", "VOLUME_UP", label: "الصوت +")
                        remoteButton("speaker.minus.fill", "VOLUME_DOWN", label: "الصوت −")
                    }

                    Text("اتصل بنفس شبكة Wi-Fi الخاصة بتلفزيون Haier. يعمل الريموت داخل HAIDER OS TV عند تفعيل Phone Remote.")
                        .font(.footnote).multilineTextAlignment(.center)
                        .foregroundColor(.secondary).padding(.top, 8)
                }
                .padding(22)
            }
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                Form {
                    Section("الاتصال بالرسيفر / التلفزيون") {
                        TextField("TV IP", text: $ip)
                            .keyboardType(.numbersAndPunctuation)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                        SecureField("رمز الاقتران: 6 أرقام", text: $pin)
                            .keyboardType(.numberPad)
                        Button("اختبار الاتصال") { send("HOME") }
                        Text("افتح HAIDER OS TV → Phone Remote على التلفزيون ثم شغّل Remote واكتب عنوان IP ورمز الاقتران المعروض.")
                            .font(.footnote)
                    }
                    Section {
                        Text("المنفذ 8765 · الاتصال عبر الشبكة المحلية فقط · رمز الاقتران لا يمنح وصولًا إلى ملفات النظام")
                            .font(.footnote)
                    }
                }
                .navigationTitle("ربط التلفزيون")
                .toolbar { ToolbarItem(placement: .confirmationAction) { Button("تم") { showSettings = false } } }
            }
        }
    }

    @ViewBuilder
    private func remoteButton(_ icon: String, _ command: String, label: String, wide: Bool = false) -> some View {
        Button { send(command) } label: {
            VStack(spacing: 5) {
                Image(systemName: icon).font(.system(size: wide ? 25 : 22, weight: .semibold))
                if !wide { Text(label).font(.system(size: 11, weight: .medium)) }
            }
            .frame(maxWidth: .infinity)
            .frame(height: wide ? 74 : 78)
            .foregroundColor(gold)
            .background(ink, in: RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
    }

    private func send(_ command: String) {
        let cleanIP = ip.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanPIN = pin.trimmingCharacters(in: .whitespacesAndNewlines)
        guard cleanPIN.count == 6, cleanPIN.allSatisfy({ $0.isNumber }) else {
            status = "أدخل رمز الاقتران المكوّن من 6 أرقام"
            showSettings = true
            return
        }
        guard cleanIP.range(of: #"^[0-9]{1,3}(\.[0-9]{1,3}){3}$"#, options: .regularExpression) != nil,
              let url = URL(string: "http://\(cleanIP):8765/command") else {
            status = "عنوان IP غير صالح"
            showSettings = true
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.timeoutInterval = 4
        request.setValue(cleanPIN, forHTTPHeaderField: "X-Haider-Token")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try? JSONSerialization.data(withJSONObject: ["key": command])
        status = "جاري الإرسال..."
        URLSession.shared.dataTask(with: request) { _, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    status = "تعذّر الاتصال: \(error.localizedDescription)"
                } else if let code = (response as? HTTPURLResponse)?.statusCode, code == 200 {
                    status = "تم إرسال الأمر"
                } else {
                    status = "فشل الاقتران. تحقق من الرمز والشبكة."
                }
            }
        }.resume()
    }
}
