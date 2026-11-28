// Часть 3. Ростислав Родионов: проверка результата, вывод и запуск

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
