import SwiftUI
import Foundation

enum SchooldayCountry {
    static let codes = "AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW".components(separatedBy:" ")
    static func name(_ code:String,_ lang:String)->String { Locale(identifier:lang=="zh" ? "zh_Hans":lang).localizedString(forRegionCode:code) ?? code }
}
struct CountryMenu: View {
    @Binding var country:String
    let language:String
    @State private var presented=false
    @State private var search=""
    private var filtered:[String] {
        let locale=Locale(identifier:language)
        return SchooldayCountry.codes.filter{code in search.isEmpty || code.localizedCaseInsensitiveContains(search) || SchooldayCountry.name(code,language).localizedCaseInsensitiveContains(search)}.sorted {SchooldayCountry.name($0,language).compare(SchooldayCountry.name($1,language),options:[],range:nil,locale:locale) == .orderedAscending}
    }
    var body:some View {
        Button {search="";presented=true} label:{
            Label(SchooldayCountry.name(country,language),systemImage:"mappin.and.ellipse")
        }.accessibilityLabel(SchooldayLanguage.text("Country / region",language))
        .sheet(isPresented:$presented){
            NavigationStack {
                List(filtered,id: \.self){code in
                    Button {country=code;presented=false} label:{
                        HStack {Text(SchooldayCountry.name(code,language));Spacer();Text(code).foregroundStyle(.secondary);if country==code{Image(systemName:"checkmark")}}
                    }
                }.searchable(text:$search,prompt:SchooldayLanguage.text("Search country / region",language))
                    .navigationTitle(SchooldayLanguage.text("Country / region",language))
                    .toolbar {ToolbarItem(placement:.confirmationAction){Button(SchooldayLanguage.text("Done",language)){presented=false}}}
            }
        }
    }
}
