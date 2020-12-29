package com.example.audiocutter

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    fun CoroutineScope.produceSquares():ReceiveChannel<Int> = produce{
        for (x in 1..5) send(x*x)
    }

    fun CoroutineScope.produceNumbers() = produce<Int>(capacity = 100){
        var x = 1
        while (true) {
            delay(400)
            println("produceNumbers:${x} ")
            send(x++)
        }
    }

    fun CoroutineScope.square(numbers:ReceiveChannel<Int>):ReceiveChannel<Int> = produce {
        for (x in numbers) send(x*x)
    }

    @Test
    fun addition_isCorrect() = runBlocking{

    }
}