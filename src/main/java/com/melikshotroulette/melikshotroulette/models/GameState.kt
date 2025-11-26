package com.melikshotroulette.melikshotroulette.models

enum class GameMode {
    SINGLE_PLAYER,
    TWO_PLAYER
}

enum class ItemType {
    BULLET_ORDER_SWITCHER,
    DOUBLE_TRIGGER,
    CIGARETTE,
    DOUBLE_DAMAGE
}

data class Item(
    val type: ItemType,
    val name: String,
    val description: String
) {
    companion object {
        fun create(type: ItemType): Item {
            return when (type) {
                ItemType.BULLET_ORDER_SWITCHER -> Item(
                    type,
                    "Bullet Order Switcher",
                    "Changes the order of bullets in the drum"
                )
                ItemType.DOUBLE_TRIGGER -> Item(
                    type,
                    "Double Trigger",
                    "Fire twice in the next round"
                )
                ItemType.CIGARETTE -> Item(
                    type,
                    "Cigarette",
                    "Restores 1 life (max 5)"
                )
                ItemType.DOUBLE_DAMAGE -> Item(
                    type,
                    "Double Damage",
                    "Next shot deals 2 damage"
                )
            }
        }
    }
}

data class Player(
    val name: String,
    var lives: Int = 3,
    val maxLives: Int = 5,
    val items: MutableList<Item?> = MutableList(8) { null },
    var roundsSurvived: Int = 0
) {
    fun isAlive(): Boolean = lives > 0
    
    fun addItem(item: Item): Boolean {
        val emptySlot = items.indexOfFirst { it == null }
        if (emptySlot != -1) {
            items[emptySlot] = item
            return true
        }
        return false
    }
    
    fun removeItem(index: Int): Item? {
        val item = items.getOrNull(index)
        if (item != null) {
            items[index] = null
        }
        return item
    }
    
    fun useItem(index: Int, gameState: GameState): Boolean {
        val item = items.getOrNull(index) ?: return false
        
        return when (item.type) {
            ItemType.BULLET_ORDER_SWITCHER -> {
                gameState.shuffleChambers()
                removeItem(index)
                true
            }
            ItemType.DOUBLE_TRIGGER -> {
                gameState.doubleTriggerActive = true
                removeItem(index)
                true
            }
            ItemType.CIGARETTE -> {
                if (lives < maxLives) {
                    lives++
                    removeItem(index)
                    true
                } else {
                    false
                }
            }
            ItemType.DOUBLE_DAMAGE -> {
                gameState.doubleDamageActive = true
                removeItem(index)
                true
            }
        }
    }
}

enum class ShootTarget {
    SELF,
    OPPONENT
}

class GameState(val mode: GameMode, player1Character: String = "Player 1", player2Character: String = "Player 2") {
    var currentChamberIndex: Int = 0
    var currentPlayerIndex: Int = 0
    var roundNumber: Int = 1
    val players: MutableList<Player> = mutableListOf()
    private val chambers: MutableList<Boolean> = mutableListOf() // true = loaded, false = empty
    var doubleTriggerActive: Boolean = false
    var doubleDamageActive: Boolean = false
    
    init {
        initializePlayers(player1Character, player2Character)
        loadChambers()
    }
    
    private fun initializePlayers(player1Character: String, player2Character: String) {
        players.clear()
        when (mode) {
            GameMode.SINGLE_PLAYER -> {
                players.add(Player(player1Character))
            }
            GameMode.TWO_PLAYER -> {
                players.add(Player(player1Character))
                players.add(Player(player2Character))
            }
        }
    }
    
    private fun loadChambers() {
        chambers.clear()
        // Random number of loaded chambers (1-4 out of 6)
        val loadedCount = (1..4).random()
        repeat(loadedCount) { chambers.add(true) }
        repeat(6 - loadedCount) { chambers.add(false) }
        chambers.shuffle()
        currentChamberIndex = 0
        
        // Print chamber contents
        println("\n╔════════════════════════════════════╗")
        println("║     NEW CHAMBER LOADED             ║")
        println("╚════════════════════════════════════╝")
        println("Chamber contents:")
        chambers.forEachIndexed { index, isLoaded ->
            val bullet = if (isLoaded) "🔴 LOADED" else "⚪ EMPTY"
            println("  Position ${index + 1}: $bullet")
        }
        println("Total: $loadedCount loaded, ${6 - loadedCount} empty")
        println("════════════════════════════════════\n")
    }
    
    fun getCurrentPlayer(): Player = players[currentPlayerIndex]
    
    fun getOpponentPlayer(): Player? {
        if (mode == GameMode.TWO_PLAYER) {
            return players[(currentPlayerIndex + 1) % 2]
        }
        return null
    }
    
    fun getCurrentChamber(): Boolean = chambers[currentChamberIndex]
    
    fun getTotalChambers(): Int = chambers.size
    
    fun getRemainingChambers(): Int = chambers.size - currentChamberIndex
    
    fun getLoadedCount(): Int = chambers.count { it }
    
    fun getEmptyCount(): Int = chambers.count { !it }
    
    fun shoot(target: ShootTarget): ShootResult {
        val isLoaded = getCurrentChamber()
        val shooter = getCurrentPlayer()
        val targetPlayer = if (target == ShootTarget.SELF) shooter else getOpponentPlayer()!!
        
        // Log round information
        println("\n========== ROUND $roundNumber ==========")
        println("Shooter: ${shooter.name}")
        println("Target: ${if (target == ShootTarget.SELF) "Self" else targetPlayer.name}")
        println("Chamber: ${if (isLoaded) "LOADED" else "EMPTY"}")
        println("Remaining chambers: ${getRemainingChambers()}")
        
        currentChamberIndex++
        
        val result = if (isLoaded) {
            val damage = if (doubleDamageActive) 2 else 1
            targetPlayer.lives -= damage
            println("Result: HIT! -$damage life")
            println("${targetPlayer.name} lives: ${targetPlayer.lives}/${targetPlayer.maxLives}")
            doubleDamageActive = false
            if (target == ShootTarget.SELF) {
                ShootResult.SELF_HIT
            } else {
                ShootResult.OPPONENT_HIT
            }
        } else {
            println("Result: CLICK! Empty chamber")
            if (target == ShootTarget.SELF) {
                ShootResult.SELF_SAFE
            } else {
                ShootResult.OPPONENT_SAFE
            }
        }
        
        // Check if we need to reload
        if (currentChamberIndex >= chambers.size) {
            println("Reloading chambers...")
            loadChambers()
        }
        
        // Spawn items after a successful shot (loaded chamber)
        if (isLoaded) {
            spawnRandomItemsForAllPlayers()
            println("Items spawned for all players")
        }
        
        println("====================================\n")
        
        return result
    }
    
    private fun spawnRandomItemsForAllPlayers() {
        players.forEach { player ->
            val itemCount = (1..3).random()
            repeat(itemCount) {
                val randomType = ItemType.values().random()
                player.addItem(Item.create(randomType))
            }
        }
    }
    
    fun shuffleChambers() {
        val remaining = chambers.subList(currentChamberIndex, chambers.size)
        remaining.shuffle()
    }
    
    fun nextTurn(lastResult: ShootResult) {
        // If player shot themselves with empty chamber, they keep their turn
        val keepTurn = lastResult == ShootResult.SELF_SAFE
        
        if (mode == GameMode.TWO_PLAYER && !doubleTriggerActive && !keepTurn) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        }
        
        if (doubleTriggerActive) {
            doubleTriggerActive = false
        }
        
        roundNumber++
    }
    
    fun isGameOver(): Boolean {
        return players.any { !it.isAlive() }
    }
    
    fun getWinner(): Player? {
        return players.firstOrNull { it.isAlive() }
    }
}

enum class ShootResult {
    SELF_SAFE,
    SELF_HIT,
    OPPONENT_SAFE,
    OPPONENT_HIT
}
