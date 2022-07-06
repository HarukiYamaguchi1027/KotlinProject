package kotlinProject.repository

import kotlinProject.data.TransactionDocument
import kotlinProject.data.TransactionWithSum
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TransactionRepository : MongoRepository<TransactionDocument, String> {
    // this query returns all transactions whose datetime is less than or equal to the given timestamp,
    // attached with aggregated sum (sum of amount of all transactions before and the given transaction, ordered by datetime)
    @Aggregation(pipeline = ["{\$match:{datetime:{\$lte:?0}}}", "{\$setWindowFields:{sortBy:{datetime:1},output:{sumAmount:{\$sum:\"\$amount\",window:{documents:[\"unbounded\",\"current\"]}}}}}"])
    fun getTransactionsWithSum(endDatetime: Date) : List<TransactionWithSum>
}