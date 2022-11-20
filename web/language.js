const ukrainian = {
    "Finance": "Фінанси",
    "Technology": "Технології",
    "Programming": "Програмування",
    "Football" : "Футбол",
    "Ukrainian": "Українська",
    "International": "Міжнародна",
    "Failed to load data from server, please try again later.": "Невдалося завантажити дані з серверу. Будь-ласка, спробуйте пізніше.",
    "Artcile is not found, please search for other one.": "Публікація не знайдена, спробуйте іншу.",
    "Failed to load article from server, please try again later.": "Невдалося завантажити публікацію з сервера. Будь-ласка, спробуйте пізніше.",
    "Go to the original article": "Перейти на оригінальну сторінку",
    " • Today": " • Сьогодні",
    " • Yesterday": " • Вчора"
};

function scanAndTranslate(languageMap) {
    const elems = document.getElementsByClassName('translatable');
    for (let i = 0; i < elems.length; i++) {
        const value = languageMap[elems[i].textContent];
        if (value !== undefined) {
            elems[i].textContent = value;
        }
    }
}

export function translate(locale) {
    if (locale === 'ua') {
        scanAndTranslate(ukrainian);
    }
}