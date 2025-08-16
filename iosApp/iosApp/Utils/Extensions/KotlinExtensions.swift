
import Foundation
import Shared

extension Optional where Wrapped == KotlinLong {
    var int64: Int64? { self?.int64Value }
    var int: Int? { self?.intValue }
}
extension KotlinLong {
    var int64: Int64 { int64Value }
    var int: Int { intValue }
}

extension Optional where Wrapped == KotlinInt {
    var int: Int? { self?.intValue }
}
extension KotlinInt {
    var int: Int { intValue }
}

extension Optional where Wrapped == KotlinDouble {
    var doubleOrZero: Double { self?.doubleValue ?? 0 }
    var doubleOrNil: Double? { self?.doubleValue }
}
extension KotlinDouble {
    var double: Double { doubleValue }
}

extension Optional where Wrapped == KotlinFloat {
    var float: Float? { self?.floatValue }
}
extension KotlinFloat {
    var float: Float { floatValue }
}

extension Optional where Wrapped == KotlinBoolean {
    var bool: Bool? { self?.boolValue }
}
extension KotlinBoolean {
    var bool: Bool { boolValue }
}
