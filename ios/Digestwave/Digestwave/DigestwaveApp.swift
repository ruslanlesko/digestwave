//
//  DigestwaveApp.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 02.03.2023.
//

import SwiftUI

@main
struct DigestwaveApp: App {
    let persistenceController = PersistenceController.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}
