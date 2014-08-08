/**
 * Created by samueldoyle on 6/29/13.
 * Sample CallBack
 *
 *  The following fields are injected via the binding
 *  requestMethod: The method used in the request GET/POST
 *  restRequest: The path used for the request
 *  jsonResponseData: The jsonDataObject returned from the response
 *  // The following added in case just the response data isn't enough
 *  restClient: The rest client object itself
 *  returnedResponse: The response object itself
 */

import groovy.json.*

DEBUG = true

def logBindings() {
    assert requestMethod && restRequest && jsonResponseData
    def jsonString = JsonOutput.toJson(jsonResponseData)
    def msg = $/
        METHOD:           $requestMethod
        REQUEST:          $restRequest
        RESPONSE STATUS:  $returnedResponse.statusCode
        RESPONSE:         ${JsonOutput.prettyPrint(jsonString)}
    /$
    println msg
}

if (DEBUG) logBindings()
