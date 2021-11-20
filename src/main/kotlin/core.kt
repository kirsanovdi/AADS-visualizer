import java.util.concurrent.Semaphore
import kotlin.math.E
import kotlin.random.Random


class CoreModel(private val renderList: MutableList<DrawableList>, private var semaphore: Semaphore = Semaphore(0, false), private var keepGoing: Boolean = true) {//semaphore
    fun run(){
        println(".run() started")
        semaphore.acquire()
        while(keepGoing){
            val map = mutableMapOf<String, List<Figure>>()
            calculate(map)
            transferToRenderList(map, 10)
            semaphore.acquire()
        }
        println(".run() ended")
    }
    fun resume(isKeepGoing: Boolean){
        println(".resume($isKeepGoing) called")
        keepGoing = isKeepGoing
        semaphore.release()
    }

    fun clear(){
        synchronized(renderList) {
            renderList.clear()
        }
        println(".clear() called, renderList cleared")
    }

    private fun transferToRenderList(algorithmDataList: MutableMap<String, List<Figure>>, delay: Long){
        for((name, list) in algorithmDataList){//спросить про это
            val mutableList = when(list.firstOrNull()){
                is Segment -> DrawableChangingLines(color = DrawableColor(1.0,0.0,0.0))
                is Point -> DrawableChangingPoints(color =  DrawableColor(1.0,0.0,0.0))
                else -> throw Exception()
            }
            renderList.add(mutableList)
            for (elem in list){
                synchronized(renderList) {
                    mutableList.addElement(elem)
                }
                Thread.sleep(delay)
            }
        }
    }

    private fun calculate(algorithmDataList: MutableMap<String, List<Figure>>){
        println(".calculate() called")
        val inputList = List(1000) {
            Point(
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0)),
                Random.nextDouble(Random.nextDouble(-7.0, -0.1), Random.nextDouble(0.1, 7.0))
            )
        }
        getDataDiameter(algorithmDataList, inputList, 10)
        println(".calculate ended")
    }
}