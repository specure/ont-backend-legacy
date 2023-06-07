package at.alladin.rmbt.controlServer.OffOnNet.controller;

import at.alladin.rmbt.controlServer.OffOnNet.service.MeasurementServerService;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import at.alladin.rmbt.controlServer.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class MeasureMeasurementServerControllerResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(MeasureMeasurementServerControllerResource.class);

    @Get()
    public String request() {

        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();
        MeasurementServerService service = new MeasurementServerService();
        addAllowOrigin();

        if (getQuery().getFirstValue("id") != null) {
            try {
                int uid = Integer.parseInt(getQuery().getFirstValue("id"));
                answer.put("measure", service.measurement(uid));
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
