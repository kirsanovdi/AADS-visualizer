import java.util.*
import kotlin.math.*

class AlgorithmModel(private val algorithmDataMap: MutableMap<String, MutableList<Figure>>){
    //first
    private fun minCircleWithPoint(list: List<Point>, point1: Point): Circle {
        var circle = smallestEnclosing(list[0], point1)
        for (i in 1 until list.size) {
            val point = list[i]
            if (!circle.contains(point)) {
                circle = minCircleWith2Points(list.subList(0, i), point1, point)
            }
        }
        return circle
    }

    private fun minCircleWith2Points(list: List<Point>, point1: Point, point2: Point): Circle {
        var circle = smallestEnclosing(point1, point2)
        for (i in list.indices) {
            if (!circle.contains(list[i])) {
                circle = circleByThreePoints(point1, point2, list[i])
                algorithmDataMap["circlesCheck"]!!.add(circle)
            }
        }
        return circle
    }

    fun getDataMinContainingCircle(points: List<Point>): Circle {//попробуем с динамическим преобразованием
        //инициализация observer data
        algorithmDataMap["circlesCheck"] = mutableListOf()
        algorithmDataMap["circle"] = mutableListOf()

        when (points.size) {
            0 -> throw IllegalArgumentException()
            1 -> return Circle(points[0], 0.0)
            2 -> return Circle(
                Point((points[0].x + points[1].x) / 2, (points[0].y + points[1].y) / 2),
                points[0].distance(points[1]) / 2
            )
        }
        var circle = smallestEnclosing(points[0], points[1])
        for (i in 2 until points.size) {
            val point = points[i]
            if (!circle.contains(point)) {
                circle = minCircleWithPoint(points.subList(0, i), point)
            }
        }
        algorithmDataMap["circle"]!!.add(circle)
        return circle
    }

    //second
    private fun getDataHull(listInput: List<Point>, precision: Double): List<Point> {

        val p = listInput.minByOrNull { it.y }!!.let { Point(toZero(it.x, precision), toZero(it.y, precision)) }
        //в одну строчку на работает?
        val listFirstSort = listInput.map {
            Point(toZero(it.x, precision), toZero(it.y, precision))
        }
        val listSecondSort = listFirstSort.filter { abs(p.distance(it)) > delta }.sortedBy { p.distance(it) }

        val listThirdSort = listSecondSort.sortedBy { ZeroVector(p, it).angleWithX() }
        //тонкий момент. далее нужно убрать подряд идущие, и если ох угол через р о остью OX будет одинаковым,
        // то взять самое дальнее
        //sortedBy - stable, сохраняет порядок одинаковых элементов

        //удаление идущих под одним углом
        val list = mutableListOf<Point>()
        list.add(listThirdSort.first())
        for (i in 1..listThirdSort.size - 2) {
            if (abs(
                    ZeroVector(p, listThirdSort[i]).angleWithX() - ZeroVector(p, listThirdSort[i + 1]).angleWithX()
                ) > delta
            ) {
                list.add(listThirdSort[i])
            }
        }
        list.add(listThirdSort.last())
        //сердце алгоритма
        val hull = Stack<Point>()
        hull.add(p)
        hull.add(list[0])
        for (index in 1 until list.size) {
            val pi = list[index]
            while (
                !isNotRightTurn(hull.previous(), hull.last(), pi)
            ) {
                hull.pop()
                algorithmDataMap["hullCheck"]!!.add(Segment(hull.last(), pi))
            }
            hull.push(pi)
        }
        //удаление крайне близких
        val hullAns = mutableListOf<Point>()
        var prev = hull[0]
        if (hull.first().distance(hull.last()) < delta) hull.pop()//для проверки перехода из последней к первой
        hullAns.add(hull[0])
        for (point in hull) {
            if (point.distance(prev) > precision) hullAns.add(point)
            prev = point
        }
        //удаление идущих в ряд
        var i = 1
        while (i < hullAns.size - 1) {
            if (!isLeftTurn(hullAns[i - 1], hullAns[i], hullAns[i + 1])) hullAns.removeAt(i) else i++
        }

        return hullAns.toList()
    }

    fun getDataDiameter(points: List<Point>): Segment {
        //инициализация observer data
        algorithmDataMap["hullCheck"] = mutableListOf()
        algorithmDataMap["hullSegment"] = mutableListOf()
        algorithmDataMap["diameterCheck"] = mutableListOf()
        algorithmDataMap["diameter"] = mutableListOf()

        if (points.size < 2) throw IllegalArgumentException()
        if (points.size == 2) return Segment(points[0], points[1])
        var hull: List<Point> = listOf()
        try {
            hull = getDataHull(points, delta)
        } catch (e: Exception) {
            throw Exception(points.toString())
        }
        if (hull.size < 2) throw Exception(points.toString())
        var pointIndex = 0
        var oppositeIndex = hull.indices.maxByOrNull { i -> hull[i].y }!!

        //var sumAngle = 0.0

        var result = Segment(hull[0], hull[1])
        var max = result.length()
        var calipersAngle = 0.0

        fun checkMax(point: Point, opposite: Point) {
            val athwart = Segment(point, opposite)
            if (athwart.length() > max) {
                max = athwart.length()
                result = athwart
            }
        }

        while (pointIndex < hull.size + 1) { //движемся против часовой стрелки
            val point = hull.getFromPos(pointIndex)
            val opposite = hull.getFromPos(oppositeIndex)
            val nextPoint = hull.getFromPos(pointIndex + 1)
            val nextOpposite = hull.getFromPos(oppositeIndex + 1)
            val pointVectorMoveTo = ZeroVector(point, nextPoint)
            val oppositeVectorMoveTo = ZeroVector(opposite, nextOpposite)

            algorithmDataMap["diameterCheck"]!!.add(Segment(point, opposite))

            checkMax(point, opposite)
            var pointAngleMoveTo = pointVectorMoveTo.angleWith(ZeroVector(1.0, 0.0))
            if (pointVectorMoveTo.y < 0) pointAngleMoveTo = 2 * PI - pointAngleMoveTo
            var oppositeAngleMoveTo = oppositeVectorMoveTo.angleWith(ZeroVector(1.0, 0.0))
            if (oppositeVectorMoveTo.y < 0) oppositeAngleMoveTo = 2 * PI - oppositeAngleMoveTo
            val pointAngle = (PI * 2 + pointAngleMoveTo - calipersAngle) % PI
            val oppositeAngle = (PI * 2 + oppositeAngleMoveTo - (calipersAngle + PI) % (PI * 2)) % PI
            if (pointIndex % hull.size == oppositeIndex % hull.size) throw Exception(points.toList().toString())
            when {
                abs(pointAngle - oppositeAngle) < delta * delta -> {
                    pointIndex++
                    oppositeIndex++
                    checkMax(nextPoint, opposite)
                    checkMax(point, nextOpposite)
                    calipersAngle += pointAngleMoveTo
                }
                pointAngle < oppositeAngle -> {
                    pointIndex++
                    calipersAngle = pointAngleMoveTo
                }
                pointAngle > oppositeAngle -> {
                    oppositeIndex++
                    calipersAngle = oppositeAngleMoveTo
                }
                else -> throw Exception(points.toList().toString())
            }
        }
        //ввод вычисляемых observer data
        algorithmDataMap["hullSegment"]!!.addAll(hull.indices.map { i ->
            Segment(hull.getFromPos(i), hull.getFromPos(i + 1))
        })
        algorithmDataMap["diameter"]!!.add(result)
        return result
    }
}

