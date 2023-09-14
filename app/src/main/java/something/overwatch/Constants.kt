package something.overwatch
import something.overwatch.MapType.*

/**
 * Best practice for global constants
 */
object Constants {
    const val REMOTE_URL: String = "http://s1.retort.ganks.me/"

    @JvmField
    val MAPS = arrayOf<Map>(
            Map("Blizzard World", Hybrid),
            Map("Busan", Control),
            Map("Dorado", Escort),
            Map("Eichenwalde", Hybrid),
            Map("Havana", Escort),
            Map("Hollywood", Hybrid),
            Map("Ilios", Control),
            Map("Junkertown", Escort),
            Map("King's Row", Hybrid),
            Map("Lijiang Tower", Control),
            Map("Nepal", Control),
            Map("Numbani", Hybrid),
            Map("Oasis", Control),
            Map("Rialto", Escort),
            Map("Route 66", Escort),
            Map("Watchpoint: Gibraltar", Escort),
            Map("Hanamura", Assault),
            Map("Horizon Lunar Colony", Assault),
            Map("Paris", Assault),
            Map("Temple of Anubis", Assault),
            Map("Volskaya Industries", Assault),
    )
}