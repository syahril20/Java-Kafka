package org.acme.services;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.acme.controller.KafkaController;
import org.acme.models.KafkaModel;
import org.acme.util.SimpleResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class KafkaServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaServices.class.getName());

    @Inject
    @Channel("kafka-out")
    Emitter<KafkaModel> emitter;

    public static String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    @Transactional
    public SimpleResponse uploadFile(MultipartFormDataInput form) {
        final String id = "id";
        final String name = "name";
        try {
            Map<String, List<InputPart>> uploadForm = form.getFormDataMap();
            List<InputPart> inputParts = uploadForm.get("file");
            InputPart inputPart = inputParts.get(0);
            MultivaluedMap<String, String> header = inputPart.getHeaders();
            String fileName = getFileName(header);
            InputStream inputStream = inputPart.getBody(InputStream.class, null);

            int fileIndex = fileName.lastIndexOf(".");
            if (!Pattern.compile("(?:xlsx|xls)").matcher(fileName.substring(fileIndex + 1).toLowerCase()).find()) {
                return new SimpleResponse(400L, "FAILED", "");
            }

            XSSFSheet sheet = new XSSFWorkbook(inputStream).getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                for (Cell cell : row) {
                    headers.add(cell.getStringCellValue());
                }
            }
            List<KafkaModel> listData = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                KafkaModel data = new KafkaModel();

                data.id = (int) row.getCell(headers.indexOf(id)).getNumericCellValue();
                data.name = row.getCell(headers.indexOf(name)).getStringCellValue();


                listData.add(data);
                emitter.send(data);
            }
//            UserModels.persist(listData);
//            JsonObject result = new JsonObject();
//            result.put("status", 200);
//            result.put("message", "SUCCESS");
//            result.put("payload", listData);
//            emitter.send(listData);
            return new SimpleResponse(200L, "SUCCESS", listData);

        } catch (Exception e) {
            return new SimpleResponse(400L, e.getMessage(), "");
        }
    }
}
