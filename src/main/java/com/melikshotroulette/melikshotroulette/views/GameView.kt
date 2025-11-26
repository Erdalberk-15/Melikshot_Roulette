package com.melikshotroulette.melikshotroulette.views

import com.melikshotroulette.melikshotroulette.GameController
import com.melikshotroulette.melikshotroulette.models.*
import com.melikshotroulette.melikshotroulette.utils.SoundManager
import javafx.animation.*
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration

class GameView(private val gameState: GameState, private val controller: GameController) {
    val scene: Scene
    private val root: BorderPane
    private val player1Box: VBox
    private val player2Box: VBox
    private val centerBox: VBox
    private val player1LifeLabel: Label
    private val player2LifeLabel: Label
    private val player1ItemsGrid: GridPane
    private val player2ItemsGrid: GridPane
    private val chamberInfoLabel: Label
    private val shotgunLabel: Label
    private val shootSelfButton: Button
    private val shootOpponentButton: Button
    private val resultLabel: Label
    
    init {
        root = BorderPane()
        root.style = "-fx-background-color: #1a1a1a;"
        
        // Left side - Player 2
        player2Box = VBox(15.0)
        player2Box.alignment = Pos.TOP_CENTER
        player2Box.padding = javafx.geometry.Insets(20.0)
        player2Box.prefWidth = 250.0
        
        val p2Title = Label("PLAYER 2")
        p2Title.font = Font.font("Arial", FontWeight.BOLD, 24.0)
        p2Title.textFill = Color.web("#00aaff")
        
        player2LifeLabel = Label()
        player2LifeLabel.font = Font.font("Arial", FontWeight.BOLD, 24.0)
        player2LifeLabel.textFill = Color.web("#ff4444")
        player2LifeLabel.style = "-fx-letter-spacing: 5px;"
        
        player2ItemsGrid = createItemsGrid()
        
        // Only show Player 2 in two-player mode
        if (gameState.mode == GameMode.TWO_PLAYER) {
            player2ItemsGrid.children.forEach { node ->
                if (node is VBox) {
                    setupItemClickHandler(node, 1)
                }
            }
            
            // Add character image for Player 2
            val player2CharImage = createCharacterImage(gameState.players[1].name)
            
            player2Box.children.addAll(p2Title, player2LifeLabel, Label("Items:").apply {
                font = Font.font("Arial", FontWeight.BOLD, 16.0)
                textFill = Color.web("#cccccc")
            }, player2ItemsGrid, player2CharImage)
            
            root.left = player2Box
        }
        
        // Right side - Player 1
        player1Box = VBox(15.0)
        player1Box.alignment = Pos.TOP_CENTER
        player1Box.padding = javafx.geometry.Insets(20.0)
        player1Box.prefWidth = 250.0
        
        val p1Title = Label("PLAYER 1")
        p1Title.font = Font.font("Arial", FontWeight.BOLD, 24.0)
        p1Title.textFill = Color.web("#ff8800")
        
        player1LifeLabel = Label()
        player1LifeLabel.font = Font.font("Arial", FontWeight.BOLD, 24.0)
        player1LifeLabel.textFill = Color.web("#ff4444")
        player1LifeLabel.style = "-fx-letter-spacing: 5px;"
        
        player1ItemsGrid = createItemsGrid()
        player1ItemsGrid.children.forEach { node ->
            if (node is VBox) {
                setupItemClickHandler(node, 0)
            }
        }
        
        // Add character image for Player 1
        val player1CharImage = createCharacterImage(gameState.players[0].name)
        
        player1Box.children.addAll(p1Title, player1LifeLabel, Label("Items:").apply {
            font = Font.font("Arial", FontWeight.BOLD, 16.0)
            textFill = Color.web("#cccccc")
        }, player1ItemsGrid, player1CharImage)
        
        root.right = player1Box
        
        // Center - Gun and controls
        centerBox = VBox(30.0)
        centerBox.alignment = Pos.CENTER
        centerBox.padding = javafx.geometry.Insets(20.0)
        
        val title = Label("MELIKSHOT ROULETTE")
        title.font = Font.font("Arial", FontWeight.BOLD, 36.0)
        title.textFill = Color.web("#ff4444")
        
        chamberInfoLabel = Label()
        chamberInfoLabel.font = Font.font("Arial", FontWeight.BOLD, 18.0)
        chamberInfoLabel.textFill = Color.web("#ffaa00")
        
        shotgunLabel = Label("🔫")
        shotgunLabel.font = Font.font(80.0)
        shotgunLabel.style = "-fx-rotate: 0;"
        
        val turnLabel = Label()
        turnLabel.font = Font.font("Arial", FontWeight.BOLD, 24.0)
        turnLabel.textFill = Color.web("#00ff00")
        turnLabel.textProperty().bind(
            javafx.beans.binding.Bindings.createStringBinding(
                { "${gameState.getCurrentPlayer().name}'s Turn" },
            )
        )
        
        resultLabel = Label("")
        resultLabel.font = Font.font("Arial", FontWeight.BOLD, 22.0)
        
        val buttonBox = HBox(20.0)
        buttonBox.alignment = Pos.CENTER
        
        shootSelfButton = createButton("Shoot Self", "#ff4444")
        shootSelfButton.setOnAction { controller.handleShoot(ShootTarget.SELF) }
        
        shootOpponentButton = createButton("Shoot Opponent", "#ff8800")
        shootOpponentButton.setOnAction { controller.handleShoot(ShootTarget.OPPONENT) }
        
        buttonBox.children.addAll(shootSelfButton, shootOpponentButton)
        
        val menuButton = createButton("Back to Menu", "#666666")
        menuButton.setOnAction { controller.backToMenu() }
        
        centerBox.children.addAll(title, chamberInfoLabel, shotgunLabel, turnLabel, resultLabel, buttonBox, menuButton)
        root.center = centerBox
        
        scene = Scene(root, 1200.0, 700.0)
        updateUI()
    }
    
    private fun createItemsGrid(): GridPane {
        val grid = GridPane()
        grid.hgap = 10.0
        grid.vgap = 10.0
        grid.alignment = Pos.CENTER
        
        for (i in 0..7) {
            val slot = createItemSlot(i)
            grid.add(slot, i % 4, i / 4)
        }
        
        return grid
    }
    
    private fun createItemSlot(index: Int): VBox {
        val container = VBox(5.0)
        container.alignment = Pos.CENTER
        container.prefWidth = 50.0
        
        val slot = StackPane()
        slot.prefWidth = 50.0
        slot.prefHeight = 50.0
        
        val bg = Rectangle(50.0, 50.0)
        bg.fill = Color.web("#333333")
        bg.stroke = Color.web("#666666")
        bg.strokeWidth = 2.0
        bg.arcWidth = 10.0
        bg.arcHeight = 10.0
        
        val iconLabel = Label("")
        iconLabel.font = Font.font("Arial", FontWeight.BOLD, 12.0)
        iconLabel.textFill = Color.web("#ffffff")
        iconLabel.style = "-fx-text-alignment: center;"
        
        slot.children.addAll(bg, iconLabel)
        
        val descLabel = Label("")
        descLabel.font = Font.font("Arial", FontWeight.NORMAL, 9.0)
        descLabel.textFill = Color.web("#aaaaaa")
        descLabel.style = "-fx-text-alignment: center;"
        descLabel.maxWidth = 50.0
        descLabel.isWrapText = true
        
        container.children.addAll(slot, descLabel)
        container.userData = index
        
        return container
    }
    
    private fun setupItemClickHandler(container: VBox, playerIndex: Int) {
        val slot = container.children[0] as StackPane
        slot.setOnMouseClicked {
            // Only allow item use if it's the player's turn
            if (gameState.currentPlayerIndex != playerIndex) {
                return@setOnMouseClicked
            }
            
            val index = container.userData as Int
            val player = gameState.players[playerIndex]
            val item = player.items.getOrNull(index)
            if (item != null) {
                if (player.useItem(index, gameState)) {
                    updateUI()
                    showItemUsed(item)
                }
            }
        }
    }
    
    private fun createCharacterImage(characterName: String): VBox {
        val container = VBox(10.0)
        container.alignment = Pos.CENTER
        container.padding = javafx.geometry.Insets(10.0, 0.0, 0.0, 0.0)
        
        // Map character name to image file
        val imagePath = when (characterName) {
            "Kedullah" -> "/com/melikshotroulette/melikshotroulette/images/cha_1.png"
            "FINAL BOSS" -> "/com/melikshotroulette/melikshotroulette/images/cha_2.png"
            "Nokia" -> "/com/melikshotroulette/melikshotroulette/images/cha_3.png"
            "Benji" -> "/com/melikshotroulette/melikshotroulette/images/cha_4.png"
            "Anne Terliği" -> "/com/melikshotroulette/melikshotroulette/images/cha_5.png"
            "Amongus" -> "/com/melikshotroulette/melikshotroulette/images/cha_6.png"
            else -> null
        }
        
        val imageView = if (imagePath != null) {
            try {
                val imageStream = javaClass.getResourceAsStream(imagePath)
                if (imageStream != null) {
                    val image = Image(imageStream)
                    ImageView(image).apply {
                        fitWidth = 180.0
                        fitHeight = 180.0
                        isPreserveRatio = true
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        if (imageView != null) {
            // Add border/frame around character image
            val imageContainer = StackPane(imageView)
            imageContainer.style = """
                -fx-background-color: #2a2a2a;
                -fx-border-color: #666666;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-padding: 5;
            """.trimIndent()
            
            val nameLabel = Label(characterName)
            nameLabel.font = Font.font("Arial", FontWeight.BOLD, 14.0)
            nameLabel.textFill = Color.web("#cccccc")
            nameLabel.style = "-fx-text-alignment: center;"
            
            container.children.addAll(imageContainer, nameLabel)
        }
        
        return container
    }
    
    fun updateUI() {
        // Update player 1
        val p1 = gameState.players[0]
        player1LifeLabel.text = "🖤".repeat(p1.lives) + "💔".repeat(p1.maxLives - p1.lives)
        updateItemsGrid(player1ItemsGrid, p1)
        
        // Update player 2
        if (gameState.mode == GameMode.TWO_PLAYER) {
            val p2 = gameState.players[1]
            player2LifeLabel.text = "🖤".repeat(p2.lives) + "💔".repeat(p2.maxLives - p2.lives)
            updateItemsGrid(player2ItemsGrid, p2)
        }
        
        // Update chamber info
        val loaded = gameState.getLoadedCount()
        val empty = gameState.getEmptyCount()
        val remaining = gameState.getRemainingChambers()
        chamberInfoLabel.text = "Chambers: $remaining left | Loaded: $loaded | Empty: $empty"
        
        // Highlight current player's side
        if (gameState.currentPlayerIndex == 0) {
            player1Box.style = "-fx-background-color: rgba(255, 136, 0, 0.2); -fx-background-radius: 10;"
            player2Box.style = ""
        } else {
            player2Box.style = "-fx-background-color: rgba(0, 170, 255, 0.2); -fx-background-radius: 10;"
            player1Box.style = ""
        }
        
        resultLabel.text = ""
        shootSelfButton.isDisable = false
        shootOpponentButton.isDisable = false
    }
    
    private fun updateItemsGrid(grid: GridPane, player: Player) {
        val playerIndex = gameState.players.indexOf(player)
        val isCurrentPlayer = playerIndex == gameState.currentPlayerIndex
        
        grid.children.forEach { node ->
            if (node is VBox) {
                val index = node.userData as Int
                val item = player.items.getOrNull(index)
                val slot = node.children[0] as StackPane
                val iconLabel = slot.children[1] as Label
                val descLabel = node.children[1] as Label
                
                if (item != null) {
                    iconLabel.text = getItemIcon(item.type)
                    descLabel.text = getItemShortDesc(item.type)
                    
                    if (isCurrentPlayer) {
                        (slot.children[0] as Rectangle).fill = Color.web("#555555")
                        slot.style = "-fx-cursor: hand;"
                        slot.opacity = 1.0
                    } else {
                        (slot.children[0] as Rectangle).fill = Color.web("#444444")
                        slot.style = "-fx-cursor: not-allowed;"
                        slot.opacity = 0.5
                    }
                } else {
                    iconLabel.text = ""
                    descLabel.text = ""
                    (slot.children[0] as Rectangle).fill = Color.web("#333333")
                    slot.style = ""
                    slot.opacity = 1.0
                }
            }
        }
    }
    
    private fun getItemIcon(type: ItemType): String {
        return when (type) {
            ItemType.BULLET_ORDER_SWITCHER -> "🔄"
            ItemType.DOUBLE_TRIGGER -> "⚡"
            ItemType.CIGARETTE -> "🚬"
            ItemType.DOUBLE_DAMAGE -> "💥"
        }
    }
    
    private fun getItemShortDesc(type: ItemType): String {
        return when (type) {
            ItemType.BULLET_ORDER_SWITCHER -> "Shuffle"
            ItemType.DOUBLE_TRIGGER -> "2x Shot"
            ItemType.CIGARETTE -> "+1 Life"
            ItemType.DOUBLE_DAMAGE -> "2x DMG"
        }
    }
    
    fun showShootResult(result: ShootResult) {
        shootSelfButton.isDisable = true
        shootOpponentButton.isDisable = true
        
        // Play sound only when weapon is loaded (HIT results)
        if (result == ShootResult.SELF_HIT || result == ShootResult.OPPONENT_HIT) {
            SoundManager.playGunshot()
        }
        
        when (result) {
            ShootResult.SELF_SAFE -> {
                resultLabel.text = "CLICK! Empty chamber - You're safe!"
                resultLabel.textFill = Color.web("#00ff00")
                animateGun(0.0)
            }
            ShootResult.SELF_HIT -> {
                resultLabel.text = "BANG! You shot yourself! -1 Life"
                resultLabel.textFill = Color.web("#ff0000")
                animateGun(0.0)
                flashScreen(Color.web("#ff0000"))
            }
            ShootResult.OPPONENT_SAFE -> {
                resultLabel.text = "CLICK! Empty chamber - Opponent safe!"
                resultLabel.textFill = Color.web("#ffaa00")
                val rotation = if (gameState.currentPlayerIndex == 0) -90.0 else 90.0
                animateGun(rotation)
            }
            ShootResult.OPPONENT_HIT -> {
                resultLabel.text = "BANG! You hit your opponent! -1 Life"
                resultLabel.textFill = Color.web("#ff8800")
                val rotation = if (gameState.currentPlayerIndex == 0) -90.0 else 90.0
                animateGun(rotation)
                flashScreen(Color.web("#ff8800"))
            }
        }
    }
    
    private fun animateGun(targetRotation: Double) {
        val rotate = RotateTransition(Duration.millis(300.0), shotgunLabel)
        rotate.toAngle = targetRotation
        
        val scaleUp = ScaleTransition(Duration.millis(150.0), shotgunLabel)
        scaleUp.toX = 1.3
        scaleUp.toY = 1.3
        
        val scaleDown = ScaleTransition(Duration.millis(150.0), shotgunLabel)
        scaleDown.toX = 1.0
        scaleDown.toY = 1.0
        
        val resetRotate = RotateTransition(Duration.millis(300.0), shotgunLabel)
        resetRotate.toAngle = 0.0
        
        val sequence = SequentialTransition(
            ParallelTransition(rotate, scaleUp),
            scaleDown,
            PauseTransition(Duration.millis(500.0)),
            resetRotate
        )
        sequence.play()
    }
    
    private fun flashScreen(color: Color) {
        val flash = Rectangle(1200.0, 700.0)
        flash.fill = color
        flash.opacity = 0.0
        
        val overlay = StackPane(flash)
        root.children.add(overlay)
        
        val fadeIn = FadeTransition(Duration.millis(100.0), flash)
        fadeIn.toValue = 0.5
        
        val fadeOut = FadeTransition(Duration.millis(300.0), flash)
        fadeOut.toValue = 0.0
        
        val sequence = SequentialTransition(fadeIn, fadeOut)
        sequence.setOnFinished { root.children.remove(overlay) }
        sequence.play()
    }
    
    private fun showItemUsed(item: Item) {
        val itemLabel = Label("Used: ${item.name}")
        itemLabel.font = Font.font("Arial", FontWeight.BOLD, 18.0)
        itemLabel.textFill = Color.web("#ffff00")
        itemLabel.opacity = 0.0
        
        centerBox.children.add(centerBox.children.indexOf(resultLabel), itemLabel)
        
        val fadeIn = FadeTransition(Duration.millis(300.0), itemLabel)
        fadeIn.toValue = 1.0
        
        val pause = PauseTransition(Duration.millis(1500.0))
        
        val fadeOut = FadeTransition(Duration.millis(300.0), itemLabel)
        fadeOut.toValue = 0.0
        
        val sequence = SequentialTransition(fadeIn, pause, fadeOut)
        sequence.setOnFinished { centerBox.children.remove(itemLabel) }
        sequence.play()
    }
    
    fun showGameOver() {
        val winner = gameState.getWinner()
        val overlay = StackPane()
        overlay.style = "-fx-background-color: rgba(0, 0, 0, 0.9);"
        
        val gameOverBox = VBox(30.0)
        gameOverBox.alignment = Pos.CENTER
        
        val gameOverLabel = Label("GAME OVER")
        gameOverLabel.font = Font.font("Arial", FontWeight.BOLD, 60.0)
        gameOverLabel.textFill = Color.web("#ff4444")
        
        val winnerLabel = Label(
            if (winner != null) {
                "${winner.name} WINS!"
            } else {
                "DRAW!"
            }
        )
        winnerLabel.font = Font.font("Arial", FontWeight.BOLD, 40.0)
        winnerLabel.textFill = if (winner == gameState.players[0]) {
            Color.web("#ff8800")
        } else {
            Color.web("#00aaff")
        }
        
        val statsLabel = Label("Round: ${gameState.roundNumber}")
        statsLabel.font = Font.font("Arial", FontWeight.NORMAL, 24.0)
        statsLabel.textFill = Color.web("#cccccc")
        
        val restartButton = createButton("Play Again", "#ff4444")
        restartButton.setOnAction { controller.restartGame() }
        
        val menuButton = createButton("Main Menu", "#666666")
        menuButton.setOnAction { controller.backToMenu() }
        
        gameOverBox.children.addAll(gameOverLabel, winnerLabel, statsLabel, restartButton, menuButton)
        overlay.children.add(gameOverBox)
        
        root.children.add(overlay)
        
        val fadeIn = FadeTransition(Duration.millis(500.0), overlay)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        fadeIn.play()
    }
    
    private fun createButton(text: String, color: String): Button {
        val button = Button(text)
        button.prefWidth = 250.0
        button.prefHeight = 50.0
        button.font = Font.font("Arial", FontWeight.BOLD, 18.0)
        button.style = """
            -fx-background-color: $color;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """.trimIndent()
        
        button.setOnMouseEntered {
            button.opacity = 0.8
        }
        
        button.setOnMouseExited {
            button.opacity = 1.0
        }
        
        return button
    }
}
