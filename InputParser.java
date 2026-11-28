// Часть 1. Артём Валента: модель данных, проверка ввода и словарь

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
