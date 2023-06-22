package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Given an explosion")
class ExplosionTest {
  private val explosion = Explosion(lifeTime = SpaceFieldConfig.explosionLifeTime,
                                    initialPosition = Point2D(0.0, 0.0),
                                    radius = SpaceFieldConfig.explosionRadius,
                                    mass = SpaceFieldConfig.explosionMass)

  @Test
  fun `it has a type Explosion `() {
    assertEquals("Explosion", explosion.type)
  }

  @Test
  fun `it has a symbol dot `() {
    assertEquals('*', explosion.symbol)
  }

  @Test
  fun `it shows the type Explosion when converted to String `() {
    assertTrue(explosion.toString().contains("Explosion"))
  }

  @Test
  fun `it is triggered after EXPLOSION_LIFE_TIME frames `() {
    repeat(explosion.lifeTime) { explosion.update() }
    assertTrue(explosion.isTriggered)
  }
}