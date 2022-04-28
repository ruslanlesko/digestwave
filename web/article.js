const ARTICLE_URL = 'http://localhost:8080/v1/articles/';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

function displayArticle(article) {
    document.getElementsByTagName('title')[0].innerHTML = article.title;

    const content = document.getElementById('content');

    const newTitle = document.createElement('h2');
    newTitle.innerHTML = article.title;

    if (article.hasCoverImage) {
        const newImg = document.createElement('img');
        newImg.setAttribute("src", ARTICLE_IMAGE_URL + article.id + "/image");
        newImg.setAttribute('width', '220px');
        newImg.setAttribute('alt', 'Article cover image');
        newImg.className = 'coverImage'
        content.appendChild(newImg);
    }

    content.appendChild(newTitle);

    const site = document.createElement('div');
    site.className = 'site';
    site.innerHTML = `<a href="https://${article.site}">${article.site}</a>`;
    content.appendChild(site);

    article.content
        .split('\n')
        .filter(s => s.trim().length > 0)
        .forEach(p => {
            const newP = document.createElement('p');
            newP.innerHTML += p.trim();
            content.appendChild(newP);
        });
}

function fetchArticle(id) {
    fetch(ARTICLE_URL + id, { 'headers': HEADERS })
        .then(resp => {
            if (resp.status == 200) {
                return resp.json();
            } else {
                throw(`Server responded with status ${resp.status}`);
            }
        })
        .then(displayArticle)
        .catch(e => console.log(`Error while fetching article: ${e}`));
}

const params = new URLSearchParams(window.location.search);
const id = params.get("id");

if (id != null && id != "") {
    fetchArticle(id);
} else {
    console.log("No ID given");
}