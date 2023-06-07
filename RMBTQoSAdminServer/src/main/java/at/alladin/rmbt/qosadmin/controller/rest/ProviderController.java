package at.alladin.rmbt.qosadmin.controller.rest;

import at.alladin.rmbt.qosadmin.model.Provider;
import at.alladin.rmbt.qosadmin.repository.ProviderRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/providers*")
public class ProviderController extends AbstractRestController<ProviderRepository, Provider, Long> {

    public ProviderController() {
        super(Provider.class);
    }

}
