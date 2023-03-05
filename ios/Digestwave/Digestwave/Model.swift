//
//  Model.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 03.03.2023.
//

enum Edition: String, CaseIterable, Identifiable {
    case international, ukrainian
    var id: Self { self }
}

struct Article: Hashable {
    var title: String
    var site: String
}
