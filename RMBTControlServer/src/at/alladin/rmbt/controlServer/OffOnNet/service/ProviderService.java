package at.alladin.rmbt.controlServer.OffOnNet.service;

import at.alladin.rmbt.controlServer.OffOnNet.model.ProviderModel;
import at.alladin.rmbt.controlServer.OffOnNet.repository.ProviderPGRepository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ProviderService extends NetTestService {
    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);
    private ProviderPGRepository repository;
    public ProviderService() throws IllegalStateException{
        repository = new ProviderPGRepository(connection);
    }
    public List<ProviderModel> getAllProvidersLimitedTo(int limit) throws SQLException {
        return repository.getAllProvidersLimitedTo(limit);
    }

    public ProviderModel getByName(String providerName) throws JSONException, SQLException {
        List<ProviderModel> providers = repository.getByName(providerName);
        return checkForUniqueAndReturnOne(providers);
    }

    public ProviderModel getByShortName(String providerShortName) throws JSONException, SQLException {
        List<ProviderModel> providers = repository.getByShortName(providerShortName);
        return checkForUniqueAndReturnOne(providers);
    }
    public ProviderModel findByAnyName(String someName) throws JSONException, SQLException {
        try {
            // start with provider name
            return getByName(someName);
        } catch (JSONException e) {
            // second attempt, may be it's a shortName ?
            return getByShortName(someName);
        }
    }
    private ProviderModel checkForUniqueAndReturnOne(List<ProviderModel> providers) throws JSONException {
        if (providers.size() > 1) {
            throw new JSONException("there were find more then one provider");
        } else if (providers.size() == 0) {
            throw new JSONException("there was not find provider");
        }
        return providers.get(0);
    }
}
