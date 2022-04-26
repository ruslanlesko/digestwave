const ARTICLES_LIST_URL = 'http://localhost:8080/v1/preview/articles';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

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
    site.innerHTML = articlePreview.site;
    newDiv.appendChild(site);
}

function fetchArticlesList() {
    fetch(ARTICLES_LIST_URL, { 'headers': HEADERS })
        .then(resp => {
            if (resp.status == 200) {
                return resp.json();
            } else {
                throw(`Server responded with status ${resp.status}`);
            }
        })
        .then(articles => articles.forEach(displayArticlePreview))
        .catch(e => console.log(`Error while fetching articles list: ${e}`));
}

fetchArticlesList();