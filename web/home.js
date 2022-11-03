import { getRegion, fillTopics, setUpLocaleSelector, highlightCurrentTopic, parsePublicationTime } from "./common.js";

const ARTICLES_LIST_URL = 'http://localhost:8080/v1/preview/articles';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' };

var page = 1;
var alreadyFetched = false;

function displayArticlePreview(articlePreview) {
    const parent = document.getElementById('articlesList');

    const newDiv = document.createElement('div');
    newDiv.className = 'articlePreview';
    parent.appendChild(newDiv);

    const fallbackImg = "/assets/" + articlePreview.site + ".svg";
    const newImg = document.createElement('img');
    const imgSrc = articlePreview.hasCoverImage ? 
        ARTICLE_IMAGE_URL + articlePreview.id + "/image"
        : fallbackImg;
    newImg.setAttribute("src", imgSrc);
    newImg.setAttribute('alt', 'Article cover image');
    newImg.setAttribute('onerror', 'this.onerror=null;this.src="' + fallbackImg + '"');
    newDiv.appendChild(newImg);

    newDiv.innerHTML += `<a href="/article.html?id=${articlePreview.id}">${articlePreview.title}</a>`;

    const publicationTime = parsePublicationTime(articlePreview.publicationTime);
    const site = document.createElement('div');
    site.className = 'site';
    site.innerHTML = `<a href="https://${articlePreview.site}">${articlePreview.site}</a><span> • ${publicationTime}</span>`;
    newDiv.appendChild(site);
}

function handleFailureToFetchArticlesList(error) {
    console.log(`Error while fetching articles list: ${error}`);
    const parent = document.getElementById('articlesList');
    if (parent.childElementCount > 0) {
        return;
    }
    const errorNode = document.createElement('div');
    errorNode.className = 'error';
    const errorImage = document.createElement('img');
    errorImage.setAttribute('src', '/assets/error.svg');
    errorImage.setAttribute('width', '72px');
    errorNode.appendChild(errorImage);
    const errorText = document.createElement('p');
    errorText.innerText = 'Failed to load data from server, please try again later.';
    errorNode.appendChild(errorText);
    parent.appendChild(errorNode);
}

async function fetchArticlesList(topic, page) {
    const region = await getRegion();
    const url = topic === null || topic === "" ? ARTICLES_LIST_URL + "?page=" + page + "&size=20" + "&region=" + region
        : ARTICLES_LIST_URL + "?topic=" + topic + "&region=" + region + "&page=" + page + "&size=20";
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
            } else {
                if (document.getElementById('articlesList').childElementCount === 0) {
                    throw ('Server returned empty list of articles');
                }
            }
        })
        .catch(e => handleFailureToFetchArticlesList(e));
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

fillTopics();
setUpLocaleSelector();

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