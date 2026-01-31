package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Cau hinh ngon ngu cho kiem tra L10n
 * Day la "Bang quyet dinh" (Decision Table) chua cac ky vong cho tung kich ban
 * ngon ngu
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

        // ==================== DECISION TABLE FIELDS ====================
        // Phan nhom ngon ngu de ap dung Equivalence Partitioning

        /**
         * Ngon ngu doc tu phai sang trai (Right-to-Left)
         * Vd: Arabic, Hebrew, Persian
         */
        public boolean isRTL;

        /**
         * Loai dau phan cach thap phan
         * DOT = dau cham (en, ja, zh)
         * COMMA = dau phay (fr, de, vi)
         */
        public DecimalSeparatorType decimalSeparatorType;

        /**
         * Loai dau phan cach hang nghin
         * COMMA = dau phay (en)
         * DOT = dau cham (de)
         * SPACE = khoang trang (fr)
         * NONE = khong co (ja)
         */
        public GroupingSeparatorType groupingSeparatorType;

        /**
         * Nhom chu viet (Script Group)
         * LATIN = chu Latin (en, fr, de)
         * CYRILLIC = chu Cyrillic (ru, bg, uk)
         * CJK = chu Han/Nhat/Han (ja, zh, ko)
         * ARABIC = chu Arab (ar, fa)
         * HEBREW = chu Do Thai (he)
         * INDIC = chu An Do (hi, bn)
         */
        public ScriptGroup scriptGroup;

        /**
         * Nhom kiem tra (Test Group) - Phan vung tuong duong
         */
        public TestGroup testGroup;

        // ==================== ENUMS ====================

        public enum DecimalSeparatorType {
                DOT, // 1.00
                COMMA // 1,00
        }

        public enum GroupingSeparatorType {
                COMMA, // 1,000
                DOT, // 1.000
                SPACE, // 1 000
                NONE // 1000
        }

        public enum ScriptGroup {
                LATIN, // abc
                CYRILLIC, // абв
                CJK, // 日本語/中文/한국어
                ARABIC, // العربية
                HEBREW, // עברית
                INDIC, // हिन्दी
                THAI, // ไทย
                OTHER
        }

        public enum TestGroup {
                LATIN_DOT, // Nhom Latin dung dau cham (en, mx)
                LATIN_COMMA, // Nhom Latin dung dau phay (fr, vi, de)
                DOUBLE_BYTE, // Nhom ky tu kep (ja, zh, ko)
                RTL, // Nhom doc tu phai sang trai (ar, he, fa)
                CYRILLIC, // Nhom Cyrillic (ru, uk, bg)
                INDIC // Nhom An Do (hi, bn)
        }

        // ==================== CONSTRUCTORS ====================

        /**
         * Constructor cu (tuong thich nguoc)
         */
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

                // Tu dong xac dinh cac truong Decision Table
                this.decimalSeparatorType = decimal.equals(".") ? DecimalSeparatorType.DOT : DecimalSeparatorType.COMMA;
                this.groupingSeparatorType = determineGroupingSeparatorType(thousand);
                this.scriptGroup = determineScriptGroup(code);
                this.isRTL = isRTLLanguage(code);
                this.testGroup = determineTestGroup(code);
        }

        /**
         * Constructor day du voi cac truong Decision Table
         */
        public LanguageConfig(String code, String name, String primary, String secondary, String defaultCur,
                        String datePattern, String decimal, String thousand, String[] keywords,
                        boolean isRTL, DecimalSeparatorType decimalType, GroupingSeparatorType groupingType,
                        ScriptGroup scriptGroup, TestGroup testGroup) {
                this.code = code;
                this.languageName = name;
                this.primaryCurrency = primary;
                this.secondaryCurrency = secondary;
                this.defaultCurrency = defaultCur;
                this.datePattern = datePattern;
                this.decimalSeparator = decimal;
                this.thousandSeparator = thousand;
                this.expectedKeywords = keywords;
                this.isRTL = isRTL;
                this.decimalSeparatorType = decimalType;
                this.groupingSeparatorType = groupingType;
                this.scriptGroup = scriptGroup;
                this.testGroup = testGroup;
        }

        // ==================== HELPER METHODS ====================

        private GroupingSeparatorType determineGroupingSeparatorType(String separator) {
                if (separator == null || separator.isEmpty())
                        return GroupingSeparatorType.NONE;
                switch (separator) {
                        case ",":
                                return GroupingSeparatorType.COMMA;
                        case ".":
                                return GroupingSeparatorType.DOT;
                        case " ":
                                return GroupingSeparatorType.SPACE;
                        default:
                                return GroupingSeparatorType.NONE;
                }
        }

        private ScriptGroup determineScriptGroup(String code) {
                switch (code) {
                        case "ar":
                        case "fa":
                                return ScriptGroup.ARABIC;
                        case "he":
                                return ScriptGroup.HEBREW;
                        case "ru":
                        case "uk":
                        case "bg":
                        case "mk":
                        case "sr":
                                return ScriptGroup.CYRILLIC;
                        case "ja":
                        case "zh":
                        case "tw":
                        case "ko":
                                return ScriptGroup.CJK;
                        case "hi":
                        case "bn":
                                return ScriptGroup.INDIC;
                        case "th":
                                return ScriptGroup.THAI;
                        default:
                                return ScriptGroup.LATIN;
                }
        }

        private boolean isRTLLanguage(String code) {
                return code.equals("ar") || code.equals("he") || code.equals("fa");
        }

        private TestGroup determineTestGroup(String code) {
                // RTL languages
                if (isRTLLanguage(code))
                        return TestGroup.RTL;

                // Double-byte languages
                if (code.equals("ja") || code.equals("zh") || code.equals("tw") || code.equals("ko")) {
                        return TestGroup.DOUBLE_BYTE;
                }

                // Cyrillic languages
                if (code.equals("ru") || code.equals("uk") || code.equals("bg") || code.equals("mk")
                                || code.equals("sr")) {
                        return TestGroup.CYRILLIC;
                }

                // Indic languages
                if (code.equals("hi") || code.equals("bn")) {
                        return TestGroup.INDIC;
                }

                // Latin languages - check decimal separator
                if (decimalSeparator != null && decimalSeparator.equals(".")) {
                        return TestGroup.LATIN_DOT;
                }
                return TestGroup.LATIN_COMMA;
        }

        // ==================== DECISION TABLE GETTERS ====================

        /**
         * Lay tat ca ngon ngu theo Test Group (Equivalence Partitioning)
         */
        public static List<LanguageConfig> getByTestGroup(TestGroup group) {
                List<LanguageConfig> result = new ArrayList<>();
                for (LanguageConfig config : CONFIGS.values()) {
                        if (config.testGroup == group) {
                                result.add(config);
                        }
                }
                return result;
        }

        /**
         * Lay ngon ngu dai dien cho moi Test Group
         */
        public static Map<TestGroup, LanguageConfig> getRepresentativeLanguages() {
                Map<TestGroup, LanguageConfig> representatives = new HashMap<>();
                representatives.put(TestGroup.LATIN_DOT, CONFIGS.get("en"));
                representatives.put(TestGroup.LATIN_COMMA, CONFIGS.get("fr"));
                representatives.put(TestGroup.DOUBLE_BYTE, CONFIGS.get("ja"));
                representatives.put(TestGroup.RTL, CONFIGS.get("ar"));
                representatives.put(TestGroup.CYRILLIC, CONFIGS.get("ru"));
                representatives.put(TestGroup.INDIC, CONFIGS.get("hi"));
                return representatives;
        }

        /**
         * Lay danh sach ngon ngu theo Script Group
         */
        public static List<LanguageConfig> getByScriptGroup(ScriptGroup group) {
                List<LanguageConfig> result = new ArrayList<>();
                for (LanguageConfig config : CONFIGS.values()) {
                        if (config.scriptGroup == group) {
                                result.add(config);
                        }
                }
                return result;
        }

        /**
         * Lay tat ca ngon ngu RTL
         */
        public static List<LanguageConfig> getRTLLanguages() {
                List<LanguageConfig> result = new ArrayList<>();
                for (LanguageConfig config : CONFIGS.values()) {
                        if (config.isRTL) {
                                result.add(config);
                        }
                }
                return result;
        }

        /**
         * Nap cau hinh tu file JSON (Decision Table)
         */
        public static void loadFromJson(String filePath) throws IOException {
                try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)) {
                        Gson gson = new Gson();
                        Map<String, LanguageConfig> loaded = gson.fromJson(reader,
                                        new TypeToken<Map<String, LanguageConfig>>() {
                                        }.getType());
                        if (loaded != null) {
                                CONFIGS.putAll(loaded);
                        }
                }
        }

        /**
         * Xuat cau hinh ra file JSON
         */
        public static void exportToJson(String filePath) throws IOException {
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
                        Gson gson = new Gson();
                        gson.toJson(CONFIGS, writer);
                }
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
                                new String[] { "Dodaj u korpu", "Početna", "Odjeća", "Dodaci", "Pretraga", "Prijava",
                                                "Korpa" }));

                // Catalan - Català
                CONFIGS.put("ca", new LanguageConfig("ca", "Català", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Afegir al carret", "Inici", "Roba", "Accessoris", "Cercar",
                                                "Iniciar sessió",
                                                "Carret" }));

                // Danish - Dansk
                CONFIGS.put("da", new LanguageConfig("da", "Dansk", "kr", "DKK", "€",
                                "\\d{1,2}-\\d{1,2}-\\d{2,4}", ",", ".",
                                new String[] { "Læg i kurv", "Hjem", "Tøj", "Tilbehør", "Søg", "Log ind", "Kurv" }));

                // German - Deutsch
                CONFIGS.put("de", new LanguageConfig("de", "Deutsch", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "In den Warenkorb", "Startseite", "Kleidung", "Zubehör", "Suchen",
                                                "Anmelden",
                                                "Warenkorb" }));

                // English
                CONFIGS.put("en", new LanguageConfig("en", "English", "$", "£", null,
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "Add to cart", "Home", "Clothes", "Accessories", "Search", "Sign in",
                                                "Cart" }));

                // Spanish - Español
                CONFIGS.put("es", new LanguageConfig("es", "Español", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Añadir al carrito", "Inicio", "Ropa", "Accesorios", "Buscar",
                                                "Iniciar sesión",
                                                "Carrito" }));

                // Spanish Mexico - Español MX
                CONFIGS.put("mx", new LanguageConfig("mx", "Español MX", "$", "MXN", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "Añadir al carrito", "Inicio", "Ropa", "Accesorios", "Buscar",
                                                "Iniciar sesión",
                                                "Carrito" }));

                // Estonian - Eesti keel
                CONFIGS.put("et", new LanguageConfig("et", "Eesti keel", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Lisa korvi", "Avaleht", "Riided", "Aksessuaarid", "Otsi", "Logi sisse",
                                                "Korv" }));

                // French - Français
                CONFIGS.put("fr", new LanguageConfig("fr", "Français", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", " ",
                                new String[] { "Ajouter au panier", "Accueil", "Vêtements", "Accessoires", "Rechercher",
                                                "Connexion",
                                                "Panier" }));

                // French Canada - Français CA
                CONFIGS.put("qc", new LanguageConfig("qc", "Français CA", "$", "CAD", "€",
                                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                                new String[] { "Ajouter au panier", "Accueil", "Vêtements", "Accessoires", "Rechercher",
                                                "Connexion",
                                                "Panier" }));

                // Galician - Galego
                CONFIGS.put("gl", new LanguageConfig("gl", "Galego", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Engadir ao carro", "Inicio", "Roupa", "Accesorios", "Buscar",
                                                "Iniciar sesión",
                                                "Carro" }));

                // Croatian - Hrvatski
                CONFIGS.put("hr", new LanguageConfig("hr", "Hrvatski", "€", "kn", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Dodaj u košaricu", "Početna", "Odjeća", "Dodaci", "Traži", "Prijava",
                                                "Košarica" }));

                // Indonesian - Indonesia
                CONFIGS.put("id", new LanguageConfig("id", "Indonesia", "Rp", "IDR", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Tambah ke keranjang", "Beranda", "Pakaian", "Aksesoris", "Cari",
                                                "Masuk",
                                                "Keranjang" }));

                // Italian - Italiano
                CONFIGS.put("it", new LanguageConfig("it", "Italiano", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Aggiungi al carrello", "Home", "Abbigliamento", "Accessori", "Cerca",
                                                "Accedi",
                                                "Carrello" }));

                // Latvian - Latviešu
                CONFIGS.put("lv", new LanguageConfig("lv", "Latviešu", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Pievienot grozam", "Sākums", "Apģērbs", "Aksesuāri", "Meklēt",
                                                "Pieslēgties",
                                                "Grozs" }));

                // Hungarian - Magyar
                CONFIGS.put("hu", new LanguageConfig("hu", "Magyar", "Ft", "HUF", "€",
                                "\\d{4}\\.\\d{1,2}\\.\\d{1,2}", ",", " ",
                                new String[] { "Kosárba", "Főoldal", "Ruházat", "Kiegészítők", "Keresés",
                                                "Bejelentkezés", "Kosár" }));

                // Dutch - Nederlands
                CONFIGS.put("nl", new LanguageConfig("nl", "Nederlands", "€", "€", "€",
                                "\\d{1,2}-\\d{1,2}-\\d{2,4}", ",", ".",
                                new String[] { "In winkelwagen", "Home", "Kleding", "Accessoires", "Zoeken", "Inloggen",
                                                "Winkelwagen" }));

                // Norwegian - Norsk
                CONFIGS.put("no", new LanguageConfig("no", "Norsk", "kr", "NOK", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Legg i handlekurv", "Hjem", "Klær", "Tilbehør", "Søk", "Logg inn",
                                                "Handlekurv" }));

                // Polish - Polski
                CONFIGS.put("pl", new LanguageConfig("pl", "Polski", "zł", "PLN", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Dodaj do koszyka", "Strona główna", "Ubrania", "Akcesoria", "Szukaj",
                                                "Zaloguj się",
                                                "Koszyk" }));

                // Portuguese - Português
                CONFIGS.put("pt", new LanguageConfig("pt", "Português", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Adicionar ao carrinho", "Início", "Roupas", "Acessórios", "Pesquisar",
                                                "Entrar",
                                                "Carrinho" }));

                // Portuguese Brazil - Português BR
                CONFIGS.put("br", new LanguageConfig("br", "Português BR", "R$", "BRL", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Adicionar ao carrinho", "Início", "Roupas", "Acessórios", "Pesquisar",
                                                "Entrar",
                                                "Carrinho" }));

                // Romanian - Română
                CONFIGS.put("ro", new LanguageConfig("ro", "Română", "lei", "RON", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Adaugă în coș", "Acasă", "Îmbrăcăminte", "Accesorii", "Căutare",
                                                "Autentificare",
                                                "Coș" }));

                // Albanian - Shqip
                CONFIGS.put("sq", new LanguageConfig("sq", "Shqip", "L", "ALL", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Shto në shportë", "Ballina", "Veshje", "Aksesorë", "Kërko", "Hyr",
                                                "Shporta" }));

                // Slovak - Slovenčina
                CONFIGS.put("sk", new LanguageConfig("sk", "Slovenčina", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Pridať do košíka", "Domov", "Oblečenie", "Doplnky", "Hľadať",
                                                "Prihlásiť sa",
                                                "Košík" }));

                // Serbian - Srpski
                CONFIGS.put("sr", new LanguageConfig("sr", "Srpski", "RSD", "дин", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Dodaj u korpu", "Početna", "Odeća", "Dodaci", "Pretraga", "Prijava",
                                                "Korpa" }));

                // Finnish - Suomi
                CONFIGS.put("fi", new LanguageConfig("fi", "Suomi", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Lisää ostoskoriin", "Etusivu", "Vaatteet", "Asusteet", "Haku",
                                                "Kirjaudu",
                                                "Ostoskori" }));

                // Swedish - Svenska
                CONFIGS.put("sv", new LanguageConfig("sv", "Svenska", "kr", "SEK", "€",
                                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                                new String[] { "Lägg i kundvagn", "Hem", "Kläder", "Tillbehör", "Sök", "Logga in",
                                                "Kundvagn" }));

                // Turkish - Türkçe
                CONFIGS.put("tr", new LanguageConfig("tr", "Türkçe", "₺", "TL", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Sepete ekle", "Ana Sayfa", "Giyim", "Aksesuar", "Ara", "Giriş yap",
                                                "Sepet" }));

                // Lithuanian - Lietuvių
                CONFIGS.put("lt", new LanguageConfig("lt", "Lietuvių", "€", "€", "€",
                                "\\d{4}-\\d{1,2}-\\d{1,2}", ",", " ",
                                new String[] { "Į krepšelį", "Pradžia", "Drabužiai", "Aksesuarai", "Ieškoti",
                                                "Prisijungti",
                                                "Krepšelis" }));

                // Slovenian - Slovenščina
                CONFIGS.put("sl", new LanguageConfig("sl", "Slovenščina", "€", "€", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Dodaj v košarico", "Domov", "Oblačila", "Dodatki", "Išči", "Prijava",
                                                "Košarica" }));

                // Vietnamese - Tiếng Việt
                CONFIGS.put("vi", new LanguageConfig("vi", "Tiếng Việt", "₫", "đ", "VND",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Thêm vào giỏ", "Trang chủ", "Quần áo", "Phụ kiện", "Tìm kiếm",
                                                "Đăng nhập",
                                                "Giỏ hàng" }));

                // Czech - Čeština
                CONFIGS.put("cs", new LanguageConfig("cs", "Čeština", "Kč", "CZK", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Přidat do košíku", "Domů", "Oblečení", "Doplňky", "Hledat",
                                                "Přihlásit se", "Košík" }));

                // Greek - Ελληνικά
                CONFIGS.put("el", new LanguageConfig("el", "Ελληνικά", "€", "€", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ",", ".",
                                new String[] { "Προσθήκη στο καλάθι", "Αρχική", "Ρούχα", "Αξεσουάρ", "Αναζήτηση",
                                                "Σύνδεση",
                                                "Καλάθι" }));

                // Ukrainian - Українська
                CONFIGS.put("uk", new LanguageConfig("uk", "Українська", "₴", "грн", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Додати до кошика", "Головна", "Одяг", "Аксесуари", "Пошук", "Увійти",
                                                "Кошик" }));

                // Russian - Русский
                CONFIGS.put("ru", new LanguageConfig("ru", "Русский", "₽", "руб", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "В корзину", "Главная", "Одежда", "Аксессуары", "Поиск", "Войти",
                                                "Корзина" }));

                // Bulgarian - Български
                CONFIGS.put("bg", new LanguageConfig("bg", "Български", "лв", "BGN", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", " ",
                                new String[] { "Добави в кошницата", "Начало", "Дрехи", "Аксесоари", "Търсене", "Вход",
                                                "Кошница" }));

                // Macedonian - Македонски
                CONFIGS.put("mk", new LanguageConfig("mk", "Македонски", "ден", "MKD", "€",
                                "\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}", ",", ".",
                                new String[] { "Додај во кошничка", "Почетна", "Облека", "Додатоци", "Барај", "Најава",
                                                "Кошничка" }));

                // Hebrew - עברית
                CONFIGS.put("he", new LanguageConfig("he", "עברית", "₪", "ש״ח", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "הוסף לסל", "דף הבית", "בגדים", "אביזרים", "חיפוש", "התחברות",
                                                "סל קניות" }));

                // Persian - فارسی
                CONFIGS.put("fa", new LanguageConfig("fa", "فارسی", "﷼", "ریال", "€",
                                "\\d{4}/\\d{1,2}/\\d{1,2}", ".", ",",
                                new String[] { "افزودن به سبد", "خانه", "لباس", "لوازم جانبی", "جستجو", "ورود",
                                                "سبد خرید" }));

                // Hindi - हिन्दी
                CONFIGS.put("hi", new LanguageConfig("hi", "हिन्दी", "₹", "रु", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "कार्ट में जोड़ें", "होम", "कपड़े", "सहायक उपकरण", "खोजें", "लॉग इन",
                                                "कार्ट" }));

                // Bengali - বাংলা
                CONFIGS.put("bn", new LanguageConfig("bn", "বাংলা", "৳", "টাকা", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "কার্টে যোগ করুন", "হোম", "পোশাক", "আনুষাঙ্গিক", "অনুসন্ধান", "লগইন",
                                                "কার্ট" }));

                // Arabic - العربية
                CONFIGS.put("ar", new LanguageConfig("ar", "العربية", "د.إ", "ر.س", "€",
                                "\\d{1,2}/\\d{1,2}/\\d{2,4}", ".", ",",
                                new String[] { "أضف إلى السلة", "الرئيسية", "ملابس", "إكسسوارات", "بحث", "تسجيل الدخول",
                                                "السلة" }));

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
