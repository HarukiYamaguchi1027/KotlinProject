package kotlinProject

import kotlinProject.data.HistoryInput
import kotlinProject.data.Transaction
import kotlinProject.data.TransactionDocument
import kotlinProject.service.TransactionService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@SpringBootApplication
class KotlinProjectApplication

fun main(args: Array<String>) {
	runApplication<KotlinProjectApplication>(*args)
}

@RestController
class TransactionController(val transactionService: TransactionService) {
	/**
	 * Return a history of sum of amounts every hour within the given time frame
	 */
	@GetMapping("/getHistory")
	fun getHistory(@RequestBody historyInput: HistoryInput) : ResponseEntity<List<Transaction>> {
		try {
			// validate if the given date time inputs have proper formats
			val startZonedDateTime = ZonedDateTime.parse(historyInput.startDatetime)
			val endZonedDateTime = ZonedDateTime.parse(historyInput.endDatetime)
			return transactionService.getHistory(startZonedDateTime, endZonedDateTime)
		} catch (e: Exception) {
			return ResponseEntity(HttpStatus.BAD_REQUEST)
		}
	}

	/**
	 * Process and store the given transaction
	 */
	@PostMapping("/save")
	fun saveTransaction(@RequestBody transaction: Transaction) : ResponseEntity<TransactionDocument> {
		try {
			// validation of datetime is done here
			// since it wasn't specified in description, amount <= 0 is accepted here
			val zonedDateTime = ZonedDateTime.parse(transaction.datetime)
			return transactionService.saveTransaction(zonedDateTime, transaction.amount)
		} catch (e: Exception) {
			return ResponseEntity(HttpStatus.BAD_REQUEST)
		}
	}
}
