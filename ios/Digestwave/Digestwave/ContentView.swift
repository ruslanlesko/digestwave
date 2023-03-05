//
//  ContentView.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 02.03.2023.
//

import SwiftUI

let intTopics = ["all", "technology", "finance", "programming"]
let uaTopics = ["all", "technology", "finance", "football"]

struct ContentView: View {
    @State private var edition: Edition = .international
    @State private var topics: [String] = intTopics
    @State private var articles: [Article] = [
        Article(title: "Hello, World!", site: "lol.kek"),
        Article(title: "New article.", site: "kek.lol")
    ]
    
    @State private var topic: String? = "all"
    @State private var selectedArticle: Article?
    
    @State private var columnVisibility =
        NavigationSplitViewVisibility.automatic
    
    var body: some View {
        NavigationSplitView(columnVisibility: $columnVisibility) {
            List(selection: $topic) {
                Picker("edition", selection: $edition) {
                    Text("international").tag(Edition.international)
                    Text("ukrainian").tag(Edition.ukrainian)
                }.onChange(of: edition, perform: { value in
                    switch value {
                    case .international: topics = intTopics
                    case .ukrainian: topics = uaTopics
                    }
                })
                
                Section(header: Text("topic")) {
                    ForEach(topics, id: \.self) {t in
                        NavigationLink(value: t) {
                            Text(LocalizedStringKey(t))
                        }
                    }
                }
            }
            .listStyle(SidebarListStyle())
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
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
