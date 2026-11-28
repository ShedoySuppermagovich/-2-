// Часть 1. Артём Валента: модель данных, проверка ввода и словарь

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
