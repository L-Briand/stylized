import kotlin.reflect.*
import kotlin.test.Test

@OptIn(ExperimentalAssociatedObjects::class)
@AssociatedObjectKey
annotation class ColoredWith(val color: KClass<out Color>)

interface Color

@ColoredWith(Testing.Red::class)
class Testing : Annotation {
    object Red : Color

    @Test
    @OptIn(ExperimentalAssociatedObjects::class)
    fun test() {
        val annot: KType = typeOf<Testing>()
        println((Testing::class).findAssociatedObject<ColoredWith>())
    }
}