package com.amanDB.ClusterDB.schemaValidators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

public class SchemaValidator {

    private String schemaPath; //Schemas/DBsSchema

    public enum JsonObjectType {
        DATABASE,
        COLLECTION,
        Document
    }

    public SchemaValidator(JsonObjectType jsonObjectType) {
        this.schemaTypeSelector(jsonObjectType);
    }


    private void schemaTypeSelector(JsonObjectType type){
        if(type == JsonObjectType.DATABASE){
            schemaPath =  "Schemas/DBsSchema";
        }else if( type == JsonObjectType.COLLECTION){
            schemaPath =  "Schemas/CollectionsSchema";
        } else {
            schemaPath =  "Schemas/DocumentsSchema";
        }
    }
    public static InputStream inputStreamFromClasspath(String path)
            throws IOException {
        File initialFile = new File(path);
        return Files.newInputStream(initialFile.toPath());
    }


    public boolean  validateJson(String jsonObject,String schemaPath) {
        // create instance of the ObjectMapper class
        ObjectMapper objectMapper = new ObjectMapper();

        // create an instance of the JsonSchemaFactory using version flag
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V201909 );
        try{
//         filePath =  "Schemas/JsonDBObject";
        InputStream jsonStream = new ByteArrayInputStream(jsonObject.getBytes());;
        InputStream schemaStream = inputStreamFromClasspath( schemaPath );

        // store the JSON data in InputStream


            // read data from the stream and store it into JsonNode
            JsonNode json = objectMapper.readTree(jsonStream);

            // get schema from the schemaStream and store it into JsonSchema
            JsonSchema schema = schemaFactory.getSchema(schemaStream);

            // create set of validation message and store result in it
            Set<ValidationMessage> validationResult = schema.validate( json );

            // show the validation errors
            if (validationResult.isEmpty()) {

                System.out.println( "There is no validation errors" );
                return true;

            } else {
                // show all the validation error
                String validationErrors= "Invalid Json Object: \n";
                validationResult.forEach(vm -> validationErrors.concat( "\n"+ vm.getMessage() ));
                throw new Exception(validationErrors);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}