package com.hannesdorfmann.ex2

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test
import kotlin.system.measureTimeMillis

class RepositoryTest {

    @Test
    fun loadPersons() {

        val clickPublisher = PublishSubject.create<Int>()

        val testView = object : View {
            override fun onPersonClicked(): Observable<Int> = clickPublisher
                    .doOnNext { println("clicked on Person with id=$it") }
        }
        val subscriber = TestObserver<PersonWithAddress>()

        val repo = Repository(testView, TestBackend())
        val duration = measureTimeMillis {
            repo.loadPersons().subscribeWith(subscriber)
            clickPublisher.onNext(2)
            clickPublisher.onNext(1)
            clickPublisher.onComplete()

            subscriber.awaitTerminalEvent()
        }

        subscriber.assertComplete()
        subscriber.assertNoErrors()
        Assert.assertEquals(listOf(
                PersonWithAddress(TestBackend.PERSON_DATA[1], TestBackend.ADDRESS_DATA[2]!!),
                PersonWithAddress(TestBackend.PERSON_DATA[0], TestBackend.ADDRESS_DATA[1]!!)
        ),
                subscriber.values())

        Assert.assertTrue("Overall, loading took to long. Expected that loading 2 Persons + their Addresses take less than 2000 ms. Your implementation took $duration ms. Try to parallize things", duration < 2000)
    }

}