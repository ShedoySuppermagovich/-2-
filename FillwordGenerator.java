import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

// ===== Часть 1. Валента Артём: модель данных, проверка ввода и словарь =====

enum Direction {
    UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1);

    private final int dRow;
    private final int dCol;

    // dRow, dCol - смещение соседней клетки по строке и столбцу
    Direction(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    int getDRow() {
        return dRow;
    }

    int getDCol() {
        return dCol;
    }
}

class Cell {
    private final int row;
    private final int col;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    int getRow() {
        return row;
    }

    int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "(" + (row + 1) + "," + (col + 1) + ")";
    }
}

class FillwordException extends Exception {
    FillwordException(String message) {
        super(message);
    }
}

class InputParser {
    static int parseSize(String text) throws FillwordException {
        String s = text.trim();
        if (!s.matches("[0-9]+")) {
            throw new FillwordException("Введите целое положительное число.");
        }
        int size = s.length() > 2 ? 0 : Integer.parseInt(s);
        if (size < 3 || size > 6) {
            throw new FillwordException("Можно выбрать только 3, 4, 5 или 6.");
        }
        return size;
    }
}

class WordDictionary {
    private static final int MAX_ATTEMPTS = 1000;
    private static final String[] WORDS = {
            "абрикос", "абсурд", "авто", "автобус", "автор", "агент", "ад", "адрес",
            "азот", "аксон", "актер", "акция", "алгоритм", "алоэ", "алый", "ампер",
            "амулет", "ангел", "анекдот", "анис", "апельсин", "арбуз", "армия", "архив",
            "ас", "аскет", "ацтек", "бадья", "базис", "бак", "бал", "балет",
            "балкон", "бампер", "банк", "бант", "бар", "барин", "бас", "басня",
            "батл", "батон", "бачок", "башня", "баян", "бег", "бегство", "бедро",
            "бездна", "бекон", "белка", "белок", "белый", "беляш", "бетон", "бидон",
            "бизон", "билет", "бинокль", "бином", "бинт", "биржа", "бирка", "бисер",
            "бит", "бита", "битва", "благо", "блеск", "блик", "блин", "блиц",
            "блог", "блок", "блоха", "блюдо", "богач", "боец", "бой", "бок",
            "бокал", "бокс", "бонус", "борец", "борщ", "бот", "ботва", "бочка",
            "брак", "брат", "бревно", "бремя", "брешь", "бровь", "броня", "брус",
            "брусника", "брюки", "буй", "буква", "букет", "булка", "бульдозер", "бунт",
            "бур", "бурый", "буря", "бутылка", "буфер", "буфет", "бухта", "бык",
            "быт", "вагон", "важность", "валенок", "валик", "вальс", "варяг", "вафля",
            "вброс", "ведро", "век", "вена", "верность", "верфь", "вес", "весло",
            "весна", "вести", "весть", "ветка", "вещь", "взаимность", "вздор", "вздох",
            "взлет", "взлом", "взмах", "взрослый", "вид", "видео", "вилка", "виноград",
            "винт", "виола", "вираж", "вирус", "вис", "висок", "виток", "вихрь",
            "вишня", "вклад", "вкус", "вниз", "внук", "вода", "воз", "воздух",
            "воин", "волк", "вольт", "воля", "вор", "ворс", "воск", "враг",
            "врач", "вред", "время", "вслух", "встреча", "вуз", "вход", "вчера",
            "вширь", "въезд", "выбор", "выгода", "выдра", "выкуп", "вырез", "высота",
            "выход", "вышка", "вьюга", "газ", "газон", "галстук", "гам", "ганец",
            "гармония", "гвоздь", "гелий", "гений", "герб", "герой", "герц", "гид",
            "гиена", "гимнаст", "гипс", "гиря", "глаз", "глобус", "глушь", "гнев",
            "гнездо", "гном", "год", "гол", "голый", "гончар", "гора", "горы",
            "гость", "градусник", "грант", "граф", "грач", "грейп", "гриб", "гриль",
            "гроза", "гром", "грот", "грудь", "грузовик", "груша", "грязь", "гуашь",
            "губа", "губка", "губы", "гудок", "гул", "гурман", "гусар", "гусь",
            "даль", "дан", "дань", "дар", "два", "дверь", "двор", "дворец",
            "дева", "девиз", "девушка", "дело", "демонстрация", "день", "депо", "десант",
            "дети", "дефис", "джаз", "джем", "диагноз", "диалог", "диван", "диета",
            "диплом", "длина", "дно", "док", "документ", "долг", "долина", "доля",
            "дом", "домик", "доска", "досуг", "дот", "дочка", "дочь", "дракон",
            "древо", "дробь", "дрова", "дрожь", "друг", "дружба", "дуб", "дуга",
            "дуло", "дурак", "дух", "духи", "душ", "душа", "дуэль", "дым",
            "дыня", "дырка", "дыхание", "дюйм", "дюна", "дюшес", "дятел", "евкалипт",
            "евнух", "евро", "еда", "ель", "емкость", "енот", "ёрш", "жабры",
            "жакет", "жало", "жгут", "жезл", "желудок", "желудь", "жена", "жертва",
            "жест", "жизнь", "жилет", "жир", "жираф", "жрица", "жук", "журналист",
            "жюри", "забег", "забор", "завод", "загиб", "загс", "закон", "зал",
            "залив", "замок", "занос", "заруб", "засев", "затея", "заход", "заяц",
            "звено", "зверь", "звон", "звук", "здание", "зебра", "земля", "зенит",
            "зерно", "зефир", "зима", "зло", "злой", "змея", "знак", "знамя",
            "зов", "зона", "зонд", "зонт", "зритель", "зуб", "зубр", "зуд",
            "зять", "ива", "иволга", "игла", "иго", "иголка", "игра", "игрок",
            "игрушка", "идеал", "идея", "изба", "изюм", "икра", "имя", "иней",
            "ион", "иск", "искра", "исток", "исход", "итог", "июль", "июнь",
            "йога", "йод", "кабинет", "кабриолет", "кадет", "кадр", "казус", "калым",
            "камень", "камин", "камыш", "капля", "капот", "карп", "картофель", "карусель",
            "кастрюля", "кат", "катер", "катод", "кафе", "кафель", "качели", "квант",
            "кварц", "кварцит", "квас", "квота", "кедр", "кефир", "километр", "кино",
            "кинозал", "кисть", "кит", "клад", "клан", "клёв", "клей", "клён",
            "клещ", "клещи", "клич", "клон", "клоун", "клуб", "клюв", "ключ",
            "книга", "кнут", "князь", "ковёр", "ковш", "код", "кожа", "коза",
            "козёл", "кол", "кола", "колба", "колун", "комар", "комбайн", "комедия",
            "конверт", "конец", "конус", "конфета", "конь", "копьё", "кор", "корабль",
            "корд", "корж", "корзина", "корм", "корт", "коса", "кость", "костюм",
            "кот", "кофе", "кофта", "краб", "край", "кран", "кредит", "крем",
            "кресло", "крест", "кров", "кровь", "крот", "круг", "круиз", "крупа",
            "крыло", "крыша", "куб", "кувшин", "кулан", "кулон", "культ", "кум",
            "кума", "кумир", "кумыс", "купе", "купец", "купол", "купон", "курица",
            "куст", "кухня", "куча", "кучер", "куш", "кювет", "лагерь", "лад",
            "ладонь", "ладья", "лазер", "лайм", "лак", "лакей", "ландыш", "лань",
            "лапти", "ларь", "ласт", "ласты", "лгун", "лев", "левша", "левый",
            "лед", "леди", "лейка", "лекарство", "лекарь", "лента", "лень", "леопард",
            "лепта", "лес", "лесник", "лестница", "лесть", "лето", "летчик", "лечо",
            "лжец", "ливер", "лига", "лидер", "лик", "лимон", "лимонад", "линза",
            "липа", "лис", "лиса", "лист", "листва", "литр", "литьё", "лифт",
            "лихач", "лихой", "лицей", "лицо", "лоб", "лобстер", "ловец", "ловушка",
            "лодка", "ложе", "ложка", "ложь", "лоза", "лом", "ломик", "лорд",
            "лоск", "лось", "лошадь", "луг", "луга", "лужа", "лук", "луна",
            "лунка", "лупа", "луч", "лыжа", "лыжи", "лыжня", "льдина", "любовь",
            "люди", "люк", "люстра", "лягушка", "ляшка", "лёд", "маг", "магия",
            "мажор", "мазут", "мазь", "май", "майор", "мак", "макет", "малёк",
            "малец", "малыш", "маляр", "манго", "манеж", "марс", "март", "масло",
            "масть", "мат", "матч", "мафия", "маяк", "мгла", "мёд", "медик",
            "медок", "медь", "мел", "мель", "мельница", "меню", "место", "месть",
            "месяц", "метан", "метка", "метод", "метр", "метро", "мех", "меч",
            "мечта", "мешок", "миг", "миля", "мина", "минус", "мир", "мираж",
            "миска", "миф", "мода", "мозг", "мозги", "мойва", "мойка", "моль",
            "моляр", "монах", "мопед", "мопс", "мор", "море", "морж", "морс",
            "моряк", "мост", "мотив", "мох", "мрак", "муж", "мужик", "муза",
            "музей", "музыка", "мука", "муляж", "мусор", "муха", "мыло", "мыс",
            "мысль", "мышка", "мышца", "мышь", "мэр", "мэрия", "мясо", "мята",
            "мятеж", "мяч", "набег", "навык", "налёт", "налог", "намёк", "нарды",
            "народ", "нарыв", "наряд", "нация", "наяву", "небо", "недуг", "нерв",
            "нефть", "ник", "нить", "ничья", "ниша", "нога", "ноги", "нож",
            "ножик", "ноль", "номер", "нора", "норка", "нос", "носки", "нота",
            "ночь", "ноша", "нюх", "оазис", "обвал", "обед", "обжиг", "обжим",
            "обида", "облик", "обман", "образ", "обруч", "обрыв", "обряд", "обувь",
            "обуза", "обыск", "овал", "овен", "овраг", "овёс", "ода", "одёжа",
            "один", "океан", "окись", "оклад", "окрас", "округ", "окунь", "олень",
            "ольха", "омар", "омега", "омлет", "омут", "опека", "опера", "опись",
            "опыт", "орган", "орда", "орден", "орех", "орёл", "оса", "осень",
            "осётр", "осина", "оскал", "ось", "осёл", "отвал", "отвар", "отдых",
            "отель", "отец", "отжим", "отзыв", "отказ", "откуп", "отлив", "отрыв",
            "отряд", "охват", "очи", "очки", "падеж", "паз", "пай", "пакет",
            "пакт", "палец", "пальто", "пар", "пари", "парик", "парк", "паром",
            "парус", "пас", "пасть", "пат", "паук", "пафос", "пашня", "паять",
            "паёк", "пегас", "пекло", "пена", "пенал", "пень", "пёс", "пески",
            "песня", "песок", "петля", "петух", "печка", "печь", "пешка", "пик",
            "пика", "пикет", "пилот", "пинта", "пир", "пират", "пирог", "пирон",
            "писец", "письмо", "пить", "пихта", "пища", "пламя", "плато", "плед",
            "племя", "плеть", "плечи", "плечо", "плита", "плог", "плуг", "плюс",
            "плющ", "побег", "повар", "поезд", "пожар", "поза", "поиск", "показ",
            "пол", "поле", "полис", "полк", "полка", "полюс", "пора", "порт",
            "порыв", "посев", "пост", "пот", "почва", "почка", "почта", "почёт",
            "поэма", "поэт", "пояс", "право", "праздник", "приз", "принц", "приют",
            "проба", "проза", "проём", "пруд", "прыг", "прядь", "пряжа", "пряха",
            "псих", "птица", "пульс", "пульт", "пуля", "пункт", "пурга", "пуск",
            "путь", "пуфик", "пушка", "пчела", "пшено", "пыл", "пыль", "пьеса",
            "пюре", "пята", "пятак", "пятка", "пятно", "пять", "раб", "рагу",
            "радий", "радио", "радон", "разум", "рай", "район", "рак", "ранг",
            "ранец", "ранчо", "рапид", "рация", "рвота", "ребус", "редис", "редут",
            "режим", "резак", "рейс", "река", "рельс", "рента", "репа", "репка",
            "речка", "речь", "решка", "ринг", "рис", "риск", "рисунок", "ритм",
            "рифма", "роба", "ров", "ровня", "рог", "род", "родня", "роды",
            "рожь", "роза", "розга", "рой", "рок", "ролик", "роль", "ром",
            "роман", "ромб", "роса", "рост", "рот", "рота", "роща", "рубеж",
            "рубин", "рубль", "руда", "ружьё", "руины", "рука", "рукав", "рулет",
            "рулон", "руль", "руно", "русло", "русый", "ручей", "ручка", "рыба",
            "рыбак", "рыбка", "рыжий", "рык", "рынок", "рысь", "рытьё", "рычаг",
            "рюмка", "рябь", "ряд", "ряска", "сабля", "сад", "садик", "сакля",
            "салки", "сало", "салон", "салют", "самбо", "самец", "самолёт", "сапог",
            "сапёр", "сачок", "сборы", "сброд", "сверх", "свет", "свеча", "свита",
            "свой", "свора", "сдвиг", "сезам", "сезон", "секта", "село", "семь",
            "семья", "сенат", "сено", "сера", "серия", "серп", "серум", "серый",
            "сетка", "сеть", "сечь", "сигма", "сила", "синяк", "сироп", "сито",
            "сифон", "скат", "сквер", "склад", "склеп", "скот", "след", "слеза",
            "слив", "слива", "слизь", "слог", "слой", "слон", "слуга", "слух",
            "слюна", "смена", "смерч", "смета", "смех", "смола", "смотр", "смрад",
            "смута", "снег", "сноб", "сова", "совет", "сок", "солнце", "соль",
            "сом", "сон", "сор", "сорт", "сота", "сотка", "сотня", "софит",
            "соха", "сошка", "союз", "спазм", "спать", "спина", "спирт", "сплав",
            "спор", "спорт", "споры", "спрут", "среда", "срок", "стадо", "стаж",
            "сталь", "стая", "ствол", "стек", "стела", "стиль", "стих", "сто",
            "стог", "сток", "стол", "столб", "столп", "стопа", "страж", "страх",
            "стриж", "строй", "струя", "стужа", "стук", "стул", "ступа", "стыд",
            "стык", "суд", "судно", "судья", "сук", "сумка", "суп", "сурок",
            "сутки", "суфле", "сучок", "сушка", "сфера", "схема", "сцена", "сын",
            "сыпь", "сыр", "сырок", "сырьё", "сэр", "сюжет", "сёмга", "табло",
            "табун", "таз", "тазик", "тайм", "такси", "талон", "тальк", "танго",
            "танец", "танк", "тапки", "тариф", "тезис", "тело", "телок", "тема",
            "темп", "темя", "тень", "тесак", "тигр", "тик", "тина", "тип",
            "типаж", "тир", "тираж", "тиран", "тишь", "ткань", "тмин", "товар",
            "ток", "толпа", "том", "тон", "тонус", "топ", "топаз", "торий",
            "торс", "тоска", "точка", "трал", "транс", "трек", "три", "трико",
            "трио", "тромб", "трон", "тропа", "трос", "труба", "труд", "трус",
            "трусы", "труха", "трюк", "трюм", "туба", "туз", "тук", "туман",
            "тумба", "тупик", "тур", "турне", "туфля", "туча", "тучка", "туша",
            "тушь", "тыква", "тыл", "тьма", "тяга", "тяпка", "тёлка", "тёща",
            "увал", "угол", "уголь", "угорь", "удав", "удаль", "удар", "ужин",
            "узел", "узкий", "узник", "узор", "указ", "уклад", "укол", "укроп",
            "улика", "улица", "улов", "ум", "умный", "уран", "урна", "урюк",
            "ус", "успех", "уста", "устав", "устой", "устье", "усы", "утеря",
            "утеха", "утиль", "утка", "утро", "утюг", "утёс", "уха", "ухват",
            "ухо", "уход", "учёба", "ученик", "учить", "уши", "ушиб", "ушко",
            "ущерб", "уют", "фагот", "факел", "факт", "фарш", "фасон", "фейк",
            "фен", "ферзь", "ферма", "фея", "фикус", "фильм", "финал", "финт",
            "фирма", "фишка", "флаг", "фланг", "флинт", "флора", "флот", "фляга",
            "фобия", "фонарь", "фонд", "фора", "форма", "форт", "фрак", "фреза",
            "фронт", "фрукт", "фугас", "фунт", "фура", "фураж", "хакер", "хан",
            "хаос", "хват", "хвост", "хвоя", "хлам", "хлеб", "хлор", "ход",
            "холм", "холст", "хомут", "хомяк", "хор", "хорда", "хорей", "хорёк",
            "храм", "храп", "хрен", "хром", "хруст", "хрящ", "хурма", "цапля",
            "царь", "цвет", "цветок", "цветы", "цезий", "цель", "цена", "цент",
            "центр", "цепь", "цикл", "цинк", "цирк", "цукат", "чай", "чан",
            "час", "часть", "часы", "червь", "черта", "честь", "чехол", "чешуя",
            "чиж", "чин", "чипсы", "число", "чокер", "чтец", "чудо", "чужой",
            "чуйка", "чулан", "чулки", "чулок", "чум", "чушь", "шаг", "шанс",
            "шар", "шарик", "шарм", "шарф", "шатун", "шатёр", "шах", "швея",
            "шельф", "шест", "шесть", "шеф", "шея", "шило", "шина", "шип",
            "ширма", "шитьё", "шифер", "шифр", "шкаф", "шквал", "шкода", "школа",
            "шкура", "шлак", "шланг", "шлейф", "шляпа", "шлях", "шнур", "шок",
            "шорты", "шофёр", "шпик", "шпиль", "шпион", "шпон", "шпора", "шприц",
            "шрам", "штаб", "штамп", "штиль", "штора", "шторм", "шторы", "штраф",
            "штрих", "штука", "штурм", "штык", "штырь", "шуба", "шулер", "шум",
            "шурин", "шурпа", "шут", "шхуна", "шёлк", "шёпот", "щека", "щель",
            "щенок", "щепа", "щит", "щука", "щуп", "щётка", "экран", "элита",
            "эльф", "эмаль", "эпик", "эпос", "эпоха", "эра", "эскиз", "этаж",
            "этап", "этюд", "эфир", "эхо", "юбка", "юг", "юзер", "юла",
            "юмор", "юнга", "юниор", "юрист", "юрта", "ябеда", "явка", "явь",
            "ягель", "ягода", "яд", "ядро", "язык", "яйцо", "якорь", "яма",
            "ямб", "ямщик", "ярд", "яркий", "ярлык", "ярус", "ясень", "ясный",
            "яхта", "ящер", "ящик", "ёж", "ёжик", "ёлка", "ехидна", "ил",
            "релиз", "трель", "литер", "набой", "кома", "найм", "ом", "бон",
            "медяк", "демо", "ком", "як", "мод", "аул", "метла", "мулат",
            "метал", "амбре", "мера", "наезд", "езда", "зад", "верно", "бор",
            "дзен", "ген", "стенд", "десна", "стена", "стан", "сет", "идол",
            "плод", "диез", "низ", "чек", "дышла"
    };

    static int size() {
        return WORDS.length;
    }

    static String[] pick(int size, Random random) throws FillwordException {
        int target = size * size;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String[] chosen = new String[target];
            int count = 0;
            int total = 0;
            for (int index : shuffled(WORDS.length, random)) {
                int length = WORDS[index].length();
                if (total + length > target) {
                    continue;
                }
                chosen[count++] = WORDS[index].toUpperCase();
                total += length;
                if (total == target) {
                    break;
                }
            }
            if (total == target) {
                String[] result = new String[count];
                System.arraycopy(chosen, 0, result, 0, count);
                return result;
            }
        }
        throw new FillwordException("Не удалось подобрать слова из словаря.");
    }

    private static int[] shuffled(int n, Random random) {
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
    }
}

// ===== Часть 2. Копасов Савелий: алгоритм размещения слов =====

class FillwordSolver {
    private static final char EMPTY = '\0';
    private static final long DEFAULT_TIME_LIMIT_MS = 20000;
    private static final long CHECK_EVERY = 4096;
    private static final long START_BUDGET = 3000;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final int size;
    private final String[] words;
    private final int[] order;
    private final boolean[][] reachable;
    private final long timeLimitMs;
    private final Random random;
    private final char[][] grid;
    private final Cell[][] paths;

    private long deadline;
    private long nodes;
    private long attemptEnd;
    private boolean cutOff;
    private boolean timedOut;

    // size - сторона сетки, words - слова в верхнем регистре,
    // timeLimitMs - предельное время поиска в миллисекундах,
    // random - источник случайности (одинаковое зерно даёт одинаковый филворд)
    FillwordSolver(int size, String[] words, long timeLimitMs, Random random) {
        this.size = size;
        this.words = words;
        this.timeLimitMs = timeLimitMs;
        this.random = random;
        this.grid = new char[size][size];
        this.paths = new Cell[words.length][];
        this.order = orderByLength(words);
        this.reachable = buildReachable();
    }

    // Случайное расположение при каждом запуске
    FillwordSolver(int size, String[] words, long timeLimitMs) {
        this(size, words, timeLimitMs, new Random());
    }

    // Лимит времени по умолчанию
    FillwordSolver(int size, String[] words) {
        this(size, words, DEFAULT_TIME_LIMIT_MS);
    }

    private int[] orderByLength(String[] words) {
        int[] order = shuffled(words.length);
        for (int i = 1; i < order.length; i++) {
            int current = order[i];
            int j = i - 1;
            while (j >= 0 && words[order[j]].length() < words[current].length()) {
                order[j + 1] = order[j];
                j--;
            }
            order[j + 1] = current;
        }
        return order;
    }

    private boolean[][] buildReachable() {
        int cells = size * size;
        boolean[][] result = new boolean[order.length + 1][cells + 1];
        result[order.length][0] = true;
        for (int k = order.length - 1; k >= 0; k--) {
            int len = words[order[k]].length();
            for (int s = 0; s <= cells; s++) {
                result[k][s] = result[k + 1][s] || (s >= len && result[k + 1][s - len]);
            }
        }
        return result;
    }

    boolean solve() {
        deadline = System.currentTimeMillis() + timeLimitMs;
        nodes = 0;
        timedOut = false;

        long budget = START_BUDGET;
        while (!timedOut) {
            clear();
            attemptEnd = nodes + budget;
            cutOff = false;
            if (placeWord(0)) {
                return true;
            }
            if (!cutOff) {
                return false;
            }
            budget *= 2;
        }
        return false;
    }

    private void clear() {
        for (char[] row : grid) {
            Arrays.fill(row, EMPTY);
        }
        for (int i = 0; i < paths.length; i++) {
            paths[i] = null;
        }
    }

    private boolean placeWord(int k) {
        if (k == order.length) {
            return true;
        }
        if (!regionsFeasible(k)) {
            return false;
        }

        int wordIndex = order[k];
        String word = words[wordIndex];

        for (int start : shuffled(size * size)) {
            int r = start / size;
            int c = start % size;
            if (grid[r][c] != EMPTY) {
                continue;
            }
            Cell[] path = new Cell[word.length()];
            path[0] = new Cell(r, c);
            paths[wordIndex] = path;
            grid[r][c] = word.charAt(0);

            if (extendPath(k, word, 1, path)) {
                return true;
            }
            grid[r][c] = EMPTY;
            if (timedOut || cutOff) {
                paths[wordIndex] = null;
                return false;
            }
        }
        paths[wordIndex] = null;
        return false;
    }

    private boolean extendPath(int k, String word, int pos, Cell[] path) {
        if (++nodes % CHECK_EVERY == 0 && System.currentTimeMillis() > deadline) {
            timedOut = true;
        }
        if (nodes > attemptEnd) {
            cutOff = true;
        }
        if (timedOut || cutOff) {
            return false;
        }
        if (pos == word.length()) {
            return placeWord(k + 1);
        }

        Cell last = path[pos - 1];
        for (int index : shuffled(DIRECTIONS.length)) {
            Direction d = DIRECTIONS[index];
            int nr = last.getRow() + d.getDRow();
            int nc = last.getCol() + d.getDCol();
            if (!isFree(nr, nc)) {
                continue;
            }
            grid[nr][nc] = word.charAt(pos);
            path[pos] = new Cell(nr, nc);

            if (extendPath(k, word, pos + 1, path)) {
                return true;
            }
            grid[nr][nc] = EMPTY;
        }
        return false;
    }

    private boolean regionsFeasible(int k) {
        boolean[][] seen = new boolean[size][size];
        int[] stack = new int[size * size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!isFree(r, c) || seen[r][c]) {
                    continue;
                }
                int top = 0;
                int count = 0;
                stack[top++] = r * size + c;
                seen[r][c] = true;

                while (top > 0) {
                    int cur = stack[--top];
                    count++;
                    for (Direction d : DIRECTIONS) {
                        int nr = cur / size + d.getDRow();
                        int nc = cur % size + d.getDCol();
                        if (isFree(nr, nc) && !seen[nr][nc]) {
                            seen[nr][nc] = true;
                            stack[top++] = nr * size + nc;
                        }
                    }
                }
                if (!reachable[k][count]) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[] shuffled(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
    }

    private boolean isFree(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size && grid[r][c] == EMPTY;
    }

    char[][] getGrid() {
        return grid;
    }

    Cell[][] getPaths() {
        return paths;
    }

    long getNodes() {
        return nodes;
    }

    boolean isTimedOut() {
        return timedOut;
    }
}

// ===== Часть 3. Родионов Ростислав: проверка результата, вывод и запуск =====

class FillwordValidator {
    static boolean isValid(char[][] grid, String[] words, Cell[][] paths) {
        int size = grid.length;
        boolean[][] used = new boolean[size][size];

        for (int i = 0; i < words.length; i++) {
            Cell[] path = paths[i];
            if (path == null || path.length != words[i].length()) {
                return false;
            }
            for (int j = 0; j < path.length; j++) {
                if (path[j] == null) {
                    return false;
                }
                int r = path[j].getRow();
                int c = path[j].getCol();
                if (used[r][c] || grid[r][c] != words[i].charAt(j)) {
                    return false;
                }
                used[r][c] = true;
                if (j > 0 && Math.abs(path[j - 1].getRow() - r)
                        + Math.abs(path[j - 1].getCol() - c) != 1) {
                    return false;
                }
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!used[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }
}

class FillwordPrinter {
    static void print(char[][] grid) {
        System.out.println("Сетка:");
        for (char[] row : grid) {
            StringBuilder sb = new StringBuilder();
            for (char ch : row) {
                sb.append(ch).append("  ");
            }
            System.out.println(sb);
        }
    }

    static void print(Cell[][] paths, int size) {
        int[][] owner = new int[size][size];
        for (int i = 0; i < paths.length; i++) {
            for (Cell cell : paths[i]) {
                owner[cell.getRow()][cell.getCol()] = i + 1;
            }
        }
        System.out.println("\nНомера слов в клетках:");
        for (int[] row : owner) {
            StringBuilder sb = new StringBuilder();
            for (int value : row) {
                sb.append(String.format("%-3d", value));
            }
            System.out.println(sb);
        }
    }

    static void print(String[] words, Cell[][] paths) {
        System.out.println("\nКоординаты слов (строка,столбец):");
        for (int i = 0; i < words.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i + 1).append(". ").append(words[i]).append(": ");
            for (Cell cell : paths[i]) {
                sb.append(cell).append(' ');
            }
            System.out.println(sb);
        }
    }
}

public class Main {
    private static final int MAX_SETS = 200;
    private static final long SET_TIME_LIMIT_MS = 2000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            int size;
            if (args.length >= 1) {
                size = InputParser.parseSize(args[0]);
            } else {
                System.out.println("Генератор филворда");
                System.out.print("Введите размер сетки (3, 4, 5 или 6): ");
                size = InputParser.parseSize(scanner.nextLine());
            }
            solveAndShow(size);
        } catch (FillwordException e) {
            System.out.println("Ошибка! " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка! Ввод прерван.");
        } finally {
            scanner.close();
        }
    }

    private static void solveAndShow(int size) throws FillwordException {
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int sets = 1; sets <= MAX_SETS; sets++) {
            String[] words = WordDictionary.pick(size, random);
            FillwordSolver solver = new FillwordSolver(size, words, SET_TIME_LIMIT_MS, random);
            if (solver.solve()) {
                long time = System.currentTimeMillis() - start;
                show(size, words, solver, sets, time);
                return;
            }
        }
        System.out.println("Не удалось построить филворд, запустите программу ещё раз.");
    }

    private static void show(int size, String[] words, FillwordSolver solver, int sets, long time) {
        boolean valid = FillwordValidator.isValid(solver.getGrid(), words, solver.getPaths());
        System.out.println("\nФилворд создан! (" + time + " мс, наборов слов: " + sets
                + ", узлов поиска: " + solver.getNodes() + ", проверка правил: "
                + (valid ? "пройдена" : "НЕ пройдена") + ")\n");
        FillwordPrinter.print(solver.getGrid());
        FillwordPrinter.print(solver.getPaths(), size);
        FillwordPrinter.print(words, solver.getPaths());
    }
}
