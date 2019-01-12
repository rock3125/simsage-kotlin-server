/*
 * Copyright (c) 2019 by Peter de Vocht
 *
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
 *
 */

package simsage.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


/**
 * Created by peter on 8/04/16.
 */
@CrossOrigin(origins = arrayOf("*"))
@RestController
@RequestMapping("/")
class Query {

    private val logger = LoggerFactory.getLogger(Query::class.java)

    // the ids are set in simsage.properties, these values must be set
    // see: https://simsage.nz/api.html
    @Value("\${simsage.organisation.id}")
    lateinit var organisationId: String

    @Value("\${simsage.kb.id}")
    lateinit var kbId: String

    @Value("\${simsage.security.id}")
    lateinit var securityId: String



    class Action {
        var action = ""
        var parameters = ArrayList<String>()
    }

    class JavascriptQuery {
        var customerId = ""  // // your customer's id (session management, the session tie)
        var query = ""  // English language query
    }

    class SimSageQuery(var query: String, var organisationId: String, var kbId: String) {
        var numResults = 10
        var scoreThreshold = 0.5f
    }

    class QueryResult {
        var url = ""  // result origin
        var score = 0.0f;  // query relevance, 1.0 == 100%
        var actionList = ArrayList<Action>()  // list of parameters to this action
    }

    class SimSageResponseObject {
        var sessionId = ""; // the user's session, same as the customerId (context related)
        var jobId = ""; // job identifier
        var organisationId = ""; // the organisation
        var kbId = ""; // the kb to query
        var email = ""; // the user / owner
        var query = ""; // the query asked
        var scoreThreshold = 0.01f; // the score threshold used / specified
        var numResults = 10; // the number of results to return
        var errorStr = ""; // any error reporting from the query engine
        var error = ""; // any error reporting from the main api
        var queryResultList = ArrayList<QueryResult>() ; // the result set
        var contextStack = ArrayList<String>();    // stack of the states, most recent at the top
        var context = HashMap<String, String>(); // user's context
    }

    /**
     * post a query to SimSage and return the response to our javascript client
     * @return a SimSage query result list or an error message
     */
    @RequestMapping(value = arrayOf("/query"), produces = arrayOf("application/json"), method = arrayOf(RequestMethod.POST))
    fun query(@RequestBody parameters: JavascriptQuery): ResponseEntity<*> {

        try {
            if (organisationId.isEmpty() || organisationId == "?") {
                return error("please set organsiation id and the other values before running this server.")
            }

            if (parameters.query.trim().isEmpty() || parameters.customerId.trim().isEmpty()) {
                return error("invalid parameter(s)")
            }

            val httpclient = HttpClients.createDefault()
            val httpPost = HttpPost("https://cloud.simsage.nz/api/query/${parameters.customerId}")
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("Security-Id", securityId)
            httpPost.setHeader("API-Version", "1")
            httpPost.setEntity(StringEntity(ObjectMapper().writeValueAsString(SimSageQuery(parameters.query, organisationId, kbId))))
            // post
            val httpResponse = httpclient.execute(httpPost)
            if (httpResponse.statusLine.statusCode != 200) {
                return error(httpResponse.toString())
            }
            val simSageData = retrieveResourceFromResponse(httpResponse, SimSageResponseObject::class.java)
            // return query result list - all our client is interested in
            return jsonObject(simSageData.queryResultList)

        } catch (ex: Exception) {
            return error(ex.toString())
        }
    }


    /**
     * return an error message in a simsage response object
     */
    private fun error(errStr: String) : ResponseEntity<*> {
        val response = SimSageResponseObject()
        response.error = errStr
        return jsonObject(response)
    }

    /**
     * return a json object
     * @param item the object to return
     */
    private fun <T> jsonObject(item: T): ResponseEntity<*> {
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_JSON
        return ResponseEntity(item, responseHeaders, HttpStatus.OK)
    }

    /**
     * deserialize an object from the http reponse of type T
     */
    private fun <T> retrieveResourceFromResponse(response: HttpResponse, clazz: Class<T>): T {
        val jsonFromResponse = EntityUtils.toString(response.entity)
        val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(jsonFromResponse, clazz)
    }

}

