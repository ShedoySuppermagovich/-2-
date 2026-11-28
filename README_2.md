# Fillword Generator

## About the Project

Fillword Generator is a console application written in Java 8 that automatically creates a fillword (word grid) puzzle.
The user enters only the grid size. The program reads Russian words from the `words.xml` file, randomly picks a set of
words whose total length equals the number of grid cells, and places all of them into a square grid with no empty cells.
It then prints the grid and the coordinates of the cells that form each word. The placement algorithm is compact and uses
**no backtracking**: it always builds the puzzle in one pass. Every run produces a new set of words and a new arrangement.

## Features

- Grid sizes: 3×3, 4×4, 5×5 and 6×6.
- The size is entered in the console or passed as the first command-line argument.
- Words are loaded from the `words.xml` file (2033 unique words).
- Words are picked randomly, without repeats, so that their total length equals the number of cells.
- Placement without backtracking and without trial and error: a random route through all cells is cut into words.
- Random arrangement: the route, the order of the words and the reading direction of each word are random.
- Automatic check of the finished grid against the placement rules.
- Output: the grid, a map of word numbers per cell, and the coordinates of every word (row, column; counting from 1).
- Input validation: only the numbers 3, 4, 5 and 6 are accepted; reading problems with `words.xml` are reported.

## Word Placement Rules

1. Every word is placed exactly once.
2. A word is read through neighboring cells: up, down, left or right. Diagonal moves are not allowed.
3. A word may be straight or bent, i.e. it may turn.
4. A cell cannot be used twice within one word.
5. Different words cannot share cells.
6. The grid is filled completely: there are no empty cells and no random letters.
7. Words are taken from the dictionary and do not repeat within one puzzle.

A necessary condition: the total length of the words equals the number of cells (`size × size`).
The program picks the words so that this condition always holds.

## Dictionary

The words are stored in the `words.xml` file as `<item>` elements inside a `string-array`. The file must be in the folder
from which the program is started.

- **2033 unique words** in total: the 1533 original words and **500 words added from Ozhegov's dictionary**
  (the added words contain no repeats of the original ones).
- Duplicates in the file are ignored when it is loaded; every word is converted to upper case.
- Only words made of Russian letters (including Ё) are used; other entries are skipped.
- The full alphabetical list, with the newly added words in **bold**, is in [`Words.md`](Words.md).

| Word length (letters) | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|
| Words | 10 | 195 | 506 | 823 | 233 | 177 | 81 | 6 | 1 | 1 |

To add your own words, append new `<item>word</item>` lines to `words.xml`.

## Technologies Used

- Java 8
- XML (DOM parser from the standard library: `DocumentBuilderFactory`)
- Classes, constructors, encapsulation (`private` fields and accessor methods)
- Enumeration (`enum`) with a constructor and fields
- Arrays: one-dimensional, two-dimensional and jagged
- Constructor overloading
- `java.util.Random` and array shuffling (the Fisher–Yates algorithm)
- Custom exception and `try / catch / finally`
- Command-line arguments

## Code Structure

Every class is in its own file in the `src` folder, and the code is divided into three parts by author.
Each file starts with a comment that names its part and author.

| Part | Author | Files |
|---|---|---|
| 1. Data model, input validation and dictionary | Artem Valenta | `Direction.java`, `Cell.java`, `FillwordException.java`, `InputParser.java`, `WordDictionary.java` |
| 2. Word placement algorithm | Saveliy Kopasov | `FillwordBuilder.java` |
| 3. Result check, output and startup | Rostislav Rodionov | `FillwordValidator.java`, `FillwordPrinter.java`, `FillwordGenerator.java` (`main`) |

Project layout:

```text
FillwordGenerator/
├── src/
│   ├── Direction.java
│   ├── Cell.java
│   ├── FillwordException.java
│   ├── InputParser.java
│   ├── WordDictionary.java
│   ├── FillwordBuilder.java
│   ├── FillwordValidator.java
│   ├── FillwordPrinter.java
│   └── FillwordGenerator.java
├── words.xml
├── Words.md
└── README.md
```

The `words.xml` file must be in the project root folder, next to the `src` folder.

### Main Classes and Methods

- `Direction` — the four allowed moves (up, down, left, right) with their row and column offsets.
- `Cell` — a grid cell (row and column); `toString()` prints it as `(row,column)`.
- `FillwordException` — exception for input and dictionary errors.
- `InputParser.parseSize()` — checks that the grid size is 3, 4, 5 or 6.
- `WordDictionary` — loads the words from the XML file (`load()`), reports their number (`size()`) and picks a random set of words (`pick()`).
- `FillwordBuilder` — builds the puzzle; it has two overloaded constructors.
- `FillwordBuilder.build()` — cuts the route into words and writes the letters into the grid.
- `FillwordBuilder.randomRoute()` — creates a random route through all cells.
- `FillwordBuilder.snake()` — creates the starting route: a "snake" row by row.
- `FillwordValidator.isValid()` — independent check of the finished result.
- `FillwordPrinter.print()` — three overloaded methods: grid, word-number map, coordinates.
- `FillwordGenerator.main()` — reads the size, loads the dictionary and starts the generation.
- `FillwordGenerator.buildAndShow()` — picks the words, builds the grid, checks it and prints the result.

## How the Algorithm Works

The algorithm has no search and no backtracking. Instead of trying to fit words into the grid and undoing failed attempts,
it first creates one path that visits every cell exactly once and then simply cuts this path into pieces, one piece per word.

1. The user enters the grid size (or passes it as an argument), and the program checks it.
2. The program loads the words from `words.xml`.
3. `WordDictionary.pick()` randomly picks a set of words whose total length equals `size × size`.
4. `snake()` creates a route through all cells: the first row left to right, the second row right to left, and so on.
5. `randomRoute()` makes the route random by many small changes (see below). After every change the route still visits every cell exactly once, and neighboring cells of the route are still neighbors in the grid.
6. `build()` goes along the route and gives the first `length` cells to the first word, the next cells to the second word, and so on. Each word is written along its piece either forward or backward (chosen randomly).
7. The result is checked by `FillwordValidator` and printed.

Because the route visits all cells and the total length of the words equals the number of cells, the words fill the grid completely.
Words never overlap, and every word is a chain of neighboring cells, so all placement rules are met automatically.

### How Word Selection Works

`WordDictionary.pick()` walks through the randomly shuffled dictionary:

```java
for (int index : shuffled(words.length, random)) {
    int length = words[index].length();
    if (total + length > target) {
        continue;                       // the word does not fit by letter count
    }
    chosen[count++] = words[index];
    total += length;
    if (total == target) {
        break;                          // exactly as many letters as there are cells
    }
}
```

Every word number appears in the shuffled array once, so a word cannot be chosen twice. The shortest word in the dictionary has two
letters, so the selection fails if exactly one letter is missing at the end. In that case the method simply repeats the selection with a new
shuffle (up to 1000 times). This is a repeated random draw, not a search with backtracking.

### How Randomness Works

Randomness works on three levels: which words get into the set, how the route runs through the grid, and in which direction each word is written.
The route is randomized by the following loop in `randomRoute()`:

```java
for (int step = 0; step < STEPS_PER_CELL * route.length; step++) {
    reverse(route, 0, last);                  // the other end of the route becomes the end
    Direction d = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
    int i = indexOf(route, route[last].getRow() + d.getDRow(),
            route[last].getCol() + d.getDCol());
    if (i >= 0) {
        reverse(route, i + 1, last);          // turn the tail of the route around
    }
}
```

At every step the end of the route looks at a random neighboring cell. If that cell is on the route (it is not outside the grid),
the part of the route after it is reversed. The end of the route then attaches to that cell, so the route stays continuous and still visits every cell once.
Repeating this 50 times per cell turns the regular snake into a random winding route.
Every step is a valid change of the route, so nothing has to be checked or undone: this is why no backtracking is needed.

## Division of Work

### Artem Valenta — Part 1

- Direction enumeration `Direction` and the cell class `Cell`.
- Exception class `FillwordException`.
- Grid size validation in `InputParser`.
- `WordDictionary`: loading words from `words.xml`, removing duplicates and random selection of words.

### Saveliy Kopasov — Part 2

- The `FillwordBuilder` class: the starting snake route, random route changes and cutting the route into words.

### Rostislav Rodionov — Part 3

- Result check in `FillwordValidator`.
- Output of the grid, word-number map and coordinates in `FillwordPrinter`.
- The `main()` method: reading the size, startup, handling of errors, building and printing in `buildAndShow()`.

## How to Run the Program

1. Keep the layout shown above: the Java files in `src` and `words.xml` in the project root folder.
2. Open a terminal in the project root folder and compile all files:

```text
javac -encoding UTF-8 -d out src/*.java
```

3. Run the program from the same folder and enter the grid size (3, 4, 5 or 6):

```text
java -cp out FillwordGenerator
```

Or pass the size as an argument:

```text
java -cp out FillwordGenerator 5
```

In IntelliJ IDEA, open the project root folder, mark `src` as the sources folder and run the `main()` method of `FillwordGenerator`;
the working directory must be the project root so that `words.xml` is found.
If Cyrillic is displayed incorrectly in the Windows console, use `chcp 65001` or start with `-Dfile.encoding=UTF-8`.

Example of a run:

```text
Генератор филворда
Введите размер сетки (3, 4, 5 или 6): 4
Слов в словаре: 2033

Филворд создан! (4 мс, слов: 3, проверка правил: пройдена)

Сетка:
Б  И  Г  Л  
Т  Н  О  А  
Н  Т  С  С  
И  Ф  Е  И  

Номера слов в клетках:
1  1  2  2  
3  1  2  2  
3  1  2  2  
3  3  2  2  

Координаты слов (строка,столбец):
1. БИНТ: (1,1) (1,2) (2,2) (3,2) 
2. СОГЛАСИЕ: (3,3) (2,3) (1,3) (1,4) (2,4) (3,4) (4,4) (4,3) 
3. ФИНТ: (4,2) (4,1) (3,1) (2,1) 
```

The set of words and their arrangement are different in every run.

## Conclusion

The project is a Java 8 application that automatically generates fillword puzzles. Words are read from an XML dictionary of
2033 words and picked at random so that their letters exactly fill the grid. The placement algorithm is compact and has no backtracking:
a random route through all cells is cut into pieces, and each piece becomes one word. The result is checked automatically and printed
together with the coordinates of every word. The code is divided into classes in separate files and into three parts, each written by one team member.
