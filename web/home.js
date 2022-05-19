const ARTICLES_LIST_URL = 'http://localhost:8080/v1/preview/articles';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

var page = 1;
var alreadyFetched = false;

function highlightCurrentTopic(topic) {
    const links = document.getElementsByTagName('a');
    for (var a in links) {
        if (links[a].getAttribute('href') === '/?topic=' + topic) {
            links[a].className = 'currentSelection';
            break;
        }
    }
}

function displayArticlePreview(articlePreview) {
    const parent = document.getElementById('articlesList');

    const newDiv = document.createElement('div');
    newDiv.className = 'articlePreview';
    parent.appendChild(newDiv);

    if (articlePreview.hasCoverImage) {
        const newImg = document.createElement('img');
        newImg.setAttribute("src", ARTICLE_IMAGE_URL + articlePreview.id + "/image");
        newImg.setAttribute('alt', 'Article cover image');
        newImg.setAttribute('width', '120px');
        newDiv.appendChild(newImg);
    }

    newDiv.innerHTML += `<a href="/article.html?id=${articlePreview.id}">${articlePreview.title}</a>`;

    const site = document.createElement('div');
    site.className = 'site';
    site.innerHTML = `<a href="https://${articlePreview.site}">${articlePreview.site}</a>`;
    newDiv.appendChild(site);
}

function fetchArticlesList(topic, page) {
    const url = topic === null || topic === "" ? ARTICLES_LIST_URL + "?page=" + page + "&size=20"
        : ARTICLES_LIST_URL + "?topic=" + topic + "&page=" + page + "&size=20";
    fetch(url, { 'headers': HEADERS })
        .then(resp => {
            if (resp.status == 200) {
                return resp.json();
            } else {
                throw (`Server responded with status ${resp.status}`);
            }
        })
        .then(articles => {
            if (articles.length > 0) {
                articles.forEach(displayArticlePreview);
                alreadyFetched = false;
            }
        })
        .catch(e => console.log(`Error while fetching articles list: ${e}`));
}

// Determine if an element is in the visible viewport: https://gist.github.com/jjmu15/8646226
function isInViewport(element) {
    var rect = element.getBoundingClientRect();
    var html = document.documentElement;
    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || html.clientHeight) &&
        rect.right <= (window.innerWidth || html.clientWidth)
    );
}

const params = new URLSearchParams(window.location.search);
const topic = params.get("topic");

if (topic != null && topic != "") {
    fetchArticlesList(topic, 1);
    highlightCurrentTopic(topic);
} else {
    fetchArticlesList("", 1);
}

window.addEventListener("scroll", () => {
    if (alreadyFetched) return;

    const previews = document.getElementsByClassName('articlePreview');
    if (previews.length === 0) return;

    var lastPreview = previews[previews.length - 1];

    if (previews.length > 6) {
        lastPreview = previews[previews.length - 6];
    }

    if (isInViewport(lastPreview)) {
        if (!alreadyFetched) {
            alreadyFetched = true;
            page += 1;
            if (topic != null && topic != "") {
                fetchArticlesList(topic, page);
            } else {
                fetchArticlesList("", page);
            }
        }
    }
});