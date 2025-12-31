@file:Suppress("UNCHECKED_CAST")
package pl.pjagielski.punkt.live

import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.BasicScriptingHost
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.templates.standard.SimpleScriptTemplate

internal fun evalScriptWithConfiguration(
    script: String,
    host: BasicScriptingHost = BasicJvmScriptingHost(),
    body: ScriptCompilationConfiguration.Builder.() -> Unit = {}
): ResultWithDiagnostics<EvaluationResult> {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>(body = body)
    return host.eval(script.toScriptSource(), compilationConfiguration, null)
}

fun executeScript(
    script: File,
    host: BasicScriptingHost = BasicJvmScriptingHost(),
    body: ScriptCompilationConfiguration.Builder.() -> Unit = {

        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }

    }
) = evalScriptWithConfiguration(script.readText(), host, body)

fun <T> ResultWithDiagnostics<EvaluationResult>.toValue() = (valueOrThrow().returnValue as ResultValue.Value).value as T

