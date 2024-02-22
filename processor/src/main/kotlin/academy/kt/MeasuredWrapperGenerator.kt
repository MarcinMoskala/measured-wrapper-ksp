package academy.kt

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class MeasuredWrapperGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.error("Implement MeasuredWrapperGenerator")
        return emptyList()
    }
}
