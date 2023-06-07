package at.alladin.rmbt.controlServer.OffOnNet.controller;

import at.alladin.rmbt.controlServer.OffOnNet.service.MeasurementServerService;
import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class PingMeasurementServerControllerResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(PingMeasurementServerControllerResource.class);

    @Get()
    public String request() {

        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();
        addAllowOrigin();
        MeasurementServerService service = new MeasurementServerService();

        if (getQuery().getFirstValue("id") != null) {
            try {
                int uid = Integer.parseInt(getQuery().getFirstValue("id"));

                answer.put("ping", service.pingMeasurementServer(uid));
            } catch (SQLException  e) {
                logger.error(e.getMessage());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                errorList.addError("ERROR_DB_GET_SERVER");
                answer.putOpt("error", errorList.getList());
                logger.error("response: " + answer.toString());
                return answer.toString();
            }
        }

        service.closeConnection();
        answer.put("error", errorList.getList());
        return answer.toString();
    }
}
