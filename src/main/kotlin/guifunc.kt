import org.lwjgl.opengl.GL11.*
import javax.sound.sampled.Line
import kotlin.math.*

private fun Double.toHFloat() = (this/ wHeight * wPrecision).toFloat()
private fun Double.toWFloat() = (this/ wWidth * wPrecision).toFloat()

private fun drawLine(segment: Segment) {
    val x1 = segment.begin.x.toWFloat()
    val y1 = segment.begin.y.toHFloat()
    val x2 = segment.end.x.toWFloat()
    val y2 = segment.end.y.toHFloat()
    glVertex2f(x1, y1)
    glVertex2f(x2, y2)
}

private fun drawPoint(point: Point){
    val x = point.x.toWFloat()
    val y = point.y.toHFloat()
    glVertex2f(x, y)
}

private fun drawCircle(circle: Circle){
    val x = circle.center.x
    val y = circle.center.y
    val radius = circle.radius
    glBegin(GL_LINE_LOOP)
    for(angle in 0..360){
        val theta = PI * angle/180.0
        glVertex2f((x + radius * cos(theta)).toWFloat(), (y + radius * sin(theta)).toHFloat())
    }
    glEnd()
}

class DrawableColor(val red: Double, val green: Double, val blue: Double)

val redDColor = DrawableColor(1.0,0.0,0.0)
val greenDColor = DrawableColor(0.0,1.0,0.0)
val blueDColor = DrawableColor(0.0,0.0,1.0)
val yellowDColor = DrawableColor(1.0,1.0,0.0)
val magentaDColor = DrawableColor(1.0,0.0,1.0)
val cyanDColor = DrawableColor(0.0,1.0,1.0)
val blackDColor = DrawableColor(0.0,0.0,0.0)

abstract class DrawableList(val drawableColor: DrawableColor){
    abstract fun draw()
}

abstract class DrawableChangeList(drawableColor: DrawableColor): DrawableList(drawableColor){
    abstract fun addElement(figure: Figure)
}

class DrawableLines(private val lines: List<Segment>, private val color: DrawableColor, private val width: Float = 1.0f): DrawableList(color){
    override fun draw() {
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        glLineWidth(width)
        glBegin(GL_LINES)
        for(line in lines) drawLine(line)
        glEnd()
    }
}
class DrawablePoints(private val points: List<Point>, private val color: DrawableColor, private val size: Float = 1.0f): DrawableList(color){
    override fun draw() {
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        glPointSize(size)
        glBegin(GL_POINTS);
        for (point in points) drawPoint(point)
        glEnd()
    }
}

class DrawableChangingLines(private val color: DrawableColor, private val width: Float = 1.0f): DrawableChangeList(color){
    private val lines: MutableList<Segment> = mutableListOf()
    override fun addElement(figure: Figure){
        lines.add(figure as Segment)
    }
    override fun draw() {
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        glLineWidth(width)
        glBegin(GL_LINES)
        for(line in lines) drawLine(line)
        glEnd()
    }
}

class DrawableChangingPoints(private val color: DrawableColor, private val size: Float = 1.0f): DrawableChangeList(color){
    private val points: MutableList<Point> = mutableListOf()
    override fun addElement(figure: Figure){
        points.add(figure as Point)
    }
    override fun draw() {
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        glPointSize(size)
        glBegin(GL_POINTS);
        for (point in points) drawPoint(point)
        glEnd()
    }
}

class DrawableChangingCircles(private val color: DrawableColor): DrawableChangeList(color){
    private val circles: MutableList<Circle> = mutableListOf()
    override fun addElement(figure: Figure) {
        circles.add(figure as Circle)
    }
    override fun draw() {
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        for (circle in circles) drawCircle(circle)
    }
}

@Deprecated("use usual with sync instead")
class DrawablePartLines(private val lines: List<Segment>, private val color: DrawableColor, private val width: Float = 1.0f, private var length: Int = 0): DrawableList(color){
    override fun draw() {
        length++
        glColor3d(drawableColor.red, drawableColor.green, drawableColor.blue)
        glLineWidth(width)
        glBegin(GL_LINES)
        for(i in 0..kotlin.math.min(length, lines.size - 1)) drawLine(lines[i])
        glEnd()
    }
}