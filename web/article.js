const ARTICLE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

function displayArticle(article) {
    document.getElementsByTagName('h2')[0].innerHTML += article.title;
    document.getElementsByTagName('title')[0].innerHTML = article.title;

    article.content
        .split('\n')
        .filter(s => s.trim().length > 0)
        .forEach(p => {
            const newP = document.createElement('p');
            newP.innerHTML += p.trim();
            document.getElementById('content').appendChild(newP);
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