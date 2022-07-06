How to run the application:
This project uses MongoDB as data storage. Therefore, prepare MongoDB and update uri and database in src/main/resources/application.properties.
Besides, please run main method in InitialSetup.kt once (only once). It sets the storage as time series collection and adds '1000' as an initial amount, as indicated in the description (the datetime of this record is set as 1970-01-01, but can be updated if the application is expected to handle transactions with older time).
At last, main application can be launched from the main method in KotlinProjectApplication.kt.

Functionality of the application:
As directed in the description, this application has two rest services: saving records and showing history of balance every hour between given time range.
By default, the server is launched in http://localhost:8080/. The saving record is done through http://localhost:8080/save with POST, while the history is retrieved from http://localhost:8080/getHistory with GET.
The call can be done with several methods (like HttpClient in IntelliJ or Chrome extension Postman). Here are sample curl calls:
curl -X POST --location "http://localhost:8080/save" -H "Content-Type: application/json" -d "{\"datetime\":\"2019-10-05T14:48:01+01:00\",\"amount\":1.1}"
curl -X GET --location "http://localhost:8080/getHistory" -H "Content-Type: application/json" -d "{\"startDatetime\":\"2011-10-05T10:48:01+00:00\",\"endDatetime\":\"2011-10-05T18:48:02+00:00\"}"

Considerations:
With the given specification, I decided to use time series collection with MongoDB to store and query data, as it efficiently stores data in time sequence and gives easy access to query and aggregate the data in given time frame.
In addition, as the characteristic of NoSQL database, it gives better scalability in distributed system.

Another consideration was to use SQL database. One possible idea was to create another table in addition to storing record. This table uses 'hour' as key and tracks sum of amount up to the given hour. Consequently, each time the record is saved, this table also needs to be updated with all 'hour' rows after the datetime of the given record.
This idea assumes that the incoming records have the datetime close to 'now.' Otherwise, a large number of rows need to be updated (e.g., there will be 8760 rows/hours in a year). Even with the case of recent datetime, there will be a bottleneck to update the 'sum' table, as it generates a race condition to rock its rows with the recent hours.
