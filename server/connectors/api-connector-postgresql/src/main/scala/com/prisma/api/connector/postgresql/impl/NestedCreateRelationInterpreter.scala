package com.prisma.api.connector.postgresql.impl

import com.prisma.api.connector.NestedCreateRelation
import slick.dbio.{Effect, NoStream}
import slick.sql.{SqlAction, SqlStreamingAction}

case class NestedCreateRelationInterpreter(mutaction: NestedCreateRelation) extends NestedRelationInterpreterBase {
  override def path        = mutaction.path
  override def project     = mutaction.project
  override def topIsCreate = mutaction.topIsCreate

  override def requiredCheck: List[SqlStreamingAction[Vector[String], String, Effect]] = topIsCreate match {
    case false =>
      (p.isList, p.isRequired, c.isList, c.isRequired) match {
        case (false, true, false, true)   => requiredRelationViolation
        case (false, true, false, false)  => noCheckRequired
        case (false, false, false, true)  => List(checkForOldChild)
        case (false, false, false, false) => noCheckRequired
        case (true, false, false, true)   => noCheckRequired
        case (true, false, false, false)  => noCheckRequired
        case (false, true, true, false)   => noCheckRequired
        case (false, false, true, false)  => noCheckRequired
        case (true, false, true, false)   => noCheckRequired
        case _                            => sysError
      }

    case true =>
      noCheckRequired
  }

  override def removalActions: List[SqlStreamingAction[Vector[Int], Int, Effect]#ResultAction[Int, NoStream, Effect]] = topIsCreate match {
    case false =>
      (p.isList, p.isRequired, c.isList, c.isRequired) match {
        case (false, true, false, true)   => requiredRelationViolation
        case (false, true, false, false)  => List(removalByParent)
        case (false, false, false, true)  => List(removalByParent)
        case (false, false, false, false) => List(removalByParent)
        case (true, false, false, true)   => noActionRequired
        case (true, false, false, false)  => noActionRequired
        case (false, true, true, false)   => List(removalByParent)
        case (false, false, true, false)  => List(removalByParent)
        case (true, false, true, false)   => noActionRequired
        case _                            => sysError
      }

    case true =>
      noActionRequired
  }

  override def addAction: List[SqlAction[Int, NoStream, Effect]] = createRelationRow
}
