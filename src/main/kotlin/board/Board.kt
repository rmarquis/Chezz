package board

import game.Player
import piece.*
import tornadofx.find
import tornadofx.getProperty
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Chess board with a 8x8 array of [Square]s holding the current game state.
 *
 * @param squares 8x8 [Matrix] of [Square]s
 *
 * @author Dominik Hoftych
 */
class Board(val squares: Matrix<Square> = Matrix(8, 8) { row, col -> Square(row, col) }) {

    /**
     * Set pieces on the board
     */
    init {
        squares[0].forEach { it.piece = resolvePiece(it, Player.BLACK) }
        squares[1].forEach { it.piece = Pawn(Player.BLACK, it.position) }
        squares[6].forEach { it.piece = Pawn(Player.WHITE, it.position) }
        squares[7].forEach { it.piece = resolvePiece(it, Player.WHITE) }
    }

    /**
     * Returns the correct piece to be set on given [Square], or null if the [Square] should be empty
     */
    private fun resolvePiece(square: Square, player: Player): Piece = when(square.file) {
        'a' -> Rook(player, square.position)
        'b' -> Knight(player, square.position)
        'c' -> Bishop(player, square.position)
        'd' -> Queen(player, square.position)
        'e' -> King(player, square.position)
        'f' -> Bishop(player, square.position)
        'g' -> Knight(player, square.position)
        'h' -> Rook(player, square.position)
        else -> throw IllegalStateException("Tile out of bounds")
    }

    /**
     * Returns the [Square] based on given [Position], or throw exception if given position is not on the board
     */
    fun getSquare(position: Position): Square {
        return if (position.onBoard) squares[position] else throw IllegalArgumentException("Position not on board")
    }

    /**
     * Returns the [Square] based on given [Position], or null if the given position is not on the board
     */
    fun getSquareOrNull(position: Position): Square? {
        return if (position.onBoard) squares[position] else null
    }

    /**
     * Returns the [Square] currently occupied by the given [piece]
     */
    fun getSquareFor(piece: Piece): Square {
        return getSquare(piece.position)
    }

    /**
     * Returns the piece on the [Square] on given [position], or null if there's no piece
     */
    fun getPiece(position: Position): Piece? = squares[position].piece

    /**
     * Returns all pieces of given [player] and [type]
     */
    fun getPiecesFor(player: Player, type: KClass<out Piece>): List<Piece> {
        return squares.matrix.flatten()
                .mapNotNull { it.piece }
                .filter { it.player == player }
                .filter { it::class == type }
    }

    /**
     * Removes piece from the square on given [position]
     */
    fun removePiece(position: Position) {
        squares[position].piece = null
    }

}

/**
 * Extension function allowing to use [Position] as an index to the receiver [Matrix]
 */
operator fun <T> Matrix<T>.get(position: Position): T = matrix[position.row][position.col]

/**
 * Print the board with pieces as unicode symbols
 */
fun Board.printUnicode() {
    squares.forEachRow { row ->
        row.forEach { square ->
            print(" ${square.piece.unicode} ")
        }
        println()
    }
}
