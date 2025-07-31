// KotlinExtensions.swift
// Your iOSApp target

import Foundation
import Shared // Change 'Shared' if your KMP module is named differently

// MARK: - KotlinLong to Int64/Int
extension Optional where Wrapped == KotlinLong {
    var int64: Int64? { self?.int64Value }
    var int: Int? { self?.intValue }
}
extension KotlinLong {
    var int64: Int64 { int64Value }
    var int: Int { intValue }
}

// MARK: - KotlinInt to Int
extension Optional where Wrapped == KotlinInt {
    var int: Int? { self?.intValue }
}
extension KotlinInt {
    var int: Int { intValue }
}

// MARK: - KotlinDouble to Double
extension Optional where Wrapped == KotlinDouble {
    var doubleOrZero: Double { self?.doubleValue ?? 0 }
    var doubleOrNil: Double? { self?.doubleValue }
}
extension KotlinDouble {
    var double: Double { doubleValue }
}

// MARK: - KotlinFloat to Float
extension Optional where Wrapped == KotlinFloat {
    var float: Float? { self?.floatValue }
}
extension KotlinFloat {
    var float: Float { floatValue }
}

// MARK: - KotlinBoolean to Bool
extension Optional where Wrapped == KotlinBoolean {
    var bool: Bool? { self?.boolValue }
}
extension KotlinBoolean {
    var bool: Bool { boolValue }
}
