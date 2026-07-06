package me.kb.ga.sudoku;

import lombok.Getter;
import me.kb.ga.sudoku.matrix.SudokuArrayMatrix;
import me.kb.ga.sudoku.matrix.SudokuMatrix;
import me.kb.ga.sudoku.matrix.SudokuMatrixView;

public class SudokuBoard implements SudokuMatrixView {

    private final SudokuMatrix matrix;
    @Getter
    private final SudokuType type;


    public SudokuBoard(SudokuType type) {
        this.type = type;
        this.matrix = new SudokuArrayMatrix(new int[type.getSize()][type.getSize()]);
    }

    // Выбрасывает IllegalArgument если конфигурация доски неверная. При этом корректность решения не проверяется
    public SudokuBoard(SudokuMatrix board) {
        if (board.getHeight() != board.getWidth()) {
            throw new IllegalArgumentException("Boards with different height and width are currently not supported");
        }

        this.type = SudokuType.getByBoardSize(board.getHeight());
        if (type == null || !validateBoard(board, type)) {
            throw new IllegalArgumentException("Invalid board");
        }
        this.matrix = board.copy();
    }

    // allowReplace - разрешает замену уже установленных чисел; замена на 0 разрешена всегда
    // force - ставит число даже если доска станет неверной
    public boolean setNumber(int x, int y, int number, boolean allowReplace, boolean force) {
        if (number > type.getSize() || number < 0) throw new IllegalArgumentException("Invalid number");
        if (x < 0 || x >= type.getSize() || y < 0 || y >= type.getSize()) throw new IllegalArgumentException("Invalid position");
        if (!force && !canSetNumber(x, y, number, allowReplace)) {
            return false;
        }

        matrix.setNumber(x, y, number);

        return true;
    }

    // allowReplace - разрешает замену уже установленных чисел; замена на 0 разрешена всегда
    public boolean canSetNumber(int x, int y, int number, boolean allowReplace) {
        if (number > type.getSize() || number < 0) throw new IllegalArgumentException("Invalid number");
        if (x < 0 || x >= type.getSize() || y < 0 || y >= type.getSize()) throw new IllegalArgumentException("Invalid position");

        if (!allowReplace && number != 0 && getNumber(x, y) != 0) return false;

        return isValid(new SudokuMatrixView() {
            @Override
            public int getNumber(int mx, int my) {
                return (x == mx && y == my) ? number : matrix.getNumber(mx, my);
            }

            @Override
            public int getWidth() {
                return matrix.getWidth();
            }

            @Override
            public int getHeight() {
                return matrix.getHeight();
            }
        });
    }

    public boolean isValid() {
        return isValid(this);
    }

    public int countErrors() {
        return countErrors(this, false);
    }

    private boolean isValid(SudokuMatrixView view) {
        return countErrors(view, true) == 0;
    }

    private int countErrors(SudokuMatrixView view, boolean stopAfterFirst) {
        int errors = 0;
        for (int i = 0; i < type.getBlockXCount(); i++) {
            for (int j = 0; j < type.getBlockYCount(); j++) {
                int subBlockErrors = countSubBlockErrors(view, i, j, stopAfterFirst);
                errors += subBlockErrors;
                if (errors != 0 && stopAfterFirst) {
                    return errors;
                }
            }
        }

        for (int x = 0; x < view.getWidth(); x++) {
            boolean[] numbersPresent = new boolean[type.getSize()];

            for (int y = 0; y < view.getHeight(); y++) {
                if (!validateNumber(view, numbersPresent, x, y)) {
                    errors++;
                    if (stopAfterFirst)
                        return errors;
                }
            }
        }

        for (int y = 0; y < view.getHeight(); y++) {
            boolean[] numbersPresent = new boolean[type.getSize()];

            for (int x = 0; x < view.getWidth(); x++) {
                if (!validateNumber(view, numbersPresent, x, y)) {
                    errors++;
                    if (stopAfterFirst)
                        return errors;
                }
            }
        }


        return errors;
    }

    public boolean isSubBlockValid(int x, int y) {
        return isSubBlockValid(matrix, x, y);
    }

    public int countSubBlockErrors(int x, int y) {
        return countSubBlockErrors(matrix, x, y, false);
    }

    private boolean isSubBlockValid(SudokuMatrixView view, int x, int y) {
        return countSubBlockErrors(view, x, y, true) == 0;
    }

    private int countSubBlockErrors(SudokuMatrixView view, int x, int y, boolean stopAfterFirst) {

        int errors = 0;
        SudokuMatrixView blockView = getBlockView(view, x, y);

        boolean[] numbersPresent = new boolean[type.getSize()];

        for (int dx = 0; dx < blockView.getWidth(); dx++) {
            for (int dy = 0; dy < blockView.getHeight(); dy++) {
                if (!validateNumber(blockView, numbersPresent, dx, dy)) {
                    errors++;
                    if (stopAfterFirst) {
                        return errors;
                    }
                }
            }
        }

        return errors;
    }

    private boolean validateNumber(SudokuMatrixView view, boolean[] numbersPresent, int x, int y) {
        int number = view.getNumber(x, y);
        if (number != 0) {
            if (numbersPresent[number - 1]) {
                return false;
            }
            numbersPresent[number - 1] = true;
        }

        return true;
    }

    private SudokuMatrixView getBlockView(SudokuMatrixView view, int bx, int by) {
        if (bx >= type.getBlockXCount() || by >= type.getBlockYCount() || bx < 0 || by < 0) {
            throw new IllegalArgumentException("Invalid block position: " + bx + ", " + by);
        }

        return new SudokuMatrixView() {
            @Override
            public int getNumber(int x, int y) {
                return view.getNumber(bx * type.getBlockWidth() + x, by * type.getBlockHeight() + y);
            }

            @Override
            public int getWidth() {
                return type.getBlockWidth();
            }

            @Override
            public int getHeight() {
                return type.getBlockHeight();
            }
        };
    }

    private static boolean isComplete(SudokuMatrixView view) {
        for (int i = 0; i < view.getWidth(); i++) {
            for (int j = 0; j < view.getHeight(); j++) {
                if (view.getNumber(i, j) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateBoard(SudokuMatrixView board, SudokuType type) {
        if (board.getHeight() != type.getSize() || board.getWidth() != type.getSize()) return false;

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.getNumber(i, j) > type.getSize() || board.getNumber(i, j) < 0) return false;
            }
        }

        return true;
    }

    public boolean isEmpty(int x, int y) {
        return matrix.getNumber(x, y) == 0;
    }

    @Override
    public int getNumber(int x, int y) {
        return matrix.getNumber(x, y);
    }

    @Override
    public int getWidth() {
        return matrix.getWidth();
    }

    @Override
    public int getHeight() {
        return matrix.getHeight();
    }

    public SudokuBoard copy() {
        return new SudokuBoard(matrix);
    }
}
