
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
