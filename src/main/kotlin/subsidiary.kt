import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.math.*
import kotlin.random.Random

/**Дельта для вычислений с плавающей точкой */
const val delta = 1e-10

/**Элемент с (k+n)%k позиции */
fun List<Point>.getFromPos(index: Int): Point = this[(this.size + index) % this.size]

/**Предпоследниий элемент стэка */
fun Stack<Point>.previous(): Point = this[this.size - 2]

/**Аппросимация к 0 */
fun toZero(a: Double, precision: Double) = if (abs(a) < precision) 0.0 else a

/**Квадрат числа */
fun sqr(value: Double) = value*value

abstract class Figure()

/**Точка */
data class Point(val x: Double, val y: Double): Figure() {
    fun distance(other: Point): Double = sqrt(sqr(x - other.x) + sqr(y - other.y))
    override fun equals(other: Any?) = other is Point && this.x == other.x && this.y == other.y
    override fun hashCode(): Int = x.hashCode() + y.hashCode()
}

/**Отрезок */
data class Segment(val begin: Point, val end: Point): Figure() {
    fun length() = begin.distance(end)
    override fun equals(other: Any?) =
        other is Segment && (begin == other.begin && end == other.end || end == other.begin && begin == other.end)
    override fun hashCode() =
        begin.hashCode() + end.hashCode()
}

/**Прямая*/
class Line private constructor(val b: Double, val angle: Double): Figure() {
    init {
        require(angle >= 0 && angle < PI) { "Incorrect line angle: $angle" }
    }

    constructor(point: Point, angle: Double) : this(point.y * cos(angle) - point.x * sin(angle), angle)

    fun crossPoint(other: Line): Point {
        if (abs(this.angle - PI / 2) <= delta) return Point(
            -this.b,
            (-this.b * sin(other.angle) + other.b) / cos(other.angle)
        )
        if (abs(other.angle - PI / 2) <= delta) return Point(
            -other.b,
            (-other.b * sin(this.angle) + this.b) / cos(this.angle)
        )
        val x = (this.b / cos(this.angle) - other.b / cos(other.angle)) / (tan(other.angle) - tan(this.angle))
        val y = (x * sin(this.angle) + this.b) / cos(this.angle)
        return Point(x, y)
    }

    override fun equals(other: Any?) = other is Line && angle == other.angle && b == other.b

    override fun hashCode(): Int {
        var result = b.hashCode()
        result = 31 * result + angle.hashCode()
        return result
    }

    override fun toString() = "Line(${cos(angle)} * y = ${sin(angle)} * x + $b)"
}

/**Окружность*/
class Circle(val center: Point, val radius: Double): Figure() {

    fun distance(other: Circle): Double = (this.center.distance(other.center) - this.radius - other.radius).let {
        if (it > 0.0) it else 0.0
    }

    fun contains(p: Point): Boolean = this.center.distance(p) <= radius + delta
}

/**Вектор в двумерном пространстве */
data class ZeroVector(val x: Double, val y: Double) {
    constructor(begin: Point, end: Point) : this(end.x - begin.x, end.y - begin.y)
    private fun length(): Double = sqrt(x * x + y * y)
    fun angleWith(other: ZeroVector): Double =
        acos((this.x * other.x + this.y * other.y) / (this.length() * other.length()))
}

/**Старый метод за N(O^2) */
fun diameterOld(vararg points: Point): Segment {
    if (points.size < 2) throw IllegalArgumentException()
    if (points.size == 2) return Segment(points[0], points[1])
    val list = points.toMutableList()
    var maxLen = 0.0
    var maxSegment = Segment(list[0], list[1])
    var remain = list.size - 1
    while (remain != 0) {
        for (i in 0..remain) {
            if (list[remain].distance(list[i]) > maxLen) {
                maxLen = list[remain].distance(list[i])
                maxSegment = Segment(list[i], list[remain])
            }
        }
        remain--
    }
    return maxSegment
}

/**Проверка на правый поворот a -> b -> c (без аппроксимации) */
fun isNotRightTurn(a: Point, b: Point, c: Point): Boolean =
    (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x) >= 0

/**Проверка на правый поворот a -> b -> c (исключающая) */
fun isLeftTurn(a: Point, b: Point, c: Point): Boolean =
    (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x) > 0

/**Угол с осью OX */
fun ZeroVector.angleWithX(): Double = this.angleWith(ZeroVector(1.0, 0.0))

/**Окружность, построенная на диаметре*/
fun circleByDiameter(diameter: Segment): Circle = Circle(
    Point((diameter.begin.x + diameter.end.x) / 2, (diameter.begin.y + diameter.end.y) / 2),
    diameter.begin.distance(diameter.end) / 2
)

/**Прямая, проходящая через отрезок*/
fun lineBySegment(s: Segment): Line = Line(
    s.begin,
    if (abs(s.begin.x - s.end.x) < delta) PI / 2 else (PI + atan((s.begin.y - s.end.y) / (s.begin.x - s.end.x))) % PI
)

/**Прямая, проходящая через 2 точки*/
fun lineByPoints(a: Point, b: Point): Line = lineBySegment(Segment(a, b))

/**Серединный перпендикуляр*/
fun bisectorByPoints(a: Point, b: Point): Line = Line(
    Point((a.x + b.x) / 2, (a.y + b.y) / 2),
    (lineByPoints(a, b).angle + PI / 2) % PI
)
/**Две ближайшие окружности*/
fun findNearestCirclePair(vararg circles: Circle): Pair<Circle, Circle> =//пока так, потом может будет NlogN
    circles.toList().let { list ->
        if (list.size < 2) throw IllegalArgumentException()
        var minDistance = list[0].distance(list[1])
        var pair = Pair(list[0], list[1])
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                if (list[i].distance(list[j]) < minDistance) {
                    pair = Pair(list[i], list[j])
                    minDistance = list[i].distance(list[j])
                }
            }
        }
        pair
    }

/**Окружность, проходящая через 3 заданные точки*/
fun circleByThreePoints(a: Point, b: Point, c: Point): Circle =
    bisectorByPoints(a, b).crossPoint(bisectorByPoints(b, c)).let { center ->
        Circle(center, center.distance(a))
    }

/**Окружность, построенная на диаметре (p1, p2)*/
fun smallestEnclosing(p1: Point, p2: Point): Circle =
    Circle(
        Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2),
        p1.distance(p2) / 2
    )

/**Список случайных точек из (f1 - f2) - (s1 - s2)*/
fun randomPoints2List(count: Int, f1: Double, f2: Double, s1:Double, s2: Double): List<Point> = List(count) {
    Point(
        Random.nextDouble(Random.nextDouble(f1, f2), Random.nextDouble(s1, s2)),
        Random.nextDouble(Random.nextDouble(f1, f2), Random.nextDouble(s1, s2))
    )
}

fun main() {
    val list = randomPoints2List(2500, -7.0, -0.1, 0.1, 7.0)
    PrintStream(File("input.txt")).use {
        for (point in list) it.println("${point.x} ${point.y}")
    }
}