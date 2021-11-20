class Controller(private val coreModel: CoreModel, private val graphicsDisplay: GraphicsDisplay, private var keepGoing: Boolean = true){
    fun run(){
        while (keepGoing){
            when (readLine()){
                "stop" ->  {
                    keepGoing = false
                    coreModel.resume(false)
                    graphicsDisplay.close()
                }
                "next" -> coreModel.resume(true)
                "clear" -> coreModel.clear()
            }
        }
    }
}