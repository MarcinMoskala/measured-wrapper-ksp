import academy.kt.MeasuredWrapperGeneratorProvider
import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.*
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.File

class MeasuredWrapperGeneratorTest {

    @Test
    fun `should generate interface for a simple class`() {
        assertGeneratedFile(
            sourceFileName = "TokenService.kt",
            source = """
                package academy.kt
                
                class TokenService {
                
                    @Measured
                    fun getToken(): String {
                        Thread.sleep(1000)
                        return "ABCD"
                    }
                }
            """,
            generatedResultFile = "academy/kt/MeasuredTokenService.kt",
            generatedSource = """
                package academy.kt
                
                import kotlin.String
                
                public class MeasuredTokenService(
                  public val wrapper: TokenService,
                ) {
                  public constructor() : this(TokenService())
                
                  public fun getToken(): String {
                    val before = System.currentTimeMillis()
                    val value = wrapper.getToken()
                    val after = System.currentTimeMillis()
                    println("getToken from TokenService took ${'$'}{after-before} ms")
                    return value
                  }
                }
            """
        )
    }

    @Test
    fun `should generate constructor with wrapped class parameters`() {
        assertGeneratedFile(
            sourceFileName = "TokenService.kt",
            source = """
                package academy.kt
                
                class TokenService(
                    private val token: String
                ) {
                
                    @Measured
                    fun getToken(): String {
                        Thread.sleep(1000)
                        return token
                    }
                }
            """,
            generatedResultFile = "academy/kt/MeasuredTokenService.kt",
            generatedSource = """
                package academy.kt
                
                import kotlin.String
                
                public class MeasuredTokenService(
                  public val wrapper: TokenService,
                ) {
                  public constructor(token: String) : this(TokenService(token))
                
                  public fun getToken(): String {
                    val before = System.currentTimeMillis()
                    val value = wrapper.getToken()
                    val after = System.currentTimeMillis()
                    println("getToken from TokenService took ${'$'}{after-before} ms")
                    return value
                  }
                }
            """
        )
    }

    @Test
    fun `should use method parameters`() {
        assertGeneratedFile(
            sourceFileName = "TokenService.kt",
            source = """
                package academy.kt
                
                class TokenService{
                
                    @Measured
                    fun getToken(token: String): String {
                        Thread.sleep(1000)
                        return token
                    }
                }
            """,
            generatedResultFile = "academy/kt/MeasuredTokenService.kt",
            generatedSource = """
                package academy.kt
                
                import kotlin.String
                
                public class MeasuredTokenService(
                  public val wrapper: TokenService,
                ) {
                  public constructor() : this(TokenService())
                
                  public fun getToken(token: String): String {
                    val before = System.currentTimeMillis()
                    val value = wrapper.getToken(token)
                    val after = System.currentTimeMillis()
                    println("getToken from TokenService took ${'$'}{after-before} ms")
                    return value
                  }
                }
            """
        )
    }

}

private fun assertGeneratedFile(
    sourceFileName: String,
    @Language("kotlin") source: String,
    generatedResultFile: String,
    @Language("kotlin") generatedSource: String
) {
    val compilation = KotlinCompilation().apply {
        inheritClassPath = true
        kspWithCompilation = true

        sources = listOf(
            SourceFile.kotlin(sourceFileName, source)
        )
        symbolProcessorProviders = listOf(
            MeasuredWrapperGeneratorProvider()
        )
    }
    val result = compilation.compile()
    assertEquals(OK, result.exitCode)

    val generated = File(
        compilation.kspSourcesDir,
        "kotlin/$generatedResultFile"
    )
    assertEquals(
        generatedSource.trimIndent(),
        generated.readText().trimIndent()
    )
}
