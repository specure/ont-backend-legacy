package at.alladin.rmbt.qosadmin.controller.rest;

import at.alladin.rmbt.qosadmin.model.AsToProvider;
import at.alladin.rmbt.qosadmin.repository.AsToProviderRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/as2providers*")
public class AsToProviderController extends AbstractRestController<AsToProviderRepository, AsToProvider, Long> {

    public AsToProviderController() {
        super(AsToProvider.class);
    }

}
