package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis
import java.io.File
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

const val MILLISECONDS_PER_SECOND: Int = 1000
const val SCORE_BOARD_PATH: String = "../score/Scoreboard.json"
const val LEADER_BOARD_PATH: String = "../score/Leaderboard.json"

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer,
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  var playing = true

  fun execute() {
    while (true) {
      val duration = measureTimeMillis { this.tick() }

      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }

    updateScoreBoard(this.field.score)
    updateLeaderBoard()
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }

    updateScoreBoard(this.field.score)
    updateLeaderBoard()
  }

  fun tick() {
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.updateExplosions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
      if (first.impacts(second)) {
        first.collideWith(second, GameEngineConfig.coefficientRestitution)
        if ((first is Asteroid && second is Missile) || (first is Missile && second is Asteroid)) {
          this.field.generateExplosion(first.center)
          this.field.score.destroyedAsteroids++
          this.field.score.finalScore += if (first is Asteroid) explosionScore(first) else explosionScore(second)
        }
      }
    }
  }

  fun explosionScore(asteroid: Asteroid): Float {
    return asteroid.mass + asteroid.radius * 0.5
  }

  fun updateExplosions() {
    this.field.updateExplosions()
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
    this.field.trimExplosions()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)
  }

  fun updateScoreBoard(newScore: Score) {
    val textString = File(SCORE_BOARD_PATH).readText()
    val mapper = jacksonObjectMapper()
    val scoreList: MutableList<Score> = mapper.readValue(textString)
    //val scoreList: MutableList<Score> = Json.decodeFromString<MutableList<Score>>(jsonString)
    scoreList.add(newScore)
    //val jsonData = Json.encodeToString(scoreList)
    val jsonData = mapper.writeValueAsString(scoreList)
    File(SCORE_BOARD_PATH).writeText(jsonData)
  }

  fun updateLeaderBoard() {
    val textString = File(LEADER_BOARD_PATH).readText()
    val mapper = jacksonObjectMapper()
    val scoreList: MutableList<Score> = mapper.readValue(textString)
    //var scoreList: MutableList<Score> = Json.decodeFromString<MutableList<Score>>(textString)
    scoreList.sortedByDescending { it.finalScore }
    //val jsonData = Json.encodeToString(listOf(scoreList[0], scoreList[1], scoreList[2]))
    val jsonData = mapper.writeValueAsString(listOf(scoreList[0], scoreList[1], scoreList[2]))
    File(LEADER_BOARD_PATH).writeText(jsonData)
  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}
