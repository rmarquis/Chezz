package board

/**
 * Specific position on the chess board.
 *
 * @param row row index in range from 0 to 7
 * @param col column index in range from 0 to 7
 *
 * @author Dominik Hoftych
 */
data class Position(val row: Int, val col: Int) {

    /**
     * Rank (row) of this position in range from 1 to 8
     */
    val rank: Int by lazy {
        8 - row
    }

    /**
     * File (column) of this position, named from 'a' to 'h'
     */
    val file: Char by lazy {
        charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')[col]
    }

    /**
     * Whether the position is on the board
     */
    val isOnBoard: Boolean = row in (0..7) && col in (0..7)
}

infix fun Position.add(other: Position) = Position(this.row + other.row, this.col + other.col)
infix fun Position.add(other: Pair<Int, Int>) = this add Position(other.first, other.second)

infix fun Position.sub(other: Pair<Int, Int>) = Position(this.row - other.first, this.col - other.second)
