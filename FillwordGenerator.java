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
