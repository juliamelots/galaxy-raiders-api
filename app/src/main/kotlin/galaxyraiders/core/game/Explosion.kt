package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

const val EXPLOSION_LIFE_TIME: Int = 30

class Explosion(
  initialPosition: Point2D,
) :
  SpaceObject("Explosion", '*', initialPosition, Vector2D(0.0, 0.0), 0.0, 0.0) {
  var isTriggered: Boolean = false

  var timer: Int = EXPLOSION_LIFE_TIME
    private set
  
  fun update() {
    this.timer--
    if (this.timer <= 0) {
      this.isTriggered = true
    }
  }
}
