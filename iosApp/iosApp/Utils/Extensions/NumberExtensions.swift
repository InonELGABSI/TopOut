import Foundation

extension Double {
    func elevationFormat() -> String {
        return String(format: "%.1f m", self)
    }
    
    func distanceFormat() -> String {
        if self >= 1000 {
            return String(format: "%.2f km", self / 1000)
        } else {
            return String(format: "%.0f m", self)
        }
    }
    
    func speedFormat() -> String {
        return String(format: "%.1f m/s", self)
    }
    
    func durationFormat() -> String {
        let minutes = Int(self) / 60
        let seconds = Int(self) % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
    
    func percentageFormat() -> String {
        return String(format: "%.1f%%", self * 100)
    }
}

extension Float {
    func elevationFormat() -> String {
        return String(format: "%.1f m", self)
    }
    
    func distanceFormat() -> String {
        if self >= 1000 {
            return String(format: "%.2f km", self / 1000)
        } else {
            return String(format: "%.0f m", self)
        }
    }
    
    func speedFormat() -> String {
        return String(format: "%.1f m/s", self)
    }
}

extension Int {
    func metersFormat() -> String {
        return "\(self) m"
    }
    
    func timeFormat() -> String {
        let hours = self / 3600
        let minutes = (self % 3600) / 60
        let seconds = self % 60
        
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String(format: "%02d:%02d", minutes, seconds)
        }
    }
}

