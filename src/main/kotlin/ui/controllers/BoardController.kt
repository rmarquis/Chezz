package ui.controllers

import board.*
import game.*
import game.GameResult.Checkmate
import game.GameResult.Stalemate
import io.PgnExporter
import io.PgnImporter
import piece.*
import tornadofx.Controller
import tornadofx.Scope
import tornadofx.ItemViewModel
import ui.controllers.ViewUpdate.*
import ui.dialogs.PromotionPieceModel
import ui.dialogs.PromotionDialog
import ui.views.BoardView
import java.io.File
import kotlin.properties.Delegates.observable


/**
 * Main controller of the chess board UI.
 *
 * @author Dominik Hoftych
 */
class BoardController : Controller() {

    /**
     * Reference to the board view where stuff is rendered
     */
    private val boardView: BoardView by inject()

    /**
     * The current board state. On each change, it resets the current selection,
     * updates the [boardView] according to the new board state, checks for promotion,
     * and checks whether the game hasn't ended yet
     */
    private var currentBoard: Board by observable(initialValue = Board.EMPTY) { _, _, newBoard ->
        boardView.updateView(BoardChanged(newBoard))
        checkGameOver()
        resetSelection()
    }

    /**
     * Currently selected piece, or null if no piece is selected
     */
    private var selectedPiece: Piece? = null

    /**
     * Allowed moves of the currently selected piece, or empty map if no piece is selected
     */
    private var allowedMovesBySquare: Map<Square, Move> = emptyMap()

    /**
     * Mouse left-click listener registered on each square.
     * The returned [ViewUpdate] contains all necessary data for the [BoardView]
     * to know what to render.
     */
    fun onSquareClicked(clickedPosition: Position) {
        val clickedSquare: Square = currentBoard.getSquare(clickedPosition)

        println("Clicked on square $clickedSquare")

        if (selectedPiece == null) {
            if (clickedSquare.piece != null) selectPiece(clickedSquare.piece)
        } else {
            if (clickedSquare occupiedBySamePlayerAs selectedPiece!!) {
                selectPiece(clickedSquare.piece!!)
            } else {
                tryMoveTo(clickedSquare)
            }
        }
    }

    /**
     * Selects given [piece] and updates the view accordingly
     */
    private fun selectPiece(piece: Piece) {
        if (piece.player != currentBoard.playerOnTurn) return

        selectedPiece = piece
        allowedMovesBySquare = piece.getAllowedMoves(currentBoard).associateBy {
            when (it) {
                is BasicMove -> currentBoard.getSquare(it.to)
                is PromotionMove -> currentBoard.getSquare(it.basicMove.to)
                is EnPassantMove -> currentBoard.getSquare(it.to)
                is CastlingMove -> currentBoard.getSquare(it.king.second)
            }
        }

        boardView.updateView(PieceSelected(
            piece = piece,
            allowedMoves = allowedMovesBySquare.keys,
            checkedKing = if (currentBoard.isCheck()) currentBoard.getKing() else null
        ))
    }

    /**
     * Based on the [clickedSquare] either
     * - moves with the selected piece to the [clickedSquare] if it's unoccupied
     * - captures the opponent's piece currently occupying the [clickedSquare]
     * - does nothing if a move to the [clickedSquare] is not possible
     */
    private fun tryMoveTo(clickedSquare: Square) {
        val move: Move = allowedMovesBySquare[clickedSquare] ?: return

        currentBoard = if (move is BasicMove && move.isPromotionMove) {
            currentBoard.playMove(PromotionMove(move, observePromotedPiece(move)))
        } else {
            currentBoard.playMove(move)
        }
    }

    /**
     * We need to observe the promotion to know the piece the player chose to
     * promote its pawn to.
     * To achieve this, we can use some TornadoFX hacks: if we open the [PromotionDialog]
     * in a particular [Scope] and provide it with an [ItemViewModel] implementation,
     * we are able to retrieve whatever gets changed and committed to the model, in that scope.
     */
    private fun observePromotedPiece(move: BasicMove): Piece {
        val (pawn, destination) = move

        val model = PromotionPieceModel()
        val scope = Scope(model)
        boardView.openPromotionWindow(scope, pawn moveTo destination)

        return model.pieceType.value
    }

    /**
     * Checks whether the game has ended and call board view if necessary
     */
    private fun checkGameOver() {
        when {
            currentBoard.isCheckmate() -> boardView.openGameOverWindow(Checkmate(currentBoard.playerOnTurn.theOtherPlayer))
            currentBoard.isStalemate() -> boardView.openGameOverWindow(Stalemate)
        }
    }

    /**
     * Mouse right-click listener registered on the whole board, used to reset (i.e. deselect) the currently selected piece
     */
    fun resetSelection() {
        selectedPiece = null
        allowedMovesBySquare = emptyMap()
    }

    /**
     * Wipes any current game state and runs game starting in given [gameState].
     * If no [gameState] is provided, a fresh game is started with pieces in their
     * initial positions and white player on turn.
     */
    fun startGame(gameState: Board = Board.INITIAL) {
        currentBoard = gameState
    }

    /**
     * Undoes the last move, or does nothing if the game hasn't started yet
     */
    fun undoLastMove() {
        if (currentBoard != Board.EMPTY) {
            currentBoard = currentBoard.previousBoard ?: currentBoard
        }
    }

    /**
     * Processes given [pgnFile] and initializes the board in the state represented
     * by the pgn file
     */
    fun importPgn(pgnFile: File) {
        currentBoard = PgnImporter.importPgn(pgnFile)
    }

    /**
     * Processes current board state to a string in pgn format
     */
    fun exportPgn(): String = PgnExporter.exportToPgn(currentBoard)

}
