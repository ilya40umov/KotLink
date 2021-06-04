package org.kotlink.core.exposed

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.VarCharColumnType

private class PsqlRegexpOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "~*")

infix fun <T : String?> ExpressionWithColumnType<T>.psqlRegexp(pattern: String): Op<Boolean> =
    PsqlRegexpOp(this, QueryParameter(pattern, columnType))

private class FullTextSearchOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@@")

private class ToTsVectorOp<T>(private val expr: ExpressionWithColumnType<T>) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("to_tsvector('simple', ")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(")")
    }
}

private class ToTsQueryOp(private val ftsQuery: String) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("to_tsquery('simple', ")
        QueryParameter(ftsQuery, VarCharColumnType()).toQueryBuilder(queryBuilder)
        queryBuilder.append(")")
    }
}

infix fun <T : String?> ExpressionWithColumnType<T>.fullTextQuery(ftsQuery: String): Op<Boolean> =
    FullTextSearchOp(ToTsVectorOp(this), ToTsQueryOp(ftsQuery))