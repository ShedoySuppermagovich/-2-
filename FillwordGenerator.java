// Часть 3. Ростислав Родионов: проверка результата, вывод и запуск

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class FillwordGenerator {
    private static final String XML_FILE = "words.xml";

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
            WordDictionary dictionary = new WordDictionary(XML_FILE);
            System.out.println("Слов в словаре: " + dictionary.size());
            buildAndShow(size, dictionary);
        } catch (FillwordException e) {
            System.out.println("Ошибка! " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка! Ввод прерван.");
        } finally {
            scanner.close();
        }
    }

    private static void buildAndShow(int size, WordDictionary dictionary) throws FillwordException {
        Random random = new Random();
        long start = System.currentTimeMillis();

        String[] words = dictionary.pick(size, random);
        FillwordBuilder builder = new FillwordBuilder(size, words, random);
        builder.build();

        long time = System.currentTimeMillis() - start;
        boolean valid = FillwordValidator.isValid(builder.getGrid(), words, builder.getPaths());
        System.out.println("\nФилворд создан! (" + time + " мс, слов: " + words.length
                + ", проверка правил: " + (valid ? "пройдена" : "НЕ пройдена") + ")\n");

        FillwordPrinter.print(builder.getGrid());
        FillwordPrinter.print(builder.getPaths(), size);
        FillwordPrinter.print(words, builder.getPaths());
    }
}
