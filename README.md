<p align="center">
  <img 
    src="https://github.com/ruslanlesko/digestwave/raw/main/logo/main.png" 
    alt="Digestwave logo"
    width="216"
    height="216"
  />
</p>

<h1 align="center">Digestwave</h1>

News aggregator which doesn't spy on you and allows adding new sources by committing into the source code.

Created by [Ruslan Lesko](https://leskor.com)

## The goal of the project

I am starting Digestwave to create a news aggregator which will include articles from websites I follow. Unfortunately, Flipboard or Apple News do not support any news sites from Ukraine. The solution should allow adding any website by publishing PR to this repo. 

Since this is my pet project, I am able to make it ad-free. And consequently, there is no need to track user data and sell it. 

To summarize, Digestwave has a goal of obtaining 2 main properties:
1. **Ad-free**, **privacy-oriented** news aggregator
2. **Flexibility** for adding new sources

## UX

Having used some *one-size-fits-all* solutions, I decided to try implementing a native experience for each of the following platforms:

* Web (HTML, CSS, vanilla JS?)
* iOS (SwiftUI)
* Android (Jetpack Compose)

The idea is to make font-ends as responsive and fast as possible. Technologies for mobile apps were selected by looking at the latest first-party UI frameworks.

## Roadmap

This is just the beginning, so I don't have any specific assumptions on when and how the project will become more or less usable. However, I do have a list of milestones I want to achieve **by the end of 2022**:

* Backend solution with minimal features and news sources. It should be able to periodically fetch and parse articles for later exposing them from public API.
* Web client to render news feed and open each article.
* Automation for publishing backend as Docker images.
* Ability to deploy it locally (both backend and web client) using a single command via Docker Compose.
