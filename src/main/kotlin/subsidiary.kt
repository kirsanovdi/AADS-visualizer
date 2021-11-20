import java.util.*
import kotlin.math.*

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