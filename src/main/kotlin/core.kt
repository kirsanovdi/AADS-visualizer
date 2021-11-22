import java.util.concurrent.Semaphore
import kotlin.random.Random


class CoreModel(private val renderList: MutableList<DrawableList>, )
{//semaphore
    private var semaphore: Semaphore = Semaphore(0, false)
    private var keepGoing: Boolean = true
    private var delay: Long = 10L
    private var funName: String = ""
    private var inputCount: Int = 1000
    private var permission: Set<String> = setOf("All")
    private var colorMap: MutableMap<String,DrawableColor> = mutableMapOf(
        "hullCheck" to cyanDColor,
        "hullSegment" to magentaDColor,
        "diameterCheck" to yellowDColor,
        "circlesCheck" to greenDColor,
        "diameter" to redDColor,
        "circle" to redDColor,
        "inputPoints" to blueDColor
    )

    fun setColor(name: String, color: DrawableColor){
        synchronized(colorMap) {
            if (name in colorMap) {
                colorMap[name] = color
            } else println("incorrect observer data name")
        }
    }

    fun run(){
        println(".run() started")
        println(" core is waiting")
        semaphore.acquire()
        while(keepGoing){
            val map = mutableMapOf<String, MutableList<Figure>>()
            when(funName){
                "diameter" -> calculate(map)
                "circle" -> calculate2(map)
                else -> println("incorrect name")
            }
            transferToRenderList(map, delay)
            println(" core is waiting")
            semaphore.acquire()
        }
        println(".run() ended")
    }
    fun stop(){
        keepGoing = false
        semaphore.release()
    }

    fun resume(isKeepGoing: Boolean, name: String = "", newDelay: Long = 10L, newCount: Int = 1000){
        println(".resume($isKeepGoing) called")
        inputCount = newCount
        funName = name
        delay = newDelay
        keepGoing = isKeepGoing
        semaphore.release()
    }

    fun clear(){
        println(".clear() called")
        synchronized(renderList) {
            renderList.clear()
        }
        println(".clear() finished, renderList cleared")
    }
    //спросить про MutableMap<String, MutableList<Figure>> -> MutableMap<String, List<Figure>>
    private fun transferToRenderList(algorithmDataList: MutableMap<String, MutableList<Figure>>, delay: Long){
        println(".transferToRenderList() started")
        for((name, list) in algorithmDataList){//спросить про это
            val color = synchronized(colorMap) {colorMap[name]?:blackDColor}
            val mutableList = when(list.firstOrNull()){
                is Segment -> DrawableChangingLines(color)
                is Point -> DrawableChangingPoints(color)
                is Circle -> DrawableChangingCircles(color)
                else -> throw Exception()
            }
            synchronized(renderList) {
                renderList.add(mutableList)
            }
            for (elem in list){
                synchronized(renderList) {
                    mutableList.addElement(elem)
                }
                Thread.sleep(delay)
            }
        }
        println(".transferToRenderList() ended")
    }

    private fun calculate(algorithmDataList: MutableMap<String, MutableList<Figure>>){
        println(".calculate() called")
        val inputList = randomPoints2List(inputCount, -7.0, -0.1, 0.1, 7.0)
        algorithmDataList["inputPoints"] = inputList.toMutableList()
        AlgorithmModel(algorithmDataList).getDataDiameter(inputList)
        println(".calculate() ended")
    }
    private fun calculate2(algorithmDataList: MutableMap<String, MutableList<Figure>>){
        println(".calculate2() called")
        val inputList = randomPoints2List(inputCount, -7.0, -0.1, 0.1, 7.0)
        algorithmDataList["inputPoints"] = inputList.toMutableList()
        AlgorithmModel(algorithmDataList).getDataMinContainingCircle(inputList)
        println(".calculate2() ended")
    }
}