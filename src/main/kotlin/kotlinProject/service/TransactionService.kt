package kotlinProject.service

import kotlinProject.data.Transaction
import kotlinProject.data.TransactionDocument
import kotlinProject.data.TransactionWithSum
import kotlinProject.repository.TransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList


@Service
class TransactionService(val transactionRepository: TransactionRepository) {

    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    /**
    The logic of this method is simple:
    1. Obtain all transactions whose datetime is less than or equal to endDateTime. As explained in TransactionRepository.getTransactionsWithSum, each transaction already has sum of amounts prior to and including itself
    2. Iterate every hour between startDateTime and endDateTime, find the closest transaction (with sum) earlier than or equal to the given hour, and add new Transaction with the hour and sum amount to output list
     */
    fun getHistory(startZonedDateTime: ZonedDateTime, endZonedDateTime: ZonedDateTime) : ResponseEntity<List<Transaction>> {
        var dateTime: Date = getNextHour(startZonedDateTime.toInstant().toEpochMilli())
        val endDateTime: Date = getPrevHour(endZonedDateTime.toInstant().toEpochMilli())
        val transactionsWithSum: List<TransactionWithSum> = transactionRepository.getTransactionsWithSum(endDateTime)

        val outputList: ArrayList<Transaction> = ArrayList<Transaction>()
        var index = 0
        val calendar: Calendar = Calendar.getInstance()
        while (!dateTime.after(endDateTime)) {
            // TODO: Update this method to find flooring with binary search
            while (index < transactionsWithSum.size && !transactionsWithSum[index].datetime.after(dateTime)) {
                index++
            }
            val am: Double
            if (index == 0) { // case if the current 'dateTime' is earlier than any other transactions (including the case that no transaction exists)
                am = 0.0
            } else {
                am = transactionsWithSum[index - 1].sumAmount
            }
            outputList.add(Transaction(formatter.format(dateTime), am))
            calendar.time = dateTime
            calendar.add(Calendar.HOUR, 1)
            dateTime = calendar.time
        }

        return ResponseEntity(outputList, HttpStatus.OK)
    }

    fun saveTransaction (zonedDateTime: ZonedDateTime, amount: Double) : ResponseEntity<TransactionDocument> {
        try {
            // due to specification of time series in mongo db, datetime fields need to be stored as BSON UTC format
            val document = TransactionDocument(Date(zonedDateTime.toInstant().toEpochMilli()), amount)
            return ResponseEntity(transactionRepository.save(document), HttpStatus.CREATED)
        } catch (e: Exception) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    val hourInMillisecond = 3600000
    /**
     * Return the datetime with the most recent HH:00:00:000 or itself if it's just at HH:00:00:000
     */
    fun getPrevHour(startDateTimeInMilliseconds: Long) : Date {
        return Date ((startDateTimeInMilliseconds / hourInMillisecond) * hourInMillisecond)
    }

    /**
     * Return the datetime with the next HH:00:00:000 or itself if it's just at HH:00:00:000
     */
    fun getNextHour(startDateTimeInMilliseconds: Long) : Date {
        if (startDateTimeInMilliseconds % hourInMillisecond == 0L) {
             return Date(startDateTimeInMilliseconds)
        }
        return Date (((startDateTimeInMilliseconds / hourInMillisecond) + 1) * hourInMillisecond)
    }

    /**
     * binary search to find the transaction right before or equal at the given datetime
     */
    fun getNextIndex(transactionList: List<TransactionWithSum>, dateTime: Date, startIndex : Int, endIndex : Int): Int {
        var l = startIndex
        var h = endIndex
        while (l <= h) {
            var m = l + (h - l) / 2
            val transaction = transactionList[m]
            if (dateTime.equals(transaction.datetime)) {
                // should be optimized
                while (m < endIndex && dateTime.equals(transactionList[m + 1].datetime)) {
                    m++
                }
                return m
            }
            if (dateTime.before(transaction.datetime)) {
                h = m - 1
            } else {
                l = m + 1
            }
        }
        return if (l == startIndex) {
            startIndex - 1
        } else h
    }
}