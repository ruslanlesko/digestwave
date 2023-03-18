//
//  Model.swift
//  Digestwave
//
//  Created by Ruslan Lesko on 03.03.2023.
//

enum EditionOpt: String, CaseIterable, Identifiable {
    case international, ukrainian
    var id: Self { self }
}
