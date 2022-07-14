const ARTICLE_URL = 'http://localhost:8080/v1/articles/';
const ARTICLE_IMAGE_URL = 'http://localhost:8080/v1/articles/';
const HEADERS = { 'Accept': 'application/json' }

function getRegion() {
    const userLocale =
        navigator.languages && navigator.languages.length
            ? navigator.languages[0]
            : navigator.language;

    const fromLocalStore = localStorage.getItem('locale');
    if (fromLocalStore !== null) return fromLocalStore;

    return userLocale.startsWith('ua') ? 'ua' : 'int';
}

function fillTopics() {
    const uaTopicKeys = ['tech', 'finance', 'football'];
    const uaTopicVals = ['Technology', 'Finance', 'Football'];

    const intTopicKeys = ['programming'];
    const intTopicVals = ['Programming'];

    const region = getRegion();
    var topicKeys = intTopicKeys;
    var topicVals = intTopicVals;
    if (region === 'ua') {
        topicKeys = uaTopicKeys;
        topicVals = uaTopicVals;
    }

    const parent = document.getElementById('topicsBar');
    for (var i = 0; i < topicKeys.length; i++) {
        const topicDiv = document.createElement('div');
        topicDiv.className = 'topic';
        const ref = document.createElement('a');
        ref.setAttribute('href', "/?topic=" + topicKeys[i] + "&region=" + region);
        ref.innerHTML = topicVals[i];
        topicDiv.appendChild(ref);
        parent.appendChild(topicDiv);
    }
}

function setUpLocaleSelector() {
    const region = getRegion();
    const selector = document.getElementById('locale-names');
    const options = selector.getElementsByTagName('option');
    for (var opt = 0; opt < options.length; opt++) {
        if (options[opt].getAttribute('value') === region) {
            options[opt].selected = true;
        } else {
            options[opt].selected = false;
        }
    }

    selector.addEventListener("change", (_) => {
        const val = document.getElementById('locale-names').value;
        localStorage.setItem('locale', val);
        window.location.reload();
    });
}

function highlightCurrentTopic(topic) {
    const links = document.getElementsByTagName('a');
    for (var a in links) {
        if (links[a].getAttribute('href').startsWith('/?topic=' + topic)) {
            links[a].className = 'currentSelection';
            break;
        }
    }
}

function displayArticle(article) {
    document.getElementsByTagName('title')[0].innerHTML = article.title;

    const content = document.getElementById('content');

    const newTitle = document.createElement('h2');
    newTitle.innerHTML = article.title;

    if (article.hasCoverImage) {
        const newImg = document.createElement('img');
        newImg.setAttribute("src", ARTICLE_IMAGE_URL + article.id + "/image");
        newImg.setAttribute('height', '120px');
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

    const originalLinkDiv = document.getElementById('originalArticleLink');
    const link = originalLinkDiv.getElementsByTagName('a')[0];
    link.setAttribute('href', article.url);

    highlightCurrentTopic(article.topic.toLowerCase());
}

function fetchArticle(id) {
    fetch(ARTICLE_URL + id, { 'headers': HEADERS })
        .then(resp => {
            if (resp.status == 200) {
                return resp.json();
            } else {
                throw (`Server responded with status ${resp.status}`);
            }
        })
        .then(displayArticle)
        .catch(e => console.log(`Error while fetching article: ${e}`));
}

fillTopics();
setUpLocaleSelector();

const params = new URLSearchParams(window.location.search);
const id = params.get("id");

if (id != null && id != "") {
    fetchArticle(id);
} else {
    console.log("No ID given");
}