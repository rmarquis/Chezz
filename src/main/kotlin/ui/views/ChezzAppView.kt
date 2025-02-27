package ui.views

import tornadofx.*
import tornadofx.View

/**
 * Main view of the Chezz application, grouping together other views in a borderpane
 *
 * @author Dominik Hoftych
 */
class ChezzAppView : View("Chezz") {

    override val root = borderpane {
        top<MenuView>()
        center<BoardView>()
        right<TimerView>()
    }
}
