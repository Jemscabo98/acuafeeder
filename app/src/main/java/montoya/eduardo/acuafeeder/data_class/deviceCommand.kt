package montoya.eduardo.acuafeeder.data_class

data class deviceCommand(var pool: Int) {
    var dispositivosXpiscina: Int = 0
    var modo: Int = 0
    var manualSW: Int = 0
    var alimentoTotal: Int = 0
    var grPorSegundo: Int = 0
    var enviarProgramacion: Int = 0
}