const ARTICLES_LIST_URL = 'http://localhost:8080/v1/preview/articles';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

function displayArticlePreview(articlePreview) {
    const parent = document.getElementById('articlesList');

    if (articlePreview.hasCoverImage) {
        const newImg = document.createElement('img');
        newImg.setAttribute("src", ARTICLE_IMAGE_URL + articlePreview.id + "/image");
        newImg.setAttribute('width', '120px');
        parent.appendChild(newImg);
    }

    const newDiv = document.createElement('div');
    newDiv.className = 'articlePreview';
    newDiv.innerHTML += `<a href="/article.html?id=${articlePreview.id}">${articlePreview.title}</a>`;
    parent.appendChild(newDiv);
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