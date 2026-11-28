import Foundation
enum SchooldayNames {
    static func options(_ fullName:String)->[String] {
        let words=fullName.split(whereSeparator:{$0.isWhitespace}).map(String.init)
        var result=words
        for (index,word) in words.enumerated(){for next in words.dropFirst(index+1){result.append(word+" "+next)}}
        var seen=Set<String>()
        return result.filter{seen.insert($0).inserted}
    }
}
