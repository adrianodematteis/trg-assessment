TRG Assessment

Very simple and beta Apache Spark application that reads data containing the UK reported street crimes 
(from July 2018 to September 2019), extracts some relevant fields and calculates simple KPIs.

Calculated KPIs:
- Different Crimes: the list of the possible crimes 
- Crimes By District: # of crimes by district
- Crimes By Crime Type: # ot crimes by cryme type

Once extracting and storing the relevant fields and the KPIs in four different Parquet tables, 
the application starts an akka-http server that allows the user to query the data just calculated. 

All the configurations can be changed in the resources/application.conf file.

API usage (local machine deployment example) 
- Query the table containing the relevant fields:
  
  curl http://127.0.0.1:61003/trg-assessment/get-data?numRows=<numberOfRowsToBeRetrieved>
  Example: curl http://127.0.0.1:61003/trg-assessment/get-data?numRows=10
  

- Get the list of different crimes
  
  curl http://127.0.0.1:61003/trg-assessment/different-crimes


- Get the number of crimes by district:
  
  curl http://127.0.0.1:61003/trg-assessment/crimes-by-district?numRows=10

- Get the number of crimes by crime type
  
  curl http://127.0.0.1:61003/trg-assessment/crimes-by-crime-type

Run the application: 
- sbt assembly to generate the fat Jar
- run the application as a normal Spark application (client mode)



  
  

