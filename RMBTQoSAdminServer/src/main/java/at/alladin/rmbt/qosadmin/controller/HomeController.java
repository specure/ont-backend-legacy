package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.model.TestDescription;
import at.alladin.rmbt.qosadmin.repository.TestDescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles requests for the application home page.
 *
 * @author alladin-IT (lb@alladin.at)
 */
@Controller
public class HomeController {

    protected static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    TestDescriptionRepository testDescriptionRepository;

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        logger.info("Welcome home! The client locale is {}.", locale);

        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);

        List<TestDescription> descList = testDescriptionRepository.findAll();
        model.addAttribute("descList", descList);

        return "home";
    }


    @RequestMapping(value = "/list/ngview", method = RequestMethod.GET)
    public String ngView(Locale locale, Model model) {
        return "/list/ngview";
    }


}
