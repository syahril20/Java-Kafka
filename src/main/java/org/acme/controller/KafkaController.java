package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.models.KafkaModel;
import org.acme.services.KafkaServices;
import org.acme.util.SimpleResponse;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Path("/kafka-out")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KafkaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaController.class.getName());

    List<KafkaModel> list = new ArrayList<>();

    @Inject
    @Channel("kafka-out")
    Emitter<KafkaModel> emitter;

    @Inject
    KafkaServices kafkaServices;


    @GET
    public SimpleResponse getText() {
        return new SimpleResponse(200L, "SUCCESS", list);
    }

    @Incoming("kafka-in")
    public void addList(KafkaModel param) {
        list.add(param);
    }

    @POST
    public SimpleResponse addJsonData(KafkaModel param) {
        emitter.send(param);
        return new SimpleResponse(200L, "SUCCESS", param);
    }

    @POST
    @Path("/uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public SimpleResponse uploadFatCode(@MultipartForm MultipartFormDataInput form){
        return kafkaServices.uploadFile(form);
    }


}