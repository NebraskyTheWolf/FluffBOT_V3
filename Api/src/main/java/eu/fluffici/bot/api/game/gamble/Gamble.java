package eu.fluffici.bot.api.game.gamble;

/*
---------------------------------------------------------------------------------
File Name : Gamble.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class Gamble {
    private final Random random = new Random();
    private List<List<Integer>> randomMatrix;
    private int rowIndex;
    private int colIndex;

    private int numberOfRepeated = 0;
    private double chanceOfWinning = 0;
    private int randomNumber = 0;
    private int twentyFivePercentOfBait = 0;
    private long maxPossible = 0;

    private final int MATRIX_ROWS;
    private final int MATRIX_COLS;
    private final int MATRIX_RAND_GAP;

    public Gamble(int rows, int cols, int gap) {
        if (rows < 2 || cols < 2 || rows > 28 || cols > 28)
            throw new IllegalArgumentException(String.format("Gamble: Matrices sizes need to be at least 2x2 (Current size: %sx%s) and maximum size is 6x6", rows, cols));
        if (gap < 100 || gap > 10000)
            throw new IllegalArgumentException(String.format("Gamble: The gap need to be at least up to 100. Current value: %s of 10,000", gap));

        this.MATRIX_ROWS = rows;
        this.MATRIX_COLS = cols;
        this.MATRIX_RAND_GAP = gap;
    }

    public boolean isDrawn(int bait) {
        randomMatrix = generateRandomMatrix(this.MATRIX_ROWS, this.MATRIX_COLS);
        rowIndex = 0;
        colIndex = 0;

        List<List<Integer>> matrixCopy = copyMatrix(randomMatrix);
        int numberOfRepeatedValues = getNumberOfRepeatedValues(matrixCopy);
        this.numberOfRepeated = numberOfRepeatedValues;

        // Calculate the chance of winning based on the occurrence of repeated values
        double chanceOfWinning = (double) numberOfRepeatedValues / (randomMatrix.size() * randomMatrix.get(0).size());
        this.chanceOfWinning = chanceOfWinning;

        int randomNumber = getNextRandomFromMatrix();
        this.randomNumber = randomNumber;
        this.twentyFivePercentOfBait = randomNumber % (bait / 2) + 1;

        return numberOfRepeatedValues >= 4 && chanceOfWinning >= 0.0625;
    }

    private int getNumberOfRepeatedValues(List<List<Integer>> matrixCopy) {
        long maxPossibleValue = (long) this.MATRIX_RAND_GAP * randomMatrix.size() * randomMatrix.get(0).size();

        this.maxPossible = maxPossibleValue;

        List<Integer> count = new ArrayList<>((int) (maxPossibleValue)); // Limit list size
        for (int i = 0; i < maxPossibleValue; i++) {
            count.add(0);
        }

        for (List<Integer> row : matrixCopy) {
            for (int num : row) {
                count.set(num, count.get(num) + 1);
            }
        }

        int numberOfRepeatedValues = 0;
        for (int numCount : count) {
            if (numCount >= 2) {
                numberOfRepeatedValues++;
            }
        }
        return numberOfRepeatedValues;
    }

    private int getNextRandomFromMatrix() {
        int randomNumber = randomMatrix.get(rowIndex).get(colIndex);
        colIndex++;
        if (colIndex >= 4) {
            colIndex = 0;
            rowIndex++;
            if (rowIndex >= 4) {
                // Regenerate matrix if we've exhausted it
                randomMatrix = generateRandomMatrix(this.MATRIX_ROWS, this.MATRIX_COLS);
                rowIndex = 0;
            }
        }
        return randomNumber;
    }

    private List<List<Integer>> generateRandomMatrix(int rows, int cols) {
        List<List<Integer>> matrix = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(random.nextInt(this.MATRIX_RAND_GAP));
            }
            matrix.add(row);
        }
        return matrix;
    }

    private List<List<Integer>> copyMatrix(List<List<Integer>> original) {
        List<List<Integer>> copy = new ArrayList<>();
        for (List<Integer> row : original) {
            copy.add(new ArrayList<>(row));
        }
        return copy;
    }

    public String toDiscordMatrixString(boolean discord) {
        StringBuilder sb = new StringBuilder();

        // Find the maximum length of values in the matrix
        int maxLength = 0;
        for (List<Integer> row : randomMatrix) {
            for (int num : row) {
                int length = String.valueOf(num).length();
                maxLength = Math.max(maxLength, length);
            }
        }

        // Add top border
        if (discord)
            sb.append("```\n");

        // Add cell values and borders
        for (List<Integer> row : randomMatrix) {
            // Add top border of the row
            sb.append("|");
            for (int i = 0; i < row.size(); i++) {
                sb.append("-".repeat(maxLength + 3)).append("|"); // Add 1 extra for the "+" mark
            }
            sb.append("\n");

            // Add cell values
            sb.append("|");
            for (int num : row) {
                String numStr = String.valueOf(num);
                int occurrences = Collections.frequency(row, num);
                sb.append(" ").append(numStr);
                sb.append(" ".repeat(maxLength - numStr.length()));
                if (occurrences > 1) {
                    sb.append("+"); // Add "+" mark for duplicated values
                } else {
                    sb.append(" "); // Add space if not duplicated
                }
                sb.append(" |");
            }
            sb.append("\n");
        }

        // Add bottom border
        sb.append("|");
        for (int i = 0; i < randomMatrix.get(0).size(); i++) {
            sb.append("-".repeat(maxLength + 3)).append("|"); // Add 1 extra for the "+" mark
        }
        sb.append("\n");

        // Close the code block
        if (discord)
            sb.append("```");

        return sb.toString();
    }

    public static String parseMatrix(String matrixString) {
        List<List<Integer>> matrix = new ArrayList<>();
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        String content = matrixString.replaceAll("```", "").trim();

        String[] lines = content.split("\n");
        Pattern matrixPattern = Pattern.compile("\\|\\s*([0-9\\s\\+]+?)\\s*\\|"); // Corrected regex pattern

        for (String line : lines) {
            Matcher matcher = matrixPattern.matcher(line);
            if (matcher.find()) { // Use find instead of matches to allow partial matches
                List<Integer> row = new ArrayList<>();
                String[] cells = matcher.group(1).split("\\s+\\|?\\s*");

                for (String cell : cells) {
                    if (cell.contains("+")) {
                        cell = cell.replace("+", "").trim();
                    }
                    if (!cell.isEmpty()) {
                        int num = Integer.parseInt(cell);
                        row.add(num);
                        frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
                    }
                }
                matrix.add(row);
            }
        }

        List<String> recurringNumbers = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > 1) {
                recurringNumbers.add(String.valueOf(entry.getKey()));
            }
        }

        int mostRecurrentNumber = Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        String allNumbersWithBias = extractAllNumbersWithBias(matrix, mostRecurrentNumber);
        String regressionResult = applyLinearRegression(frequencyMap);

        StringBuilder sb = new StringBuilder();
        sb.append("### Parsed Matrix:\n");
        for (List<Integer> row : matrix) {
            sb.append(row).append("\n");
        }
        sb.append("\n\n");

        sb.append("Recurring Numbers: \n").append("`").append(String.join(", ", recurringNumbers.stream().limit(10).toList())).append("`\n\n");
        sb.append("All Numbers with Bias: \n").append("`").append(allNumbersWithBias, 0, Math.min(250, allNumbersWithBias.length())).append("`\n");
        sb.append("Regression Result: \n").append("`").append(regressionResult).append("`\n\n");

        sb.append("Matrix:\n");
        sb.append(content);

        return sb.toString();
    }

    @NotNull
    public static String extractAllNumbersWithBias(@NotNull List<List<Integer>> matrix, int mostRecurrentNumber) {
        StringBuilder allNumbersWithBias = new StringBuilder();
        for (List<Integer> row : matrix) {
            for (Integer num : row) {
                allNumbersWithBias.append(num);
                if (num == mostRecurrentNumber) {
                    allNumbersWithBias.append("(bias)");
                }
                allNumbersWithBias.append(", ");
            }
        }

        if (!allNumbersWithBias.isEmpty()) {
            allNumbersWithBias.setLength(allNumbersWithBias.length() - 2);
        }

        return allNumbersWithBias.toString();
    }

    public static String applyLinearRegression(@NotNull Map<Integer, Integer> frequencyMap) {
        List<double[]> points = frequencyMap.entrySet().stream()
                .map(entry -> new double[]{entry.getKey(), entry.getValue()})
                .toList();

        double meanX = points.stream().mapToDouble(point -> point[0]).average().orElse(0);
        double meanY = points.stream().mapToDouble(point -> point[1]).average().orElse(0);

        double numerator = points.stream().mapToDouble(point -> (point[0] - meanX) * (point[1] - meanY)).sum();
        double denominator = points.stream().mapToDouble(point -> Math.pow(point[0] - meanX, 2)).sum();
        double slope = numerator / denominator;

        double intercept = meanY - (slope * meanX);
        return String.format("y = %.2fx + %.2f", slope, intercept);
    }

}
