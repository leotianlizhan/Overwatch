package something.overwatch

data class Map(val name: String, val type: MapType)

enum class MapType(val value: String){
    Hybrid("hybrid"),
    Control("control"),
    Escort("escort"),
    Assault("assault")
}
