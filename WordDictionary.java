// Часть 1. Артём Валента: модель данных, проверка ввода и словарь

import java.io.File;
import java.util.Random;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

class WordDictionary {
    private static final int MAX_ATTEMPTS = 1000;

    private final String[] words;

    // file - путь к XML-файлу, слова читаются из его элементов item
    WordDictionary(String file) throws FillwordException {
        this.words = load(file);
    }

    int size() {
        return words.length;
    }

    String[] pick(int size, Random random) throws FillwordException {
        int target = size * size;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String[] chosen = new String[target];
            int count = 0;
            int total = 0;
            for (int index : shuffled(words.length, random)) {
                int length = words[index].length();
                if (total + length > target) {
                    continue;
                }
                chosen[count++] = words[index];
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

    private static String[] load(String file) throws FillwordException {
        NodeList items;
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new File(file));
            items = document.getElementsByTagName("item");
        } catch (Exception e) {
            throw new FillwordException("Не удалось прочитать файл " + file
                    + ". Проверьте расположение и формат файла.");
        }

        String[] unique = new String[items.getLength()];
        int count = 0;
        for (int i = 0; i < items.getLength(); i++) {
            String word = items.item(i).getTextContent().trim().toUpperCase();
            if (word.matches("[А-ЯЁ]+") && !contains(unique, count, word)) {
                unique[count++] = word;
            }
        }
        if (count == 0) {
            throw new FillwordException("В файле " + file + " нет подходящих слов.");
        }

        String[] result = new String[count];
        System.arraycopy(unique, 0, result, 0, count);
        return result;
    }

    private static boolean contains(String[] items, int count, String word) {
        for (int i = 0; i < count; i++) {
            if (items[i].equals(word)) {
                return true;
            }
        }
        return false;
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
