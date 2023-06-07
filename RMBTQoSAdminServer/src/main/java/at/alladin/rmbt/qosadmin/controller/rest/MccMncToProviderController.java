package at.alladin.rmbt.qosadmin.controller.rest;

import at.alladin.rmbt.qosadmin.model.MccMncToProvider;
import at.alladin.rmbt.qosadmin.repository.MccMncToProvierRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/mccmnc2providers")
public class MccMncToProviderController extends AbstractRestController<MccMncToProvierRepository, MccMncToProvider, Long> {

    public MccMncToProviderController() {
        super(MccMncToProvider.class);
    }

}
