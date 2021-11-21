import java.util.concurrent.Semaphore
import kotlin.random.Random


class CoreModel(private val renderList: MutableList<DrawableList>, )
{//semaphore
    private var semaphore: Semaphore = Semaphore(0, false)
    private var keepGoing: Boolean = true
    private var delay: Long = 10L
    private var funName: String = ""
    private var inputCount: Int = 1000
    //private var sizeOfRandom: Int = 500
    //private var from
    fun run(){
        println(".run() started")
        println(" core is waiting")
        semaphore.acquire()
        while(keepGoing){
            //val map = mutableMapOf<String, List<Figure>>()
            //calculate(map)
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
            val mutableList = when(list.firstOrNull()){
                is Segment -> DrawableChangingLines(color = DrawableColor(1.0,0.0,0.0))
                is Point -> DrawableChangingPoints(color =  DrawableColor(0.0,0.0,1.0))
                is Circle -> DrawableChangingCircles(color =  DrawableColor(0.0,1.0,0.0))
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
        val inputList = List(inputCount) {
            Point(
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0)),
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0))
            )
        }
        algorithmDataList["input"] = inputList.toMutableList()
        AlgorithmModel(algorithmDataList).getDataDiameter(inputList)
        println(".calculate() ended")
    }
    private fun calculate2(algorithmDataList: MutableMap<String, MutableList<Figure>>){
        println(".calculate2() called")
        val inputList = List(inputCount) {
            Point(
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0)),
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0))
            )
        }
        algorithmDataList["input"] = inputList.toMutableList()
        AlgorithmModel(algorithmDataList).getDataMinContainingCircle(inputList)
        println(".calculate2() ended")
    }
}