package at.alladin.rmbt.controlServer.OffOnNet.service;

import at.alladin.rmbt.controlServer.OffOnNet.model.MeasurementServerModel;
import at.alladin.rmbt.controlServer.OffOnNet.repository.MeasurementServerPGRepository;
import at.alladin.rmbt.controlServer.OffOnNet.repository.TestServerProviderRepository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class MeasurementServerService extends NetTestService {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementServerService.class);

    private final MeasurementServerPGRepository measurementServerRepository;
    private final TestServerProviderRepository testServerProviderRepository;

    public MeasurementServerService() throws IllegalStateException{
            measurementServerRepository = new MeasurementServerPGRepository(connection);
            testServerProviderRepository = new TestServerProviderRepository(connection);
    }
    public List<MeasurementServerModel> getAllServicesLimitedTo(int limit) throws SQLException  {
        return measurementServerRepository.getAllLimitedTo(limit);
    }
    public MeasurementServerModel getMeasurementServerById(int uid) throws  SQLException {
        return measurementServerRepository.getMeasurementServerById(uid);
    }
    public MeasurementServerModel createMeasurementServer(MeasurementServerModel data) throws SQLException, JSONException {
        int providerId = getProviderIdByData(data);
        MeasurementServerModel createdServer = measurementServerRepository.createMeasurementServer(data);
        testServerProviderRepository.createProviderAndTestServerRelation(providerId, createdServer.getUid());
        return measurementServerRepository.getMeasurementServerById(data.getUid());
    }
    public MeasurementServerModel updateMeasurementServer(MeasurementServerModel data) throws SQLException {
        int providerId = getProviderIdByData(data);
        measurementServerRepository.updateMeasurementServer(data);
        testServerProviderRepository.updateProviderAndTestServerRelation(providerId, data.getUid());
        return measurementServerRepository.getMeasurementServerById(data.getUid());
    }
    public boolean pingMeasurementServer(int uid) throws SQLException {
        MeasurementServerModel server = measurementServerRepository.getMeasurementServerById(uid);
        return ping(server);
    }
    public boolean measurement(int testServerUID) throws SQLException {
        MeasurementServerModel server = measurementServerRepository.getMeasurementServerById(testServerUID);
        // TODO: make it dynamically
        String uuid = "e251df5c-0749-4242-b29c-753c7c25ed78";
        return measure(server, uuid);
    }
    private boolean ping(MeasurementServerModel server){
        return Math.random() > 0.5;
    }
    private boolean measure(MeasurementServerModel server, String uuid){
        return Math.random() > 0.5;
    }
    private int getProviderIdByData(MeasurementServerModel data) throws SQLException, JSONException {
        int providerId = data.getProviderId();
        if(providerId == 0 ) {
            ProviderService providerService = new ProviderService();
            if (data.getProviderName() != null && !data.getProviderName().isEmpty()) {
                providerId = providerService
                        .getByName(data.getProviderName())
                        .getUid();
            } else if (data.getProviderShortName() != null && !data.getProviderShortName().isEmpty()){
                providerId = providerService
                        .getByShortName(data.getProviderShortName())
                        .getUid();
            } else {
                providerService.closeConnection();
                throw new JSONException("there is no providerId, providerName, providerShortName");
            }
            providerService.closeConnection();
        }
        return providerId;
    }
    public MeasurementServerModel getMeasurementServerByWebAddress(String ip) throws SQLException, JSONException {
        List<MeasurementServerModel> servers = measurementServerRepository.getByIP(ip);
        if (servers.size() == 0) {
            throw new JSONException("there is no find test server by ip " + ip);
        }
        if (servers.size() > 1) {
            throw new JSONException("there were found more then one server by ip" + ip);
        }
        return servers.get(0);
    }
}
