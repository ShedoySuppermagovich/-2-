// Часть 3. Ростислав Родионов: проверка результата, вывод и запуск

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
