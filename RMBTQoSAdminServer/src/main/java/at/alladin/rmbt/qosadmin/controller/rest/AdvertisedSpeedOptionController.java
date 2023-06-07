package at.alladin.rmbt.qosadmin.controller.rest;

import at.alladin.rmbt.qosadmin.model.AdvertisedSpeedOption;
import at.alladin.rmbt.qosadmin.repository.AdvertisedSpeedOptionRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/advertised_speed_option*")
public class AdvertisedSpeedOptionController extends AbstractRestController<AdvertisedSpeedOptionRepository, AdvertisedSpeedOption, Long> {

    public AdvertisedSpeedOptionController() {
        super(AdvertisedSpeedOption.class);
    }

}
