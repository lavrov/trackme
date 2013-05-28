package test

import play.api.test._
import play.api.test.Helpers._

object TestHelper {
  def fakeApp[T](block: => T) = running(FakeApplication(additionalConfiguration = inMemoryDatabase()))(block)
}
