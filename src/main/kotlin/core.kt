import java.util.concurrent.Semaphore
import kotlin.math.E
import kotlin.random.Random


class CoreModel(private val renderList: MutableList<DrawableList>, )
{//semaphore
    private var semaphore: Semaphore = Semaphore(0, false)
    private var keepGoing: Boolean = true
    private var delay: Long = 10L
    //private var sizeOfRandom: Int = 500
    //private var from
    fun run(){
        println(".run() started")
        println(" core is waiting")
        semaphore.acquire()
        while(keepGoing){
            val map = mutableMapOf<String, List<Figure>>()
            calculate(map)
            transferToRenderList(map, delay)
            println(" core is waiting")
            semaphore.acquire()
        }
        println(".run() ended")
    }
    fun resume(isKeepGoing: Boolean, newDelay: Long = 10L){
        println(".resume($isKeepGoing) called")
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

    private fun transferToRenderList(algorithmDataList: MutableMap<String, List<Figure>>, delay: Long){
        println(".transferToRenderList() started")
        for((name, list) in algorithmDataList){//спросить про это
            val mutableList = when(list.firstOrNull()){
                is Segment -> DrawableChangingLines(color = DrawableColor(1.0,0.0,0.0))
                is Point -> DrawableChangingPoints(color =  DrawableColor(0.0,0.0,1.0))
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

    private fun calculate(algorithmDataList: MutableMap<String, List<Figure>>){
        println(".calculate() called")
        val inputList = List(500) {
            Point(
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0)),
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0))
            )
        }
        algorithmDataList["input"] = inputList
        getDataDiameter(algorithmDataList, inputList)
        println(".calculate() ended")
    }
}