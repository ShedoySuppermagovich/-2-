# Fillword Generator

## About the Project

Fillword Generator is a console application written in Java 8 that automatically creates a fillword (word grid) puzzle.
The user enters only the grid size. The program reads Russian words from the `words.xml` file, randomly picks a set of
words whose total length equals the number of grid cells, and places all of them into a square grid with no empty cells.
It then prints the grid and the coordinates of the cells that form each word. Every run produces a new set of words
and a new arrangement.

## Features

- Grid sizes: 3×3, 4×4, 5×5 and 6×6.
- The size is entered in the console or passed as the first command-line argument.
- Words are loaded from the `words.xml` file (2033 unique words).
- Words are picked randomly, without repeats, so that their total length equals the number of cells.
- Random placement: the starting cells, the directions and the order of equally long words are shuffled.
- Backtracking search with pruning and random restarts.
- If a chosen word set cannot be placed, another set is picked automatically (up to 200 sets).
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
- Constructor and method overloading
- Recursion and the backtracking algorithm
- `java.util.Random` and array shuffling (the Fisher–Yates algorithm)
- Custom exception and `try / catch / finally`
- Command-line arguments

## Code Structure

Every class is in its own file in the `src` folder, and the code is divided into three parts by author.
Each file starts with a comment that names its part and author.

| Part | Author | Files |
|---|---|---|
| 1. Data model, input validation and dictionary | Artem Valenta | `Direction.java`, `Cell.java`, `FillwordException.java`, `InputParser.java`, `WordDictionary.java` |
| 2. Word placement algorithm | Saveliy Kopasov | `FillwordSolver.java` |
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
│   ├── FillwordSolver.java
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
- `FillwordSolver` — places the words in the grid; it has three overloaded constructors.
- `FillwordSolver.solve()` — runs the search with restarts.
- `FillwordSolver.placeWord()` — chooses a starting cell for the next word.
- `FillwordSolver.extendPath()` — recursively leads the word through neighboring free cells.
- `FillwordSolver.regionsFeasible()` — cuts off hopeless variants by the sizes of free areas.
- `FillwordSolver.shuffled()` — returns a random permutation of the numbers 0…n−1.
- `FillwordValidator.isValid()` — independent check of the finished result.
- `FillwordPrinter.print()` — three overloaded methods: grid, word-number map, coordinates.
- `FillwordGenerator.main()` — reads the size, loads the dictionary and starts the generation.
- `FillwordGenerator.solveAndShow()` — picks word sets in a loop, runs the solver and prints the result.

## How the Algorithm Works

1. The user enters the grid size (or passes it as an argument), and the program checks it.
2. The program loads the words from `words.xml`.
3. `WordDictionary.pick()` randomly picks a set of words whose total length equals `size × size`.
4. The words of the set are sorted by length, longest first, because long words restrict the search the most. Words of equal length go in random order.
5. For every tail of the word list, the program calculates in advance which total lengths can be made from the remaining words.
6. For each next word, the starting cells are tried in random order, and the word moves through neighboring free cells; the direction at each step is also chosen in random order.
7. After a word is placed, the free cells are split into connected areas. The size of every area must be equal to the total length of some of the remaining words. If not, the variant is rejected at once.
8. If a word cannot be placed, the algorithm returns to the previous step and tries another option (backtracking).
9. If an attempt takes too long (more than a given number of steps), the search restarts with a new random order.
10. If no arrangement is found for the chosen set or the time limit (2 seconds per set) is exceeded, `solveAndShow()` picks a new set of words. Up to 200 sets are tried.
11. When all words are placed, the result is checked and printed.

The main algorithm is backtracking with pruning and random restarts.

### How Word Selection Works

`WordDictionary.pick()` does not fit the set to a known answer. It walks through the randomly shuffled dictionary:

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
letters, so the selection fails if exactly one letter is missing at the end. In that case the method tries again with a new
shuffle (up to 1000 times).

### How Randomness Works

Randomness works on three levels: which words get into the set, in what order they are placed, and in which cells each word lies.
The order of search is shuffled, but all variants are still checked, so a solution for the chosen set is found whenever it exists.
The shuffling is done by `shuffled(n)`: it creates the array `0, 1, …, n−1` and swaps its elements at random (the Fisher–Yates algorithm).

```java
for (int start : shuffled(size * size)) {          // starting cells in random order
    ...
}
for (int index : shuffled(DIRECTIONS.length)) {    // directions in random order
    ...
}
```

### Why Restarts Are Needed

With a random order the search sometimes gets stuck for a long time in an unlucky branch. That is why `solve()` limits every
attempt to a number of steps:

```java
long budget = START_BUDGET;               // 3000 steps for the first attempt
while (!timedOut) {
    clear();                              // empty grid
    attemptEnd = nodes + budget;          // the step at which the attempt is cut off
    cutOff = false;
    if (placeWord(0)) return true;        // solution found
    if (!cutOff) return false;            // everything was checked: no solution
    budget *= 2;                          // the next attempt is twice as long
}
```

If an attempt was cut off (`cutOff`), the grid is cleared, the order is shuffled again and the search starts over.
If an attempt ended without being cut off, all variants were checked and there is no solution for this word set.

## Division of Work

### Artem Valenta — Part 1

- Direction enumeration `Direction` and the cell class `Cell`.
- Exception class `FillwordException`.
- Grid size validation in `InputParser`.
- `WordDictionary`: loading words from `words.xml`, removing duplicates and random selection of words.

### Saveliy Kopasov — Part 2

- The `FillwordSolver` class: ordering of words, calculation of possible total lengths, backtracking search.
- Random order of starting cells and directions, random restarts.
- Pruning by the sizes of free areas (`regionsFeasible()`).

### Rostislav Rodionov — Part 3

- Result check in `FillwordValidator`.
- Output of the grid, word-number map and coordinates in `FillwordPrinter`.
- The `main()` method: reading the size, startup, handling of errors, repeated picking of word sets in `solveAndShow()`.

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

Филворд создан! (5 мс, наборов слов: 1, узлов поиска: 26, проверка правил: пройдена)

Сетка:
Л  П  Р  С  
Я  Т  Е  З  
Ж  Р  Д  Л  
С  О  А  О  

Номера слов в клетках:
4  4  3  3  
4  2  3  1  
4  2  3  1  
2  2  3  1  

Координаты слов (строка,столбец):
1. ЗЛО: (2,4) (3,4) (4,4) 
2. СОРТ: (4,1) (4,2) (3,2) (2,2) 
3. СРЕДА: (1,4) (1,3) (2,3) (3,3) (4,3) 
4. ПЛЯЖ: (1,2) (1,1) (2,1) (3,1) 
```

The set of words and their arrangement are different in every run.

## Conclusion

The project is a Java 8 application that automatically generates fillword puzzles. Words are read from an XML dictionary of
2033 words, picked at random so that their letters exactly fill the grid, and placed by a backtracking algorithm with pruning
and random restarts. The result is checked automatically and printed together with the coordinates of every word. The code is
divided into three parts, each written by one team member.
