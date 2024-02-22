package academy.kt

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


class MeasuredWrapperGeneratorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MeasuredWrapperGenerator(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator
        )
    }
}
