# Ticket Metrics

This program requests tickets by user name from Zendesk and creates the following:
* JSON report 
* a customer-consumable PDF report

### Developer notes 
Then: 
`git clone http://github.com/rcprcp/TicketMetrics.git`

cd into the directory then:

`mvn clean package`

You will need environment variable for the Zendesk URL, Zendesk email and token: 

```shell
export ZENDESK_EMAIL=someone@somewhere.com
export ZENDESK_TOKEN=F4dRblahblahblahnx2DSn
export ZENDESK_URL=https://subdomain.zendesk.com
```
Run the command with a command line switches:

```shell
java -jar target/TicketMetrics-1.0-SNAPSHOT-jar-with-dependencies.jar --user "Joe Smith" --pdf
```

If --pdf is not present the program creates a JSON report.

The userName must be unique. 

### Notes for the JSON output files.
* Currently, the program creates an output file with a dedicated name - output.json
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
   * Timestamps are in the local machine's time.  eg. -0400 for US EDT.  This is easy to change with this JVM argument argument:  -user.timezone=UTC  or CEST (for example) 
