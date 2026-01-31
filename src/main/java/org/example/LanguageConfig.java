package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Cau hinh ngon ngu cho kiem tra L10n
 */
public class LanguageConfig {
    public String code;
    public String languageName;
    public String primaryCurrency;
    public String secondaryCurrency;
    public String defaultCurrency;
    public String datePattern;
    public String decimalSeparator;
    public String thousandSeparator;
    public String[] expectedKeywords;

    public LanguageConfig(String code, String name, String primary, String secondary, String defaultCur,
            String datePattern, String decimal, String thousand, String[] keywords) {
        this.code = code;
        this.languageName = name;
        this.primaryCurrency = primary;
        this.secondaryCurrency = secondary;
        this.defaultCurrency = defaultCur;
        this.datePattern = datePattern;
        this.decimalSeparator = decimal;
        this.thousandSeparator = thousand;
        this.expectedKeywords = keywords;
    }

    // ==================== ALL PRESTASHOP LANGUAGES ====================
    private static final Map<String, LanguageConfig> CONFIGS = new HashMap<>();

    static {
        // Korean - 한국어
        CONFIGS.put("ko", new LanguageConfig("ko", "한국어", "₩", "원", "€",
                "\\d{4}[/.-]\\d{1,2}[/.-]\\d{1,2}", ",", ".",
                new String[] { "장바구니에 담기", "홈", "의류", "액세서리", "검색", "로그인", "장바구니" }));

        // Bosnian - Bosanski
        CONFIGS.put("bs", new LanguageConfig("bs", "Bosanski", "KM", "BAM", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Dodaj u korpu", "Početna", "Odjeća", "Dodaci", "Pretraga", "Prijava", "Korpa" }));

        // Catalan - Català
        CONFIGS.put("ca", new LanguageConfig("ca", "Català", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Afegir al carret", "Inici", "Roba", "Accessoris", "Cercar", "Iniciar sessió",
                        "Carret" }));

        // Danish - Dansk
        CONFIGS.put("da", new LanguageConfig("da", "Dansk", "kr", "DKK", "€",
                "\\d{1,2}-\\d{1,2}-\\d{2,4}", ",", ".",
                new String[] { "Læg i kurv", "Hjem", "Tøj", "Tilbehør", "Søg", "Log ind", "Kurv" }));

        // German - Deutsch
        CONFIGS.put("de", new LanguageConfig("de", "Deutsch", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "In den Warenkorb", "Startseite", "Kleidung", "Zubehör", "Suchen", "Anmelden",
                        "Warenkorb" }));

        // English
        CONFIGS.put("en", new LanguageConfig("en", "English", "$", "£", null,
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "Add to cart", "Home", "Clothes", "Accessories", "Search", "Sign in", "Cart" }));

        // Spanish - Español
        CONFIGS.put("es", new LanguageConfig("es", "Español", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Añadir al carrito", "Inicio", "Ropa", "Accesorios", "Buscar", "Iniciar sesión",
                        "Carrito" }));

        // Spanish Mexico - Español MX
        CONFIGS.put("mx", new LanguageConfig("mx", "Español MX", "$", "MXN", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "Añadir al carrito", "Inicio", "Ropa", "Accesorios", "Buscar", "Iniciar sesión",
                        "Carrito" }));

        // Estonian - Eesti keel
        CONFIGS.put("et", new LanguageConfig("et", "Eesti keel", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Lisa korvi", "Avaleht", "Riided", "Aksessuaarid", "Otsi", "Logi sisse", "Korv" }));

        // French - Français
        CONFIGS.put("fr", new LanguageConfig("fr", "Français", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", " ",
                new String[] { "Ajouter au panier", "Accueil", "Vêtements", "Accessoires", "Rechercher", "Connexion",
                        "Panier" }));

        // French Canada - Français CA
        CONFIGS.put("qc", new LanguageConfig("qc", "Français CA", "$", "CAD", "€",
                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                new String[] { "Ajouter au panier", "Accueil", "Vêtements", "Accessoires", "Rechercher", "Connexion",
                        "Panier" }));

        // Galician - Galego
        CONFIGS.put("gl", new LanguageConfig("gl", "Galego", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Engadir ao carro", "Inicio", "Roupa", "Accesorios", "Buscar", "Iniciar sesión",
                        "Carro" }));

        // Croatian - Hrvatski
        CONFIGS.put("hr", new LanguageConfig("hr", "Hrvatski", "€", "kn", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Dodaj u košaricu", "Početna", "Odjeća", "Dodaci", "Traži", "Prijava", "Košarica" }));

        // Indonesian - Indonesia
        CONFIGS.put("id", new LanguageConfig("id", "Indonesia", "Rp", "IDR", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Tambah ke keranjang", "Beranda", "Pakaian", "Aksesoris", "Cari", "Masuk",
                        "Keranjang" }));

        // Italian - Italiano
        CONFIGS.put("it", new LanguageConfig("it", "Italiano", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Aggiungi al carrello", "Home", "Abbigliamento", "Accessori", "Cerca", "Accedi",
                        "Carrello" }));

        // Latvian - Latviešu
        CONFIGS.put("lv", new LanguageConfig("lv", "Latviešu", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Pievienot grozam", "Sākums", "Apģērbs", "Aksesuāri", "Meklēt", "Pieslēgties",
                        "Grozs" }));

        // Hungarian - Magyar
        CONFIGS.put("hu", new LanguageConfig("hu", "Magyar", "Ft", "HUF", "€",
                "\\d{4}\\.\\d{1,2}\\.\\d{1,2}", ",", " ",
                new String[] { "Kosárba", "Főoldal", "Ruházat", "Kiegészítők", "Keresés", "Bejelentkezés", "Kosár" }));

        // Dutch - Nederlands
        CONFIGS.put("nl", new LanguageConfig("nl", "Nederlands", "€", "€", "€",
                "\\d{1,2}-\\d{1,2}-\\d{2,4}", ",", ".",
                new String[] { "In winkelwagen", "Home", "Kleding", "Accessoires", "Zoeken", "Inloggen",
                        "Winkelwagen" }));

        // Norwegian - Norsk
        CONFIGS.put("no", new LanguageConfig("no", "Norsk", "kr", "NOK", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Legg i handlekurv", "Hjem", "Klær", "Tilbehør", "Søk", "Logg inn", "Handlekurv" }));

        // Polish - Polski
        CONFIGS.put("pl", new LanguageConfig("pl", "Polski", "zł", "PLN", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Dodaj do koszyka", "Strona główna", "Ubrania", "Akcesoria", "Szukaj", "Zaloguj się",
                        "Koszyk" }));

        // Portuguese - Português
        CONFIGS.put("pt", new LanguageConfig("pt", "Português", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Adicionar ao carrinho", "Início", "Roupas", "Acessórios", "Pesquisar", "Entrar",
                        "Carrinho" }));

        // Portuguese Brazil - Português BR
        CONFIGS.put("br", new LanguageConfig("br", "Português BR", "R$", "BRL", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Adicionar ao carrinho", "Início", "Roupas", "Acessórios", "Pesquisar", "Entrar",
                        "Carrinho" }));

        // Romanian - Română
        CONFIGS.put("ro", new LanguageConfig("ro", "Română", "lei", "RON", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Adaugă în coș", "Acasă", "Îmbrăcăminte", "Accesorii", "Căutare", "Autentificare",
                        "Coș" }));

        // Albanian - Shqip
        CONFIGS.put("sq", new LanguageConfig("sq", "Shqip", "L", "ALL", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Shto në shportë", "Ballina", "Veshje", "Aksesorë", "Kërko", "Hyr", "Shporta" }));

        // Slovak - Slovenčina
        CONFIGS.put("sk", new LanguageConfig("sk", "Slovenčina", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Pridať do košíka", "Domov", "Oblečenie", "Doplnky", "Hľadať", "Prihlásiť sa",
                        "Košík" }));

        // Serbian - Srpski
        CONFIGS.put("sr", new LanguageConfig("sr", "Srpski", "RSD", "дин", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Dodaj u korpu", "Početna", "Odeća", "Dodaci", "Pretraga", "Prijava", "Korpa" }));

        // Finnish - Suomi
        CONFIGS.put("fi", new LanguageConfig("fi", "Suomi", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Lisää ostoskoriin", "Etusivu", "Vaatteet", "Asusteet", "Haku", "Kirjaudu",
                        "Ostoskori" }));

        // Swedish - Svenska
        CONFIGS.put("sv", new LanguageConfig("sv", "Svenska", "kr", "SEK", "€",
                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                new String[] { "Lägg i kundvagn", "Hem", "Kläder", "Tillbehör", "Sök", "Logga in", "Kundvagn" }));

        // Turkish - Türkçe
        CONFIGS.put("tr", new LanguageConfig("tr", "Türkçe", "₺", "TL", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Sepete ekle", "Ana Sayfa", "Giyim", "Aksesuar", "Ara", "Giriş yap", "Sepet" }));

        // Lithuanian - Lietuvių
        CONFIGS.put("lt", new LanguageConfig("lt", "Lietuvių", "€", "€", "€",
                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                new String[] { "Į krepšelį", "Pradžia", "Drabužiai", "Aksesuarai", "Ieškoti", "Prisijungti",
                        "Krepšelis" }));

        // Slovenian - Slovenščina
        CONFIGS.put("sl", new LanguageConfig("sl", "Slovenščina", "€", "€", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Dodaj v košarico", "Domov", "Oblačila", "Dodatki", "Išči", "Prijava", "Košarica" }));

        // Vietnamese - Tiếng Việt
        CONFIGS.put("vi", new LanguageConfig("vi", "Tiếng Việt", "₫", "đ", "VND",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Thêm vào giỏ", "Trang chủ", "Quần áo", "Phụ kiện", "Tìm kiếm", "Đăng nhập",
                        "Giỏ hàng" }));

        // Czech - Čeština
        CONFIGS.put("cs", new LanguageConfig("cs", "Čeština", "Kč", "CZK", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Přidat do košíku", "Domů", "Oblečení", "Doplňky", "Hledat", "Přihlásit se", "Košík" }));

        // Greek - Ελληνικά
        CONFIGS.put("el", new LanguageConfig("el", "Ελληνικά", "€", "€", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                new String[] { "Προσθήκη στο καλάθι", "Αρχική", "Ρούχα", "Αξεσουάρ", "Αναζήτηση", "Σύνδεση",
                        "Καλάθι" }));

        // Ukrainian - Українська
        CONFIGS.put("uk", new LanguageConfig("uk", "Українська", "₴", "грн", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Додати до кошика", "Головна", "Одяг", "Аксесуари", "Пошук", "Увійти", "Кошик" }));

        // Russian - Русский
        CONFIGS.put("ru", new LanguageConfig("ru", "Русский", "₽", "руб", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "В корзину", "Главная", "Одежда", "Аксессуары", "Поиск", "Войти", "Корзина" }));

        // Bulgarian - Български
        CONFIGS.put("bg", new LanguageConfig("bg", "Български", "лв", "BGN", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                new String[] { "Добави в кошницата", "Начало", "Дрехи", "Аксесоари", "Търсене", "Вход", "Кошница" }));

        // Macedonian - Македонски
        CONFIGS.put("mk", new LanguageConfig("mk", "Македонски", "ден", "MKD", "€",
                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                new String[] { "Додај во кошничка", "Почетна", "Облека", "Додатоци", "Барај", "Најава", "Кошничка" }));

        // Hebrew - עברית
        CONFIGS.put("he", new LanguageConfig("he", "עברית", "₪", "ש״ח", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "הוסף לסל", "דף הבית", "בגדים", "אביזרים", "חיפוש", "התחברות", "סל קניות" }));

        // Persian - فارسی
        CONFIGS.put("fa", new LanguageConfig("fa", "فارسی", "﷼", "ریال", "€",
                "\\d{4}/\\d{1,2}/\\d{1,2}", ".", ",",
                new String[] { "افزودن به سبد", "خانه", "لباس", "لوازم جانبی", "جستجو", "ورود", "سبد خرید" }));

        // Hindi - हिन्दी
        CONFIGS.put("hi", new LanguageConfig("hi", "हिन्दी", "₹", "रु", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "कार्ट में जोड़ें", "होम", "कपड़े", "सहायक उपकरण", "खोजें", "लॉग इन", "कार्ट" }));

        // Bengali - বাংলা
        CONFIGS.put("bn", new LanguageConfig("bn", "বাংলা", "৳", "টাকা", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "কার্টে যোগ করুন", "হোম", "পোশাক", "আনুষাঙ্গিক", "অনুসন্ধান", "লগইন", "কার্ট" }));

        // Arabic - العربية
        CONFIGS.put("ar", new LanguageConfig("ar", "العربية", "د.إ", "ر.س", "€",
                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                new String[] { "أضف إلى السلة", "الرئيسية", "ملابس", "إكسسوارات", "بحث", "تسجيل الدخول", "السلة" }));

        // Japanese - 日本語
        CONFIGS.put("ja", new LanguageConfig("ja", "日本語", "¥", "円", "€",
                "\\d{4}[/年]\\d{1,2}[/月]\\d{1,2}", ".", ",",
                new String[] { "カートに入れる", "ホーム", "服", "アクセサリー", "検索", "ログイン", "カート" }));

        // Chinese Simplified - 简体中文
        CONFIGS.put("zh", new LanguageConfig("zh", "简体中文", "¥", "元", "€",
                "\\d{4}[/年-]\\d{1,2}[/月-]\\d{1,2}", ".", ",",
                new String[] { "加入购物车", "首页", "服装", "配饰", "搜索", "登录", "购物车" }));

        // Chinese Traditional - 繁體中文
        CONFIGS.put("tw", new LanguageConfig("tw", "繁體中文", "NT$", "$", "€",
                "\\d{4}[/年-]\\d{1,2}[/月-]\\d{1,2}", ".", ",",
                new String[] { "加入購物車", "首頁", "服飾", "配件", "搜尋", "登入", "購物車" }));
    }

    public static LanguageConfig get(String code) {
        return CONFIGS.get(code);
    }

    public static boolean isSupported(String code) {
        return CONFIGS.containsKey(code);
    }

    public static String[] getSupportedLanguages() {
        return CONFIGS.keySet().toArray(new String[0]);
    }
}
