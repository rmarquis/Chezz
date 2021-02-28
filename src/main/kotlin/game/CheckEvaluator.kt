package game

import board.Board
import board.Position
import board.Square
import board.whiteOnTurn
import piece.King
import piece.Piece

/**
 * Returns true if the position is in check w.r.t given [board] state
 */
fun Position.isInCheck(board: Board): Boolean {
    val enemyPieces: List<Piece> = board.getPiecesFor(board.playerOnTurn.theOtherPlayer)
    return this in enemyPieces
        .flatMap { it.getAllowedMoves(board = board, validateForCheck = false) }
        .filterIsInstance<BasicMove>()
        .map { it.to }
}

/**
 * Returns true if the square is in check w.r.t given [board] state
 */
fun Square.isInCheck(board: Board) = position.isInCheck(board)

/**
 * Returns true if the piece is in check w.r.t given [board] state
 */
fun Piece.isInCheck(board: Board) = position.isInCheck(board)

/**
 * Returns true if the king of the player on turn is in check
 */
fun Board.isCheck() = getKing().position.isInCheck(this)

/**
 * Returns true if the king of the player on turn is not in check
 */
fun Board.isNotCheck() = !isCheck()

/**
 * Returns true if the king of the player on turn has been checkmated
 */
fun Board.isCheckmate() = isCheck() && getPiecesFor(playerOnTurn).all { it.getAllowedMoves(this).isEmpty() }

/**
 * Returns true if the stalemate occurred
 */
fun Board.isStalemate() = !isCheck() && getPiecesFor(playerOnTurn).all { it.getAllowedMoves(this).isEmpty() }

/**
 * Returns the king of the player on turn
 */
fun Board.getKing(): Piece = getPiecesFor(playerOnTurn, King::class).first()

/**
 * Returns a [GameResult] describing the current board state
 * TODO verify that this is the right place for this method & how to integrate future WinType.TIMEOUT into this?
 */
fun Board.getGameResult(): GameResult = when {
    isStalemate() -> GameResult.Draw(DrawType.STALEMATE)
    isCheckmate() && whiteOnTurn() -> GameResult.BlackWins(WinType.CHECKMATE)
    isCheckmate() && !whiteOnTurn() -> GameResult.WhiteWins(WinType.CHECKMATE)
    else -> GameResult.StillPlaying
}

