export async function getRegion() {
    const fromLocalStore = localStorage.getItem('locale');
    if (fromLocalStore !== null) return fromLocalStore;

    const response = await fetch('https://wtfismyip.com/json');
    const jsonReponse = await response.json();
    const userLocale = jsonReponse.YourFuckingCountryCode;

    const result = userLocale.startsWith('UA') ? 'ua' : 'int';
    localStorage.setItem('locale', result);
    return result;
}

export async function fillTopics() {
    const uaTopicKeys = ['tech', 'finance', 'football'];
    const uaTopicVals = ['Technology', 'Finance', 'Football'];

    const intTopicKeys = ['tech', 'finance', 'programming'];
    const intTopicVals = ['Technology', 'Finance', 'Programming'];

    const region = await getRegion();
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

export async function setUpLocaleSelector() {
    const region = await getRegion();
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

export function highlightCurrentTopic(topic) {
    const links = document.getElementsByTagName('a');
    for (var a in links) {
        if (links[a].getAttribute('href').startsWith('/?topic=' + topic)) {
            links[a].className = 'currentSelection';
            break;
        }
    }
}

function formatDate(date) {
    return date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear();
}

export function parsePublicationTime(stamp) {
    var date = formatDate(new Date(stamp * 1000));
    var currentDate = new Date();
    var today = formatDate(currentDate);
    currentDate.setDate(currentDate.getDate() - 1);
    
    var yesterday = formatDate(currentDate);
    switch (date) {
        case today:
            return "Today";
        case yesterday:
            return "Yesterday";
        default:
            return date;
    }
}