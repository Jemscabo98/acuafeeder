package montoya.eduardo.acuafeeder.data_class

import java.io.Serializable

data class Comida (var pool: Int): Serializable {
    var idFood: String = ""
    var allimentoGrSeg: Int = 0
}