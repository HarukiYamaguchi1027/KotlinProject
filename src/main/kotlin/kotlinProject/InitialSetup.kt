package kotlinProject

import com.mongodb.MongoCommandException
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.TimeSeriesGranularity
import com.mongodb.client.model.TimeSeriesOptions
import org.bson.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import java.util.Date
import javax.annotation.PostConstruct

@SpringBootApplication
/**
 * This class is for creating time series in mongo db and inserting one transaction record with amount = 1000
 * should be run only once before processing KotlinProjectApplication
 */
class InitialSetup
{
    @Value("\${spring.data.mongodb.uri}")
    val uri: String = ""
    @Value("\${spring.data.mongodb.database}")
    val database: String = ""

    @PostConstruct
    fun getStoreInfo(): Unit {
        setupTimeSeries(uri, database)
    }

    fun setupTimeSeries(uri : String, database: String) {
        var mongodb : MongoDatabase? = null
        try {
            mongodb = MongoClients.create(uri).getDatabase(database)
            val tsOptions = TimeSeriesOptions("datetime").metaField("amount").granularity(TimeSeriesGranularity.HOURS)
            val collOptions = CreateCollectionOptions().timeSeriesOptions(tsOptions)
            mongodb.createCollection("Transaction", collOptions)
        } catch (e : MongoCommandException) {
            // Time series already exists
        }
        // insert 'initia' 1000.0 amount
        // the datetime can be different depending on which time to set as 'initial'
        val doc = Document().append("datetime", Date(0)).append("amount", 1000.0)
        mongodb!!.getCollection("Transaction").insertOne(doc)
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinProjectApplication>(*args)
}



@ConfigurationProperties(prefix = "spring.data.mongodb")
data class MongoConfig (
    val uri: String,
    val database: String
        )
