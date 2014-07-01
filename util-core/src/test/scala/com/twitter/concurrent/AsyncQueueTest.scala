package com.twitter.concurrent

// <<<<<<< HEAD
// import com.twitter.util.{Return, Throw}
// import org.scalatest.FunSuite
// import org.scalatest.junit.JUnitRunner
// import org.junit.runner.RunWith

// class AsyncQueueTest extends FunSuite {
//   test("queue pollers") {
//     val q = new AsyncQueue[Int]

//     val p0 = q.poll()
//     val p1 = q.poll()
//     val p2 = q.poll()

//     assert(!p0.isDefined)
//     assert(!p1.isDefined)
//     assert(!p2.isDefined)

//     q.offer(1)
//     assert(p0.poll === Some(Return(1)))
//     assert(!p1.isDefined)
//     assert(!p2.isDefined)

//     q.offer(2)
//     assert(p1.poll === Some(Return(2)))
//     assert(!p2.isDefined)

//     q.offer(3)
//     assert(p2.poll === Some(Return(3)))
//   }

//   test("queue offers") {
//     val q = new AsyncQueue[Int]

//     q.offer(1)
//     q.offer(2)
//     q.offer(3)

//     assert(q.poll().poll === Some(Return(1)))
//     assert(q.poll().poll === Some(Return(2)))
//     assert(q.poll().poll === Some(Return(3)))
//   }

//   test("into idle state and back") {
//     val q = new AsyncQueue[Int]

//     q.offer(1)
//     assert(q.poll().poll === Some(Return(1)))

//     val p = q.poll()
//     assert(!p.isDefined)
//     q.offer(2)
//     assert(p.poll === Some(Return(2)))

//     q.offer(3)
//     assert(q.poll().poll === Some(Return(3)))
//   }

//   test("fail pending and new pollers discard=true") {
//     val q = new AsyncQueue[Int]

//     val exc = new Exception("sad panda")
//     val p0 = q.poll()
//     val p1 = q.poll()

//     assert(!p0.isDefined)
//     assert(!p1.isDefined)

//     q.fail(exc)
//     assert(p0.poll === Some(Throw(exc)))
//     assert(p1.poll === Some(Throw(exc)))

//     assert(q.poll().poll === Some(Throw(exc)))
//   }

//   test("fail pending and new pollers discard=false") {
//     val q = new AsyncQueue[Int]

//     val exc = new Exception("sad panda")
//     val p0 = q.poll()

//     assert(!p0.isDefined)

//     q.offer(1)
//     q.offer(2)
//     q.fail(exc, false)
//     q.offer(3)

//     assert(p0.poll === Some(Return(1)))
//     assert(q.poll().poll === Some(Return(2)))
//     assert(q.poll().poll === Some(Throw(exc)))
//     assert(q.poll().poll === Some(Throw(exc)))
//   }

//   test("fail doesn't blow up offer") {
//     val q = new AsyncQueue[Int]

//     val exc = new Exception
//     q.fail(exc)

//     q.offer(1)
//     assert(q.poll().poll === Some(Throw(exc)))
// =======

import org.scalatest.WordSpec

import com.twitter.util.{Return, Throw}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AsyncQueueTest extends WordSpec {
  "AsyncQueue" should {
    val q = new AsyncQueue[Int]
    val exc = new Exception("sad panda")

    "queue pollers" in {
      val p0 = q.poll()
      val p1 = q.poll()
      val p2 = q.poll()

      assert(p0.isDefined === false)
      assert(p1.isDefined === false)
      assert(p2.isDefined === false)

      q.offer(1)
      assert(p0.poll === Some(Return(1)))
      assert(p1.isDefined === false)
      assert(p2.isDefined === false)

      q.offer(2)
      assert(p1.poll === Some(Return(2)))
      assert(p2.isDefined === false)

      q.offer(3)
      assert(p2.poll === Some(Return(3)))
    }

    "queue offers" in {
      q.offer(1)
      q.offer(2)
      q.offer(3)

      assert(q.poll().poll === Some(Return(1)))
      assert(q.poll().poll === Some(Return(2)))
      assert(q.poll().poll === Some(Return(3)))
    }

    "into idle state and back" in {
      q.offer(1)
      assert(q.poll().poll === Some(Return(1)))

      val p = q.poll()
      assert(p.isDefined === false)
      q.offer(2)
      assert(p.poll === Some(Return(2)))

      q.offer(3)
      assert(q.poll().poll === Some(Return(3)))
    }

    "fail pending and new pollers" in {
      val p0 = q.poll()
      val p1 = q.poll()

      assert(p0.isDefined === false)
      assert(p1.isDefined === false)

      q.fail(exc)
      assert(p0.poll === Some(Throw(exc)))
      assert(p1.poll === Some(Throw(exc)))

      assert(q.poll().poll === Some(Throw(exc)))
    }

    "fail doesn't blow up offer" in {
      q.fail(exc)
      q.offer(1)
      assert(q.poll().poll === Some(Throw(exc)))
    }
// >>>>>>> 4b78f69d0b5ea50e63f197cafcf46d16335f74b6
  }
}
