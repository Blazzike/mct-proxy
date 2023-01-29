package components.regions.types

import components.regions.Region
import components.regions.packets.client.DisplayObjective
import components.regions.packets.client.UpdateObjectives
import components.regions.packets.client.UpdateScore
import models.MCText
import models.MCText.Color.DARK_PURPLE
import models.MCText.Color.GRAY
import models.UserChannel

fun sendRegionScoreboardDisplay(userChannel: UserChannel, region: Region) {
  UpdateObjectives(
    objectiveName = "region",
    mode = UpdateObjectives.Mode.CREATE,
    objectiveValue = MCText("Region").toJsonStr(),
  ).write(userChannel)

  DisplayObjective(
    position = DisplayObjective.Position.SIDEBAR,
    scoreName = "region",
  ).write(userChannel)

  val values = arrayOf(
    Pair(MCText(DARK_PURPLE, region.name), 100),
    Pair(MCText(DARK_PURPLE, "Protected"), 99),
    Pair(MCText(GRAY, MCText.bold, MCText.underlined, "Residents"), 1),
    Pair(MCText(GRAY, "None"), 0),
  )

  values.forEach { (text, score) ->
    UpdateScore(
      label = text.toCodedString(),
      action = UpdateScore.Action.CREATE,
      objectiveName = "region",
      value = score,
    ).write(userChannel)
  }
}

fun removeRegionScoreboardDisplay(userChannel: UserChannel) {
  UpdateObjectives(
    objectiveName = "region",
    mode = UpdateObjectives.Mode.REMOVE,
  ).write(userChannel)
}