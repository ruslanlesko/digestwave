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

enum EditionOpt: String, CaseIterable, Identifiable {
    case international, ukrainian
    var id: Self { self }
}

struct ContentView: View {
    @Environment(\.managedObjectContext) private var viewContext
    @Environment(\.colorScheme) var colorScheme
    
    @State private var cancellable: AnyCancellable?
    
    @FetchRequest(entity: Edition.entity(), sortDescriptors: [])
    var editions: FetchedResults<Edition>
    
    @FetchRequest(entity: Bookmark.entity(), sortDescriptors: [])
    var bookmarks: FetchedResults<Bookmark>
    
    @State private var edition: EditionOpt = .international
    @State private var topics: [String] = intTopics
    @State private var articlePreviews: [ArticlePreview] = []
    @State private var topic: String? = "all"
    @State private var selectedArticle: ArticlePreview?
    @State private var loading = false
    @State private var page = 1
    @State private var errorLoading = false
    @State private var article: Article? = nil
    
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
                                .foregroundColor(.accentColor)
                            Text(LocalizedStringKey(t))
                        }
                    }
                }
                
                #if os(macOS)
                Section {
                    EmptyView()
                }
                #endif
                
                NavigationLink(value: "bookmarks") {
                    Image(systemName: "bookmark")
                        .foregroundColor(.accentColor)
                    Text(LocalizedStringKey("bookmarks"))
                }
            }
            .onChange(of: topic, perform: { value in
                if topic == "bookmarks" {
                    displayBookmarks()
                    return;
                }
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
            List(selection: $selectedArticle) {
                ForEach(articlePreviews, id: \.id) { article in
                    NavigationLink(value: article) {
                        HStack {
                            VStack {
                                HStack {
                                    Text(formatTitle(title: replaceHtml(article.title)))
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
                .onDelete(perform: topic == "bookmarks" ? deleteBookmark : nil)
            }
            .refreshable {
                doRefresh()
            }
            .onChange(of: selectedArticle, perform: { value in
                self.article = nil
                if value != nil {
                    DispatchQueue.main.async { fetchArticle(id: value!.id) }
                }
            })
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
            if selectedArticle == nil {
                Image(systemName: "newspaper")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .foregroundColor(.secondary)
                    .frame(width: 128, height: 128)
            } else {
                ScrollView {
                    #if os(macOS)
                    Text(selectedArticle!.site)
                        .fontWeight(.light)
                        .foregroundColor(.secondary)
                        .padding([.vertical], 8)
                    #endif
                    HStack {
                        Text(replaceHtml(selectedArticle!.title))
                            .font(.title)
                            #if os(iOS)
                            .navigationTitle(selectedArticle!.site)
                            .navigationBarTitleDisplayMode(.inline)
                            #endif
                        Spacer()
                    }
                    HStack {
                        Text(LocalizedStringKey(dateToString(timestamp: selectedArticle!.publicationTime)))
                            .fontWeight(.light)
                            .foregroundColor(.secondary)
                        Spacer()
                    }.padding([.top], 1)
                    if selectedArticle!.hasCoverImage {
                        HStack {
                            AsyncImage(url: URL(string: "https://api.leskor.com/digestwave/v1/articles/\(selectedArticle!.id)/image")) {image in
                                image.resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                EmptyView()
                            }
                            .frame(width: 200, height: 128)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            Spacer()
                        }.padding([.vertical], 12)
                    }
                    if article != nil {
                        ForEach(article!.content, id: \.self) { p in
                            HStack {
                                if let style = article!.styles[String(article!.content.firstIndex(of: p)!)] {
                                    if style == "code" {
                                        Text(replaceHtml(p))
                                            .font(.monospaced(Font.system(size: 11))())
                                            .foregroundColor(Color(red: 193, green: 184, blue: 155))
                                            .padding([.all], 10)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .background(.black)
                                            .border(Color.black, width: 4)
                                            .cornerRadius(8)
                                    } else {
                                        Text(replaceHtml(p)).padding([.vertical], 10)
                                    }
                                } else {
                                    Text(replaceHtml(p)).padding([.vertical], 10)
                                }
                                Spacer()
                            }
                        }
                        Link(destination: URL(string: article!.url)!, label: {
                            Text("to_original")
                        }).padding([.bottom], 16)
                    }
                    Spacer()
                }.padding([.horizontal], 12)
                #if os(iOS)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        if article != nil {
                            HStack {
                                Button { handleBookmarkClick(article: article!) } label: {
                                    Image(systemName: isArticleBookmarked(article!) ? "bookmark.circle.fill" : "bookmark.circle")
                                }
                                Link(destination: URL(string: article!.url)!, label: {
                                    Image(systemName: "safari")
                                })
                            }
                        } else {
                            EmptyView()
                        }
                    }
                }
                #endif
                #if os(macOS)
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        if article != nil {
                            Button { handleBookmarkClick(article: article!) } label: {
                                Image(systemName: isArticleBookmarked(article!) ? "bookmark.circle.fill" : "bookmark.circle")
                            }
                        } else {
                            EmptyView()
                        }
                    }
                }
                #endif
            }
        }
        .background(colorScheme == .dark ? Color.black : Color.white)
        .navigationSplitViewStyle(.balanced)
        .tint(.purple)
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
        if (topic == "bookmarks") {
            return;
        }
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
    
    private func fetchArticle(id: String) {
        if topic == "bookmarks" {
            do {
                article = try bookmarks.filter{b in b.id == id}.map { b in
                    let decoded = try JSONDecoder().decode(Article.self, from: b.json!.data(using: .utf8)!)
                    return Article(id: b.id!, title: decoded.title, content: decoded.content, styles: decoded.styles, site: decoded.site, topic: decoded.topic, hasCoverImage: decoded.hasCoverImage, publicationTime: b.timestamp, url: decoded.url)
                }[0]
            }
            catch { print(error.localizedDescription) }
            return;
        }
        
        self.cancellable = DigestwaveAPI.article(id: id)
            .sink(receiveCompletion: { completion in
            switch completion {
                case let .failure(error):
                    print("Couldn't fetch article: \(error)")
                case .finished: break
            }
        }) { response in
            if response.status == 200 {
                self.article = response.value!
            }
            else {
                print("Error making request to article")
            }
        }
    }
    
    private func replaceHtml(_ string: String) -> String {
        return string.replacingOccurrences(of: "<br>", with: "\n")
            .replacingOccurrences(of: "&nbsp;", with: " ")
            .replacingOccurrences(of: "&lt;", with: "<")
            .replacingOccurrences(of: "&gt;", with: ">")
            .replacingOccurrences(of: "\t", with: "")
            .replacingOccurrences(of: "&quot;", with: "\"")
        
    }
    
    private func doRefresh() {
        if loading || topic == "bookmarks" {
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
    
    private func isArticleBookmarked(_ article: Article) -> Bool {
        return bookmarks.first(where: {b in b.id == article.id}) != nil;
    }
    
    private func handleBookmarkClick(article: Article) {
        if isArticleBookmarked(article) {
            let bookmark = bookmarks.first(where: {b in b.id == article.id})!
            viewContext.delete(bookmark)
            do {
                try viewContext.save()
            }
            catch { print(error.localizedDescription) }
            if (topic == "bookmarks") {
                displayBookmarks()
            }
            return;
        }
        let bookmark = Bookmark(context: viewContext)
        bookmark.id = article.id
        bookmark.timestamp = article.publicationTime
        do {
            bookmark.json = String(data: try JSONEncoder().encode(article), encoding: .utf8)
            try viewContext.save()
        }
        catch { print(error.localizedDescription) }
    }
    
    private func displayBookmarks() {
        do {
            self.articlePreviews = try bookmarks.map { b in
                let decoded = try JSONDecoder().decode(Article.self, from: b.json!.data(using: .utf8)!)
                return ArticlePreview(id: b.id!, title: decoded.title, site: decoded.site, topic: decoded.topic, hasCoverImage: decoded.hasCoverImage, publicationTime: b.timestamp)
            }
        }
        catch { print(error.localizedDescription) }
    }
    
    private func deleteBookmark(at offsets: IndexSet) {
        self.articlePreviews.remove(atOffsets: offsets)
        offsets.forEach { idx in
            viewContext.delete(bookmarks[idx])
        }
        do {
            try viewContext.save()
        } catch {
            print(error.localizedDescription)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
    }
}
