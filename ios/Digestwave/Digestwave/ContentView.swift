//
//  ContentView.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 02.03.2023.
//

import SwiftUI
import CoreData
import Combine
import Foundation

let intTopics = ["all", "tech", "finance", "programming"]
let uaTopics = ["all", "tech", "finance", "football"]

struct ContentView: View {
    @Environment(\.managedObjectContext) private var viewContext
    
    @State private var cancellable: AnyCancellable?
    
    @FetchRequest(entity: Edition.entity(), sortDescriptors: [])
    var editions: FetchedResults<Edition>
    
    @State private var edition: EditionOpt = .international
    @State private var topics: [String] = intTopics
    @State private var articlePreviews: [ArticlePreview] = []
    @State private var topic: String? = "all"
    @State private var selectedArticle: ArticlePreview?
    @State private var loading = false
    @State private var page = 1
    @State private var errorLoading = false
    
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
            if self.loading || self.page > 1 {
                return
            }
            fetchPreviewArticles()
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
            .onChange(of: topic, perform: { value in
                if topic != nil {
                    DispatchQueue.main.async { fetchPreviewArticles() }
                }
            })
            .listStyle(SidebarListStyle())
            #if os(macOS)
            .navigationSplitViewColumnWidth(min: 192, ideal: 224, max: 288)
            #endif
            .navigationTitle(Text("topics"))
        } content: {
            if (articlePreviews.isEmpty && errorLoading) {
                Image(systemName: "icloud.slash")
                    .resizable()
                    .frame(width: 56, height: 46)
                Text("failure_loading")
            }
            List(articlePreviews, id: \.id, selection: $selectedArticle) { article in
                NavigationLink(value: article) {
                    HStack {
                        VStack {
                            HStack {
                                Text(formatTitle(title: article.title))
                                Spacer()
                            }
                            #if os(iOS)
                            .padding([.top], 2)
                            #else
                            .padding([.bottom], 12)
                            #endif
                            Spacer(minLength: 1)
                            HStack {
                                Text(article.site)
                                Text("•")
                                Text(LocalizedStringKey(dateToString(timestamp: article.publicationTime)))
                                Spacer()
                            }.font(.system(size: 14).weight(.light))
                                .foregroundColor(.secondary)
                            #if os(iOS)
                            .padding([.bottom], 2)
                            #else
                            .padding([.bottom], 12)
                            #endif
                        }
                        Spacer()
                        if article.hasCoverImage {
                            AsyncImage(url: URL(string: "https://api.leskor.com/digestwave/v1/articles/\(article.id)/image")) {image in
                                image.resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                EmptyView()
                            }
                            .frame(width: 104, height: 74)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                }
                .listRowSeparator(.visible)
                .onAppear {
                    handleAppear(articlePreview: article)
                }
            }
            .refreshable {
                doRefresh()
            }
            #if os(macOS)
            .navigationSplitViewColumnWidth(min: 340, ideal: 400, max: 450)
            .toolbar {
                ToolbarItem(placement: .navigation) {
                    Button(
                        action: doRefresh,
                        label: {
                            Image(systemName: "gobackward")
                        }
                    )
                }
            }
            #endif
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
        let editionStr = editionOptToString(editionOpt: editionOpt)
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
        case "tech": return "cpu"
        case "programming": return "curlybraces"
        default: return "newspaper"
        }
    }
    
    private func fetchPreviewArticles(page: Int = 1) {
        self.loading = true
        self.page = page
        self.cancellable = DigestwaveAPI.articlePreviews(
            region: self.editionOptToString(editionOpt: self.edition),
            topic: self.topic == "all" ? nil : self.topic,
            page: page
        ).sink(receiveCompletion: { completion in
            self.loading = false
            switch completion {
                case let .failure(error):
                    print("Couldn't fetch articles preview: \(error)")
                    errorLoading = true
                case .finished: break
            }
        }) { response in
            self.loading = false
            if response.status == 200 {
                self.errorLoading = false
                if page == 1 {
                    self.articlePreviews = response.value!
                } else {
                    self.articlePreviews = self.articlePreviews + response.value!
                }
            }
            else {
                print("Error making request to article previews")
                errorLoading = true
            }
        }
    }
    
    private func doRefresh() {
        if loading {
            return
        }
        self.articlePreviews = []
        DispatchQueue.main.async { fetchPreviewArticles() }
    }
    
    private func editionOptToString(editionOpt: EditionOpt) -> String {
        switch editionOpt {
        case .international: return "int"
        case .ukrainian: return "ua"
        }
    }
    
    private func formatTitle(title: String) -> String {
        if title.count < 76 {
            return title
        }
        
        let index = title.index(title.startIndex, offsetBy: 66)
        return String(title[..<index]) + "..."
    }
    
    private func dateToString(timestamp: Int64) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd.MM.yyyy"
        
        let now = dateFormatter.string(from: Date())
        let yesterday = dateFormatter.string(from: Calendar.current.date(byAdding: .day, value: -1, to: Date())!)
        let date = dateFormatter.string(from: Date(timeIntervalSince1970: TimeInterval(timestamp)))
        
        if date == now {
            return "today"
        }
        if date == yesterday {
            return "yesterday"
        }
        return date
    }
    
    private func handleAppear(articlePreview: ArticlePreview) {
        if !loading && (articlePreviews.firstIndex(of: articlePreview) ?? 0) + 7 > articlePreviews.count {
            self.loading = true
            DispatchQueue.main.async {
                fetchPreviewArticles(page: self.page + 1)
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
    }
}
