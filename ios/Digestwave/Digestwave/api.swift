//
//  api.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 12.03.2023.
//

import Foundation
import Combine

struct Agent {
    struct Response<T> {
        let value: T?
        let response: URLResponse
        let status: Int
    }
    
    func run<T: Decodable>(_ request: URLRequest, noResponseBody: Bool = false, _ decoder: JSONDecoder = JSONDecoder()) -> AnyPublisher<Response<T>, Error> {
        return URLSession.shared
            .dataTaskPublisher(for: request)
            .tryMap { result -> Response<T> in
                if let httpResp = result.response as? HTTPURLResponse {
                    let statusCode = httpResp.statusCode
                    if statusCode < 400 {
                        if noResponseBody {
                            return Response(value: nil, response: result.response, status: statusCode)
                        }
                        
                        let value = try decoder.decode(T.self, from: result.data)
                        return Response(value: value, response: result.response, status: statusCode)
                    }
                    return Response(value: nil, response: result.response, status: statusCode)
                }
                return Response(value: nil, response: result.response, status: 500)
            }
            .receive(on: DispatchQueue.main)
            .eraseToAnyPublisher()
    }
}

enum DigestwaveAPI {
    static let agent = Agent()
    static let base = URL(string: "https://api.leskor.com/digestwave/v1")!
}

extension DigestwaveAPI {
    static func articlePreviews(region: String, topic: String?, page: Int) -> AnyPublisher<Agent.Response<[ArticlePreview]>, Error> {
        var path = "/preview/articles?page=\(page)&size=25&region=\(region)"
        if topic != nil {
            path = path + "&topic=\(topic!)"
        }
        var request = URLRequest(url: URL(string: base.absoluteString + path)!)
        request.httpMethod = "GET"
        
        return agent.run(request)
            .eraseToAnyPublisher()
    }
    
    static func article(id: String) -> AnyPublisher<Agent.Response<Article>, Error> {
        var request = URLRequest(url: URL(string: base.absoluteString + "/articles/\(id)")!)
        request.httpMethod = "GET"
        
        return agent.run(request)
            .eraseToAnyPublisher()
    }
}

struct ArticlePreview: Codable, Hashable, Identifiable {
    var id: String
    var title: String
    var site: String
    var topic: String
    var hasCoverImage: Bool
    var publicationTime: Int64
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(title)
        hasher.combine(site)
        hasher.combine(topic)
        hasher.combine(hasCoverImage)
        hasher.combine(publicationTime)
    }
}

struct Article: Codable, Hashable, Identifiable {
    var id: String
    var title: String
    var content: [String]
    var styles: [String: String]
    var site: String
    var topic: String
    var hasCoverImage: Bool
    var publicationTime: Int64
    var url: String
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(title)
        hasher.combine(site)
        hasher.combine(topic)
        hasher.combine(hasCoverImage)
        hasher.combine(publicationTime)
        hasher.combine(url)
    }
}
