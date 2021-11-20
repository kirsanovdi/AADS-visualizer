class Controller(private val coreModel: CoreModel, private val graphicsDisplay: GraphicsDisplay, private var keepGoing: Boolean = true){
    fun run(){
        while (keepGoing){
            println(" controller is waiting")
            val command = (readLine()?:"").trim().split(Regex("""\s"""))
            try {
                when (command[0]) {
                    "stop" -> {
                        keepGoing = false
                        coreModel.resume(false)
                        graphicsDisplay.close()
                    }
                    "next" -> coreModel.resume(true, command[1].toLong())
                    "clear" -> coreModel.clear()
                    else -> println("non-existent command, print \"help\" for displaying a list of commands")
                }
            } catch (e: Exception) {
                println("incorrect form of command, print \"help\" for displaying a list of commands")
            }
        }
    }
}