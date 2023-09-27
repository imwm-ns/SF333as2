package com.example.tictactoe.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tictactoe.action.UserAction
import com.example.tictactoe.state.BoardCellValue
import com.example.tictactoe.state.GameState
import com.example.tictactoe.state.VictoryType

class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())

    var boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> {
                addValueToBoard(action.cellNo)
            }

            UserAction.PlayAgainButtonClicked -> {
                gameReset()
            }
        }
    }

    private fun gameReset() {
        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }
        if (state.hintText === "Player 'O' Won." || state.hasDraw) {
            state = state.copy(
                hintText = "Player 'O' turn.",
                currentTurn = BoardCellValue.CIRCLE,
                victoryType = VictoryType.NONE,
                hasWon = false,
                hasDraw = false,
            )
        } else {
            state = state.copy(
                hintText = "Player 'X' turn.",
                currentTurn = BoardCellValue.CROSS,
                victoryType = VictoryType.NONE,
                hasWon = false,
                hasDraw = false,
            )
            makeCrossComputerMove()
        }
    }

    private fun canCrossWin(boardValue: BoardCellValue): Boolean {
        boardItems.forEach { (i, _) ->
            if (boardItems[i] === BoardCellValue.NONE) {
                boardItems[i] = BoardCellValue.CROSS
                if (checkForVictory(BoardCellValue.CROSS)) {
                    state = state.copy(
                        hintText = "Player 'X' Won.",
                        currentTurn = BoardCellValue.NONE,
                        playerCrossCount = state.playerCrossCount + 1,
                        hasWon = true
                    )
                    return true
                }
                boardItems[i] = BoardCellValue.NONE
            }
        }
        return false
    }

    private fun blockCircleIfCanWin(boardValue: BoardCellValue): Boolean {
        boardItems.forEach { (i, _) ->
            if (boardItems[i] === BoardCellValue.NONE) {
                boardItems[i] = BoardCellValue.CIRCLE
                if (checkForVictory(BoardCellValue.CIRCLE)) {
                    boardItems[i] = BoardCellValue.CROSS
                    state = state.copy(
                        hintText = "Player 'O' turn.",
                        currentTurn = BoardCellValue.CIRCLE,
                    )
                    return true
                }
                boardItems[i] = BoardCellValue.NONE
            }
        }
        return false
    }

    private fun makeCrossComputerMove() {
        // Check if 'X' can win in next move.
        if (canCrossWin(BoardCellValue.CROSS)) return
        // Check if 'O' can win then 'X' should block it.
        if (blockCircleIfCanWin(BoardCellValue.CIRCLE)) return
        // Try to place 'X' in a center of the board.
        if (boardItems[5] === BoardCellValue.NONE) {
            boardItems[5] = BoardCellValue.CROSS
            state = state.copy(
                hintText = "Player 'O' turn.",
                currentTurn = BoardCellValue.CIRCLE,
            )
            return
        }
        // Try to random and place sign in the board.
        val emptyCells = boardItems.filter { it.value === BoardCellValue.NONE }.keys
        val index = emptyCells.random()
        boardItems[index] = BoardCellValue.CROSS
        state = state.copy(
            hintText = "Player 'O' turn.",
            currentTurn = BoardCellValue.CIRCLE,
        )
    }

    private fun addValueToBoard(cellNo: Int) {
        if (boardItems[cellNo] != BoardCellValue.NONE) return
        if (state.currentTurn === BoardCellValue.CIRCLE) {
            boardItems[cellNo] = BoardCellValue.CIRCLE
            state = if (checkForVictory(BoardCellValue.CIRCLE)) {
                state.copy(
                    hintText = "Player 'O' Won.",
                    currentTurn = BoardCellValue.NONE,
                    playerCircleCount = state.playerCircleCount + 1,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw.",
                    drawCount = state.drawCount + 1,
                    hasDraw = true,
                )
            } else {
                state.copy(
                    hintText = "Player 'X' turn.",
                    currentTurn = BoardCellValue.CROSS,
                )
            }
        } else if (state.currentTurn === BoardCellValue.CROSS) {
            boardItems[cellNo] = BoardCellValue.CROSS
            state = if (checkForVictory(BoardCellValue.CROSS)) {
                state.copy(
                    hintText = "Player 'X' Won.",
                    currentTurn = BoardCellValue.NONE,
                    playerCrossCount = state.playerCrossCount + 1,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw.",
                    drawCount = state.drawCount + 1,
                    hasDraw = true,
                )
            } else {
                state.copy(
                    hintText = "Player 'O' turn.",
                    currentTurn = BoardCellValue.CIRCLE,
                )
            }
        }
        if (!hasBoardFull() && state.currentTurn === BoardCellValue.CROSS) {
            makeCrossComputerMove()
            if (hasBoardFull()) {
                state = state.copy(
                    hintText = "Game Draw.",
                    drawCount = state.drawCount + 1,
                    hasDraw = true,
                )
            }
        }
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] === boardValue && boardItems[2] === boardValue && boardItems[3] === boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] === boardValue && boardItems[5] === boardValue && boardItems[6] === boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] === boardValue && boardItems[8] === boardValue && boardItems[9] === boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] === boardValue && boardItems[4] === boardValue && boardItems[7] === boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] === boardValue && boardItems[5] === boardValue && boardItems[8] === boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] === boardValue && boardItems[6] === boardValue && boardItems[9] === boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] === boardValue && boardItems[5] === boardValue && boardItems[9] === boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] === boardValue && boardItems[5] === boardValue && boardItems[7] === boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }

    private fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }
}