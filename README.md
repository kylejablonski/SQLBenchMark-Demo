# SQLBenchMark-Demo
Bench mark demo for SQLite using a cities json file of 23018 cities across the world

Please use this as a reference for inserting into android SqliteDatabase (all tests run on Samsung Galaxy S7).

We are going to attempt 4 different insert operations:
1. ContentValues
2. Prepared statements
3. ContentValues w/ Transactions
4. Prepared statements w/ Transactions

### Inserting via ContentValues

This is slowest but easiest way to integrate saving items in your database. Android gives you a class called `ContentValues` which you can
use to add values into the database. 

Sample usage (with cities):

```java
      ContentValues contentValues = new ContentValues();
      contentValues.put(CitiesContracts.KEY_NAME, city.name);
      contentValues.put(CitiesContracts.KEY_COUNTRY, city.country);
      contentValues.put(CitiesContracts.KEY_SUB_COUNTRY, city.subCountry);
      contentValues.put(CitiesContracts.KEY_GEO_NAME_ID, city.geoNameId);
      db.insert(CitiesContracts.TABLE_NAME, null, contentValues); // insert function from SQLiteDatabase class
```
**Results**: 
on 23K items this operation takes roughly *50000ms+*

### Insert via Prepared Statement

We will use the following insert Statement via a SQLiteStatement:

```java
      public static final String SQL_INSERT = "INSERT INTO "+ TABLE_NAME + " ( "
            + CitiesContracts.KEY_NAME + ", "
            + CitiesContracts.KEY_COUNTRY + ", "
            + CitiesContracts.KEY_SUB_COUNTRY + ", "
            + CitiesContracts.KEY_GEO_NAME_ID
            + " ) VALUES (?, ?, ? , ?)";
```

Sample usage:

```java
      SQLiteDatabase db = getWritableDatabase();
      SQLiteStatement stmtInsert = db.compileStatement(CitiesContracts.SQL_INSERT);

      for (CityResponse.City city : cityList) {
          stmtInsert.clearBindings();
          stmtInsert.bindString(1, city.name);
          stmtInsert.bindString(2, city.country);
          stmtInsert.bindString(3, city.subCountry);
          stmtInsert.bindString(4, city.geoNameId);
          stmtInsert.executeInsert();
      }
```

**Results**: 
on 23K items this takes roughly *48000ms+*
While this is better than ContentValues this is still not a great improvement... lets dig deeper

Now that we know there are two ways to insert data into our DB (in this example at least...), let us take a look at the
work horse of our Database operations, Transactions.

## Transactions
So, we saw before when we insert using ContentValues it takes quite a bit of time. Let us see how transactions can improve this!

### Inserting via ContentValues using transactions:

Sample usage (with cities):

```java
      SQLiteDatabase db = getWritableDatabase();
      try{
          db.beginTransaction();

          for(CityResponse.City city: cityList){

              ContentValues contentValues = new ContentValues();
              contentValues.put(CitiesContracts.KEY_NAME, city.name);
              contentValues.put(CitiesContracts.KEY_COUNTRY, city.country);
              contentValues.put(CitiesContracts.KEY_SUB_COUNTRY, city.subCountry);
              contentValues.put(CitiesContracts.KEY_GEO_NAME_ID, city.geoNameId);

              db.insert(CitiesContracts.TABLE_NAME, null, contentValues);
          }

          db.setTransactionSuccessful();
      }catch(Exception ex){

      }finally{
          db.endTransaction();
      }
```
**Results**:
on 23K items this operation takes roughly *1500ms+*
As you can note this is a dramatic difference between not using Transactions and using transactions. But we can do better!


### Insert via Prepared Statement with transactions (using the same SQL_INSERT String from above):

```java
      SQLiteDatabase db = getWritableDatabase();
      SQLiteStatement stmtInsert = db.compileStatement(CitiesContracts.SQL_INSERT);
      try{
          db.beginTransaction();

          for (CityResponse.City city : cityList) {
              stmtInsert.clearBindings();
              stmtInsert.bindString(1, city.name);
              stmtInsert.bindString(2, city.country);
              stmtInsert.bindString(3, city.subCountry);
              stmtInsert.bindString(4, city.geoNameId);
              stmtInsert.executeInsert();
          }

          db.setTransactionSuccessful();
      }catch(Exception ex){
          // .. handle your exceptions
      }finally{
          db.endTransaction();
      }
```

**Results**:
on 23K items this takes roughly *800ms+*
Wow! we are already improving our code dramatically!

So there it is, we have optimized our inserts by walking through our options.

Some operations here may make sense or time may not matter depending on the use case, but if we want the fastest inserts we MUST
use the Prepared Statements with transactions!

Good Luck and Happy Coding!
