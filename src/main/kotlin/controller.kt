class Controller(private val coreModel: CoreModel, private val graphicsDisplay: GraphicsDisplay){
    private var keepGoing: Boolean = true
    private fun stop(){
        keepGoing = false
    }
    private fun setCoreResume(cmdLine: String){
        val commands = cmdLine.split(Regex("""\s+"""))
        val settings = Regex("""\S+\=\S+""").findAll(cmdLine)
        var delay = 10L
        var count = 1000
        for (setting in settings){
            val (param, value) = setting.value.split("=")
            when (param){
                "delay" -> delay = value.toLong()
                "count" -> count = value.toInt()
            }
        }
        coreModel.resume(isKeepGoing = true, name = commands[1], newDelay = delay, newCount = count)
    }
    fun run(){
        while (keepGoing){
            println(" controller is waiting")
            val cmdLine = (readLine()?:"").trim()
            val command = cmdLine.trim().split(Regex("""\s"""))
            try {
                when (command[0]) {
                    "stop" -> {
                        this.stop()
                        coreModel.stop()
                        graphicsDisplay.close()
                    }
                    "run" -> setCoreResume(cmdLine)
                    "clear" -> coreModel.clear()
                    //крание случаи, ошибки и т.д., думаю стоит перенести все проверки чисто на контроллер
                    "setColor" -> coreModel.setColor(command[1], DrawableColor(command[2].toDouble(), command[3].toDouble(), command[4].toDouble()))
                    else -> println("non-existent command, print \"help\" for displaying a list of commands")
                }
            } catch (e: Exception) {
                println("incorrect form of command, print \"help\" for displaying a list of commands")
            }
        }
        println(" controller closed")
    }
    fun createResume(cmdLine: String){

    }
}