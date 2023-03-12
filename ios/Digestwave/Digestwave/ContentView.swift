//
//  ContentView.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 02.03.2023.
//

import SwiftUI
import CoreData

let intTopics = ["all", "technology", "finance", "programming"]
let uaTopics = ["all", "technology", "finance", "football"]

struct ContentView: View {
    @Environment(\.managedObjectContext) private var viewContext
    
    @FetchRequest(entity: Edition.entity(), sortDescriptors: [])
    var editions: FetchedResults<Edition>
    
    @State private var edition: EditionOpt = .international
    @State private var topics: [String] = intTopics
    @State private var articles: [Article] = [
        Article(title: "Hello, World!", site: "lol.kek"),
        Article(title: "New article.", site: "kek.lol")
    ]
    
    @State private var topic: String? = "all"
    @State private var selectedArticle: Article?
    
    #if os(iOS)
    @State private var columnVisibility =
    NavigationSplitViewVisibility.doubleColumn
    #else
    @State private var columnVisibility =
    NavigationSplitViewVisibility.all
    #endif
    
    var body: some View {
        DispatchQueue.main.async {
            initEdition()
        }
        
        return NavigationSplitView(columnVisibility: $columnVisibility) {
            List(selection: $topic) {
                Picker("edition", selection: $edition) {
                    Text("international").tag(EditionOpt.international)
                    Text("ukrainian").tag(EditionOpt.ukrainian)
                }.onChange(of: edition, perform: { value in
                    switch value {
                    case .international: topics = intTopics
                    case .ukrainian: topics = uaTopics
                    }
                    persistEdition(editionOpt: value)
                })
                
                Section(header: Text("topic")) {
                    ForEach(topics, id: \.self) {t in
                        NavigationLink(value: t) {
                            Image(systemName: topicIcon(topicName: t))
                            Text(LocalizedStringKey(t))
                        }
                    }
                }
            }
            .listStyle(SidebarListStyle())
            #if os(macOS)
            .navigationSplitViewColumnWidth(min: 192, ideal: 224, max: 288)
            #endif
            .navigationTitle(Text("topics"))
        } content: {
            List(articles, id: \.self, selection: $selectedArticle) { article in
                NavigationLink(value: article) {
                    Text(article.title)
                }
            }
            .navigationTitle((topic == nil || topic == "all" ? Text("news") : Text(LocalizedStringKey(topic!))))
        } detail: {
            ArticleView()
        }
        .navigationSplitViewStyle(.balanced)
    }
    
    private func initEdition() {
        if !editions.isEmpty {
            switch editions[0].value {
            case "ua":
                edition = EditionOpt.ukrainian
            default:
                edition = EditionOpt.international
            }
        }
    }
    
    private func persistEdition(editionOpt: EditionOpt) {
        var editionStr: String;
        switch editionOpt {
        case .international: editionStr = "int"
        case .ukrainian: editionStr = "ua"
        }
        if editions.isEmpty {
            let newEdition = Edition(context: viewContext)
            newEdition.value = editionStr
        } else {
            editions[0].value = editionStr
        }
        do { try viewContext.save() }
        catch { print(error.localizedDescription) }
    }
    
    private func topicIcon(topicName: String) -> String {
        switch topicName {
        case "finance": return "dollarsign.circle"
        case "football": return "soccerball"
        case "technology": return "cpu"
        case "programming": return "curlybraces"
        default: return "newspaper"
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
    }
}
