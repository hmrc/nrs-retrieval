# nrs-retrieval

This is a service providing an MDTP proxy to the nonrep-retrieval API on AWS.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")


### Running the application

In order to run the microservice, you must have SBT installed. You should then be able to start the application using:

> ```sbt run {PORT}```

> The port used for this project is 9391

> To run the tests for the application, you can run: ```sbt test```
> or to view coverage run: ```sbt coverage test coverageReport```

> Landing page URL for the service is ```https://{HOST:PORT}/nrs-retrieval/start```

### Running the application using Service Manager

In order to run the application and all of it's dependencies using service manager, you must have service manager installed.
You should then be able to start teh application using:

> ```sm --start NRS_RETRIEVAL_ALL -f```

### Test-only endpoints

There is a test-only endpoint that tests whether the request is authorised with one of the `stride` enrolments 
{`nrs_digital_investigator`, `nrs digital investigator`}

`GET /nrs-retrieval/test-only/check-authorisation`

If the request is authenticated by `stride` and has the enrolment `nrs_digital_investigator` then `200 OK` is returned

Else if the request is authenticated by `stride` but does not have the enrolment `nrs_digital_investigator` then `403 FORBIDDEN` is returned

Else If the request is not authenticated by `stride` then `401 UNAUTHORIZED` is returned

To enable this endpoint run the service using the `./run-with-test-only-endpoints.sh` script.
