package com.twitter.util.repository

import com.twitter.util.Future
import scala.collection.mutable

class CachingRepository[Q, E](
  map: mutable.Map[Long, Future[Option[E]]],
  index: mutable.Map[Q, Future[Seq[E]]],
  underlying: Repository[Q, E])
  extends Repository[Q, E]
{
  def apply(id: Long) = map getOrElseUpdate(id, underlying(id))
  def apply(query: Q) = index getOrElseUpdate(query, underlying(query))
}
