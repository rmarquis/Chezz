package piece

import board.Board
import board.Position
import game.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * Abstract parent of all chess pieces, i.e. pawn, rook, bishop, knight, queen and king.
 * Each piece belongs to a particular [player], occupies a particular square on the [position] and
 * records its [history], i.e. a list of previous its previous positions.
 *
 * @author Dominik Hoftych
 */
sealed class Piece {

    /**
     * The player to whom the piece belongs
     */
    abstract val player: Player

    /**
     * The position of the piece on the board
     */
    abstract val position: Position

    /**
     * Ordered list of positions that were occupied by the piece previously,
     * including the initial position
     */
    abstract val history: List<Position>

    /**
     * Name of the piece in the "pieceType_colorLetter" format, e.g. Rook_b or King_w.
     * Is used when loading piece images so it MUST correspond.
     */
    abstract val name: String

    /**
     * Whether the piece has already moved from its original position
     */
    val hasMoved: Boolean get() = history.size > 1

    /**
     * Whether the piece belongs to the white player
     */
    val isWhite: Boolean get() = player == Player.WHITE

    /**
     * The opposite player
     */
    val theOtherPlayer: Player get() = if (isWhite) Player.BLACK else Player.WHITE

    /**
     * Movement of the piece defined as a set of directions along x and y axis respectively.
     * Defaults to an empty set but is overridden by each piece type except for pawn, whose movement
     * is a bit more complex.
     */
    open val movement: Set<Direction> = emptySet()

    /**
     * Returns the set of allowed moves w.r.t. given [board].
     * If [validateForCheck] is false, the resulting list may contain moves that
     * would put or leave the king in check, which is generally not allowed in the game.
     * However, such moves are still considered check moves,
     * see https://www.fide.com/FIDE/handbook/LawsOfChess.pdf, paragraph 3.1.
     */
    fun getAllowedMoves(board: Board, validateForCheck: Boolean = true): List<Move> =
        MoveGenerator.generate(this, board, validateForCheck)
}

/**
 * Image (icon) of the piece
 */
val Piece.icon: ImageView
    get() = ImageView(Image("/pieces/$name.png", 50.0, 50.0, true, true))

/**
 * Moves with the piece to the square in given [position].
 * A new instance of [Piece] is initialized on its new position with the move recorded in its history list.
 */
infix fun Piece.moveTo(position: Position): Piece = when (this) {
    is Pawn -> Pawn(player, position, history + position)
    is Rook -> Rook(player, position, history + position)
    is Knight -> Knight(player, position, history + position)
    is Bishop -> Bishop(player, position, history + position)
    is Queen -> Queen(player, position, history + position)
    is King -> King(player, position, history + position)
}

data class Pawn(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "Pawn_${player.color()}"

    /**
     * Direction along rows (either up or down based on pawn's color)
     */
    val rowDirection: Int = if (isWhite) -1 else 1
}

data class Rook(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "Rook_${player.color()}"
    override val movement: Set<Direction> = setOf(
        Direction(-1, 0), // up
        Direction(0, 1), // right
        Direction(1, 0), // down
        Direction(0, -1) // left
    )
}

data class Knight(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "Knight_${player.color()}"
    override val movement: Set<Direction> = setOf(
        Direction(-2, 1), // up->right
        Direction(-1, 2), // right->up
        Direction(1, 2), // right->down
        Direction(2, 1), // down->right
        Direction(2, -1), // down->left
        Direction(1, -2), // left->down
        Direction(-1, -2), // left->up
        Direction(-2, -1), // up->left
    )
}

data class Bishop(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "Bishop_${player.color()}"
    override val movement: Set<Direction> = setOf(
        Direction(-1, 1), // up-right
        Direction(1, 1), // down-right
        Direction(1, - 1), // down-left
        Direction(-1, - 1) // up-left
    )
}

data class Queen(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "Queen_${player.color()}"
    override val movement: Set<Direction> = setOf(
        Direction(-1, 0), // up
        Direction(-1, 1), // up-right
        Direction(0, 1), // right
        Direction(1, 1), // down-right
        Direction(1, 0), // down
        Direction(1, -1), // down-left
        Direction(0, -1), // left
        Direction(-1, -1) // up-left
    )
}

data class King(
    override val player: Player,
    override val position: Position,
    override val history: List<Position> = listOf(position)
) : Piece() {

    override val name: String = "King_${player.color()}"
    override val movement: Set<Direction> = setOf(
        Direction(-1, 0), // up
        Direction(-1, 1), // up-right
        Direction(0, 1), // right
        Direction(1, 1), // down-right
        Direction(1, 0), // down
        Direction(1, -1), // down-left
        Direction(0, -1), // left
        Direction(-1, -1) // up-left
    )
}
