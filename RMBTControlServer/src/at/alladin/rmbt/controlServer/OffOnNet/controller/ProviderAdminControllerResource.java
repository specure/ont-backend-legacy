package at.alladin.rmbt.controlServer.OffOnNet.controller;

import at.alladin.rmbt.controlServer.OffOnNet.model.ProviderModel;
import at.alladin.rmbt.controlServer.OffOnNet.service.ProviderService;
import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ProviderAdminControllerResource extends ServerResource {
    private static final Logger logger = LoggerFactory.getLogger(ProviderAdminControllerResource.class);

    @Get()
    public String request() {
        ErrorList errorList = new ErrorList();
        JSONObject answer = new JSONObject();
        addAllowOrigin();

        try {
            JSONArray providersAsJson = new JSONArray();

            int limit = 100;
            if (getQuery().getFirstValue("limit") != null) {
                limit = Integer.parseInt(getQuery().getFirstValue("limit"));
            }
            ProviderService service = new ProviderService();
            List<ProviderModel> providers = service.getAllProvidersLimitedTo(limit);
            service.closeConnection();

            if (!providers.isEmpty()) {
                for( ProviderModel server : providers) {
                    providersAsJson.put(server.asJsonObject());
                }
            }
            answer.put("error", errorList.getList());
            answer.put("servers", providersAsJson);

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
}
