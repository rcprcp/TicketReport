# Ticket Report

This program requests tickets by org name from Zendesk tickets for, and generates one of the following:
* a customer-consumable JSON report with the Org Name, a List of tickets for the Org, and a list of the customer-facing comments for each ticket.
* a customer-consumable PDF report, in 1 file, that contains the same information as above.

### Developer notes 
You will need the StreamSets SupportLibrary which is in a StreamSets internal (private) repo.
You will need this jar - 
```xml
    <dependency>
      <groupId>com.itextpdf</groupId>
      <artifactId>itextpdf</artifactId>
      <version>${itextpdf.version}</version>
    </dependency>
```

It is easiest to install this on your local Mac with thie command: 
```shell
mvn dependency:get 
  -DremoteRepositories=https://mvnrepository.com/artifact  
  -DgroupId=com.itextpdf 
  -DartifactId=itextpdf 
  -Dversion=5.5.13.3 
  -Dtransitive=true
```

Then: 
`git clone http://github.com/rcprcp/TicketReport.git`

cd into the directory then:

`mvn clean package`

You will need AWS credentials, with the correctl roles, as the Zendesk Name and Password are pulled from AWS Secret Manager.
set these environment variables:

`export SECRET_NAME=<secret>`

`export AWS_REGION=<region>`

Run the command with a command line switches:

`--org <orgName> --pdf`

If --pdf is not present the program creates a JSON report.

The orgName must be unique. 

### Notes for people reading the JSON output files.
* Currently, the progam creates an output file with a dedicated name - output.json
* The program creates one json document, containing
    * A few header fields
    * An array of tickets 
    * Each ticket has an array of comments
* Times are specified as epoch in milliseconds

### Notes for the PDF output files. 
* the file name is output.pdf
* This is one document.
   * there is a page break after each ticket 
   * there is horizontal line after each comment 
   * Timestamps are in the local machine's time.  eg. -0400 for US EDT.  This is easy to change with this JVM argument argument:  -user.timezone=UTC  or CEST 
