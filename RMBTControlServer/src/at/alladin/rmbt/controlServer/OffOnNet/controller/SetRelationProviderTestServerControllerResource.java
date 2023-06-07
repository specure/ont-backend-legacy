package at.alladin.rmbt.controlServer.OffOnNet.controller;

import at.alladin.rmbt.controlServer.OffOnNet.model.MeasurementServerModel;
import at.alladin.rmbt.controlServer.OffOnNet.model.ProviderModel;
import at.alladin.rmbt.controlServer.OffOnNet.service.MeasurementServerService;
import at.alladin.rmbt.controlServer.OffOnNet.service.ProviderService;
import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SetRelationProviderTestServerControllerResource extends ServerResource {
    private static final Logger logger = LoggerFactory.getLogger(SetRelationProviderTestServerControllerResource.class);

    @Post("json")
    public String request(final String entity) {
        logger.debug("POST request admin/setRelationProviderTestServer: " + entity);
        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();
        addAllowOrigin();
        MeasurementServerService service = new MeasurementServerService();
        ProviderService providerService = new ProviderService();

        try {
            JSONObject request = new JSONObject(entity);
            String webAddress = request.getString("webAddress");
            MeasurementServerModel server = service.getMeasurementServerByWebAddress(webAddress);
            String providerName = request.getString("providerName");
            ProviderModel provider = providerService.findByAnyName(providerName);

            server.setProviderId(provider.getUid());
            server.setCountry(request.getString("country"));
            server.setContactEmail(request.getString("contactEmail"));
            server.setContactName(request.getString("contactName"));

            service.updateMeasurementServer(server);

        } catch (JSONException | SQLException e) {
            logger.error(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addError("ERROR_DB_GET_SERVER");
            answer.putOpt("error", errorList.getList());
            logger.error("response: " + answer.toString());
            return answer.toString();
        }
        service.closeConnection();
        providerService.closeConnection();

        answer.put("error", errorList.getList());
        logger.error("admin/setRelationProviderTestServer response: " + answer.toString());
        return answer.toString();
    }
}
