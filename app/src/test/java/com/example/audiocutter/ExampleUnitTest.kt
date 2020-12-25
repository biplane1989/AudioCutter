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
        /*launch {  }
        println("My job is: ${coroutineContext[Job]?.children?.count()}")
        val number = produceNumbers()
        square(produceSquares())

        println("My job is: ${coroutineContext[Job]?.children?.count()}")
       coroutineContext.cancelChildren()
        delay(2000)
        println("Done!")*/
        val a = CoroutineName("SDSD")

        val coroutineScope1 = CoroutineScope(Dispatchers.Default)
        val coroutineScope2 = CoroutineScope(Dispatchers.Default)
        coroutineScope1.launch(a) {
            println("My context is $coroutineContext}")
            while (true){
                delay(1000)
                println("coroutineScope1")
            }
        }
        coroutineScope2.launch(a) {
            println("My context is $coroutineContext}")
            while (true){
                delay(1000)
                println("coroutineScope2")
            }
        }

        delay(3000)
        coroutineScope1.cancel()
        coroutineScope2.cancel()
        delay(3000)
        println("Done!")
    }
}