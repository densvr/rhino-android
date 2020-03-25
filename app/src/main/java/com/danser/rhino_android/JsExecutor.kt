package com.danser.rhino_android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.danser.rhino_android_library.RhinoAndroidHelper
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import rx.Completable
import rx.Single
import rx.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap


interface IJsExecutor {
    fun execute(code: JsCode): Single<String>
    fun compileScriptAndCache(code: String): Completable
}

class JsExecutor(private val context: Context) : IJsExecutor {

    private val scripts: MutableMap<Int, Script> = ConcurrentHashMap()
    private val failedScripts: MutableMap<Int, String> = ConcurrentHashMap()

    override fun execute(code: JsCode): Single<String> = Single.create<String> { subscriber ->
        @Suppress("TooGenericExceptionCaught")
        try {
            val rhinoContext = getRhinoContext()
            val key = code.code.hashCode()
            failedScripts[key]?.let { errorMessage ->
                val error =
                    IllegalArgumentException("Script already was tried to compile or execute with error: $errorMessage")
                subscriber.onError(error)
                return@create
            }

            //compile script or get precompiled script from cache
            val script = compileScriptCached(rhinoContext, code.code)

            //then execute it
            val result: String = script.execute(rhinoContext, code.arguments)

            subscriber.onSuccess(result)
        } catch (th: Throwable) {
            subscriber.onError(th)
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }
        .subscribeOn(Schedulers.computation())
        .doOnError { throwable ->
            logError(throwable)
            failedScripts[code.code.hashCode()] = throwable.message.orEmpty()
        }

    override fun compileScriptAndCache(code: String): Completable = Completable
        .fromAction { compileScriptCached(getRhinoContext(), code) }
        .subscribeOn(Schedulers.computation())
        .doOnError { throwable ->
            logError(throwable)
            failedScripts[code.hashCode()] = throwable.message.orEmpty()
        }

    private fun compileScriptCached(context: org.mozilla.javascript.Context, code: String): Script =
        scripts[code.hashCode()] ?: context.compileString(
            code,
            null,
            1,
            null
        ).apply { scripts[code.hashCode()] = this }

    private fun Script.execute(
        context: org.mozilla.javascript.Context,
        arguments: Map<String, Any>
    ): String {
        val scope = ImporterTopLevel(context)
        putArguments(scope, arguments)
        return this.exec(context, scope) as String
    }

    private fun putArguments(scope: Scriptable, arguments: Map<String, Any>) {
        arguments.entries.forEach { (key, value) ->
            val arg: Any = org.mozilla.javascript.Context.javaToJS(value, scope)
            ScriptableObject.putProperty(scope, key, arg)
        }
    }

    private fun getRhinoContext(): org.mozilla.javascript.Context = RhinoAndroidHelper(context)
        .enterContext()
        .apply {
            optimizationLevel = 1

        }

    private fun logError(th: Throwable) {
        Log.e(JsExecutor::class.java.simpleName, th.message ?: "")
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, th.message, Toast.LENGTH_LONG).show()
        }
    }
}

data class JsCode(
    val code: String,
    val arguments: Map<String, Any>
)
