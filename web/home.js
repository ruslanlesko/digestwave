import { getRegion, fillTopics, setUpLocaleSelector, highlightCurrentTopic, parsePublicationTime, handleTheme, getBaseURL } from "./common.js";
import { translate } from "./language.js";

const TOP_ARTICLES_URL = getBaseURL() + '/v1/preview/top/'
const ARTICLES_LIST_URL = getBaseURL() + '/v1/preview/articles';
const ARTICLE_IMAGE_URL = getBaseURL() + '/v1/articles/';
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
    const imgLink = document.createElement('a');
    imgLink.setAttribute('href', `/article.html?id=${articlePreview.id}`);
    imgLink.appendChild(newImg);
    newDiv.appendChild(imgLink);

    var title = articlePreview.title.length > 120 ?
        articlePreview.title.substring(0, 110) + "..." : articlePreview.title;

    newDiv.innerHTML += `<a href="/article.html?id=${articlePreview.id}">${title}</a>`;

    const publicationTime = parsePublicationTime(articlePreview.publicationTime);
    const site = document.createElement('div');
    site.className = 'site';
    site.innerHTML = `<a href="https://${articlePreview.site}">${articlePreview.site}</a><span class="translatable"> • ${publicationTime}</span>`;
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
    errorText.className = 'translatable';
    errorNode.appendChild(errorText);
    parent.appendChild(errorNode);
}

function displayTopArticles(topArticles) {
    if (topArticles.length < 3) {
        return;
    }
    const parent = document.getElementById('topArticles');

    const firstLink = document.createElement('a');
    firstLink.setAttribute('href', `/article.html?id=${topArticles[0].id}`);
    const firstThumbnail = document.createElement('div');
    firstThumbnail.className = 'article-thumbnail';
    const firstImage = document.createElement('img');
    firstImage.setAttribute('src', ARTICLE_IMAGE_URL + topArticles[0].id + '/image');
    const firstText = document.createElement('div');
    firstText.className = 'article-thumbnail-text';
    const firstTitle =  document.createElement('h1');
    firstTitle.className = 'article-thumbnail-title';
    const firstTitleText = topArticles[0].title.length > 80 ?
        topArticles[0].title.substring(0, 70) + "..." : topArticles[0].title;
    firstTitle.innerText += firstTitleText;
    const firstSubTitle = document.createElement('div');
    firstSubTitle.className = 'aticle-thimbnail-subtitle';
    const firstPublicationTime = parsePublicationTime(topArticles[0].publicationTime);
    firstSubTitle.innerHTML += `${topArticles[0].site}<span class="translatable"> • ${firstPublicationTime}</span>`;
    firstText.appendChild(firstTitle);
    firstText.appendChild(firstSubTitle);
    firstThumbnail.appendChild(firstImage);
    firstThumbnail.appendChild(firstText);
    firstLink.appendChild(firstThumbnail);

    parent.appendChild(firstLink);

    const rightPaneDiv = document.createElement('div');
    rightPaneDiv.className = 'right-thumbnail-pane';

    const secondLink = document.createElement('a');
    secondLink.setAttribute('href', `/article.html?id=${topArticles[1].id}`);
    const secondThumbnail = document.createElement('div');
    secondThumbnail.className = 'article-thumbnail article-thumbnail-small';
    const secondImage = document.createElement('img');
    secondImage.setAttribute('src', ARTICLE_IMAGE_URL + topArticles[1].id + '/image');
    const secondText = document.createElement('div');
    secondText.className = 'article-thumbnail-text';
    const secondTitle =  document.createElement('h1');
    secondTitle.className = 'article-thumbnail-title';
    const secondTitleText = topArticles[1].title.length > 80 ?
        topArticles[1].title.substring(0, 70) + "..." : topArticles[1].title;
    secondTitle.innerText += secondTitleText;
    const secondSubTitle = document.createElement('div');
    secondSubTitle.className = 'aticle-thimbnail-subtitle';
    const secondPublicationTime = parsePublicationTime(topArticles[1].publicationTime);
    secondSubTitle.innerHTML += `${topArticles[1].site}<span class="translatable"> • ${secondPublicationTime}</span>`;
    secondText.appendChild(secondTitle);
    secondText.appendChild(secondSubTitle);
    secondThumbnail.appendChild(secondImage);
    secondThumbnail.appendChild(secondText);
    secondLink.appendChild(secondThumbnail);

    const thirdLink = document.createElement('a');
    thirdLink.setAttribute('href', `/article.html?id=${topArticles[2].id}`);
    const thirdThumbnail = document.createElement('div');
    thirdThumbnail.className = 'article-thumbnail article-thumbnail-small';
    const thirdImage = document.createElement('img');
    thirdImage.setAttribute('src', ARTICLE_IMAGE_URL + topArticles[2].id + '/image');
    const thirdText = document.createElement('div');
    thirdText.className = 'article-thumbnail-text';
    const thirdTitle =  document.createElement('h1');
    thirdTitle.className = 'article-thumbnail-title';
    const thirdTitleText = topArticles[2].title.length > 80 ?
        topArticles[2].title.substring(0, 70) + "..." : topArticles[2].title;
    thirdTitle.innerText += thirdTitleText;
    const thirdSubTitle = document.createElement('div');
    thirdSubTitle.className = 'aticle-thimbnail-subtitle';
    const thirdPublicationTime = parsePublicationTime(topArticles[2].publicationTime);
    thirdSubTitle.innerHTML += `${topArticles[2].site}<span class="translatable"> • ${thirdPublicationTime}</span>`;
    thirdText.appendChild(thirdTitle);
    thirdText.appendChild(thirdSubTitle);
    thirdThumbnail.appendChild(thirdImage);
    thirdThumbnail.appendChild(thirdText);
    thirdLink.appendChild(thirdThumbnail);

    rightPaneDiv.appendChild(secondLink);
    rightPaneDiv.appendChild(thirdLink);

    parent.appendChild(rightPaneDiv);
}

async function fetchTopArticles(topic) {
    const region = await getRegion();
    const url = topic === null || topic === "" || !isTopicCompatibleWithRegion(topic, region)
        ? TOP_ARTICLES_URL + region : TOP_ARTICLES_URL + region + "/" + topic;
    return fetch(url, { 'headers': HEADERS })
        .then(resp => {
            if (resp.status == 200) {
                return resp.json();
            } else {
                console.log(`Server responded with status ${resp.status}`);
                return [];
            }
        });
}

async function fetchArticlesList(topic, page, topArticles) {
    const region = await getRegion();
    const url = topic === null || topic === "" || !isTopicCompatibleWithRegion(topic, region) ? ARTICLES_LIST_URL + "?page=" + page + "&size=20" + "&region=" + region
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
                articles.filter(a => {
                    const title = a.title;
                    for (var i = 0; i < topArticles.length; i++) {
                        if (title === topArticles[i].title) {
                            return false;
                        }
                    }
                    return true;
                }).forEach(displayArticlePreview);
                alreadyFetched = false;
                translate(region);
            } else {
                if (document.getElementById('articlesList').childElementCount === 0) {
                    throw ('Server returned empty list of articles');
                }
            }
        })
        .catch(e => {
            handleFailureToFetchArticlesList(e);
            translate(region);
        });
}

function isTopicCompatibleWithRegion(topic, region) {
    if (region === 'ua') {
        return topic !== 'programming';
    }
    return topic !== 'football';
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

const topArticles = await fetchTopArticles(topic);
displayTopArticles(topArticles);

if (topic != null && topic != "") {
    fetchArticlesList(topic, 1, topArticles);
    highlightCurrentTopic(topic);
} else {
    fetchArticlesList("", 1, topArticles);
}

handleTheme();

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
                fetchArticlesList(topic, page, topArticles);
            } else {
                fetchArticlesList("", page, topArticles);
            }
        }
    }
});