// Часть 1. Артём Валента: модель данных, проверка ввода и словарь

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
