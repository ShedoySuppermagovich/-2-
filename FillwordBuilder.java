// Часть 2. Копасов Савелий: построение филворда без возвратов

import java.util.Random;

class FillwordBuilder {
    private static final int STEPS_PER_CELL = 50;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final int size;
    private final String[] words;
    private final Random random;
    private final char[][] grid;
    private final Cell[][] paths;

    // size - сторона сетки, words - слова с суммарной длиной size * size,
    // random - источник случайности
    FillwordBuilder(int size, String[] words, Random random) {
        this.size = size;
        this.words = words;
        this.random = random;
        this.grid = new char[size][size];
        this.paths = new Cell[words.length][];
    }

    // Случайное расположение при каждом запуске
    FillwordBuilder(int size, String[] words) {
        this(size, words, new Random());
    }

    void build() {
        Cell[] route = randomRoute();
        int position = 0;
        for (int i = 0; i < words.length; i++) {
            int length = words[i].length();
            boolean reversed = random.nextBoolean();
            Cell[] path = new Cell[length];
            for (int j = 0; j < length; j++) {
                path[j] = route[reversed ? position + length - 1 - j : position + j];
                grid[path[j].getRow()][path[j].getCol()] = words[i].charAt(j);
            }
            paths[i] = path;
            position += length;
        }
    }

    private Cell[] randomRoute() {
        Cell[] route = snake();
        int last = route.length - 1;
        for (int step = 0; step < STEPS_PER_CELL * route.length; step++) {
            reverse(route, 0, last);
            Direction d = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            int i = indexOf(route, route[last].getRow() + d.getDRow(),
                    route[last].getCol() + d.getDCol());
            if (i >= 0) {
                reverse(route, i + 1, last);
            }
        }
        return route;
    }

    private Cell[] snake() {
        Cell[] route = new Cell[size * size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                route[r * size + c] = new Cell(r, r % 2 == 0 ? c : size - 1 - c);
            }
        }
        return route;
    }

    private static int indexOf(Cell[] route, int row, int col) {
        for (int i = 0; i < route.length; i++) {
            if (route[i].getRow() == row && route[i].getCol() == col) {
                return i;
            }
        }
        return -1;
    }

    private static void reverse(Cell[] route, int from, int to) {
        for (; from < to; from++, to--) {
            Cell tmp = route[from];
            route[from] = route[to];
            route[to] = tmp;
        }
    }

    char[][] getGrid() {
        return grid;
    }

    Cell[][] getPaths() {
        return paths;
    }
}
