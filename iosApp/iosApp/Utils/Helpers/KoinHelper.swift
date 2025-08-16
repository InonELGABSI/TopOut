import Foundation
import Shared

@inline(__always)
func get<T: AnyObject>(_ type: T.Type = T.self) -> T {
    return Shared.sharedKoin.get(objCClass: type) as! T
}

func get<T: AnyObject>(_ type: T.Type,
                       qualifier: Koin_coreQualifier? = nil,
                       parameter: Any) -> T {
    return Shared.sharedKoin.get(
        objCClass: type,
        qualifier: qualifier,
        parameter: parameter
    ) as! T
}

