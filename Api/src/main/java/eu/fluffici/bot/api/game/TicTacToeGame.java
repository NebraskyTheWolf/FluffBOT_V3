/*
---------------------------------------------------------------------------------
File Name : TicTacToeGame

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

@Getter
@Setter
public class TicTacToeGame {
    private static final char PLAYER_MARK = 'X';
    private static final char BOT_MARK = 'O';
    private static final char EMPTY_MARK = ' ';
    private static final int SIZE = 3;
    private final char[][] board;
    private final Random random;
    private User currentPlayer;

    /**
     * The TicTacToeGame class represents a game of Tic-Tac-Toe.
     */
    public TicTacToeGame() {
        board = new char[SIZE][SIZE];
        random = new Random();
    }

    /**
     * Initializes the game board by setting all elements to the empty mark.
     *
     * The game board is represented as a 2D array of size SIZE x SIZE, where each element
     * represents a cell of the board. This method sets all elements of the array to the
     * empty mark, indicating that the corresponding cell is empty.
     *
     * Note: This method does not return anything. It modifies the state of the object.
     */
    public void initializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY_MARK;
            }
        }
    }

    /**
     * Makes a move for the bot on the Tic-Tac-Toe board.
     * The bot selects a random row and column until it finds an empty position on the board.
     * The selected position is then marked with the bot's mark.
     */
    public void botMove() {
        int row, col;
        while (true) {
            row = random.nextInt(SIZE);
            col = random.nextInt(SIZE);
            if (isValidMove(row, col)) {
                board[row][col] = BOT_MARK;
                break;
            }
        }
    }

    /**
     * Checks if a move is valid at the specified row and column on the Tic-Tac-Toe board.
     *
     * @param row The row index of the move.
     * @param col The column index of the move.
     * @return {@code true} if the move is valid, {@code false} otherwise.
     */
    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] == EMPTY_MARK;
    }

    /**
     * Checks if the given mark has won the Tic-Tac-Toe game.
     *
     * @param mark The mark to check for the win.
     * @return true if the mark has won the game, false otherwise.
     */
    public boolean checkWin(char mark) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark) return true;
            if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark) return true;
        }
        if (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark) return true;
        return board[0][2] == mark && board[1][1] == mark && board[2][0] == mark;
    }

    /**
     * Checks if the Tic-Tac-Toe board is full.
     *
     * @return {@code true} if the board is full, {@code false} otherwise.
     */
    public boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY_MARK) return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the mark used by the player.
     *
     * @return The player mark.
     */
    public char getPlayerMark() {
        return PLAYER_MARK;
    }

    /**
     * Retrieves the mark used by the bot.
     *
     * @return The bot mark.
     */
    public char getBotMark() {
        return BOT_MARK;
    }

    /**
     * Makes a move on the Tic-Tac-Toe board at the specified row and column.
     *
     * @param row The row index of the move.
     * @param col The column index of the move.
     */
    public void makeMove(int row, int col) {
        board[row][col] = PLAYER_MARK;
    }
    public EmbedBuilder buildBoardEmbed(String title) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(title, "https://fluffici.eu", ICON_QUESTION_MARK);
        embed.setColor(Color.CYAN);
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (j == 0) {
                    // Add left padding for each row
                    boardString.append("| ");
                }
                // Add cell content with padding
                boardString.append(board[i][j] == EMPTY_MARK ? " " : board[i][j]);
                boardString.append(" | ");
            }
            // Add horizontal line between rows
            boardString.append("\n");
            if (i < SIZE - 1) {
                boardString.append("|");
                for (int k = 0; k < SIZE; k++) {
                    // Add horizontal line
                    boardString.append("---");
                    if (k < SIZE - 1) {
                        boardString.append("|");
                    }
                }
                boardString.append("\n");
            }
        }

        embed.setTimestamp(Instant.now());
        embed.setDescription("```" + boardString + "```"); // Wrap in code block for monospace font
        return embed;
    }


    /**
     * Builds a list of buttons for a TicTacToe game.
     *
     * @return The list of buttons.
     */
    public List<ItemComponent> buildButtons() {
        List<ItemComponent> buttons = new ArrayList<>();
        int buttonsPerRow = SIZE;
        for (int i = 0; i < SIZE; i += buttonsPerRow) {
            for (int j = 0; j < buttonsPerRow; j++) {
                int index = i + j;
                if (index >= SIZE) {
                    break;
                }
                buttons.add(Button.primary("button:move:" + index + ":" + index, board[index][index] == EMPTY_MARK ? " " : Character.toString(board[index][index]))
                        .withDisabled(board[index][index] != EMPTY_MARK));
            }
        }
        return buttons;
    }

    public List<ItemComponent> buildPlayerGrid() {
        return buildPlayerGridRows(SIZE).stream()
                .flatMap(actionRow -> actionRow.getComponents().stream())
                .map(ItemComponent.class::cast)
                .collect(Collectors.toList());
    }

    private List<ActionRow> buildPlayerGridRows(int buttonsPerRow) {
        List<ActionRow> rows = new ArrayList<>();
        List<ItemComponent> rowComponents = new ArrayList<>();
        for (int i = 0; i < SIZE; i += buttonsPerRow) {
            for (int j = 0; j < buttonsPerRow; j++) {
                int index = i + j;
                if (index >= SIZE) {
                    break;
                }
                rowComponents.add(Button.primary("move:" + index + ":" + index, Character.toString(PLAYER_MARK)));
            }
            rows.add(ActionRow.of(rowComponents));
            rowComponents.clear();
        }
        return rows;
    }
}
