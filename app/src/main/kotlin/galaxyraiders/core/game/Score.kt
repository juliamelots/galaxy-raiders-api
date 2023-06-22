package galaxyraiders.core.game

import java.io.File
import java.nio.file.Paths
import com.beust.klaxon.Klaxon
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

data class Score (
    var dateTime: String = "",
    var finalScore: Double = 0.0,
    var destroyedAsteroids: Int = 0
)

object ScoreConfig {
  fun configJSONFile(filePath: String) : File {
    var file: File = File(filePath)
    if (!file.exists()) {
      file.createNewFile()
    }
    if (file.readText().isEmpty()) {
      file.writeText("{}")
    }
    return file
  }

  fun getJSONData(filePath: String) : MutableList<Score> {
    val file: File = configJSONFile(filePath)
    val fileString = file.readText()
    val scoreList = Klaxon().parseArray<Score>(fileString)
    if (scoreList != null) { return scoreList.toMutableList() }
    else { return mutableListOf<Score>() }
  }

  fun setJSONData(filePath: String, list: MutableList<Score>) {
    val jsonData = Klaxon().toJsonString(list)
    File(filePath).writeText(jsonData)
  }

  fun updateScoreBoard(newScore: Score) {
    var scoreList: MutableList<Score> = getJSONData(SCORE_BOARD_PATH)
    scoreList.add(newScore)
    setJSONData(SCORE_BOARD_PATH, scoreList)
  }

  fun updateLeaderBoard(newScore: Score) {
    var leaderList: MutableList<Score> = getJSONData(LEADER_BOARD_PATH)
    leaderList.add(newScore)
    leaderList.sortedByDescending { it.finalScore }
    leaderList.take(3)
    setJSONData(LEADER_BOARD_PATH, leaderList)
  }
}