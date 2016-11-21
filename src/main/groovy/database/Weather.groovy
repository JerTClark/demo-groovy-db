package database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.MarkupBuilder

/**
 * Various ways to add something to the classpath other than Gradle...
 * 1.) this.class.classLoader.rootLoader.addURL(
 *          new URL("file:///C:/PATH-TO/mysql-connector-java-5.1.x.jar"))
 * 2.) Annotate with
 *      @GrabConfig(systemClassLoader=true)
 *      @Grab('mysql:mysql-connector-java:5.1.x')
 * 3.) Put the mysql-connector-java-5.1.x.jar in %USER_HOME%/.groovy/lib
 */
class Cred {
    /**These are example values*/
    String ip = "10.123.123.123"
    /**Must be authorized to use the server from the local machine*/
    String username = "YourMySqlUserName"
    /**Horrible idea...*/
    String password = "YourMySqlPassword"
}

def cred = new Cred()

def sql = Sql.newInstance("jdbc:mysql://" + cred.ip + ":3306/weatherinfo",
        cred.username, cred.password, "com.mysql.jdbc.Driver")

println sql.connection.catalog

sql.eachRow("select * from weather") {
    printf("%-20s%s\n", it.city, it.temperature)
}

processMetadata = { metaData ->
    metaData.columnCount.times { i ->
        printf("%-20s", metaData.getColumnLabel(i + 1))
    }
    println ""
}

sql.eachRow("select * from weather", processMetadata) {
    printf("%-20s%s\n", it.city, it.temperature)
}

ArrayList<GroovyRowResult> results = sql.rows("select * from weather")
println "We have weather info for ${results.size()} cities"

new FileWriter("weather.xml").withWriter { writer ->
    builder = new MarkupBuilder(writer)
    builder.mkp.xmlDeclaration(version:"1.0")
    builder.weather {
        sql.eachRow("select * from weather") {
            city(name: it.city, temperature: it.temperature)
        }
    }
}

dataSet = sql.dataSet("weather")
citiesBelowFreezing = dataSet.findAll {it.temperature < 32}
sleep(500)
println "Cities below freezing..."
citiesBelowFreezing.each {
    println "${it.city}"
}

dataSet.add(city: "Gainesville", temperature: 78)
dataSet.add(city: "Warrenton", temperature: 80)

temperature = 50

sql.executeInsert("""insert into weather (city, temperature)
values ('Oklahoma City', $temperature)""")

sql.eachRow("select * from weather") {
    printf("%-20s%s\n", it.city, it.temperature)
}