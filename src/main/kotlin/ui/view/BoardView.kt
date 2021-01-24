package ui.view

import board.*
import javafx.geometry.HPos
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import piece.Piece
import tornadofx.*
import ui.controller.BoardController
import ui.controller.RenderObject

/**
 * Main view representing the chess board UI.
 *
 * @author Dominik Hoftych
 */
class BoardView : View() {

    /**
     * Reference to controller in which mouse clicks are processed.
     */
    private val controller: BoardController by inject()

    /**
     * Matrix of [Rectangle]s that represent individual squares of the board
     */
    private val boardSquares: Matrix<Rectangle> = Matrix(8, 8) { _, _ ->
        Rectangle(0.0, 0.0, 80.0, 80.0).apply { arcWidth = 3.0; arcHeight = 3.0 }
    }

    /**
     * Matrix of [Label]s that contain the piece images
     */
    private val boardPieces: Matrix<Label> = Matrix(8, 8) { _, _ ->
        Label().apply {
            // workaround so the piece is centered
            GridPane.setHalignment(this, HPos.CENTER)
        }
    }

    /**
     * Root grid pane that consists of a 8x8 matrix of child panes, each of those representing
     * a single square on the chess board.
     *
     * Each square registers a left-mouse click listener, which will either select given piece,
     * or move with the selected piece.
     */
    override val root = gridpane {

        val initialBoard = Board()
        initialBoard.squares.forEachRow { rowSquares ->
            row {
                rowSquares.map { initSquare(this, it.position) }
            }
        }

        redrawBoard(initialBoard)

        onRightClick {
            controller.resetSelection()
            repaintBoard()
        }
    }

    /**
     * Initializes UI of the square on given [position] and registers mouse left-click listener
     */
    private fun initSquare(parent: Pane, position: Position) : Pane {
        // could not center the piece image other than using another nested gridpane :(
        return parent.gridpane {
            add(boardSquares[position])
            add(boardPieces[position])

            onLeftClick {
                val renderObject: RenderObject = controller.onSquareClicked(position)
                updateView(renderObject)
            }
        }
    }

    /**
     * Updates the view according to given [renderObject]
     */
    private fun updateView(renderObject: RenderObject) {
        when (renderObject) {
            is RenderObject.Nothing -> Unit
            is RenderObject.UpdatedBoard -> redrawBoard(renderObject.board)
            is RenderObject.SelectedPiece -> renderSelectedPiece(renderObject.piece, renderObject.allowedMoves)
        }
    }

    /**
     * Renders the currently selected piece and its allowed moves.
     *
     * @param selectedPiece the currently selected piece
     * @param movePositions set of positions to which it's legal to move the piece
     */
    private fun renderSelectedPiece(selectedPiece: Piece, movePositions: Set<Position>) {
        repaintBoard()
        boardSquares[selectedPiece.position].fill = Color.OLIVE
        movePositions.forEach { boardSquares[it].fill = Color.FORESTGREEN }
    }

    /**
     * Redraw the whole board, i.e. repaint squares to default colors and also render pieces on their
     * current positions
     *
     * @param board the updated board
     */
    private fun redrawBoard(board: Board) {
        repaintBoard()
        board.squares.forEach { boardPieces[it.position].graphic = it.piece?.img }
    }

    /**
     * Paints the board squares to default colors.
     */
    private fun repaintBoard() {
        boardSquares.forEachIndexed { row, col, square ->
            square.fill = getSquareColor(row, col)
        }
    }

    /**
     * Returns correct color for square on given [row] and [column]
     */
    private fun getSquareColor(row: Int, column: Int): Color = if((row * 7 + column) % 2 < 1) Color.SANDYBROWN else Color.SADDLEBROWN

}