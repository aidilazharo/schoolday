import XCTest
@testable import Schoolday
final class NameTests:XCTestCase {
    func testOptions(){
        XCTAssertEqual(SchooldayNames.options("Muhammad Aiman Hakim"),["Muhammad","Aiman","Hakim","Muhammad Aiman","Muhammad Hakim","Aiman Hakim"])
        XCTAssertEqual(SchooldayNames.options("王小明"),["王小明"])
        XCTAssertFalse(SchooldayNames.options("Nur Sara Amira").contains("Sara Queen"))
    }
}
