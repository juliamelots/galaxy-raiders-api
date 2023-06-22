package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

class Explosion(
  val lifeTime: Int,
  initialPosition: Point2D,
  radius: Double,
  mass: Double
) :
  SpaceObject("Explosion", '*', initialPosition, Vector2D(0.0, 0.0), radius, mass) {
  var isTriggered: Boolean = false

  var timer: Int = lifeTime
    private set

  fun update() {
    this.timer--
    this.isTriggered = (this.timer <= 0)
  }
}
