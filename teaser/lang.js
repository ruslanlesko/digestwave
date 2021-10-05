function setSubtitle(text) {
    const el = document.querySelector("div#center h2")
    el.textContent = text
}

function setLink(text) {
    const el = document.querySelector("div#center a")
    el.textContent = text
}

const lang = navigator.language

if (lang.startsWith("uk")) {
    setSubtitle("Незабаром")
    setLink("відстежувати на GitHub")
}

if (lang.startsWith("ru")) {
    setSubtitle("Уже Скоро")
    setLink("отслеживать на GitHub")
}