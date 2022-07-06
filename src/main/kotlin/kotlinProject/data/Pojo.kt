package kotlinProject.data

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

data class Transaction(
    val datetime : String,
    val amount : Double
)

@Document("Transaction")
data class TransactionDocument(
    @Field(name="datetime")
    @Indexed
    val datetime : Date,
    @Field(name="amount")
    val amount : Double
)

data class TransactionWithSum(
    val datetime : Date,
    val amount : Double,
    val sumAmount : Double
)

data class HistoryInput(
    val startDatetime : String,
    val endDatetime : String
)