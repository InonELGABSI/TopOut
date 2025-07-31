import Foundation
import Shared // Replace with your module if needed

// MARK: - KotlinLong to Int64
extension Optional where Wrapped == KotlinLong {
    var int64: Int64? {
        self?.int64Value
    }
}

extension KotlinLong {
    var int64: Int64 { int64Value }
}

// MARK: - KotlinDouble to Double
extension Optional where Wrapped == KotlinDouble {
    var double: Double? {
        self?.doubleValue
    }
}

extension KotlinDouble {
    var double: Double { doubleValue }
}

// MARK: - KotlinInt to Int
extension Optional where Wrapped == KotlinInt {
    var int: Int? {
        self?.intValue
    }
}

// Add more as needed...
