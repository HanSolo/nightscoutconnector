## NightscoutConnector

NightscoutConnector is a little library that enables you to connect to your Nightscout server incl. API_SECRET and TOKEN to retrieve blood glucose values.

It comes with sync and async methods to retrieve 
- current entry
- entries between a defined from and to date
- last given number of entries
- entries in a given interval

There are also methods to convert between mg/dl and mmol/l and vice versa.

Because the project also imports my toolbox library, you can use properties and events very similar to the ones implemented in JavaFX too.