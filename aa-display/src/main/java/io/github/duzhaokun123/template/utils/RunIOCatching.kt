package io.github.nitsuya.template.bases

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

fun <R> CoroutineScope.runIOCatching(block: suspend CoroutineScope.() -> R) =
    this.run { this to future(Dispatchers.IO) { runCatching { block() } } }

fun <R> runIOCatching(block: suspend CoroutineScope.() -> R) =
    MainScope().run { this to future(Dispatchers.IO) { runCatching { block() } } }

fun <R> Pair<CoroutineScope, CompletableFuture<Result<R>>>.onSuccess(onSuccess: suspend CoroutineScope.(R) -> Unit) =
    this.apply { first.launch(Dispatchers.IO) { second.await().onSuccess { onSuccess(it) } } }

fun <R> Pair<CoroutineScope, CompletableFuture<Result<R>>>.onFailure(onFailure: suspend CoroutineScope.(Throwable) -> Unit) =
    this.apply { first.launch(Dispatchers.IO) { second.await().onFailure { onFailure(it) } } }