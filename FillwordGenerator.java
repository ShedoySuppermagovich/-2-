import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Генератор структуры филворда (Java 8).
 *
 * Все слова словаря размещаются в квадратной сетке size x size ровно по одному разу.
 * Слово читается по соседним клеткам (вверх/вниз/влево/вправо, без диагоналей),
 * может поворачивать, клетки разных слов не пересекаются, пустых клеток не остаётся.
 *
 * Алгоритм: поиск с возвратом (backtracking) + отсечения:
 *   1) слова размещаются от длинных к коротким;
 *   2) после каждого слова свободная область делится на связные куски (flood fill),
 *      и размер каждого куска обязан быть суммой длин каких-то ещё не размещённых слов;
 *   3) ограничение по времени, чтобы программа не «зависала» на неразрешимых данных.
 */
public class FillwordGenerator {

    // ------------------------------------------------------------------ данные

    /** Клетка сетки (хранит координаты, нумерация с 0). */
    public static final class Cell {
        public final int row;
        public final int col;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "(" + (row + 1) + "," + (col + 1) + ")";
        }
    }

    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, -1, 1};
    private static final char EMPTY = '\0';
    private static final long CHECK_EVERY = 4096;

    private final int size;
    private final String[] words;          // слова в исходном порядке (ВЕРХНИЙ РЕГИСТР)
    private final int[] order;             // порядок размещения: индексы слов, длинные первыми
    private final boolean[][] reachable;   // reachable[k][s]: s = сумма длин каких-то слов order[k..]
    private final long timeLimitMs;

    private final char[][] grid;
    private final List<List<Cell>> paths;  // paths.get(i) — путь слова words[i]

    private long deadline;
    private long nodes;
    private boolean timedOut;

    public FillwordGenerator(int size, List<String> dictionary, long timeLimitMs) {
        this.size = size;
        this.words = dictionary.toArray(new String[0]);
        this.timeLimitMs = timeLimitMs;
        this.grid = new char[size][size];
        this.paths = new ArrayList<List<Cell>>();
        for (int i = 0; i < words.length; i++) {
            paths.add(null);
        }

        // порядок: длинные слова первыми (они сильнее всего ограничивают поиск)
        Integer[] idx = new Integer[words.length];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = i;
        }
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                int byLen = Integer.compare(words[b].length(), words[a].length());
                return byLen != 0 ? byLen : Integer.compare(a, b);
            }
        });
        this.order = new int[idx.length];
        for (int i = 0; i < idx.length; i++) {
            order[i] = idx[i];
        }

        // reachable[k] — задача о сумме подмножества для «хвоста» order[k..]
        int cells = size * size;
        this.reachable = new boolean[order.length + 1][cells + 1];
        reachable[order.length][0] = true;
        for (int k = order.length - 1; k >= 0; k--) {
            int len = words[order[k]].length();
            for (int s = 0; s <= cells; s++) {
                reachable[k][s] = reachable[k + 1][s]
                        || (s >= len && reachable[k + 1][s - len]);
            }
        }
    }

    // --------------------------------------------------------------- публичное API

    /** Запускает поиск. true — филворд построен, false — не найден или вышло время. */
    public boolean generate() {
        deadline = System.currentTimeMillis() + timeLimitMs;
        nodes = 0;
        timedOut = false;
        return placeWord(0);
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public long getNodes() {
        return nodes;
    }

    public char[][] getGrid() {
        return grid;
    }

    public List<List<Cell>> getPaths() {
        return paths;
    }

    public String[] getWords() {
        return words;
    }

    // ------------------------------------------------------------------ алгоритм

    /** Размещает k-е по порядку слово: перебирает стартовые клетки. */
    private boolean placeWord(int k) {
        if (k == order.length) {
            return true;
        }
        if (!regionsFeasible(k)) {
            return false; // отсечение: свободные «карманы» нельзя заполнить оставшимися словами
        }

        int wordIndex = order[k];
        String word = words[wordIndex];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != EMPTY) {
                    continue;
                }
                List<Cell> path = new ArrayList<Cell>();
                grid[r][c] = word.charAt(0);
                path.add(new Cell(r, c));

                if (extendPath(k, word, 1, r, c, path)) {
                    return true;
                }
                grid[r][c] = EMPTY; // откат стартовой клетки
                if (timedOut) {
                    return false;
                }
            }
        }
        return false;
    }

    /** Рекурсивно «рисует» слово по соседним клеткам. */
    private boolean extendPath(int k, String word, int pos, int row, int col, List<Cell> path) {
        if (++nodes % CHECK_EVERY == 0 && System.currentTimeMillis() > deadline) {
            timedOut = true;
        }
        if (timedOut) {
            return false;
        }

        if (pos == word.length()) {                 // слово целиком уложено
            int wordIndex = order[k];
            paths.set(wordIndex, new ArrayList<Cell>(path));
            if (placeWord(k + 1)) {                 // переходим к следующему слову
                return true;
            }
            paths.set(wordIndex, null);             // откат
            return false;
        }

        for (int d = 0; d < 4; d++) {
            int nr = row + DR[d];
            int nc = col + DC[d];
            if (nr < 0 || nr >= size || nc < 0 || nc >= size || grid[nr][nc] != EMPTY) {
                continue;
            }
            grid[nr][nc] = word.charAt(pos);
            path.add(new Cell(nr, nc));

            if (extendPath(k, word, pos + 1, nr, nc, path)) {
                return true;
            }
            path.remove(path.size() - 1);           // откат клетки
            grid[nr][nc] = EMPTY;
        }
        return false;
    }

    /**
     * Проверка «карманов»: каждая связная область свободных клеток должна иметь размер,
     * равный сумме длин какого-то подмножества оставшихся слов. Иначе ветку можно не искать.
     */
    private boolean regionsFeasible(int k) {
        boolean[][] seen = new boolean[size][size];
        int[] stack = new int[size * size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != EMPTY || seen[r][c]) {
                    continue;
                }
                int top = 0;
                int count = 0;
                stack[top++] = r * size + c;
                seen[r][c] = true;
                while (top > 0) {
                    int cur = stack[--top];
                    count++;
                    int cr = cur / size;
                    int cc = cur % size;
                    for (int d = 0; d < 4; d++) {
                        int nr = cr + DR[d];
                        int nc = cc + DC[d];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size
                                && grid[nr][nc] == EMPTY && !seen[nr][nc]) {
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

    // ------------------------------------------------------------ самопроверка

    /** Независимая проверка результата по всем правилам задания. */
    public boolean validate() {
        boolean[][] used = new boolean[size][size];
        for (int i = 0; i < words.length; i++) {
            List<Cell> p = paths.get(i);
            if (p == null || p.size() != words[i].length()) {
                return false;
            }
            for (int j = 0; j < p.size(); j++) {
                Cell cell = p.get(j);
                if (used[cell.row][cell.col] || grid[cell.row][cell.col] != words[i].charAt(j)) {
                    return false;           // пересечение или неверная буква
                }
                used[cell.row][cell.col] = true;
                if (j > 0) {
                    Cell prev = p.get(j - 1);
                    if (Math.abs(prev.row - cell.row) + Math.abs(prev.col - cell.col) != 1) {
                        return false;       // не соседние по стороне клетки
                    }
                }
            }
        }
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!used[r][c]) {
                    return false;           // осталась пустая клетка
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ main

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Генератор филворда");
        System.out.print("Введите размер сетки (3, 4, 5 или 6): ");
        String input = scanner.nextLine().trim();

        if (!input.matches("[0-9]+")) {
            System.out.println("Ошибка! Введите целое положительное число.");
            return;
        }
        int size = Integer.parseInt(input);
        if (size < 3 || size > 6) {
            System.out.println("Ошибка! Можно выбрать только 3, 4, 5 или 6.");
            return;
        }

        System.out.println("Введите слова через пробел:");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("Ошибка! Список слов пуст.");
            return;
        }

        // LinkedHashSet убирает дубликаты, сохраняя порядок ввода
        Set<String> unique = new LinkedHashSet<String>();
        int totalLength = 0;
        for (String w : line.split("[\\s,;]+")) {
            if (!w.matches("[А-Яа-яЁё]+")) {
                System.out.println("Ошибка! Слова должны содержать только русские буквы.");
                return;
            }
            if (unique.add(w.toUpperCase())) {
                totalLength += w.length();
            } else {
                System.out.println("Предупреждение: слово «" + w + "» повторяется, учтено один раз.");
            }
        }

        if (totalLength != size * size) {
            System.out.println("Ошибка! Количество букв должно быть равно "
                    + (size * size) + " (сейчас " + totalLength + ").");
            return;
        }

        FillwordGenerator gen = new FillwordGenerator(size, new ArrayList<String>(unique), 20000);
        long start = System.currentTimeMillis();
        boolean ok = gen.generate();
        long time = System.currentTimeMillis() - start;

        if (!ok) {
            System.out.println(gen.isTimedOut()
                    ? "Время поиска истекло, филворд не найден."
                    : "Филворд для этих слов построить невозможно.");
            return;
        }
        print(gen, time);
        scanner.close();
    }

    private static void print(FillwordGenerator gen, long time) {
        char[][] grid = gen.getGrid();
        String[] words = gen.getWords();
        int size = grid.length;

        System.out.println("\nФилворд создан! (" + time + " мс, узлов поиска: " + gen.getNodes()
                + ", проверка правил: " + (gen.validate() ? "пройдена" : "НЕ пройдена") + ")\n");

        System.out.println("Сетка:");
        for (char[] row : grid) {
            StringBuilder sb = new StringBuilder();
            for (char ch : row) {
                sb.append(ch).append("  ");
            }
            System.out.println(sb.toString());
        }

        // карта принадлежности: в каждой клетке — номер слова
        int[][] owner = new int[size][size];
        for (int i = 0; i < words.length; i++) {
            for (Cell cell : gen.getPaths().get(i)) {
                owner[cell.row][cell.col] = i + 1;
            }
        }
        System.out.println("\nНомера слов в клетках:");
        for (int[] row : owner) {
            StringBuilder sb = new StringBuilder();
            for (int v : row) {
                sb.append(String.format("%-3d", v));
            }
            System.out.println(sb.toString());
        }

        System.out.println("\nКоординаты слов (строка,столбец):");
        for (int i = 0; i < words.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i + 1).append(". ").append(words[i]).append(": ");
            for (Cell cell : gen.getPaths().get(i)) {
                sb.append(cell).append(' ');
            }
            System.out.println(sb.toString());
        }
    }
}
