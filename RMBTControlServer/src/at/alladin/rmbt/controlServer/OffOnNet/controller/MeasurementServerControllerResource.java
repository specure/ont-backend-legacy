package at.alladin.rmbt.controlServer.OffOnNet.controller;

import at.alladin.rmbt.controlServer.OffOnNet.model.MeasurementServerModel;
import at.alladin.rmbt.controlServer.OffOnNet.service.MeasurementServerService;
import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class MeasurementServerControllerResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementServerControllerResource.class);

    @Get()
    public String request() {
        ErrorList errorList = new ErrorList();
        JSONObject answer = new JSONObject();
        addAllowOrigin();

        try {
            if (getQuery().getFirstValue("uid") != null) {
                int uid = Integer.parseInt(getQuery().getFirstValue("uid"));
                // return only one server
                MeasurementServerService service = new MeasurementServerService();
                MeasurementServerModel server = service.getMeasurementServerById(uid);
                service.closeConnection();
                answer.put("server", server.asJsonObject());
            } else {
                // get list of servers
                JSONArray serversAsJson = new JSONArray();
                int limit = 100;
                if (getQuery().getFirstValue("limit") != null) {
                    limit = Integer.parseInt(getQuery().getFirstValue("limit"));
                }

                MeasurementServerService service = new MeasurementServerService();
                List<MeasurementServerModel> servers = service.getAllServicesLimitedTo(limit);
                service.closeConnection();

                if (!servers.isEmpty()) {
                    for( MeasurementServerModel server : servers) {
                        serversAsJson.put(server.asJsonObject());
                    }
                }
                answer.put("servers", serversAsJson);
            }


            answer.put("error", errorList.getList());
            return answer.toString();

        } catch (JSONException | SQLException e) {
            logger.error(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addError("ERROR_DB_GET_SERVER");
            answer.putOpt("error", errorList.getList());
            logger.error("response: " + answer.toString());
            return answer.toString();
        }
    }
    @Post("json")
    public String request(final String entity) {

        logger.debug("POST request admin/measurement: " + entity);

        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();
        addAllowOrigin();
        MeasurementServerService service = new MeasurementServerService();

        try {
            JSONObject entityInRequest = new JSONObject(entity);

            MeasurementServerModel data = new MeasurementServerModel(entityInRequest);
            if (data.getUid() == 0){
                answer.put("created", service.createMeasurementServer(data).asJsonObject());
            } else {
                answer.put("updated", service.updateMeasurementServer(data).asJsonObject());
            }
        } catch (JSONException e) {
            logger.error("Error parsing JSON Data " + e.toString());
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            errorList.addError("ERROR_REQUEST_JSON");
        } catch (SQLException e) {
            logger.error(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addError("ERROR_DB_GET_SERVER");
        }

        service.closeConnection();
        answer.put("error", errorList.getList());
        logger.error("admin/measurement response: " + answer.toString());
        return answer.toString();
    }
}
