package me.kb.ga.sudoku;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum SudokuType {
    SUDOKU_9(9, 3, 3),

    SUDOKU_6(6, 2, 3),
    SUDOKU_8(8, 2, 4),
    SUDOKU_10(10, 2, 5),
    SUDOKU_12(12, 3, 4),
    SUDOKU_16(16, 4, 4),
    SUDOKU_25(25, 5, 5);

    private final int size;
    private final int blockHeight;
    private final int blockWidth;

    SudokuType(int size, int blockHeight, int blockWidth) {
        this.size = size;
        this.blockHeight = blockHeight;
        this.blockWidth = blockWidth;

        if (blockHeight * blockWidth != size) {
            throw new IllegalArgumentException("Invalid sudoku dimensions for " + name());
        }
    }

    public int getBlockXCount() {
        return size / blockWidth;
    }

    public int getBlockYCount() {
        return size / blockHeight;
    }


    public static SudokuType getByBoardSize(int size) {
        for (SudokuType type : SudokuType.values()) {
            if (type.size == size) {
                return type;
            }
        }
        return null;
    }
}
