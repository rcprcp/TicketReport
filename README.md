# Ticket Report

This program requests tickets by org name from Zendesk tickets for, and generates a customer-consumable JSON report with the Org Name, a List of tickets for the Org, and a list of the customer-facing comments for each ticket.

### Developer notes 
You will need the StreamSets SupportLibrary which is in a StreamSets internal (private) repo.

Then: 
`git clone http://github.com/rcprcp/TicketReport.git`

cd into the directory then:

`mvn clean package`

You will need AWS credentials, with the correctl roles, as the Zendesk Name and Password are pulled from AWS Secret Manager.
set these environment variables:

`export SECRET_NAME=<secret>`

`export AWS_REGION=<region>`

Run the command with a command line switch:

`--org <orgName>`

The orgName must be unique. 

### Notes for people reading the JSON output files.
* Currently, the progam creates an output file with a dedicated name - output.json
* The program creates one json document, containing
    * A few header fields
    * An array of tickets 
    * Each ticket has an array of comments
* Times are specified as epoch in milliseconds
