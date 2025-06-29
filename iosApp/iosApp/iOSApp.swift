import SwiftUI
import Shared
import Firebase

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        FirebaseApp.configure()
        
        return true
    }
}


@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            TabView {
                
//                MoviesScreen(viewModel: .init()).tabItem {
//                    Label("Movies", systemImage: "film")
//                }
//                
//                FavouritesMoviesScreen(viewModel: .init()).tabItem {
//                    Label("Favorites", systemImage: "heart")
//                }
            }
        }
    }
}
